package conexion;

import com.google.gson.Gson;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

// guardamos los cambios de datos y su auditoría dentro de la misma transacción
public final class AuditoriaConexion {
    private static final Gson JSON = new Gson();
    private static final Pattern TABLA =
            Pattern.compile(
                    "(?is)^\\s*(INSERT\\s+INTO|UPDATE|DELETE\\s+FROM)\\s+`?([\\p{L}_0-9]+)`?");

    private AuditoriaConexion() {}

    public static Connection envolver(Connection db, Integer usuario) {
        return (Connection)
                Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class<?>[] {Connection.class},
                        (proxy, metodo, argumentos) -> {
                            try {
                                Object respuesta = metodo.invoke(db, argumentos);
                                if (metodo.getName().equals("prepareStatement")
                                        && argumentos[0] instanceof String sql) {
                                    Matcher m = TABLA.matcher(sql);
                                    if (usuario != null
                                            && m.find()
                                            && !m.group(2).equalsIgnoreCase("log_usuario")) {
                                        return preparar(
                                                db,
                                                (PreparedStatement) respuesta,
                                                sql,
                                                m.group(2),
                                                usuario);
                                    }
                                }
                                return respuesta;
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        });
    }

    private static PreparedStatement preparar(
            Connection db, PreparedStatement real, String sql, String tabla, int usuario) {
        Map<Integer, Object> parametros = new TreeMap<>();
        List<Map<Integer, Object>> lote = new ArrayList<>();
        return (PreparedStatement)
                Proxy.newProxyInstance(
                        PreparedStatement.class.getClassLoader(),
                        new Class<?>[] {PreparedStatement.class},
                        (proxy, metodo, argumentos) -> {
                            String nombre = metodo.getName();
                            if (nombre.startsWith("set")
                                    && argumentos != null
                                    && argumentos.length >= 2
                                    && argumentos[0] instanceof Integer indice) {
                                parametros.put(
                                        indice, nombre.equals("setNull") ? null : argumentos[1]);
                            } else if (nombre.equals("clearParameters")) parametros.clear();
                            else if (nombre.equals("addBatch")) lote.add(new TreeMap<>(parametros));
                            else if (nombre.equals("clearBatch")) lote.clear();

                            if (List.of(
                                            "executeUpdate",
                                            "executeLargeUpdate",
                                            "execute",
                                            "executeBatch",
                                            "executeLargeBatch")
                                    .contains(nombre)) {
                                boolean propio = db.getAutoCommit();
                                if (propio) db.setAutoCommit(false);
                                try {
                                    String anterior = consultar(db, sql, tabla, parametros);
                                    Object resultado = metodo.invoke(real, argumentos);
                                    boolean cambio =
                                            resultado instanceof Number n
                                                    ? n.longValue() > 0
                                                    : resultado instanceof int[] a
                                                            ? Arrays.stream(a)
                                                                    .anyMatch(
                                                                            v ->
                                                                                    v > 0
                                                                                            || v
                                                                                                    == Statement
                                                                                                            .SUCCESS_NO_INFO)
                                                            : resultado instanceof long[] a
                                                                    ? Arrays.stream(a)
                                                                            .anyMatch(
                                                                                    v ->
                                                                                            v > 0
                                                                                                    || v
                                                                                                            == Statement
                                                                                                                    .SUCCESS_NO_INFO)
                                                                    : real.getUpdateCount() > 0;
                                    if (cambio) {
                                        String accion =
                                                sql.stripLeading()
                                                        .split("\\s+", 2)[0]
                                                        .toUpperCase(Locale.ROOT);
                                        String nuevo =
                                                accion.equals("DELETE")
                                                        ? ""
                                                        : consultar(db, sql, tabla, parametros);
                                        if (accion.equals("INSERT")) {
                                            List<Map<String, Object>> valores = new ArrayList<>();
                                            for (Map<Integer, Object> fila :
                                                    lote.isEmpty() ? List.of(parametros) : lote) {
                                                valores.add(datosInsert(sql, fila));
                                            }
                                            nuevo = JSON.toJson(valores);
                                        }
                                        try (PreparedStatement q =
                                                db.prepareStatement(
                                                        "INSERT INTO"
                                                            + " log_usuario(id_usuario,accion,campo_modificado,valor_anterior,valor_nuevo)"
                                                            + " VALUES(?,?,?,?,?)")) {
                                            q.setInt(1, usuario);
                                            q.setString(2, accion);
                                            q.setString(3, tabla);
                                            q.setString(4, anterior);
                                            q.setString(5, nuevo);
                                            q.executeUpdate();
                                        }
                                    }
                                    if (propio) db.commit();
                                    lote.clear();
                                    return resultado;
                                } catch (Throwable error) {
                                    // si falla el registro tampoco dejamos el cambio a medias
                                    if (propio) db.rollback();
                                    if (error instanceof InvocationTargetException e)
                                        throw e.getCause();
                                    throw error;
                                } finally {
                                    if (propio) db.setAutoCommit(true);
                                }
                            }
                            try {
                                return metodo.invoke(real, argumentos);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        });
    }

    // consultamos la fila anterior y la posterior sin incluir contraseñas
    private static String consultar(
            Connection db, String sql, String tabla, Map<Integer, Object> parametros)
            throws SQLException {
        if (sql.stripLeading().toUpperCase(Locale.ROOT).startsWith("INSERT")) return "";
        Matcher donde = Pattern.compile("(?is)\\bWHERE\\b(.*)$").matcher(sql);
        String condicion = donde.find() ? donde.group(1) : "1=1";
        int antes = donde.hasMatch() ? contar(sql.substring(0, donde.start()), '?') : 0;
        List<Map<String, Object>> filas = new ArrayList<>();
        try (PreparedStatement q =
                db.prepareStatement(
                        "SELECT * FROM `" + tabla + "` WHERE " + condicion + " FOR UPDATE")) {
            for (int i = 1; i <= contar(condicion, '?'); i++)
                q.setObject(i, parametros.get(antes + i));
            try (ResultSet rs = q.executeQuery()) {
                ResultSetMetaData m = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= m.getColumnCount(); i++) {
                        String campo = m.getColumnLabel(i);
                        if (!secreto(campo)) fila.put(campo, rs.getString(i));
                    }
                    filas.add(fila);
                }
            }
        }
        return JSON.toJson(filas);
    }

    private static Map<String, Object> datosInsert(String sql, Map<Integer, Object> parametros) {
        Map<String, Object> datos = new LinkedHashMap<>();
        Matcher m =
                Pattern.compile(
                                "(?is)INSERT\\s+INTO\\s+[^"
                                        + " (]+\\s*\\((.*?)\\)\\s*VALUES\\s*\\((.*?)\\)")
                        .matcher(sql);
        if (m.find()) {
            String[] campos = m.group(1).split(","), valores = m.group(2).split(",");
            int parametro = 1;
            for (int i = 0; i < Math.min(campos.length, valores.length); i++) {
                String campo = campos[i].trim().replace("`", "");
                String valor = valores[i].trim();
                Object dato = valor.equals("?") ? parametros.get(parametro++) : valor;
                if (!secreto(campo)) datos.put(campo, Objects.toString(dato, ""));
            }
        }
        return datos;
    }

    private static boolean secreto(String campo) {
        return campo.equalsIgnoreCase("contraseña")
                || campo.toLowerCase(Locale.ROOT).contains("password");
    }

    private static int contar(String texto, char caracter) {
        return (int) texto.chars().filter(c -> c == caracter).count();
    }
}

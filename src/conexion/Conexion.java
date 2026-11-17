package conexion;

import com.google.gson.Gson;

import java.lang.reflect.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.regex.*;

// abrimos la conexión con MySQL y asociamos los cambios al usuario de la sesión
public class Conexion {
    private static final String URL =
            "jdbc:mysql://pma.torga.com.ar:3306/s23_academia_integral_xxi";
    private static final String USER = "u23_GZ8nhz7rRw";
    private static final String PASSWORD = "SGpFfP1KLWxw9Gv=ile+whK=";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conexion =
                    DriverManager.getConnection(
                            URL + "?connectTimeout=10000&socketTimeout=15000", USER, PASSWORD);
            // identificamos la cuenta para que los cambios queden asociados a quien los hizo
            try (java.sql.PreparedStatement q =
                    conexion.prepareStatement("SET @academia_usuario=?, @academia_origen='Java'")) {
                q.setObject(1, servicios.Auditoria_servicio.usuarioActual());
                q.execute();
                return envolver(conexion, servicios.Auditoria_servicio.usuarioActual());
            } catch (SQLException e) {
                conexion.close();
                throw e;
            }
        } catch (ClassNotFoundException error) {
            // si falta el conector, avisamos el problema en vez de devolver una conexión vacía.
            throw new IllegalStateException(
                    "No se encontró el conector MySQL. Revisá las bibliotecas del proyecto en"
                            + " Eclipse.",
                    error);
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo conectar con la base de datos. Revisá Internet y la configuración"
                            + " de conexión.",
                    error);
        }
    }

    // recibimos las operaciones que deben guardarse juntas, por ejemplo una nota y su historial
    public interface Operacion<T> {
        T ejecutar(Connection conexion) throws Exception;
    }

    // usamos una sola conexión para todos los cambios de la operación
    // si falla una consulta, rollback deshace también las que ya se habían ejecutado
    public static <T> T transaccion(Operacion<T> operacion) throws Exception {
        try (Connection conexion = getConnection()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (Exception error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    // desde acá controlamos la auditoría de las consultas que modifican datos
    // envolvemos la conexión para registrar quién hizo el cambio sin abrir otra conexión
    // el dato modificado y su log se confirman juntos o se deshacen juntos si hay un error
    private static final Gson JSON = new Gson();
    private static final Pattern TABLA =
            Pattern.compile(
                    "(?is)^\\s*(INSERT\\s+INTO|UPDATE|DELETE\\s+FROM)\\s+`?([\\p{L}_0-9]+)`?");

    private static Connection envolver(Connection db, Integer usuario) {
        // dejamos pasar las llamadas a la conexión real, pero revisamos prepareStatement
        // cuando la consulta cambia datos, le agregamos el registro de auditoría
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
                                        // no auditamos la tabla de logs: guardar un log generaría
                                        // otro sin terminar
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
        // guardamos la posición y el valor de cada ? para saber qué filas cambió la consulta
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
                            // copiamos los valores de cada fila, si guardáramos el mismo mapa,
                            // quedaría sólo la última
                            else if (nombre.equals("addBatch")) lote.add(new TreeMap<>(parametros));
                            else if (nombre.equals("clearBatch")) lote.clear();

                            if (List.of(
                                            "executeUpdate",
                                            "executeLargeUpdate",
                                            "execute",
                                            "executeBatch",
                                            "executeLargeBatch")
                                    .contains(nombre)) {
                                // si el servicio ya empezó una transacción, él se encarga de
                                // confirmarla
                                // si no empezó ninguna, guardamos esta consulta y su auditoría en
                                // una nueva
                                boolean transaccionPropia = db.getAutoCommit();
                                if (transaccionPropia) db.setAutoCommit(false);
                                try {
                                    // leemos el dato antes de cambiarlo y después ejecutamos la
                                    // consulta original
                                    String anterior = consultar(db, sql, tabla, parametros);
                                    Object resultado = metodo.invoke(real, argumentos);
                                    boolean cambio = huboCambios(resultado, real);
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
                                        clases.Log_Usuario log = new clases.Log_Usuario();
                                        log.setId_usuario(usuario);
                                        log.setAccion(accion);
                                        log.setCampo_modificado(tabla);
                                        log.setValor_anterior(anterior);
                                        log.setValor_nuevo(nuevo);
                                        // La conexión real evita volver a interceptar el registro
                                        // de auditoría.
                                        new dao.Log_Usuario_dao().crear(db, log);
                                    }
                                    if (transaccionPropia) db.commit();
                                    lote.clear();
                                    return resultado;
                                } catch (Throwable error) {
                                    // si falla el registro tampoco dejamos el cambio a medias
                                    if (transaccionPropia) db.rollback();
                                    if (error instanceof InvocationTargetException e)
                                        throw e.getCause();
                                    throw error;
                                } finally {
                                    if (transaccionPropia) db.setAutoCommit(true);
                                }
                            }
                            try {
                                return metodo.invoke(real, argumentos);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        });
    }

    // executeUpdate devuelve un número, pero una carga por lotes devuelve un arreglo
    // revisamos ambos casos para no guardar un log cuando la consulta no cambió ninguna fila
    private static boolean huboCambios(Object resultado, PreparedStatement consulta)
            throws SQLException {
        if (resultado instanceof Number cantidad) {
            return cantidad.longValue() > 0;
        }
        if (resultado instanceof int[] cantidades) {
            for (int cantidad : cantidades) {
                if (cantidad > 0 || cantidad == Statement.SUCCESS_NO_INFO) return true;
            }
            return false;
        }
        if (resultado instanceof long[] cantidades) {
            for (long cantidad : cantidades) {
                if (cantidad > 0 || cantidad == Statement.SUCCESS_NO_INFO) return true;
            }
            return false;
        }
        return consulta.getUpdateCount() > 0;
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

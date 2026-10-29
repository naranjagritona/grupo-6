package dao;

import conexion.Conexion;
import java.sql.*;
import java.util.*;

// compartimos la conexión y las consultas que utilizan los daos académicos
public class Consulta_academica_dao {
    // abrimos la conexión que se utiliza para consultar o guardar los datos
    protected Connection abrirConexion() {
        return Conexion.getConnection();
    }

    public interface Operacion<T> {
        T ejecutar(Connection conexion) throws Exception;
    }

    // guardamos los cambios juntos y los deshacemos si falla alguna validación
    public <T> T transaccion(Operacion<T> operacion) throws Exception {
        try (Connection conexion = abrirConexion()) {
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

    // asignamos los valores a los parámetros antes de ejecutar la consulta
    private void parametros(PreparedStatement consulta, Object... valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            consulta.setObject(i + 1, valores[i]);
        }
    }

    // pasamos las filas de la consulta a una lista con los nombres de sus columnas
    protected List<Map<String, Object>> consultar(Connection conexion, String sql, Object... valores) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            parametros(consulta, valores);
            try (ResultSet registros = consulta.executeQuery()) {
                List<Map<String, Object>> lista = new ArrayList<>();
                while (registros.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= registros.getMetaData().getColumnCount(); i++) {
                        fila.put(registros.getMetaData().getColumnLabel(i), registros.getObject(i));
                    }
                    lista.add(fila);
                }
                return lista;
            }
        }
    }

    // ejecutamos una modificación y devolvemos la cantidad de registros afectados
    protected int ejecutar(Connection conexion, String sql, Object... valores) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            parametros(consulta, valores);
            return consulta.executeUpdate();
        }
    }

    // insertamos un registro y devolvemos el identificador generado por la base de datos
    protected int insertarRegistro(Connection conexion, String sql, Object... valores) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(consulta, valores);
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // buscamos un registro y avisamos si ya no se encuentra en la base de datos
    protected Map<String, Object> uno(Connection conexion, String sql, Object... valores) throws SQLException {
        List<Map<String, Object>> filas = consultar(conexion, sql, valores);
        if (filas.isEmpty()) throw new IllegalArgumentException("El registro no existe. Actualizá el listado.");
        return filas.getFirst();
    }

    // consultamos el rol real de la cuenta antes de permitir una operación
    public int obtenerRol(Connection conexion, int usuario) throws SQLException {
        int rol = numero(uno(conexion, "SELECT id_rol FROM usuario WHERE id_usuario=?", usuario), "id_rol");
        String tabla = rol == 1 ? "secretaria" : rol == 4 ? "preceptor" : rol == 5 ? "directivo" : null;
        if (tabla != null) {
            var perfiles = consultar(conexion, "SELECT estado FROM " + tabla + " WHERE id_usuario=?", usuario);
            if (!perfiles.isEmpty() && !"Activo".equals(perfiles.getFirst().get("estado"))) throw new IllegalArgumentException("La cuenta de personal está inactiva.");
        }
        return rol;
    }

    // obtenemos el valor numérico de una columna del registro consultado
    public static int numero(Map<String, Object> fila, String campo) {
        return ((Number) fila.get(campo)).intValue();
    }
}

package dao;

import clases.Log_Usuario;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de log usuario en la base de datos
public class Log_Usuario_dao implements Dao<Log_Usuario, Integer> {
    // la auditoría sólo puede consultarse desde una sesión vigente de dirección
    private void verificarLectura() {
        try {
            Conexion.transaccion(
                    db -> {
                        servicios.Permisos_servicio.verificar(
                                db,
                                servicios.Auditoria_servicio.usuarioActual(),
                                clases.Rol.DIRECTIVO);
                        return null;
                    });
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Log_Usuario objeto) {
        String consulta =
                "INSERT INTO log_usuario (id_usuario, accion, campo_modificado, valor_anterior,"
                        + " valor_nuevo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_usuario());
            ps.setString(2, objeto.getAccion());
            ps.setString(3, objeto.getCampo_modificado());
            ps.setString(4, objeto.getValor_anterior() == null ? "" : objeto.getValor_anterior());
            ps.setString(5, objeto.getValor_nuevo() == null ? "" : objeto.getValor_nuevo());

            ps.executeUpdate();

        } catch (SQLException error) {
            // avisamos el error al servicio para que no dé por guardado un ingreso que se perdió
            throw new IllegalStateException("No se pudo guardar el registro de actividad.", error);
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Log_Usuario obtenerPorId(Integer id) {
        verificarLectura();
        String consulta = "SELECT * FROM log_usuario WHERE id_log_usuario=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de log_usuario.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Log_Usuario> obtenerTodos() {
        verificarLectura();
        String consulta = "SELECT * FROM log_usuario";
        List<Log_Usuario> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de log_usuario.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Log_Usuario objeto) {
        verificarLectura();
        String consulta =
                "UPDATE log_usuario SET id_usuario = ?, accion = ?, campo_modificado = ?,"
                        + " valor_anterior = ?, valor_nuevo = ? WHERE id_log_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_usuario());
            ps.setString(2, objeto.getAccion());
            ps.setString(3, objeto.getCampo_modificado());
            ps.setString(4, objeto.getValor_anterior() == null ? "" : objeto.getValor_anterior());
            ps.setString(5, objeto.getValor_nuevo() == null ? "" : objeto.getValor_nuevo());
            ps.setInt(6, objeto.getId_log_usuario());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el log de usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        verificarLectura();
        String consulta = "DELETE FROM log_usuario WHERE id_log_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el log de usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    protected Log_Usuario leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Log_Usuario objeto = new Log_Usuario();
        objeto.setId_log_usuario((Integer) registros.getObject("id_log_usuario"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setAccion(registros.getString("accion"));
        objeto.setCampo_modificado(registros.getString("campo_modificado"));
        objeto.setValor_anterior(registros.getString("valor_anterior"));
        objeto.setValor_nuevo(registros.getString("valor_nuevo"));
        objeto.setFecha_hora(
                (registros.getTimestamp("fecha_hora") == null
                        ? null
                        : registros.getTimestamp("fecha_hora").toLocalDateTime()));
        return objeto;
    }

    public List<Log_Usuario> ultimos(Connection db) throws SQLException {
        return seleccionar(
                db,
                "SELECT * FROM log_usuario ORDER BY fecha_hora DESC,id_log_usuario DESC LIMIT 200");
    }

    public void crear(Connection conexion, Log_Usuario objeto) throws SQLException {
        String consulta =
                "INSERT INTO"
                    + " log_usuario(id_usuario,accion,campo_modificado,valor_anterior,valor_nuevo)"
                    + " VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setObject(1, objeto.getId_usuario());
            ps.setString(2, objeto.getAccion());
            ps.setString(3, objeto.getCampo_modificado());
            ps.setString(4, objeto.getValor_anterior());
            ps.setString(5, objeto.getValor_nuevo());
            ps.executeUpdate();
        }
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Log_Usuario
    private java.util.List<Log_Usuario> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Log_Usuario> lista = new java.util.ArrayList<>();
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            try (java.sql.ResultSet registros = ps.executeQuery()) {
                while (registros.next()) {
                    lista.add(leerEntidad(registros));
                }
            }
        }
        return lista;
    }

    // recibimos la conexión del servicio para no separar las consultas de una transacción
    public java.util.List<Log_Usuario> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM log_usuario");
    }

    public Log_Usuario buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM log_usuario WHERE id_log_usuario=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Log_Usuario requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM log_usuario WHERE id_log_usuario=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

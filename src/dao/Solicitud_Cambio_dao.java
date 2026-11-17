package dao;

import clases.Solicitud_Cambio;

import conexion.Conexion;

import java.sql.*;
import java.util.*;

// consultamos y guardamos las solicitudes de cambio que debe revisar dirección
public class Solicitud_Cambio_dao implements Dao<Solicitud_Cambio, Integer> {
    // buscamos una solicitud pendiente de la misma inscripción para evitar duplicados
    public boolean pendiente(Connection conexion, int inscripcion) throws SQLException {
        return !seleccionar(
                        conexion,
                        "SELECT * FROM solicitud_cambio WHERE id_inscripcion=? AND"
                                + " estado='Pendiente' FOR UPDATE",
                        inscripcion)
                .isEmpty();
    }

    // registramos el destino y la explicación que acompaña a la solicitud
    public int crear(Connection conexion, Solicitud_Cambio solicitud) throws SQLException {
        String consulta =
                "INSERT INTO"
                    + " solicitud_cambio(id_inscripcion,id_curso_destino,observacion,id_solicitante)"
                    + " VALUES(?,?,?,?)";
        try (java.sql.PreparedStatement ps =
                conexion.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, solicitud.getId_inscripcion());
            ps.setObject(2, solicitud.getId_curso_destino());
            ps.setObject(3, solicitud.getObservacion());
            ps.setObject(4, solicitud.getId_solicitante());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    public List<Solicitud_Cambio> listar(Connection conexion) throws SQLException {
        return seleccionar(
                conexion,
                "SELECT * FROM solicitud_cambio ORDER BY estado='Pendiente' DESC,id_solicitud"
                        + " DESC");
    }

    public Solicitud_Cambio bloquear(Connection conexion, int id) throws SQLException {
        return requerir(conexion, id);
    }

    // guardamos la decisión de dirección junto a su motivo y la inscripción generada
    public void resolver(
            Connection conexion, int id, boolean aprobar, int usuario, String motivo, Integer nueva)
            throws SQLException {
        String consulta =
                "UPDATE solicitud_cambio SET"
                    + " estado=?,id_directivo=?,motivo_resolucion=?,fecha_resolucion=NOW(),id_inscripcion_nueva=?"
                    + " WHERE id_solicitud=?";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setObject(1, aprobar ? "Aprobada" : "Rechazada");
            ps.setObject(2, usuario);
            ps.setObject(3, motivo);
            ps.setObject(4, nueva);
            ps.setObject(5, id);
            ps.executeUpdate();
        }
    }

    protected Solicitud_Cambio leerEntidad(java.sql.ResultSet registros)
            throws java.sql.SQLException {
        Solicitud_Cambio objeto = new Solicitud_Cambio();
        objeto.setId_solicitud((Integer) registros.getObject("id_solicitud"));
        objeto.setId_inscripcion((Integer) registros.getObject("id_inscripcion"));
        objeto.setId_curso_destino((Integer) registros.getObject("id_curso_destino"));
        objeto.setObservacion(registros.getString("observacion"));
        objeto.setEstado(registros.getString("estado"));
        objeto.setId_solicitante((Integer) registros.getObject("id_solicitante"));
        objeto.setId_directivo((Integer) registros.getObject("id_directivo"));
        objeto.setMotivo_resolucion(registros.getString("motivo_resolucion"));
        objeto.setFecha_solicitud(
                (registros.getTimestamp("fecha_solicitud") == null
                        ? null
                        : registros.getTimestamp("fecha_solicitud").toLocalDateTime()));
        objeto.setFecha_resolucion(
                (registros.getTimestamp("fecha_resolucion") == null
                        ? null
                        : registros.getTimestamp("fecha_resolucion").toLocalDateTime()));
        objeto.setId_inscripcion_nueva((Integer) registros.getObject("id_inscripcion_nueva"));
        return objeto;
    }

    @Override
    public Solicitud_Cambio obtenerPorId(Integer id) {
        try (Connection conexion = Conexion.getConnection()) {
            return buscar(conexion, id);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro.", error);
        }
    }

    @Override
    public List<Solicitud_Cambio> obtenerTodos() {
        try (Connection conexion = Conexion.getConnection()) {
            return listarTabla(conexion);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado.", error);
        }
    }

    @Override
    public void insertar(Solicitud_Cambio objeto) {
        try {
            Conexion.transaccion(
                    conexion -> {
                        crear(conexion, objeto);
                        return null;
                    });
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo guardar el registro.", error);
        }
    }

    @Override
    public void modificar(Solicitud_Cambio objeto) {
        throw new UnsupportedOperationException(
                "Usá el servicio correspondiente para conservar el historial.");
    }

    @Override
    public void eliminar(Integer id) {
        throw new UnsupportedOperationException("El registro conserva su historial.");
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Solicitud_Cambio
    private java.util.List<Solicitud_Cambio> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Solicitud_Cambio> lista = new java.util.ArrayList<>();
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
    public java.util.List<Solicitud_Cambio> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM solicitud_cambio");
    }

    public Solicitud_Cambio buscar(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(conexion, "SELECT * FROM solicitud_cambio WHERE id_solicitud=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Solicitud_Cambio requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM solicitud_cambio WHERE id_solicitud=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

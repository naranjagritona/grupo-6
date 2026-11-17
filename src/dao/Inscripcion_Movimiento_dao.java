package dao;

import clases.Inscripcion_Movimiento;

import conexion.Conexion;

import java.sql.*;
import java.util.*;

public class Inscripcion_Movimiento_dao implements Dao<Inscripcion_Movimiento, Integer> {
    public List<Inscripcion_Movimiento> deInscripcion(Connection db, int id) throws SQLException {
        return seleccionar(
                db,
                "SELECT * FROM inscripcion_movimiento WHERE id_inscripcion=? ORDER BY"
                        + " id_movimiento",
                id);
    }

    public void crear(Connection db, Inscripcion_Movimiento m) throws SQLException {
        String consulta =
                "INSERT INTO"
                    + " inscripcion_movimiento(id_inscripcion,estado_anterior,estado_nuevo,motivo,id_usuario)"
                    + " VALUES(?,?,?,?,?)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, m.getId_inscripcion());
            ps.setObject(2, m.getEstado_anterior());
            ps.setObject(3, m.getEstado_nuevo());
            ps.setObject(4, m.getMotivo());
            ps.setObject(5, m.getId_usuario());
            ps.executeUpdate();
        }
    }

    protected Inscripcion_Movimiento leerEntidad(java.sql.ResultSet registros)
            throws java.sql.SQLException {
        Inscripcion_Movimiento objeto = new Inscripcion_Movimiento();
        objeto.setId_movimiento((Integer) registros.getObject("id_movimiento"));
        objeto.setId_inscripcion((Integer) registros.getObject("id_inscripcion"));
        objeto.setEstado_anterior(registros.getString("estado_anterior"));
        objeto.setEstado_nuevo(registros.getString("estado_nuevo"));
        objeto.setMotivo(registros.getString("motivo"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setFecha(
                (registros.getTimestamp("fecha") == null
                        ? null
                        : registros.getTimestamp("fecha").toLocalDateTime()));
        return objeto;
    }

    @Override
    public Inscripcion_Movimiento obtenerPorId(Integer id) {
        try (Connection conexion = Conexion.getConnection()) {
            return buscar(conexion, id);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro.", error);
        }
    }

    @Override
    public List<Inscripcion_Movimiento> obtenerTodos() {
        try (Connection conexion = Conexion.getConnection()) {
            return listarTabla(conexion);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado.", error);
        }
    }

    @Override
    public void insertar(Inscripcion_Movimiento objeto) {
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
    public void modificar(Inscripcion_Movimiento objeto) {
        throw new UnsupportedOperationException(
                "Usá el servicio correspondiente para conservar el historial.");
    }

    @Override
    public void eliminar(Integer id) {
        throw new UnsupportedOperationException("El registro conserva su historial.");
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Inscripcion_Movimiento
    private java.util.List<Inscripcion_Movimiento> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Inscripcion_Movimiento> lista = new java.util.ArrayList<>();
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
    public java.util.List<Inscripcion_Movimiento> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM inscripcion_movimiento");
    }

    public Inscripcion_Movimiento buscar(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion, "SELECT * FROM inscripcion_movimiento WHERE id_movimiento=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Inscripcion_Movimiento requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM inscripcion_movimiento WHERE id_movimiento=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

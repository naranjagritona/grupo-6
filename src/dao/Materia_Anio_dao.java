package dao;

import clases.Materia_Anio;

import conexion.Conexion;

import java.sql.*;
import java.util.*;

public class Materia_Anio_dao implements Dao<Materia_Anio, Materia_Anio_dao.Clave> {
    public record Clave(int anio, int id_materia) {}

    public List<Materia_Anio> delAnio(Connection db, int anio) throws SQLException {
        return seleccionar(
                db, "SELECT * FROM materia_anio WHERE anio=? ORDER BY id_materia FOR UPDATE", anio);
    }

    public void agregar(Connection db, Materia_Anio relacion) throws SQLException {
        String consulta =
                "INSERT INTO materia_anio(anio,id_materia) VALUES(?,?) ON DUPLICATE KEY UPDATE"
                        + " id_materia=VALUES(id_materia)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, relacion.getAnio());
            ps.setObject(2, relacion.getId_materia());
            ps.executeUpdate();
        }
    }

    public void quitar(Connection db, int anio, int materia) throws SQLException {
        String consulta = "DELETE FROM materia_anio WHERE anio=? AND id_materia=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, anio);
            ps.setObject(2, materia);
            ps.executeUpdate();
        }
    }

    protected Materia_Anio leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Materia_Anio objeto = new Materia_Anio();
        objeto.setAnio((Integer) registros.getObject("anio"));
        objeto.setId_materia((Integer) registros.getObject("id_materia"));
        return objeto;
    }

    @Override
    public Materia_Anio obtenerPorId(Clave id) {
        try (Connection conexion = Conexion.getConnection()) {
            var filas =
                    seleccionar(
                            conexion,
                            "SELECT * FROM materia_anio WHERE anio=? AND id_materia=?",
                            id.anio(),
                            id.id_materia());
            return filas.isEmpty() ? null : filas.getFirst();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar la relación.", error);
        }
    }

    @Override
    public List<Materia_Anio> obtenerTodos() {
        try (Connection conexion = Conexion.getConnection()) {
            return listarTabla(conexion);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado.", error);
        }
    }

    @Override
    public void insertar(Materia_Anio objeto) {
        throw new UnsupportedOperationException(
                "Usá el servicio de materias para validar la relación.");
    }

    @Override
    public void modificar(Materia_Anio objeto) {
        throw new UnsupportedOperationException(
                "Usá el servicio de materias para validar la relación.");
    }

    @Override
    public void eliminar(Clave id) {
        throw new UnsupportedOperationException(
                "Usá el servicio de materias para conservar los datos académicos.");
    }

    public Materia_Anio buscar(Connection db, int id) {
        throw new UnsupportedOperationException("La relación requiere su clave compuesta.");
    }

    public Materia_Anio requerir(Connection db, int id) {
        throw new UnsupportedOperationException("La relación requiere su clave compuesta.");
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Materia_Anio
    private java.util.List<Materia_Anio> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Materia_Anio> lista = new java.util.ArrayList<>();
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
    public java.util.List<Materia_Anio> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM materia_anio");
    }
}

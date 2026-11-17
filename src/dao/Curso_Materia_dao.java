package dao;

import clases.Curso_Materia;

import conexion.Conexion;

import java.sql.*;
import java.util.*;

public class Curso_Materia_dao implements Dao<Curso_Materia, Curso_Materia_dao.Clave> {
    public record Clave(int id_curso, int id_materia) {}

    public List<Curso_Materia> delCurso(Connection db, int curso) throws SQLException {
        return seleccionar(
                db, "SELECT * FROM curso_materia WHERE id_curso=? ORDER BY id_materia", curso);
    }

    public Curso_Materia bloquear(Connection db, int curso, int materia) throws SQLException {
        var filas =
                seleccionar(
                        db,
                        "SELECT * FROM curso_materia WHERE id_curso=? AND id_materia=? FOR UPDATE",
                        curso,
                        materia);
        if (filas.isEmpty())
            throw new IllegalArgumentException("La materia no pertenece al curso.");
        return filas.getFirst();
    }

    public boolean asignado(Connection db, int curso, int materia, int usuario)
            throws SQLException {
        return !seleccionar(
                        db,
                        "SELECT cm.* FROM curso_materia cm JOIN docente d ON"
                            + " d.id_docente=cm.id_docente WHERE cm.id_curso=? AND cm.id_materia=?"
                            + " AND d.id_usuario=?",
                        curso,
                        materia,
                        usuario)
                .isEmpty();
    }

    public boolean tieneDocente(Connection db, int curso, int usuario) throws SQLException {
        return !seleccionar(
                        db,
                        "SELECT cm.* FROM curso_materia cm JOIN docente d ON"
                            + " d.id_docente=cm.id_docente WHERE cm.id_curso=? AND d.id_usuario=?",
                        curso,
                        usuario)
                .isEmpty();
    }

    public void actualizar(Connection db, Curso_Materia relacion) throws SQLException {
        String consulta = "UPDATE curso_materia SET id_docente=? WHERE id_curso=? AND id_materia=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, relacion.getId_docente());
            ps.setObject(2, relacion.getId_curso());
            ps.setObject(3, relacion.getId_materia());
            ps.executeUpdate();
        }
    }

    public void incorporarAlAnio(Connection db, int materia, int anio) throws SQLException {
        String consulta =
                "INSERT INTO curso_materia(id_curso,id_materia) SELECT c.id_curso,? FROM curso c"
                    + " WHERE CAST(c.año AS UNSIGNED)=? AND NOT EXISTS(SELECT 1 FROM curso_materia"
                    + " cm WHERE cm.id_curso=c.id_curso AND cm.id_materia=?)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, materia);
            ps.setObject(2, anio);
            ps.setObject(3, materia);
            ps.executeUpdate();
        }
    }

    public void quitarDelAnio(Connection db, int anio, int materia) throws SQLException {
        String consulta =
                "DELETE cm FROM curso_materia cm JOIN curso c ON c.id_curso=cm.id_curso WHERE"
                        + " CAST(c.año AS UNSIGNED)=? AND cm.id_materia=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, anio);
            ps.setObject(2, materia);
            ps.executeUpdate();
        }
    }

    protected Curso_Materia leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Curso_Materia objeto = new Curso_Materia();
        objeto.setId_curso((Integer) registros.getObject("id_curso"));
        objeto.setId_materia((Integer) registros.getObject("id_materia"));
        objeto.setId_docente((Integer) registros.getObject("id_docente"));
        return objeto;
    }

    public void sincronizar(Connection db, int curso, List<Curso_Materia> materias)
            throws SQLException {
        if (materias.isEmpty()) throw new IllegalArgumentException("El curso debe tener materias.");
        String espacios = String.join(",", Collections.nCopies(materias.size(), "?"));
        try (PreparedStatement consulta =
                db.prepareStatement(
                        "DELETE FROM curso_materia WHERE id_curso=? AND id_materia NOT IN ("
                                + espacios
                                + ")")) {
            consulta.setInt(1, curso);
            for (int i = 0; i < materias.size(); i++)
                consulta.setInt(i + 2, materias.get(i).getId_materia());
            consulta.executeUpdate();
        }
        try (PreparedStatement consulta =
                db.prepareStatement(
                        "INSERT INTO curso_materia(id_curso,id_materia) VALUES(?,?) ON DUPLICATE"
                                + " KEY UPDATE id_materia=VALUES(id_materia)")) {
            for (Curso_Materia materia : materias) {
                consulta.setInt(1, curso);
                consulta.setInt(2, materia.getId_materia());
                consulta.addBatch();
            }
            consulta.executeBatch();
        }
    }

    @Override
    public Curso_Materia obtenerPorId(Clave id) {
        try (Connection conexion = Conexion.getConnection()) {
            var filas =
                    seleccionar(
                            conexion,
                            "SELECT * FROM curso_materia WHERE id_curso=? AND id_materia=?",
                            id.id_curso(),
                            id.id_materia());
            return filas.isEmpty() ? null : filas.getFirst();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar la relación.", error);
        }
    }

    @Override
    public List<Curso_Materia> obtenerTodos() {
        try (Connection conexion = Conexion.getConnection()) {
            return listarTabla(conexion);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado.", error);
        }
    }

    @Override
    public void insertar(Curso_Materia objeto) {
        throw new UnsupportedOperationException(
                "Usá el servicio de materias para validar la relación.");
    }

    @Override
    public void modificar(Curso_Materia objeto) {
        throw new UnsupportedOperationException(
                "Usá el servicio de materias para validar la relación.");
    }

    @Override
    public void eliminar(Clave id) {
        throw new UnsupportedOperationException(
                "Usá el servicio de materias para conservar los datos académicos.");
    }

    public Curso_Materia buscar(Connection db, int id) {
        throw new UnsupportedOperationException("La relación requiere su clave compuesta.");
    }

    public Curso_Materia requerir(Connection db, int id) {
        throw new UnsupportedOperationException("La relación requiere su clave compuesta.");
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Curso_Materia
    private java.util.List<Curso_Materia> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Curso_Materia> lista = new java.util.ArrayList<>();
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
    public java.util.List<Curso_Materia> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM curso_materia");
    }
}

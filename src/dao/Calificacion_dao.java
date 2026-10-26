package dao;

import clases.Calificacion;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de calificacion en la base de datos
public class Calificacion_dao implements Dao<Calificacion, Integer> {

    public void insertarVerificando(Calificacion objeto) throws SQLException {
        String sql =
                "INSERT INTO calificacion (id_inscripcion, id_columna_notas, nota, observaciones)"
                        + " VALUES (?, ?, ?, ?)";
        try (Connection conexion = Conexion.getConnection()) {
            if (conexion == null)
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, objeto.getId_inscripcion());
                ps.setInt(2, objeto.getId_columna_notas());
                ps.setDouble(3, objeto.getNota());
                ps.setString(4, objeto.getObservaciones());
                ps.executeUpdate();
            }
        }
    }

    /** Actualiza la nota y registra su auditoría en una única transacción. */
    public void modificarConHistorial(Calificacion objeto, Double notaAnterior, String motivo)
            throws SQLException {
        String actualizar =
                "UPDATE calificacion SET nota = ?, fecha_carga = ?, observaciones = ? WHERE"
                        + " id_calificacion = ?";
        String historial =
                "INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva,"
                        + " fecha_modificacion, motivo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = Conexion.getConnection()) {
            if (conexion == null)
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            boolean autoCommit = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try (PreparedStatement psNota = conexion.prepareStatement(actualizar);
                    PreparedStatement psHistorial = conexion.prepareStatement(historial)) {
                psNota.setDouble(1, objeto.getNota());
                psNota.setObject(2, objeto.getFecha_carga());
                psNota.setString(3, objeto.getObservaciones());
                psNota.setInt(4, objeto.getId_calificacion());
                if (psNota.executeUpdate() != 1)
                    throw new SQLException("La calificación ya no existe.");

                psHistorial.setInt(1, objeto.getId_calificacion());
                psHistorial.setDouble(2, notaAnterior);
                psHistorial.setDouble(3, objeto.getNota());
                psHistorial.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                psHistorial.setString(5, motivo);
                psHistorial.executeUpdate();
                conexion.commit();
            } catch (SQLException error) {
                conexion.rollback();
                throw error;
            } finally {
                conexion.setAutoCommit(autoCommit);
            }
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Calificacion objeto) {
        String consulta =
                "INSERT INTO calificacion (id_inscripcion, id_columna_notas, nota, observaciones)"
                        + " VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_inscripcion());
            ps.setInt(2, objeto.getId_columna_notas());
            ps.setObject(3, objeto.getNota());
            ps.setString(4, objeto.getObservaciones());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Calificacion obtenerPorId(Integer id) {
        Calificacion calificacion = null;
        String consulta = "SELECT * FROM calificacion WHERE id_calificacion = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    calificacion = new Calificacion();
                    calificacion.setId_calificacion(rs.getInt("id_calificacion"));
                    calificacion.setId_inscripcion(rs.getInt("id_inscripcion"));
                    calificacion.setId_columna_notas(rs.getInt("id_columna_notas"));
                    calificacion.setNota(rs.getDouble("nota"));
                    Date fecha = rs.getDate("fecha_carga");
                    calificacion.setFecha_carga(fecha != null ? fecha.toLocalDate() : null);
                    calificacion.setObservaciones(rs.getString("observaciones"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la calificación por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return calificacion;
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Calificacion> obtenerTodos() {
        List<Calificacion> listaCalificaciones = new ArrayList<>();
        String consulta = "SELECT * FROM calificacion";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Calificacion calificacion = new Calificacion();

                calificacion.setId_calificacion(rs.getInt("id_calificacion"));
                calificacion.setId_inscripcion(rs.getInt("id_inscripcion"));
                calificacion.setId_columna_notas(rs.getInt("id_columna_notas"));
                calificacion.setNota(rs.getDouble("nota"));
                Date fecha = rs.getDate("fecha_carga");
                calificacion.setFecha_carga(fecha != null ? fecha.toLocalDate() : null);
                calificacion.setObservaciones(rs.getString("observaciones"));

                listaCalificaciones.add(calificacion);
            }

        } catch (SQLException error) {
            System.err.println(
                    "Error al obtener la lista de calificaciones: " + error.getMessage());
            error.printStackTrace();
        }

        return listaCalificaciones;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Calificacion objeto) {
        String consulta =
                "UPDATE calificacion SET id_inscripcion = ?, id_columna_notas = ?, nota = ?,"
                        + " observaciones = ? WHERE id_calificacion = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_inscripcion());
            ps.setInt(2, objeto.getId_columna_notas());
            ps.setObject(3, objeto.getNota());
            ps.setString(4, objeto.getObservaciones());

            ps.setInt(5, objeto.getId_calificacion());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM calificacion WHERE id_calificacion = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

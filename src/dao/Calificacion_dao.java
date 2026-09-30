package dao;

import clases.Calificacion;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Calificacion_dao implements Dao<Calificacion, Integer> {

    @Override
    public void insertar(Calificacion objeto) {
        String consulta = "INSERT INTO calificacion (id_inscripcion, id_columna_notas, nota, observaciones) VALUES (?, ?, ?, ?)";

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
            System.err.println("Error al obtener la lista de calificaciones: " + error.getMessage());
            error.printStackTrace();
        }

        return listaCalificaciones;
    }

    @Override
    public void modificar(Calificacion objeto) {
        String consulta = "UPDATE calificacion SET id_inscripcion = ?, id_columna_notas = ?, nota = ?, observaciones = ? WHERE id_calificacion = ?";

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

    @Override
    public void eliminar(Integer id) {
        String consultaHistorial = "DELETE FROM calificacion_historial WHERE id_calificacion = ?";
        String consultaCalificacion = "DELETE FROM calificacion WHERE id_calificacion = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaHistorial);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaCalificacion)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar la calificación: " + error.getMessage());
            error.printStackTrace();
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException error2) {
                    error2.printStackTrace();
                }
            }
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException error3) {
                    error3.printStackTrace();
                }
            }
        }
    }
}
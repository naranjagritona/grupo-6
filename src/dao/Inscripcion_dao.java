package dao;

import clases.Inscripcion;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Inscripcion_dao implements Dao<Inscripcion, Integer> {

    @Override
    public void insertar(Inscripcion objeto) {
        String consulta = "INSERT INTO inscripcion (id_alumno, id_curso, fecha_inscripcion, estado, motivo_baja, importe_cuota) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_alumno());
            ps.setInt(2, objeto.getId_curso());
            ps.setObject(3, objeto.getFecha_inscripcion());
            ps.setString(4, objeto.getEstado());
            ps.setString(5, objeto.getMotivo_baja());
            ps.setObject(6, objeto.getImporte_cuota());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la inscripción: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Inscripcion> obtenerTodos() {
        List<Inscripcion> listaInscripciones = new ArrayList<>();
        String consulta = "SELECT * FROM inscripcion";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Inscripcion inscripcion = new Inscripcion();

                inscripcion.setId_inscripcion(rs.getInt("id_inscripcion"));
                inscripcion.setId_alumno(rs.getInt("id_alumno"));
                inscripcion.setId_curso(rs.getInt("id_curso"));
                
                Date fecha = rs.getDate("fecha_inscripcion");
                inscripcion.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                
                inscripcion.setEstado(rs.getString("estado"));
                inscripcion.setMotivo_baja(rs.getString("motivo_baja"));
                inscripcion.setImporte_cuota(rs.getDouble("importe_cuota"));

                listaInscripciones.add(inscripcion);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de inscripciones: " + error.getMessage());
            error.printStackTrace();
        }

        return listaInscripciones;
    }

    @Override
    public void modificar(Inscripcion objeto) {
        String consulta = "UPDATE inscripcion SET id_alumno = ?, id_curso = ?, fecha_inscripcion = ?, estado = ?, motivo_baja = ?, importe_cuota = ? WHERE id_inscripcion = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_alumno());
            ps.setInt(2, objeto.getId_curso());
            ps.setObject(3, objeto.getFecha_inscripcion());
            ps.setString(4, objeto.getEstado());
            ps.setString(5, objeto.getMotivo_baja());
            ps.setObject(6, objeto.getImporte_cuota());

            ps.setInt(7, objeto.getId_inscripcion());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la inscripción: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        //al eliminar una inscripción borramos sus calificaciones y resultados asociados
        String consultaCalificaciones = "DELETE FROM calificacion WHERE id_inscripcion = ?";
        String consultaResultados = "DELETE FROM resultado_materia WHERE id_resultado = ?";
        String consultaInscripcion = "DELETE FROM inscripcion WHERE id_inscripcion = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaCalificaciones);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaResultados);
                 PreparedStatement ps3 = conexion.prepareStatement(consultaInscripcion)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                ps3.executeUpdate();

                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar la inscripción en cascada: " + error.getMessage());
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
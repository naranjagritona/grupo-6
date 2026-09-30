package dao;

import clases.Periodo_Lectivo;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Periodo_Lectivo_dao implements Dao<Periodo_Lectivo, Integer> {

    @Override
    public void insertar(Periodo_Lectivo objeto) {
        String consulta = "INSERT INTO periodo_lectivo (nombre, tipo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getTipo());
            ps.setObject(3, objeto.getFecha_inicio());
            ps.setObject(4, objeto.getFecha_fin());
            ps.setString(5, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Periodo_Lectivo> obtenerTodos() {
        List<Periodo_Lectivo> listaPeriodos = new ArrayList<>();
        String consulta = "SELECT * FROM periodo_lectivo";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Periodo_Lectivo periodo = new Periodo_Lectivo();

                periodo.setId_periodo(rs.getInt("id_periodo"));
                periodo.setNombre(rs.getString("nombre"));
                periodo.setTipo(rs.getString("tipo"));
                
                Date fInicio = rs.getDate("fecha_inicio");
                periodo.setFecha_inicio(fInicio != null ? fInicio.toLocalDate() : null);
                
                Date fFin = rs.getDate("fecha_fin");
                periodo.setFecha_fin(fFin != null ? fFin.toLocalDate() : null);
                
                periodo.setEstado(rs.getString("estado"));

                listaPeriodos.add(periodo);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de períodos lectivos: " + error.getMessage());
            error.printStackTrace();
        }

        return listaPeriodos;
    }

    @Override
    public void modificar(Periodo_Lectivo objeto) {
        String consulta = "UPDATE periodo_lectivo SET nombre = ?, tipo = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_periodo = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getTipo());
            ps.setObject(3, objeto.getFecha_inicio());
            ps.setObject(4, objeto.getFecha_fin());
            ps.setString(5, objeto.getEstado());

            ps.setInt(6, objeto.getId_periodo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        //consultas en orden jerárquico para borrar los cursos del periodo y todas sus dependencias
        String consultaCalificaciones = "DELETE FROM calificacion WHERE id_inscripcion IN (SELECT id_inscripcion FROM inscripcion WHERE id_curso IN (SELECT id_curso FROM curso WHERE id_periodo = ?))";
        String consultaInscripciones = "DELETE FROM inscripcion WHERE id_curso IN (SELECT id_curso FROM curso WHERE id_periodo = ?)";
        String consultaColumnas = "DELETE FROM columna_notas WHERE id_curso IN (SELECT id_curso FROM curso WHERE id_periodo = ?)";
        String consultaCursos = "DELETE FROM curso WHERE id_periodo = ?";
        String consultaPeriodo = "DELETE FROM periodo_lectivo WHERE id_periodo = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaCalificaciones);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaInscripciones);
                 PreparedStatement ps3 = conexion.prepareStatement(consultaColumnas);
                 PreparedStatement ps4 = conexion.prepareStatement(consultaCursos);
                 PreparedStatement ps5 = conexion.prepareStatement(consultaPeriodo)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                ps3.executeUpdate();

                ps4.setInt(1, id);
                ps4.executeUpdate();

                ps5.setInt(1, id);
                ps5.executeUpdate();

                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar el período lectivo en cascada: " + error.getMessage());
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
package dao;

import clases.Curso;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Curso_dao implements Dao<Curso, Integer> {

    @Override
    public void insertar(Curso objeto) {
        String consulta = "INSERT INTO curso (id_periodo, id_materia, id_docente, año, division, modalidad, turno, cupo_maximo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_periodo());
            ps.setInt(2, objeto.getId_materia());
            ps.setInt(3, objeto.getId_docente());
            ps.setObject(4, objeto.getAño());
            ps.setString(5, objeto.getDivision());
            ps.setString(6, objeto.getModalidad());
            ps.setString(7, objeto.getTurno());
            ps.setInt(8, objeto.getCupo_maximo());
            ps.setObject(9, objeto.getFecha_inicio());
            ps.setObject(10, objeto.getFecha_fin());
            ps.setString(11, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el curso: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Curso> obtenerTodos() {
        List<Curso> listaCursos = new ArrayList<>();
        String consulta = "SELECT * FROM curso";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Curso curso = new Curso();

                curso.setId_curso(rs.getInt("id_curso"));
                curso.setId_periodo(rs.getInt("id_periodo"));
                curso.setId_materia(rs.getInt("id_materia"));
                curso.setId_docente(rs.getInt("id_docente"));
                curso.setAño(rs.getInt("año"));
                curso.setDivision(rs.getString("division"));
                curso.setModalidad(rs.getString("modalidad"));
                curso.setTurno(rs.getString("turno"));
                curso.setCupo_maximo(rs.getInt("cupo_maximo"));
                
                Date fInicio = rs.getDate("fecha_inicio");
                curso.setFecha_inicio(fInicio != null ? fInicio.toLocalDate() : null);
                
                Date fFin = rs.getDate("fecha_fin");
                curso.setFecha_fin(fFin != null ? fFin.toLocalDate() : null);
                
                curso.setEstado(rs.getString("estado"));

                listaCursos.add(curso);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de cursos: " + error.getMessage());
            error.printStackTrace();
        }

        return listaCursos;
    }

    @Override
    public void modificar(Curso objeto) {
        String consulta = "UPDATE curso SET id_periodo = ?, id_materia = ?, id_docente = ?, año = ?, division = ?, modalidad = ?, turno = ?, cupo_maximo = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_curso = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_periodo());
            ps.setInt(2, objeto.getId_materia());
            ps.setInt(3, objeto.getId_docente());
            ps.setObject(4, objeto.getAño());
            ps.setString(5, objeto.getDivision());
            ps.setString(6, objeto.getModalidad());
            ps.setString(7, objeto.getTurno());
            ps.setInt(8, objeto.getCupo_maximo());
            ps.setObject(9, objeto.getFecha_inicio());
            ps.setObject(10, objeto.getFecha_fin());
            ps.setString(11, objeto.getEstado());

            ps.setInt(12, objeto.getId_curso());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el curso: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        //al borrar un curso debemos limpiar calificaciones, inscripciones y columnas de notas asociadas
        String consultaCalificaciones = "DELETE FROM calificacion WHERE id_columna_notas IN (SELECT id_columna_notas FROM columna_notas WHERE id_curso = ?) OR id_inscripcion IN (SELECT id_inscripcion FROM inscripcion WHERE id_curso = ?)";
        String consultaInscripciones = "DELETE FROM inscripcion WHERE id_curso = ?";
        String consultaColumnas = "DELETE FROM columna_notas WHERE id_curso = ?";
        String consultaCurso = "DELETE FROM curso WHERE id_curso = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaCalificaciones);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaInscripciones);
                 PreparedStatement ps3 = conexion.prepareStatement(consultaColumnas);
                 PreparedStatement ps4 = conexion.prepareStatement(consultaCurso)) {

                ps1.setInt(1, id);
                ps1.setInt(2, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                ps3.executeUpdate();

                ps4.setInt(1, id);
                ps4.executeUpdate();

                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar el curso en cascada: " + error.getMessage());
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
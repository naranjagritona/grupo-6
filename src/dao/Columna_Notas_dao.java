package dao;

import clases.Columna_Notas;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Columna_Notas_dao implements Dao<Columna_Notas, Integer> {

    @Override
    public void insertar(Columna_Notas objeto) {
        String consulta = "INSERT INTO columna_notas (id_curso, titulo, tipo, nota_maxima, nota_aprobacion, fecha_evaluacion, fecha_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_curso());
            ps.setString(2, objeto.getTitulo());
            ps.setString(3, objeto.getTipo());
            ps.setObject(4, objeto.getNota_maxima());
            ps.setObject(5, objeto.getNota_aprobacion());
            ps.setObject(6, objeto.getFecha_evaluacion());
            ps.setObject(7, objeto.getFecha_cierre());
            ps.setString(8, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la columna de notas: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Columna_Notas> obtenerTodos() {
        List<Columna_Notas> lista = new ArrayList<>();
        String consulta = "SELECT * FROM columna_notas";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Columna_Notas columna = new Columna_Notas();

                columna.setId_columna_notas(rs.getInt("id_columna_notas"));
                columna.setId_curso(rs.getInt("id_curso"));
                columna.setTitulo(rs.getString("titulo"));
                columna.setTipo(rs.getString("tipo"));
                columna.setNota_maxima(rs.getDouble("nota_maxima"));
                columna.setNota_aprobacion(rs.getDouble("nota_aprobacion"));
                
                Date fEval = rs.getDate("fecha_evaluacion");
                columna.setFecha_evaluacion(fEval != null ? fEval.toLocalDate() : null);
                
                Date fCierre = rs.getDate("fecha_cierre");
                columna.setFecha_cierre(fCierre != null ? fCierre.toLocalDate() : null);
                
                columna.setEstado(rs.getString("estado"));

                lista.add(columna);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de columnas de notas: " + error.getMessage());
            error.printStackTrace();
        }

        return lista;
    }

    @Override
    public void modificar(Columna_Notas objeto) {
        String consulta = "UPDATE columna_notas SET id_curso = ?, titulo = ?, tipo = ?, nota_maxima = ?, nota_aprobacion = ?, fecha_evaluacion = ?, fecha_cierre = ?, estado = ? WHERE id_columna_notas = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_curso());
            ps.setString(2, objeto.getTitulo());
            ps.setString(3, objeto.getTipo());
            ps.setObject(4, objeto.getNota_maxima());
            ps.setObject(5, objeto.getNota_aprobacion());
            ps.setObject(6, objeto.getFecha_evaluacion());
            ps.setObject(7, objeto.getFecha_cierre());
            ps.setString(8, objeto.getEstado());

            ps.setInt(9, objeto.getId_columna_notas());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la columna de notas: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consultaCalificaciones = "DELETE FROM calificacion WHERE id_columna_notas = ?";
        String consultaColumna = "DELETE FROM columna_notas WHERE id_columna_notas = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaCalificaciones);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaColumna)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar la columna de notas: " + error.getMessage());
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
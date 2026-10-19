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

    public void insertarVerificando(Columna_Notas objeto) throws SQLException {
        String sql = "INSERT INTO columna_notas (id_curso, titulo, tipo, nota_maxima, nota_aprobacion, fecha_evaluacion, fecha_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = Conexion.getConnection()) {
            if (conexion == null) throw new SQLException("No se pudo establecer conexión con la base de datos.");
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, objeto.getId_curso());
                ps.setString(2, objeto.getTitulo());
                ps.setString(3, objeto.getTipo());
                ps.setDouble(4, objeto.getNota_maxima());
                ps.setDouble(5, objeto.getNota_aprobacion());
                ps.setObject(6, objeto.getFecha_evaluacion());
                ps.setObject(7, objeto.getFecha_cierre());
                ps.setString(8, objeto.getEstado());
                ps.executeUpdate();
            }
        }
    }

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
    public Columna_Notas obtenerPorId(Integer id) {
        Columna_Notas columna = null;
        String consulta = "SELECT * FROM columna_notas WHERE id_columna_notas = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    columna = new Columna_Notas();

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
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la columna de notas por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return columna;
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
        String consulta = "DELETE FROM columna_notas WHERE id_columna_notas = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la columna de notas: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

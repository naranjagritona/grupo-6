package dao;

import clases.Resultado_Materia;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Resultado_Materia_dao implements Dao<Resultado_Materia, Integer> {

    @Override
    public void insertar(Resultado_Materia objeto) {
        String consulta = "INSERT INTO resultado_materia (id_inscripcion, promedio, resultado, fecha_calculo) VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_inscripcion());
            ps.setObject(2, objeto.getPromedio());
            ps.setString(3, objeto.getResultado());
            ps.setObject(4, objeto.getFecha_calculo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el resultado de materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Resultado_Materia> obtenerTodos() {
        List<Resultado_Materia> listaResultados = new ArrayList<>();
        String consulta = "SELECT * FROM resultado_materia";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Resultado_Materia resultado = new Resultado_Materia();

                resultado.setId_resultado(rs.getInt("id_resultado"));
                resultado.setId_inscripcion(rs.getInt("id_inscripcion"));
                resultado.setPromedio(rs.getDouble("promedio"));
                resultado.setResultado(rs.getString("resultado"));
                
                Date fCalc = rs.getDate("fecha_calculo");
                resultado.setFecha_calculo(fCalc != null ? fCalc.toLocalDate() : null);

                listaResultados.add(resultado);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de resultados de materias: " + error.getMessage());
            error.printStackTrace();
        }

        return listaResultados;
    }

    @Override
    public void modificar(Resultado_Materia objeto) {
        String consulta = "UPDATE resultado_materia SET id_inscripcion = ?, promedio = ?, resultado = ?, fecha_calculo = ? WHERE id_resultado = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_inscripcion());
            ps.setObject(2, objeto.getPromedio());
            ps.setString(3, objeto.getResultado());
            ps.setObject(4, objeto.getFecha_calculo());

            ps.setInt(5, objeto.getId_resultado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el resultado de materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM resultado_materia WHERE id_resultado = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el resultado de materia: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
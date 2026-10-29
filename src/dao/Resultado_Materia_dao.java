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

// consultamos y modificamos los registros de resultado materia en la base de datos
public class Resultado_Materia_dao implements Dao<Resultado_Materia, Integer> {

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Resultado_Materia objeto) {
        throw new IllegalStateException("El resultado final se calcula desde Calificaciones para conservar la relación con la materia.");
    }

    // consulta un registro usando su identificador
    @Override
    public Resultado_Materia obtenerPorId(Integer id) {
        Resultado_Materia resultado = null;
        String consulta = "SELECT * FROM resultado_materia WHERE id_resultado_materia = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = new Resultado_Materia();

                    resultado.setId_resultado(rs.getInt("id_resultado_materia"));
                    resultado.setId_inscripcion(rs.getInt("id_inscripcion"));
                    resultado.setPromedio(rs.getDouble("promedio"));
                    resultado.setResultado(rs.getString("resultado"));

                    Date fCalc = rs.getDate("fecha_calculo");
                    resultado.setFecha_calculo(fCalc != null ? fCalc.toLocalDate() : null);
                }
            }

        } catch (SQLException error) {
            System.err.println(
                    "Error al obtener el resultado de materia por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return resultado;
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Resultado_Materia> obtenerTodos() {
        List<Resultado_Materia> listaResultados = new ArrayList<>();
        String consulta = "SELECT * FROM resultado_materia";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Resultado_Materia resultado = new Resultado_Materia();

                resultado.setId_resultado(rs.getInt("id_resultado_materia"));
                resultado.setId_inscripcion(rs.getInt("id_inscripcion"));
                resultado.setPromedio(rs.getDouble("promedio"));
                resultado.setResultado(rs.getString("resultado"));

                Date fCalc = rs.getDate("fecha_calculo");
                resultado.setFecha_calculo(fCalc != null ? fCalc.toLocalDate() : null);

                listaResultados.add(resultado);
            }

        } catch (SQLException error) {
            System.err.println(
                    "Error al obtener la lista de resultados de materias: " + error.getMessage());
            error.printStackTrace();
        }

        return listaResultados;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Resultado_Materia objeto) {
        throw new IllegalStateException("El resultado final se calcula desde Calificaciones para conservar la relación con la materia.");
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        throw new IllegalStateException("El resultado final se calcula desde Calificaciones para conservar la relación con la materia.");
    }
}

package dao;

import clases.Calificacion_Historial;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de calificacion historial en la base de datos
public class Calificacion_Historial_dao implements Dao<Calificacion_Historial, Integer> {

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Calificacion_Historial objeto) {
        String consulta =
                "INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva,"
                        + " motivo) VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_calificacion());
            ps.setObject(2, objeto.getNota_anterior());
            ps.setObject(3, objeto.getNota_nueva());
            ps.setString(4, objeto.getMotivo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println(
                    "Error al insertar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Calificacion_Historial obtenerPorId(Integer id) {
        Calificacion_Historial historial = null;
        String consulta =
                "SELECT * FROM calificacion_historial WHERE id_calificacion_historial = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    historial = new Calificacion_Historial();

                    historial.setId_historial(rs.getInt("id_calificacion_historial"));
                    historial.setId_calificacion(rs.getInt("id_calificacion"));
                    historial.setNota_anterior(
                            rs.getObject("nota_anterior") != null
                                    ? rs.getDouble("nota_anterior")
                                    : null);
                    historial.setNota_nueva(rs.getDouble("nota_nueva"));
                    Timestamp ts = rs.getTimestamp("fecha_modificacion");
                    historial.setFecha_modificacion(ts != null ? ts.toLocalDateTime() : null);
                    historial.setMotivo(rs.getString("motivo"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el historial por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return historial;
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Calificacion_Historial> obtenerTodos() {
        List<Calificacion_Historial> listaHistorial = new ArrayList<>();
        String consulta = "SELECT * FROM calificacion_historial";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Calificacion_Historial historial = new Calificacion_Historial();

                historial.setId_historial(rs.getInt("id_calificacion_historial"));
                historial.setId_calificacion(rs.getInt("id_calificacion"));
                historial.setNota_anterior(
                        rs.getObject("nota_anterior") != null
                                ? rs.getDouble("nota_anterior")
                                : null);
                historial.setNota_nueva(rs.getDouble("nota_nueva"));
                Timestamp ts = rs.getTimestamp("fecha_modificacion");
                historial.setFecha_modificacion(ts != null ? ts.toLocalDateTime() : null);
                historial.setMotivo(rs.getString("motivo"));

                listaHistorial.add(historial);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de historiales: " + error.getMessage());
            error.printStackTrace();
        }

        return listaHistorial;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Calificacion_Historial objeto) {
        String consulta =
                "UPDATE calificacion_historial SET id_calificacion = ?, nota_anterior = ?,"
                        + " nota_nueva = ?, motivo = ? WHERE id_calificacion_historial = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_calificacion());
            ps.setObject(2, objeto.getNota_anterior());
            ps.setObject(3, objeto.getNota_nueva());
            ps.setString(4, objeto.getMotivo());
            ps.setInt(5, objeto.getId_historial());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println(
                    "Error al modificar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM calificacion_historial WHERE id_calificacion_historial = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println(
                    "Error al eliminar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

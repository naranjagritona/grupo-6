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

public class Calificacion_Historial_dao implements Dao<Calificacion_Historial, Integer> {

    @Override
    public void insertar(Calificacion_Historial objeto) {
        String consulta = "INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva, motivo) VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_calificacion());
            ps.setObject(2, objeto.getNota_anterior());
            ps.setObject(3, objeto.getNota_nueva());
            ps.setString(4, objeto.getMotivo());

            //ejecutamos la consulta en la BD
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Calificacion_Historial> obtenerTodos() {
        List<Calificacion_Historial> listaHistorial = new ArrayList<>();
        String consulta = "SELECT * FROM calificacion_historial";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            //recorremos todos los registros devueltos por la consulta
            while (rs.next()) {
                Calificacion_Historial historial = new Calificacion_Historial();

                historial.setId_historial(rs.getInt("id_historial"));
                historial.setId_calificacion(rs.getInt("id_calificacion"));
                historial.setNota_anterior(rs.getObject("nota_anterior") != null ? rs.getDouble("nota_anterior") : null);
                historial.setNota_nueva(rs.getDouble("nota_nueva"));
                Timestamp ts = rs.getTimestamp("fecha_modificacion");
                historial.setFecha_modificacion(ts != null ? ts.toLocalDateTime() : null);
                historial.setMotivo(rs.getString("motivo"));

                //se agrega la instancia cargada a la lista
                listaHistorial.add(historial);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de historiales: " + error.getMessage());
            error.printStackTrace();
        }

        return listaHistorial;
    }

    @Override
    public void modificar(Calificacion_Historial objeto) {
        String consulta = "UPDATE calificacion_historial SET id_calificacion = ?, nota_anterior = ?, nota_nueva = ?, motivo = ? WHERE id_historial = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_calificacion());
            ps.setObject(2, objeto.getNota_anterior());
            ps.setObject(3, objeto.getNota_nueva());
            ps.setString(4, objeto.getMotivo());

            //seteamos la clave primaria para el WHERE
            ps.setInt(5, objeto.getId_historial());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM calificacion_historial WHERE id_historial = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            //asignamos el id del registro a eliminar
            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
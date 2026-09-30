package dao;

import clases.Log_Usuario;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Log_Usuario_dao implements Dao<Log_Usuario, Integer> {

    @Override
    public void insertar(Log_Usuario objeto) {
        String consulta = "INSERT INTO log_usuario (id_usuario, accion, campo_modificado, valor_anterior, valor_nuevo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_usuario());
            ps.setString(2, objeto.getAccion());
            ps.setString(3, objeto.getCampo_modificado());
            ps.setString(4, objeto.getValor_anterior());
            ps.setString(5, objeto.getValor_nuevo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el log de usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Log_Usuario> obtenerTodos() {
        List<Log_Usuario> listaLogs = new ArrayList<>();
        String consulta = "SELECT * FROM log_usuario";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Log_Usuario log = new Log_Usuario();

                log.setId_log_usuario(rs.getInt("id_log_usuario"));
                log.setId_usuario(rs.getInt("id_usuario"));
                log.setAccion(rs.getString("accion"));
                log.setCampo_modificado(rs.getString("campo_modificado"));
                log.setValor_anterior(rs.getString("valor_anterior"));
                log.setValor_nuevo(rs.getString("valor_nuevo"));
                Timestamp ts = rs.getTimestamp("fecha_hora");
                log.setFecha_hora(ts != null ? ts.toLocalDateTime() : null);

                listaLogs.add(log);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de logs: " + error.getMessage());
            error.printStackTrace();
        }

        return listaLogs;
    }

    @Override
    public void modificar(Log_Usuario objeto) {
        String consulta = "UPDATE log_usuario SET id_usuario = ?, accion = ?, campo_modificado = ?, valor_anterior = ?, valor_nuevo = ? WHERE id_log_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_usuario());
            ps.setString(2, objeto.getAccion());
            ps.setString(3, objeto.getCampo_modificado());
            ps.setString(4, objeto.getValor_anterior());
            ps.setString(5, objeto.getValor_nuevo());

            ps.setInt(6, objeto.getId_log_usuario());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el log de usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM log_usuario WHERE id_log_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el log de usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
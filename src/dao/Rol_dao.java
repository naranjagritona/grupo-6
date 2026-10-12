package dao;

import clases.Rol;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Rol_dao implements Dao<Rol, Integer> {

    @Override
    public void insertar(Rol objeto) {
        String consulta = "INSERT INTO rol (nombre, descripcion_rol) VALUES (?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getDescripcion_rol());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el rol: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public Rol obtenerPorId(Integer id) {
        Rol rol = null;
        String consulta = "SELECT * FROM rol WHERE id_rol = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rol = new Rol();

                    rol.setId_rol(rs.getInt("id_rol"));
                    rol.setNombre(rs.getString("nombre"));
                    rol.setDescripcion_rol(rs.getString("descripcion_rol"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el rol por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return rol;
    }

    @Override
    public List<Rol> obtenerTodos() {
        List<Rol> listaRoles = new ArrayList<>();
        String consulta = "SELECT * FROM rol";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Rol rol = new Rol();

                rol.setId_rol(rs.getInt("id_rol"));
                rol.setNombre(rs.getString("nombre"));
                rol.setDescripcion_rol(rs.getString("descripcion_rol"));

                listaRoles.add(rol);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de roles: " + error.getMessage());
            error.printStackTrace();
        }

        return listaRoles;
    }

    @Override
    public void modificar(Rol objeto) {
        String consulta = "UPDATE rol SET nombre = ?, descripcion_rol = ? WHERE id_rol = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getDescripcion_rol());
            ps.setInt(3, objeto.getId_rol());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el rol: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM rol WHERE id_rol = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el rol: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
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
        //al eliminar un rol borramos en cascada los logs, alumnos, docentes y usuarios asociados
        String consultaLogs = "DELETE FROM log_usuario WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE id_rol = ?)";
        String consultaAlumnos = "DELETE FROM alumno WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE id_rol = ?)";
        String consultaDocentes = "DELETE FROM docente WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE id_rol = ?)";
        String consultaUsuarios = "DELETE FROM usuario WHERE id_rol = ?";
        String consultaRol = "DELETE FROM rol WHERE id_rol = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaLogs);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaAlumnos);
                 PreparedStatement ps3 = conexion.prepareStatement(consultaDocentes);
                 PreparedStatement ps4 = conexion.prepareStatement(consultaUsuarios);
                 PreparedStatement ps5 = conexion.prepareStatement(consultaRol)) {

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
            System.err.println("Error al eliminar el rol en cascada: " + error.getMessage());
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
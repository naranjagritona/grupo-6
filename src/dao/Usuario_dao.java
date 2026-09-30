package dao;

import clases.Usuario;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuario_dao implements Dao<Usuario, Integer> {

    @Override
    public void insertar(Usuario objeto) {
        String consulta = "INSERT INTO usuario (id_rol, nombre_usuario, contraseña) VALUES (?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_rol());
            ps.setString(2, objeto.getNombre_usuario());
            ps.setString(3, objeto.getContraseña());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Usuario> obtenerTodos() {
        List<Usuario> listaUsuarios = new ArrayList<>();
        String consulta = "SELECT * FROM usuario";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario();

                usuario.setId_usuario(rs.getInt("id_usuario"));
                usuario.setId_rol(rs.getInt("id_rol"));
                usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                usuario.setContraseña(rs.getString("contraseña"));

                listaUsuarios.add(usuario);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de usuarios: " + error.getMessage());
            error.printStackTrace();
        }

        return listaUsuarios;
    }

    @Override
    public void modificar(Usuario objeto) {
        String consulta = "UPDATE usuario SET id_rol = ?, nombre_usuario = ?, contraseña = ? WHERE id_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_rol());
            ps.setString(2, objeto.getNombre_usuario());
            ps.setString(3, objeto.getContraseña());
            
            //seteamos el id del usuario para el WHERE
            ps.setInt(4, objeto.getId_usuario());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        //al eliminar usuario borramos también los registros vinculados en alumno, docente y logs
        String consultaLogs = "DELETE FROM log_usuario WHERE id_usuario = ?";
        String consultaAlumno = "DELETE FROM alumno WHERE id_usuario = ?";
        String consultaDocente = "DELETE FROM docente WHERE id_usuario = ?";
        String consultaUsuario = "DELETE FROM usuario WHERE id_usuario = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaLogs);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaAlumno);
                 PreparedStatement ps3 = conexion.prepareStatement(consultaDocente);
                 PreparedStatement ps4 = conexion.prepareStatement(consultaUsuario)) {

                ps1.setInt(1, id);
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
            System.err.println("Error al eliminar el usuario en cascada: " + error.getMessage());
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
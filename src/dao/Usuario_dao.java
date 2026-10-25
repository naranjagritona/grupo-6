package dao;

import clases.Usuario; 
import conexion.Conexion;
import seguridad.Contrasenas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuario_dao implements Dao<Usuario, Integer> {

    public Usuario autenticar(String nombreUsuario, String contraseña) {
        Usuario usuario = null;
        String consulta = "SELECT * FROM usuario WHERE nombre_usuario = ? ORDER BY id_usuario";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Buscamos el usuario y después verificamos su contraseña con bcrypt.
                    if (!Contrasenas.verificar(contraseña, rs.getString("contraseña"))) continue;
                    usuario = new Usuario();
                    usuario.setId_usuario(rs.getInt("id_usuario"));
                    usuario.setId_rol(rs.getInt("id_rol"));
                    usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                    usuario.setContraseña(rs.getString("contraseña"));
                    return usuario;
                }
            }

        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el usuario en la base de datos.", error);
        }

        return usuario;
    }

    public void cerrarSesion(Usuario usuario) {
        if (usuario != null) {
            System.out.println("el usuario " + usuario.getNombre_usuario() + " ha cerrado sesión correctamente.");
        }
    }

    @Override
    public void insertar(Usuario objeto) {
        String consulta = "INSERT INTO usuario (id_rol, nombre_usuario, contraseña) VALUES (?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_rol());
            ps.setString(2, objeto.getNombre_usuario());
            ps.setString(3, Contrasenas.paraGuardar(objeto.getContraseña()));

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public Usuario obtenerPorId(Integer id) {
        Usuario usuario = null;
        String consulta = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setId_usuario(rs.getInt("id_usuario"));
                    usuario.setId_rol(rs.getInt("id_rol"));
                    usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                    usuario.setContraseña(rs.getString("contraseña"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el usuario por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return usuario;
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
            ps.setString(3, Contrasenas.paraGuardar(objeto.getContraseña()));
            ps.setInt(4, objeto.getId_usuario());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el usuario: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
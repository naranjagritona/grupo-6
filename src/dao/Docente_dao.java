package dao;

import clases.Docente;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Docente_dao implements Dao<Docente, Integer> {

    @Override
    public void insertar(Docente objeto) {
        String consulta = "INSERT INTO docente (id_usuario, nombre, apellido, dni, legajo, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setObject(1, objeto.getId_usuario());
            ps.setString(2, objeto.getNombre());
            ps.setString(3, objeto.getApellido());
            ps.setString(4, objeto.getDni());
            ps.setString(5, objeto.getLegajo());
            ps.setString(6, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Docente> obtenerTodos() {
        List<Docente> listaDocentes = new ArrayList<>();
        String consulta = "SELECT * FROM docente";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Docente docente = new Docente();

                docente.setId_docente(rs.getInt("id_docente"));
                docente.setId_usuario((Integer) rs.getObject("id_usuario"));
                docente.setNombre(rs.getString("nombre"));
                docente.setApellido(rs.getString("apellido"));
                docente.setDni(rs.getString("dni"));
                docente.setLegajo(rs.getString("legajo"));
                docente.setEstado(rs.getString("estado"));

                listaDocentes.add(docente);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de docentes: " + error.getMessage());
            error.printStackTrace();
        }

        return listaDocentes;
    }

    @Override
    public void modificar(Docente objeto) {
        String consulta = "UPDATE docente SET id_usuario = ?, nombre = ?, apellido = ?, dni = ?, legajo = ?, estado = ? WHERE id_docente = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setObject(1, objeto.getId_usuario());
            ps.setString(2, objeto.getNombre());
            ps.setString(3, objeto.getApellido());
            ps.setString(4, objeto.getDni());
            ps.setString(5, objeto.getLegajo());
            ps.setString(6, objeto.getEstado());

            ps.setInt(7, objeto.getId_docente());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consultaDocente = "DELETE FROM docente WHERE id_docente = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consultaDocente)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
package dao;

import clases.Materia;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Materia_dao implements Dao<Materia, Integer> {

    @Override
    public void insertar(Materia objeto) {
        String consulta = "INSERT INTO materia (nombre_materia) VALUES (?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre_materia());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Materia> obtenerTodos() {
        List<Materia> listaMaterias = new ArrayList<>();
        String consulta = "SELECT * FROM materia";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Materia materia = new Materia();

                materia.setId_materia(rs.getInt("id_materia"));
                materia.setNombre_materia(rs.getString("nombre_materia"));

                listaMaterias.add(materia);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de materias: " + error.getMessage());
            error.printStackTrace();
        }

        return listaMaterias;
    }

    @Override
    public void modificar(Materia objeto) {
        String consulta = "UPDATE materia SET nombre_materia = ? WHERE id_materia = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre_materia());
            ps.setInt(2, objeto.getId_materia());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        String consultaMateria = "DELETE FROM materia WHERE id_materia = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consultaMateria)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
package dao;

import clases.Materia;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de materia en la base de datos
public class Materia_dao implements Dao<Materia, Integer> {

    // inserta el registro recibido en la tabla correspondiente
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

    // consulta un registro usando su identificador
    @Override
    public Materia obtenerPorId(Integer id) {
        Materia materia = null;
        String consulta = "SELECT * FROM materia WHERE id_materia = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    materia = new Materia();
                    materia.setId_materia(rs.getInt("id_materia"));
                    materia.setNombre_materia(rs.getString("nombre_materia"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la materia por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return materia;
    }

    // consulta los registros y los devuelve como una lista de objetos
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

    // actualiza los datos del registro seleccionado
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

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM materia WHERE id_materia = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

package dao;

import clases.Responsable;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Responsable_dao implements Dao<Responsable, Integer> {

    @Override
    public void insertar(Responsable objeto) {
        String consulta = "INSERT INTO responsable (nombre, apellido, dni, tipo_relacion) VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getApellido());
            ps.setString(3, objeto.getDni());
            ps.setString(4, objeto.getTipo_relacion());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el responsable: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public List<Responsable> obtenerTodos() {
        List<Responsable> listaResponsables = new ArrayList<>();
        String consulta = "SELECT * FROM responsable";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Responsable responsable = new Responsable();

                responsable.setId_responsable(rs.getInt("id_responsable"));
                responsable.setNombre(rs.getString("nombre"));
                responsable.setApellido(rs.getString("apellido"));
                responsable.setDni(rs.getString("dni"));
                responsable.setTipo_relacion(rs.getString("tipo_relacion"));

                listaResponsables.add(responsable);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de responsables: " + error.getMessage());
            error.printStackTrace();
        }

        return listaResponsables;
    }

    @Override
    public void modificar(Responsable objeto) {
        String consulta = "UPDATE responsable SET nombre = ?, apellido = ?, dni = ?, tipo_relacion = ? WHERE id_responsable = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getApellido());
            ps.setString(3, objeto.getDni());
            ps.setString(4, objeto.getTipo_relacion());

            ps.setInt(5, objeto.getId_responsable());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el responsable: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        //desvinculamos el responsable en la tabla alumno asignando NULL antes de eliminarlo
        String desvincularAlumno = "UPDATE alumno SET id_responsable = NULL WHERE id_responsable = ?";
        String consultaResponsable = "DELETE FROM responsable WHERE id_responsable = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(desvincularAlumno);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaResponsable)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar el responsable: " + error.getMessage());
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
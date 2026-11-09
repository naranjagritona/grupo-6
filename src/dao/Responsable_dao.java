package dao;

import clases.Responsable;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de responsable en la base de datos
public class Responsable_dao extends Consulta_academica_dao implements Dao<Responsable, Integer> {
    // guardamos el responsable usando la conexión del alta de la cuenta del alumno
    public int crear(java.sql.Connection db, Responsable r) throws java.sql.SQLException {
        return insertarRegistro(db,"INSERT INTO responsable(nombre,apellido,dni,tipo_relacion) VALUES(?,?,?,?)",r.getNombre(),r.getApellido(),r.getDni(),r.getTipo_relacion());
    }

    // modificamos los datos del responsable sin cambiar el vínculo con la inscripción del alumno
    public void actualizarAdministracion(java.sql.Connection db, Responsable r) throws java.sql.SQLException {
        ejecutar(db,"UPDATE responsable SET nombre=?,apellido=?,dni=?,tipo_relacion=? WHERE id_responsable=?",r.getNombre(),r.getApellido(),r.getDni(),r.getTipo_relacion(),r.getId_responsable());
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Responsable objeto) {
        String consulta =
                "INSERT INTO responsable (nombre, apellido, dni, tipo_relacion) VALUES (?, ?, ?,"
                        + " ?)";

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

    // consulta un registro usando su identificador
    @Override
    public Responsable obtenerPorId(Integer id) {
        Responsable responsable = null;
        String consulta = "SELECT * FROM responsable WHERE id_responsable = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    responsable = new Responsable();

                    responsable.setId_responsable(rs.getInt("id_responsable"));
                    responsable.setNombre(rs.getString("nombre"));
                    responsable.setApellido(rs.getString("apellido"));
                    responsable.setDni(rs.getString("dni"));
                    responsable.setTipo_relacion(rs.getString("tipo_relacion"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el responsable por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return responsable;
    }

    // consulta los registros y los devuelve como una lista de objetos
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

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Responsable objeto) {
        String consulta =
                "UPDATE responsable SET nombre = ?, apellido = ?, dni = ?, tipo_relacion = ? WHERE"
                        + " id_responsable = ?";

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

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM responsable WHERE id_responsable = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el responsable: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

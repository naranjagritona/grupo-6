package dao;

import clases.Docente;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de docente en la base de datos
public class Docente_dao extends Consulta_academica_dao implements Dao<Docente, Integer> {
    // registramos el docente junto con su cuenta y el legajo generado por el servicio
    public int crear(java.sql.Connection db, Docente d) throws java.sql.SQLException {
        return insertarRegistro(db,"INSERT INTO docente(id_usuario,nombre,apellido,dni,legajo,estado) VALUES(?,?,?,?,?,?)",d.getId_usuario(),d.getNombre(),d.getApellido(),d.getDni(),d.getLegajo(),d.getEstado());
    }

    // actualizamos los datos personales sin aceptar un legajo o una cuenta diferentes
    public void actualizarAdministracion(java.sql.Connection db, Docente d) throws java.sql.SQLException {
        ejecutar(db,"UPDATE docente SET nombre=?,apellido=?,dni=?,estado=? WHERE id_docente=?",d.getNombre(),d.getApellido(),d.getDni(),d.getEstado(),d.getId_docente());
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Docente objeto) {
        String consulta =
                "INSERT INTO docente (id_usuario, nombre, apellido, dni, legajo, estado) VALUES (?,"
                        + " ?, ?, ?, ?, ?)";

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

    // consulta un registro usando su identificador
    @Override
    public Docente obtenerPorId(Integer id) {
        Docente docente = null;
        String consulta = "SELECT * FROM docente WHERE id_docente = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    docente = new Docente();

                    docente.setId_docente(rs.getInt("id_docente"));
                    docente.setId_usuario((Integer) rs.getObject("id_usuario"));
                    docente.setNombre(rs.getString("nombre"));
                    docente.setApellido(rs.getString("apellido"));
                    docente.setDni(rs.getString("dni"));
                    docente.setLegajo(rs.getString("legajo"));
                    docente.setEstado(rs.getString("estado"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el docente por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return docente;
    }

    // consulta los registros y los devuelve como una lista de objetos
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

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Docente objeto) {
        String consulta =
                "UPDATE docente SET id_usuario = ?, nombre = ?, apellido = ?, dni = ?, legajo = ?,"
                        + " estado = ? WHERE id_docente = ?";

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

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM docente WHERE id_docente = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

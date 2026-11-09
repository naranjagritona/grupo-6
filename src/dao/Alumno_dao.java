package dao;

import clases.Alumno;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de alumno en la base de datos
public class Alumno_dao extends Consulta_academica_dao implements Dao<Alumno, Integer> {
    // guardamos el alumno con la cuenta y el responsable dentro de la misma transacción
    public int crear(java.sql.Connection db, Alumno a) throws java.sql.SQLException {
        return insertarRegistro(db, "INSERT INTO alumno(id_usuario,id_responsable,nombre,apellido,dni,matricula,estado) VALUES(?,?,?,?,?,?,?)", a.getId_usuario(),a.getId_responsable(),a.getNombre(),a.getApellido(),a.getDni(),a.getMatricula(),a.getEstado());
    }

    // conservamos la matrícula y la cuenta al modificar los datos administrativos del alumno
    public void actualizarAdministracion(java.sql.Connection db, Alumno a) throws java.sql.SQLException {
        ejecutar(db,"UPDATE alumno SET id_responsable=?,nombre=?,apellido=?,dni=?,estado=? WHERE id_alumno=?",a.getId_responsable(),a.getNombre(),a.getApellido(),a.getDni(),a.getEstado(),a.getId_alumno());
    }

    // buscamos únicamente alumnos activos para las sugerencias del formulario de inscripción
    public java.util.List<Alumno> activosParaInscripcion() {
        return obtenerTodos().stream().filter(a -> "Activo".equals(a.getEstado())).toList();
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Alumno objeto) {
        String consulta =
                "INSERT INTO alumno (id_usuario, id_responsable, nombre, apellido, dni, matricula,"
                        + " estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            // reemplazamos los signos ? por los atributos del objeto recibido
            ps.setObject(1, objeto.getId_usuario());
            ps.setObject(2, objeto.getId_responsable());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getDni());
            ps.setString(6, objeto.getMatricula());
            ps.setString(7, objeto.getEstado());

            // ejecutamos la consulta en la BD
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el alumno: " + error.getMessage());
            error.printStackTrace(); // imprime el error
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Alumno obtenerPorId(Integer id) {
        Alumno alumno = null;
        String consulta = "SELECT * FROM alumno WHERE id_alumno = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    alumno = new Alumno();

                    alumno.setId_alumno(rs.getInt("id_alumno"));
                    alumno.setId_usuario((Integer) rs.getObject("id_usuario"));
                    alumno.setId_responsable((Integer) rs.getObject("id_responsable"));
                    alumno.setNombre(rs.getString("nombre"));
                    alumno.setApellido(rs.getString("apellido"));
                    alumno.setDni(rs.getString("dni"));
                    alumno.setMatricula(rs.getString("matricula"));
                    alumno.setEstado(rs.getString("estado"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el alumno por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return alumno;
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Alumno> obtenerTodos() {
        List<Alumno> listaAlumnos = new ArrayList<>();
        String consulta = "SELECT * FROM alumno";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            // recorremos todos los registros devueltos por la consulta con la variable rs
            while (rs.next()) {
                Alumno alumno = new Alumno();

                // ponemos las columnas del ResultSet y las asignamos al objeto Alumno
                alumno.setId_alumno(rs.getInt("id_alumno"));
                alumno.setId_usuario((Integer) rs.getObject("id_usuario"));
                alumno.setId_responsable((Integer) rs.getObject("id_responsable"));
                alumno.setNombre(rs.getString("nombre"));
                alumno.setApellido(rs.getString("apellido"));
                alumno.setDni(rs.getString("dni"));
                alumno.setMatricula(rs.getString("matricula"));
                alumno.setEstado(rs.getString("estado"));

                // se agrega la instancia cargada a la lista
                listaAlumnos.add(alumno);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de alumnos: " + error.getMessage());
            error.printStackTrace();
        }

        return listaAlumnos;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Alumno objeto) {
        String consulta =
                "UPDATE alumno SET id_usuario = ?, id_responsable = ?, nombre = ?, apellido = ?,"
                        + " dni = ?, matricula = ?, estado = ? WHERE id_alumno = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setObject(1, objeto.getId_usuario());
            ps.setObject(2, objeto.getId_responsable());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getDni());
            ps.setString(6, objeto.getMatricula());
            ps.setString(7, objeto.getEstado());

            // seteamos la clave primaria para modificar los datos del id del alumno deseado (dnd
            // esta el WHERE)
            ps.setInt(8, objeto.getId_alumno());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el alumno: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM alumno WHERE id_alumno = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el alumno: " + error.getMessage());
            error.printStackTrace();
        }
    }
}

package dao;

import clases.Alumno;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//reemplazamos la T por la clase Alumno y el id por Integer
public class Alumno_dao implements Dao<Alumno, Integer> {

    @Override
    public void insertar(Alumno objeto) { 
        String consulta = "INSERT INTO alumno (id_usuario, id_responsable, nombre, apellido, dni, matricula, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {
            
            //reemplazamos los signos ? por los atributos del objeto recibido
            ps.setObject(1, objeto.getId_usuario());
            ps.setObject(2, objeto.getId_responsable());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getDni());
            ps.setString(6, objeto.getMatricula());
            ps.setString(7, objeto.getEstado());
            
            //ejecutamos la consulta en la BD
            ps.executeUpdate();
            
        } catch (SQLException error) {
            System.err.println("Error al insertar el alumno: " + error.getMessage());
            error.printStackTrace(); //imprime el error
        }
    }

    @Override
    public List<Alumno> obtenerTodos() {
        List<Alumno> listaAlumnos = new ArrayList<>();
        String consulta = "SELECT * FROM alumno";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            //recorremos todos los registros devueltos por la consulta
            while (rs.next()) {
                Alumno alumno = new Alumno();
                
                //ponemos las columnas del ResultSet y las asignamos al objeto Alumno
                alumno.setId_alumno(rs.getInt("id_alumno"));
                alumno.setId_usuario((Integer) rs.getObject("id_usuario"));
                alumno.setId_responsable((Integer) rs.getObject("id_responsable"));
                alumno.setNombre(rs.getString("nombre"));
                alumno.setApellido(rs.getString("apellido"));
                alumno.setDni(rs.getString("dni"));
                alumno.setMatricula(rs.getString("matricula"));
                alumno.setEstado(rs.getString("estado"));
                
                //se agrega la instancia cargada a la lista
                listaAlumnos.add(alumno);
            }
            
        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de alumnos: " + error.getMessage());
            error.printStackTrace();
        }
        
        return listaAlumnos;
    }

    @Override
    public void modificar(Alumno objeto) {
        String consulta = "UPDATE alumno SET id_usuario = ?, id_responsable = ?, nombre = ?, apellido = ?, dni = ?, matricula = ?, estado = ? WHERE id_alumno = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {
            
            ps.setObject(1, objeto.getId_usuario());
            ps.setObject(2, objeto.getId_responsable());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getDni());
            ps.setString(6, objeto.getMatricula());
            ps.setString(7, objeto.getEstado());
            
            //seteamos la clave primaria para modificar los datos del id del alumno deseado (dnd esta el WHERE)
            ps.setInt(8, objeto.getId_alumno());
            
            ps.executeUpdate();
            
        } catch (SQLException error) {
            System.err.println("Error al modificar el alumno: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public void eliminar(Integer id) {
        //consultas en orden jerárquico para borrar dependencias en cascada manualmente por seguridad
        String consultaHijos1 = "DELETE FROM calificacion WHERE id_inscripcion IN (SELECT id_inscripcion FROM inscripcion WHERE id_alumno = ?)";
        String consultaHijos2 = "DELETE FROM resultado_materia WHERE id_resultado IN (SELECT id_inscripcion FROM inscripcion WHERE id_alumno = ?)";
        String consultaInscripciones = "DELETE FROM inscripcion WHERE id_alumno = ?";
        String consultaAlumno = "DELETE FROM alumno WHERE id_alumno = ?";

        Connection conexion = null;

        try {
            conexion = Conexion.getConnection();
            //desactivamos autocommit para manejar la transacción de borrado en cascada
            conexion.setAutoCommit(false);

            try (PreparedStatement ps1 = conexion.prepareStatement(consultaHijos1);
                 PreparedStatement ps2 = conexion.prepareStatement(consultaHijos2);
                 PreparedStatement ps3 = conexion.prepareStatement(consultaInscripciones);
                 PreparedStatement ps4 = conexion.prepareStatement(consultaAlumno)) {

                //asignamos el id del alumno a eliminar en todas las subconsultas
                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                ps3.executeUpdate();

                ps4.setInt(1, id);
                ps4.executeUpdate();

                //confirmamos los cambios en la BD si todo salió bien
                conexion.commit();
            }

        } catch (SQLException error) {
            System.err.println("Error al eliminar el alumno en cascada: " + error.getMessage());
            error.printStackTrace();
            if (conexion != null) {
                try {
                    //revertimos los cambios si falla algún paso
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
package dao;

import clases.Inscripcion;
import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Inscripcion_dao implements Dao<Inscripcion, Integer> {

    @Override
    public void insertar(Inscripcion objeto) {
        String consulta = "INSERT INTO inscripcion (id_alumno, id_curso, fecha_inscripcion, estado, motivo_baja, importe_cuota) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_alumno());
            ps.setInt(2, objeto.getId_curso());
            ps.setObject(3, objeto.getFecha_inscripcion());
            ps.setString(4, objeto.getEstado());
            ps.setString(5, objeto.getMotivo_baja());
            ps.setObject(6, objeto.getImporte_cuota());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la inscripción: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
    public Inscripcion obtenerPorId(Integer id) {
        Inscripcion inscripcion = null;
        String consulta = "SELECT * FROM inscripcion WHERE id_inscripcion = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    inscripcion = new Inscripcion();

                    inscripcion.setId_inscripcion(rs.getInt("id_inscripcion"));
                    inscripcion.setId_alumno(rs.getInt("id_alumno"));
                    inscripcion.setId_curso(rs.getInt("id_curso"));
                    
                    Date fecha = rs.getDate("fecha_inscripcion");
                    inscripcion.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                    
                    inscripcion.setEstado(rs.getString("estado"));
                    inscripcion.setMotivo_baja(rs.getString("motivo_baja"));
                    inscripcion.setImporte_cuota(rs.getDouble("importe_cuota"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la inscripción por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return inscripcion;
    }

    @Override
    public List<Inscripcion> obtenerTodos() {
        List<Inscripcion> listaInscripciones = new ArrayList<>();
        String consulta = "SELECT * FROM inscripcion";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Inscripcion inscripcion = new Inscripcion();

                inscripcion.setId_inscripcion(rs.getInt("id_inscripcion"));
                inscripcion.setId_alumno(rs.getInt("id_alumno"));
                inscripcion.setId_curso(rs.getInt("id_curso"));
                
                Date fecha = rs.getDate("fecha_inscripcion");
                inscripcion.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                
                inscripcion.setEstado(rs.getString("estado"));
                inscripcion.setMotivo_baja(rs.getString("motivo_baja"));
                inscripcion.setImporte_cuota(rs.getDouble("importe_cuota"));

                listaInscripciones.add(inscripcion);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de inscripciones: " + error.getMessage());
            error.printStackTrace();
        }

        return listaInscripciones;
    }

    @Override
public void modificar(Inscripcion objeto) {
        String consulta = "UPDATE inscripcion SET id_alumno = ?, id_curso = ?, fecha_inscripcion = ?, estado = ?, motivo_baja = ?, importe_cuota = ? WHERE id_inscripcion = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_alumno());
            ps.setInt(2, objeto.getId_curso());
            ps.setObject(3, objeto.getFecha_inscripcion());
            ps.setString(4, objeto.getEstado());
            ps.setString(5, objeto.getMotivo_baja());
            ps.setObject(6, objeto.getImporte_cuota());
            ps.setInt(7, objeto.getId_inscripcion());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la inscripción: " + error.getMessage());
            error.printStackTrace();
        }
    }

    @Override
public void eliminar(Integer id) {
        String consulta = "DELETE FROM inscripcion WHERE id_inscripcion = ?";

        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la inscripción: " + error.getMessage());
            error.printStackTrace();
        }
    }

 // Busca por alumno (nombre, apellido, DNI), id de curso o estado
public List<Inscripcion> buscarCampo(String filtro) {
        List<Inscripcion> lista = new ArrayList<>();
        String consulta = "SELECT DISTINCT i.* FROM inscripcion i " +
                          "JOIN alumno a ON i.id_alumno = a.id_alumno " +
                          "WHERE a.nombre LIKE ? " +
                          "   OR a.apellido LIKE ? " +
                          "   OR a.dni LIKE ? " +
                          "   OR i.estado LIKE ? " +
                          "   OR CAST(i.id_curso AS CHAR) LIKE ?";
        
        try (Connection conexion = Conexion.getConnection();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {
            
            String wildcard = "%" + filtro + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);
            ps.setString(5, wildcard);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Inscripcion ins = new Inscripcion();
                    ins.setId_inscripcion(rs.getInt("id_inscripcion"));
                    ins.setId_alumno(rs.getInt("id_alumno"));
                    ins.setId_curso(rs.getInt("id_curso"));
                    Date fecha = rs.getDate("fecha_inscripcion");
                    ins.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                    ins.setEstado(rs.getString("estado"));
                    ins.setMotivo_baja(rs.getString("motivo_baja"));
                    ins.setImporte_cuota(rs.getDouble("importe_cuota"));
                    lista.add(ins);
                }
            }
        } catch (SQLException error) {
            System.err.println("Error al buscar inscripciones: " + error.getMessage());
        }
        return lista;
    }

//filtra cada estado de las inscripciones
public List<Inscripcion> obtenerPorEstado(String estado) {
    List<Inscripcion> lista = new ArrayList<>();
    String consulta = "SELECT * FROM inscripcion WHERE estado = ?";
    
    try (Connection conexion = Conexion.getConnection();
         PreparedStatement ps = conexion.prepareStatement(consulta)) {
        
        ps.setString(1, estado);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inscripcion ins = new Inscripcion();
                ins.setId_inscripcion(rs.getInt("id_inscripcion"));
                ins.setId_alumno(rs.getInt("id_alumno"));
                ins.setId_curso(rs.getInt("id_curso"));
                Date fecha = rs.getDate("fecha_inscripcion");
                ins.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                ins.setEstado(rs.getString("estado"));
                ins.setMotivo_baja(rs.getString("motivo_baja"));
                ins.setImporte_cuota(rs.getDouble("importe_cuota"));
                lista.add(ins);
            }
        }
    } catch (SQLException error) {
        System.err.println("Error al filtrar por estado: " + error.getMessage());
    }
    return lista;
}
}
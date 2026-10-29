package dao;

import clases.Calificacion;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de calificacion en la base de datos
public class Calificacion_dao implements Dao<Calificacion, Integer> {

    public void insertarVerificando(Calificacion objeto) throws SQLException {
        try { new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual()).registrarNota(objeto.getId_inscripcion(), objeto.getId_columna_notas(), String.valueOf(objeto.getNota()), objeto.getObservaciones() == null ? "" : objeto.getObservaciones()); } catch (Exception error) { throw new SQLException(error.getMessage(), error); }
    }

    /** Actualiza la nota y registra su auditoría en una única transacción. */
    public void modificarConHistorial(Calificacion objeto, Double notaAnterior, String motivo)
            throws SQLException {
        try { new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual()).modificarNota(objeto.getId_calificacion(), String.valueOf(objeto.getNota()), motivo); } catch (Exception error) { throw new SQLException(error.getMessage(), error); }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Calificacion objeto) {
        try { insertarVerificando(objeto); } catch (SQLException error) { throw new IllegalArgumentException(error.getMessage(), error); }
    }

    // consulta un registro usando su identificador
    @Override
    public Calificacion obtenerPorId(Integer id) {
        Calificacion calificacion = null;
        String consulta = "SELECT * FROM calificacion WHERE id_calificacion = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    calificacion = new Calificacion();
                    calificacion.setId_calificacion(rs.getInt("id_calificacion"));
                    calificacion.setId_inscripcion(rs.getInt("id_inscripcion"));
                    calificacion.setId_columna_notas(rs.getInt("id_columna_notas"));
                    calificacion.setNota(rs.getDouble("nota"));
                    Date fecha = rs.getDate("fecha_carga");
                    calificacion.setFecha_carga(fecha != null ? fecha.toLocalDate() : null);
                    calificacion.setObservaciones(rs.getString("observaciones"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la calificación por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return calificacion;
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Calificacion> obtenerTodos() {
        List<Calificacion> listaCalificaciones = new ArrayList<>();
        String consulta = "SELECT * FROM calificacion";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Calificacion calificacion = new Calificacion();

                calificacion.setId_calificacion(rs.getInt("id_calificacion"));
                calificacion.setId_inscripcion(rs.getInt("id_inscripcion"));
                calificacion.setId_columna_notas(rs.getInt("id_columna_notas"));
                calificacion.setNota(rs.getDouble("nota"));
                Date fecha = rs.getDate("fecha_carga");
                calificacion.setFecha_carga(fecha != null ? fecha.toLocalDate() : null);
                calificacion.setObservaciones(rs.getString("observaciones"));

                listaCalificaciones.add(calificacion);
            }

        } catch (SQLException error) {
            System.err.println(
                    "Error al obtener la lista de calificaciones: " + error.getMessage());
            error.printStackTrace();
        }

        return listaCalificaciones;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Calificacion objeto) {
        try { new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual()).modificarNota(objeto.getId_calificacion(), String.valueOf(objeto.getNota()), objeto.getObservaciones()); } catch (Exception error) { throw new IllegalArgumentException(error.getMessage(), error); }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        try { new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual()).eliminarNota(id); } catch (Exception error) { throw new IllegalArgumentException(error.getMessage(), error); }
    }
}

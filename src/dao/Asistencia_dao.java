package dao;

import clases.Usuario;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

// consultamos y guardamos las asistencias de los alumnos de cada curso
public class Asistencia_dao extends Consulta_academica_dao {
    // consultamos las asistencias que puede ver el usuario según su rol y su curso
    public List<Map<String, Object>> listar(Connection conexion, int curso, LocalDate fecha, Usuario usuario) throws SQLException {
        String alcance = switch (usuario.getId_rol()) {
            case 1, 5 -> "1=1";
            case 2 -> "EXISTS(SELECT 1 FROM docente d WHERE d.id_docente=c.id_docente AND d.id_usuario=" + usuario.getId_usuario() + ")";
            case 3 -> "a.id_usuario=" + usuario.getId_usuario();
            case 4 -> "EXISTS(SELECT 1 FROM preceptor_curso pc WHERE pc.id_curso=c.id_curso AND pc.id_usuario=" + usuario.getId_usuario() + ")";
            default -> "1=0";
        };
        return consultar(conexion, "SELECT i.id_inscripcion,CONCAT(a.apellido,', ',a.nombre) alumno,i.estado inscripcion,s.id_asistencia,s.estado,s.observacion,s.justificacion FROM inscripcion i JOIN alumno a ON a.id_alumno=i.id_alumno JOIN curso c ON c.id_curso=i.id_curso LEFT JOIN asistencia s ON s.id_inscripcion=i.id_inscripcion AND s.fecha=? WHERE c.id_curso=? AND " + alcance + " ORDER BY a.apellido,a.nombre", fecha, curso);
    }

    // comprobamos que el preceptor tenga el curso asignado
    public boolean asignado(Connection conexion, int usuario, int curso) throws SQLException {
        return !consultar(conexion, "SELECT id_usuario FROM preceptor_curso WHERE id_usuario=? AND id_curso=?", usuario, curso).isEmpty();
    }

    // buscamos el registro de la fecha antes de guardar la asistencia
    public List<Map<String, Object>> buscar(Connection conexion, int inscripcion, LocalDate fecha) throws SQLException {
        return consultar(conexion, "SELECT * FROM asistencia WHERE id_inscripcion=? AND fecha=? FOR UPDATE", inscripcion, fecha);
    }

    // guardamos la asistencia y conservamos la justificación mientras no cambie su estado
    public void guardar(Connection conexion, int inscripcion, LocalDate fecha, String estado, String observacion, int usuario) throws SQLException {
        ejecutar(conexion, "INSERT INTO asistencia(id_inscripcion,fecha,estado,observacion,id_usuario) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE justificacion=IF(estado=VALUES(estado),justificacion,''),id_justificador=IF(estado=VALUES(estado),id_justificador,NULL),estado=VALUES(estado),observacion=VALUES(observacion),id_usuario=VALUES(id_usuario)", inscripcion, fecha, estado, observacion, usuario);
    }

    // buscamos una asistencia para validar la justificación de la falta
    public Map<String, Object> obtener(Connection conexion, int id) throws SQLException {
        return uno(conexion, "SELECT * FROM asistencia WHERE id_asistencia=? FOR UPDATE", id);
    }

    // guardamos el motivo que presenta secretaría para justificar una ausencia
    public void justificar(Connection conexion, int id, String motivo, int usuario) throws SQLException {
        ejecutar(conexion, "UPDATE asistencia SET justificacion=?,id_justificador=? WHERE id_asistencia=?", motivo, usuario, id);
    }

    // consultamos los perfiles activos de preceptor para asignarles un curso
    public List<Map<String, Object>> preceptores(Connection conexion) throws SQLException {
        return consultar(conexion, "SELECT p.id_usuario,CONCAT(p.apellido,', ',p.nombre,' · ',p.legajo) nombre FROM preceptor p JOIN usuario u ON u.id_usuario=p.id_usuario WHERE p.estado='Activo' AND u.id_rol=4 ORDER BY p.apellido,p.nombre");
    }

    // vinculamos al preceptor con el curso que debe controlar
    public void asignar(Connection conexion, int usuario, int curso) throws SQLException {
        ejecutar(conexion, "INSERT INTO preceptor_curso(id_usuario,id_curso) VALUES(?,?) ON DUPLICATE KEY UPDATE id_usuario=VALUES(id_usuario)", usuario, curso);
    }
}

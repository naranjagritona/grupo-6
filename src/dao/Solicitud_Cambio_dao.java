package dao;

import java.sql.*;
import java.util.*;

// consultamos y guardamos las solicitudes de cambio que debe revisar dirección
public class Solicitud_Cambio_dao extends Consulta_academica_dao {
    // buscamos una solicitud pendiente de la misma inscripción para evitar duplicados
    public boolean pendiente(Connection conexion, int inscripcion) throws SQLException {
        return !consultar(conexion, "SELECT id_solicitud FROM solicitud_cambio WHERE id_inscripcion=? AND estado='Pendiente' FOR UPDATE", inscripcion).isEmpty();
    }

    // registramos el destino y la explicación que acompaña a la solicitud
    public int crear(Connection conexion, int inscripcion, int destino, String observacion, int usuario) throws SQLException {
        return insertarRegistro(conexion, "INSERT INTO solicitud_cambio(id_inscripcion,id_curso_destino,observacion,id_solicitante) VALUES(?,?,?,?)", inscripcion, destino, observacion, usuario);
    }

    // consultamos las solicitudes con los datos de origen, destino y sus motivos
    public List<Map<String, Object>> listar(Connection conexion) throws SQLException {
        return consultar(conexion, "SELECT s.*,CONCAT(a.apellido,', ',a.nombre) alumno,CONCAT(c.año,'° ',c.division) origen,CONCAT(d.año,'° ',d.division) destino,u.nombre_usuario solicitante FROM solicitud_cambio s JOIN inscripcion i ON i.id_inscripcion=s.id_inscripcion JOIN alumno a ON a.id_alumno=i.id_alumno JOIN curso c ON c.id_curso=i.id_curso JOIN curso d ON d.id_curso=s.id_curso_destino JOIN usuario u ON u.id_usuario=s.id_solicitante ORDER BY s.estado='Pendiente' DESC,s.id_solicitud DESC");
    }

    // bloqueamos la solicitud para impedir que dos usuarios la resuelvan al mismo tiempo
    public Map<String, Object> bloquear(Connection conexion, int id) throws SQLException {
        return uno(conexion, "SELECT * FROM solicitud_cambio WHERE id_solicitud=? FOR UPDATE", id);
    }

    // guardamos la decisión de dirección junto a su motivo y la inscripción generada
    public void resolver(Connection conexion, int id, boolean aprobar, int usuario, String motivo, Integer nueva) throws SQLException {
        ejecutar(conexion, "UPDATE solicitud_cambio SET estado=?,id_directivo=?,motivo_resolucion=?,fecha_resolucion=NOW(),id_inscripcion_nueva=? WHERE id_solicitud=?", aprobar ? "Aprobada" : "Rechazada", usuario, motivo, nueva, id);
    }
}

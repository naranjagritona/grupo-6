package dao;

import conexion.Conexion;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// consultamos los contadores, las personas y los logs con una sola conexión
public class ConsultaAdmin_dao {
    public record Datos(
            int[] totales, List<Object[]> alumnos, List<Object[]> docentes, List<Object[]> logs) {}

    public Datos cargar() {
        if (!servicios.Permisos_servicio.gestion(servicios.Permisos_servicio.actual())) throw new IllegalArgumentException("No tenés acceso a administración.");
        try (Connection db = Conexion.getConnection()) {
            int[] totales = new int[5];
            String sql =
                    """
                    SELECT (SELECT COUNT(*) FROM alumno), (SELECT COUNT(*) FROM docente),
                           (SELECT COUNT(*) FROM curso WHERE estado='Activo'),
                           (SELECT COUNT(*) FROM inscripcion), (SELECT COUNT(*) FROM usuario)
                    """;
            try (PreparedStatement q = db.prepareStatement(sql);
                    ResultSet rs = q.executeQuery()) {
                if (rs.next()) {
                    for (int i = 0; i < totales.length; i++) {
                        totales[i] = rs.getInt(i + 1);
                    }
                }
            }
            List<Object[]> alumnos = new ArrayList<>();
            List<Object[]> docentes = new ArrayList<>();
            sql =
                    """
                    SELECT 'Alumno' tipo,a.id_alumno id,a.nombre,a.apellido,a.dni,
                           a.matricula numero,a.estado,u.nombre_usuario
                    FROM alumno a LEFT JOIN usuario u ON u.id_usuario=a.id_usuario
                    UNION ALL
                    SELECT 'Docente',d.id_docente,d.nombre,d.apellido,d.dni,
                           d.legajo,d.estado,u.nombre_usuario
                    FROM docente d LEFT JOIN usuario u ON u.id_usuario=d.id_usuario
                    ORDER BY apellido,nombre,id
                    """;
            try (PreparedStatement q = db.prepareStatement(sql);
                    ResultSet rs = q.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = {
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("numero"),
                        rs.getString("estado"),
                        rs.getString("nombre_usuario")
                    };
                    if ("Alumno".equals(rs.getString("tipo"))) {
                        alumnos.add(fila);
                    } else {
                        docentes.add(fila);
                    }
                }
            }
            List<Object[]> logs = new ArrayList<>();
            // la base trae los nombres relacionados sin buscar usuario por usuario en Java.
            sql =
                    """
                    SELECT l.id_log_usuario,COALESCE(u.nombre_usuario,'Desconocido') usuario,
                           COALESCE(r.nombre,CONCAT('Rol ',u.id_rol),'N/A') rol,
                           l.accion,l.campo_modificado,COALESCE(l.valor_nuevo,l.valor_anterior) detalle,
                           l.fecha_hora
                    FROM log_usuario l
                    LEFT JOIN usuario u ON u.id_usuario=l.id_usuario
                    LEFT JOIN rol r ON r.id_rol=u.id_rol
                    ORDER BY l.fecha_hora DESC,l.id_log_usuario DESC
                    """;
            try (PreparedStatement q = db.prepareStatement(sql + (servicios.Permisos_servicio.directivo(servicios.Permisos_servicio.actual()) ? "" : " LIMIT 0"));
                    ResultSet rs = q.executeQuery()) {
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha_hora");
                    logs.add(
                            new Object[] {
                                rs.getInt("id_log_usuario"),
                                rs.getString("usuario"),
                                rs.getString("rol"),
                                rs.getString("accion"),
                                rs.getString("campo_modificado"),
                                rs.getString("detalle"),
                                fecha == null ? "" : fecha.toLocalDateTime().format(formato)
                            });
                }
            }
            return new Datos(totales, alumnos, docentes, logs);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar los datos de Administración.", e);
        }
    }
}

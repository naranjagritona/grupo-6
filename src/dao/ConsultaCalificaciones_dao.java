package dao;

import clases.Calificacion;
import clases.Columna_Notas;

import conexion.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// juntamos los alumnos, las evaluaciones y las notas del curso en una consulta
public class ConsultaCalificaciones_dao {
    public record Fila(
            int idInscripcion,
            String alumno,
            String dni,
            Columna_Notas columna,
            Calificacion nota) {}

    // consulta los registros que puede ver la cuenta actual
    public List<Fila> listar(int idCurso) {
        return listar(idCurso, null);
    }

    public List<Fila> listar(int idCurso, clases.Usuario usuario) {
        String sql =
                """
                SELECT a.nombre,a.apellido,a.dni,i.id_inscripcion,m.nombre_materia,
                       c.id_columna_notas,c.id_curso,c.titulo,c.tipo,c.nota_maxima,
                       c.nota_aprobacion,c.fecha_evaluacion,c.fecha_cierre,c.estado estado_columna,
                       n.id_calificacion,n.nota,n.fecha_carga,n.observaciones
                FROM columna_notas c
                JOIN materia m ON m.id_materia=c.id_materia
                JOIN inscripcion i ON i.id_curso=c.id_curso
                JOIN alumno a ON a.id_alumno=i.id_alumno
                LEFT JOIN calificacion n ON n.id_inscripcion=i.id_inscripcion
                    AND n.id_columna_notas=c.id_columna_notas
                WHERE c.id_curso=? AND 1=1
                ORDER BY c.id_columna_notas,i.id_inscripcion
                """;
        if (usuario != null) {
            String permiso;
            if ((usuario.getId_rol() == 1 || usuario.getId_rol() == 5)) {
                permiso = "";
            } else if (usuario.getId_rol() == 2) {
                permiso =
                        " AND EXISTS(SELECT 1 FROM curso cu JOIN docente d ON"
                                + " d.id_docente=cu.id_docente WHERE cu.id_curso=c.id_curso AND"
                                + " d.id_usuario=?) ";
            } else if (usuario.getId_rol() == 4) {
                permiso = " AND EXISTS(SELECT 1 FROM preceptor_curso pc WHERE pc.id_curso=c.id_curso AND pc.id_usuario=?) ";
            } else if (usuario.getId_rol() == 3) {
                permiso = " AND a.id_usuario=? ";
            } else {
                throw new IllegalArgumentException("Esta cuenta no tiene acceso a Calificaciones.");
            }
            sql = sql.replace("ORDER BY", permiso + " ORDER BY");
        }
        try (Connection db = Conexion.getConnection();
                PreparedStatement q = db.prepareStatement(sql)) {
            q.setInt(1, idCurso);
            if (usuario != null && usuario.getId_rol() != 1 && usuario.getId_rol() != 5) {
                q.setInt(2, usuario.getId_usuario());
            }
            List<Fila> filas = new ArrayList<>();
            try (ResultSet rs = q.executeQuery()) {
                while (rs.next()) {
                    Columna_Notas c =
                            new Columna_Notas(
                                    rs.getInt("id_columna_notas"),
                                    rs.getInt("id_curso"),
                                    rs.getString("titulo"),
                                    rs.getString("tipo"),
                                    rs.getDouble("nota_maxima"),
                                    rs.getDouble("nota_aprobacion"),
                                    fecha(rs, "fecha_evaluacion"),
                                    fecha(rs, "fecha_cierre"),
                                    rs.getString("estado_columna"));
                    c.setNombre_materia(rs.getString("nombre_materia"));
                    Calificacion n = null;
                    if (rs.getObject("id_calificacion") != null) {
                        n =
                                new Calificacion(
                                        rs.getInt("id_calificacion"),
                                        rs.getInt("id_inscripcion"),
                                        rs.getInt("id_columna_notas"),
                                        rs.getDouble("nota"),
                                        fecha(rs, "fecha_carga"),
                                        rs.getString("observaciones"));
                    }
                    filas.add(
                            new Fila(
                                    rs.getInt("id_inscripcion"),
                                    rs.getString("nombre") + " " + rs.getString("apellido"),
                                    rs.getString("dni"),
                                    c,
                                    n));
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar las calificaciones del curso.", e);
        }
    }

    private java.time.LocalDate fecha(ResultSet rs, String campo) throws SQLException {
        Date fecha = rs.getDate(campo);
        return fecha == null ? null : fecha.toLocalDate();
    }
}

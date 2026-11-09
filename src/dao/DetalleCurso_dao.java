package dao;

import clases.Curso;
import clases.Usuario;
import java.time.LocalDate;
import java.util.*;

// Consultamos el curso, la ocupación y sus inscripciones desde la misma transacción.
public class DetalleCurso_dao extends Consulta_academica_dao {
    // guardamos el nombre, el estado y la fecha de cada inscripción del listado
    public record Alumno(String nombre, String estado, LocalDate fecha) {}
    // reunimos los datos del curso y los totales de inscripciones para mostrarlos juntos
    public record Datos(Curso curso, int ocupados, int confirmados, int pendientes,
                        int totalInscripciones, List<Alumno> alumnos) {
        // calculamos los lugares libres sin mostrar valores negativos si el cupo ya está completo
        public int disponibles() { return Math.max(0, curso.getCupo_maximo() - ocupados); }
    }

    // consultamos el curso con la misma conexión utilizada para contar sus inscripciones
    protected Curso obtenerCurso(java.sql.Connection db, int idCurso) throws java.sql.SQLException {
        return new Curso_dao().obtenerPorId(db, idCurso);
    }

    // verificamos el acceso del usuario y consultamos la información general y sus alumnos
    public Datos consultar(int idCurso, Usuario usuario) throws Exception {
        return transaccion(db -> {
            if (usuario == null || servicios.Auditoria_servicio.usuarioActual() == null) throw new IllegalArgumentException("Iniciá sesión para consultar el curso.");
            servicios.Permisos_servicio.verificar(db, this, usuario.getId_usuario(), clases.Rol.SECRETARIA, clases.Rol.DOCENTE, clases.Rol.ALUMNO, clases.Rol.DIRECTIVO);
            int rol = obtenerRol(db, usuario.getId_usuario());
            String alcance = switch (rol) {
                case clases.Rol.SECRETARIA, clases.Rol.DIRECTIVO -> null;
                case clases.Rol.DOCENTE -> "SELECT 1 FROM curso_materia cm JOIN docente d ON d.id_docente=cm.id_docente WHERE cm.id_curso=? AND d.id_usuario=?";
                case clases.Rol.ALUMNO -> "SELECT 1 FROM inscripcion i JOIN alumno a ON a.id_alumno=i.id_alumno WHERE i.id_curso=? AND a.id_usuario=?";
                default -> throw new IllegalArgumentException("No tenés acceso a este curso.");
            };
            if (alcance != null && consultar(db, alcance, idCurso, usuario.getId_usuario()).isEmpty())
                throw new IllegalArgumentException("El curso no corresponde a tu cuenta.");
            Curso curso = obtenerCurso(db, idCurso);
            if (curso == null) throw new IllegalArgumentException("El curso ya no existe.");
            var resumen = uno(db, "SELECT COUNT(*) total,COALESCE(SUM(estado IN ('Pendiente','Confirmada','Recuperacion')),0) ocupados,COALESCE(SUM(estado IN ('Confirmada','Recuperacion')),0) confirmados,COALESCE(SUM(estado='Pendiente'),0) pendientes FROM inscripcion WHERE id_curso=?", idCurso);
            String sql = "SELECT CONCAT(a.apellido,', ',a.nombre) nombre,i.estado,i.fecha_inscripcion FROM inscripcion i JOIN alumno a ON a.id_alumno=i.id_alumno WHERE i.id_curso=?";
            // El alumno consulta su inscripción; los roles de gestión ven el listado del curso.
            List<Map<String,Object>> filas = rol == clases.Rol.ALUMNO
                    ? consultar(db, sql + " AND a.id_usuario=? ORDER BY a.apellido,a.nombre,i.id_inscripcion", idCurso, usuario.getId_usuario())
                    : consultar(db, sql + " ORDER BY a.apellido,a.nombre,i.id_inscripcion", idCurso);
            List<Alumno> alumnos = new ArrayList<>();
            for (var fila : filas) {
                Object fecha = fila.get("fecha_inscripcion");
                LocalDate dia = fecha == null ? null : fecha instanceof java.sql.Date d ? d.toLocalDate() : LocalDate.parse(fecha.toString().substring(0,10));
                alumnos.add(new Alumno((String) fila.get("nombre"), (String) fila.get("estado"), dia));
            }
            return new Datos(curso, numero(resumen,"ocupados"), numero(resumen,"confirmados"),
                    numero(resumen,"pendientes"), numero(resumen,"total"), List.copyOf(alumnos));
        });
    }
}

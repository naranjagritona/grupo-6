package servicios;

import clases.Rol;
import clases.Usuario;

import conexion.Conexion;

import dao.*;

import dto.CursoVista;

import java.time.LocalDate;
import java.util.*;

public class DetalleCurso_servicio {
    public record Alumno(String nombre, String estado, LocalDate fecha) {}

    public record Datos(
            CursoVista curso,
            int ocupados,
            int confirmados,
            int pendientes,
            int totalInscripciones,
            List<Alumno> alumnos) {
        public int disponibles() {
            return Math.max(0, curso.getCupo_maximo() - ocupados);
        }
    }

    protected CursoVista obtenerCurso(java.sql.Connection db, int id) throws java.sql.SQLException {
        return new Curso_servicio().obtenerPorId(db, id);
    }

    public Datos consultar(int idCurso, Usuario usuario) throws Exception {
        return Conexion.transaccion(
                db -> {
                    Permisos_servicio.verificar(
                            db,
                            usuario == null ? null : usuario.getId_usuario(),
                            Rol.SECRETARIA,
                            Rol.DOCENTE,
                            Rol.ALUMNO,
                            Rol.DIRECTIVO);
                    int rol = new dao.Usuario_dao().rolActivo(db, usuario.getId_usuario());
                    if (rol == Rol.DOCENTE
                                    && !new Curso_Materia_dao()
                                            .tieneDocente(db, idCurso, usuario.getId_usuario())
                            || rol == Rol.ALUMNO
                                    && !new Inscripcion_dao()
                                            .perteneceUsuario(db, idCurso, usuario.getId_usuario()))
                        throw new IllegalArgumentException("El curso no corresponde a tu cuenta.");
                    CursoVista curso = obtenerCurso(db, idCurso);
                    if (curso == null) throw new IllegalArgumentException("El curso ya no existe.");
                    var inscripciones = new Inscripcion_dao().delCurso(db, idCurso);
                    int ocupados = 0, confirmados = 0, pendientes = 0;
                    List<Alumno> alumnos = new ArrayList<>();
                    Map<Integer, clases.Alumno> personas = new HashMap<>();
                    for (var alumno : new Alumno_dao().delCurso(db, idCurso))
                        personas.put(alumno.getId_alumno(), alumno);
                    for (var inscripcion : inscripciones) {
                        String estado = inscripcion.getEstado();
                        if (List.of("Pendiente", "Confirmada", "Recuperacion").contains(estado))
                            ocupados++;
                        if (List.of("Confirmada", "Recuperacion").contains(estado)) confirmados++;
                        if ("Pendiente".equals(estado)) pendientes++;
                        var alumno = personas.get(inscripcion.getId_alumno());
                        if (rol == Rol.ALUMNO
                                && !Objects.equals(alumno.getId_usuario(), usuario.getId_usuario()))
                            continue;
                        alumnos.add(
                                new Alumno(
                                        alumno.getApellido() + ", " + alumno.getNombre(),
                                        estado,
                                        inscripcion.getFecha_inscripcion()));
                    }
                    alumnos.sort(Comparator.comparing(Alumno::nombre));
                    return new Datos(
                            curso,
                            ocupados,
                            confirmados,
                            pendientes,
                            inscripciones.size(),
                            List.copyOf(alumnos));
                });
    }
}

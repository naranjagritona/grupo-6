package servicios;

import clases.*;

import conexion.Conexion;

import dao.*;

import java.sql.*;
import java.util.*;

public class ConsultaCalificaciones_servicio {
    public record Fila(
            int idInscripcion,
            String alumno,
            String dni,
            dto.ColumnaVista columna,
            Calificacion nota) {}

    public List<Fila> listar(int curso) {
        return listar(curso, null);
    }

    public List<Fila> listar(int curso, Usuario usuario) {
        usuario = Permisos_servicio.actual();
        if (!Permisos_servicio.verCalificaciones(usuario))
            throw new IllegalArgumentException("No tenés acceso a Calificaciones.");
        try (Connection db = Conexion.getConnection()) {
            List<Fila> filas = new ArrayList<>();
            var inscripciones = new Inscripcion_dao().delCurso(db, curso);
            var columnas = new Columna_Notas_dao().delCurso(db, curso);
            Map<Integer, List<Calificacion>> notas = new HashMap<>();
            Map<Integer, Alumno> alumnos = new HashMap<>();
            for (Calificacion nota : new Calificacion_dao().delCurso(db, curso))
                notas.computeIfAbsent(nota.getId_inscripcion(), k -> new ArrayList<>()).add(nota);
            for (Alumno alumno : new Alumno_dao().delCurso(db, curso))
                alumnos.put(alumno.getId_alumno(), alumno);
            Map<Integer, Materia> materias = new HashMap<>();
            for (Materia materia : new Materia_dao().listarTabla(db))
                materias.put(materia.getId_materia(), materia);
            Set<Integer> asignadas = new HashSet<>();
            if (usuario.getId_rol() == Rol.DOCENTE) {
                Docente docente = new Docente_dao().porUsuario(db, usuario.getId_usuario());
                if (docente != null) {
                    for (Curso_Materia relacion : new Curso_Materia_dao().delCurso(db, curso)) {
                        if (Objects.equals(relacion.getId_docente(), docente.getId_docente()))
                            asignadas.add(relacion.getId_materia());
                    }
                }
            }
            for (Columna_Notas columna : columnas) {
                if (usuario.getId_rol() == Rol.DOCENTE
                        && !asignadas.contains(columna.getId_materia())) continue;
                var vista =
                        new dto.ColumnaVista(
                                columna, materias.get(columna.getId_materia()).getNombre_materia());
                for (Inscripcion i : inscripciones) {
                    Alumno alumno = alumnos.get(i.getId_alumno());
                    if (usuario.getId_rol() == Rol.ALUMNO
                            && !Objects.equals(alumno.getId_usuario(), usuario.getId_usuario()))
                        continue;
                    Calificacion nota =
                            notas.getOrDefault(i.getId_inscripcion(), List.of()).stream()
                                    .filter(
                                            n ->
                                                    Objects.equals(
                                                            n.getId_columna_notas(),
                                                            columna.getId_columna_notas()))
                                    .findFirst()
                                    .orElse(null);
                    filas.add(
                            new Fila(
                                    i.getId_inscripcion(),
                                    alumno.getNombre() + " " + alumno.getApellido(),
                                    alumno.getDni(),
                                    vista,
                                    nota));
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar las calificaciones del curso.", e);
        }
    }
}

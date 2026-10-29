package servicios;

import clases.Usuario;
import dao.Asistencia_dao;
import dao.Inscripcion_dao;
import java.time.LocalDate;
import java.util.*;
import static dao.Consulta_academica_dao.numero;

// validamos las fechas y los permisos antes de registrar o justificar las asistencias
public class Asistencia_servicio {
    private final Usuario usuario;
    private final Asistencia_dao dao;
    private final Inscripcion_dao inscripciones;

    public Asistencia_servicio(Usuario usuario) { this(usuario, new Asistencia_dao(), new Inscripcion_dao()); }

    // compartimos los daos que consultan el curso y guardan la asistencia de la inscripción
    public Asistencia_servicio(Usuario usuario, Asistencia_dao dao, Inscripcion_dao inscripciones) {
        this.usuario = usuario; this.dao = dao; this.inscripciones = inscripciones;
    }

    // consultamos las asistencias del curso sin permitir el acceso a registros de otros alumnos o cursos
    public List<Map<String, Object>> listar(int curso, LocalDate fecha) throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario.getId_usuario(), 1, 2, 3, 4, 5);
            return dao.listar(conexion, curso, fecha, usuario);
        });
    }

    // verificamos el curso y la fecha y pedimos una observación cuando se corrige la asistencia
    public void guardar(int id, LocalDate fecha, String estado, String observacion) throws Exception {
        if (fecha.isAfter(LocalDate.now())) throw new IllegalArgumentException("No se registra asistencia futura.");
        if (!List.of("Presente", "Ausente", "Tarde").contains(estado) || observacion.length() > 255) throw new IllegalArgumentException("Revisá el estado y la observación.");
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario.getId_usuario(), 4);
            var inscripcion = inscripciones.bloquear(conexion, id);
            if (!List.of("Confirmada", "Recuperacion").contains(inscripcion.get("estado"))) throw new IllegalArgumentException("La inscripción está cerrada o pendiente.");
            int curso = numero(inscripcion, "id_curso");
            if (!dao.asignado(conexion, usuario.getId_usuario(), curso)) throw new IllegalArgumentException("El curso no está asignado al preceptor.");
            var datos = inscripciones.curso(conexion, curso);
            if (fecha.isBefore(((java.sql.Date) inscripcion.get("fecha_inscripcion")).toLocalDate())
                    || fecha.isBefore(((java.sql.Date) datos.get("fecha_inicio")).toLocalDate())
                    || fecha.isAfter(((java.sql.Date) datos.get("fecha_fin")).toLocalDate())) throw new IllegalArgumentException("La fecha no corresponde a la inscripción o a la cursada.");
            var anterior = dao.buscar(conexion, id, fecha);
            if (!anterior.isEmpty() && (!estado.equals(anterior.getFirst().get("estado")) || !observacion.equals(anterior.getFirst().get("observacion")))) Inscripcion_servicio.motivo(observacion);
            dao.guardar(conexion, id, fecha, estado, observacion, usuario.getId_usuario());
            return null;
        });
    }

    // justificamos una ausencia y dejamos la cuenta de secretaría que registró el motivo
    public void justificar(int id, String motivo) throws Exception {
        String razon = Inscripcion_servicio.motivo(motivo);
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario.getId_usuario(), 1);
            var registro = dao.obtener(conexion, id);
            Inscripcion_servicio.validarAbierta(inscripciones.bloquear(conexion, numero(registro, "id_inscripcion")));
            if (!"Ausente".equals(registro.get("estado"))) throw new IllegalArgumentException("Solo se justifican ausencias.");
            dao.justificar(conexion, id, razon, usuario.getId_usuario());
            return null;
        });
    }

    // buscamos los preceptores que dirección puede asignar a un curso
    public List<Map<String, Object>> preceptores() throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario.getId_usuario(), 5);
            return dao.preceptores(conexion);
        });
    }

    // guardamos la asignación después de verificar que la cuenta sea de un preceptor activo
    public void asignar(int preceptor, int curso) throws Exception {
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario.getId_usuario(), 5);
            inscripciones.curso(conexion, curso);
            if (dao.preceptores(conexion).stream().noneMatch(p -> numero(p, "id_usuario") == preceptor)) throw new IllegalArgumentException("El preceptor no está activo.");
            dao.asignar(conexion, preceptor, curso);
            return null;
        });
    }
}

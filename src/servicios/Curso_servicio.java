package servicios;

import clases.Curso;
import clases.Usuario;
import dao.Curso_dao;
import java.time.LocalDate;
import java.util.List;

// coordinamos las consultas y la gestión de cursos con los permisos de cada rol
public class Curso_servicio {
    // declaramos el dao que consulta y guarda los datos de los cursos
    private final Curso_dao dao = new Curso_dao();

    // La fecha de fin es inclusiva: desde ese día el curso deja de estar activo.
    public static String estadoSegunFecha(String estado, LocalDate fin, LocalDate hoy) {
        return "Activo".equals(estado) && fin != null && !fin.isAfter(hoy) ? "Finalizado" : estado;
    }

    // consultamos los cursos que corresponden al usuario
    public List<Curso> listar(Usuario usuario) { return dao.listar(usuario); }

    // buscamos las materias que ya están relacionadas con el curso
    public List<Integer> materiasDelCurso(int curso) { return dao.materiasDelCurso(curso); }

    // enviamos el curso y las materias al dao para validar y guardar sus datos
    public void guardar(Curso curso, List<Integer> materias) { dao.guardar(curso, materias); }

    // cancelamos el curso conservando las inscripciones y el historial
    public void cancelar(int curso) { dao.cancelar(curso); }

    // verificamos el cupo antes de guardar el nuevo valor
    public void modificarCupo(int curso, int cupo) throws Exception { dao.modificarCupo(curso, cupo); }

    // delegamos la asignación para informar que las materias se administran por año
    public void asignarMaterias(int curso, List<Integer> materias) throws Exception { dao.asignarMaterias(curso, materias); }

    // comprobamos el nombre, el tipo y las fechas antes de registrar el período
    public void crearPeriodo(String nombre, String tipo, String desde, String hasta) throws Exception {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 100) throw new IllegalArgumentException("Ingresá un nombre de período de hasta 100 caracteres.");
        if (!List.of("Anual", "Cuatrimestral", "Otro").contains(tipo)) throw new IllegalArgumentException("Seleccioná un tipo de período válido.");
        LocalDate inicio = LocalDate.parse(desde.trim()), fin = LocalDate.parse(hasta.trim());
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("El fin no puede ser anterior al inicio.");
        dao.crearPeriodo(nombre.trim(), tipo, inicio, fin);
    }
    // coordinamos la asignación y su aviso en una transacción y enviamos sólo después de confirmarla
    public void asignarDocente(dao.PlanCurso_dao plan, int curso, int materia, Integer docente, Integer anterior, Integer actor) throws Exception {
        boolean cambio = plan.transaccion(db -> {
            Permisos_servicio.verificar(db, plan, actor, clases.Rol.DIRECTIVO);
            var relacion = plan.bloquearAsignacion(db, curso, materia);
            if (!java.util.Objects.equals(relacion.get("id_docente"), anterior)) throw new IllegalArgumentException("Otro usuario cambió esta asignación. Actualizá el detalle y volvé a intentar.");
            if (docente != null && !plan.docenteActivo(db, docente)) throw new IllegalArgumentException("Seleccioná un docente activo.");
            if (java.util.Objects.equals(docente, anterior)) return false;
            plan.actualizarAsignacion(db, curso, materia, docente);
            if (docente != null) {
                var avisos = new dao.Notificacion_Correo_dao();
                new Notificaciones_servicio(avisos).asignacion(db, avisos.usuarioDocente(db, docente), curso, "docente-" + java.util.UUID.randomUUID(), actor);
            }
            return true;
        });
        if (cambio) Notificaciones_servicio.enviarPendientes(actor);
    }

}

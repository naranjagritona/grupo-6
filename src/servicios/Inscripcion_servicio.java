package servicios;

import clases.Alumno;
import clases.Curso;
import clases.Inscripcion;

import dao.Alumno_dao;
import dao.Curso_dao;
import dao.Inscripcion_dao;
import dao.Resultado_Materia_dao;

import java.time.LocalDate;
import java.util.List;

// revisamos las condiciones de inscripción antes de modificar los datos
public class Inscripcion_servicio {

    // declaramos los daos que vamos a usar para consultar la base de datos
    private Inscripcion_dao inscripcionDao;
    private Alumno_dao alumnoDao;
    private Curso_dao cursoDao;
    private Resultado_Materia_dao resultadoMateriaDao;

    public Inscripcion_servicio() {
        this.inscripcionDao = new Inscripcion_dao();
        this.alumnoDao = new Alumno_dao();
        this.cursoDao = new Curso_dao();
        this.resultadoMateriaDao = new Resultado_Materia_dao();
    }

    // consulta al alumno por su id usando el alumno dao
    // para verificar si su estado actual es activo
    public boolean alumnoActivo(Integer idAlumno) {
        Alumno alumno = alumnoDao.obtenerPorId(idAlumno);
        return alumno != null && "Activo".equalsIgnoreCase(alumno.getEstado());
    }

    // recorre todas las inscripciones para ver si el alumno ya esta anotado
    // en el mismo curso con estado pendiente o confirmada
    public boolean yaInscripto(Integer idAlumno, Integer idCurso) {
        List<Inscripcion> inscripciones = inscripcionDao.obtenerTodos();
        for (Inscripcion ins : inscripciones) {
            if (ins.getId_alumno().equals(idAlumno) && ins.getId_curso().equals(idCurso)) {
                if ("Confirmada".equalsIgnoreCase(ins.getEstado())
                        || "Pendiente".equalsIgnoreCase(ins.getEstado())) {
                    return true;
                }
            }
        }
        return false;
    }

    // cuenta cuantas inscripciones confirmadas tiene el curso
    // y las compara con el cupo maximo permitido para saber si hay lugar
    public boolean hayCupo(Integer idCurso) {
        Curso curso = cursoDao.obtenerPorId(idCurso);
        if (curso == null) return false;

        int confirmadas = 0;
        List<Inscripcion> inscripciones = inscripcionDao.obtenerTodos();
        for (Inscripcion ins : inscripciones) {
            if (ins.getId_curso().equals(idCurso)
                    && "Confirmada".equalsIgnoreCase(ins.getEstado())) {
                confirmadas++;
            }
        }
        return confirmadas < curso.getCupo_maximo();
    }

    // verificamos que la fecha actual este en el rango del periodo lectivo
    // del curso
    public boolean plazoPeriodo(Integer idCurso) {
        Curso curso = cursoDao.obtenerPorId(idCurso);
        if (curso == null) return false;

        LocalDate hoy = LocalDate.now();
        if (curso.getFecha_inicio() != null && curso.getFecha_fin() != null) {
            return !hoy.isBefore(curso.getFecha_inicio()) && !hoy.isAfter(curso.getFecha_fin());
        }
        return true;
    }

    // junta todas las validaciones anteriores antes de dejar inscribir a alguien
    // si algo falla devuelve un texto con el motivo
    public String puedeInscribirse(Integer idAlumno, Integer idCurso) {
        if (!alumnoActivo(idAlumno)) {
            return "El alumno no se encuentra con estado Activo.";
        }
        if (yaInscripto(idAlumno, idCurso)) {
            return "El alumno ya posee una inscripción activa o pendiente para este curso.";
        }
        if (!hayCupo(idCurso)) {
            return "El curso ha alcanzado su cupo máximo de inscripciones.";
        }
        if (!plazoPeriodo(idCurso)) {
            return "La inscripción está fuera de término respecto a las fechas del período"
                    + " lectivo.";
        }
        return "OK";
    }

    // llama al dao para buscar inscripciones por texto o filtro
    public List<Inscripcion> buscarCampo(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return inscripcionDao.obtenerTodos();
        }
        return inscripcionDao.buscarCampo(filtro.trim());
    }

    // filtra las inscripciones segun el estado seleccionado en la interfaz
    public List<Inscripcion> filtrarEstadoInscrip(String estadoFiltro) {
        if (estadoFiltro == null
                || estadoFiltro.trim().isEmpty()
                || "Todos".equalsIgnoreCase(estadoFiltro)) {
            return inscripcionDao.obtenerTodos();
        }
        return inscripcionDao.obtenerPorEstado(estadoFiltro);
    }

    // envia la nueva inscripcion al dao para guardarla en la bd
    // y despues deja registro en el log de usuarios
    public void registrar(Inscripcion inscripcion, Integer idUsuario) {
        inscripcionDao.insertar(inscripcion);
    }

    // actualiza los datos de una inscripcion existente
    // guardando el valor viejo y el nuevo en los logs
    public void modificar(Inscripcion inscripcion, Integer idUsuario) {
        Inscripcion anterior = inscripcionDao.obtenerPorId(inscripcion.getId_inscripcion());

        inscripcionDao.modificar(inscripcion);
    }

    // cambia el estado de la inscripcion a cancelada y guarda el motivo de baja
    public void darDeBaja(Integer idInscripcion, String motivoBaja, Integer idUsuario) {
        Inscripcion ins = inscripcionDao.obtenerPorId(idInscripcion);
        if (ins != null) {

            ins.setEstado("Cancelada");
            ins.setMotivo_baja(motivoBaja);
            inscripcionDao.modificar(ins);
        }
    }

    // valida las condiciones y ejecuta el cambio de curso para el alumno
    public void cambiarCurso(
            Integer idInscripcionActual, Integer idNuevoCurso, String motivoBaja, Integer idUsuario)
            throws Exception {
        if (motivoBaja == null || motivoBaja.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo del traslado es obligatorio.");
        }

        Inscripcion actual = inscripcionDao.obtenerPorId(idInscripcionActual);
        if (actual == null) {
            throw new IllegalStateException("La inscripción seleccionada no existe.");
        }

        if (!alumnoActivo(actual.getId_alumno())) {
            throw new IllegalStateException("El alumno no se encuentra con estado Activo.");
        }

        if (yaInscripto(actual.getId_alumno(), idNuevoCurso)) {
            throw new IllegalStateException(
                    "El alumno ya posee una inscripción activa en este curso de destino.");
        }

        if (!hayCupo(idNuevoCurso)) {
            throw new IllegalStateException(
                    "El curso de destino ha alcanzado su cupo máximo de inscripciones.");
        }

        try {
            inscripcionDao.cambiarCursoTransaccional(idInscripcionActual, idNuevoCurso, motivoBaja);

        } catch (Exception e) {
            throw new Exception("Error al procesar el cambio de curso: " + e.getMessage());
        }
    }
}

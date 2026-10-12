package servicios;

import clases.Alumno;
import clases.Curso;
import clases.Inscripcion;
import clases.Resultado_Materia;
import dao.Alumno_dao;
import dao.Curso_dao;
import dao.Inscripcion_dao;
import dao.Resultado_Materia_dao;

import java.util.List;

public class Inscripcion_servicio {

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

    /** 1. Revisa que el estado del alumno sea exactamente 'Activo' */
    public boolean alumnoActivo(Integer idAlumno) {
        Alumno alumno = alumnoDao.obtenerPorId(idAlumno);
        return alumno != null && "Activo".equalsIgnoreCase(alumno.getEstado());
    }

    /** 2. Comprueba si el alumno ya tiene un registro activo o pendiente para ese mismo curso */
    public boolean yaInscripto(Integer idAlumno, Integer idCurso) {
        List<Inscripcion> inscripciones = inscripcionDao.obtenerTodos();
        for (Inscripcion ins : inscripciones) {
            if (ins.getId_alumno().equals(idAlumno) && ins.getId_curso().equals(idCurso)) {
                if ("Confirmada".equalsIgnoreCase(ins.getEstado()) || "Pendiente".equalsIgnoreCase(ins.getEstado())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 3. Cuenta las inscripciones confirmadas y asegura que no superen el cupo máximo */
    public boolean hayCupo(Integer idCurso) {
        Curso curso = cursoDao.obtenerPorId(idCurso);
        if (curso == null) return false;

        int confirmadas = 0;
        List<Inscripcion> inscripciones = inscripcionDao.obtenerTodos();
        for (Inscripcion ins : inscripciones) {
            if (ins.getId_curso().equals(idCurso) && "Confirmada".equalsIgnoreCase(ins.getEstado())) {
                confirmadas++;
            }
        }
        return confirmadas < curso.getCupo_maximo();
    }

    /** 4. Consulta el historial académico para confirmar requisitos previos */
    public boolean tieneMateriaAprobada(Integer idAlumno, Integer idMateriaRequisito) {
        List<Resultado_Materia> resultados = resultadoMateriaDao.obtenerTodos();
        List<Inscripcion> inscripciones = inscripcionDao.obtenerTodos();

        for (Resultado_Materia res : resultados) {
            for (Inscripcion ins : inscripciones) {
                if (ins.getId_inscripcion().equals(res.getId_inscripcion()) &&
                    ins.getId_alumno().equals(idAlumno)) {
                    Curso curso = cursoDao.obtenerPorId(ins.getId_curso());
                    if (curso != null && curso.getId_materia().equals(idMateriaRequisito)) {
                        if ("Aprobado".equalsIgnoreCase(res.getResultado()) || "Promocionado".equalsIgnoreCase(res.getResultado())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** 5. Validación general unificada antes de permitir la inscripción */
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
        return "OK";
    }

    /** 6. Búsqueda delegada al DAO mediante SQL */
    public List<Inscripcion> buscarCampo(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return inscripcionDao.obtenerTodos();
        }
        return inscripcionDao.buscar(filtro.trim());
    }

    /** 7. Filtrado por estado delegado al DAO mediante SQL */
    public List<Inscripcion> filtrarEstadoInscrip(String estadoFiltro) {
        if (estadoFiltro == null || estadoFiltro.trim().isEmpty() || "Todos".equalsIgnoreCase(estadoFiltro)) {
            return inscripcionDao.obtenerTodos();
        }
        return inscripcionDao.obtenerPorEstado(estadoFiltro);
    }

    public void registrar(Inscripcion inscripcion) {
        inscripcionDao.insertar(inscripcion);
    }

    public void modificar(Inscripcion inscripcion) {
        inscripcionDao.modificar(inscripcion);
    }

    public void darDeBaja(Integer idInscripcion, String motivoBaja) {
        Inscripcion ins = inscripcionDao.obtenerPorId(idInscripcion);
        if (ins != null) {
            ins.setEstado("Finalizada");
            ins.setMotivo_baja(motivoBaja);
            inscripcionDao.modificar(ins);
        }
    }
}
package servicios;

import clases.Alumno;
import clases.Curso;
import clases.Inscripcion;
import clases.Log_Usuario;
import clases.Resultado_Materia;
import dao.Alumno_dao;
import dao.Curso_dao;
import dao.Inscripcion_dao;
import dao.Log_Usuario_dao;
import dao.Resultado_Materia_dao;

import java.time.LocalDateTime;
import java.util.List;

public class Inscripcion_servicio {

    private Inscripcion_dao inscripcionDao;
    private Alumno_dao alumnoDao;
    private Curso_dao cursoDao;
    private Resultado_Materia_dao resultadoMateriaDao;
    private Log_Usuario_dao logUsuarioDao;

    public Inscripcion_servicio() {
        this.inscripcionDao = new Inscripcion_dao();
        this.alumnoDao = new Alumno_dao();
        this.cursoDao = new Curso_dao();
        this.resultadoMateriaDao = new Resultado_Materia_dao();
        this.logUsuarioDao = new Log_Usuario_dao();
    }

    private void registrarLog(Integer idUsuario, String accion, String campo, String valorAnterior, String valorNuevo) {
        try {
            Log_Usuario log = new Log_Usuario();
            log.setId_usuario(idUsuario != null ? idUsuario : 1);
            log.setAccion(accion);
            log.setCampo_modificado(campo);
            log.setValor_anterior(valorAnterior);
            log.setValor_nuevo(valorNuevo);
            log.setFecha_hora(LocalDateTime.now());
            logUsuarioDao.insertar(log);
        } catch (Exception e) {
            System.err.println("Error al registrar log de inscripción: " + e.getMessage());
        }
    }

    public boolean alumnoActivo(Integer idAlumno) {
        Alumno alumno = alumnoDao.obtenerPorId(idAlumno);
        return alumno != null && "Activo".equalsIgnoreCase(alumno.getEstado());
    }

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

    public List<Inscripcion> buscarCampo(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return inscripcionDao.obtenerTodos();
        }
        return inscripcionDao.buscarCampo(filtro.trim());
    }

    public List<Inscripcion> filtrarEstadoInscrip(String estadoFiltro) {
        if (estadoFiltro == null || estadoFiltro.trim().isEmpty() || "Todos".equalsIgnoreCase(estadoFiltro)) {
            return inscripcionDao.obtenerTodos();
        }
        return inscripcionDao.obtenerPorEstado(estadoFiltro);
    }

    public void registrar(Inscripcion inscripcion, Integer idUsuario) {
        inscripcionDao.insertar(inscripcion);
        registrarLog(idUsuario, "REGISTRAR", "Inscripciones", null, 
            "Alumno ID: " + inscripcion.getId_alumno() + " | Curso ID: " + inscripcion.getId_curso() + " | Estado: " + inscripcion.getEstado() + " | Cuota: $" + inscripcion.getImporte_cuota());
    }

    public void modificar(Inscripcion inscripcion, Integer idUsuario) {
        Inscripcion anterior = inscripcionDao.obtenerPorId(inscripcion.getId_inscripcion());
        String valorAnt = anterior != null ? "Estado: " + anterior.getEstado() + " | Importe: $" + anterior.getImporte_cuota() : "Desconocido";
        
        inscripcionDao.modificar(inscripcion);
        
        String valorNue = "Estado: " + inscripcion.getEstado() + " | Importe: $" + inscripcion.getImporte_cuota();
        registrarLog(idUsuario, "MODIFICAR", "Inscripciones", valorAnt, valorNue);
    }

    public void darDeBaja(Integer idInscripcion, String motivoBaja, Integer idUsuario) {
        Inscripcion ins = inscripcionDao.obtenerPorId(idInscripcion);
        if (ins != null) {
            String valorAnt = "Estado: " + ins.getEstado();
            ins.setEstado("Finalizada");
            ins.setMotivo_baja(motivoBaja);
            inscripcionDao.modificar(ins);
            
            String valorNue = "Estado: Finalizada | Motivo de baja: " + motivoBaja;
            registrarLog(idUsuario, "DAR DE BAJA", "Inscripciones", valorAnt, valorNue);
        }
    }
}
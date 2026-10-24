package servicios;

import dao.*;
import clases.*;
import java.time.LocalDateTime;
import java.util.List;

public class Admin_servicio {
    private Usuario_dao usuarioDao;
    private Log_Usuario_dao logUsuarioDao;
    private Alumno_dao alumnoDao;
    private Docente_dao docenteDao;
    private Curso_dao cursoDao;
    private Inscripcion_dao inscripcionDao;
    private Responsable_dao responsableDao;

    public Admin_servicio() {
        this.usuarioDao = new Usuario_dao();
        this.logUsuarioDao = new Log_Usuario_dao();
        this.alumnoDao = new Alumno_dao();
        this.docenteDao = new Docente_dao();
        this.cursoDao = new Curso_dao();
        this.inscripcionDao = new Inscripcion_dao();
        this.responsableDao = new Responsable_dao();
    }

    // cuenta cuantos alumnos hay consultando al dao
    public int contarAlumnos() {
        try { return alumnoDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

    // cuenta el total de docentes registrados
    public int contarDocentes() {
        try { return docenteDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

    // recorre todos los cursos buscando los que tengan estado activo
    public int contarCursosActivos() {
        try {
            List<Curso> cursos = cursoDao.obtenerTodos();
            int count = 0;
            for (Curso c : cursos) {
                if ("Activo".equalsIgnoreCase(c.getEstado())) count++;
            }
            return count > 0 ? count : cursos.size();
        } catch (Exception e) { return 0; }
    }

    // cuenta todas las inscripciones que existen en la bd
    public int contarInscripciones() {
        try { return inscripcionDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

    // cuenta el total de cuentas de usuario creadas
    public int contarUsuarios() {
        try { return usuarioDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

    // revisa los legajos de los docentes existentes para calcular el proximo numero disponible
    public String generarSiguienteLegajoDocente() {
        try {
            List<Docente> docentes = docenteDao.obtenerTodos();
            int maxNum = 0;
            for (Docente d : docentes) {
                String leg = d.getLegajo();
                if (leg != null) {
                    String digits = leg.replaceAll("\\D+", "");
                    if (!digits.isEmpty()) {
                        int val = Integer.parseInt(digits);
                        if (val > maxNum) maxNum = val;
                    }
                }
            }
            if (maxNum == 0) maxNum = docentes.size();
            return String.valueOf(maxNum + 1);
        } catch (Exception e) {
            return "1";
        }
    }

    // calcula de forma automatica la siguiente matricula numerica para un nuevo alumno
    public String generarSiguienteMatriculaAlumno() {
        try {
            List<Alumno> alumnos = alumnoDao.obtenerTodos();
            int maxNum = 0;
            for (Alumno a : alumnos) {
                String mat = a.getMatricula();
                if (mat != null) {
                    String digits = mat.replaceAll("\\D+", "");
                    if (!digits.isEmpty()) {
                        int val = Integer.parseInt(digits);
                        if (val > maxNum) maxNum = val;
                    }
                }
            }
            if (maxNum == 0) maxNum = alumnos.size();
            return String.valueOf(maxNum + 1);
        } catch (Exception e) {
            return "1";
        }
    }

    // registra una accion en la tabla de auditoria log_usuario
    private void registrarLog(Integer idUsuario, String accion, String campo, String valorNuevo) {
        try {
            Log_Usuario log = new Log_Usuario();
            log.setId_usuario(idUsuario != null ? idUsuario : 1);
            log.setAccion(accion);
            log.setCampo_modificado(campo);
            log.setValor_nuevo(valorNuevo);
            log.setFecha_hora(LocalDateTime.now());
            logUsuarioDao.insertar(log);
        } catch (Exception e) {
            System.err.println("Error al registrar log: " + e.getMessage());
        }
    }

    // registra todo junto: crea el usuario, el responsable, genera la matricula y guarda el alumno
    public void registrarAlumnoConResponsableYUsuario(
            Integer idAdminLogueado,
            String usuario, String contraseña,
            String nombreAlumno, String apellidoAlumno, String dniAlumno, String estadoAlumno,
            String nombreResp, String apellidoResp, String dniResp, String tipoRelacion) {
        
        Usuario u = new Usuario(null, 3, usuario, contraseña);
        usuarioDao.insertar(u);

        Integer idUsuario = null;
        for (Usuario usu : usuarioDao.obtenerTodos()) {
            if (usu.getNombre_usuario().equals(usuario)) {
                idUsuario = usu.getId_usuario();
                break;
            }
        }

        Responsable resp = new Responsable(null, nombreResp, apellidoResp, dniResp, tipoRelacion);
        responsableDao.insertar(resp);

        Integer idResp = null;
        for (Responsable r : responsableDao.obtenerTodos()) {
            if (r.getDni().equals(dniResp)) {
                idResp = r.getId_responsable();
                break;
            }
        }

        String matriculaAuto = generarSiguienteMatriculaAlumno();
        Alumno alumno = new Alumno(null, idUsuario, idResp, nombreAlumno, apellidoAlumno, dniAlumno, matriculaAuto, estadoAlumno);
        alumnoDao.insertar(alumno);

        registrarLog(idAdminLogueado, "INSERT", "Alumnos", "Nuevo alumno registrado: " + nombreAlumno + " " + apellidoAlumno);
    }

    // crea un nuevo usuario docente con su respectivo legajo automatico
    public void registrarDocenteConUsuario(
            Integer idAdminLogueado,
            String usuario, String contraseña,
            String nombreDocente, String apellidoDocente, String dniDocente, String estadoDocente) {
        
        Usuario u = new Usuario(null, 2, usuario, contraseña);
        usuarioDao.insertar(u);

        Integer idUsuario = null;
        for (Usuario usu : usuarioDao.obtenerTodos()) {
            if (usu.getNombre_usuario().equals(usuario)) {
                idUsuario = usu.getId_usuario();
                break;
            }
        }

        String legajoAuto = generarSiguienteLegajoDocente();
        Docente docente = new Docente(null, idUsuario, nombreDocente, apellidoDocente, dniDocente, legajoAuto, estadoDocente);
        docenteDao.insertar(docente);

        registrarLog(idAdminLogueado, "INSERT", "Docentes", "Nuevo docente registrado: " + nombreDocente + " " + apellidoDocente);
    }

    // modificamos los datos del alumno y guardamos el log
    public void modificarAlumno(Alumno alumno, Integer idAdminLogueado) {
        alumnoDao.modificar(alumno);
        registrarLog(idAdminLogueado, "MODIFICAR", "Alumnos", "Actualizado alumno ID: " + alumno.getId_alumno() + " - Estado: " + alumno.getEstado());
    }

    // modificamos los datos del docente y guardamos el log
    public void modificarDocente(Docente docente, Integer idAdminLogueado) {
        docenteDao.modificar(docente);
        registrarLog(idAdminLogueado, "MODIFICAR", "Docentes", "Actualizado docente ID: " + docente.getId_docente() + " - Estado: " + docente.getEstado());
    }

    // damos de baja al alumno pasando su estado a inactivo
    public void darDeBajaAlumno(Integer idAlumno, Integer idAdminLogueado) {
        Alumno alu = alumnoDao.obtenerPorId(idAlumno);
        if (alu != null) {
            alu.setEstado("Inactivo");
            alumnoDao.modificar(alu);
            registrarLog(idAdminLogueado, "DAR DE BAJA", "Alumnos", "Alumno ID " + idAlumno + " pasado a estado Inactivo");
        }
    }

    // damos de baja al docente pasando su estado a inactivo
    public void darDeBajaDocente(Integer idDocente, Integer idAdminLogueado) {
        Docente doc = docenteDao.obtenerPorId(idDocente);
        if (doc != null) {
            doc.setEstado("Inactivo");
            docenteDao.modificar(doc);
            registrarLog(idAdminLogueado, "DAR DE BAJA", "Docentes", "Docente ID " + idDocente + " pasado a estado Inactivo");
        }
    }
}
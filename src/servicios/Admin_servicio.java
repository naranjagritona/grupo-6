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

    public int contarAlumnos() {
        try { return alumnoDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

    public int contarDocentes() {
        try { return docenteDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

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

    public int contarInscripciones() {
        try { return inscripcionDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

    public int contarUsuarios() {
        try { return usuarioDao.obtenerTodos().size(); } catch (Exception e) { return 0; }
    }

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

    public void registrarAlumnoConResponsableYUsuario(
            Integer idAdminLogueado,
            String usuario, String contraseña,
            String nombreAlumno, String apellidoAlumno, String dniAlumno, String estadoAlumno,
            String nombreResp, String apellidoResp, String dniResp, String tipoRelacion) {
        
        // 1. Crear Usuario (Rol 3 = Alumno)
        Usuario u = new Usuario(null, 3, usuario, contraseña);
        usuarioDao.insertar(u);

        // Recuperar ID de usuario creado
        Integer idUsuario = null;
        for (Usuario usu : usuarioDao.obtenerTodos()) {
            if (usu.getNombre_usuario().equals(usuario)) {
                idUsuario = usu.getId_usuario();
                break;
            }
        }

        // 2. Crear Responsable
        Responsable resp = new Responsable(null, nombreResp, apellidoResp, dniResp, tipoRelacion);
        responsableDao.insertar(resp);

        // Recuperar ID de responsable creado
        Integer idResp = null;
        for (Responsable r : responsableDao.obtenerTodos()) {
            if (r.getDni().equals(dniResp)) {
                idResp = r.getId_responsable();
                break;
            }
        }

        // 3. Generar Matrícula Automática
        String matriculaAuto = generarSiguienteMatriculaAlumno();

        // 4. Crear Alumno
        Alumno alumno = new Alumno(null, idUsuario, idResp, nombreAlumno, apellidoAlumno, dniAlumno, matriculaAuto, estadoAlumno);
        alumnoDao.insertar(alumno);

        // 5. Registrar en Log de Usuarios
        registrarLog(idAdminLogueado, "INSERT", "Alumnos", "Nuevo alumno registrado: " + nombreAlumno + " " + apellidoAlumno + " (Mat: " + matriculaAuto + ")");
    }

    public void registrarDocenteConUsuario(
            Integer idAdminLogueado,
            String usuario, String contraseña,
            String nombreDocente, String apellidoDocente, String dniDocente, String estadoDocente) {
        
        // 1. Crear Usuario (Rol 2 = Docente)
        Usuario u = new Usuario(null, 2, usuario, contraseña);
        usuarioDao.insertar(u);

        // Recuperar ID de usuario creado
        Integer idUsuario = null;
        for (Usuario usu : usuarioDao.obtenerTodos()) {
            if (usu.getNombre_usuario().equals(usuario)) {
                idUsuario = usu.getId_usuario();
                break;
            }
        }

        // 2. Generar Legajo Automático
        String legajoAuto = generarSiguienteLegajoDocente();

        // 3. Crear Docente
        Docente docente = new Docente(null, idUsuario, nombreDocente, apellidoDocente, dniDocente, legajoAuto, estadoDocente);
        docenteDao.insertar(docente);

        // 4. Registrar en Log de Usuarios
        registrarLog(idAdminLogueado, "INSERT", "Docentes", "Nuevo docente registrado: " + nombreDocente + " " + apellidoDocente + " (Leg: " + legajoAuto + ")");
    }
}
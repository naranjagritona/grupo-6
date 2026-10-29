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

    // recibimos el dao que se utiliza para compartir la conexión durante los cambios
    public Inscripcion_servicio(Inscripcion_dao inscripciones) {
        this.inscripcionDao = inscripciones;
        this.alumnoDao = new Alumno_dao();
        this.cursoDao = new Curso_dao();
        this.resultadoMateriaDao = new Resultado_Materia_dao();
    }

    // preparamos los daos para validar y consultar los datos de inscripción
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
                    && java.util.List.of("Confirmada", "Pendiente", "Recuperacion").contains(ins.getEstado())) {
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

    // validamos los permisos, el alumno y el cupo antes de guardar una inscripción
    public void registrar(Inscripcion inscripcion, Integer idUsuario) {
        try {
            inscripcionDao.transaccion(conexion -> {
                Permisos_servicio.verificar(conexion, inscripcionDao, idUsuario, 1);
                if (!java.util.List.of("Pendiente", "Confirmada").contains(inscripcion.getEstado())) {
                    throw new IllegalArgumentException("La inscripción se registra como Pendiente o Confirmada.");
                }
                if (!"Activo".equals(inscripcionDao.alumno(conexion, inscripcion.getId_alumno()).get("estado"))) {
                    throw new IllegalArgumentException("El alumno no está activo.");
                }
                validarDestino(conexion, inscripcion.getId_alumno(), inscripcion.getId_curso());
                if (inscripcion.getFecha_inscripcion() == null) throw new IllegalArgumentException("Ingresá una fecha válida.");
                int id = inscripcionDao.crear(conexion, inscripcion);
                inscripcionDao.movimiento(conexion, id, null, inscripcion.getEstado(), "Registro de inscripción", idUsuario);
                return null;
            });
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // conservamos el alumno y el curso originales y modificamos solamente una inscripción abierta
    public void modificar(Inscripcion inscripcion, Integer idUsuario) {
        try {
            inscripcionDao.transaccion(conexion -> {
                Permisos_servicio.verificar(conexion, inscripcionDao, idUsuario, 1);
                var anterior = inscripcionDao.bloquear(conexion, inscripcion.getId_inscripcion());
                validarAbierta(anterior);
                if (!java.util.List.of("Pendiente", "Confirmada").contains(inscripcion.getEstado())) {
                    throw new IllegalArgumentException("Para cerrar o trasladar la inscripción utilizá su opción correspondiente.");
                }
                if (dao.Consulta_academica_dao.numero(anterior, "id_alumno") != inscripcion.getId_alumno()
                        || dao.Consulta_academica_dao.numero(anterior, "id_curso") != inscripcion.getId_curso()) {
                    throw new IllegalArgumentException("El curso y el alumno se conservan. Utilizá la opción de cambio de división.");
                }
                if ("Recuperacion".equals(anterior.get("estado"))) {
                    throw new IllegalArgumentException("La recuperación se resuelve al aprobar las materias y revisar la promoción.");
                }
                inscripcionDao.estado(conexion, inscripcion.getId_inscripcion(), inscripcion.getEstado(), "");
                inscripcionDao.movimiento(conexion, inscripcion.getId_inscripcion(), anterior.get("estado").toString(), inscripcion.getEstado(), "Modificación administrativa", idUsuario);
                return null;
            });
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // cancelamos la inscripción con un motivo sin eliminar sus notas ni sus datos anteriores
    public void darDeBaja(Integer id, String motivo, Integer usuario) {
        try {
            String razon = motivo(motivo);
            inscripcionDao.transaccion(conexion -> {
                Permisos_servicio.verificar(conexion, inscripcionDao, usuario, 1);
                var anterior = inscripcionDao.bloquear(conexion, id);
                validarAbierta(anterior);
                inscripcionDao.estado(conexion, id, "Cancelada", razon);
                inscripcionDao.movimiento(conexion, id, anterior.get("estado").toString(), "Cancelada", razon, usuario);
                return null;
            });
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // comprobamos que el motivo esté completo y se pueda guardar sin recortarlo
    public static String motivo(String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.trim().length() > 255) {
            throw new IllegalArgumentException("Ingresá un motivo de hasta 255 caracteres.");
        }
        return motivo.trim();
    }

    // revisamos el estado para impedir cambios en inscripciones que ya fueron cerradas
    public static void validarAbierta(java.util.Map<String, Object> inscripcion) {
        if (!java.util.List.of("Pendiente", "Confirmada", "Recuperacion").contains(inscripcion.get("estado"))) {
            throw new IllegalArgumentException("La inscripción está cerrada y conserva sus notas e historial.");
        }
    }

    // revisamos que el destino esté activo, tenga lugar y no duplique la inscripción del alumno
    private void validarDestino(java.sql.Connection conexion, int alumno, int curso) throws Exception {
        var destino = inscripcionDao.curso(conexion, curso);
        if (!"Activo".equals(destino.get("estado"))) throw new IllegalArgumentException("El curso de destino no está activo.");
        if (inscripcionDao.duplicada(conexion, alumno, curso)) throw new IllegalArgumentException("El alumno ya tiene una inscripción activa en el destino.");
        if (inscripcionDao.ocupacion(conexion, curso) >= dao.Consulta_academica_dao.numero(destino, "cupo_maximo")) throw new IllegalArgumentException("El destino no tiene cupo disponible.");
    }

    // trasladamos al alumno a otra división del mismo año y período sin mover sus calificaciones
    public void cambiarCurso(Integer origen, Integer destino, String motivo, Integer usuario) throws Exception {
        String razon = motivo(motivo);
        inscripcionDao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, inscripcionDao, usuario, 1);
            trasladar(conexion, origen, destino, razon, usuario, false, "Trasladado");
            return null;
        });
    }

    // cerramos la inscripción anterior y generamos la de destino dentro de la misma transacción
    public int trasladar(java.sql.Connection conexion, int origen, int destino, String motivo, int usuario, boolean excepcional, String estadoOrigen) throws Exception {
        var anterior = inscripcionDao.bloquear(conexion, origen);
        validarAbierta(anterior);
        if ("Pendiente".equals(anterior.get("estado"))) throw new IllegalArgumentException("Primero confirmá la inscripción.");
        if (!"Activo".equals(anterior.get("estado_alumno"))) throw new IllegalArgumentException("El alumno no está activo.");
        var curso = inscripcionDao.curso(conexion, destino);
        if (dao.Consulta_academica_dao.numero(anterior, "id_curso") == destino) throw new IllegalArgumentException("Seleccioná una división de destino diferente.");
        if (!excepcional && (!anterior.get("año").toString().equals(curso.get("año").toString())
                || dao.Consulta_academica_dao.numero(anterior, "id_periodo") != dao.Consulta_academica_dao.numero(curso, "id_periodo"))) {
            throw new IllegalArgumentException("El cambio de división conserva el año y el período. Para otro año enviá una solicitud a dirección.");
        }
        validarDestino(conexion, dao.Consulta_academica_dao.numero(anterior, "id_alumno"), destino);
        inscripcionDao.estado(conexion, origen, estadoOrigen, motivo);
        int nueva = inscripcionDao.destino(conexion, dao.Consulta_academica_dao.numero(anterior, "id_alumno"), destino, origen);
        inscripcionDao.movimiento(conexion, origen, anterior.get("estado").toString(), estadoOrigen, motivo, usuario);
        inscripcionDao.movimiento(conexion, nueva, null, "Confirmada", "Inscripción de destino vinculada a la anterior #" + origen, usuario);
        return nueva;
    }

    public record Ficha(java.util.Map<String, Object> datos, java.util.List<java.util.Map<String, Object>> notas,
            java.util.List<java.util.Map<String, Object>> resultados, java.util.List<java.util.Map<String, Object>> movimientos) {}

    // consultamos los datos del alumno y de la inscripción para mostrarlos en una ventana de detalle
    public Ficha ficha(int id, Integer usuario) throws Exception {
        return inscripcionDao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, inscripcionDao, usuario, 1, 5);
            return new Ficha(inscripcionDao.ficha(conexion, id), inscripcionDao.notas(conexion, id), inscripcionDao.resultados(conexion, id), inscripcionDao.movimientos(conexion, id));
        });
    }
}

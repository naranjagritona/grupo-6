package servicios;

import clases.Curso;
import clases.Usuario;

import conexion.Conexion;

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
    public List<dto.CursoVista> listar(Usuario usuario) {
        try (java.sql.Connection db = conexion.Conexion.getConnection()) {
            return vistas(db, dao.listar(usuario));
        } catch (java.sql.SQLException error) {
            throw new IllegalStateException(
                    "No se pudieron cargar los datos de los cursos.", error);
        }
    }

    public List<dto.CursoVista> obtenerTodos() {
        return listar(null);
    }

    public dto.CursoVista obtenerPorId(java.sql.Connection db, int id)
            throws java.sql.SQLException {
        Curso curso = dao.buscar(db, id);
        return curso == null ? null : vistas(db, List.of(curso)).getFirst();
    }

    private List<dto.CursoVista> vistas(java.sql.Connection db, List<Curso> cursos)
            throws java.sql.SQLException {
        var periodos = new java.util.HashMap<Integer, clases.Periodo_Lectivo>();
        for (var periodo : new dao.Periodo_Lectivo_dao().listarTabla(db))
            periodos.put(periodo.getId_periodo_lectivo(), periodo);
        var materias = new java.util.HashMap<Integer, clases.Materia>();
        for (var materia : new dao.Materia_dao().listarTabla(db))
            materias.put(materia.getId_materia(), materia);
        var docentes = new java.util.HashMap<Integer, clases.Docente>();
        for (var docente : new dao.Docente_dao().listarTabla(db))
            docentes.put(docente.getId_docente(), docente);
        var relaciones = new dao.Curso_Materia_dao().listarTabla(db);
        List<dto.CursoVista> lista = new java.util.ArrayList<>();
        for (Curso curso : cursos) {
            var vista = new dto.CursoVista(curso);
            var periodo = periodos.get(curso.getId_periodo());
            vista.setNombre_periodo(periodo == null ? null : periodo.getNombre());
            var asignaciones =
                    relaciones.stream()
                            .filter(r -> r.getId_curso().equals(curso.getId_curso()))
                            .toList();
            vista.setCantidad_materias(asignaciones.size());
            var nombres =
                    asignaciones.stream()
                            .map(r -> materias.get(r.getId_materia()))
                            .filter(java.util.Objects::nonNull)
                            .map(clases.Materia::getNombre_materia)
                            .sorted()
                            .toList();
            vista.setNombre_materia(
                    nombres.isEmpty() ? "Sin materias" : String.join(", ", nombres));
            var profesores =
                    asignaciones.stream()
                            .map(r -> docentes.get(r.getId_docente()))
                            .filter(java.util.Objects::nonNull)
                            .sorted(java.util.Comparator.comparing(clases.Docente::getApellido))
                            .map(d -> d.getNombre() + " " + d.getApellido())
                            .distinct()
                            .toList();
            vista.setNombre_docente(
                    profesores.isEmpty() ? "Sin asignar" : String.join(", ", profesores));
            lista.add(vista);
        }
        return lista;
    }

    // buscamos las materias que ya están relacionadas con el curso
    public List<Integer> materiasDelCurso(int curso) {
        try (java.sql.Connection db = conexion.Conexion.getConnection()) {
            return new dao.Curso_Materia_dao()
                    .delCurso(db, curso).stream().map(clases.Curso_Materia::getId_materia).toList();
        } catch (java.sql.SQLException error) {
            throw new IllegalStateException(
                    "No se pudieron consultar las materias del curso.", error);
        }
    }

    // enviamos el curso y las materias al dao para validar y guardar sus datos
    public void guardar(Curso curso, List<Integer> materias) {
        dao.guardar(curso, materias);
    }

    // cancelamos el curso conservando las inscripciones y el historial
    public void cancelar(int curso) {
        dao.cancelar(curso);
    }

    // verificamos el cupo antes de guardar el nuevo valor
    public void modificarCupo(int curso, int cupo) throws Exception {
        dao.modificarCupo(curso, cupo);
    }

    // delegamos la asignación para informar que las materias se administran por año
    public void asignarMaterias(int curso, List<Integer> materias) throws Exception {
        dao.asignarMaterias(curso, materias);
    }

    // comprobamos el nombre, el tipo y las fechas antes de registrar el período
    public void crearPeriodo(String nombre, String tipo, String desde, String hasta)
            throws Exception {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 100)
            throw new IllegalArgumentException(
                    "Ingresá un nombre de período de hasta 100 caracteres.");
        if (!List.of("Anual", "Cuatrimestral", "Otro").contains(tipo))
            throw new IllegalArgumentException("Seleccioná un tipo de período válido.");
        LocalDate inicio = LocalDate.parse(desde.trim()), fin = LocalDate.parse(hasta.trim());
        if (fin.isBefore(inicio))
            throw new IllegalArgumentException("El fin no puede ser anterior al inicio.");
        dao.crearPeriodo(nombre.trim(), tipo, inicio, fin);
    }

    // coordinamos la asignación y su aviso en una transacción y enviamos sólo después de
    // confirmarla
    public void asignarDocente(
            servicios.PlanCurso_servicio plan,
            int curso,
            int materia,
            Integer docente,
            Integer anterior,
            Integer actor)
            throws Exception {
        boolean cambio =
                Conexion.transaccion(
                        db -> {
                            Permisos_servicio.verificar(db, actor, clases.Rol.DIRECTIVO);
                            var relacion = plan.bloquearAsignacion(db, curso, materia);
                            if (!java.util.Objects.equals(relacion.getId_docente(), anterior))
                                throw new IllegalArgumentException(
                                        "Otro usuario cambió esta asignación. Actualizá el detalle"
                                                + " y volvé a intentar.");
                            if (docente != null && !plan.docenteActivo(db, docente))
                                throw new IllegalArgumentException("Seleccioná un docente activo.");
                            if (java.util.Objects.equals(docente, anterior)) return false;
                            plan.actualizarAsignacion(db, curso, materia, docente);
                            if (docente != null) {
                                var avisos = new dao.Notificacion_Correo_dao();
                                new Notificaciones_servicio(avisos)
                                        .asignacion(
                                                db,
                                                new dao.Docente_dao()
                                                        .requerir(db, docente)
                                                        .getId_usuario(),
                                                curso,
                                                "docente-" + java.util.UUID.randomUUID(),
                                                actor);
                            }
                            return true;
                        });
        if (cambio) Notificaciones_servicio.enviarPendientes(actor);
    }
}

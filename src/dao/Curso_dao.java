package dao;

import clases.Curso;
import clases.Usuario;

import conexion.Conexion;

import servicios.PlanCurso_servicio;

import java.sql.*;
import java.util.*;

// consultamos y modificamos los registros de curso en la base de datos
public class Curso_dao implements Dao<Curso, Integer> {
    // abrimos la conexión que utilizan las consultas y los cambios de cursos

    private static final String CONSULTA = "SELECT c.* FROM curso c";

    private Curso leer(ResultSet rs) throws SQLException {
        return leerEntidad(rs);
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Curso> obtenerTodos() {
        return listar(null);
    }

    // el administrador ve todos, docentes y alumnos solo ven los cursos que les corresponden.
    public List<Curso> listar(Usuario usuario) {
        usuario = servicios.Permisos_servicio.actual();
        if (!servicios.Permisos_servicio.verAcademico(usuario))
            throw new IllegalArgumentException("No tenés acceso a Cursos.");
        String sql = CONSULTA;
        List<Integer> parametros = new ArrayList<>();
        if (usuario != null && usuario.getId_rol() == clases.Rol.DOCENTE) {
            sql +=
                    " WHERE EXISTS(SELECT 1 FROM curso_materia cm2 JOIN docente d2 ON"
                            + " d2.id_docente=cm2.id_docente WHERE cm2.id_curso=c.id_curso AND"
                            + " d2.id_usuario=?)";
            parametros.add(usuario.getId_usuario());
        }
        if (usuario != null && usuario.getId_rol() == clases.Rol.ALUMNO) {
            sql +=
                    " WHERE EXISTS(SELECT 1 FROM inscripcion i JOIN alumno a ON"
                            + " a.id_alumno=i.id_alumno WHERE i.id_curso=c.id_curso AND"
                            + " a.id_usuario=?)";
            parametros.add(usuario.getId_usuario());
        }
        sql += " ORDER BY CAST(c.año AS UNSIGNED),c.division,c.id_curso";
        try (Connection db = Conexion.getConnection();
                PreparedStatement q = db.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) q.setInt(i + 1, parametros.get(i));
            List<Curso> lista = new ArrayList<>();
            try (ResultSet rs = q.executeQuery()) {
                while (rs.next()) lista.add(leer(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar los cursos.", e);
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Curso obtenerPorId(Integer id) {
        try (Connection db = Conexion.getConnection()) {
            return obtenerPorId(db, id);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el curso.", e);
        }
    }

    // consultamos un curso usando una conexión existente para conservar la misma transacción
    public Curso obtenerPorId(Connection db, int id) throws SQLException {
        try (PreparedStatement q = db.prepareStatement(CONSULTA + " WHERE c.id_curso=?")) {
            q.setInt(1, id);
            try (ResultSet rs = q.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Curso c) {
        guardar(c, List.of(c.getId_materia()));
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Curso c) {
        List<Integer> materias = new servicios.Curso_servicio().materiasDelCurso(c.getId_curso());
        if (materias.isEmpty()) materias = List.of(c.getId_materia());
        guardar(c, materias);
    }

    // validamos los datos antes de guardar para no dejar un curso incompleto.
    public void guardar(Curso c, List<Integer> seleccion) {
        if (!servicios.Permisos_servicio.directivo(servicios.Permisos_servicio.actual()))
            throw new IllegalArgumentException("Esta operación corresponde a dirección.");
        List<Integer> materias = new ArrayList<>();
        if (c.getAño() == null || c.getAño() < 1 || c.getAño() > 6)
            throw new IllegalArgumentException("El año debe estar entre 1 y 6.");
        if (c.getCupo_maximo() == null || c.getCupo_maximo() < 1 || c.getCupo_maximo() > 1000)
            throw new IllegalArgumentException("Revisá el cupo máximo.");
        if (c.getFecha_inicio() == null
                || c.getFecha_fin() == null
                || c.getFecha_fin().isBefore(c.getFecha_inicio()))
            throw new IllegalArgumentException("Revisá las fechas del curso.");
        if (!List.of("Presencial", "Virtual", "Híbrido").contains(c.getModalidad())
                || !List.of("mañana", "tarde", "vespertino").contains(c.getTurno())
                || !List.of("Activo", "Cancelado", "Finalizado").contains(c.getEstado()))
            throw new IllegalArgumentException("Revisá modalidad, turno y estado.");
        if (c.getDivision() == null
                || c.getDivision().isBlank()
                || c.getDivision().trim().length() > 30)
            throw new IllegalArgumentException("Ingresá una división de hasta 30 caracteres.");
        c.setDivision(c.getDivision().trim());
        c.setEstado(
                servicios.Curso_servicio.estadoSegunFecha(
                        c.getEstado(),
                        c.getFecha_fin(),
                        java.time.LocalDate.now(
                                java.time.ZoneId.of("America/Argentina/Buenos_Aires"))));
        boolean nuevo = c.getId_curso() == null || c.getId_curso() == 0;
        int id = nuevo ? 0 : c.getId_curso();
        try (Connection db = Conexion.getConnection()) {
            db.setAutoCommit(false);
            try {
                // Leemos el plan dentro de la misma transacción que crea el curso.
                materias.addAll(
                        new PlanCurso_servicio()
                                .materias(db, c.getAño()).stream()
                                        .map(clases.Materia::getId_materia)
                                        .toList());
                if (materias.isEmpty())
                    throw new IllegalArgumentException(
                            "Agregá materias a este año desde el módulo Materias.");
                // bloqueamos el período para evitar dos cursos del mismo año al guardar a la vez.
                try (PreparedStatement q =
                        db.prepareStatement(
                                "SELECT id_periodo_lectivo FROM periodo_lectivo WHERE"
                                        + " id_periodo_lectivo=? FOR UPDATE")) {
                    q.setInt(1, c.getId_periodo());
                    try (ResultSet rs = q.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("El período no existe.");
                    }
                }
                try (PreparedStatement q =
                        db.prepareStatement(
                                "SELECT COUNT(*) FROM curso WHERE id_periodo=? AND año=? AND"
                                        + " division=? AND turno=? AND id_curso<>?")) {
                    q.setInt(1, c.getId_periodo());
                    q.setInt(2, c.getAño());
                    q.setString(3, c.getDivision());
                    q.setString(4, c.getTurno());
                    q.setInt(5, id);
                    try (ResultSet rs = q.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0)
                            throw new IllegalArgumentException(
                                    "Ya existe ese año, división y turno en el período"
                                            + " seleccionado.");
                    }
                }
                if (!nuevo) {
                    try (PreparedStatement consulta =
                            db.prepareStatement(
                                    "SELECT c.año,c.id_periodo,c.division,(SELECT COUNT(*) FROM"
                                            + " inscripcion i WHERE i.id_curso=c.id_curso)"
                                            + " inscripciones FROM curso c WHERE c.id_curso=? FOR"
                                            + " UPDATE")) {
                        consulta.setInt(1, id);
                        try (ResultSet anterior = consulta.executeQuery()) {
                            if (!anterior.next())
                                throw new IllegalArgumentException("El curso no existe.");
                            if (anterior.getInt("inscripciones") > 0
                                    && (anterior.getInt("año") != c.getAño()
                                            || anterior.getInt("id_periodo") != c.getId_periodo()
                                            || (!"Sin asignar"
                                                            .equals(anterior.getString("division"))
                                                    && !c.getDivision()
                                                            .equals(
                                                                    anterior.getString(
                                                                            "division")))))
                                throw new IllegalArgumentException(
                                        "El curso tiene inscripciones. Conservá su año, división y"
                                                + " período y creá otro curso para el destino.");
                        }
                    }
                }
                // conservamos la referencia original para no cambiar la materia de las
                // evaluaciones.
                int principal = materias.getFirst();
                if (!nuevo) {
                    try (PreparedStatement q =
                            db.prepareStatement(
                                    "SELECT id_materia FROM curso WHERE id_curso=? FOR UPDATE")) {
                        q.setInt(1, id);
                        try (ResultSet rs = q.executeQuery()) {
                            if (!rs.next())
                                throw new IllegalArgumentException("El curso no existe.");
                            int anterior = rs.getInt(1);
                            if (materias.contains(anterior)) principal = anterior;
                            else
                                try (PreparedStatement notas =
                                        db.prepareStatement(
                                                "SELECT COUNT(*) FROM columna_notas WHERE"
                                                        + " id_curso=?")) {
                                    notas.setInt(1, id);
                                    try (ResultSet n = notas.executeQuery()) {
                                        n.next();
                                        if (n.getInt(1) > 0)
                                            throw new IllegalArgumentException(
                                                    "Conservá la materia original porque ya tiene"
                                                            + " evaluaciones registradas.");
                                    }
                                }
                        }
                    }
                    try (PreparedStatement q =
                            db.prepareStatement(
                                    "SELECT COUNT(*) FROM inscripcion WHERE id_curso=? AND estado"
                                            + " IN ('Pendiente','Confirmada','Recuperacion')")) {
                        q.setInt(1, id);
                        try (ResultSet rs = q.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) > c.getCupo_maximo())
                                throw new IllegalArgumentException(
                                        "El cupo es menor que la cantidad de alumnos inscriptos.");
                        }
                    }
                }
                String sql =
                        nuevo
                                ? "INSERT INTO"
                                      + " curso(id_periodo,id_materia,id_docente,año,modalidad,turno,cupo_maximo,fecha_inicio,fecha_fin,estado,division)"
                                      + " VALUES(?,?,?,?,?,?,?,?,?,?,?)"
                                : "UPDATE curso SET"
                                      + " id_periodo=?,id_materia=?,id_docente=?,año=?,modalidad=?,turno=?,cupo_maximo=?,fecha_inicio=?,fecha_fin=?,estado=?,division=?"
                                      + " WHERE id_curso=?";
                try (PreparedStatement q =
                        db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    q.setInt(1, c.getId_periodo());
                    q.setInt(2, principal);
                    q.setObject(
                            3,
                            c.getId_docente() == null || c.getId_docente() == 0
                                    ? null
                                    : c.getId_docente());
                    q.setInt(4, c.getAño());
                    q.setString(5, c.getModalidad());
                    q.setString(6, c.getTurno());
                    q.setInt(7, c.getCupo_maximo());
                    q.setObject(8, c.getFecha_inicio());
                    q.setObject(9, c.getFecha_fin());
                    q.setString(10, c.getEstado());
                    q.setString(11, c.getDivision());
                    if (!nuevo) q.setInt(12, id);
                    q.executeUpdate();
                    if (nuevo)
                        try (ResultSet keys = q.getGeneratedKeys()) {
                            if (!keys.next())
                                throw new SQLException("No se obtuvo el curso creado.");
                            id = keys.getInt(1);
                        }
                }
                try (PreparedStatement q =
                        db.prepareStatement(
                                "SELECT DISTINCT id_materia FROM columna_notas WHERE id_curso=?")) {
                    q.setInt(1, id);
                    try (ResultSet rs = q.executeQuery()) {
                        while (rs.next())
                            if (!materias.contains(rs.getInt(1)))
                                throw new IllegalArgumentException(
                                        "Conservá las materias que ya tienen evaluaciones.");
                    }
                }
                List<clases.Curso_Materia> relaciones = new ArrayList<>();
                for (int materia : materias) {
                    clases.Curso_Materia relacion = new clases.Curso_Materia();
                    relacion.setId_curso(id);
                    relacion.setId_materia(materia);
                    relaciones.add(relacion);
                }
                new Curso_Materia_dao().sincronizar(db, id, relaciones);
                db.commit();
                c.setId_curso(id);
                c.setId_materia(principal);
                // si algo falla, deshacemos todo para conservar el curso y sus materias anteriores.
            } catch (Exception e) {
                db.rollback();
                if (e instanceof IllegalArgumentException x) throw x;
                throw new IllegalStateException("No se pudo guardar el curso.", e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el curso.", e);
        }
    }

    // cancelar cambia el estado, pero mantiene las inscripciones y el historial.
    public void cancelar(int id) {
        if (!servicios.Permisos_servicio.directivo(servicios.Permisos_servicio.actual()))
            throw new IllegalArgumentException("Esta operación corresponde a dirección.");
        try (Connection db = Conexion.getConnection();
                PreparedStatement q =
                        db.prepareStatement(
                                "UPDATE curso SET estado='Cancelado' WHERE id_curso=?")) {
            q.setInt(1, id);
            q.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cancelar el curso.", e);
        }
    }

    // también bloqueamos el borrado acá para que no dependa solamente del botón.
    @Override
    public void eliminar(Integer id) {
        throw new IllegalStateException(
                "Los cursos no se eliminan. Podés editarlos o cancelarlos.");
    }

    // modificamos el cupo sin permitir que sea menor que la cantidad de inscripciones activas
    public void modificarCupo(int id, int cupo) throws Exception {
        Inscripcion_dao inscripciones = new Inscripcion_dao();
        Conexion.transaccion(
                conexion -> {
                    servicios.Permisos_servicio.verificar(
                            conexion,
                            servicios.Auditoria_servicio.usuarioActual(),
                            clases.Rol.SECRETARIA,
                            clases.Rol.DIRECTIVO);
                    requerir(conexion, id);
                    if (cupo < 1 || cupo > 1000 || cupo < inscripciones.ocupacion(conexion, id))
                        throw new IllegalArgumentException(
                                "Revisá el cupo y la cantidad de inscripciones activas.");
                    try (PreparedStatement consulta =
                            conexion.prepareStatement(
                                    "UPDATE curso SET cupo_maximo=? WHERE id_curso=?")) {
                        consulta.setInt(1, cupo);
                        consulta.setInt(2, id);
                        consulta.executeUpdate();
                    }
                    return null;
                });
    }

    // impedimos la asignación individual porque las materias se administran desde el plan del año
    public void asignarMaterias(int id, List<Integer> materias) throws Exception {
        throw new IllegalArgumentException(
                "Las materias se administran por año desde el módulo Materias.");
    }

    // registramos el período lectivo para poder preparar los cursos del año siguiente
    public void crearPeriodo(
            String nombre, String tipo, java.time.LocalDate inicio, java.time.LocalDate fin)
            throws Exception {
        Conexion.transaccion(
                conexion -> {
                    servicios.Permisos_servicio.verificar(
                            conexion,
                            servicios.Auditoria_servicio.usuarioActual(),
                            clases.Rol.DIRECTIVO);
                    clases.Periodo_Lectivo periodo = new clases.Periodo_Lectivo();
                    periodo.setNombre(nombre);
                    periodo.setTipo(tipo);
                    periodo.setFecha_inicio(inicio);
                    periodo.setFecha_fin(fin);
                    periodo.setEstado("Planificado");
                    new Periodo_Lectivo_dao().crear(conexion, periodo);
                    return null;
                });
    }

    public List<Curso> delAnio(Connection db, int anio) throws SQLException {
        return seleccionar(
                db,
                "SELECT * FROM curso WHERE CAST(año AS UNSIGNED)=? ORDER BY id_curso FOR UPDATE",
                anio);
    }

    public void materiaPrincipal(Connection db, int curso, int materia) throws SQLException {
        String consulta = "UPDATE curso SET id_materia=? WHERE id_curso=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, materia);
            ps.setObject(2, curso);
            ps.executeUpdate();
        }
    }

    public void regla(Connection db, int curso, double materia, double promocion, String criterio)
            throws SQLException {
        String consulta =
                "UPDATE curso SET"
                        + " nota_aprobacion_materia=?,promedio_promocion=?,criterio_recuperatorio=?"
                        + " WHERE id_curso=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, materia);
            ps.setObject(2, promocion);
            ps.setObject(3, criterio);
            ps.setObject(4, curso);
            ps.executeUpdate();
        }
    }

    protected Curso leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Curso objeto = new Curso();
        objeto.setDivision(registros.getString("division"));
        objeto.setNota_aprobacion_materia(
                (registros.getObject("nota_aprobacion_materia") == null
                        ? null
                        : registros.getDouble("nota_aprobacion_materia")));
        objeto.setPromedio_promocion(
                (registros.getObject("promedio_promocion") == null
                        ? null
                        : registros.getDouble("promedio_promocion")));
        objeto.setCriterio_recuperatorio(registros.getString("criterio_recuperatorio"));
        objeto.setId_curso((Integer) registros.getObject("id_curso"));
        objeto.setId_periodo((Integer) registros.getObject("id_periodo"));
        objeto.setId_materia((Integer) registros.getObject("id_materia"));
        objeto.setId_docente((Integer) registros.getObject("id_docente"));
        objeto.setAño((registros.getObject("año") == null ? null : registros.getInt("año")));
        objeto.setModalidad(registros.getString("modalidad"));
        objeto.setTurno(registros.getString("turno"));
        objeto.setCupo_maximo((Integer) registros.getObject("cupo_maximo"));
        objeto.setFecha_inicio(
                (registros.getDate("fecha_inicio") == null
                        ? null
                        : registros.getDate("fecha_inicio").toLocalDate()));
        objeto.setFecha_fin(
                (registros.getDate("fecha_fin") == null
                        ? null
                        : registros.getDate("fecha_fin").toLocalDate()));
        objeto.setEstado(registros.getString("estado"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Curso
    private java.util.List<Curso> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Curso> lista = new java.util.ArrayList<>();
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            try (java.sql.ResultSet registros = ps.executeQuery()) {
                while (registros.next()) {
                    lista.add(leerEntidad(registros));
                }
            }
        }
        return lista;
    }

    // recibimos la conexión del servicio para no separar las consultas de una transacción
    public java.util.List<Curso> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM curso");
    }

    public Curso buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM curso WHERE id_curso=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Curso requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM curso WHERE id_curso=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

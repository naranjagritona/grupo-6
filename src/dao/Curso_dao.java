package dao;

import clases.Curso;
import clases.Usuario;

import conexion.Conexion;

import java.sql.*;
import java.util.*;

// consultamos y modificamos los registros de curso en la base de datos
public class Curso_dao implements Dao<Curso, Integer> {
    protected Connection abrirConexion() {
        return Conexion.getConnection();
    }

    // Juntamos los nombres de las materias para mostrar un solo registro por curso.
    private static final String CONSULTA =
            "SELECT c.*,p.nombre nombre_periodo,COALESCE((SELECT GROUP_CONCAT(m.nombre_materia"
                + " ORDER BY m.nombre_materia SEPARATOR ', ') FROM curso_materia cm JOIN materia m"
                + " ON m.id_materia=cm.id_materia WHERE cm.id_curso=c.id_curso),'Sin materias')"
                + " nombre_materia,CONCAT(d.nombre,' ',d.apellido) nombre_docente FROM curso c LEFT"
                + " JOIN periodo_lectivo p ON p.id_periodo_lectivo=c.id_periodo LEFT JOIN docente d"
                + " ON d.id_docente=c.id_docente";

    // Pasamos cada fila de la consulta a un objeto Curso.
    private Curso leer(ResultSet rs) throws SQLException {
        Curso c = new Curso();
        c.setId_curso(rs.getInt("id_curso"));
        c.setId_periodo(rs.getInt("id_periodo"));
        c.setId_materia(rs.getInt("id_materia"));
        c.setId_docente(rs.getInt("id_docente"));
        c.setAño(rs.getInt("año"));
        c.setModalidad(rs.getString("modalidad"));
        c.setTurno(rs.getString("turno"));
        c.setCupo_maximo(rs.getInt("cupo_maximo"));
        c.setFecha_inicio(rs.getDate("fecha_inicio").toLocalDate());
        c.setFecha_fin(rs.getDate("fecha_fin").toLocalDate());
        c.setEstado(rs.getString("estado"));
        c.setNombre_periodo(rs.getString("nombre_periodo"));
        c.setNombre_materia(rs.getString("nombre_materia"));
        c.setNombre_docente(rs.getString("nombre_docente"));
        return c;
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Curso> obtenerTodos() {
        return listar(null);
    }

    // El administrador ve todos; docentes y alumnos solo ven los cursos que les corresponden.
    public List<Curso> listar(Usuario usuario) {
        String sql = CONSULTA;
        List<Integer> parametros = new ArrayList<>();
        if (usuario != null && usuario.getId_rol() == 2) {
            sql +=
                    " WHERE EXISTS(SELECT 1 FROM docente d2 WHERE d2.id_docente=c.id_docente AND"
                            + " d2.id_usuario=?)";
            parametros.add(usuario.getId_usuario());
        }
        if (usuario != null && usuario.getId_rol() == 3) {
            sql +=
                    " WHERE EXISTS(SELECT 1 FROM inscripcion i JOIN alumno a ON"
                            + " a.id_alumno=i.id_alumno WHERE i.id_curso=c.id_curso AND"
                            + " a.id_usuario=?)";
            parametros.add(usuario.getId_usuario());
        }
        sql += " ORDER BY CAST(c.año AS UNSIGNED),c.id_curso";
        try (Connection db = abrirConexion();
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
        try (Connection db = abrirConexion();
                PreparedStatement q = db.prepareStatement(CONSULTA + " WHERE c.id_curso=?")) {
            q.setInt(1, id);
            try (ResultSet rs = q.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el curso.", e);
        }
    }

    // Traemos las relaciones juntas para no abrir una conexión por cada curso.
    public Map<Integer, List<Integer>> materiasPorCurso(List<Curso> cursos) {
        Map<Integer, List<Integer>> resultado = new HashMap<>();
        if (cursos.isEmpty()) {
            return resultado;
        }
        String espacios = String.join(",", Collections.nCopies(cursos.size(), "?"));
        String sql =
                "SELECT id_curso,id_materia FROM curso_materia WHERE id_curso IN ("
                        + espacios
                        + ") ORDER BY id_curso,id_materia";
        try (Connection db = abrirConexion();
                PreparedStatement q = db.prepareStatement(sql)) {
            for (int i = 0; i < cursos.size(); i++) {
                q.setInt(i + 1, cursos.get(i).getId_curso());
            }
            try (ResultSet rs = q.executeQuery()) {
                while (rs.next()) {
                    resultado
                            .computeIfAbsent(rs.getInt(1), id -> new ArrayList<>())
                            .add(rs.getInt(2));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar las relaciones de materias.", e);
        }
    }

    // Estos identificadores permiten marcar las materias al editar el curso.
    public List<Integer> materiasDelCurso(int id) {
        try (Connection db = abrirConexion();
                PreparedStatement q =
                        db.prepareStatement(
                                "SELECT id_materia FROM curso_materia WHERE id_curso=? ORDER BY"
                                        + " id_materia")) {
            q.setInt(1, id);
            List<Integer> lista = new ArrayList<>();
            try (ResultSet rs = q.executeQuery()) {
                while (rs.next()) lista.add(rs.getInt(1));
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar las materias.", e);
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
        List<Integer> materias = materiasDelCurso(c.getId_curso());
        if (materias.isEmpty()) materias = List.of(c.getId_materia());
        guardar(c, materias);
    }

    // Validamos los datos antes de guardar para no dejar un curso incompleto.
    public void guardar(Curso c, List<Integer> seleccion) {
        List<Integer> materias = new ArrayList<>(new LinkedHashSet<>(seleccion));
        if (materias.isEmpty() || materias.contains(null))
            throw new IllegalArgumentException("Seleccioná al menos una materia.");
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
        boolean nuevo = c.getId_curso() == null || c.getId_curso() == 0;
        int id = nuevo ? 0 : c.getId_curso();
        try (Connection db = abrirConexion()) {
            db.setAutoCommit(false);
            try {
                // Bloqueamos el período para evitar dos cursos del mismo año al guardar a la vez.
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
                                        + " id_curso<>?")) {
                    q.setInt(1, c.getId_periodo());
                    q.setInt(2, c.getAño());
                    q.setInt(3, id);
                    try (ResultSet rs = q.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0)
                            throw new IllegalArgumentException(
                                    "Ya existe ese año en el período seleccionado.");
                    }
                }
                // Conservamos la referencia original para no cambiar la materia de las
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
                                    "SELECT COUNT(*) FROM inscripcion WHERE id_curso=? AND"
                                            + " estado='Confirmada'")) {
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
                                      + " curso(id_periodo,id_materia,id_docente,año,modalidad,turno,cupo_maximo,fecha_inicio,fecha_fin,estado)"
                                      + " VALUES(?,?,?,?,?,?,?,?,?,?)"
                                : "UPDATE curso SET"
                                      + " id_periodo=?,id_materia=?,id_docente=?,año=?,modalidad=?,turno=?,cupo_maximo=?,fecha_inicio=?,fecha_fin=?,estado=?"
                                      + " WHERE id_curso=?";
                try (PreparedStatement q =
                        db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    q.setInt(1, c.getId_periodo());
                    q.setInt(2, principal);
                    q.setInt(3, c.getId_docente());
                    q.setInt(4, c.getAño());
                    q.setString(5, c.getModalidad());
                    q.setString(6, c.getTurno());
                    q.setInt(7, c.getCupo_maximo());
                    q.setObject(8, c.getFecha_inicio());
                    q.setObject(9, c.getFecha_fin());
                    q.setString(10, c.getEstado());
                    if (!nuevo) q.setInt(11, id);
                    q.executeUpdate();
                    if (nuevo)
                        try (ResultSet keys = q.getGeneratedKeys()) {
                            if (!keys.next())
                                throw new SQLException("No se obtuvo el curso creado.");
                            id = keys.getInt(1);
                        }
                }
                // Guardamos el curso y todas sus materias en la misma transacción.
                try (PreparedStatement q =
                        db.prepareStatement("DELETE FROM curso_materia WHERE id_curso=?")) {
                    q.setInt(1, id);
                    q.executeUpdate();
                }
                try (PreparedStatement q =
                        db.prepareStatement(
                                "INSERT INTO curso_materia(id_curso,id_materia) VALUES(?,?)")) {
                    for (int m : materias) {
                        q.setInt(1, id);
                        q.setInt(2, m);
                        q.addBatch();
                    }
                    q.executeBatch();
                }
                db.commit();
                c.setId_curso(id);
                c.setId_materia(principal);
                // Si algo falla, deshacemos todo para conservar el curso y sus materias anteriores.
            } catch (Exception e) {
                db.rollback();
                if (e instanceof IllegalArgumentException x) throw x;
                throw new IllegalStateException("No se pudo guardar el curso.", e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el curso.", e);
        }
    }

    // Cancelar cambia el estado, pero mantiene las inscripciones y el historial.
    public void cancelar(int id) {
        try (Connection db = abrirConexion();
                PreparedStatement q =
                        db.prepareStatement(
                                "UPDATE curso SET estado='Cancelado' WHERE id_curso=?")) {
            q.setInt(1, id);
            q.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cancelar el curso.", e);
        }
    }

    // También bloqueamos el borrado acá para que no dependa solamente del botón.
    @Override
    public void eliminar(Integer id) {
        throw new IllegalStateException(
                "Los cursos no se eliminan. Podés editarlos o cancelarlos.");
    }
}

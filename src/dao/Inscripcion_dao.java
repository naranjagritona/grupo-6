package dao;

import clases.Inscripcion;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de inscripcion en la base de datos
public class Inscripcion_dao implements Dao<Inscripcion, Integer> {
    // inserta una nueva inscripcion en la base de datos
    @Override
    public void insertar(Inscripcion objeto) {
        new servicios.Inscripcion_servicio(this)
                .registrar(objeto, servicios.Auditoria_servicio.usuarioActual());
    }

    // obtiene una inscripcion especifica buscando por su id
    @Override
    public Inscripcion obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM inscripcion WHERE id_inscripcion=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de inscripcion.", error);
        }
    }

    // devuelve una lista con todas las inscripciones registradas
    @Override
    public List<Inscripcion> obtenerTodos() {
        String consulta = "SELECT * FROM inscripcion";
        List<Inscripcion> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de inscripcion.", error);
        }
        return lista;
    }

    // actualiza los datos de una inscripcion existente
    @Override
    public void modificar(Inscripcion objeto) {
        new servicios.Inscripcion_servicio(this)
                .modificar(objeto, servicios.Auditoria_servicio.usuarioActual());
    }

    // elimina una inscripcion por su id
    @Override
    public void eliminar(Integer id) {
        throw new IllegalStateException(
                "Las inscripciones se conservan con su historial. Usá la baja con motivo.");
    }

    // realiza la transaccion para cancelar la inscripcion actual y generar la nueva en el curso
    // destino
    public void cambiarCursoTransaccional(
            Integer idInscripcionActual, Integer idNuevoCurso, String motivoBaja)
            throws SQLException {
        throw new IllegalStateException(
                "Usá el servicio de inscripciones para validar y registrar el traslado.");
    }

    // busca por alumno (nombre, apellido, DNI), id de curso o estado
    public List<Inscripcion> buscarCampo(String filtro) {
        List<Inscripcion> lista = new ArrayList<>();
        String consulta =
                "SELECT DISTINCT i.*,c.año año_curso FROM inscripcion i JOIN curso c ON"
                    + " c.id_curso=i.id_curso JOIN alumno a ON i.id_alumno = a.id_alumno WHERE"
                    + " a.nombre LIKE ?    OR a.apellido LIKE ?    OR a.dni LIKE ?    OR i.estado"
                    + " LIKE ?    OR CAST(c.año AS CHAR) LIKE ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            String wildcard = "%" + filtro + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);
            ps.setString(5, wildcard);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerEntidad(rs));
                }
            }
        } catch (SQLException error) {
            System.err.println("Error al buscar inscripciones: " + error.getMessage());
        }
        return lista;
    }

    // filtra cada estado de las inscripciones
    public List<Inscripcion> obtenerPorEstado(String estado) {
        List<Inscripcion> lista = new ArrayList<>();
        String consulta =
                "SELECT i.*,c.año año_curso FROM inscripcion i JOIN curso c ON"
                        + " c.id_curso=i.id_curso WHERE i.estado = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerEntidad(rs));
                }
            }
        } catch (SQLException error) {
            System.err.println("Error al filtrar por estado: " + error.getMessage());
        }
        return lista;
    }

    // contamos las inscripciones que reservan lugar en el curso
    public int ocupacion(Connection conexion, int curso) throws SQLException {
        String consulta =
                "SELECT COUNT(*) FROM inscripcion WHERE id_curso=? AND estado IN"
                        + " ('Pendiente','Confirmada','Recuperacion')";
        try (PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, curso);
            try (ResultSet registros = ps.executeQuery()) {
                registros.next();
                return registros.getInt(1);
            }
        }
    }

    // buscamos si el alumno ya tiene otra inscripción activa en el mismo curso
    public boolean duplicada(Connection conexion, int alumno, int curso) throws SQLException {
        return !seleccionar(
                        conexion,
                        "SELECT * FROM inscripcion WHERE id_alumno=? AND id_curso=? AND estado IN"
                                + " ('Pendiente','Confirmada','Recuperacion') FOR UPDATE",
                        alumno,
                        curso)
                .isEmpty();
    }

    // cerramos la inscripción anterior sin cambiar el curso ni las notas que le corresponden
    public void estado(Connection conexion, int id, String estado, String motivo)
            throws SQLException {
        String consulta =
                "UPDATE inscripcion SET estado=?,motivo_baja=? WHERE id_inscripcion=? AND estado IN"
                        + " ('Pendiente','Confirmada','Recuperacion')";
        int filasActualizadas;
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setObject(1, estado);
            ps.setObject(2, motivo);
            ps.setObject(3, id);
            filasActualizadas = ps.executeUpdate();
        }
        if (filasActualizadas != 1) {
            throw new IllegalArgumentException(
                    "La inscripción ya fue cerrada. Actualizá el listado.");
        }
    }

    // generamos otra inscripción para el destino y dejamos el vínculo con la anterior
    public int destino(Connection conexion, int alumno, int curso, int origen) throws SQLException {
        String consulta =
                "INSERT INTO"
                    + " inscripcion(id_alumno,id_curso,fecha_inscripcion,estado,motivo_baja,id_inscripcion_origen)"
                    + " VALUES(?,?,CURRENT_DATE,'Confirmada','',?)";
        try (java.sql.PreparedStatement ps =
                conexion.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, alumno);
            ps.setObject(2, curso);
            ps.setObject(3, origen);
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // la modificación administrativa de la fecha conserva el alumno y el curso de la inscripción
    public void fecha(Connection db, int id, java.time.LocalDate fecha) throws SQLException {
        String consulta = "UPDATE inscripcion SET fecha_inscripcion=? WHERE id_inscripcion=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, fecha);
            ps.setObject(2, id);
            ps.executeUpdate();
        }
    }

    // insertamos la inscripción con el estado y la fecha que se validaron en el servicio
    public int crear(Connection conexion, Inscripcion inscripcion) throws SQLException {
        String consulta =
                "INSERT INTO inscripcion(id_alumno,id_curso,fecha_inscripcion,estado,motivo_baja)"
                        + " VALUES(?,?,?,?, '')";
        try (java.sql.PreparedStatement ps =
                conexion.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, inscripcion.getId_alumno());
            ps.setObject(2, inscripcion.getId_curso());
            ps.setObject(3, inscripcion.getFecha_inscripcion());
            ps.setObject(4, inscripcion.getEstado());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    public List<Inscripcion> delCurso(java.sql.Connection db, int curso)
            throws java.sql.SQLException {
        return seleccionar(
                db,
                "SELECT * FROM inscripcion WHERE id_curso=? ORDER BY id_inscripcion FOR UPDATE",
                curso);
    }

    public boolean perteneceUsuario(java.sql.Connection db, int curso, int usuario)
            throws java.sql.SQLException {
        return !seleccionar(
                        db,
                        "SELECT i.* FROM inscripcion i JOIN alumno a ON a.id_alumno=i.id_alumno"
                                + " WHERE i.id_curso=? AND a.id_usuario=?",
                        curso,
                        usuario)
                .isEmpty();
    }

    protected Inscripcion leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Inscripcion objeto = new Inscripcion();
        objeto.setId_inscripcion((Integer) registros.getObject("id_inscripcion"));
        objeto.setId_alumno((Integer) registros.getObject("id_alumno"));
        objeto.setId_curso((Integer) registros.getObject("id_curso"));
        objeto.setFecha_inscripcion(
                (registros.getDate("fecha_inscripcion") == null
                        ? null
                        : registros.getDate("fecha_inscripcion").toLocalDate()));
        objeto.setEstado(registros.getString("estado"));
        objeto.setMotivo_baja(registros.getString("motivo_baja"));
        objeto.setFecha_creacion(
                (registros.getTimestamp("fecha_creacion") == null
                        ? null
                        : registros.getTimestamp("fecha_creacion").toLocalDateTime()));
        objeto.setFecha_modificacion(
                (registros.getTimestamp("fecha_modificacion") == null
                        ? null
                        : registros.getTimestamp("fecha_modificacion").toLocalDateTime()));
        objeto.setId_inscripcion_origen((Integer) registros.getObject("id_inscripcion_origen"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Inscripcion
    private java.util.List<Inscripcion> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Inscripcion> lista = new java.util.ArrayList<>();
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
    public java.util.List<Inscripcion> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM inscripcion");
    }

    public Inscripcion buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM inscripcion WHERE id_inscripcion=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Inscripcion requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM inscripcion WHERE id_inscripcion=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }

    public int contar(java.sql.Connection conexion) throws java.sql.SQLException {
        String consulta = "SELECT COUNT(*) FROM inscripcion";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta);
                java.sql.ResultSet registros = ps.executeQuery()) {
            registros.next();
            return registros.getInt(1);
        }
    }
}

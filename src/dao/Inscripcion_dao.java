package dao;

import clases.Inscripcion;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de inscripcion en la base de datos
public class Inscripcion_dao extends Consulta_academica_dao implements Dao<Inscripcion, Integer> {

    // inserta una nueva inscripcion en la base de datos
    @Override
    public void insertar(Inscripcion objeto) {
        new servicios.Inscripcion_servicio(this).registrar(objeto, servicios.Auditoria_servicio.usuarioActual());
    }

    // obtiene una inscripcion especifica buscando por su id
    @Override
    public Inscripcion obtenerPorId(Integer id) {
        Inscripcion inscripcion = null;
        String consulta =
                "SELECT i.*,c.año año_curso FROM inscripcion i JOIN curso c ON"
                        + " c.id_curso=i.id_curso WHERE i.id_inscripcion = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    inscripcion = new Inscripcion();

                    inscripcion.setId_inscripcion(rs.getInt("id_inscripcion"));
                    inscripcion.setId_alumno(rs.getInt("id_alumno"));
                    inscripcion.setId_curso(rs.getInt("id_curso"));

                    Date fecha = rs.getDate("fecha_inscripcion");
                    inscripcion.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);

                    inscripcion.setEstado(rs.getString("estado"));
                    inscripcion.setMotivo_baja(rs.getString("motivo_baja"));
                    inscripcion.setAño_curso(rs.getInt("año_curso"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la inscripción por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return inscripcion;
    }

    // devuelve una lista con todas las inscripciones registradas
    @Override
    public List<Inscripcion> obtenerTodos() {
        List<Inscripcion> listaInscripciones = new ArrayList<>();
        String consulta =
                "SELECT i.*,c.año año_curso FROM inscripcion i JOIN curso c ON"
                        + " c.id_curso=i.id_curso";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Inscripcion inscripcion = new Inscripcion();

                inscripcion.setId_inscripcion(rs.getInt("id_inscripcion"));
                inscripcion.setId_alumno(rs.getInt("id_alumno"));
                inscripcion.setId_curso(rs.getInt("id_curso"));

                Date fecha = rs.getDate("fecha_inscripcion");
                inscripcion.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);

                inscripcion.setEstado(rs.getString("estado"));
                inscripcion.setMotivo_baja(rs.getString("motivo_baja"));
                inscripcion.setAño_curso(rs.getInt("año_curso"));

                listaInscripciones.add(inscripcion);
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener la lista de inscripciones: " + error.getMessage());
            error.printStackTrace();
        }

        return listaInscripciones;
    }

    // actualiza los datos de una inscripcion existente
    @Override
    public void modificar(Inscripcion objeto) {
        new servicios.Inscripcion_servicio(this).modificar(objeto, servicios.Auditoria_servicio.usuarioActual());
    }

    // elimina una inscripcion por su id
    @Override
    public void eliminar(Integer id) {
        throw new IllegalStateException("Las inscripciones se conservan con su historial. Usá la baja con motivo.");
    }

    // realiza la transaccion para cancelar la inscripcion actual y generar la nueva en el curso
    // destino
    public void cambiarCursoTransaccional(
            Integer idInscripcionActual, Integer idNuevoCurso, String motivoBaja)
            throws SQLException {
        throw new IllegalStateException("Usá el servicio de inscripciones para validar y registrar el traslado.");
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
                    Inscripcion ins = new Inscripcion();
                    ins.setId_inscripcion(rs.getInt("id_inscripcion"));
                    ins.setId_alumno(rs.getInt("id_alumno"));
                    ins.setId_curso(rs.getInt("id_curso"));
                    Date fecha = rs.getDate("fecha_inscripcion");
                    ins.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                    ins.setEstado(rs.getString("estado"));
                    ins.setMotivo_baja(rs.getString("motivo_baja"));
                    ins.setAño_curso(rs.getInt("año_curso"));
                    lista.add(ins);
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
                    Inscripcion ins = new Inscripcion();
                    ins.setId_inscripcion(rs.getInt("id_inscripcion"));
                    ins.setId_alumno(rs.getInt("id_alumno"));
                    ins.setId_curso(rs.getInt("id_curso"));
                    Date fecha = rs.getDate("fecha_inscripcion");
                    ins.setFecha_inscripcion(fecha != null ? fecha.toLocalDate() : null);
                    ins.setEstado(rs.getString("estado"));
                    ins.setMotivo_baja(rs.getString("motivo_baja"));
                    ins.setAño_curso(rs.getInt("año_curso"));
                    lista.add(ins);
                }
            }
        } catch (SQLException error) {
            System.err.println("Error al filtrar por estado: " + error.getMessage());
        }
        return lista;
    }
    // bloqueamos la inscripción y consultamos los datos que se necesitan para validar el cambio
    public java.util.Map<String, Object> bloquear(Connection conexion, int id) throws SQLException {
        return uno(conexion, "SELECT i.*,c.año,c.division,c.id_periodo,a.estado estado_alumno FROM inscripcion i JOIN curso c ON c.id_curso=i.id_curso JOIN alumno a ON a.id_alumno=i.id_alumno WHERE i.id_inscripcion=? FOR UPDATE", id);
    }

    // consultamos y bloqueamos el curso para que el cupo no cambie durante la operación
    public java.util.Map<String, Object> curso(Connection conexion, int id) throws SQLException {
        return uno(conexion, "SELECT c.*,p.fecha_inicio inicio_periodo FROM curso c JOIN periodo_lectivo p ON p.id_periodo_lectivo=c.id_periodo WHERE c.id_curso=? FOR UPDATE", id);
    }

    // contamos las inscripciones que reservan lugar en el curso
    public int ocupacion(Connection conexion, int curso) throws SQLException {
        return numero(uno(conexion, "SELECT COUNT(*) cantidad FROM inscripcion WHERE id_curso=? AND estado IN ('Pendiente','Confirmada','Recuperacion')", curso), "cantidad");
    }

    // buscamos si el alumno ya tiene otra inscripción activa en el mismo curso
    public boolean duplicada(Connection conexion, int alumno, int curso) throws SQLException {
        return !consultar(conexion, "SELECT id_inscripcion FROM inscripcion WHERE id_alumno=? AND id_curso=? AND estado IN ('Pendiente','Confirmada','Recuperacion') FOR UPDATE", alumno, curso).isEmpty();
    }

    // cerramos la inscripción anterior sin cambiar el curso ni las notas que le corresponden
    public void estado(Connection conexion, int id, String estado, String motivo) throws SQLException {
        if (ejecutar(conexion, "UPDATE inscripcion SET estado=?,motivo_baja=? WHERE id_inscripcion=? AND estado IN ('Pendiente','Confirmada','Recuperacion')", estado, motivo, id) != 1) {
            throw new IllegalArgumentException("La inscripción ya fue cerrada. Actualizá el listado.");
        }
    }

    // generamos otra inscripción para el destino y dejamos el vínculo con la anterior
    public int destino(Connection conexion, int alumno, int curso, int origen) throws SQLException {
        return insertarRegistro(conexion, "INSERT INTO inscripcion(id_alumno,id_curso,fecha_inscripcion,estado,motivo_baja,id_inscripcion_origen) VALUES(?,?,CURRENT_DATE,'Confirmada','',?)", alumno, curso, origen);
    }

    // guardamos quién cambió el estado, cuándo lo hizo y cuál fue el motivo
    public void movimiento(Connection conexion, int id, String anterior, String nuevo, String motivo, int usuario) throws SQLException {
        ejecutar(conexion, "INSERT INTO inscripcion_movimiento(id_inscripcion,estado_anterior,estado_nuevo,motivo,id_usuario) VALUES(?,?,?,?,?)", id, anterior, nuevo, motivo, usuario);
    }

    // consultamos los alumnos confirmados y en recuperación del curso que se va a promocionar
    public List<java.util.Map<String, Object>> alumnosPromocion(Connection conexion, int curso) throws SQLException {
        return consultar(conexion, "SELECT i.id_inscripcion,CONCAT(a.apellido,', ',a.nombre) alumno FROM inscripcion i JOIN alumno a ON a.id_alumno=i.id_alumno WHERE i.id_curso=? AND i.estado IN ('Confirmada','Recuperacion') ORDER BY i.id_inscripcion FOR UPDATE", curso);
    }

    // consultamos todas las materias del curso, incluso las que todavía no tienen resultado
    public List<java.util.Map<String, Object>> resultados(Connection conexion, int inscripcion) throws SQLException {
        return consultar(conexion, "SELECT m.id_materia,m.nombre_materia,r.promedio,r.resultado FROM inscripcion i JOIN curso_materia cm ON cm.id_curso=i.id_curso JOIN materia m ON m.id_materia=cm.id_materia LEFT JOIN resultado_materia r ON r.id_inscripcion=i.id_inscripcion AND r.id_materia=cm.id_materia WHERE i.id_inscripcion=? ORDER BY m.nombre_materia", inscripcion);
    }

    // buscamos la inscripción con el alumno, el curso, el período y el vínculo con su origen
    public java.util.Map<String, Object> ficha(Connection conexion, int id) throws SQLException {
        return uno(conexion, "SELECT i.*,a.nombre,a.apellido,a.dni,a.matricula,c.año,c.division,c.turno,p.nombre periodo FROM inscripcion i JOIN alumno a ON a.id_alumno=i.id_alumno JOIN curso c ON c.id_curso=i.id_curso JOIN periodo_lectivo p ON p.id_periodo_lectivo=c.id_periodo WHERE i.id_inscripcion=?", id);
    }

    // buscamos las notas de la inscripción sin trasladarlas a otro curso
    public List<java.util.Map<String, Object>> notas(Connection conexion, int id) throws SQLException {
        return consultar(conexion, "SELECT m.nombre_materia materia,n.titulo,n.tipo,n.fecha_evaluacion,c.nota,c.observaciones FROM calificacion c JOIN columna_notas n ON n.id_columna_notas=c.id_columna_notas JOIN materia m ON m.id_materia=n.id_materia WHERE c.id_inscripcion=? ORDER BY m.nombre_materia,n.fecha_evaluacion", id);
    }

    // consultamos los movimientos anteriores para mostrarlos en la ficha
    public List<java.util.Map<String, Object>> movimientos(Connection conexion, int id) throws SQLException {
        return consultar(conexion, "SELECT m.estado_anterior,m.estado_nuevo,m.motivo,u.nombre_usuario usuario,m.fecha FROM inscripcion_movimiento m JOIN usuario u ON u.id_usuario=m.id_usuario WHERE m.id_inscripcion=? ORDER BY m.id_movimiento", id);
    }

    // guardamos las condiciones académicas que define dirección para el curso
    public void regla(Connection conexion, int curso, double materia, double promocion, String recuperatorio) throws SQLException {
        ejecutar(conexion, "UPDATE curso SET nota_aprobacion_materia=?,promedio_promocion=?,criterio_recuperatorio=? WHERE id_curso=?", materia, promocion, recuperatorio, curso);
    }
    // buscamos y bloqueamos al alumno antes de registrar su inscripción
    public java.util.Map<String, Object> alumno(Connection conexion, int id) throws SQLException {
        return uno(conexion, "SELECT * FROM alumno WHERE id_alumno=? FOR UPDATE", id);
    }

    // insertamos la inscripción con el estado y la fecha que se validaron en el servicio
    public int crear(Connection conexion, Inscripcion inscripcion) throws SQLException {
        return insertarRegistro(conexion, "INSERT INTO inscripcion(id_alumno,id_curso,fecha_inscripcion,estado,motivo_baja) VALUES(?,?,?,?, '')", inscripcion.getId_alumno(), inscripcion.getId_curso(), inscripcion.getFecha_inscripcion(), inscripcion.getEstado());
    }
}

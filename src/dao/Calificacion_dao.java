package dao;

import clases.Calificacion;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de calificacion en la base de datos
public class Calificacion_dao implements Dao<Calificacion, Integer> {
    public void insertarVerificando(Calificacion objeto) throws SQLException {
        try {
            new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual())
                    .registrarNota(
                            objeto.getId_inscripcion(),
                            objeto.getId_columna_notas(),
                            String.valueOf(objeto.getNota()),
                            objeto.getObservaciones() == null ? "" : objeto.getObservaciones());
        } catch (Exception error) {
            throw new SQLException(error.getMessage(), error);
        }
    }

    /** Actualiza la nota y registra su auditoría en una única transacción. */
    public void modificarConHistorial(Calificacion objeto, Double notaAnterior, String motivo)
            throws SQLException {
        try {
            new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual())
                    .modificarNota(
                            objeto.getId_calificacion(), String.valueOf(objeto.getNota()), motivo);
        } catch (Exception error) {
            throw new SQLException(error.getMessage(), error);
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Calificacion objeto) {
        try {
            insertarVerificando(objeto);
        } catch (SQLException error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Calificacion obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM calificacion WHERE id_calificacion=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de calificacion.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Calificacion> obtenerTodos() {
        String consulta = "SELECT * FROM calificacion";
        List<Calificacion> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de calificacion.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Calificacion objeto) {
        try {
            new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual())
                    .modificarNota(
                            objeto.getId_calificacion(),
                            String.valueOf(objeto.getNota()),
                            objeto.getObservaciones());
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        try {
            new servicios.Calificaciones_servicio(servicios.Permisos_servicio.actual())
                    .eliminarNota(id);
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    public List<clases.Calificacion> deInscripcion(java.sql.Connection db, int id)
            throws java.sql.SQLException {
        return seleccionar(db, "SELECT * FROM calificacion WHERE id_inscripcion=?", id);
    }

    public void crear(java.sql.Connection db, clases.Calificacion c) throws java.sql.SQLException {
        String consulta =
                "INSERT INTO calificacion(id_inscripcion,id_columna_notas,nota,observaciones)"
                        + " VALUES(?,?,?,?)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, c.getId_inscripcion());
            ps.setObject(2, c.getId_columna_notas());
            ps.setObject(3, c.getNota());
            ps.setObject(4, c.getObservaciones());
            ps.executeUpdate();
        }
    }

    public void corregir(java.sql.Connection db, clases.Calificacion c)
            throws java.sql.SQLException {
        String consulta =
                "UPDATE calificacion SET nota=?,fecha_carga=NOW(),observaciones=? WHERE"
                        + " id_calificacion=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, c.getNota());
            ps.setObject(2, c.getObservaciones());
            ps.setObject(3, c.getId_calificacion());
            ps.executeUpdate();
        }
    }

    public void quitar(java.sql.Connection db, int id) throws java.sql.SQLException {
        String consulta = "DELETE FROM calificacion WHERE id_calificacion=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existeEnMateria(java.sql.Connection db, int materia)
            throws java.sql.SQLException {
        return !seleccionar(
                        db,
                        "SELECT n.* FROM calificacion n JOIN columna_notas cn ON"
                            + " cn.id_columna_notas=n.id_columna_notas WHERE cn.id_materia=? LIMIT"
                            + " 1",
                        materia)
                .isEmpty();
    }

    protected Calificacion leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Calificacion objeto = new Calificacion();
        objeto.setId_calificacion((Integer) registros.getObject("id_calificacion"));
        objeto.setId_inscripcion((Integer) registros.getObject("id_inscripcion"));
        objeto.setId_columna_notas((Integer) registros.getObject("id_columna_notas"));
        objeto.setNota((registros.getObject("nota") == null ? null : registros.getDouble("nota")));
        objeto.setFecha_carga(
                (registros.getTimestamp("fecha_carga") == null
                        ? null
                        : registros.getTimestamp("fecha_carga").toLocalDateTime()));
        objeto.setObservaciones(registros.getString("observaciones"));
        return objeto;
    }

    public java.util.List<clases.Calificacion> delCurso(java.sql.Connection db, int curso)
            throws java.sql.SQLException {
        return seleccionar(
                db,
                "SELECT n.* FROM calificacion n JOIN inscripcion i ON"
                        + " i.id_inscripcion=n.id_inscripcion WHERE i.id_curso=?",
                curso);
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Calificacion
    private java.util.List<Calificacion> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Calificacion> lista = new java.util.ArrayList<>();
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
    public java.util.List<Calificacion> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM calificacion");
    }

    public Calificacion buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM calificacion WHERE id_calificacion=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Calificacion requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM calificacion WHERE id_calificacion=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

package dao;

import clases.Columna_Notas;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de columna notas en la base de datos
public class Columna_Notas_dao implements Dao<Columna_Notas, Integer> {
    public void insertarVerificando(Columna_Notas objeto) throws SQLException {
        String sql =
                "INSERT INTO columna_notas (id_curso, titulo, tipo, nota_maxima, nota_aprobacion,"
                    + " fecha_evaluacion, fecha_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = Conexion.getConnection()) {
            if (conexion == null)
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, objeto.getId_curso());
                ps.setString(2, objeto.getTitulo());
                ps.setString(3, objeto.getTipo());
                ps.setDouble(4, objeto.getNota_maxima());
                ps.setDouble(5, objeto.getNota_aprobacion());
                ps.setObject(6, objeto.getFecha_evaluacion());
                ps.setObject(7, objeto.getFecha_cierre());
                ps.setString(8, objeto.getEstado());
                ps.executeUpdate();
            }
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Columna_Notas objeto) {
        String consulta =
                "INSERT INTO columna_notas (id_curso, titulo, tipo, nota_maxima, nota_aprobacion,"
                    + " fecha_evaluacion, fecha_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_curso());
            ps.setString(2, objeto.getTitulo());
            ps.setString(3, objeto.getTipo());
            ps.setObject(4, objeto.getNota_maxima());
            ps.setObject(5, objeto.getNota_aprobacion());
            ps.setObject(6, objeto.getFecha_evaluacion());
            ps.setObject(7, objeto.getFecha_cierre());
            ps.setString(8, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la columna de notas: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Columna_Notas obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM columna_notas WHERE id_columna_notas=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de columna_notas.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Columna_Notas> obtenerTodos() {
        String consulta = "SELECT * FROM columna_notas";
        List<Columna_Notas> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de columna_notas.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Columna_Notas objeto) {
        String consulta =
                "UPDATE columna_notas SET id_curso = ?, titulo = ?, tipo = ?, nota_maxima = ?,"
                        + " nota_aprobacion = ?, fecha_evaluacion = ?, fecha_cierre = ?, estado = ?"
                        + " WHERE id_columna_notas = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_curso());
            ps.setString(2, objeto.getTitulo());
            ps.setString(3, objeto.getTipo());
            ps.setObject(4, objeto.getNota_maxima());
            ps.setObject(5, objeto.getNota_aprobacion());
            ps.setObject(6, objeto.getFecha_evaluacion());
            ps.setObject(7, objeto.getFecha_cierre());
            ps.setString(8, objeto.getEstado());
            ps.setInt(9, objeto.getId_columna_notas());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la columna de notas: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM columna_notas WHERE id_columna_notas = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la columna de notas: " + error.getMessage());
            error.printStackTrace();
        }
    }

    public List<clases.Columna_Notas> delCurso(java.sql.Connection db, int curso)
            throws java.sql.SQLException {
        return seleccionar(
                db,
                "SELECT * FROM columna_notas WHERE id_curso=? ORDER BY id_columna_notas",
                curso);
    }

    public void crearEvaluacion(java.sql.Connection db, clases.Columna_Notas c)
            throws java.sql.SQLException {
        String consulta =
                "INSERT INTO"
                    + " columna_notas(id_curso,titulo,tipo,nota_maxima,nota_aprobacion,fecha_evaluacion,fecha_cierre,estado,id_materia)"
                    + " VALUES(?,?,?,?,?,?,?,?,?)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, c.getId_curso());
            ps.setObject(2, c.getTitulo());
            ps.setObject(3, c.getTipo());
            ps.setObject(4, c.getNota_maxima());
            ps.setObject(5, c.getNota_aprobacion());
            ps.setObject(6, c.getFecha_evaluacion());
            ps.setObject(7, c.getFecha_cierre());
            ps.setObject(8, c.getEstado());
            ps.setObject(9, c.getId_materia());
            ps.executeUpdate();
        }
    }

    public void cerrar(java.sql.Connection db, int id) throws java.sql.SQLException {
        String consulta =
                "UPDATE columna_notas SET estado='Cerrada',fecha_cierre=CURRENT_DATE WHERE"
                        + " id_columna_notas=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existeEnAnio(java.sql.Connection db, int anio, int materia)
            throws java.sql.SQLException {
        return !seleccionar(
                        db,
                        "SELECT cn.* FROM columna_notas cn JOIN curso c ON c.id_curso=cn.id_curso"
                                + " WHERE CAST(c.año AS UNSIGNED)=? AND cn.id_materia=? LIMIT 1",
                        anio,
                        materia)
                .isEmpty();
    }

    protected Columna_Notas leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Columna_Notas objeto = new Columna_Notas();
        objeto.setId_columna_notas((Integer) registros.getObject("id_columna_notas"));
        objeto.setId_curso((Integer) registros.getObject("id_curso"));
        objeto.setTitulo(registros.getString("titulo"));
        objeto.setTipo(registros.getString("tipo"));
        objeto.setNota_maxima(
                (registros.getObject("nota_maxima") == null
                        ? null
                        : registros.getDouble("nota_maxima")));
        objeto.setNota_aprobacion(
                (registros.getObject("nota_aprobacion") == null
                        ? null
                        : registros.getDouble("nota_aprobacion")));
        objeto.setFecha_evaluacion(
                (registros.getDate("fecha_evaluacion") == null
                        ? null
                        : registros.getDate("fecha_evaluacion").toLocalDate()));
        objeto.setFecha_cierre(
                (registros.getDate("fecha_cierre") == null
                        ? null
                        : registros.getDate("fecha_cierre").toLocalDate()));
        objeto.setEstado(registros.getString("estado"));
        objeto.setId_materia((Integer) registros.getObject("id_materia"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Columna_Notas
    private java.util.List<Columna_Notas> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Columna_Notas> lista = new java.util.ArrayList<>();
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
    public java.util.List<Columna_Notas> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM columna_notas");
    }

    public Columna_Notas buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(conexion, "SELECT * FROM columna_notas WHERE id_columna_notas=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Columna_Notas requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM columna_notas WHERE id_columna_notas=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

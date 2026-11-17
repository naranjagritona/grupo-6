package dao;

import clases.Periodo_Lectivo;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de periodo lectivo en la base de datos
public class Periodo_Lectivo_dao implements Dao<Periodo_Lectivo, Integer> {
    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Periodo_Lectivo objeto) {
        String consulta =
                "INSERT INTO periodo_lectivo (nombre, tipo, fecha_inicio, fecha_fin, estado) VALUES"
                        + " (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getTipo());
            ps.setObject(3, objeto.getFecha_inicio());
            ps.setObject(4, objeto.getFecha_fin());
            ps.setString(5, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Periodo_Lectivo obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM periodo_lectivo WHERE id_periodo_lectivo=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de periodo_lectivo.", error);
        }
    }

    @Override
    public List<Periodo_Lectivo> obtenerTodos() {
        String consulta = "SELECT * FROM periodo_lectivo";
        List<Periodo_Lectivo> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de periodo_lectivo.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Periodo_Lectivo objeto) {
        String consulta =
                "UPDATE periodo_lectivo SET nombre = ?, tipo = ?, fecha_inicio = ?, fecha_fin = ?,"
                        + " estado = ? WHERE id_periodo_lectivo = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getTipo());
            ps.setObject(3, objeto.getFecha_inicio());
            ps.setObject(4, objeto.getFecha_fin());
            ps.setString(5, objeto.getEstado());
            ps.setInt(6, objeto.getId_periodo_lectivo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM periodo_lectivo WHERE id_periodo_lectivo = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    protected Periodo_Lectivo leerEntidad(java.sql.ResultSet registros)
            throws java.sql.SQLException {
        Periodo_Lectivo objeto = new Periodo_Lectivo();
        objeto.setId_periodo_lectivo((Integer) registros.getObject("id_periodo_lectivo"));
        objeto.setNombre(registros.getString("nombre"));
        objeto.setTipo(registros.getString("tipo"));
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

    public void crear(java.sql.Connection db, Periodo_Lectivo periodo)
            throws java.sql.SQLException {
        String consulta =
                "INSERT INTO periodo_lectivo(nombre,tipo,fecha_inicio,fecha_fin,estado)"
                        + " VALUES(?,?,?,?,?)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, periodo.getNombre());
            ps.setObject(2, periodo.getTipo());
            ps.setObject(3, periodo.getFecha_inicio());
            ps.setObject(4, periodo.getFecha_fin());
            ps.setObject(5, periodo.getEstado());
            ps.executeUpdate();
        }
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Periodo_Lectivo
    private java.util.List<Periodo_Lectivo> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Periodo_Lectivo> lista = new java.util.ArrayList<>();
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
    public java.util.List<Periodo_Lectivo> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM periodo_lectivo");
    }

    public Periodo_Lectivo buscar(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion, "SELECT * FROM periodo_lectivo WHERE id_periodo_lectivo=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Periodo_Lectivo requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM periodo_lectivo WHERE id_periodo_lectivo=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

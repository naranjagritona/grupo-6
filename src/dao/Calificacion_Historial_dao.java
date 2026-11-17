package dao;

import clases.Calificacion_Historial;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de calificacion historial en la base de datos
public class Calificacion_Historial_dao implements Dao<Calificacion_Historial, Integer> {
    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Calificacion_Historial objeto) {
        String consulta =
                "INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva,"
                        + " motivo) VALUES (?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_calificacion());
            ps.setObject(2, objeto.getNota_anterior());
            ps.setObject(3, objeto.getNota_nueva());
            ps.setString(4, objeto.getMotivo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println(
                    "Error al insertar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Calificacion_Historial obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM calificacion_historial WHERE id_calificacion_historial=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de calificacion_historial.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Calificacion_Historial> obtenerTodos() {
        String consulta = "SELECT * FROM calificacion_historial";
        List<Calificacion_Historial> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de calificacion_historial.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Calificacion_Historial objeto) {
        String consulta =
                "UPDATE calificacion_historial SET id_calificacion = ?, nota_anterior = ?,"
                        + " nota_nueva = ?, motivo = ? WHERE id_calificacion_historial = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, objeto.getId_calificacion());
            ps.setObject(2, objeto.getNota_anterior());
            ps.setObject(3, objeto.getNota_nueva());
            ps.setString(4, objeto.getMotivo());
            ps.setInt(5, objeto.getId_calificacion_historial());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println(
                    "Error al modificar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM calificacion_historial WHERE id_calificacion_historial = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println(
                    "Error al eliminar el historial de calificación: " + error.getMessage());
            error.printStackTrace();
        }
    }

    public List<clases.Calificacion_Historial> deCalificacion(java.sql.Connection db, int id)
            throws java.sql.SQLException {
        return seleccionar(
                db,
                "SELECT * FROM calificacion_historial WHERE id_calificacion=? ORDER BY"
                        + " fecha_modificacion DESC,id_calificacion_historial DESC",
                id);
    }

    public void crear(java.sql.Connection db, clases.Calificacion_Historial h)
            throws java.sql.SQLException {
        String consulta =
                "INSERT INTO"
                        + " calificacion_historial(id_calificacion,nota_anterior,nota_nueva,motivo)"
                        + " VALUES(?,?,?,?)";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, h.getId_calificacion());
            ps.setObject(2, h.getNota_anterior());
            ps.setObject(3, h.getNota_nueva());
            ps.setObject(4, h.getMotivo());
            ps.executeUpdate();
        }
    }

    protected Calificacion_Historial leerEntidad(java.sql.ResultSet registros)
            throws java.sql.SQLException {
        Calificacion_Historial objeto = new Calificacion_Historial();
        objeto.setId_calificacion_historial(
                (Integer) registros.getObject("id_calificacion_historial"));
        objeto.setId_calificacion((Integer) registros.getObject("id_calificacion"));
        objeto.setNota_anterior(
                (registros.getObject("nota_anterior") == null
                        ? null
                        : registros.getDouble("nota_anterior")));
        objeto.setNota_nueva(
                (registros.getObject("nota_nueva") == null
                        ? null
                        : registros.getDouble("nota_nueva")));
        objeto.setFecha_modificacion(
                (registros.getTimestamp("fecha_modificacion") == null
                        ? null
                        : registros.getTimestamp("fecha_modificacion").toLocalDateTime()));
        objeto.setMotivo(registros.getString("motivo"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Calificacion_Historial
    private java.util.List<Calificacion_Historial> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Calificacion_Historial> lista = new java.util.ArrayList<>();
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
    public java.util.List<Calificacion_Historial> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM calificacion_historial");
    }

    public Calificacion_Historial buscar(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM calificacion_historial WHERE id_calificacion_historial=?",
                        id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Calificacion_Historial requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM calificacion_historial WHERE id_calificacion_historial=? FOR"
                                + " UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

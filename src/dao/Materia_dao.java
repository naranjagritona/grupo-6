package dao;

import clases.Materia;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de materia en la base de datos
public class Materia_dao implements Dao<Materia, Integer> {
    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Materia objeto) {
        if (!servicios.Permisos_servicio.directivo(servicios.Permisos_servicio.actual()))
            throw new IllegalArgumentException("Esta operación corresponde a Dirección.");
        String consulta = "INSERT INTO materia (nombre_materia) VALUES (?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre_materia());
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Materia obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM materia WHERE id_materia=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro de materia.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Materia> obtenerTodos() {
        String consulta = "SELECT * FROM materia";
        List<Materia> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado de materia.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Materia objeto) {
        if (!servicios.Permisos_servicio.directivo(servicios.Permisos_servicio.actual()))
            throw new IllegalArgumentException("Esta operación corresponde a Dirección.");
        String consulta = "UPDATE materia SET nombre_materia = ? WHERE id_materia = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre_materia());
            ps.setInt(2, objeto.getId_materia());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        if (!servicios.Permisos_servicio.directivo(servicios.Permisos_servicio.actual()))
            throw new IllegalArgumentException("Esta operación corresponde a Dirección.");
        try (Connection db = Conexion.getConnection();
                PreparedStatement q =
                        db.prepareStatement(
                                "SELECT anio FROM materia_anio WHERE id_materia=? ORDER BY anio")) {
            q.setInt(1, id);
            try (ResultSet rs = q.executeQuery()) {
                if (rs.next())
                    throw new IllegalArgumentException(
                            "La materia pertenece al plan de "
                                    + rs.getInt(1)
                                    + "° año y no se puede eliminar.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar el plan de la materia.", e);
        }
        String consulta = "DELETE FROM materia WHERE id_materia = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar la materia: " + error.getMessage());
            error.printStackTrace();
        }
    }

    public clases.Materia porNombre(java.sql.Connection db, String nombre)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        db,
                        "SELECT * FROM materia WHERE LOWER(TRIM(nombre_materia))=LOWER(?)",
                        nombre);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    public int crear(java.sql.Connection db, clases.Materia m) throws java.sql.SQLException {
        String consulta = "INSERT INTO materia(nombre_materia) VALUES(?)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, m.getNombre_materia());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    protected Materia leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Materia objeto = new Materia();
        objeto.setId_materia((Integer) registros.getObject("id_materia"));
        objeto.setNombre_materia(registros.getString("nombre_materia"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Materia
    private java.util.List<Materia> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Materia> lista = new java.util.ArrayList<>();
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
    public java.util.List<Materia> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM materia");
    }

    public Materia buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM materia WHERE id_materia=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Materia requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(conexion, "SELECT * FROM materia WHERE id_materia=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

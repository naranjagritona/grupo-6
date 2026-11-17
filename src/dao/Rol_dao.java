package dao;

import clases.Rol;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de rol en la base de datos
public class Rol_dao implements Dao<Rol, Integer> {
    // consultamos los roles disponibles con la conexión de la operación administrativa
    public List<Rol> listar(Connection db) throws SQLException {
        return seleccionar(db, "SELECT * FROM rol ORDER BY id_rol");
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Rol objeto) {
        String consulta = "INSERT INTO rol (nombre, descripcion_rol) VALUES (?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getDescripcion_rol());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el rol: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Rol obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM rol WHERE id_rol=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro de rol.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Rol> obtenerTodos() {
        String consulta = "SELECT * FROM rol";
        List<Rol> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado de rol.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Rol objeto) {
        String consulta = "UPDATE rol SET nombre = ?, descripcion_rol = ? WHERE id_rol = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getDescripcion_rol());
            ps.setInt(3, objeto.getId_rol());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el rol: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM rol WHERE id_rol = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el rol: " + error.getMessage());
            error.printStackTrace();
        }
    }

    protected Rol leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Rol objeto = new Rol();
        objeto.setId_rol((Integer) registros.getObject("id_rol"));
        objeto.setNombre(registros.getString("nombre"));
        objeto.setDescripcion_rol(registros.getString("descripcion_rol"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Rol
    private java.util.List<Rol> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Rol> lista = new java.util.ArrayList<>();
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
    public java.util.List<Rol> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM rol");
    }

    public Rol buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM rol WHERE id_rol=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Rol requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM rol WHERE id_rol=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

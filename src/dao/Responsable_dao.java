package dao;

import clases.Responsable;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de responsable en la base de datos
public class Responsable_dao implements Dao<Responsable, Integer> {
    // guardamos el responsable usando la conexión del alta de la cuenta del alumno
    public int crear(java.sql.Connection db, Responsable r) throws java.sql.SQLException {
        String consulta =
                "INSERT INTO responsable(nombre,apellido,dni,tipo_relacion) VALUES(?,?,?,?)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, r.getNombre());
            ps.setObject(2, r.getApellido());
            ps.setObject(3, r.getDni());
            ps.setObject(4, r.getTipo_relacion());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // modificamos los datos del responsable sin cambiar el vínculo con la inscripción del alumno
    public void actualizarAdministracion(java.sql.Connection db, Responsable r)
            throws java.sql.SQLException {
        String consulta =
                "UPDATE responsable SET nombre=?,apellido=?,dni=?,tipo_relacion=? WHERE"
                        + " id_responsable=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, r.getNombre());
            ps.setObject(2, r.getApellido());
            ps.setObject(3, r.getDni());
            ps.setObject(4, r.getTipo_relacion());
            ps.setObject(5, r.getId_responsable());
            ps.executeUpdate();
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Responsable objeto) {
        String consulta =
                "INSERT INTO responsable (nombre, apellido, dni, tipo_relacion) VALUES (?, ?, ?,"
                        + " ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getApellido());
            ps.setString(3, objeto.getDni());
            ps.setString(4, objeto.getTipo_relacion());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el responsable: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Responsable obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM responsable WHERE id_responsable=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de responsable.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Responsable> obtenerTodos() {
        String consulta = "SELECT * FROM responsable";
        List<Responsable> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de responsable.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Responsable objeto) {
        String consulta =
                "UPDATE responsable SET nombre = ?, apellido = ?, dni = ?, tipo_relacion = ? WHERE"
                        + " id_responsable = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getApellido());
            ps.setString(3, objeto.getDni());
            ps.setString(4, objeto.getTipo_relacion());
            ps.setInt(5, objeto.getId_responsable());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el responsable: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM responsable WHERE id_responsable = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el responsable: " + error.getMessage());
            error.printStackTrace();
        }
    }

    protected Responsable leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Responsable objeto = new Responsable();
        objeto.setId_responsable((Integer) registros.getObject("id_responsable"));
        objeto.setNombre(registros.getString("nombre"));
        objeto.setApellido(registros.getString("apellido"));
        objeto.setDni(registros.getString("dni"));
        objeto.setTipo_relacion(registros.getString("tipo_relacion"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Responsable
    private java.util.List<Responsable> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Responsable> lista = new java.util.ArrayList<>();
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
    public java.util.List<Responsable> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM responsable");
    }

    public Responsable buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM responsable WHERE id_responsable=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Responsable requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM responsable WHERE id_responsable=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

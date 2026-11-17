package dao;

import clases.Docente;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de docente en la base de datos
public class Docente_dao implements Dao<Docente, Integer> {
    // registramos el docente junto con su cuenta y el legajo generado por el servicio
    public int crear(java.sql.Connection db, Docente d) throws java.sql.SQLException {
        String consulta =
                "INSERT INTO docente(id_usuario,nombre,apellido,dni,legajo,estado)"
                        + " VALUES(?,?,?,?,?,?)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, d.getId_usuario());
            ps.setObject(2, d.getNombre());
            ps.setObject(3, d.getApellido());
            ps.setObject(4, d.getDni());
            ps.setObject(5, d.getLegajo());
            ps.setObject(6, d.getEstado());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // actualizamos los datos personales sin aceptar un legajo o una cuenta diferentes
    public void actualizarAdministracion(java.sql.Connection db, Docente d)
            throws java.sql.SQLException {
        String consulta =
                "UPDATE docente SET nombre=?,apellido=?,dni=?,estado=? WHERE id_docente=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, d.getNombre());
            ps.setObject(2, d.getApellido());
            ps.setObject(3, d.getDni());
            ps.setObject(4, d.getEstado());
            ps.setObject(5, d.getId_docente());
            ps.executeUpdate();
        }
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Docente objeto) {
        String consulta =
                "INSERT INTO docente (id_usuario, nombre, apellido, dni, legajo, estado) VALUES (?,"
                        + " ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setObject(1, objeto.getId_usuario());
            ps.setString(2, objeto.getNombre());
            ps.setString(3, objeto.getApellido());
            ps.setString(4, objeto.getDni());
            ps.setString(5, objeto.getLegajo());
            ps.setString(6, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Docente obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM docente WHERE id_docente=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro de docente.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Docente> obtenerTodos() {
        String consulta = "SELECT * FROM docente";
        List<Docente> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado de docente.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Docente objeto) {
        String consulta =
                "UPDATE docente SET id_usuario = ?, nombre = ?, apellido = ?, dni = ?, legajo = ?,"
                        + " estado = ? WHERE id_docente = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setObject(1, objeto.getId_usuario());
            ps.setString(2, objeto.getNombre());
            ps.setString(3, objeto.getApellido());
            ps.setString(4, objeto.getDni());
            ps.setString(5, objeto.getLegajo());
            ps.setString(6, objeto.getEstado());
            ps.setInt(7, objeto.getId_docente());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM docente WHERE id_docente = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el docente: " + error.getMessage());
            error.printStackTrace();
        }
    }

    public clases.Docente porUsuario(java.sql.Connection db, int usuario)
            throws java.sql.SQLException {
        var filas = seleccionar(db, "SELECT * FROM docente WHERE id_usuario=? FOR UPDATE", usuario);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    public void inactivar(java.sql.Connection db, int usuario) throws java.sql.SQLException {
        String consulta = "UPDATE docente SET estado='Inactivo' WHERE id_usuario=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, usuario);
            ps.executeUpdate();
        }
    }

    protected Docente leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Docente objeto = new Docente();
        objeto.setId_docente((Integer) registros.getObject("id_docente"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setNombre(registros.getString("nombre"));
        objeto.setApellido(registros.getString("apellido"));
        objeto.setDni(registros.getString("dni"));
        objeto.setLegajo(registros.getString("legajo"));
        objeto.setEstado(registros.getString("estado"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Docente
    private java.util.List<Docente> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Docente> lista = new java.util.ArrayList<>();
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
    public java.util.List<Docente> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM docente");
    }

    public Docente buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM docente WHERE id_docente=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Docente requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(conexion, "SELECT * FROM docente WHERE id_docente=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }

    public int contar(java.sql.Connection conexion) throws java.sql.SQLException {
        String consulta = "SELECT COUNT(*) FROM docente";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta);
                java.sql.ResultSet registros = ps.executeQuery()) {
            registros.next();
            return registros.getInt(1);
        }
    }
}

package dao;

import clases.Secretaria;

import conexion.Conexion;

import java.sql.*;
import java.util.*;

public class Secretaria_dao implements Dao<Secretaria, Integer> {
    public Secretaria porUsuario(Connection db, int usuario) throws SQLException {
        var filas =
                seleccionar(db, "SELECT * FROM secretaria WHERE id_usuario=? FOR UPDATE", usuario);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    public int crear(Connection db, Secretaria p) throws SQLException {
        String consulta =
                "INSERT INTO secretaria(id_usuario,nombre,apellido,dni,mail,legajo,estado)"
                        + " VALUES(?,?,?,?,?,?,?)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, p.getId_usuario());
            ps.setObject(2, p.getNombre());
            ps.setObject(3, p.getApellido());
            ps.setObject(4, p.getDni());
            ps.setObject(5, p.getMail());
            ps.setObject(6, p.getLegajo());
            ps.setObject(7, p.getEstado());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    public void actualizar(Connection db, Secretaria p) throws SQLException {
        String consulta =
                "UPDATE secretaria SET nombre=?,apellido=?,dni=?,mail=?,estado=? WHERE"
                        + " id_secretaria=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, p.getNombre());
            ps.setObject(2, p.getApellido());
            ps.setObject(3, p.getDni());
            ps.setObject(4, p.getMail());
            ps.setObject(5, p.getEstado());
            ps.setObject(6, p.getId_secretaria());
            ps.executeUpdate();
        }
    }

    public void inactivar(Connection db, int usuario) throws SQLException {
        String consulta = "UPDATE secretaria SET estado='Inactivo' WHERE id_usuario=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, usuario);
            ps.executeUpdate();
        }
    }

    protected Secretaria leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Secretaria objeto = new Secretaria();
        objeto.setId_secretaria((Integer) registros.getObject("id_secretaria"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setNombre(registros.getString("nombre"));
        objeto.setApellido(registros.getString("apellido"));
        objeto.setDni(registros.getString("dni"));
        objeto.setMail(registros.getString("mail"));
        objeto.setLegajo(registros.getString("legajo"));
        objeto.setEstado(registros.getString("estado"));
        return objeto;
    }

    @Override
    public Secretaria obtenerPorId(Integer id) {
        try (Connection conexion = Conexion.getConnection()) {
            return buscar(conexion, id);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro.", error);
        }
    }

    @Override
    public List<Secretaria> obtenerTodos() {
        try (Connection conexion = Conexion.getConnection()) {
            return listarTabla(conexion);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado.", error);
        }
    }

    @Override
    public void insertar(Secretaria objeto) {
        try {
            objeto.setId_secretaria(Conexion.transaccion(conexion -> crear(conexion, objeto)));
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo guardar el perfil.", error);
        }
    }

    @Override
    public void modificar(Secretaria objeto) {
        try {
            Conexion.transaccion(
                    conexion -> {
                        actualizar(conexion, objeto);
                        return null;
                    });
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo modificar el perfil.", error);
        }
    }

    @Override
    public void eliminar(Integer id) {
        throw new UnsupportedOperationException(
                "Los perfiles se dan de baja cambiando su estado para conservar el historial.");
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Secretaria
    private java.util.List<Secretaria> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Secretaria> lista = new java.util.ArrayList<>();
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
    public java.util.List<Secretaria> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM secretaria");
    }

    public Secretaria buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM secretaria WHERE id_secretaria=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Secretaria requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion, "SELECT * FROM secretaria WHERE id_secretaria=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

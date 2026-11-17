package dao;

import clases.Notificacion_Correo;
import clases.Rol;

import conexion.Conexion;

import servicios.Auditoria_servicio;
import servicios.Permisos_servicio;

import java.sql.*;
import java.util.*;

// persistimos los avisos en la cola compartida con el servicio de correo de la web
public class Notificacion_Correo_dao implements Dao<Notificacion_Correo, Integer> {
    // el evento es único para no generar el mismo aviso dos veces al repetir una operación
    public int agregar(Connection db, Notificacion_Correo n) throws SQLException {
        String consulta =
                "INSERT INTO"
                    + " notificacion_correo(id_usuario,destinatario,tipo,clave_evento,asunto,mensaje)"
                    + " VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE"
                    + " id_notificacion=LAST_INSERT_ID(id_notificacion)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, n.getId_usuario());
            ps.setObject(2, n.getDestinatario());
            ps.setObject(3, n.getTipo());
            ps.setObject(4, n.getClave_evento());
            ps.setObject(5, n.getAsunto());
            ps.setObject(6, n.getMensaje());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // buscamos los avisos de esta sesión que todavía no pudieron enviarse
    public List<Integer> pendientesDelActor(int actor) throws Exception {
        return Conexion.transaccion(
                db -> {
                    Permisos_servicio.verificar(db, actor, Rol.DIRECTIVO, Rol.SECRETARIA);
                    List<Integer> ids = new ArrayList<>();
                    String consulta =
                            "SELECT * FROM notificacion_correo WHERE clave_evento LIKE ? AND estado"
                                    + " IN ('Pendiente','Error') ORDER BY id_notificacion";
                    for (Notificacion_Correo aviso :
                            seleccionar(db, consulta, "java-" + actor + "-%")) {
                        ids.add(aviso.getId_notificacion());
                    }
                    return ids;
                });
    }

    @Override
    public void insertar(Notificacion_Correo aviso) {
        try {
            aviso.setId_notificacion(Conexion.transaccion(db -> agregar(db, aviso)));
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo guardar el aviso.", error);
        }
    }

    @Override
    public Notificacion_Correo obtenerPorId(Integer id) {
        try {
            return Conexion.transaccion(
                    db -> {
                        Integer actor = Auditoria_servicio.usuarioActual();
                        Permisos_servicio.verificar(db, actor, Rol.DIRECTIVO, Rol.SECRETARIA);
                        Notificacion_Correo aviso = buscar(db, id);
                        if (aviso == null) return null;
                        // dirección ve todos, secretaría solamente los avisos que generó su cuenta
                        if (new Usuario_dao().rolActivo(db, actor) != Rol.DIRECTIVO
                                && !aviso.getClave_evento().startsWith("java-" + actor + "-")) {
                            throw new IllegalArgumentException(
                                    "No tenés permiso para consultar ese aviso.");
                        }
                        return aviso;
                    });
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    @Override
    public List<Notificacion_Correo> obtenerTodos() {
        try {
            return Conexion.transaccion(
                    db -> {
                        Permisos_servicio.verificar(
                                db, Auditoria_servicio.usuarioActual(), Rol.DIRECTIVO);
                        return seleccionar(
                                db,
                                "SELECT * FROM notificacion_correo ORDER BY id_notificacion DESC"
                                        + " LIMIT 100");
                    });
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // el servicio web administra los estados para conservar los intentos y evitar envíos
    // simultáneos
    @Override
    public void modificar(Notificacion_Correo n) {
        throw new UnsupportedOperationException(
                "El estado del aviso se actualiza mediante el servicio de correo.");
    }

    @Override
    public void eliminar(Integer id) {
        throw new UnsupportedOperationException("Los avisos conservan su historial de envío.");
    }

    protected Notificacion_Correo leerEntidad(java.sql.ResultSet registros)
            throws java.sql.SQLException {
        Notificacion_Correo objeto = new Notificacion_Correo();
        objeto.setId_notificacion((Integer) registros.getObject("id_notificacion"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setDestinatario(registros.getString("destinatario"));
        objeto.setTipo(registros.getString("tipo"));
        objeto.setClave_evento(registros.getString("clave_evento"));
        objeto.setAsunto(registros.getString("asunto"));
        objeto.setMensaje(registros.getString("mensaje"));
        objeto.setEstado(registros.getString("estado"));
        objeto.setIntentos(registros.getInt("intentos"));
        objeto.setCreado(
                (registros.getTimestamp("creado") == null
                        ? null
                        : registros.getTimestamp("creado").toLocalDateTime()));
        objeto.setUltimo_intento(
                (registros.getTimestamp("ultimo_intento") == null
                        ? null
                        : registros.getTimestamp("ultimo_intento").toLocalDateTime()));
        objeto.setEnviado(
                (registros.getTimestamp("enviado") == null
                        ? null
                        : registros.getTimestamp("enviado").toLocalDateTime()));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Notificacion_Correo
    private java.util.List<Notificacion_Correo> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Notificacion_Correo> lista = new java.util.ArrayList<>();
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
    public java.util.List<Notificacion_Correo> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM notificacion_correo");
    }

    public Notificacion_Correo buscar(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion, "SELECT * FROM notificacion_correo WHERE id_notificacion=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Notificacion_Correo requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM notificacion_correo WHERE id_notificacion=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

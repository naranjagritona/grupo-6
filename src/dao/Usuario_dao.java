package dao;

import clases.Usuario;

import conexion.Conexion;

import seguridad.Contrasenas;

import servicios.Auditoria_servicio;
import servicios.Permisos_servicio;

import java.sql.*;
import java.util.*;

// consultamos las cuentas y compartimos la conexión de sus operaciones administrativas
public class Usuario_dao implements Dao<Usuario, Integer> {
    // comprobamos la contraseña y el estado del perfil sin crear todavía una sesión
    public Usuario autenticar(String nombre, String clave) {
        try (Connection db = Conexion.getConnection();
                PreparedStatement q =
                        db.prepareStatement(
                                "SELECT * FROM usuario WHERE nombre_usuario=? OR mail=? ORDER BY"
                                        + " id_usuario")) {
            q.setString(1, nombre);
            q.setString(2, nombre);
            try (ResultSet r = q.executeQuery()) {
                while (r.next())
                    if (Contrasenas.verificar(clave, r.getString("contraseña"))
                            && perfilActivo(db, r.getInt("id_usuario"), r.getInt("id_rol")))
                        return leer(r);
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo comprobar la cuenta.", e);
        }
    }

    // renovamos el contexto de la sesión y comprobamos el estado vigente del perfil
    public Usuario obtenerSesion(Integer id) {
        if (id == null || !id.equals(Auditoria_servicio.usuarioActual())) {
            throw new IllegalArgumentException("Iniciá sesión para continuar.");
        }
        try {
            return Conexion.transaccion(
                    db -> {
                        rolActivo(db, id);
                        Usuario usuario = buscar(db, id);
                        if (usuario == null)
                            throw new IllegalArgumentException("La cuenta ya no está disponible.");
                        // devolvemos los datos de la cuenta sin pasar la contraseña a las pantallas
                        usuario.setContraseña(null);
                        return usuario;
                    });
        } catch (Exception error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    // pasamos los datos de la tabla a su objeto sin cambiar el identificador del rol
    private Usuario leer(ResultSet r) throws SQLException {
        Usuario u =
                new Usuario(
                        r.getInt("id_usuario"),
                        r.getInt("id_rol"),
                        r.getString("nombre_usuario"),
                        r.getString("contraseña"));
        u.setMail(r.getString("mail"));
        return u;
    }

    // reservamos el alta con el mismo bloqueo que utiliza la web para evitar cuentas repetidas
    public void reservarAlta(Connection db) throws SQLException {
        String consulta = "SELECT GET_LOCK('academia_alta_usuario',10)";
        try (PreparedStatement ps = db.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            // la web usa el mismo bloqueo, así no se crean dos cuentas iguales al mismo tiempo
            if (!registros.next() || registros.getInt(1) != 1) {
                throw new IllegalArgumentException(
                        "Otra cuenta se está registrando. Intentá nuevamente.");
            }
        }
    }

    // comprobamos tanto los nombres como los correos porque ambos se utilizan para iniciar sesión
    public boolean nombreOMailOcupado(Connection db, String nombre, String mail, int excepto)
            throws SQLException {
        return !seleccionar(
                        db,
                        "SELECT * FROM usuario WHERE id_usuario<>? AND (nombre_usuario IN (?,?) OR"
                                + " mail IN (?,?)) FOR UPDATE",
                        excepto,
                        nombre,
                        mail,
                        nombre,
                        mail)
                .isEmpty();
    }

    // insertamos la cuenta y devolvemos su identificador dentro de la transacción del servicio
    public int crear(Connection db, Usuario u) throws SQLException {
        String consulta =
                "INSERT INTO usuario(id_rol,nombre_usuario,contraseña,mail) VALUES(?,?,?,?)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, u.getId_rol());
            ps.setObject(2, u.getNombre_usuario());
            ps.setObject(3, u.getContraseña());
            ps.setObject(4, u.getMail());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // bloqueamos la cuenta antes de revisar su rol, su perfil y los cambios pendientes
    public Usuario bloquear(Connection db, int id) throws SQLException {
        Usuario usuario = requerir(db, id);
        usuario.setContraseña(null);
        return usuario;
    }

    // conservamos la contraseña cuando la operación solamente modifica el rol o los datos de acceso
    public void actualizarCuenta(Connection db, Usuario u) throws SQLException {
        String consulta = "UPDATE usuario SET id_rol=?,nombre_usuario=?,mail=? WHERE id_usuario=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, u.getId_rol());
            ps.setObject(2, u.getNombre_usuario());
            ps.setObject(3, u.getMail());
            ps.setObject(4, u.getId_usuario());
            ps.executeUpdate();
        }
    }

    // registramos la cuenta básica respetando los permisos del usuario de la sesión
    @Override
    public void insertar(Usuario u) {
        try {
            Conexion.transaccion(
                    db -> {
                        Integer actor = Auditoria_servicio.usuarioActual();
                        Permisos_servicio.verificar(
                                db, actor, clases.Rol.DIRECTIVO, clases.Rol.SECRETARIA);
                        if (!Permisos_servicio.rolesCreables(
                                        new dao.Usuario_dao().rolActivo(db, actor))
                                .contains(u.getId_rol()))
                            throw new IllegalArgumentException("No podés crear ese rol.");
                        reservarAlta(db);
                        if (nombreOMailOcupado(db, u.getNombre_usuario(), u.getMail(), 0))
                            throw new IllegalArgumentException(
                                    "El usuario o mail ya está registrado.");
                        u.setContraseña(Contrasenas.paraGuardar(u.getContraseña()));
                        u.setId_usuario(crear(db, u));
                        return null;
                    });
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    // recuperamos una cuenta para autenticar la sesión o consultar sus datos administrativos
    @Override
    public Usuario obtenerPorId(Integer id) {
        try (Connection db = Conexion.getConnection();
                PreparedStatement q =
                        db.prepareStatement("SELECT * FROM usuario WHERE id_usuario=?")) {
            q.setInt(1, id);
            try (ResultSet r = q.executeQuery()) {
                return r.next() ? leer(r) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la cuenta.", e);
        }
    }

    @Override
    public List<Usuario> obtenerTodos() {
        try (Connection db = Conexion.getConnection();
                PreparedStatement q =
                        db.prepareStatement("SELECT * FROM usuario ORDER BY id_usuario");
                ResultSet r = q.executeQuery()) {
            List<Usuario> lista = new ArrayList<>();
            while (r.next()) lista.add(leer(r));
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar las cuentas.", e);
        }
    }

    // comprobamos el rol original y el solicitado antes de modificar una cuenta
    @Override
    public void modificar(Usuario u) {
        try {
            Conexion.transaccion(
                    db -> {
                        Integer actor = Auditoria_servicio.usuarioActual();
                        Permisos_servicio.verificar(
                                db, actor, clases.Rol.DIRECTIVO, clases.Rol.SECRETARIA);
                        var anterior = bloquear(db, u.getId_usuario());
                        if (!Permisos_servicio.cambiarRol(
                                new dao.Usuario_dao().rolActivo(db, actor),
                                anterior.getId_rol(),
                                u.getId_rol()))
                            throw new IllegalArgumentException(
                                    "No tenés permiso para modificar esa cuenta o asignar ese"
                                            + " rol.");
                        if (actor.equals(u.getId_usuario())
                                && !Objects.equals(u.getId_rol(), anterior.getId_rol()))
                            throw new IllegalArgumentException("No podés cambiar tu propio rol.");
                        if (nombreOMailOcupado(
                                db, u.getNombre_usuario(), u.getMail(), u.getId_usuario()))
                            throw new IllegalArgumentException(
                                    "El usuario o mail ya está registrado.");
                        actualizarCuenta(db, u);
                        return null;
                    });
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    // las cuentas conservan sus relaciones, la baja se realiza cambiando el estado del perfil
    @Override
    public void eliminar(Integer id) {
        throw new UnsupportedOperationException(
                "Usá la baja administrativa para conservar la cuenta y su historial.");
    }

    public void cerrarSesion(Usuario usuario) {
        servicios.Facturacion_servicio.cerrarSesion();
        Auditoria_servicio.cerrar();
    }

    public int rolActivo(Connection db, int id) throws SQLException {
        Usuario u = buscar(db, id);
        if (u == null || !perfilActivo(db, id, u.getId_rol()))
            throw new IllegalArgumentException(
                    "La cuenta está inactiva o no tiene un perfil válido.");
        return u.getId_rol();
    }

    private boolean perfilActivo(Connection db, int usuario, int rol) throws SQLException {
        var perfil = new servicios.Perfiles_servicio().perfil(db, usuario, rol);
        if (perfil.isEmpty())
            return java.util.List.of(
                            clases.Rol.DIRECTIVO, clases.Rol.SECRETARIA, clases.Rol.COOPERADORA)
                    .contains(rol);
        return "Activo".equals(perfil.get("estado"));
    }

    protected Usuario leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Usuario objeto = new Usuario();
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setId_rol((Integer) registros.getObject("id_rol"));
        objeto.setNombre_usuario(registros.getString("nombre_usuario"));
        objeto.setContraseña(registros.getString("contraseña"));
        objeto.setMail(registros.getString("mail"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Usuario
    private java.util.List<Usuario> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Usuario> lista = new java.util.ArrayList<>();
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
    public java.util.List<Usuario> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM usuario");
    }

    public Usuario buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM usuario WHERE id_usuario=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Usuario requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas =
                seleccionar(conexion, "SELECT * FROM usuario WHERE id_usuario=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }

    public int contar(java.sql.Connection conexion) throws java.sql.SQLException {
        String consulta = "SELECT COUNT(*) FROM usuario";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta);
                java.sql.ResultSet registros = ps.executeQuery()) {
            registros.next();
            return registros.getInt(1);
        }
    }
}

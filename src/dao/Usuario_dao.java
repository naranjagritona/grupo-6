package dao;

import clases.Usuario;
import conexion.Conexion;
import seguridad.Contrasenas;
import servicios.Auditoria_servicio;
import servicios.Permisos_servicio;
import java.sql.*;
import java.util.*;

// consultamos las cuentas y compartimos la conexión de sus operaciones administrativas
public class Usuario_dao extends Consulta_academica_dao implements Dao<Usuario, Integer> {
    // comprobamos la contraseña y el estado del perfil sin crear todavía una sesión
    public Usuario autenticar(String nombre, String clave) {
        try (Connection db = abrirConexion(); PreparedStatement q = db.prepareStatement(
                "SELECT * FROM usuario WHERE nombre_usuario=? OR mail=? ORDER BY id_usuario")) {
            q.setString(1, nombre); q.setString(2, nombre);
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) if (Contrasenas.verificar(clave, r.getString("contraseña"))
                        && perfilActivo(db, r.getInt("id_usuario"), r.getInt("id_rol"))) return leer(r);
            }
            return null;
        } catch (SQLException e) { throw new IllegalStateException("No se pudo comprobar la cuenta.", e); }
    }

    // renovamos el contexto de la sesión y comprobamos el estado vigente del perfil
    public Usuario obtenerSesion(Integer id) {
        if (id == null || !id.equals(Auditoria_servicio.usuarioActual())) throw new IllegalArgumentException("Iniciá sesión para continuar.");
        try { return transaccion(db -> {
            obtenerRol(db, id);
            var filas = consultar(db, "SELECT id_usuario,id_rol,nombre_usuario,mail FROM usuario WHERE id_usuario=?", id);
            if (filas.isEmpty()) throw new IllegalArgumentException("La cuenta ya no está disponible.");
            var fila = filas.getFirst(); Usuario u = new Usuario(id, numero(fila,"id_rol"), (String)fila.get("nombre_usuario"), null);u.setMail((String)fila.get("mail"));return u;
        }); } catch (Exception e) { throw new IllegalArgumentException(e.getMessage(), e); }
    }

    // pasamos los datos de la tabla a su objeto sin cambiar el identificador del rol
    private Usuario leer(ResultSet r) throws SQLException {
        Usuario u = new Usuario(r.getInt("id_usuario"), r.getInt("id_rol"), r.getString("nombre_usuario"), r.getString("contraseña"));
        u.setMail(r.getString("mail")); return u;
    }

    // reservamos el alta con el mismo bloqueo que utiliza la web para evitar cuentas repetidas
    public void reservarAlta(Connection db) throws SQLException {
        if (numero(uno(db, "SELECT GET_LOCK('academia_alta_usuario',10) reservado"), "reservado") != 1)
            throw new IllegalArgumentException("Otra cuenta se está registrando. Intentá nuevamente.");
    }

    // comprobamos tanto los nombres como los correos porque ambos se utilizan para iniciar sesión
    public boolean nombreOMailOcupado(Connection db, String nombre, String mail, int excepto) throws SQLException {
        return !consultar(db, "SELECT id_usuario FROM usuario WHERE id_usuario<>? AND (nombre_usuario IN (?,?) OR mail IN (?,?)) FOR UPDATE", excepto, nombre, mail, nombre, mail).isEmpty();
    }

    // insertamos la cuenta y devolvemos su identificador dentro de la transacción del servicio
    public int crear(Connection db, Usuario u) throws SQLException {
        return insertarRegistro(db, "INSERT INTO usuario(id_rol,nombre_usuario,contraseña,mail) VALUES(?,?,?,?)", u.getId_rol(), u.getNombre_usuario(), u.getContraseña(), u.getMail());
    }

    // bloqueamos la cuenta antes de revisar su rol, su perfil y los cambios pendientes
    public Map<String,Object> bloquear(Connection db, int id) throws SQLException {
        return uno(db, "SELECT id_usuario,id_rol,nombre_usuario,mail FROM usuario WHERE id_usuario=? FOR UPDATE", id);
    }

    // conservamos la contraseña cuando la operación solamente modifica el rol o los datos de acceso
    public void actualizarCuenta(Connection db, Usuario u) throws SQLException {
        ejecutar(db, "UPDATE usuario SET id_rol=?,nombre_usuario=?,mail=? WHERE id_usuario=?", u.getId_rol(), u.getNombre_usuario(), u.getMail(), u.getId_usuario());
    }

    // elegimos una tabla conocida; ningún nombre de tabla proviene de un formulario
    public String tablaPerfil(int rol) {
        return switch (rol) {
            case clases.Rol.DIRECTIVO -> "directivo";
            case clases.Rol.SECRETARIA -> "secretaria";
            case clases.Rol.DOCENTE -> "docente";
            case clases.Rol.ALUMNO -> "alumno";
            case clases.Rol.COOPERADORA -> "cooperadora";
            default -> throw new IllegalArgumentException("Seleccioná un rol válido.");
        };
    }

    // consultamos el perfil que corresponde al rol actual y conservamos los perfiles anteriores
    public Map<String,Object> perfil(Connection db, int usuario, int rol) throws SQLException {
        var filas = consultar(db, "SELECT * FROM " + tablaPerfil(rol) + " WHERE id_usuario=? FOR UPDATE", usuario);
        return filas.isEmpty() ? new LinkedHashMap<>() : filas.getFirst();
    }
    public Map<String,Object> responsable(Connection db, int id) throws SQLException {
        var filas = consultar(db, "SELECT * FROM responsable WHERE id_responsable=?", id);
        return filas.isEmpty() ? Map.of() : filas.getFirst();
    }

    // desactivamos el perfil anterior al cambiar de rol sin eliminar sus inscripciones ni su historial
    public void inactivarPerfil(Connection db, int usuario, int rol) throws SQLException {
        ejecutar(db, "UPDATE " + tablaPerfil(rol) + " SET estado='Inactivo' WHERE id_usuario=?", usuario);
    }

    // relacionamos únicamente el perfil del rol vigente para no mezclar personas de roles anteriores
    public List<Map<String,Object>> listarCuentas(Connection db) throws SQLException {
        return consultar(db, "SELECT u.id_usuario,u.id_rol,u.nombre_usuario,u.mail,r.nombre rol,"
            + "COALESCE(di.nombre,s.nombre,d.nombre,a.nombre,co.nombre,'') nombre,"
            + "COALESCE(di.apellido,s.apellido,d.apellido,a.apellido,co.apellido,'') apellido,"
            + "COALESCE(di.dni,s.dni,d.dni,a.dni,co.dni,'') dni,"
            + "COALESCE(di.legajo,s.legajo,d.legajo,a.matricula,co.legajo,'') numero,"
            + "COALESCE(di.estado,s.estado,d.estado,a.estado,co.estado,'Sin perfil') estado "
            + "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol "
            + "LEFT JOIN directivo di ON di.id_usuario=u.id_usuario AND u.id_rol=1 "
            + "LEFT JOIN secretaria s ON s.id_usuario=u.id_usuario AND u.id_rol=2 "
            + "LEFT JOIN docente d ON d.id_usuario=u.id_usuario AND u.id_rol=3 "
            + "LEFT JOIN alumno a ON a.id_usuario=u.id_usuario AND u.id_rol=4 "
            + "LEFT JOIN cooperadora co ON co.id_usuario=u.id_usuario AND u.id_rol=5 ORDER BY u.id_usuario DESC");
    }

    // registramos la cuenta básica respetando los permisos del usuario de la sesión
    @Override public void insertar(Usuario u) {
        try { transaccion(db -> {
            Integer actor=Auditoria_servicio.usuarioActual();
            Permisos_servicio.verificar(db,this,actor,clases.Rol.DIRECTIVO,clases.Rol.SECRETARIA);
            if(!Permisos_servicio.rolesCreables(obtenerRol(db,actor)).contains(u.getId_rol()))throw new IllegalArgumentException("No podés crear ese rol.");
            reservarAlta(db);
            if(nombreOMailOcupado(db,u.getNombre_usuario(),u.getMail(),0))throw new IllegalArgumentException("El usuario o mail ya está registrado.");
            u.setContraseña(Contrasenas.paraGuardar(u.getContraseña()));u.setId_usuario(crear(db,u));return null;
        }); } catch(Exception e){throw new IllegalArgumentException(e.getMessage(),e);}
    }

    // recuperamos una cuenta para autenticar la sesión o consultar sus datos administrativos
    @Override public Usuario obtenerPorId(Integer id) {
        try(Connection db=abrirConexion();PreparedStatement q=db.prepareStatement("SELECT * FROM usuario WHERE id_usuario=?")){
            q.setInt(1,id);try(ResultSet r=q.executeQuery()){return r.next()?leer(r):null;}
        }catch(SQLException e){throw new IllegalStateException("No se pudo consultar la cuenta.",e);}
    }

    @Override public List<Usuario> obtenerTodos() {
        try(Connection db=abrirConexion();PreparedStatement q=db.prepareStatement("SELECT * FROM usuario ORDER BY id_usuario" );ResultSet r=q.executeQuery()){
            List<Usuario> lista=new ArrayList<>();while(r.next())lista.add(leer(r));return lista;
        }catch(SQLException e){throw new IllegalStateException("No se pudieron consultar las cuentas.",e);}
    }

    // comprobamos el rol original y el solicitado antes de modificar una cuenta
    @Override public void modificar(Usuario u) {
        try { transaccion(db->{Integer actor=Auditoria_servicio.usuarioActual();Permisos_servicio.verificar(db,this,actor,clases.Rol.DIRECTIVO,clases.Rol.SECRETARIA);
            var anterior=bloquear(db,u.getId_usuario());
            if(!Permisos_servicio.cambiarRol(obtenerRol(db,actor),numero(anterior,"id_rol"),u.getId_rol()))throw new IllegalArgumentException("No tenés permiso para modificar esa cuenta o asignar ese rol.");
            if(actor.equals(u.getId_usuario())&&!Objects.equals(u.getId_rol(),numero(anterior,"id_rol")))throw new IllegalArgumentException("No podés cambiar tu propio rol.");
            if(nombreOMailOcupado(db,u.getNombre_usuario(),u.getMail(),u.getId_usuario()))throw new IllegalArgumentException("El usuario o mail ya está registrado.");
            actualizarCuenta(db,u);return null;});
        }catch(Exception e){throw new IllegalArgumentException(e.getMessage(),e);}
    }

    // las cuentas conservan sus relaciones; la baja se realiza cambiando el estado del perfil
    @Override public void eliminar(Integer id) {throw new UnsupportedOperationException("Usá la baja administrativa para conservar la cuenta y su historial.");}
    public void cerrarSesion(Usuario usuario) { servicios.Facturacion_servicio.cerrarSesion();Auditoria_servicio.cerrar(); }
}

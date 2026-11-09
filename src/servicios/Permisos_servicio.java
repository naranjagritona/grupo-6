package servicios;

import clases.Usuario;
import clases.Rol;
import dao.Consulta_academica_dao;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;

// comprobamos el rol de la sesión y los permisos que corresponden a cada operación
public final class Permisos_servicio {
    private Permisos_servicio() {}

    // verificamos si la cuenta pertenece a dirección
    public static boolean directivo(Usuario usuario) {
        return tieneRol(usuario, Rol.DIRECTIVO);
    }

    // verificamos si la cuenta pertenece a secretaría
    public static boolean secretario(Usuario usuario) {
        return tieneRol(usuario, Rol.SECRETARIA);
    }

    // comprobamos si el usuario puede consultar y gestionar inscripciones
    public static boolean gestion(Usuario usuario) {
        return directivo(usuario) || secretario(usuario);
    }

    // distinguimos los módulos de gestión, el trabajo académico y los pagos de cada perfil
    private static boolean tieneRol(Usuario usuario, int rol) { return usuario != null && Objects.equals(usuario.getId_rol(), rol); }
    public static boolean docente(Usuario usuario) { return tieneRol(usuario, Rol.DOCENTE); }
    public static boolean alumno(Usuario usuario) { return tieneRol(usuario, Rol.ALUMNO); }
    public static boolean cooperadora(Usuario usuario) { return tieneRol(usuario, Rol.COOPERADORA); }
    public static boolean verAcademico(Usuario usuario) { return gestion(usuario) || docente(usuario) || alumno(usuario); }
    public static boolean verCalificaciones(Usuario usuario) { return directivo(usuario) || docente(usuario) || alumno(usuario); }
    public static boolean editarCalificaciones(Usuario usuario) { return directivo(usuario) || docente(usuario); }
    public static boolean verFacturacion(Usuario usuario) { return directivo(usuario) || cooperadora(usuario) || alumno(usuario); }
    public static boolean gestionarFacturacion(Usuario usuario) { return directivo(usuario) || cooperadora(usuario); }
    public static boolean verLogs(Usuario usuario) { return directivo(usuario); }

    // conservamos los mismos roles creables que utiliza la web para cada cuenta administrativa
    public static List<Integer> rolesCreables(int rol) {
        return rol == Rol.DIRECTIVO ? List.of(Rol.DIRECTIVO, Rol.SECRETARIA, Rol.DOCENTE, Rol.ALUMNO, Rol.COOPERADORA)
                : rol == Rol.SECRETARIA ? List.of(Rol.DOCENTE, Rol.ALUMNO, Rol.COOPERADORA) : List.of();
    }

    // protegemos también el rol de origen para que secretaría no modifique cuentas de dirección
    public static boolean administrarCuenta(int actor, int destino) {
        return actor == Rol.DIRECTIVO || (actor == Rol.SECRETARIA && rolesCreables(actor).contains(destino));
    }
    public static boolean cambiarRol(int actor, int anterior, int nuevo) {
        return administrarCuenta(actor, anterior) && rolesCreables(actor).contains(nuevo);
    }

    // consultamos el rol en la base y comprobamos que coincida con la sesión y los permisos requeridos
    public static void verificar(Connection conexion, Consulta_academica_dao dao, Integer usuario, int... roles) throws Exception {
        Integer actual = Auditoria_servicio.usuarioActual();
        if (actual == null || usuario == null || !actual.equals(usuario)) {
            throw new IllegalArgumentException("Iniciá sesión para realizar esta operación.");
        }
        int rol = dao.obtenerRol(conexion, usuario);
        for (int permitido : roles) if (rol == permitido) return;
        throw new IllegalArgumentException("No tenés permisos para realizar esta operación.");
    }

    // buscamos el usuario que tiene la sesión iniciada
    public static Usuario actual() {
        Integer id = Auditoria_servicio.usuarioActual();
        if (id == null) throw new IllegalArgumentException("Iniciá sesión para realizar esta operación.");
        Usuario usuario = new dao.Usuario_dao().obtenerSesion(id);
        if (usuario == null) throw new IllegalArgumentException("La cuenta ya no está disponible.");
        return usuario;
    }
}

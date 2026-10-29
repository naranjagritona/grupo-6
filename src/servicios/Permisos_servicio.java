package servicios;

import clases.Usuario;
import dao.Consulta_academica_dao;
import java.sql.Connection;

// comprobamos el rol de la sesión y los permisos que corresponden a cada operación
public final class Permisos_servicio {
    private Permisos_servicio() {}

    // verificamos si la cuenta pertenece a dirección
    public static boolean directivo(Usuario usuario) {
        return usuario != null && usuario.getId_rol() == 5;
    }

    // verificamos si la cuenta pertenece a secretaría
    public static boolean secretario(Usuario usuario) {
        return usuario != null && usuario.getId_rol() == 1;
    }

    // comprobamos si el usuario puede consultar y gestionar inscripciones
    public static boolean gestion(Usuario usuario) {
        return directivo(usuario) || secretario(usuario);
    }

    // consultamos el rol en la base y comprobamos que coincida con la sesión y los permisos requeridos
    public static void verificar(Connection conexion, Consulta_academica_dao dao, Integer usuario, int... roles) throws Exception {
        Integer actual = Auditoria_servicio.usuarioActual();
        if (usuario == null || (actual != null && !actual.equals(usuario))) {
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
        return new dao.Usuario_dao().obtenerPorId(id);
    }
}

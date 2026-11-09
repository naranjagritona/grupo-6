import clases.*;
import dao.*;
import servicios.*;
import java.util.*;

// comprobamos los accesos reales sin modificar cuentas, cursos ni inscripciones
public class IntegracionJava {
    private static void comprobar(boolean condicion, String texto) { if (!condicion) throw new AssertionError(texto); }
    public static void main(String[] args) throws Exception {
        Usuario_dao usuarios = new Usuario_dao();
        Usuario direccion = usuarios.autenticar("administracion.academia", System.getenv("ACADEMIA_CLAVE_PRUEBA"));
        comprobar(direccion != null && direccion.getId_rol() == Rol.DIRECTIVO, "Acceso de dirección");
        Auditoria_servicio.iniciar(direccion.getId_usuario());
        var admin = new Admin_servicio(); var datos = admin.cargar(direccion.getId_usuario());
        comprobar(datos.roles().size() == 5, "Dirección dispone de los cinco roles");
        comprobar(!datos.cuentas().isEmpty(), "Consulta de cuentas");
        comprobar(new Curso_dao().listar(direccion).size() > 0, "Cursos de dirección");
        System.out.println("Dirección: acceso, cuentas, roles, cursos y auditoría OK");
        Usuario secretaria = usuarios.autenticar("admin", System.getenv("ACADEMIA_CLAVE_PRUEBA"));
        comprobar(secretaria != null && secretaria.getId_rol() == Rol.SECRETARIA, "Acceso de secretaría");
        Auditoria_servicio.iniciar(secretaria.getId_usuario());
        var limitados = admin.cargar(secretaria.getId_usuario());
        comprobar(new java.util.HashSet<>(limitados.roles().stream().map(Rol::getId_rol).toList()).equals(java.util.Set.of(3,4,5)), "Roles creables de secretaría");
        comprobar(limitados.logs().isEmpty(), "Secretaría no recibe logs");
        try { new Log_Usuario_dao().obtenerTodos(); throw new AssertionError("Se permitió consultar logs"); } catch (IllegalArgumentException esperado) {}
        try { new ConsultaCalificaciones_dao().listar(1, secretaria); throw new AssertionError("Se permitió consultar notas"); } catch (IllegalArgumentException esperado) {}
        try { new Notificaciones_servicio().probar("prueba@example.com", "denegada", secretaria.getId_usuario()); throw new AssertionError("Se permitió correo de prueba"); } catch (IllegalArgumentException esperado) {}
        System.out.println("Secretaría: altas permitidas, logs/notas/prueba de correo denegados OK");
        for (var perfil : List.of(new Object[]{"alumno.640",Rol.ALUMNO}, new Object[]{"ana.castillo",Rol.DOCENTE}, new Object[]{"cooperadora",Rol.COOPERADORA})) {
            Usuario u = usuarios.autenticar((String)perfil[0], System.getenv("ACADEMIA_CLAVE_PRUEBA"));
            comprobar(u != null && u.getId_rol().equals(perfil[1]), "Acceso " + perfil[0]);
            Auditoria_servicio.iniciar(u.getId_usuario());
            try { admin.cargar(u.getId_usuario()); throw new AssertionError("Se permitió administración a " + perfil[0]); } catch (IllegalArgumentException esperado) {}
        }
        System.out.println("Alumno, docente y cooperadora: administración denegada OK");
        Auditoria_servicio.iniciar(direccion.getId_usuario());
        if (args.length > 0) {
            // este bloque sólo se ejecuta al indicar el destinatario autorizado de la única prueba
            SesionWeb_servicio.prepararSesion("administracion.academia", System.getenv("ACADEMIA_CLAVE_PRUEBA"));
            var avisos = new Notificaciones_servicio(); var resultado = avisos.probar(args[0], "20261109-unica", direccion.getId_usuario());
            comprobar(resultado.enviado(), resultado.detalle());
            var dao = new Notificacion_Correo_dao(); var antes = dao.obtenerPorId(resultado.id());
            comprobar(avisos.enviar(resultado.id()).enviado(), "Reintento idempotente");
            comprobar(dao.obtenerPorId(resultado.id()).getIntentos() == antes.getIntentos(), "El aviso enviado no se duplica");
            System.out.println("SMTP: un único mensaje aceptado, reintento sin duplicar. Aviso #" + resultado.id());
        }
        SesionWeb_servicio.cerrarSesion(); Auditoria_servicio.cerrar();
    }
}

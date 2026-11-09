import clases.*;
import dao.*;
import servicios.*;
import com.google.gson.JsonObject;

// comprobamos la reversión real y las autorizaciones del correo sin enviar mensajes nuevos
public class PersistenciaJava {
    public static void main(String[] args) throws Exception {
        Usuario_dao usuarios=new Usuario_dao();
        String clave=System.getenv("ACADEMIA_CLAVE_PRUEBA");
        Usuario director=usuarios.autenticar("administracion.academia",clave);
        Auditoria_servicio.iniciar(director.getId_usuario());
        Notificacion_Correo_dao dao=new Notificacion_Correo_dao();
        int[] insertado={0};
        try {
            dao.transaccion(db->{
                Notificacion_Correo n=new Notificacion_Correo();n.setId_usuario(director.getId_usuario());
                n.setDestinatario(args[0]);n.setTipo("Alta");n.setClave_evento("java-"+director.getId_usuario()+"-rollback-"+java.util.UUID.randomUUID());
                n.setAsunto("Verificación de rollback");n.setMensaje("Esta transacción debe revertirse y no se envía.");
                insertado[0]=dao.agregar(db,n);
                throw new IllegalArgumentException("Fallo controlado antes del commit");
            });
        } catch(IllegalArgumentException esperado) {}
        if(insertado[0]==0 || dao.obtenerPorId(insertado[0])!=null)throw new AssertionError("La transacción no se revirtió");
        System.out.println("MySQL: inserción real revertida, aviso inexistente y sin envío OK");
        Usuario secretaria=usuarios.autenticar("admin",clave);
        Auditoria_servicio.iniciar(secretaria.getId_usuario());SesionWeb_servicio.prepararSesion("admin",clave);
        JsonObject peticion=new JsonObject();peticion.addProperty("id_notificacion",Integer.parseInt(args[1]));
        try {SesionWeb_servicio.json("api_notificaciones.php","POST","",peticion);throw new AssertionError("Secretaría envió un aviso ajeno");}
        catch(IllegalStateException esperado) {if(!esperado.getMessage().contains("permiso"))throw esperado;}
        System.out.println("HTTPS: Secretaría no puede enviar avisos ajenos ni de Dirección OK");
        Auditoria_servicio.iniciar(director.getId_usuario());SesionWeb_servicio.prepararSesion("administracion.academia",clave);
        peticion.addProperty("id_rol",1);
        try {SesionWeb_servicio.json("api_notificaciones.php","POST","",peticion);throw new AssertionError("Se aceptó un rol del cliente");}
        catch(IllegalStateException esperado) {if(!esperado.getMessage().contains("inválido"))throw esperado;}
        System.out.println("HTTPS: formulario con rol enviado por cliente rechazado OK");
        SesionWeb_servicio.cerrarSesion();Auditoria_servicio.cerrar();
    }
}

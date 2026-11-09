package servicios;

import clases.Notificacion_Correo;
import dao.Notificacion_Correo_dao;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;

// preparamos avisos en la transacción del alta o asignación y enviamos después de confirmarla
public class Notificaciones_servicio {
    private final Notificacion_Correo_dao dao;
    private static final ExecutorService envios=Executors.newSingleThreadExecutor(r->{Thread h=new Thread(r,"avisos-de-correo");h.setDaemon(true);return h;});
    public Notificaciones_servicio() {this(new Notificacion_Correo_dao());}
    public Notificaciones_servicio(Notificacion_Correo_dao dao) {this.dao=dao;}
    public record Resultado(boolean enviado,Integer id,String detalle) {}

    // usamos el mail de la cuenta y nunca incluimos su contraseña en el mensaje de alta
    public Integer alta(Connection db,int usuario,String nombre,String cuenta,int actor) throws Exception {
        var destino=dao.destinatario(db,usuario);String mail=(String)destino.get("mail");if(mail==null||mail.isBlank())return null;
        return agregar(db,usuario,mail,"Alta","java-"+actor+"-alta-"+usuario,"Alta de cuenta - Academia Integral XXI","Hola "+nombre+". Se dio de alta tu cuenta en Academia Integral XXI. Tu nombre de usuario es "+cuenta+". Podés ingresar con tu usuario o este mail en https://academiaintegral.cavernservices.org/ usando la contraseña que acordaste con Secretaría.");
    }

    // el aviso describe el curso completo y conserva una clave única del evento que lo originó
    public Integer asignacion(Connection db,int usuario,int curso,String evento,int actor) throws Exception {
        var destino=dao.destinatario(db,usuario);String mail=(String)destino.get("mail");if(mail==null||mail.isBlank())return null;
        var c=dao.curso(db,curso);
        return agregar(db,usuario,mail,"Curso","java-"+actor+"-curso-"+evento,"Asignación de curso - Academia Integral XXI","Se te asignó el curso "+c.get("año")+"° año, división "+c.get("division")+", turno "+c.get("turno")+", período "+c.get("periodo")+". Podés consultar los detalles ingresando a https://academiaintegral.cavernservices.org/");
    }

    // persistimos el mensaje usando el dao y la conexión de la operación que genera el aviso
    private Integer agregar(Connection db,int usuario,String mail,String tipo,String evento,String asunto,String mensaje) throws Exception {
        Notificacion_Correo n=new Notificacion_Correo();n.setId_usuario(usuario);n.setDestinatario(mail);n.setTipo(tipo);n.setClave_evento(evento);n.setAsunto(asunto);n.setMensaje(mensaje);return dao.agregar(db,n);
    }

    // delegamos el SMTP a la web para reutilizar su remitente y su configuración existente
    public Resultado enviar(Integer id) {
        if(id==null)return new Resultado(false,null,"No corresponde un aviso por correo para este perfil o no tiene mail registrado.");
        try{JsonObject datos=new JsonObject();datos.addProperty("id_notificacion",id);JsonObject r=SesionWeb_servicio.json("api_notificaciones.php","POST","",datos);boolean enviado="Enviado".equals(r.get("estado").getAsString());return new Resultado(enviado,id,enviado?"El aviso por mail se envió correctamente.":"El aviso quedó pendiente de reintento en Administración.");}
        catch(Exception e){return new Resultado(false,id,"Los datos se guardaron. El aviso quedó pendiente para reintentar el envío.");}
    }

    // reintentamos los avisos de la sesión; el servidor conserva el estado de los ya enviados
    public String reintentar(Integer actor) throws Exception {
        List<Integer> pendientes = dao.pendientesDelActor(actor);
        int enviados = 0;
        for (int id : pendientes) if (enviar(id).enviado()) enviados++;
        return "Avisos enviados: " + enviados + ". Pendientes: " + (pendientes.size() - enviados) + ".";
    }

    // una prueba de dirección usa un evento único y no crea cuentas ni modifica destinatarios de personas
    public Resultado probar(String destino, String evento, Integer actor) throws Exception {
        if (destino == null || !destino.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Ingresá un mail válido.");
        if (evento == null || !evento.matches("[A-Za-z0-9-]{1,60}")) throw new IllegalArgumentException("Identificador de prueba inválido.");
        int id = dao.transaccion(db -> {
            Permisos_servicio.verificar(db, dao, actor, clases.Rol.DIRECTIVO);
            return agregar(db, actor, destino, "Alta", "java-" + actor + "-prueba-" + evento, "Prueba de correo - Academia Integral XXI", "Este es un único mensaje de prueba enviado desde la aplicación Java para verificar la conexión con el correo de Academia Integral XXI. No se creó ni modificó ninguna cuenta.");
        });
        return enviar(id);
    }

    // el trabajo en segundo plano conserva los avisos si la sesión se cierra o el servidor no responde
    public static void enviarPendientes(int actor) {
        envios.submit(()->{if(!Objects.equals(Auditoria_servicio.usuarioActual(),actor))return;try{Notificaciones_servicio s=new Notificaciones_servicio();for(int id:s.dao.pendientesDelActor(actor)){if(!Objects.equals(Auditoria_servicio.usuarioActual(),actor))break;s.enviar(id);}}catch(Exception e){System.err.println("Los avisos guardados se podrán reintentar desde Administración.");}});
    }
}

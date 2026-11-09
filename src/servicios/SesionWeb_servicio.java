package servicios;

import com.google.gson.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

// compartimos una sesión HTTPS para pagos y notificaciones sin guardar contraseñas en archivos
public final class SesionWeb_servicio {
    private static final String BASE="https://academiaintegral.cavernservices.org/modulos/";
    private static final CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    private static final HttpClient cliente=HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(15)).build();
    private static String usuario,csrf;
    private static char[] clave;
    private static Integer identidad;
    private SesionWeb_servicio() {}

    // retenemos la contraseña sólo hasta abrir la sesión web y luego limpiamos su contenido
    public static synchronized void prepararSesion(String nombre,String contraseña) {
        cerrarSesion();usuario=nombre;clave=contraseña.toCharArray();identidad=Auditoria_servicio.usuarioActual();
    }
    public static synchronized void cerrarSesion() {
        cookies.getCookieStore().removeAll();csrf=null;usuario=null;identidad=null;if(clave!=null)Arrays.fill(clave,'\0');clave=null;
    }

    // comprobamos que la sesión web pertenezca al mismo usuario autenticado en Java
    private static void asegurarSesion() throws Exception {
        if(!Objects.equals(identidad,Auditoria_servicio.usuarioActual())||identidad==null)throw new IllegalStateException("Volvé a iniciar sesión para usar el servicio web.");
        if(csrf!=null)return;if(clave==null)throw new IllegalStateException("Volvé a iniciar sesión para renovar la conexión web.");
        JsonObject datos=new JsonObject();datos.addProperty("accion","ingresar");datos.addProperty("usuario",usuario);datos.addProperty("clave",new String(clave));
        JsonObject respuesta=convertir(enviarDirecto("api_facturacion.php","POST","",datos,null).body());
        if(!respuesta.has("id_usuario")||respuesta.get("id_usuario").getAsInt()!=identidad)throw new IllegalStateException("La sesión web no corresponde a la cuenta de Java.");
        csrf=respuesta.get("csrf").getAsString();Arrays.fill(clave,'\0');clave=null;
    }

    // enviamos únicamente a las rutas conocidas y protegemos las modificaciones con el token de sesión
    public static synchronized byte[] enviar(String ruta,String metodo,String consulta,JsonObject datos) throws Exception {
        if(!java.util.List.of("api_facturacion.php","api_notificaciones.php").contains(ruta))throw new IllegalArgumentException("Servicio no permitido.");
        asegurarSesion();return enviarDirecto(ruta,metodo,consulta,datos,"POST".equals(metodo)?csrf:null).body();
    }
    public static JsonObject json(String ruta,String metodo,String consulta,JsonObject datos) throws Exception {return convertir(enviar(ruta,metodo,consulta,datos));}

    private static HttpResponse<byte[]> enviarDirecto(String ruta,String metodo,String consulta,JsonObject datos,String token) throws Exception {
        HttpRequest.Builder peticion=HttpRequest.newBuilder(URI.create(BASE+ruta+consulta)).timeout(Duration.ofSeconds(60)).header("Accept","application/json");
        if("POST".equals(metodo)){peticion.header("Content-Type","application/json; charset=UTF-8");if(token!=null)peticion.header("X-CSRF-Token",token);peticion.POST(HttpRequest.BodyPublishers.ofString(datos.toString(),StandardCharsets.UTF_8));}else peticion.GET();
        HttpResponse<byte[]> respuesta=cliente.send(peticion.build(),HttpResponse.BodyHandlers.ofByteArray());
        if(respuesta.statusCode()<200||respuesta.statusCode()>=300){String mensaje="No se pudo completar la operación del servicio web.";try{mensaje=convertir(respuesta.body()).get("error").getAsString();}catch(Exception ignorado){}throw new IllegalStateException(mensaje);}
        return respuesta;
    }
    private static JsonObject convertir(byte[] cuerpo) {try{return JsonParser.parseString(new String(cuerpo,StandardCharsets.UTF_8)).getAsJsonObject();}catch(Exception e){throw new IllegalStateException("La web no devolvió una respuesta válida.",e);}}
}

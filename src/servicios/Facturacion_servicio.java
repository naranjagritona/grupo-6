package servicios;

import com.google.gson.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// consultamos pagos y comprobantes con la sesión HTTPS compartida de la aplicación
public final class Facturacion_servicio {
    private Facturacion_servicio() {}
    public static void prepararSesion(String usuario,String clave) { SesionWeb_servicio.prepararSesion(usuario,clave); }
    public static void cerrarSesion() { SesionWeb_servicio.cerrarSesion(); }

    // la web limita los resultados según el rol autenticado y valida cada operación
    public static JsonObject listar(String buscar) throws Exception {
        return SesionWeb_servicio.json("api_facturacion.php","GET","?accion=listar&q="+URLEncoder.encode(buscar,StandardCharsets.UTF_8),null);
    }
    public static JsonArray historial(int id) throws Exception {
        return SesionWeb_servicio.json("api_facturacion.php","GET","?accion=historial&id_inscripcion="+id,null).getAsJsonArray("pagos");
    }
    public static JsonObject operar(String accion,JsonObject datos) throws Exception {
        datos.addProperty("accion",accion);return SesionWeb_servicio.json("api_facturacion.php","POST","",datos);
    }

    // comprobamos la cabecera PDF antes de entregar el comprobante al visor
    public static byte[] factura(int id) throws Exception {
        byte[] pdf=SesionWeb_servicio.enviar("api_facturacion.php","GET","?accion=pdf&id_factura="+id,null);
        if(pdf.length<5||!new String(pdf,0,5,StandardCharsets.US_ASCII).equals("%PDF-"))throw new IllegalStateException("La factura recibida no es un PDF válido.");
        return pdf;
    }
}

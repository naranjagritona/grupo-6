package servicios;

import com.google.gson.*;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

// enviamos las consultas de facturación a la web para usar los mismos pagos y facturas
public final class Facturacion_servicio {
    private static final String URL =
            "https://academiaintegral.cavernservices.org/modulos/api_facturacion.php";
    // Guardamos la sesión en memoria para usarla en las siguientes consultas.
    private static final CookieManager cookies =
            new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    private static final HttpClient cliente =
            HttpClient.newBuilder()
                    .cookieHandler(cookies)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
    private static String usuario;
    private static char[] clave;
    private static String csrf;

    private Facturacion_servicio() {}

    // Recibimos los datos del acceso y limpiamos cualquier sesión anterior.
    public static synchronized void prepararSesion(String nombre, String contrasena) {
        cerrarSesion();
        usuario = nombre;
        clave = contrasena.toCharArray();
    }

    // Al salir borramos la sesión y la contraseña que todavía estuviera en memoria.
    public static synchronized void cerrarSesion() {
        cookies.getCookieStore().removeAll();
        csrf = null;
        usuario = null;
        if (clave != null) Arrays.fill(clave, '\0');
        clave = null;
    }

    // Abrimos la sesión web solo la primera vez que se usa Facturación.
    private static synchronized void asegurarSesion() throws Exception {
        if (csrf != null) return;
        if (clave == null)
            throw new IllegalStateException("Volvé a iniciar sesión para usar Facturación.");
        JsonObject datos = new JsonObject();
        datos.addProperty("accion", "ingresar");
        datos.addProperty("usuario", usuario);
        datos.addProperty("clave", new String(clave));
        JsonObject respuesta = json(enviar("POST", "", datos, null));
        csrf = respuesta.get("csrf").getAsString();
        // Después de abrir la sesión web, ya no necesitamos conservar la contraseña.
        Arrays.fill(clave, '\0');
        clave = null;
    }

    // Las operaciones usan JSON y un código de sesión para validar los formularios.
    private static HttpResponse<byte[]> enviar(
            String metodo, String consulta, JsonObject datos, String token) throws Exception {
        HttpRequest.Builder peticion =
                HttpRequest.newBuilder(URI.create(URL + consulta))
                        .timeout(Duration.ofSeconds(60))
                        .header("Accept", "application/json");
        if (metodo.equals("POST")) {
            peticion.header("Content-Type", "application/json; charset=UTF-8");
            if (token != null) peticion.header("X-CSRF-Token", token);
            peticion.POST(
                    HttpRequest.BodyPublishers.ofString(datos.toString(), StandardCharsets.UTF_8));
        } else peticion.GET();
        HttpResponse<byte[]> respuesta =
                cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofByteArray());
        if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
            String mensaje = "No se pudo completar la operación de facturación.";
            try {
                mensaje =
                        JsonParser.parseString(new String(respuesta.body(), StandardCharsets.UTF_8))
                                .getAsJsonObject()
                                .get("error")
                                .getAsString();
            } catch (Exception ignored) {
            }
            throw new IllegalStateException(mensaje);
        }
        return respuesta;
    }

    // Convertimos la respuesta para que el panel pueda leer sus datos.
    private static JsonObject json(HttpResponse<byte[]> r) {
        try {
            return JsonParser.parseString(new String(r.body(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "La web no devolvió una respuesta válida de facturación.", e);
        }
    }

    // Consultamos las cuotas; la web aplica los permisos del usuario.
    public static JsonObject listar(String buscar) throws Exception {
        asegurarSesion();
        return json(
                enviar(
                        "GET",
                        "?accion=listar&q=" + URLEncoder.encode(buscar, StandardCharsets.UTF_8),
                        null,
                        null));
    }

    // Buscamos los pagos de la inscripción elegida.
    public static JsonArray historial(int id) throws Exception {
        asegurarSesion();
        return json(enviar("GET", "?accion=historial&id_inscripcion=" + id, null, null))
                .getAsJsonArray("pagos");
    }

    // El pago, la confirmación y el reenvío se procesan del lado de la web.
    public static JsonObject operar(String accion, JsonObject datos) throws Exception {
        asegurarSesion();
        datos.addProperty("accion", accion);
        return json(enviar("POST", "", datos, csrf));
    }

    // Revisamos que la descarga sea un PDF antes de mandarla al visor.
    public static byte[] factura(int id) throws Exception {
        asegurarSesion();
        byte[] pdf = enviar("GET", "?accion=pdf&id_factura=" + id, null, null).body();
        if (pdf.length < 5 || !new String(pdf, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"))
            throw new IllegalStateException("La factura recibida no es un PDF válido.");
        return pdf;
    }
}

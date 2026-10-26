package servicios;

import clases.Log_Usuario;

import dao.Log_Usuario_dao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// registramos las consultas y acciones de la sesión sin detener la pantalla
public final class Auditoria_servicio {
    private static volatile Integer usuario;
    private static final ExecutorService tareas =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread hilo = new Thread(r, "registro-actividades");
                        hilo.setDaemon(true);
                        return hilo;
                    });

    private Auditoria_servicio() {}

    public static void iniciar(Integer id) {
        usuario = id;
    }

    public static Integer usuarioActual() {
        return usuario;
    }

    // guarda la operación recibida y conserva el registro de la acción
    public static void registrar(String accion, String modulo, String detalle) {
        Integer id = usuario;
        if (id == null) return;
        tareas.submit(
                () -> {
                    Log_Usuario log = new Log_Usuario();
                    log.setId_usuario(id);
                    log.setAccion(accion);
                    log.setCampo_modificado(modulo);
                    log.setValor_anterior("");
                    log.setValor_nuevo(detalle == null ? "" : detalle);
                    new Log_Usuario_dao().insertar(log);
                });
    }

    // esperamos a que termine de escribir para registrar una sola búsqueda
    private static final java.util.Map<String, javax.swing.Timer> busquedas =
            new java.util.HashMap<>();

    public static void buscar(String modulo, String texto) {
        javax.swing.Timer anterior = busquedas.remove(modulo);
        if (anterior != null) {
            anterior.stop();
        }
        javax.swing.Timer espera =
                new javax.swing.Timer(
                        700,
                        e -> {
                            registrar(
                                    "BUSCAR",
                                    modulo,
                                    texto.isBlank() ? "Limpiar búsqueda" : "Buscar: " + texto);
                            busquedas.remove(modulo);
                        });
        espera.setRepeats(false);
        busquedas.put(modulo, espera);
        espera.start();
    }

    public static void cerrar() {
        registrar("CERRAR SESION", "Acceso", "Salida de la aplicación");
        usuario = null;
    }
}

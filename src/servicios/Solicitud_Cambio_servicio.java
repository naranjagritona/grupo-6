package servicios;

import dao.Inscripcion_dao;
import dao.Solicitud_Cambio_dao;
import java.util.*;
import static dao.Consulta_academica_dao.numero;

// validamos los cambios excepcionales antes de enviarlos a dirección o resolverlos
public class Solicitud_Cambio_servicio {
    private final Solicitud_Cambio_dao dao;
    private final Inscripcion_dao inscripciones;

    public Solicitud_Cambio_servicio() {
        this(new Solicitud_Cambio_dao(), new Inscripcion_dao());
    }

    // compartimos la conexión para guardar la solicitud y el traslado sin dejar cambios a medias
    public Solicitud_Cambio_servicio(Solicitud_Cambio_dao dao, Inscripcion_dao inscripciones) {
        this.dao = dao;
        this.inscripciones = inscripciones;
    }

    // registramos la solicitud con su explicación sin cambiar todavía la inscripción del alumno
    public void solicitar(int id, int destino, String observacion, Integer usuario) throws Exception {
        String motivo = Inscripcion_servicio.motivo(observacion);
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            var actual = inscripciones.bloquear(conexion, id);
            Inscripcion_servicio.validarAbierta(actual);
            if ("Pendiente".equals(actual.get("estado"))) throw new IllegalArgumentException("Primero confirmá la inscripción.");
            if (numero(actual, "id_curso") == destino) throw new IllegalArgumentException("Elegí un curso diferente.");
            if (!"Activo".equals(inscripciones.curso(conexion, destino).get("estado"))) throw new IllegalArgumentException("El destino no está activo.");
            if (dao.pendiente(conexion, id)) throw new IllegalArgumentException("La inscripción ya tiene una solicitud pendiente.");
            dao.crear(conexion, id, destino, motivo, usuario);
            return null;
        });
    }

    // consultamos la bandeja para que secretaría pueda seguirla y dirección pueda resolverla
    public List<Map<String, Object>> listar(Integer usuario) throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            return dao.listar(conexion);
        });
    }

    // exigimos un motivo tanto al aprobar como al rechazar y realizamos el traslado si corresponde
    public void resolver(int id, boolean aprobar, String observacion, Integer usuario) throws Exception {
        String motivo = Inscripcion_servicio.motivo(observacion);
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 5);
            var solicitud = dao.bloquear(conexion, id);
            if (!"Pendiente".equals(solicitud.get("estado"))) throw new IllegalArgumentException("La solicitud ya fue resuelta.");
            Integer nueva = null;
            if (aprobar) {
                nueva = new Inscripcion_servicio(inscripciones).trasladar(conexion, numero(solicitud, "id_inscripcion"), numero(solicitud, "id_curso_destino"), solicitud.get("observacion").toString(), usuario, true, "Trasladado");
            }
            dao.resolver(conexion, id, aprobar, usuario, motivo, nueva);
            return null;
        });
    }
}

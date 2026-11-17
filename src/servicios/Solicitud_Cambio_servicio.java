package servicios;

import static dto.Vistas.numero;

import conexion.Conexion;

import dao.Inscripcion_dao;
import dao.Solicitud_Cambio_dao;

import java.util.*;

// validamos los cambios excepcionales antes de enviarlos a dirección o resolverlos
public class Solicitud_Cambio_servicio {
    private final Solicitud_Cambio_dao dao;
    private final Inscripcion_dao inscripciones;
    private final Inscripcion_servicio traslados;

    public Solicitud_Cambio_servicio() {
        this(new Solicitud_Cambio_dao(), new Inscripcion_dao());
    }

    // compartimos la conexión para guardar la solicitud y el traslado sin dejar cambios a medias
    public Solicitud_Cambio_servicio(Solicitud_Cambio_dao dao, Inscripcion_dao inscripciones) {
        this(dao, inscripciones, new Inscripcion_servicio(inscripciones));
    }

    // coordinamos la resolución con el mismo servicio de traslado y su envío posterior al commit
    public Solicitud_Cambio_servicio(
            Solicitud_Cambio_dao dao,
            Inscripcion_dao inscripciones,
            Inscripcion_servicio traslados) {
        this.dao = dao;
        this.inscripciones = inscripciones;
        this.traslados = traslados;
    }

    // registramos la solicitud con su explicación sin cambiar todavía la inscripción del alumno
    public void solicitar(int id, int destino, String observacion, Integer usuario)
            throws Exception {
        String motivo = Inscripcion_servicio.motivo(observacion);
        Conexion.transaccion(
                conexion -> {
                    Permisos_servicio.verificar(
                            conexion, usuario, clases.Rol.SECRETARIA, clases.Rol.DIRECTIVO);
                    var actual =
                            new ConsultasInscripcion_servicio(inscripciones).bloquear(conexion, id);
                    Inscripcion_servicio.validarAbierta(actual);
                    if ("Pendiente".equals(actual.get("estado")))
                        throw new IllegalArgumentException("Primero confirmá la inscripción.");
                    if (numero(actual, "id_curso") == destino)
                        throw new IllegalArgumentException("Elegí un curso diferente.");
                    if (!"Activo"
                            .equals(
                                    new ConsultasInscripcion_servicio(inscripciones)
                                            .curso(conexion, destino)
                                            .get("estado")))
                        throw new IllegalArgumentException("El destino no está activo.");
                    if (dao.pendiente(conexion, id))
                        throw new IllegalArgumentException(
                                "La inscripción ya tiene una solicitud pendiente.");
                    clases.Solicitud_Cambio solicitud = new clases.Solicitud_Cambio();
                    solicitud.setId_inscripcion(id);
                    solicitud.setId_curso_destino(destino);
                    solicitud.setObservacion(motivo);
                    solicitud.setId_solicitante(usuario);
                    dao.crear(conexion, solicitud);
                    return null;
                });
    }

    // consultamos la bandeja para que secretaría pueda seguirla y dirección pueda resolverla
    public List<Map<String, Object>> listar(Integer usuario) throws Exception {
        return Conexion.transaccion(
                conexion -> {
                    Permisos_servicio.verificar(
                            conexion, usuario, clases.Rol.SECRETARIA, clases.Rol.DIRECTIVO);
                    List<Map<String, Object>> filas = new ArrayList<>();
                    for (var solicitud : dao.listar(conexion)) {
                        var fila = dto.Vistas.fila(solicitud);
                        var inscripcion =
                                inscripciones.requerir(conexion, solicitud.getId_inscripcion());
                        var alumno =
                                new dao.Alumno_dao().requerir(conexion, inscripcion.getId_alumno());
                        var origen =
                                new dao.Curso_dao().requerir(conexion, inscripcion.getId_curso());
                        var destino =
                                new dao.Curso_dao()
                                        .requerir(conexion, solicitud.getId_curso_destino());
                        var solicitante =
                                new dao.Usuario_dao()
                                        .requerir(conexion, solicitud.getId_solicitante());
                        fila.put("alumno", alumno.getApellido() + ", " + alumno.getNombre());
                        fila.put("origen", origen.getAño() + "° " + origen.getDivision());
                        fila.put("destino", destino.getAño() + "° " + destino.getDivision());
                        fila.put("solicitante", solicitante.getNombre_usuario());
                        filas.add(fila);
                    }
                    return filas;
                });
    }

    // exigimos un motivo tanto al aprobar como al rechazar y realizamos el traslado si corresponde
    public void resolver(int id, boolean aprobar, String observacion, Integer usuario)
            throws Exception {
        String motivo = Inscripcion_servicio.motivo(observacion);
        Conexion.transaccion(
                conexion -> {
                    Permisos_servicio.verificar(conexion, usuario, clases.Rol.DIRECTIVO);
                    var solicitud = dao.bloquear(conexion, id);
                    if (!"Pendiente".equals(solicitud.getEstado()))
                        throw new IllegalArgumentException("La solicitud ya fue resuelta.");
                    Integer nueva = null;
                    if (aprobar) {
                        nueva =
                                traslados.trasladar(
                                        conexion,
                                        solicitud.getId_inscripcion(),
                                        solicitud.getId_curso_destino(),
                                        solicitud.getObservacion(),
                                        usuario,
                                        true,
                                        "Trasladado");
                    }
                    dao.resolver(conexion, id, aprobar, usuario, motivo, nueva);
                    return null;
                });
        if (aprobar) traslados.enviarAvisos(usuario);
    }
}

package clases;

// Representa una fila de la tabla solicitud_cambio.
public class Solicitud_Cambio {
    private Integer id_solicitud;
    private Integer id_inscripcion;
    private Integer id_curso_destino;
    private String observacion;
    private String estado;
    private Integer id_solicitante;
    private Integer id_directivo;
    private String motivo_resolucion;
    private java.time.LocalDateTime fecha_solicitud;
    private java.time.LocalDateTime fecha_resolucion;
    private Integer id_inscripcion_nueva;

    public Integer getId_solicitud() {
        return id_solicitud;
    }

    public void setId_solicitud(Integer id_solicitud) {
        this.id_solicitud = id_solicitud;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public Integer getId_curso_destino() {
        return id_curso_destino;
    }

    public void setId_curso_destino(Integer id_curso_destino) {
        this.id_curso_destino = id_curso_destino;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getId_solicitante() {
        return id_solicitante;
    }

    public void setId_solicitante(Integer id_solicitante) {
        this.id_solicitante = id_solicitante;
    }

    public Integer getId_directivo() {
        return id_directivo;
    }

    public void setId_directivo(Integer id_directivo) {
        this.id_directivo = id_directivo;
    }

    public String getMotivo_resolucion() {
        return motivo_resolucion;
    }

    public void setMotivo_resolucion(String motivo_resolucion) {
        this.motivo_resolucion = motivo_resolucion;
    }

    public java.time.LocalDateTime getFecha_solicitud() {
        return fecha_solicitud;
    }

    public void setFecha_solicitud(java.time.LocalDateTime fecha_solicitud) {
        this.fecha_solicitud = fecha_solicitud;
    }

    public java.time.LocalDateTime getFecha_resolucion() {
        return fecha_resolucion;
    }

    public void setFecha_resolucion(java.time.LocalDateTime fecha_resolucion) {
        this.fecha_resolucion = fecha_resolucion;
    }

    public Integer getId_inscripcion_nueva() {
        return id_inscripcion_nueva;
    }

    public void setId_inscripcion_nueva(Integer id_inscripcion_nueva) {
        this.id_inscripcion_nueva = id_inscripcion_nueva;
    }
}

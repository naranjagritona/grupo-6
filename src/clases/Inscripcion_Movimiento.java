package clases;

// Representa una fila de la tabla inscripcion_movimiento.
public class Inscripcion_Movimiento {
    private Integer id_movimiento;
    private Integer id_inscripcion;
    private String estado_anterior;
    private String estado_nuevo;
    private String motivo;
    private Integer id_usuario;
    private java.time.LocalDateTime fecha;

    public Integer getId_movimiento() {
        return id_movimiento;
    }

    public void setId_movimiento(Integer id_movimiento) {
        this.id_movimiento = id_movimiento;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public String getEstado_anterior() {
        return estado_anterior;
    }

    public void setEstado_anterior(String estado_anterior) {
        this.estado_anterior = estado_anterior;
    }

    public String getEstado_nuevo() {
        return estado_nuevo;
    }

    public void setEstado_nuevo(String estado_nuevo) {
        this.estado_nuevo = estado_nuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }
}

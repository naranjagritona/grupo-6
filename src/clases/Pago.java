package clases;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Guardamos los datos de un pago. Por ahora esta clase no se usa en las pantallas.
public class Pago {
    private Integer id_pago;
    private Integer id_inscripcion;
    private String periodo;
    private BigDecimal importe;
    private String metodo;
    private String estado;
    private LocalDateTime fecha_pago;
    private String email;
    private String dni;
    private String tramitante;

    public Pago() {
    }

    public Integer getId_pago() {
        return id_pago;
    }

    public void setId_pago(Integer id_pago) {
        this.id_pago = id_pago;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha_pago() {
        return fecha_pago;
    }

    public void setFecha_pago(LocalDateTime fecha_pago) {
        this.fecha_pago = fecha_pago;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTramitante() {
        return tramitante;
    }

    public void setTramitante(String tramitante) {
        this.tramitante = tramitante;
    }
}

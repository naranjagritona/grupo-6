package clases;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// guardamos los datos de un pago sin conectarlos a las pantallas
public class Pago_Cuota {
    private Integer id_pago;
    private Integer id_inscripcion;
    private String periodo;
    private BigDecimal importe;
    private String metodo;
    private String estado;
    private LocalDateTime fecha;
    private String email;
    private String dni;
    private String tramitante;
    private String titular;
    private Integer id_administrador;
    private String referencia;
    private String atendido_por;
    private Integer id_usuario_registro;
    private Integer confirmado_por;
    private LocalDateTime fecha_confirmacion;

    public Pago_Cuota() {}

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public Integer getId_administrador() {
        return id_administrador;
    }

    public void setId_administrador(Integer id_administrador) {
        this.id_administrador = id_administrador;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getAtendido_por() {
        return atendido_por;
    }

    public void setAtendido_por(String atendido_por) {
        this.atendido_por = atendido_por;
    }

    public Integer getId_usuario_registro() {
        return id_usuario_registro;
    }

    public void setId_usuario_registro(Integer id_usuario_registro) {
        this.id_usuario_registro = id_usuario_registro;
    }

    public Integer getConfirmado_por() {
        return confirmado_por;
    }

    public void setConfirmado_por(Integer confirmado_por) {
        this.confirmado_por = confirmado_por;
    }

    public LocalDateTime getFecha_confirmacion() {
        return fecha_confirmacion;
    }

    public void setFecha_confirmacion(LocalDateTime fecha_confirmacion) {
        this.fecha_confirmacion = fecha_confirmacion;
    }
}

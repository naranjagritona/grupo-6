package clases;
import java.time.LocalDateTime;

public class Log_Usuario {
    private Integer id_log_usuario;
    private Integer id_usuario;
    private String accion;
    private String campo_modificado;
    private String valor_anterior;
    private String valor_nuevo;
    private LocalDateTime fecha_hora;

    public Log_Usuario() {
    }

    public Log_Usuario(Integer id_log_usuario, Integer id_usuario, String accion, String campo_modificado, String valor_anterior, String valor_nuevo, LocalDateTime fecha_hora) {
        this.id_log_usuario = id_log_usuario;
        this.id_usuario = id_usuario;
        this.accion = accion;
        this.campo_modificado = campo_modificado;
        this.valor_anterior = valor_anterior;
        this.valor_nuevo = valor_nuevo;
        this.fecha_hora = fecha_hora;
    }

    public Integer getId_log_usuario() {
        return id_log_usuario;
    }

    public void setId_log_usuario(Integer id_log_usuario) {
        this.id_log_usuario = id_log_usuario;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getCampo_modificado() {
        return campo_modificado;
    }

    public void setCampo_modificado(String campo_modificado) {
        this.campo_modificado = campo_modificado;
    }

    public String getValor_anterior() {
        return valor_anterior;
    }

    public void setValor_anterior(String valor_anterior) {
        this.valor_anterior = valor_anterior;
    }

    public String getValor_nuevo() {
        return valor_nuevo;
    }

    public void setValor_nuevo(String valor_nuevo) {
        this.valor_nuevo = valor_nuevo;
    }

    public LocalDateTime getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(LocalDateTime fecha_hora) {
        this.fecha_hora = fecha_hora;
    }
}
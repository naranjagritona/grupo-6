package clases;

import java.time.LocalDateTime;

// representamos un aviso de la tabla notificacion_correo y su estado de envío
public class Notificacion_Correo {
    private Integer id_notificacion, id_usuario;
    private String destinatario, tipo, clave_evento, asunto, mensaje, estado = "Pendiente";
    private int intentos;
    private LocalDateTime creado, ultimo_intento, enviado;

    public Integer getId_notificacion() { return id_notificacion; }
    public void setId_notificacion(Integer valor) { id_notificacion = valor; }
    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer valor) { id_usuario = valor; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String valor) { destinatario = valor; }
    public String getTipo() { return tipo; }
    public void setTipo(String valor) { tipo = valor; }
    public String getClave_evento() { return clave_evento; }
    public void setClave_evento(String valor) { clave_evento = valor; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String valor) { asunto = valor; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String valor) { mensaje = valor; }
    public String getEstado() { return estado; }
    public void setEstado(String valor) { estado = valor; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int valor) { intentos = valor; }
    public LocalDateTime getCreado() { return creado; }
    public void setCreado(LocalDateTime valor) { creado = valor; }
    public LocalDateTime getUltimo_intento() { return ultimo_intento; }
    public void setUltimo_intento(LocalDateTime valor) { ultimo_intento = valor; }
    public LocalDateTime getEnviado() { return enviado; }
    public void setEnviado(LocalDateTime valor) { enviado = valor; }
}

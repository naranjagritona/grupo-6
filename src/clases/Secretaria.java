package clases;

// Representa una fila de la tabla secretaria.
public class Secretaria {
    private Integer id_secretaria;
    private Integer id_usuario;
    private String nombre;
    private String apellido;
    private String dni;
    private String mail;
    private String legajo;
    private String estado;

    public Integer getId_secretaria() {
        return id_secretaria;
    }

    public void setId_secretaria(Integer id_secretaria) {
        this.id_secretaria = id_secretaria;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

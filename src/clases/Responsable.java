package clases;

public class Responsable {
    private Integer id_responsable;
    private String nombre;
    private String apellido;
    private String dni;
    private String tipo_relacion;

    public Responsable() {
    }

    public Responsable(Integer id_responsable, String nombre, String apellido, String dni, String tipo_relacion) {
        this.id_responsable = id_responsable;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.tipo_relacion = tipo_relacion;
    }

    public Integer getId_responsable() {
        return id_responsable;
    }

    public void setId_responsable(Integer id_responsable) {
        this.id_responsable = id_responsable;
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

    public String getTipo_relacion() {
        return tipo_relacion;
    }

    public void setTipo_relacion(String tipo_relacion) {
        this.tipo_relacion = tipo_relacion;
    }
}
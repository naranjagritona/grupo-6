package clases;

public class Rol {
    private Integer id_rol;
    private String nombre;
    private String descripcion_rol;

    public Rol() {
    }

    public Rol(Integer id_rol, String nombre, String descripcion_rol) {
        this.id_rol = id_rol;
        this.nombre = nombre;
        this.descripcion_rol = descripcion_rol;
    }

    public Integer getId_rol() {
        return id_rol;
    }

    public void setId_rol(Integer id_rol) {
        this.id_rol = id_rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion_rol() {
        return descripcion_rol;
    }

    public void setDescripcion_rol(String descripcion_rol) {
        this.descripcion_rol = descripcion_rol;
    }
}
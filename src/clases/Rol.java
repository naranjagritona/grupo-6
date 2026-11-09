package clases;

// identificamos el tipo de cuenta y sus permisos dentro del sistema
public class Rol {
    // identificamos los mismos roles que utiliza la aplicación web y la base compartida
    public static final int DIRECTIVO = 1;
    public static final int SECRETARIA = 2;
    public static final int DOCENTE = 3;
    public static final int ALUMNO = 4;
    public static final int COOPERADORA = 5;

    private Integer id_rol;
    private String nombre;
    private String descripcion_rol;

    public Rol() {}

    public Rol(Integer id_rol, String nombre, String descripcion_rol) {
        this.id_rol = id_rol;
        this.nombre = nombre;
        this.descripcion_rol = descripcion_rol;
    }

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
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

    @Override
    public String toString() { return nombre; }
}

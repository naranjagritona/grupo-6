package clases;

// guardamos el identificador y el nombre de una materia
public class Materia {
    private Integer id_materia;
    private String nombre_materia;

    public Materia() {}

    public Materia(Integer id_materia, String nombre_materia) {
        this.id_materia = id_materia;
        this.nombre_materia = nombre_materia;
    }

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
    public Integer getId_materia() {
        return id_materia;
    }

    public void setId_materia(Integer id_materia) {
        this.id_materia = id_materia;
    }

    public String getNombre_materia() {
        return nombre_materia;
    }

    public void setNombre_materia(String nombre_materia) {
        this.nombre_materia = nombre_materia;
    }
}

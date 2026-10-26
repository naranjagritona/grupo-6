package clases;

// mostramos un texto en el selector y conservamos el identificador para las consultas
public class ItemCombo {

    private Integer id;
    private String texto;

    public ItemCombo(Integer id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
    public Integer getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}

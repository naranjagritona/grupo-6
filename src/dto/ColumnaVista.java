package dto;

import clases.Columna_Notas;

// Datos combinados para mostrar en pantalla, no representa una tabla adicional.
public class ColumnaVista extends Columna_Notas {
    private String nombre_materia;

    public ColumnaVista(Columna_Notas columna, String materia) {
        super(
                columna.getId_columna_notas(),
                columna.getId_curso(),
                columna.getTitulo(),
                columna.getTipo(),
                columna.getNota_maxima(),
                columna.getNota_aprobacion(),
                columna.getFecha_evaluacion(),
                columna.getFecha_cierre(),
                columna.getEstado());
        setId_materia(columna.getId_materia());
        setNombre_materia(materia);
    }

    public String getNombre_materia() {
        return nombre_materia;
    }

    public void setNombre_materia(String nombre) {
        nombre_materia = nombre;
    }
}

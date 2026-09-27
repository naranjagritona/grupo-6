package clases;
import java.time.LocalDate;

public class Calificacion {
    private Integer id_calificacion;
    private Integer id_inscripcion;
    private Integer id_columna_notas;
    private Double nota;
    private LocalDate fecha_carga;
    private String observaciones;

    public Calificacion() {
    }

    public Calificacion(Integer id_calificacion, Integer id_inscripcion, Integer id_columna_notas, Double nota, LocalDate fecha_carga, String observaciones) {
        this.id_calificacion = id_calificacion;
        this.id_inscripcion = id_inscripcion;
        this.id_columna_notas = id_columna_notas;
        this.nota = nota;
        this.fecha_carga = fecha_carga;
        this.observaciones = observaciones;
    }

    public Integer getId_calificacion() {
        return id_calificacion;
    }

    public void setId_calificacion(Integer id_calificacion) {
        this.id_calificacion = id_calificacion;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public Integer getId_columna_notas() {
        return id_columna_notas;
    }

    public void setId_columna_notas(Integer id_columna_notas) {
        this.id_columna_notas = id_columna_notas;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public LocalDate getFecha_carga() {
        return fecha_carga;
    }

    public void setFecha_carga(LocalDate fecha_carga) {
        this.fecha_carga = fecha_carga;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
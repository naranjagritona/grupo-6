package clases;

import java.time.LocalDate;

public class Columna_Notas {
    private Integer id_columna_notas;
    private Integer id_curso;
    private String titulo;  
    private String tipo;
    private Double nota_maxima;
    private Double nota_aprobacion;
    private LocalDate fecha_evaluacion;
    private LocalDate fecha_cierre;
    private String estado;

    public Columna_Notas() {
    }

    public Columna_Notas(Integer id_columna_notas, Integer id_curso, String titulo, String tipo, Double nota_maxima, Double nota_aprobacion, LocalDate fecha_evaluacion, LocalDate fecha_cierre, String estado) {
        this.id_columna_notas = id_columna_notas;
        this.id_curso = id_curso;
        this.titulo = titulo;
        this.tipo = tipo;
        this.nota_maxima = nota_maxima;
        this.nota_aprobacion = nota_aprobacion;
        this.fecha_evaluacion = fecha_evaluacion;
        this.fecha_cierre = fecha_cierre;
        this.estado = estado;
    }

    public Integer getId_columna_notas() {
        return id_columna_notas;
    }

    public void setId_columna_notas(Integer id_columna_notas) {
        this.id_columna_notas = id_columna_notas;
    }

    public Integer getId_curso() {
        return id_curso;
    }

    public void setId_curso(Integer id_curso) {
        this.id_curso = id_curso;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getNota_maxima() {
        return nota_maxima;
    }

    public void setNota_maxima(Double nota_maxima) {
        this.nota_maxima = nota_maxima;
    }

    public Double getNota_aprobacion() {
        return nota_aprobacion;
    }

    public void setNota_aprobacion(Double nota_aprobacion) {
        this.nota_aprobacion = nota_aprobacion;
    }

    public LocalDate getFecha_evaluacion() {
        return fecha_evaluacion;
    }

    public void setFecha_evaluacion(LocalDate fecha_evaluacion) {
        this.fecha_evaluacion = fecha_evaluacion;
    }

    public LocalDate getFecha_cierre() {
        return fecha_cierre;
    }

    public void setFecha_cierre(LocalDate fecha_cierre) {
        this.fecha_cierre = fecha_cierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
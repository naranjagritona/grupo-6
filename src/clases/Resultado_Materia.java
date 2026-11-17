package clases;

import java.time.LocalDateTime;

// guardamos el resultado final de la materia para una inscripción
public class Resultado_Materia {
    private Integer id_resultado_materia;
    private Integer id_inscripcion;
    private Double promedio;
    private String resultado;
    private LocalDateTime fecha_calculo;
    private Integer id_materia;
    private Integer id_usuario;
    private String observacion;

    public Resultado_Materia() {}

    public Resultado_Materia(
            Integer id_resultado_materia,
            Integer id_inscripcion,
            Double promedio,
            String resultado,
            LocalDateTime fecha_calculo) {
        this.id_resultado_materia = id_resultado_materia;
        this.id_inscripcion = id_inscripcion;
        this.promedio = promedio;
        this.resultado = resultado;
        this.fecha_calculo = fecha_calculo;
    }

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
    public Integer getId_resultado_materia() {
        return id_resultado_materia;
    }

    public void setId_resultado_materia(Integer id_resultado_materia) {
        this.id_resultado_materia = id_resultado_materia;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public LocalDateTime getFecha_calculo() {
        return fecha_calculo;
    }

    public void setFecha_calculo(LocalDateTime fecha_calculo) {
        this.fecha_calculo = fecha_calculo;
    }

    public Integer getId_materia() {
        return id_materia;
    }

    public void setId_materia(Integer id_materia) {
        this.id_materia = id_materia;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

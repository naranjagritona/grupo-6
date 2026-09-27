package clases;
import java.time.LocalDate;

public class Resultado_Materia {
    private Integer id_resultado;
    private Integer id_inscripcion;
    private Double promedio;
    private String resultado;
    private LocalDate fecha_calculo;

    public Resultado_Materia() {
    }

    public Resultado_Materia(Integer id_resultado, Integer id_inscripcion, Double promedio, String resultado, LocalDate fecha_calculo) {
        this.id_resultado = id_resultado;
        this.id_inscripcion = id_inscripcion;
        this.promedio = promedio;
        this.resultado = resultado;
        this.fecha_calculo = fecha_calculo;
    }

    public Integer getId_resultado() {
        return id_resultado;
    }

    public void setId_resultado(Integer id_resultado) {
        this.id_resultado = id_resultado;
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

    public LocalDate getFecha_calculo() {
        return fecha_calculo;
    }

    public void setFecha_calculo(LocalDate fecha_calculo) {
        this.fecha_calculo = fecha_calculo;
    }
}
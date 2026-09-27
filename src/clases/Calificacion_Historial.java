package clases;
import java.time.LocalDateTime;

public class Calificacion_Historial {
    private Integer id_historial;
    private Integer id_calificacion;
    private Double nota_anterior;
    private Double nota_nueva;
    private LocalDateTime fecha_modificacion;
    private String motivo;

    public Calificacion_Historial() {
    }

    public Calificacion_Historial(Integer id_historial, Integer id_calificacion, Double nota_anterior, Double nota_nueva, LocalDateTime fecha_modificacion, String motivo) {
        this.id_historial = id_historial;
        this.id_calificacion = id_calificacion;
        this.nota_anterior = nota_anterior;
        this.nota_nueva = nota_nueva;
        this.fecha_modificacion = fecha_modificacion;
        this.motivo = motivo;
    }

    public Integer getId_historial() {
        return id_historial;
    }

    public void setId_historial(Integer id_historial) {
        this.id_historial = id_historial;
    }

    public Integer getId_calificacion() {
        return id_calificacion;
    }

    public void setId_calificacion(Integer id_calificacion) {
        this.id_calificacion = id_calificacion;
    }

    public Double getNota_anterior() {
        return nota_anterior;
    }

    public void setNota_anterior(Double nota_anterior) {
        this.nota_anterior = nota_anterior;
    }

    public Double getNota_nueva() {
        return nota_nueva;
    }

    public void setNota_nueva(Double nota_nueva) {
        this.nota_nueva = nota_nueva;
    }

    public LocalDateTime getFecha_modificacion() {
        return fecha_modificacion;
    }

    public void setFecha_modificacion(LocalDateTime fecha_modificacion) {
        this.fecha_modificacion = fecha_modificacion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
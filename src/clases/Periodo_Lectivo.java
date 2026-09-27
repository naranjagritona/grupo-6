package clases;
import java.time.LocalDate;

public class Periodo_Lectivo {
    private Integer id_periodo;
    private String nombre;
    private String tipo;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private String estado;

    public Periodo_Lectivo() {
    }

    public Periodo_Lectivo(Integer id_periodo, String nombre, String tipo, LocalDate fecha_inicio, LocalDate fecha_fin, String estado) {
        this.id_periodo = id_periodo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.estado = estado;
    }

    public Integer getId_periodo() {
        return id_periodo;
    }

    public void setId_periodo(Integer id_periodo) {
        this.id_periodo = id_periodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(LocalDate fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public LocalDate getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(LocalDate fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
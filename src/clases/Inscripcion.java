package clases;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Inscripcion {
    private Integer id_inscripcion;
    private Integer id_alumno;
    private Integer id_curso;
    private LocalDate fecha_inscripcion;
    private String estado;
    private String motivo_baja;
    private Double importe_cuota;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_modificacion;

    public Inscripcion() {
    }

    public Inscripcion(Integer id_inscripcion, Integer id_alumno, Integer id_curso, LocalDate fecha_inscripcion, String estado, String motivo_baja, Double importe_cuota, LocalDateTime fecha_creacion, LocalDateTime fecha_modificacion) {
        this.id_inscripcion = id_inscripcion;
        this.id_alumno = id_alumno;
        this.id_curso = id_curso;
        this.fecha_inscripcion = fecha_inscripcion;
        this.estado = estado;
        this.motivo_baja = motivo_baja;
        this.importe_cuota = importe_cuota;
        this.fecha_creacion = fecha_creacion;
        this.fecha_modificacion = fecha_modificacion;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public Integer getId_alumno() {
        return id_alumno;
    }

    public void setId_alumno(Integer id_alumno) {
        this.id_alumno = id_alumno;
    }

    public Integer getId_curso() {
        return id_curso;
    }

    public void setId_curso(Integer id_curso) {
        this.id_curso = id_curso;
    }

    public LocalDate getFecha_inscripcion() {
        return fecha_inscripcion;
    }

    public void setFecha_inscripcion(LocalDate fecha_inscripcion) {
        this.fecha_inscripcion = fecha_inscripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo_baja() {
        return motivo_baja;
    }

    public void setMotivo_baja(String motivo_baja) {
        this.motivo_baja = motivo_baja;
    }

    public Double getImporte_cuota() {
        return importe_cuota;
    }

    public void setImporte_cuota(Double importe_cuota) {
        this.importe_cuota = importe_cuota;
    }

    public LocalDateTime getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(LocalDateTime fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public LocalDateTime getFecha_modificacion() {
        return fecha_modificacion;
    }

    public void setFecha_modificacion(LocalDateTime fecha_modificacion) {
        this.fecha_modificacion = fecha_modificacion;
    }
}
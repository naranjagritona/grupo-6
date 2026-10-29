package clases;

import java.time.LocalDate;

// guardamos el año, el docente y los datos que identifican al curso
public class Curso {
    private Integer id_curso;
    private Integer id_periodo;
    // la materia original se conserva para las evaluaciones; las demás van en curso_materia.
    private Integer id_materia;
    private Integer id_docente;
    private Integer año;
    // guardamos la división y las condiciones de aprobación del curso
    private String division = "Sin asignar";
    private Double nota_aprobacion_materia;
    private Double promedio_promocion;
    private String criterio_recuperatorio;

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public Double getNota_aprobacion_materia() { return nota_aprobacion_materia; }
    public void setNota_aprobacion_materia(Double nota) { this.nota_aprobacion_materia = nota; }
    public Double getPromedio_promocion() { return promedio_promocion; }
    public void setPromedio_promocion(Double promedio) { this.promedio_promocion = promedio; }
    public String getCriterio_recuperatorio() { return criterio_recuperatorio; }
    public void setCriterio_recuperatorio(String criterio) { this.criterio_recuperatorio = criterio; }
    private String modalidad;
    private String turno;
    private Integer cupo_maximo;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private String estado;
    // estos nombres se completan al consultar para mostrarlos directamente en la tabla.
    private String nombre_periodo;
    private String nombre_materia;
    private String nombre_docente;

    public Curso() {}

    public Curso(
            Integer id_curso,
            Integer id_periodo,
            Integer id_materia,
            Integer id_docente,
            Integer año,
            String modalidad,
            String turno,
            Integer cupo_maximo,
            LocalDate fecha_inicio,
            LocalDate fecha_fin,
            String estado) {
        this.id_curso = id_curso;
        this.id_periodo = id_periodo;
        this.id_materia = id_materia;
        this.id_docente = id_docente;
        this.año = año;
        this.modalidad = modalidad;
        this.turno = turno;
        this.cupo_maximo = cupo_maximo;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.estado = estado;
    }

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
    public Integer getId_curso() {
        return id_curso;
    }

    public void setId_curso(Integer id_curso) {
        this.id_curso = id_curso;
    }

    public Integer getId_periodo() {
        return id_periodo;
    }

    public void setId_periodo(Integer id_periodo) {
        this.id_periodo = id_periodo;
    }

    public Integer getId_materia() {
        return id_materia;
    }

    public void setId_materia(Integer id_materia) {
        this.id_materia = id_materia;
    }

    public Integer getId_docente() {
        return id_docente;
    }

    public void setId_docente(Integer id_docente) {
        this.id_docente = id_docente;
    }

    public Integer getAño() {
        return año;
    }

    public void setAño(Integer año) {
        this.año = año;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Integer getCupo_maximo() {
        return cupo_maximo;
    }

    public void setCupo_maximo(Integer cupo_maximo) {
        this.cupo_maximo = cupo_maximo;
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

    public String getNombre_periodo() {
        return nombre_periodo;
    }

    public void setNombre_periodo(String nombre_periodo) {
        this.nombre_periodo = nombre_periodo;
    }

    public String getNombre_materia() {
        return nombre_materia;
    }

    public void setNombre_materia(String nombre_materia) {
        this.nombre_materia = nombre_materia;
    }

    public String getNombre_docente() {
        return nombre_docente;
    }

    public void setNombre_docente(String nombre_docente) {
        this.nombre_docente = nombre_docente;
    }
}

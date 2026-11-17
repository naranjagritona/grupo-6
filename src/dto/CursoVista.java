package dto;

import clases.Curso;

// Datos combinados para mostrar en pantalla, no representa una tabla adicional.
public class CursoVista extends Curso {
    private String nombre_periodo;
    private String nombre_materia;
    private String nombre_docente;
    private int cantidad_materias;

    public CursoVista() {}

    public CursoVista(Curso curso) {
        super(
                curso.getId_curso(),
                curso.getId_periodo(),
                curso.getId_materia(),
                curso.getId_docente(),
                curso.getAño(),
                curso.getModalidad(),
                curso.getTurno(),
                curso.getCupo_maximo(),
                curso.getFecha_inicio(),
                curso.getFecha_fin(),
                curso.getEstado());
        setDivision(curso.getDivision());
        setNota_aprobacion_materia(curso.getNota_aprobacion_materia());
        setPromedio_promocion(curso.getPromedio_promocion());
        setCriterio_recuperatorio(curso.getCriterio_recuperatorio());
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

    public int getCantidad_materias() {
        return cantidad_materias;
    }

    public void setCantidad_materias(int cantidad) {
        this.cantidad_materias = cantidad;
    }
}

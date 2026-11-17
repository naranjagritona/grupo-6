package servicios;

import clases.*;

import dao.*;

import dto.Vistas;

import java.sql.*;
import java.util.*;

// Arma los datos de las pantallas a partir de las entidades de cada tabla.
public class ConsultasInscripcion_servicio {
    private final Inscripcion_dao inscripciones;

    public ConsultasInscripcion_servicio() {
        this(new Inscripcion_dao());
    }

    public ConsultasInscripcion_servicio(Inscripcion_dao inscripciones) {
        this.inscripciones = inscripciones;
    }

    public Map<String, Object> bloquear(Connection db, int id) throws SQLException {
        Inscripcion i = inscripciones.requerir(db, id);
        Curso curso = new Curso_dao().requerir(db, i.getId_curso());
        Alumno alumno = new Alumno_dao().requerir(db, i.getId_alumno());
        var fila = Vistas.fila(i);
        fila.put("año", curso.getAño());
        fila.put("division", curso.getDivision());
        fila.put("id_periodo", curso.getId_periodo());
        fila.put("estado_alumno", alumno.getEstado());
        return fila;
    }

    public Map<String, Object> curso(Connection db, int id) throws SQLException {
        Curso curso = new Curso_dao().requerir(db, id);
        Periodo_Lectivo periodo = new Periodo_Lectivo_dao().requerir(db, curso.getId_periodo());
        var fila = Vistas.fila(curso);
        fila.put("inicio_periodo", java.sql.Date.valueOf(periodo.getFecha_inicio()));
        return fila;
    }

    public List<Map<String, Object>> alumnosPromocion(Connection db, int curso)
            throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Inscripcion i : inscripciones.delCurso(db, curso)) {
            if (!List.of("Confirmada", "Recuperacion").contains(i.getEstado())) continue;
            Alumno alumno = new Alumno_dao().requerir(db, i.getId_alumno());
            filas.add(
                    Map.of(
                            "id_inscripcion",
                            i.getId_inscripcion(),
                            "alumno",
                            alumno.getApellido() + ", " + alumno.getNombre()));
        }
        return filas;
    }

    public List<Map<String, Object>> resultados(Connection db, int id) throws SQLException {
        Inscripcion i = inscripciones.requerir(db, id);
        List<Resultado_Materia> resultados = new Resultado_Materia_dao().deInscripcion(db, id);
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Curso_Materia relacion : new Curso_Materia_dao().delCurso(db, i.getId_curso())) {
            Materia materia = new Materia_dao().requerir(db, relacion.getId_materia());
            var resultado =
                    resultados.stream()
                            .filter(r -> Objects.equals(r.getId_materia(), materia.getId_materia()))
                            .findFirst()
                            .orElse(null);
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("id_materia", materia.getId_materia());
            fila.put("nombre_materia", materia.getNombre_materia());
            fila.put("promedio", resultado == null ? null : resultado.getPromedio());
            fila.put("resultado", resultado == null ? null : resultado.getResultado());
            filas.add(fila);
        }
        filas.sort(Comparator.comparing(f -> f.get("nombre_materia").toString()));
        return filas;
    }

    public Map<String, Object> ficha(Connection db, int id) throws SQLException {
        Inscripcion i = inscripciones.requerir(db, id);
        Alumno alumno = new Alumno_dao().requerir(db, i.getId_alumno());
        Curso curso = new Curso_dao().requerir(db, i.getId_curso());
        Periodo_Lectivo periodo = new Periodo_Lectivo_dao().requerir(db, curso.getId_periodo());
        var fila = Vistas.fila(i);
        fila.put("nombre", alumno.getNombre());
        fila.put("apellido", alumno.getApellido());
        fila.put("dni", alumno.getDni());
        fila.put("matricula", alumno.getMatricula());
        fila.put("año", curso.getAño());
        fila.put("division", curso.getDivision());
        fila.put("turno", curso.getTurno());
        fila.put("periodo", periodo.getNombre());
        return fila;
    }

    public List<Map<String, Object>> notas(Connection db, int id) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Calificacion nota : new Calificacion_dao().deInscripcion(db, id)) {
            Columna_Notas columna =
                    new Columna_Notas_dao().requerir(db, nota.getId_columna_notas());
            Materia materia = new Materia_dao().requerir(db, columna.getId_materia());
            var fila = Vistas.fila(nota);
            fila.put("materia", materia.getNombre_materia());
            fila.put("titulo", columna.getTitulo());
            fila.put("tipo", columna.getTipo());
            fila.put(
                    "fecha_evaluacion",
                    columna.getFecha_evaluacion() == null
                            ? null
                            : java.sql.Date.valueOf(columna.getFecha_evaluacion()));
            filas.add(fila);
        }
        filas.sort(
                Comparator.comparing((Map<String, Object> f) -> f.get("materia").toString())
                        .thenComparing(f -> Objects.toString(f.get("fecha_evaluacion"), "")));
        return filas;
    }

    public List<Map<String, Object>> movimientos(Connection db, int id) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Inscripcion_Movimiento movimiento :
                new Inscripcion_Movimiento_dao().deInscripcion(db, id)) {
            var fila = Vistas.fila(movimiento);
            Usuario usuario = new Usuario_dao().buscar(db, movimiento.getId_usuario());
            fila.put("usuario", usuario == null ? null : usuario.getNombre_usuario());
            filas.add(fila);
        }
        return filas;
    }
}

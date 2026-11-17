package servicios;

import clases.*;

import dao.*;

import dto.Vistas;

import java.sql.*;
import java.util.*;

public class ConsultasResultado_servicio {
    public boolean asignado(Connection db, int curso, int materia, int usuario)
            throws SQLException {
        return new Curso_Materia_dao().asignado(db, curso, materia, usuario);
    }

    public List<Map<String, Object>> materiasDocente(Connection db, int curso, int usuario)
            throws SQLException {
        return materias(db, curso, usuario);
    }

    public List<Map<String, Object>> materias(Connection db, int curso) throws SQLException {
        return materias(db, curso, null);
    }

    private List<Map<String, Object>> materias(Connection db, int curso, Integer usuario)
            throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Curso_Materia relacion : new Curso_Materia_dao().delCurso(db, curso)) {
            if (usuario != null && !asignado(db, curso, relacion.getId_materia(), usuario))
                continue;
            filas.add(Vistas.fila(new Materia_dao().requerir(db, relacion.getId_materia())));
        }
        filas.sort(Comparator.comparing(f -> f.get("nombre_materia").toString()));
        return filas;
    }

    public List<Map<String, Object>> notas(Connection db, int inscripcion, int curso, int materia)
            throws SQLException {
        List<Calificacion> notas = new Calificacion_dao().deInscripcion(db, inscripcion);
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Columna_Notas columna : new Columna_Notas_dao().delCurso(db, curso)) {
            if (!Objects.equals(columna.getId_materia(), materia)) continue;
            var nota =
                    notas.stream()
                            .filter(
                                    n ->
                                            Objects.equals(
                                                    n.getId_columna_notas(),
                                                    columna.getId_columna_notas()))
                            .findFirst()
                            .orElse(null);
            var fila = Vistas.fila(columna);
            fila.put("nota", nota == null ? null : nota.getNota());
            filas.add(fila);
        }
        filas.sort(Comparator.comparing(f -> Objects.toString(f.get("fecha_evaluacion"), "")));
        return filas;
    }

    public void guardar(
            Connection db,
            int inscripcion,
            int materia,
            java.math.BigDecimal promedio,
            String resultado,
            String observacion,
            int usuario)
            throws SQLException {
        Resultado_Materia r = new Resultado_Materia();
        r.setId_inscripcion(inscripcion);
        r.setId_materia(materia);
        r.setPromedio(promedio.doubleValue());
        r.setResultado(resultado);
        r.setObservacion(observacion);
        r.setId_usuario(usuario);
        new Resultado_Materia_dao().guardar(db, r);
    }
}

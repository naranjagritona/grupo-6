package servicios;

import dao.Inscripcion_dao;
import dao.Resultado_Academico_dao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import static dao.Consulta_academica_dao.numero;

// calculamos el resultado de cada materia con las notas y las condiciones definidas por dirección
public class Resultado_Academico_servicio {
    private final Resultado_Academico_dao dao;
    private final Inscripcion_dao inscripciones;

    public Resultado_Academico_servicio() { this(new Resultado_Academico_dao(), new Inscripcion_dao()); }

    // compartimos los daos que validan la inscripción y calculan el resultado por materia
    public Resultado_Academico_servicio(Resultado_Academico_dao dao, Inscripcion_dao inscripciones) {
        this.dao = dao; this.inscripciones = inscripciones;
    }

    // consultamos las materias disponibles para el docente del curso
    public List<Map<String, Object>> materias(int curso, Integer usuario) throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 2);
            if (dao.docente(conexion, curso) != usuario) throw new IllegalArgumentException("El curso no corresponde al docente.");
            return dao.materias(conexion, curso);
        });
    }

    // calculamos el promedio de acuerdo con la forma de contar los recuperatorios
    public static double promedio(List<Double> notas, List<Double> recuperatorios, String criterio) {
        List<Double> valores = new ArrayList<>(notas);
        if ("Promediar".equals(criterio)) valores.addAll(recuperatorios);
        else if ("Reemplazar menor".equals(criterio)) {
            for (double nota : recuperatorios) {
                if (!valores.isEmpty()) {
                    int indice = valores.indexOf(Collections.min(valores));
                    valores.set(indice, Math.max(valores.get(indice), nota));
                }
            }
        } else if ("Ultimo recuperatorio".equals(criterio) && !recuperatorios.isEmpty()) return recuperatorios.getLast();
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // verificamos las evaluaciones y conservamos el resultado de la materia en la inscripción actual
    public String cerrar(int id, int materia, String observacion, Integer usuario) throws Exception {
        String motivo = Inscripcion_servicio.motivo(observacion);
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 2);
            var inscripcion = inscripciones.bloquear(conexion, id);
            Inscripcion_servicio.validarAbierta(inscripcion);
            if (!List.of("Confirmada", "Recuperacion").contains(inscripcion.get("estado"))) throw new IllegalArgumentException("La inscripción debe estar confirmada o en recuperación.");
            int curso = numero(inscripcion, "id_curso");
            if (dao.docente(conexion, curso) != usuario) throw new IllegalArgumentException("El curso no corresponde al docente.");
            var datos = inscripciones.curso(conexion, curso);
            if (datos.get("nota_aprobacion_materia") == null || datos.get("criterio_recuperatorio") == null) throw new IllegalArgumentException("Dirección debe configurar la nota mínima y el criterio de recuperatorios.");
            if (dao.materias(conexion, curso).stream().noneMatch(m -> numero(m, "id_materia") == materia)) throw new IllegalArgumentException("La materia no pertenece al curso.");
            var evaluaciones = dao.notas(conexion, id, curso, materia);
            if (evaluaciones.isEmpty() || evaluaciones.stream().anyMatch(n -> n.get("nota") == null || !"Cerrada".equals(n.get("estado")))) throw new IllegalArgumentException("Faltan notas o evaluaciones por cerrar para calcular la materia.");
            List<Double> normales = new ArrayList<>(), recuperatorios = new ArrayList<>();
            for (var nota : evaluaciones) {
                double valor = ((Number) nota.get("nota")).doubleValue() / ((Number) nota.get("nota_maxima")).doubleValue() * 10;
                ("Recuperatorio".equals(nota.get("tipo")) ? recuperatorios : normales).add(valor);
            }
            if (normales.isEmpty()) throw new IllegalArgumentException("La materia necesita al menos una evaluación además de los recuperatorios.");
            BigDecimal promedio = BigDecimal.valueOf(promedio(normales, recuperatorios, datos.get("criterio_recuperatorio").toString())).setScale(2, RoundingMode.DOWN);
            String resultado = promedio.doubleValue() >= ((Number) datos.get("nota_aprobacion_materia")).doubleValue() ? "Aprobada" : "Desaprobada";
            dao.guardar(conexion, id, materia, promedio, resultado, motivo, usuario);
            return resultado + " · Promedio " + promedio;
        });
    }
}

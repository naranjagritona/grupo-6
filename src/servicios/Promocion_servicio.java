package servicios;

import dao.Inscripcion_dao;
import java.sql.Connection;
import java.util.*;
import static dao.Consulta_academica_dao.numero;

// revisamos las materias y el promedio de cada alumno antes de promocionar una división
public class Promocion_servicio {
    private final Inscripcion_dao dao;
    private final Inscripcion_servicio inscripciones;

    public Promocion_servicio() {
        this(new Inscripcion_dao());
    }

    // compartimos el dao para guardar todas las promociones dentro de una misma transacción
    public Promocion_servicio(Inscripcion_dao dao) {
        this.dao = dao;
        this.inscripciones = new Inscripcion_servicio(dao);
    }

    public record Revision(int inscripcion, String alumno, boolean aprobado, Double promedio, String motivo) {}
    public record Resultado(int promocionados, int recuperacion) {}

    // comprobamos que dirección haya definido las condiciones antes de evaluar a los alumnos
    private void validarRegla(Map<String, Object> curso) {
        if (curso.get("nota_aprobacion_materia") == null || curso.get("promedio_promocion") == null) {
            throw new IllegalArgumentException("Dirección debe configurar la nota mínima por materia y el promedio de promoción del curso.");
        }
    }

    // revisamos todas las materias, incluyendo las que todavía no tienen un resultado aprobado
    private Revision revisar(Connection conexion, Map<String, Object> alumno, Map<String, Object> curso) throws Exception {
        int id = numero(alumno, "id_inscripcion");
        var resultados = dao.resultados(conexion, id);
        List<String> pendientes = new ArrayList<>();
        double suma = 0;
        double minimo = ((Number) curso.get("nota_aprobacion_materia")).doubleValue();
        for (var materia : resultados) {
            Object promedio = materia.get("promedio");
            if (!"Aprobada".equals(materia.get("resultado")) || promedio == null || ((Number) promedio).doubleValue() < minimo) {
                pendientes.add(materia.get("nombre_materia").toString());
            }
            if (promedio != null) suma += ((Number) promedio).doubleValue();
        }
        Double promedio = resultados.isEmpty() ? null : suma / resultados.size();
        boolean aprobado = !resultados.isEmpty() && pendientes.isEmpty()
                && promedio >= ((Number) curso.get("promedio_promocion")).doubleValue();
        String motivo = resultados.isEmpty() ? "El curso no tiene materias asignadas."
                : !pendientes.isEmpty() ? "Materias pendientes: " + String.join(", ", pendientes)
                : aprobado ? "Todas las materias aprobadas y promedio suficiente."
                : "El promedio no alcanza el mínimo de promoción.";
        return new Revision(id, alumno.get("alumno").toString(), aprobado, promedio, motivo);
    }

    // mostramos la revisión de la división antes de realizar cualquier cambio de inscripción
    public List<Revision> revisar(int curso, Integer usuario) throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            var datos = dao.curso(conexion, curso);
            validarRegla(datos);
            List<Revision> lista = new ArrayList<>();
            for (var alumno : dao.alumnosPromocion(conexion, curso)) lista.add(revisar(conexion, alumno, datos));
            return lista;
        });
    }

    // volvemos a revisar cada alumno y promovemos solamente a quienes cumplen todas las condiciones
    public Resultado promocionar(int origen, int destino, Integer usuario) throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            var curso = dao.curso(conexion, origen);
            var siguiente = dao.curso(conexion, destino);
            validarRegla(curso);
            if (!"Activo".equals(siguiente.get("estado"))) {
                throw new IllegalArgumentException("El curso de destino debe estar activo.");
            }
            if (Integer.parseInt(siguiente.get("año").toString()) != Integer.parseInt(curso.get("año").toString()) + 1) {
                throw new IllegalArgumentException("El destino tiene que ser el año siguiente.");
            }
            if (!((java.sql.Date) siguiente.get("inicio_periodo")).after((java.sql.Date) curso.get("inicio_periodo"))) {
                throw new IllegalArgumentException("La promoción se realiza hacia un período lectivo posterior.");
            }
            List<Revision> alumnos = new ArrayList<>();
            for (var alumno : dao.alumnosPromocion(conexion, origen)) alumnos.add(revisar(conexion, alumno, curso));
            long aptos = alumnos.stream().filter(Revision::aprobado).count();
            if (aptos > numero(siguiente, "cupo_maximo") - dao.ocupacion(conexion, destino)) {
                throw new IllegalArgumentException("El destino no tiene cupo para todos los alumnos aprobados. No se realizaron cambios.");
            }
            int promovidos = 0, recuperacion = 0;
            for (var alumno : alumnos) {
                if (alumno.aprobado()) {
                    inscripciones.trasladar(conexion, alumno.inscripcion(), destino, "Promoción de año: " + alumno.motivo(), usuario, true, "Finalizada");
                    promovidos++;
                } else {
                    var anterior = dao.bloquear(conexion, alumno.inscripcion());
                    String motivo = alumno.motivo().length() > 255 ? alumno.motivo().substring(0, 252) + "..." : alumno.motivo();
                    dao.estado(conexion, alumno.inscripcion(), "Recuperacion", motivo);
                    dao.movimiento(conexion, alumno.inscripcion(), anterior.get("estado").toString(), "Recuperacion", motivo, usuario);
                    recuperacion++;
                }
            }
            return new Resultado(promovidos, recuperacion);
        });
    }

    // guardamos las notas mínimas y el criterio de recuperatorios que define dirección
    public void configurar(int curso, double materia, double promocion, String criterio, Integer usuario) throws Exception {
        if (!Double.isFinite(materia) || !Double.isFinite(promocion) || materia <= 0 || materia > 10
                || promocion <= 0 || promocion > 10 || !List.of("Promediar", "Reemplazar menor", "Ultimo recuperatorio").contains(criterio)) {
            throw new IllegalArgumentException("Revisá las notas mínimas de 0 a 10 y el criterio de recuperatorios.");
        }
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 5);
            var anterior = dao.curso(conexion, curso);
            dao.regla(conexion, curso, materia, promocion, criterio);
            if (!Objects.equals(anterior.get("criterio_recuperatorio"), criterio)) {
                new dao.Resultado_Academico_dao().invalidarCurso(conexion, curso, null);
            }
            return null;
        });
    }
}

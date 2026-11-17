package servicios;

import clases.*;

import conexion.Conexion;

import dao.*;

import java.sql.*;
import java.util.*;

// El plan y las asignaciones se coordinan aquí, sus tablas tienen DAO propios.
public class PlanCurso_servicio {
    public record Detalle(int materia, String nombre, Integer docente, String nombreDocente) {}

    public Map<Integer, List<Materia>> planPorAnio() {
        try (Connection db = Conexion.getConnection()) {
            Map<Integer, List<Materia>> plan = new LinkedHashMap<>();
            Map<Integer, Materia> materias = new HashMap<>();
            for (Materia materia : new Materia_dao().listarTabla(db))
                materias.put(materia.getId_materia(), materia);
            for (int anio = 1; anio <= 6; anio++) plan.put(anio, new ArrayList<>());
            for (Materia_Anio relacion : new Materia_Anio_dao().listarTabla(db)) {
                Materia materia = materias.get(relacion.getId_materia());
                if (materia != null && plan.containsKey(relacion.getAnio()))
                    plan.get(relacion.getAnio()).add(materia);
            }
            for (List<Materia> lista : plan.values())
                lista.sort(Comparator.comparing(Materia::getNombre_materia));
            return plan;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar las materias por año.", e);
        }
    }

    public List<Materia> materias(int anio) {
        try (Connection db = Conexion.getConnection()) {
            return materias(db, anio);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar las materias del año.", e);
        }
    }

    public List<Materia> materias(Connection db, int anio) throws SQLException {
        List<Materia> lista = new ArrayList<>();
        Map<Integer, Materia> materias = new HashMap<>();
        for (Materia materia : new Materia_dao().listarTabla(db))
            materias.put(materia.getId_materia(), materia);
        for (Materia_Anio relacion : new Materia_Anio_dao().delAnio(db, anio)) {
            Materia materia = materias.get(relacion.getId_materia());
            if (materia != null) lista.add(materia);
        }
        lista.sort(Comparator.comparing(Materia::getNombre_materia));
        return lista;
    }

    public List<Detalle> detalle(int curso) throws Exception {
        try (Connection db = Conexion.getConnection()) {
            List<Detalle> lista = new ArrayList<>();
            for (Curso_Materia relacion : new Curso_Materia_dao().delCurso(db, curso)) {
                Materia materia = new Materia_dao().requerir(db, relacion.getId_materia());
                Docente docente =
                        relacion.getId_docente() == null
                                ? null
                                : new Docente_dao().requerir(db, relacion.getId_docente());
                lista.add(
                        new Detalle(
                                materia.getId_materia(),
                                materia.getNombre_materia(),
                                relacion.getId_docente(),
                                docente == null
                                        ? null
                                        : docente.getApellido() + ", " + docente.getNombre()));
            }
            lista.sort(Comparator.comparing(Detalle::nombre));
            return lista;
        }
    }

    public void agregar(int anio, Integer materia, String nombre) throws Exception {
        if (anio < 1 || anio > 6)
            throw new IllegalArgumentException("Seleccioná un año entre 1 y 6.");
        Conexion.transaccion(
                db -> {
                    Permisos_servicio.verificar(
                            db, Auditoria_servicio.usuarioActual(), Rol.DIRECTIVO);
                    int id;
                    if (materia == null) {
                        String valor = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
                        if (valor.isEmpty() || valor.length() > 200)
                            throw new IllegalArgumentException(
                                    "Ingresá un nombre de materia de hasta 200 caracteres.");
                        Materia existente = new Materia_dao().porNombre(db, valor);
                        id =
                                existente == null
                                        ? new Materia_dao().crear(db, new Materia(null, valor))
                                        : existente.getId_materia();
                    } else {
                        new Materia_dao().requerir(db, materia);
                        id = materia;
                    }
                    Materia_Anio relacion = new Materia_Anio();
                    relacion.setAnio(anio);
                    relacion.setId_materia(id);
                    new Materia_Anio_dao().agregar(db, relacion);
                    new Curso_Materia_dao().incorporarAlAnio(db, id, anio);
                    return null;
                });
    }

    public void eliminarDelAnio(int anio, int materia) throws Exception {
        if (anio < 1 || anio > 6) throw new IllegalArgumentException("Seleccioná un año válido.");
        Conexion.transaccion(
                db -> {
                    Permisos_servicio.verificar(
                            db, Auditoria_servicio.usuarioActual(), Rol.DIRECTIVO);
                    var cursos = new Curso_dao().delAnio(db, anio);
                    new Materia_dao().requerir(db, materia);
                    if (new Materia_Anio_dao()
                            .delAnio(db, anio).stream()
                                    .noneMatch(m -> m.getId_materia() == materia))
                        throw new IllegalArgumentException(
                                "La materia ya no pertenece a este año. Actualizá el listado.");
                    if (new Calificacion_dao().existeEnMateria(db, materia))
                        throw new IllegalArgumentException(
                                "No se puede eliminar la materia porque tiene calificaciones"
                                        + " registradas.");
                    if (new Columna_Notas_dao().existeEnAnio(db, anio, materia))
                        throw new IllegalArgumentException(
                                "La materia tiene evaluaciones creadas en este año. Conservá esas"
                                        + " evaluaciones antes de quitarla.");
                    if (new Resultado_Materia_dao().registradoEnAnio(db, anio, materia))
                        throw new IllegalArgumentException(
                                "No se puede eliminar la materia porque tiene resultados académicos"
                                        + " registrados.");
                    for (Curso curso : cursos) {
                        if (Objects.equals(curso.getId_materia(), materia)) {
                            var alternativa =
                                    new Curso_Materia_dao()
                                            .delCurso(db, curso.getId_curso()).stream()
                                                    .filter(m -> m.getId_materia() != materia)
                                                    .findFirst();
                            if (alternativa.isEmpty())
                                throw new IllegalArgumentException(
                                        "El curso no puede quedar sin materias. Agregá otra materia"
                                                + " al año antes de quitar la última.");
                            new Curso_dao()
                                    .materiaPrincipal(
                                            db,
                                            curso.getId_curso(),
                                            alternativa.get().getId_materia());
                        }
                    }
                    new Resultado_Materia_dao().quitarPendientesDelAnio(db, anio, materia);
                    new Curso_Materia_dao().quitarDelAnio(db, anio, materia);
                    new Materia_Anio_dao().quitar(db, anio, materia);
                    return null;
                });
    }

    public Curso_Materia bloquearAsignacion(Connection db, int curso, int materia)
            throws SQLException {
        new Curso_dao().requerir(db, curso);
        return new Curso_Materia_dao().bloquear(db, curso, materia);
    }

    public boolean docenteActivo(Connection db, int docente) throws SQLException {
        return "Activo".equals(new Docente_dao().requerir(db, docente).getEstado());
    }

    public void actualizarAsignacion(Connection db, int curso, int materia, Integer docente)
            throws SQLException {
        Curso_Materia relacion = new Curso_Materia();
        relacion.setId_curso(curso);
        relacion.setId_materia(materia);
        relacion.setId_docente(docente);
        new Curso_Materia_dao().actualizar(db, relacion);
    }

    public void asignar(int curso, int materia, Integer docente, Integer anterior)
            throws Exception {
        new Curso_servicio()
                .asignarDocente(
                        this,
                        curso,
                        materia,
                        docente,
                        anterior,
                        Auditoria_servicio.usuarioActual());
    }
}

package servicios;

import clases.*;

import conexion.Conexion;

import dao.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

// Las reglas se validan aquí, cada DAO persiste exclusivamente su entidad.
public class Calificaciones_servicio {
    private final Usuario usuario;
    private final Curso_dao cursos = new Curso_dao();
    private final Curso_Materia_dao materias = new Curso_Materia_dao();
    private final Columna_Notas_dao evaluaciones = new Columna_Notas_dao();
    private final Inscripcion_dao inscripciones = new Inscripcion_dao();
    private final Calificacion_dao notas = new Calificacion_dao();
    private final Calificacion_Historial_dao historial = new Calificacion_Historial_dao();
    private final Resultado_Materia_dao resultados = new Resultado_Materia_dao();

    public Calificaciones_servicio(Usuario usuario) {
        this.usuario = usuario;
    }

    private void permitir(Connection db, int curso) throws Exception {
        permitir(db, curso, true);
    }

    private void permitir(Connection db, int curso, boolean requiereActivo) throws Exception {
        Permisos_servicio.verificar(
                db, usuario == null ? null : usuario.getId_usuario(), Rol.DIRECTIVO, Rol.DOCENTE);
        Curso datos = cursos.requerir(db, curso);
        if (requiereActivo && !"Activo".equals(datos.getEstado()))
            throw new IllegalArgumentException("El curso no está activo.");
        if (new dao.Usuario_dao().rolActivo(db, usuario.getId_usuario()) != Rol.DIRECTIVO
                && !materias.tieneDocente(db, curso, usuario.getId_usuario())) {
            throw new IllegalArgumentException("El curso no te corresponde.");
        }
    }

    private void permitirMateria(Connection db, int curso, int materia) throws SQLException {
        materias.bloquear(db, curso, materia);
        if (new dao.Usuario_dao().rolActivo(db, usuario.getId_usuario()) != Rol.DIRECTIVO
                && !materias.asignado(db, curso, materia, usuario.getId_usuario())) {
            throw new IllegalArgumentException("La materia no está asignada a tu cuenta.");
        }
    }

    private void permitirEvaluacion(Connection db, int id) throws SQLException {
        Columna_Notas columna = evaluaciones.requerir(db, id);
        permitirMateria(db, columna.getId_curso(), columna.getId_materia());
    }

    public static BigDecimal numero(String texto) {
        String valor = texto.trim().replace(',', '.');
        if (!valor.matches("[0-9]{1,2}(\\.[0-9]{1,2})?"))
            throw new IllegalArgumentException(
                    "Ingresá un número entre 0 y 99,99, con hasta dos decimales.");
        return new BigDecimal(valor);
    }

    private String texto(String valor, int maximo, String campo) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty() || limpio.length() > maximo)
            throw new IllegalArgumentException(
                    "Revisá " + campo + " (hasta " + maximo + " caracteres).");
        return limpio;
    }

    public void crearEvaluacion(
            int curso, String titulo, String tipo, String maximo, String aprobacion, String fecha)
            throws Exception {
        Curso seleccionado = cursos.obtenerPorId(curso);
        if (seleccionado == null) throw new IllegalArgumentException("El curso no existe.");
        crearEvaluacion(
                curso, seleccionado.getId_materia(), titulo, tipo, maximo, aprobacion, fecha);
    }

    public void crearEvaluacion(
            int curso,
            int materia,
            String titulo,
            String tipo,
            String maximo,
            String aprobacion,
            String fecha)
            throws Exception {
        String nombre = texto(titulo, 200, "el título");
        BigDecimal max = numero(maximo), apr = numero(aprobacion);
        if (max.signum() <= 0
                || apr.compareTo(max) > 0
                || !List.of("Evaluación", "Trabajo Práctico", "Recuperatorio", "Otro")
                        .contains(tipo))
            throw new IllegalArgumentException(
                    "Revisá el tipo de evaluación y la escala de notas.");
        LocalDate dia = LocalDate.parse(fecha.trim());
        try (Connection db = Conexion.getConnection()) {
            db.setAutoCommit(false);
            try {
                permitir(db, curso);
                permitirMateria(db, curso, materia);
                Columna_Notas columna =
                        new Columna_Notas(
                                null,
                                curso,
                                nombre,
                                tipo,
                                max.doubleValue(),
                                apr.doubleValue(),
                                dia,
                                dia,
                                "Abierta");
                columna.setId_materia(materia);
                evaluaciones.crearEvaluacion(db, columna);
                resultados.invalidarCurso(db, curso, materia);
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    public void cerrarEvaluacion(int id) throws Exception {
        try (Connection db = Conexion.getConnection()) {
            db.setAutoCommit(false);
            try {
                Columna_Notas columna = evaluaciones.requerir(db, id);
                permitir(db, columna.getId_curso());
                permitirEvaluacion(db, id);
                evaluaciones.cerrar(db, id);
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    private BigDecimal validarNota(Connection db, int inscripcion, int columna, String valor)
            throws Exception {
        BigDecimal nota = numero(valor);
        Inscripcion i = inscripciones.requerir(db, inscripcion);
        Columna_Notas c = evaluaciones.requerir(db, columna);
        if (!Objects.equals(i.getId_curso(), c.getId_curso())
                || !List.of("Confirmada", "Recuperacion").contains(i.getEstado()))
            throw new IllegalArgumentException(
                    "La inscripción no corresponde a esa evaluación o ya no está activa.");
        if (!"Abierta".equals(c.getEstado())
                || nota.compareTo(BigDecimal.valueOf(c.getNota_maxima())) > 0)
            throw new IllegalArgumentException(
                    "La evaluación está cerrada o la nota supera el máximo.");
        permitir(db, i.getId_curso());
        permitirEvaluacion(db, columna);
        return nota;
    }

    public void registrarNota(int inscripcion, int columna, String valor, String observaciones)
            throws Exception {
        if (observaciones == null || observaciones.length() > 255)
            throw new IllegalArgumentException("Las observaciones admiten hasta 255 caracteres.");
        try (Connection db = Conexion.getConnection()) {
            db.setAutoCommit(false);
            try {
                BigDecimal nota = validarNota(db, inscripcion, columna, valor);
                notas.crear(
                        db,
                        new Calificacion(
                                null,
                                inscripcion,
                                columna,
                                nota.doubleValue(),
                                null,
                                observaciones.trim()));
                resultados.invalidar(db, inscripcion, columna);
                db.commit();
            } catch (Exception e) {
                db.rollback();
                if (e instanceof SQLException sql && sql.getErrorCode() == 1062)
                    throw new IllegalArgumentException(
                            "Ya existe una nota para ese alumno y evaluación. Usá Modificar nota.");
                throw e;
            }
        }
    }

    public void modificarNota(int id, String valor, String motivo) throws Exception {
        String razon = texto(motivo, 255, "el motivo del cambio");
        try (Connection db = Conexion.getConnection()) {
            db.setAutoCommit(false);
            try {
                Calificacion anterior = notas.requerir(db, id);
                BigDecimal nueva =
                        validarNota(
                                db,
                                anterior.getId_inscripcion(),
                                anterior.getId_columna_notas(),
                                valor);
                Calificacion_Historial cambio =
                        new Calificacion_Historial(
                                null, id, anterior.getNota(), nueva.doubleValue(), null, razon);
                anterior.setNota(nueva.doubleValue());
                anterior.setObservaciones(razon);
                notas.corregir(db, anterior);
                historial.crear(db, cambio);
                resultados.invalidar(
                        db, anterior.getId_inscripcion(), anterior.getId_columna_notas());
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    public void eliminarNota(int id) throws Exception {
        try (Connection db = Conexion.getConnection()) {
            db.setAutoCommit(false);
            try {
                Calificacion anterior = notas.requerir(db, id);
                validarNota(
                        db,
                        anterior.getId_inscripcion(),
                        anterior.getId_columna_notas(),
                        anterior.getNota().toString());
                notas.quitar(db, id);
                resultados.invalidar(
                        db, anterior.getId_inscripcion(), anterior.getId_columna_notas());
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    public List<String> historial(int id) throws Exception {
        try (Connection db = Conexion.getConnection()) {
            Permisos_servicio.verificar(
                    db,
                    usuario == null ? null : usuario.getId_usuario(),
                    Rol.ALUMNO,
                    Rol.DOCENTE,
                    Rol.SECRETARIA,
                    Rol.DIRECTIVO);
            Calificacion nota = notas.requerir(db, id);
            Inscripcion inscripcion = inscripciones.requerir(db, nota.getId_inscripcion());
            int rol = new dao.Usuario_dao().rolActivo(db, usuario.getId_usuario());
            if (rol == Rol.ALUMNO) {
                Alumno alumno = new Alumno_dao().requerir(db, inscripcion.getId_alumno());
                if (!Objects.equals(alumno.getId_usuario(), usuario.getId_usuario()))
                    throw new IllegalArgumentException("Esta nota no corresponde a tu cuenta.");
            } else if (rol == Rol.DOCENTE) {
                permitir(db, inscripcion.getId_curso(), false);
                permitirEvaluacion(db, nota.getId_columna_notas());
            }
            List<String> lineas = new ArrayList<>();
            for (Calificacion_Historial cambio : historial.deCalificacion(db, id)) {
                lineas.add(
                        cambio.getFecha_modificacion()
                                + " | "
                                + cambio.getNota_anterior()
                                + " → "
                                + cambio.getNota_nueva()
                                + " | "
                                + cambio.getMotivo());
            }
            return lineas;
        }
    }
}

package servicios;

import clases.Usuario;

import conexion.Conexion;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// validamos las notas y los permisos antes de guardar la evaluación o su historial
public class Calificaciones_servicio {
    private final Usuario usuario;

    public Calificaciones_servicio(Usuario usuario) {
        this.usuario = usuario;
    }

    // obtiene la conexión que utiliza la consulta
    protected Connection abrirConexion() {
        return Conexion.getConnection();
    }

    // comprueba el rol de la cuenta y su relación con el curso
    private void permitir(Connection db, int curso) throws SQLException {
        permitir(db, curso, true);
    }

    // comprueba el rol de la cuenta y su relación con el curso
    private void permitir(Connection db, int curso, boolean requiereActivo) throws SQLException {
        try { servicios.Permisos_servicio.verificar(db, new dao.Consulta_academica_dao(), usuario == null ? null : usuario.getId_usuario(), 2); }
        catch (Exception error) { throw new IllegalArgumentException(error.getMessage(), error); }
        if (usuario == null || usuario.getId_rol() != 2) {
            throw new IllegalArgumentException("No tenés permisos para modificar calificaciones.");
        }
        try (PreparedStatement q =
                db.prepareStatement(
                        "SELECT c.estado,d.id_usuario FROM curso c LEFT JOIN docente d ON"
                                + " d.id_docente=c.id_docente WHERE c.id_curso=? FOR UPDATE")) {
            q.setInt(1, curso);
            try (ResultSet rs = q.executeQuery()) {
                if (!rs.next()
                        || (requiereActivo && !"Activo".equals(rs.getString(1)))
                        || (usuario.getId_rol() == 2 && usuario.getId_usuario() != rs.getInt(2))) {
                    throw new IllegalArgumentException(
                            "El curso no está activo o no te corresponde.");
                }
            }
        }
    }

    // usamos decimales para no aceptar infinitos ni valores fuera de la escala
    public static BigDecimal numero(String texto) {
        String valor = texto.trim().replace(',', '.');
        if (!valor.matches("[0-9]{1,2}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException(
                    "Ingresá un número entre 0 y 99,99, con hasta dos decimales.");
        }
        return new BigDecimal(valor);
    }

    private String texto(String valor, int maximo, String campo) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty() || limpio.length() > maximo) {
            throw new IllegalArgumentException(
                    "Revisá " + campo + " (hasta " + maximo + " caracteres).");
        }
        return limpio;
    }

    // prepara los datos necesarios para registrar una evaluación
    public void crearEvaluacion(
            int curso, String titulo, String tipo, String maximo, String aprobacion, String fecha)
            throws Exception {
        clases.Curso seleccionado = new dao.Curso_dao().obtenerPorId(curso);
        if (seleccionado == null) throw new IllegalArgumentException("El curso no existe.");
        crearEvaluacion(curso, seleccionado.getId_materia(), titulo, tipo, maximo, aprobacion, fecha);
    }

    // verificamos la materia y los datos de la evaluación antes de guardarla en el curso
    public void crearEvaluacion(int curso, int materia, String titulo, String tipo, String maximo, String aprobacion, String fecha) throws Exception {
        String nombre = texto(titulo, 200, "el título");
        BigDecimal max = numero(maximo), apr = numero(aprobacion);
        if (max.signum() <= 0
                || apr.compareTo(max) > 0
                || !List.of("Evaluación", "Trabajo Práctico", "Recuperatorio", "Otro")
                        .contains(tipo)) {
            throw new IllegalArgumentException(
                    "Revisá el tipo de evaluación y la escala de notas.");
        }
        LocalDate dia = LocalDate.parse(fecha.trim());
        try (Connection db = abrirConexion()) {
            db.setAutoCommit(false);
            try {
                permitir(db, curso);
                try (PreparedStatement consulta = db.prepareStatement("SELECT id_materia FROM curso_materia WHERE id_curso=? AND id_materia=?")) {
                    consulta.setInt(1, curso); consulta.setInt(2, materia);
                    try (ResultSet registros = consulta.executeQuery()) { if (!registros.next()) throw new IllegalArgumentException("La materia no pertenece al curso."); }
                }
                try (PreparedStatement q =
                        db.prepareStatement(
                                "INSERT INTO"
                                    + " columna_notas(id_curso,titulo,tipo,nota_maxima,nota_aprobacion,fecha_evaluacion,fecha_cierre,estado,id_materia)"
                                    + " VALUES(?,?,?,?,?,?,?,'Abierta',?)")) {
                    q.setInt(1, curso);
                    q.setString(2, nombre);
                    q.setString(3, tipo);
                    q.setBigDecimal(4, max);
                    q.setBigDecimal(5, apr);
                    q.setObject(6, dia);
                    q.setObject(7, dia);
                    q.setInt(8, materia);
                    q.executeUpdate();
                }
                new dao.Resultado_Academico_dao().invalidarCurso(db, curso, materia);
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    // cerramos la evaluación para que el docente pueda calcular el resultado final de la materia
    public void cerrarEvaluacion(int id) throws Exception {
        try (Connection conexion = abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                int curso;
                try (PreparedStatement consulta = conexion.prepareStatement("SELECT id_curso FROM columna_notas WHERE id_columna_notas=? FOR UPDATE")) {
                    consulta.setInt(1, id);
                    try (ResultSet registros = consulta.executeQuery()) { if (!registros.next()) throw new IllegalArgumentException("La evaluación no existe."); curso = registros.getInt(1); }
                }
                permitir(conexion, curso);
                try (PreparedStatement consulta = conexion.prepareStatement("UPDATE columna_notas SET estado='Cerrada',fecha_cierre=CURRENT_DATE WHERE id_columna_notas=?")) {
                    consulta.setInt(1, id); consulta.executeUpdate();
                }
                conexion.commit();
            } catch (Exception error) { conexion.rollback(); throw error; }
        }
    }

    // comprobamos la inscripción y la evaluación antes de aceptar una nota
    private BigDecimal validarNota(Connection db, int inscripcion, int columna, String valor)
            throws Exception {
        BigDecimal nota = numero(valor);
        int curso;
        try (PreparedStatement q =
                db.prepareStatement(
                        "SELECT i.id_curso,c.nota_maxima,c.estado FROM inscripcion i JOIN"
                            + " columna_notas c ON c.id_curso=i.id_curso WHERE i.id_inscripcion=?"
                            + " AND c.id_columna_notas=? AND i.estado IN ('Confirmada','Recuperacion')"
                            + " FOR UPDATE")) {
            q.setInt(1, inscripcion);
            q.setInt(2, columna);
            try (ResultSet rs = q.executeQuery()) {
                if (!rs.next())
                    throw new IllegalArgumentException(
                            "La inscripción no corresponde a esa evaluación o ya no está activa.");
                curso = rs.getInt(1);
                if (!"Abierta".equals(rs.getString(3)) || nota.compareTo(rs.getBigDecimal(2)) > 0) {
                    throw new IllegalArgumentException(
                            "La evaluación está cerrada o la nota supera el máximo.");
                }
            }
        }
        permitir(db, curso);
        return nota;
    }

    // valida la inscripción y guarda una nota sin duplicar la evaluación
    public void registrarNota(int inscripcion, int columna, String valor, String observaciones)
            throws Exception {
        if (observaciones.length() > 255)
            throw new IllegalArgumentException("Las observaciones admiten hasta 255 caracteres.");
        try (Connection db = abrirConexion()) {
            db.setAutoCommit(false);
            try {
                BigDecimal nota = validarNota(db, inscripcion, columna, valor);
                try (PreparedStatement q =
                        db.prepareStatement(
                                "INSERT INTO"
                                    + " calificacion(id_inscripcion,id_columna_notas,nota,observaciones)"
                                    + " VALUES(?,?,?,?)")) {
                    q.setInt(1, inscripcion);
                    q.setInt(2, columna);
                    q.setBigDecimal(3, nota);
                    q.setString(4, observaciones.trim());
                    q.executeUpdate();
                }
                new dao.Resultado_Academico_dao().invalidar(db, inscripcion, columna);
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

    // guarda la corrección de la nota junto con el motivo del cambio
    public void modificarNota(int id, String valor, String motivo) throws Exception {
        String razon = texto(motivo, 255, "el motivo del cambio");
        try (Connection db = abrirConexion()) {
            db.setAutoCommit(false);
            try {
                int inscripcion, columna;
                BigDecimal anterior;
                try (PreparedStatement q =
                        db.prepareStatement(
                                "SELECT id_inscripcion,id_columna_notas,nota FROM calificacion"
                                        + " WHERE id_calificacion=? FOR UPDATE")) {
                    q.setInt(1, id);
                    try (ResultSet rs = q.executeQuery()) {
                        if (!rs.next())
                            throw new IllegalArgumentException(
                                    "La nota ya no existe. Actualizá el listado.");
                        inscripcion = rs.getInt(1);
                        columna = rs.getInt(2);
                        anterior = rs.getBigDecimal(3);
                    }
                }
                BigDecimal nueva = validarNota(db, inscripcion, columna, valor);
                try (PreparedStatement q =
                        db.prepareStatement(
                                "UPDATE calificacion SET nota=?,fecha_carga=NOW(),observaciones=?"
                                        + " WHERE id_calificacion=?")) {
                    q.setBigDecimal(1, nueva);
                    q.setString(2, razon);
                    q.setInt(3, id);
                    q.executeUpdate();
                }
                try (PreparedStatement q =
                        db.prepareStatement(
                                "INSERT INTO"
                                    + " calificacion_historial(id_calificacion,nota_anterior,nota_nueva,motivo)"
                                    + " VALUES(?,?,?,?)")) {
                    q.setInt(1, id);
                    q.setBigDecimal(2, anterior);
                    q.setBigDecimal(3, nueva);
                    q.setString(4, razon);
                    q.executeUpdate();
                }
                new dao.Resultado_Academico_dao().invalidar(db, inscripcion, columna);
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    // verifica la selección antes de quitar la nota
    public void eliminarNota(int id) throws Exception {
        try (Connection db = abrirConexion()) {
            db.setAutoCommit(false);
            try {
                int inscripcion, columna;
                String valor;
                try (PreparedStatement q =
                        db.prepareStatement(
                                "SELECT id_inscripcion,id_columna_notas,nota FROM calificacion"
                                        + " WHERE id_calificacion=? FOR UPDATE")) {
                    q.setInt(1, id);
                    try (ResultSet rs = q.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("La nota ya no existe.");
                        inscripcion = rs.getInt(1);
                        columna = rs.getInt(2);
                        valor = rs.getString(3);
                    }
                }
                validarNota(db, inscripcion, columna, valor);
                try (PreparedStatement q =
                        db.prepareStatement("DELETE FROM calificacion WHERE id_calificacion=?")) {
                    q.setInt(1, id);
                    q.executeUpdate();
                }
                new dao.Resultado_Academico_dao().invalidar(db, inscripcion, columna);
                db.commit();
            } catch (Exception e) {
                db.rollback();
                throw e;
            }
        }
    }

    // consulta las modificaciones de la nota o los pagos de la inscripción
    public List<String> historial(int id) throws Exception {
        try (Connection db = abrirConexion()) {
            // el historial se consulta solo desde cursos que puede administrar esta cuenta
            try (PreparedStatement q =
                    db.prepareStatement(
                            "SELECT c.id_curso,a.id_usuario FROM calificacion n JOIN columna_notas"
                                + " c ON c.id_columna_notas=n.id_columna_notas JOIN inscripcion i"
                                + " ON i.id_inscripcion=n.id_inscripcion JOIN alumno a ON"
                                + " a.id_alumno=i.id_alumno WHERE n.id_calificacion=?")) {
                q.setInt(1, id);
                try (ResultSet rs = q.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("La nota ya no existe.");
                    if (usuario != null && usuario.getId_rol() == 3) {
                        if (usuario.getId_usuario() != rs.getInt(2)) {
                            throw new IllegalArgumentException(
                                    "Esta nota no corresponde a tu cuenta.");
                        }
                    } else {
                        if (usuario != null && (usuario.getId_rol() == 1 || usuario.getId_rol() == 5)) {
                            // permitimos la consulta de auditoría sin habilitar la modificación de notas
                        } else if (usuario != null && usuario.getId_rol() == 4) {
                            try (PreparedStatement consulta = db.prepareStatement("SELECT id_usuario FROM preceptor_curso WHERE id_usuario=? AND id_curso=?")) {
                                consulta.setInt(1, usuario.getId_usuario()); consulta.setInt(2, rs.getInt(1));
                                try (ResultSet registros = consulta.executeQuery()) { if (!registros.next()) throw new IllegalArgumentException("El curso no corresponde al preceptor."); }
                            }
                        } else permitir(db, rs.getInt(1), false);
                    }
                }
            }
            List<String> lineas = new ArrayList<>();
            try (PreparedStatement q =
                    db.prepareStatement(
                            "SELECT fecha_modificacion,nota_anterior,nota_nueva,motivo FROM"
                                    + " calificacion_historial WHERE id_calificacion=? ORDER BY"
                                    + " fecha_modificacion DESC,id_calificacion_historial DESC")) {
                q.setInt(1, id);
                try (ResultSet rs = q.executeQuery()) {
                    while (rs.next())
                        lineas.add(
                                rs.getString(1)
                                        + " | "
                                        + rs.getString(2)
                                        + " → "
                                        + rs.getString(3)
                                        + " | "
                                        + rs.getString(4));
                }
            }
            return lineas;
        }
    }
}

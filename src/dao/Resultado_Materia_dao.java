package dao;

import clases.Resultado_Materia;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de resultado materia en la base de datos
public class Resultado_Materia_dao implements Dao<Resultado_Materia, Integer> {
    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Resultado_Materia objeto) {
        throw new IllegalStateException(
                "El resultado final se calcula desde Calificaciones para conservar la relación con"
                        + " la materia.");
    }

    // consulta un registro usando su identificador
    @Override
    public Resultado_Materia obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM resultado_materia WHERE id_resultado_materia=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el registro de resultado_materia.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Resultado_Materia> obtenerTodos() {
        String consulta = "SELECT * FROM resultado_materia";
        List<Resultado_Materia> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No se pudo consultar el listado de resultado_materia.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Resultado_Materia objeto) {
        throw new IllegalStateException(
                "El resultado final se calcula desde Calificaciones para conservar la relación con"
                        + " la materia.");
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        throw new IllegalStateException(
                "El resultado final se calcula desde Calificaciones para conservar la relación con"
                        + " la materia.");
    }

    // guardamos el resultado final sin cambiar la inscripción a la que pertenecen las notas
    public void guardar(Connection conexion, Resultado_Materia r) throws SQLException {
        String consulta =
                "INSERT INTO"
                    + " resultado_materia(id_inscripcion,id_materia,promedio,resultado,fecha_calculo,id_usuario,observacion)"
                    + " VALUES(?,?,?,?,NOW(),?,?) ON DUPLICATE KEY UPDATE"
                    + " promedio=VALUES(promedio),resultado=VALUES(resultado),fecha_calculo=VALUES(fecha_calculo),id_usuario=VALUES(id_usuario),observacion=VALUES(observacion)";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setObject(1, r.getId_inscripcion());
            ps.setObject(2, r.getId_materia());
            ps.setObject(3, r.getPromedio());
            ps.setObject(4, r.getResultado());
            ps.setObject(5, r.getId_usuario());
            ps.setObject(6, r.getObservacion());
            ps.executeUpdate();
        }
    }

    // dejamos pendiente el resultado cuando cambian las notas para exigir una nueva revisión de la
    // materia
    public void invalidar(Connection conexion, int inscripcion, int evaluacion)
            throws SQLException {
        String consulta =
                "UPDATE resultado_materia SET resultado='En curso',fecha_calculo=NOW() WHERE"
                    + " id_inscripcion=? AND id_materia=(SELECT id_materia FROM columna_notas WHERE"
                    + " id_columna_notas=?)";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setObject(1, inscripcion);
            ps.setObject(2, evaluacion);
            ps.executeUpdate();
        }
    }

    // pedimos revisar los resultados abiertos cuando se incorpora una evaluación o cambia el
    // criterio de cálculo
    public void invalidarCurso(Connection conexion, int curso, Integer materia)
            throws SQLException {
        String consulta =
                "UPDATE resultado_materia SET resultado='En curso',fecha_calculo=NOW() WHERE"
                    + " id_inscripcion IN (SELECT id_inscripcion FROM inscripcion WHERE id_curso=?"
                    + " AND estado IN ('Confirmada','Recuperacion')) AND (? IS NULL OR"
                    + " id_materia=?)";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setObject(1, curso);
            ps.setObject(2, materia);
            ps.setObject(3, materia);
            ps.executeUpdate();
        }
    }

    public java.util.List<Resultado_Materia> deInscripcion(Connection db, int id)
            throws SQLException {
        return seleccionar(db, "SELECT * FROM resultado_materia WHERE id_inscripcion=?", id);
    }

    public boolean registradoEnAnio(Connection db, int anio, int materia) throws SQLException {
        return !seleccionar(
                        db,
                        "SELECT r.* FROM resultado_materia r JOIN inscripcion i ON"
                            + " i.id_inscripcion=r.id_inscripcion JOIN curso c ON"
                            + " c.id_curso=i.id_curso WHERE CAST(c.año AS UNSIGNED)=? AND"
                            + " r.id_materia=? AND (r.resultado NOT IN ('En curso','Pendiente') OR"
                            + " r.promedio<>0 OR r.id_usuario IS NOT NULL) LIMIT 1",
                        anio,
                        materia)
                .isEmpty();
    }

    public void quitarPendientesDelAnio(Connection db, int anio, int materia) throws SQLException {
        String consulta =
                "DELETE r FROM resultado_materia r JOIN inscripcion i ON"
                        + " i.id_inscripcion=r.id_inscripcion JOIN curso c ON c.id_curso=i.id_curso"
                        + " WHERE CAST(c.año AS UNSIGNED)=? AND r.id_materia=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, anio);
            ps.setObject(2, materia);
            ps.executeUpdate();
        }
    }

    protected Resultado_Materia leerEntidad(java.sql.ResultSet registros)
            throws java.sql.SQLException {
        Resultado_Materia objeto = new Resultado_Materia();
        objeto.setId_resultado_materia((Integer) registros.getObject("id_resultado_materia"));
        objeto.setId_inscripcion((Integer) registros.getObject("id_inscripcion"));
        objeto.setPromedio(
                (registros.getObject("promedio") == null ? null : registros.getDouble("promedio")));
        objeto.setResultado(registros.getString("resultado"));
        objeto.setFecha_calculo(
                (registros.getTimestamp("fecha_calculo") == null
                        ? null
                        : registros.getTimestamp("fecha_calculo").toLocalDateTime()));
        objeto.setId_materia((Integer) registros.getObject("id_materia"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setObservacion(registros.getString("observacion"));
        return objeto;
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Resultado_Materia
    private java.util.List<Resultado_Materia> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Resultado_Materia> lista = new java.util.ArrayList<>();
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            try (java.sql.ResultSet registros = ps.executeQuery()) {
                while (registros.next()) {
                    lista.add(leerEntidad(registros));
                }
            }
        }
        return lista;
    }

    // recibimos la conexión del servicio para no separar las consultas de una transacción
    public java.util.List<Resultado_Materia> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM resultado_materia");
    }

    public Resultado_Materia buscar(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM resultado_materia WHERE id_resultado_materia=?",
                        id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Resultado_Materia requerir(java.sql.Connection conexion, int id)
            throws java.sql.SQLException {
        var filas =
                seleccionar(
                        conexion,
                        "SELECT * FROM resultado_materia WHERE id_resultado_materia=? FOR UPDATE",
                        id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }
}

package dao;

import clases.Materia;
import java.sql.*;
import java.util.*;

// El plan pertenece al año; la asignación de cada docente pertenece al curso y materia.
public class PlanCurso_dao extends Consulta_academica_dao {
    // guardamos la materia y el docente que se muestran en el detalle del curso
    public record Detalle(int materia, String nombre, Integer docente, String nombreDocente) {}

    // reunimos las materias de los seis años para mostrar el plan en una sola consulta
    public Map<Integer,List<Materia>> planPorAnio() {
        Map<Integer,List<Materia>> plan = new LinkedHashMap<>();
        for (int anio=1; anio<=6; anio++) plan.put(anio, new ArrayList<>());
        try (Connection db = abrirConexion()) {
            for (var fila : consultar(db, "SELECT ma.anio,m.id_materia,m.nombre_materia FROM materia_anio ma JOIN materia m ON m.id_materia=ma.id_materia ORDER BY ma.anio,m.nombre_materia")) {
                plan.get(numero(fila, "anio")).add(new Materia(numero(fila, "id_materia"), (String) fila.get("nombre_materia")));
            }
            return plan;
        } catch (SQLException e) { throw new IllegalStateException("No se pudieron cargar las materias por año.", e); }
    }

    // abrimos la conexión y buscamos las materias que corresponden al año seleccionado
    public List<Materia> materias(int anio) {
        try (Connection db = abrirConexion()) { return materias(db, anio); }
        catch (SQLException e) { throw new IllegalStateException("No se pudieron cargar las materias del año.", e); }
    }

    // consultamos el plan del año usando la conexión de la operación actual
    public List<Materia> materias(Connection db, int anio) throws SQLException {
        List<Materia> lista = new ArrayList<>();
        try (PreparedStatement q = db.prepareStatement("SELECT m.id_materia,m.nombre_materia FROM materia_anio ma JOIN materia m ON m.id_materia=ma.id_materia WHERE ma.anio=? ORDER BY m.nombre_materia")) {
            q.setInt(1, anio);
            try (ResultSet rs = q.executeQuery()) {
                while (rs.next()) lista.add(new Materia(rs.getInt(1), rs.getString(2)));
            }
        }
        return lista;
    }

    // traemos las materias del curso y dejamos sin asignar las que todavía no tienen docente
    public List<Detalle> detalle(int curso) throws Exception {
        try (Connection db = abrirConexion()) {
            List<Detalle> lista = new ArrayList<>();
            try (PreparedStatement q = db.prepareStatement("SELECT cm.id_materia,m.nombre_materia,cm.id_docente,CONCAT(d.apellido,', ',d.nombre) docente FROM curso_materia cm JOIN materia m ON m.id_materia=cm.id_materia LEFT JOIN docente d ON d.id_docente=cm.id_docente WHERE cm.id_curso=? ORDER BY m.nombre_materia")) {
                q.setInt(1, curso);
                try (ResultSet rs = q.executeQuery()) {
                    while (rs.next()) lista.add(new Detalle(rs.getInt(1), rs.getString(2), (Integer) rs.getObject(3), rs.getString(4)));
                }
            }
            return lista;
        }
    }

    // verificamos el permiso de dirección y agregamos la materia al año y a sus cursos
    public void agregar(int anio, Integer materia, String nombre) throws Exception {
        if (anio < 1 || anio > 6) throw new IllegalArgumentException("Seleccioná un año entre 1 y 6.");
        transaccion(db -> {
            servicios.Permisos_servicio.verificar(db, this, servicios.Auditoria_servicio.usuarioActual(), 5);
            int id;
            if (materia == null) {
                String valor = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
                if (valor.isEmpty() || valor.length() > 200) throw new IllegalArgumentException("Ingresá un nombre de materia de hasta 200 caracteres.");
                var existentes = consultar(db, "SELECT id_materia FROM materia WHERE LOWER(TRIM(nombre_materia))=LOWER(?)", valor);
                id = existentes.isEmpty() ? insertarRegistro(db, "INSERT INTO materia(nombre_materia) VALUES(?)", valor) : numero(existentes.getFirst(), "id_materia");
            } else id = materia;
            ejecutar(db, "INSERT INTO materia_anio(anio,id_materia) VALUES(?,?) ON DUPLICATE KEY UPDATE id_materia=VALUES(id_materia)", anio, id);
            ejecutar(db, "INSERT INTO curso_materia(id_curso,id_materia) SELECT c.id_curso,? FROM curso c WHERE CAST(c.año AS UNSIGNED)=? AND NOT EXISTS(SELECT 1 FROM curso_materia cm WHERE cm.id_curso=c.id_curso AND cm.id_materia=?)", id, anio, id);
            return null;
        });
    }

    // Se retira del año seleccionado; los otros años conservan su plan y sus datos.
    public void eliminarDelAnio(int anio, int materia) throws Exception {
        if (anio < 1 || anio > 6) throw new IllegalArgumentException("Seleccioná un año válido.");
        transaccion(db -> {
            servicios.Permisos_servicio.verificar(db, this, servicios.Auditoria_servicio.usuarioActual(), 5);
            var cursos = consultar(db, "SELECT id_curso,id_materia FROM curso WHERE CAST(año AS UNSIGNED)=? ORDER BY id_curso FOR UPDATE", anio);
            uno(db, "SELECT id_materia FROM materia WHERE id_materia=? FOR UPDATE", materia);
            if (consultar(db, "SELECT id_materia FROM materia_anio WHERE anio=? AND id_materia=? FOR UPDATE", anio, materia).isEmpty())
                throw new IllegalArgumentException("La materia ya no pertenece a este año. Actualizá el listado.");
            if (!consultar(db, "SELECT n.id_calificacion FROM calificacion n JOIN columna_notas cn ON cn.id_columna_notas=n.id_columna_notas WHERE cn.id_materia=? LIMIT 1", materia).isEmpty())
                throw new IllegalArgumentException("No se puede eliminar la materia porque tiene calificaciones registradas.");
            if (!consultar(db, "SELECT cn.id_columna_notas FROM columna_notas cn JOIN curso c ON c.id_curso=cn.id_curso WHERE CAST(c.año AS UNSIGNED)=? AND cn.id_materia=? LIMIT 1", anio, materia).isEmpty())
                throw new IllegalArgumentException("La materia tiene evaluaciones creadas en este año. Conservá esas evaluaciones antes de quitarla.");
            if (!consultar(db, "SELECT r.id_resultado_materia FROM resultado_materia r JOIN inscripcion i ON i.id_inscripcion=r.id_inscripcion JOIN curso c ON c.id_curso=i.id_curso WHERE CAST(c.año AS UNSIGNED)=? AND r.id_materia=? AND (r.resultado NOT IN ('En curso','Pendiente') OR r.promedio<>0 OR r.id_usuario IS NOT NULL) LIMIT 1", anio, materia).isEmpty())
                throw new IllegalArgumentException("No se puede eliminar la materia porque tiene resultados académicos registrados.");
            for (var curso : cursos) {
                int id = numero(curso, "id_curso");
                if (numero(curso, "id_materia") == materia) {
                    var alternativas = consultar(db, "SELECT id_materia FROM curso_materia WHERE id_curso=? AND id_materia<>? ORDER BY id_materia LIMIT 1", id, materia);
                    if (alternativas.isEmpty()) throw new IllegalArgumentException("El curso no puede quedar sin materias. Agregá otra materia al año antes de quitar la última.");
                    ejecutar(db, "UPDATE curso SET id_materia=? WHERE id_curso=?", numero(alternativas.getFirst(), "id_materia"), id);
                }
            }
            ejecutar(db, "DELETE r FROM resultado_materia r JOIN inscripcion i ON i.id_inscripcion=r.id_inscripcion JOIN curso c ON c.id_curso=i.id_curso WHERE CAST(c.año AS UNSIGNED)=? AND r.id_materia=?", anio, materia);
            ejecutar(db, "DELETE cm FROM curso_materia cm JOIN curso c ON c.id_curso=cm.id_curso WHERE CAST(c.año AS UNSIGNED)=? AND cm.id_materia=?", anio, materia);
            ejecutar(db, "DELETE FROM materia_anio WHERE anio=? AND id_materia=?", anio, materia);
            return null;
        });
    }

    // comprobamos el docente y la asignación anterior antes de guardar el cambio
    public void asignar(int curso, int materia, Integer docente, Integer anterior) throws Exception {
        transaccion(db -> {
            servicios.Permisos_servicio.verificar(db, this, servicios.Auditoria_servicio.usuarioActual(), 1, 5);
            uno(db, "SELECT id_curso FROM curso WHERE id_curso=? FOR UPDATE", curso);
            var relacion = uno(db, "SELECT id_docente FROM curso_materia WHERE id_curso=? AND id_materia=? FOR UPDATE", curso, materia);
            if (!Objects.equals(relacion.get("id_docente"), anterior)) throw new IllegalArgumentException("Otro usuario cambió esta asignación. Actualizá el detalle y volvé a intentar.");
            if (docente != null) {
                var datos = uno(db, "SELECT estado FROM docente WHERE id_docente=? FOR UPDATE", docente);
                if (!"Activo".equals(datos.get("estado"))) throw new IllegalArgumentException("Seleccioná un docente activo.");
            }
            ejecutar(db, "UPDATE curso_materia SET id_docente=? WHERE id_curso=? AND id_materia=?", docente, curso, materia);
            return null;
        });
    }
}

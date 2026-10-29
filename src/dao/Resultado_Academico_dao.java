package dao;

import java.sql.*;
import java.util.*;

// consultamos las evaluaciones y guardamos el resultado final por materia de cada inscripción
public class Resultado_Academico_dao extends Consulta_academica_dao {
    // consultamos el docente del curso para comprobar los permisos de carga
    public int docente(Connection conexion, int curso) throws SQLException {
        return numero(uno(conexion, "SELECT d.id_usuario FROM curso c JOIN docente d ON d.id_docente=c.id_docente WHERE c.id_curso=?", curso), "id_usuario");
    }

    // buscamos las materias que el docente puede elegir al preparar una evaluación
    public List<Map<String, Object>> materias(Connection conexion, int curso) throws SQLException {
        return consultar(conexion, "SELECT m.id_materia,m.nombre_materia FROM curso_materia cm JOIN materia m ON m.id_materia=cm.id_materia WHERE cm.id_curso=? ORDER BY m.nombre_materia", curso);
    }

    // buscamos todas las notas de la materia y dejamos visibles las evaluaciones sin calificar
    public List<Map<String, Object>> notas(Connection conexion, int inscripcion, int curso, int materia) throws SQLException {
        return consultar(conexion, "SELECT n.tipo,n.estado,n.nota_maxima,c.nota FROM columna_notas n LEFT JOIN calificacion c ON c.id_columna_notas=n.id_columna_notas AND c.id_inscripcion=? WHERE n.id_curso=? AND n.id_materia=? ORDER BY n.fecha_evaluacion,n.id_columna_notas", inscripcion, curso, materia);
    }

    // guardamos el resultado final sin cambiar la inscripción a la que pertenecen las notas
    public void guardar(Connection conexion, int inscripcion, int materia, java.math.BigDecimal promedio, String resultado, String observacion, int usuario) throws SQLException {
        ejecutar(conexion, "INSERT INTO resultado_materia(id_inscripcion,id_materia,promedio,resultado,fecha_calculo,id_usuario,observacion) VALUES(?,?,?,?,NOW(),?,?) ON DUPLICATE KEY UPDATE promedio=VALUES(promedio),resultado=VALUES(resultado),fecha_calculo=VALUES(fecha_calculo),id_usuario=VALUES(id_usuario),observacion=VALUES(observacion)", inscripcion, materia, promedio, resultado, usuario, observacion);
    }
    // dejamos pendiente el resultado cuando cambian las notas para exigir una nueva revisión de la materia
    public void invalidar(Connection conexion, int inscripcion, int evaluacion) throws SQLException {
        ejecutar(conexion, "UPDATE resultado_materia SET resultado='En curso',fecha_calculo=NOW() WHERE id_inscripcion=? AND id_materia=(SELECT id_materia FROM columna_notas WHERE id_columna_notas=?)", inscripcion, evaluacion);
    }

    // pedimos revisar los resultados abiertos cuando se incorpora una evaluación o cambia el criterio de cálculo
    public void invalidarCurso(Connection conexion, int curso, Integer materia) throws SQLException {
        ejecutar(conexion, "UPDATE resultado_materia r JOIN inscripcion i ON i.id_inscripcion=r.id_inscripcion SET r.resultado='En curso',r.fecha_calculo=NOW() WHERE i.id_curso=? AND i.estado IN ('Confirmada','Recuperacion') AND (? IS NULL OR r.id_materia=?)", curso, materia, materia);
    }
}

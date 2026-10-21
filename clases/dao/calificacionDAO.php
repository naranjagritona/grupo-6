<?php

require_once 'dao.php';

class CalificacionDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($calificacion): void {
        $sql = "INSERT INTO calificacion (id_inscripcion, id_columna_notas, nota, fecha_carga, observaciones) VALUES (?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $calificacion->getId_inscripcion(),
            $calificacion->getId_columna_notas(),
            $calificacion->getNota(),
            $calificacion->getFecha_carga(),
            $calificacion->getObservaciones()
        ]);
    }

    public function modificar($calificacion) {
        $sql = "UPDATE calificacion SET id_inscripcion = ?, id_columna_notas = ?, nota = ?, fecha_carga = ?, observaciones = ? WHERE id_calificacion = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $calificacion->getId_inscripcion(),
            $calificacion->getId_columna_notas(),
            $calificacion->getNota(),
            $calificacion->getFecha_carga(),
            $calificacion->getObservaciones(),
            $calificacion->getId_calificacion()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM calificacion WHERE id_calificacion = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM calificacion";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Calificacion(
                $fila['id_calificacion'],
                $fila['id_inscripcion'],
                $fila['id_columna_notas'],
                $fila['nota'],
                $fila['fecha_carga'],
                $fila['observaciones']
            );
        }
        return $resultados;
    }

    private function consultaCalificaciones(): string
    {
        return "FROM calificacion g
            INNER JOIN inscripcion i ON i.id_inscripcion = g.id_inscripcion
            INNER JOIN alumno a ON a.id_alumno = i.id_alumno
            INNER JOIN columna_notas cn ON cn.id_columna_notas = g.id_columna_notas
            INNER JOIN curso c ON c.id_curso = cn.id_curso
            INNER JOIN materia m ON m.id_materia = c.id_materia";
    }

    private function alcancePorUsuario(array $usuario): array
    {
        if ($usuario['isAdmin']) {
            return ['', []];
        }
        if ($usuario['isTeacher']) {
            return ['c.id_docente = :scope', ['scope' => (int) $usuario['id_docente']]];
        }
        if ($usuario['isStudent']) {
            return ['i.id_alumno = :scope', ['scope' => (int) $usuario['id_alumno']]];
        }
        throw new RuntimeException('No tenés permisos para ver calificaciones.');
    }

    public function listarCalificacionesDeUsuario(array $usuario, string $busqueda = ''): array
    {
        [$scope, $parameters] = $this->alcancePorUsuario($usuario);
        $conditions = $scope === '' ? [] : [$scope];
        if ($busqueda !== '') {
            $conditions[] = "(a.nombre LIKE :search_first_name OR a.apellido LIKE :search_last_name OR a.matricula LIKE :search_student_number OR m.nombre_materia LIKE :search_subject OR cn.titulo LIKE :search_evaluation OR g.observaciones LIKE :search_notes)";
            foreach (['first_name', 'last_name', 'student_number', 'subject', 'evaluation', 'notes'] as $field) {
                $parameters['search_' . $field] = '%' . $busqueda . '%';
            }
        }
        $where = $conditions === [] ? '' : ' WHERE ' . implode(' AND ', $conditions);
        $sql = "SELECT g.id_calificacion, i.id_inscripcion, cn.id_columna_notas,
            CONCAT(a.apellido, ', ', a.nombre) AS alumno,
            CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS curso,
            cn.titulo AS evaluacion, g.nota, cn.nota_maxima, g.fecha_carga, g.observaciones,
            (SELECT COUNT(*) FROM calificacion_historial h WHERE h.id_calificacion = g.id_calificacion) AS correcciones "
            . $this->consultaCalificaciones() . $where . ' ORDER BY a.apellido, a.nombre LIMIT 250';
        $query = $this->conexion->prepare($sql);
        $query->execute($parameters);
        return $query->fetchAll(PDO::FETCH_ASSOC);
    }

    public function obtenerCalificacionDeUsuario(int $id, array $usuario): ?array
    {
        [$scope, $parameters] = $this->alcancePorUsuario($usuario);
        $where = ' WHERE g.id_calificacion = :id' . ($scope === '' ? '' : ' AND ' . $scope);
        $query = $this->conexion->prepare(
            'SELECT g.*, i.id_alumno, c.id_docente ' . $this->consultaCalificaciones() . $where
        );
        $query->execute(['id' => $id] + $parameters);
        $row = $query->fetch(PDO::FETCH_ASSOC);
        return $row ?: null;
    }

    public function opcionesDeCalificacion(array $usuario, ?int $selectedEnrollment = null, ?int $selectedEvaluation = null): array
    {
        $enrollmentSql = "SELECT i.id_inscripcion AS id,
            CONCAT(a.apellido, ', ', a.nombre, ' - ', c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS etiqueta
            FROM inscripcion i INNER JOIN alumno a ON a.id_alumno = i.id_alumno
            INNER JOIN curso c ON c.id_curso = i.id_curso INNER JOIN materia m ON m.id_materia = c.id_materia
            WHERE i.estado IN ('Confirmada', 'Finalizada')";
        $evaluationSql = "SELECT cn.id_columna_notas AS id,
            CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia, ' - ', cn.titulo, ' (máximo ', cn.nota_maxima, ')') AS etiqueta
            FROM columna_notas cn INNER JOIN curso c ON c.id_curso = cn.id_curso
            INNER JOIN materia m ON m.id_materia = c.id_materia WHERE cn.estado = 'Abierta'";
        $enrollmentParameters = [];
        $evaluationParameters = [];
        if ($usuario['isTeacher']) {
            $enrollmentSql .= ' AND c.id_docente = :teacher';
            $evaluationSql .= ' AND c.id_docente = :teacher';
            $enrollmentParameters['teacher'] = (int) $usuario['id_docente'];
            $evaluationParameters['teacher'] = (int) $usuario['id_docente'];
        } elseif ($usuario['isStudent']) {
            $enrollmentSql .= ' AND i.id_alumno = :student';
            $enrollmentParameters['student'] = (int) $usuario['id_alumno'];
        }
        $enrollmentSql .= ' ORDER BY a.apellido, a.nombre LIMIT 300';
        $evaluationSql .= ' ORDER BY cn.fecha_evaluacion DESC, cn.titulo LIMIT 300';
        $enrollments = $this->conexion->prepare($enrollmentSql);
        $enrollments->execute($enrollmentParameters);
        $evaluations = $this->conexion->prepare($evaluationSql);
        $evaluations->execute($evaluationParameters);
        $enrollmentRows = $enrollments->fetchAll(PDO::FETCH_ASSOC);
        $evaluationRows = $evaluations->fetchAll(PDO::FETCH_ASSOC);
        if ($selectedEnrollment !== null && !in_array($selectedEnrollment, array_column($enrollmentRows, 'id'))) {
            $selected = $this->conexion->prepare(
                "SELECT i.id_inscripcion AS id, CONCAT(a.apellido, ', ', a.nombre, ' - ', c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS etiqueta
                FROM inscripcion i INNER JOIN alumno a ON a.id_alumno = i.id_alumno
                INNER JOIN curso c ON c.id_curso = i.id_curso INNER JOIN materia m ON m.id_materia = c.id_materia
                WHERE i.id_inscripcion = ?"
                . ($usuario['isTeacher'] ? ' AND c.id_docente = ?' : '')
                . ($usuario['isStudent'] ? ' AND i.id_alumno = ?' : '')
            );
            $selectedParameters = [$selectedEnrollment];
            if ($usuario['isTeacher']) {
                $selectedParameters[] = (int) $usuario['id_docente'];
            }
            if ($usuario['isStudent']) {
                $selectedParameters[] = (int) $usuario['id_alumno'];
            }
            $selected->execute($selectedParameters);
            $row = $selected->fetch(PDO::FETCH_ASSOC);
            if ($row) {
                $enrollmentRows[] = $row;
            }
        }
        if ($selectedEvaluation !== null && !in_array($selectedEvaluation, array_column($evaluationRows, 'id'))) {
            $selected = $this->conexion->prepare(
                "SELECT cn.id_columna_notas AS id, CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia, ' - ', cn.titulo, ' (máximo ', cn.nota_maxima, ')') AS etiqueta
                FROM columna_notas cn INNER JOIN curso c ON c.id_curso = cn.id_curso
                INNER JOIN materia m ON m.id_materia = c.id_materia WHERE cn.id_columna_notas = ?"
                . ($usuario['isTeacher'] ? ' AND c.id_docente = ?' : '')
            );
            $selectedParameters = [$selectedEvaluation];
            if ($usuario['isTeacher']) {
                $selectedParameters[] = (int) $usuario['id_docente'];
            }
            $selected->execute($selectedParameters);
            $row = $selected->fetch(PDO::FETCH_ASSOC);
            if ($row) {
                $evaluationRows[] = $row;
            }
        }
        return [
            'inscripciones' => $enrollmentRows,
            'evaluaciones' => $evaluationRows,
        ];
    }

    public function guardarCalificacion(int $id, array $datos, array $usuario): void
    {
        $inscripcion = filter_var($datos['id_inscripcion'] ?? null, FILTER_VALIDATE_INT);
        $evaluacion = filter_var($datos['id_columna_notas'] ?? null, FILTER_VALIDATE_INT);
        $nota = filter_var($datos['nota'] ?? null, FILTER_VALIDATE_FLOAT);
        $observaciones = trim((string) ($datos['observaciones'] ?? ''));
        if (!$inscripcion || !$evaluacion || $nota === false || $nota < 0 || strlen($observaciones) > 255) {
            throw new InvalidArgumentException('Revisá la inscripción, la evaluación y la nota ingresadas.');
        }

        $actual = $id > 0 ? $this->obtenerCalificacionDeUsuario($id, $usuario) : null;
        if ($id > 0 && $actual === null) {
            throw new InvalidArgumentException('La calificación no existe o no pertenece a tus cursos.');
        }
        if ($usuario['isTeacher'] && ($actual === null || (int) $actual['id_inscripcion'] !== $inscripcion || (int) $actual['id_columna_notas'] !== $evaluacion)) {
            throw new InvalidArgumentException('Solo podés editar una nota existente y no cambiar su alumno ni evaluación.');
        }
        $relacion = $this->conexion->prepare(
            "SELECT i.id_curso AS curso_inscripcion, i.id_alumno, i.estado AS estado_inscripcion,
                cn.id_curso AS curso_evaluacion, cn.nota_maxima, cn.estado AS estado_evaluacion, c.id_docente
            FROM inscripcion i INNER JOIN columna_notas cn ON cn.id_columna_notas = ?
            INNER JOIN curso c ON c.id_curso = i.id_curso WHERE i.id_inscripcion = ?"
        );
        $relacion->execute([$evaluacion, $inscripcion]);
        $registro = $relacion->fetch(PDO::FETCH_ASSOC);
        if (!$registro || (int) $registro['curso_inscripcion'] !== (int) $registro['curso_evaluacion']
            || $nota > (float) $registro['nota_maxima']) {
            throw new InvalidArgumentException('La evaluación no corresponde al curso o la nota supera el máximo permitido.');
        }
        if ($actual && ((int) $actual['id_inscripcion'] !== $inscripcion || (int) $actual['id_columna_notas'] !== $evaluacion)) {
            throw new InvalidArgumentException('No se puede cambiar el alumno o la evaluación de una calificación existente.');
        }
        if ($usuario['isTeacher'] && (int) $registro['id_docente'] !== (int) $usuario['id_docente']) {
            throw new InvalidArgumentException('Solo podés cargar notas de tus cursos.');
        }
        if (!$actual && (!in_array($registro['estado_inscripcion'], ['Confirmada', 'Finalizada'], true) || $registro['estado_evaluacion'] !== 'Abierta')) {
            throw new InvalidArgumentException('Solo se pueden calificar inscripciones confirmadas o finalizadas.');
        }

        $this->conexion->beginTransaction();
        try {
            if ($actual) {
                if ((float) $actual['nota'] !== (float) $nota) {
                    $motivo = trim((string) ($datos['motivo_correccion'] ?? ''));
                    if ($motivo === '' || preg_match_all('/./us', $motivo) > 255) {
                        throw new InvalidArgumentException('Indicá un motivo de corrección de hasta 255 caracteres.');
                    }
                    $history = $this->conexion->prepare(
                        'INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva, motivo) VALUES (?, ?, ?, ?)'
                    );
                    $history->execute([$id, $actual['nota'], $nota, $motivo]);
                }
                $query = $this->conexion->prepare('UPDATE calificacion SET nota = ?, observaciones = ? WHERE id_calificacion = ?');
                $query->execute([$nota, $observaciones, $id]);
            } else {
                $query = $this->conexion->prepare(
                    'INSERT INTO calificacion (id_inscripcion, id_columna_notas, nota, fecha_carga, observaciones) VALUES (?, ?, ?, CURRENT_DATE, ?)'
                );
                $query->execute([$inscripcion, $evaluacion, $nota, $observaciones]);
            }
            $this->conexion->commit();
        } catch (Throwable $exception) {
            if ($this->conexion->inTransaction()) {
                $this->conexion->rollBack();
            }
            throw $exception;
        }
    }

    public function eliminarCalificacion(int $id): void
    {
        $this->conexion->beginTransaction();
        try {
            $lock = $this->conexion->prepare('SELECT id_calificacion FROM calificacion WHERE id_calificacion = ? FOR UPDATE');
            $lock->execute([$id]);
            if ($lock->fetchColumn() === false) {
                throw new InvalidArgumentException('La calificación ya no existe.');
            }
            $history = $this->conexion->prepare('SELECT COUNT(*) FROM calificacion_historial WHERE id_calificacion = ?');
            $history->execute([$id]);
            if ((int) $history->fetchColumn() > 0) {
                throw new InvalidArgumentException('No se puede eliminar una calificación que tiene correcciones auditadas.');
            }
            $query = $this->conexion->prepare('DELETE FROM calificacion WHERE id_calificacion = ?');
            $query->execute([$id]);
            $this->conexion->commit();
        } catch (Throwable $exception) {
            if ($this->conexion->inTransaction()) {
                $this->conexion->rollBack();
            }
            throw $exception;
        }
    }
}
?>
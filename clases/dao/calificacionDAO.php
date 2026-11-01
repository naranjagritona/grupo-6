<?php

require_once __DIR__ . '/dao.php';

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

    public function listarCalificacionesDeUsuario(
        array $usuario,
        string $busqueda = '',
        ?int $idCurso = null,
        ?int $idMateria = null,
        ?int $idEvaluacion = null
    ): array
    {
        [$scope, $parameters] = $this->alcancePorUsuario($usuario);
        $conditions = $scope === '' ? [] : [$scope];
        if ($idCurso !== null && $idCurso > 0) {
            $conditions[] = 'c.id_curso = :filter_course';
            $parameters['filter_course'] = $idCurso;
        }
        if ($idMateria !== null && $idMateria > 0) {
            $conditions[] = 'm.id_materia = :filter_subject';
            $parameters['filter_subject'] = $idMateria;
        }
        if ($idEvaluacion !== null && $idEvaluacion > 0) {
            $conditions[] = 'cn.id_columna_notas = :filter_evaluation';
            $parameters['filter_evaluation'] = $idEvaluacion;
        }
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

    public function opcionesFiltrosCalificacion(
        array $usuario,
        ?int $idCurso = null,
        ?int $idMateria = null
    ): array
    {
        [$scope, $parameters] = $this->alcancePorUsuario($usuario);
        $conditions = $scope === '' ? [] : [$scope];
        $where = $conditions === [] ? '' : ' WHERE ' . implode(' AND ', $conditions);
        $courses = $this->conexion->prepare(
            "SELECT DISTINCT c.id_curso AS id,
                CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia, ' - ', p.nombre) AS etiqueta,
                p.fecha_inicio AS fecha_orden
             FROM curso c INNER JOIN materia m ON m.id_materia = c.id_materia
             INNER JOIN periodo_lectivo p ON p.id_periodo_lectivo = c.id_periodo
             LEFT JOIN inscripcion i ON i.id_curso = c.id_curso" . $where .
            ' ORDER BY p.fecha_inicio DESC, c.`año`, c.division, m.nombre_materia LIMIT 500'
        );
        $courses->execute($parameters);
        $subjectConditions = $conditions;
        $subjectParameters = $parameters;
        if ($idCurso !== null && $idCurso > 0) {
            $subjectConditions[] = 'c.id_curso = :filter_course';
            $subjectParameters['filter_course'] = $idCurso;
        }
        $subjectWhere = $subjectConditions === [] ? '' : ' WHERE ' . implode(' AND ', $subjectConditions);
        $subjects = $this->conexion->prepare(
            "SELECT DISTINCT m.id_materia AS id, m.nombre_materia AS etiqueta
             FROM curso c INNER JOIN materia m ON m.id_materia = c.id_materia
             LEFT JOIN inscripcion i ON i.id_curso = c.id_curso" . $subjectWhere .
            ' ORDER BY m.nombre_materia LIMIT 250'
        );
        $subjects->execute($subjectParameters);
        $evaluationConditions = $subjectConditions;
        $evaluationParameters = $subjectParameters;
        if ($idMateria !== null && $idMateria > 0) {
            $evaluationConditions[] = 'm.id_materia = :filter_subject';
            $evaluationParameters['filter_subject'] = $idMateria;
        }
        $evaluationWhere = $evaluationConditions === [] ? '' : ' WHERE ' . implode(' AND ', $evaluationConditions);
        $evaluations = $this->conexion->prepare(
            "SELECT DISTINCT cn.id_columna_notas AS id,
                CONCAT(cn.titulo, ' - ', m.nombre_materia, ' (', cn.tipo, ')') AS etiqueta,
                cn.fecha_evaluacion AS fecha_orden
             FROM columna_notas cn INNER JOIN curso c ON c.id_curso = cn.id_curso
             INNER JOIN materia m ON m.id_materia = c.id_materia
             LEFT JOIN inscripcion i ON i.id_curso = c.id_curso" . $evaluationWhere .
            ' ORDER BY cn.fecha_evaluacion DESC, cn.titulo LIMIT 500'
        );
        $evaluations->execute($evaluationParameters);
        return [
            'cursos' => $courses->fetchAll(PDO::FETCH_ASSOC),
            'materias' => $subjects->fetchAll(PDO::FETCH_ASSOC),
            'evaluaciones' => $evaluations->fetchAll(PDO::FETCH_ASSOC),
        ];
    }

    public function listarPlanillaDeCurso(
        int $idCurso,
        array $usuario,
        ?int $idMateria = null,
        ?int $idEvaluacion = null
    ): array {
        $conditions = ['c.id_curso = :course', "i.estado IN ('Confirmada', 'Finalizada')"];
        $parameters = ['course' => $idCurso];
        if ($usuario['isTeacher']) {
            $conditions[] = 'c.id_docente = :scope';
            $parameters['scope'] = (int) $usuario['id_docente'];
        } elseif ($usuario['isStudent']) {
            $conditions[] = 'i.id_alumno = :scope';
            $parameters['scope'] = (int) $usuario['id_alumno'];
        } elseif (!$usuario['isAdmin']) {
            throw new RuntimeException('No tenés permisos para ver calificaciones.');
        }
        if ($idMateria !== null && $idMateria > 0) {
            $conditions[] = 'm.id_materia = :subject';
            $parameters['subject'] = $idMateria;
        }
        if ($idEvaluacion !== null && $idEvaluacion > 0) {
            $conditions[] = 'cn.id_columna_notas = :evaluation';
            $parameters['evaluation'] = $idEvaluacion;
        }
        $query = $this->conexion->prepare(
            'SELECT i.id_inscripcion, CONCAT(a.apellido, ", ", a.nombre) AS alumno,
                a.matricula, cn.id_columna_notas, cn.titulo, cn.tipo, cn.nota_maxima,
                cn.nota_aprobacion, cn.estado AS estado_evaluacion, cn.fecha_evaluacion,
                g.id_calificacion, g.nota, g.observaciones
             FROM inscripcion i INNER JOIN alumno a ON a.id_alumno = i.id_alumno
             INNER JOIN curso c ON c.id_curso = i.id_curso
             LEFT JOIN materia m ON m.id_materia = c.id_materia
             LEFT JOIN columna_notas cn ON cn.id_curso = c.id_curso
             LEFT JOIN calificacion g ON g.id_inscripcion = i.id_inscripcion
                AND g.id_columna_notas = cn.id_columna_notas
             WHERE ' . implode(' AND ', $conditions) .
            ' ORDER BY a.apellido, a.nombre, cn.fecha_evaluacion, cn.id_columna_notas'
        );
        $query->execute($parameters);
        return $query->fetchAll(PDO::FETCH_ASSOC);
    }

    public function listarPromocionPorUsuario(
        array $usuario,
        float $notaMinima = 6.0,
        int $materiasDesaprobadasPermitidas = 0
    ): array {
        if (!$usuario['isAdmin'] && !$usuario['isStudent']) return [];
        [$scope, $parameters] = $this->alcancePorUsuario($usuario);
        $conditions = ["i.estado IN ('Confirmada', 'Finalizada')", "c.estado <> 'Cancelado'"];
        if ($scope !== '') $conditions[] = $scope;
        $query = $this->conexion->prepare(
            "SELECT i.id_alumno, CONCAT(a.apellido, ', ', a.nombre) AS alumno,
                c.id_periodo, c.`año` AS anio, c.division, c.estado AS estado_curso,
                m.id_materia, m.nombre_materia AS materia, cn.id_columna_notas,
                g.nota, cn.nota_maxima, p.nombre AS periodo
             FROM inscripcion i INNER JOIN alumno a ON a.id_alumno = i.id_alumno
             INNER JOIN curso c ON c.id_curso = i.id_curso
             INNER JOIN materia m ON m.id_materia = c.id_materia
             INNER JOIN periodo_lectivo p ON p.id_periodo_lectivo = c.id_periodo
             LEFT JOIN columna_notas cn ON cn.id_curso = c.id_curso
             LEFT JOIN calificacion g ON g.id_inscripcion = i.id_inscripcion
                AND g.id_columna_notas = cn.id_columna_notas
             WHERE " . implode(' AND ', $conditions) .
            ' ORDER BY a.apellido, a.nombre, c.`año`, c.division, p.fecha_inicio, m.nombre_materia'
        );
        $query->execute($parameters);
        $grupos = [];
        foreach ($query->fetchAll(PDO::FETCH_ASSOC) as $fila) {
            $clave = implode('|', [$fila['id_alumno'], $fila['id_periodo'], $fila['anio'], $fila['division']]);
            if (!isset($grupos[$clave])) {
                $grupos[$clave] = [
                    'alumno' => $fila['alumno'], 'anio' => $fila['anio'],
                    'division' => $fila['division'], 'periodo' => $fila['periodo'],
                    'materias' => [], 'en_curso' => false,
                ];
            }
            $materia = (string) $fila['id_materia'];
            if (!isset($grupos[$clave]['materias'][$materia])) {
                $grupos[$clave]['materias'][$materia] = [
                    'nombre' => $fila['materia'], 'puntos' => 0.0, 'maximo' => 0.0,
                    'evaluaciones' => 0, 'calificadas' => 0, 'curso_finalizado' => false,
                ];
            }
            if ($fila['estado_curso'] === 'Finalizado') {
                $grupos[$clave]['materias'][$materia]['curso_finalizado'] = true;
            } else {
                $grupos[$clave]['en_curso'] = true;
            }
            if ($fila['id_columna_notas'] !== null) {
                $grupos[$clave]['materias'][$materia]['evaluaciones']++;
                if ($fila['nota'] !== null && (float) $fila['nota_maxima'] > 0) {
                    $grupos[$clave]['materias'][$materia]['puntos'] += (float) $fila['nota'];
                    $grupos[$clave]['materias'][$materia]['maximo'] += (float) $fila['nota_maxima'];
                    $grupos[$clave]['materias'][$materia]['calificadas']++;
                }
            }
        }
        $resultado = [];
        foreach ($grupos as $grupo) {
            $materias = array_values($grupo['materias']);
            $aprobadas = $desaprobadas = $incompletas = 0;
            foreach ($materias as &$materia) {
                $materia['promedio'] = $materia['maximo'] > 0
                    ? 10 * $materia['puntos'] / $materia['maximo'] : null;
                if ($materia['promedio'] === null || ($materia['curso_finalizado']
                    && ($materia['evaluaciones'] === 0 || $materia['calificadas'] < $materia['evaluaciones']))) {
                    $incompletas++;
                } elseif ($materia['promedio'] >= $notaMinima) {
                    $aprobadas++;
                } else {
                    $desaprobadas++;
                }
            }
            unset($materia);
            if ($grupo['en_curso']) $estado = 'En curso';
            elseif ($incompletas > 0) $estado = 'Incompleto';
            else $estado = $desaprobadas <= $materiasDesaprobadasPermitidas ? 'Promociona' : 'No promociona';
            $resultado[] = [
                'alumno' => $grupo['alumno'], 'anio' => $grupo['anio'],
                'division' => $grupo['division'], 'periodo' => $grupo['periodo'],
                'cantidad_materias' => count($materias), 'aprobadas' => $aprobadas,
                'desaprobadas' => $desaprobadas, 'incompletas' => $incompletas,
                'estado' => $estado, 'materias_detalle' => $materias,
            ];
        }
        return $resultado;
    }

    public function guardarEvaluacion(int $idCurso, array $datos, array $usuario): void
    {
        if (!$usuario['isAdmin'] && !$usuario['isTeacher']) {
            throw new InvalidArgumentException('No tenés permisos para agregar evaluaciones.');
        }
        $titulo = trim((string) ($datos['titulo'] ?? ''));
        $tipo = trim((string) ($datos['tipo'] ?? ''));
        $maximo = filter_var($datos['nota_maxima'] ?? null, FILTER_VALIDATE_FLOAT);
        $aprobacion = filter_var($datos['nota_aprobacion'] ?? null, FILTER_VALIDATE_FLOAT);
        $fecha = trim((string) ($datos['fecha_evaluacion'] ?? ''));
        $tiposPermitidos = ['Evaluación', 'Trabajo Práctico', 'Recuperatorio', 'Otro'];
        $fechaValida = DateTimeImmutable::createFromFormat('!Y-m-d', $fecha);
        if ($titulo === '' || preg_match_all('/./us', $titulo) > 200
            || !in_array($tipo, $tiposPermitidos, true)
            || $maximo === false || $maximo <= 0
            || $aprobacion === false || $aprobacion < 0 || $aprobacion > $maximo
            || !$fechaValida || $fechaValida->format('Y-m-d') !== $fecha) {
            throw new InvalidArgumentException('Revisá el título, el tipo, la escala y la fecha de evaluación.');
        }
        $curso = $this->conexion->prepare('SELECT id_docente FROM curso WHERE id_curso = ?');
        $curso->execute([$idCurso]);
        $docente = $curso->fetchColumn();
        if ($docente === false || ($usuario['isTeacher'] && (int) $docente !== (int) $usuario['id_docente'])) {
            throw new InvalidArgumentException('El curso no existe o no está asignado a tu cuenta.');
        }
        $query = $this->conexion->prepare(
            "INSERT INTO columna_notas (id_curso, titulo, tipo, nota_maxima, nota_aprobacion,
                fecha_evaluacion, fecha_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'Abierta')"
        );
        $query->execute([$idCurso, $titulo, $tipo, $maximo, $aprobacion, $fecha, $fecha]);
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
        if ($usuario['isTeacher'] && $id > 0 && ($actual === null || (int) $actual['id_inscripcion'] !== $inscripcion || (int) $actual['id_columna_notas'] !== $evaluacion)) {
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

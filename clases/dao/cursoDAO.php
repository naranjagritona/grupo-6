<?php

require_once 'dao.php';

class CursoDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($curso): void {
        $sql = "INSERT INTO curso (id_periodo, id_materia, id_docente, año, division, modalidad, turno, cupo_maximo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $curso->getId_periodo(),
            $curso->getId_materia(),
            $curso->getId_docente(),
            $curso->getAño(),
            $curso->getDivision(),
            $curso->getModalidad()->value,
            $curso->getTurno()->value,
            $curso->getCupo_maximo(),
            $curso->getFecha_inicio(),
            $curso->getFecha_fin(),
            $curso->getEstado()->value
        ]);
    }

    public function modificar($curso) {
        $sql = "UPDATE curso SET id_periodo = ?, id_materia = ?, id_docente = ?, año = ?, division = ?, modalidad = ?, turno = ?, cupo_maximo = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_curso = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $curso->getId_periodo(),
            $curso->getId_materia(),
            $curso->getId_docente(),
            $curso->getAño(),
            $curso->getDivision(),
            $curso->getModalidad()->value,
            $curso->getTurno()->value,
            $curso->getCupo_maximo(),
            $curso->getFecha_inicio(),
            $curso->getFecha_fin(),
            $curso->getEstado()->value,
            $curso->getId_curso()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM curso WHERE id_curso = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM curso";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Curso(
                $fila['id_curso'],
                $fila['id_periodo'],
                $fila['id_materia'],
                $fila['id_docente'],
                $fila['año'],
                $fila['division'],
                ModalidadCurso::from($fila['modalidad']),
                TurnoCurso::from($fila['turno']),
                $fila['cupo_maximo'],
                $fila['fecha_inicio'],
                $fila['fecha_fin'],
                EstadoCurso::from($fila['estado'])
            );
        }
        return $resultados;
    }

    public function listarCursosConUsuario(array $usuario, string $busqueda = '', string $estado = ''): array
    {
        $sql = "SELECT c.id_curso, c.id_periodo, c.id_materia, c.id_docente, c.`año`, c.division,
            c.modalidad, c.turno, c.cupo_maximo, c.fecha_inicio, c.fecha_fin, c.estado,
            CONCAT(c.`año`, '° ', c.division, ' - ', COALESCE(m.nombre_materia, 'Materia no disponible')) AS curso,
            COALESCE(CONCAT(d.apellido, ', ', d.nombre), 'Docente no disponible') AS docente,
            COALESCE(p.nombre, 'Período no disponible') AS periodo
            FROM curso c
            LEFT JOIN materia m ON m.id_materia = c.id_materia
            LEFT JOIN docente d ON d.id_docente = c.id_docente
            LEFT JOIN periodo_lectivo p ON p.id_periodo_lectivo = c.id_periodo";
        $conditions = [];
        $parameters = [];
        if ($usuario['isTeacher']) {
            $conditions[] = 'c.id_docente = :scope';
            $parameters['scope'] = (int) $usuario['id_docente'];
        } elseif ($usuario['isStudent']) {
            $conditions[] = 'EXISTS (SELECT 1 FROM inscripcion si WHERE si.id_curso = c.id_curso AND si.id_alumno = :scope)';
            $parameters['scope'] = (int) $usuario['id_alumno'];
        }
        if ($busqueda !== '') {
            $conditions[] = "(c.`año` LIKE :search_year OR c.division LIKE :search_division OR m.nombre_materia LIKE :search_subject OR d.nombre LIKE :search_first_name OR d.apellido LIKE :search_last_name OR p.nombre LIKE :search_period)";
            foreach (['year', 'division', 'subject', 'first_name', 'last_name', 'period'] as $field) {
                $parameters['search_' . $field] = '%' . $busqueda . '%';
            }
        }
        if ($estado !== '' && in_array($estado, ['Activo', 'Finalizado', 'Cancelado'], true)) {
            $conditions[] = 'c.estado = :status';
            $parameters['status'] = $estado;
        }
        if ($conditions !== []) {
            $sql .= ' WHERE ' . implode(' AND ', $conditions);
        }
        $sql .= ' ORDER BY p.fecha_inicio DESC, c.`año`, c.division LIMIT 275';
        $query = $this->conexion->prepare($sql);
        $query->execute($parameters);
        return $query->fetchAll(PDO::FETCH_ASSOC);
    }

    public function obtenerCursoConUsuario(int $id, array $usuario): ?array
    {
        $sql = "SELECT c.id_curso, c.id_periodo, c.id_materia, c.id_docente, c.`año`, c.division,
            c.modalidad, c.turno, c.cupo_maximo, c.fecha_inicio, c.fecha_fin, c.estado,
            CONCAT(c.`año`, '° ', c.division, ' - ', COALESCE(m.nombre_materia, 'Materia no disponible')) AS curso,
            COALESCE(CONCAT(d.apellido, ', ', d.nombre), 'Docente no disponible') AS docente,
            COALESCE(p.nombre, 'Período no disponible') AS periodo
            FROM curso c LEFT JOIN materia m ON m.id_materia = c.id_materia
            LEFT JOIN docente d ON d.id_docente = c.id_docente
            LEFT JOIN periodo_lectivo p ON p.id_periodo_lectivo = c.id_periodo WHERE c.id_curso = :id";
        $parameters = ['id' => $id];
        if ($usuario['isTeacher']) {
            $sql .= ' AND c.id_docente = :scope';
            $parameters['scope'] = (int) $usuario['id_docente'];
        } elseif ($usuario['isStudent']) {
            $sql .= ' AND EXISTS (SELECT 1 FROM inscripcion si WHERE si.id_curso = c.id_curso AND si.id_alumno = :scope)';
            $parameters['scope'] = (int) $usuario['id_alumno'];
        }
        $query = $this->conexion->prepare($sql);
        $query->execute($parameters);
        $row = $query->fetch(PDO::FETCH_ASSOC);
        return $row ?: null;
    }

    public function opcionesDeCurso(?int $cursoActual = null): array
    {
        return [
            'periodos' => $this->conexion->query(
                'SELECT id_periodo_lectivo AS id, nombre AS etiqueta FROM periodo_lectivo ORDER BY fecha_inicio DESC'
            )->fetchAll(PDO::FETCH_ASSOC),
            'materias' => $this->conexion->query(
                'SELECT id_materia AS id, nombre_materia AS etiqueta FROM materia ORDER BY nombre_materia'
            )->fetchAll(PDO::FETCH_ASSOC),
            'docentes' => $this->obtenerOpcionesDocentes($cursoActual),
        ];
    }

    private function obtenerOpcionesDocentes(?int $cursoActual): array
    {
        $sql = "SELECT id_docente AS id, CONCAT(apellido, ', ', nombre) AS etiqueta
            FROM docente WHERE estado = 'Activo'";
        if ($cursoActual !== null) {
            $sql .= ' OR id_docente = (SELECT id_docente FROM curso WHERE id_curso = ' . (int) $cursoActual . ')';
        }
        $sql .= ' ORDER BY apellido, nombre';
        return $this->conexion->query($sql)->fetchAll(PDO::FETCH_ASSOC);
    }

    public function guardarCurso(int $id, array $datos): void
    {
        $modalidad = ['Presencial', 'Virtual', 'Híbrido'];
        $turnos = ['mañana', 'tarde', 'vespertino'];
        $estados = ['Activo', 'Finalizado', 'Cancelado'];
        if (!in_array($datos['modalidad'] ?? '', $modalidad, true)
            || !in_array($datos['turno'] ?? '', $turnos, true)
            || !in_array($datos['estado'] ?? '', $estados, true)) {
            throw new InvalidArgumentException('Seleccioná modalidad, turno y estado válidos.');
        }
        $periodo = filter_var($datos['id_periodo'] ?? null, FILTER_VALIDATE_INT);
        $materia = filter_var($datos['id_materia'] ?? null, FILTER_VALIDATE_INT);
        $docente = filter_var($datos['id_docente'] ?? null, FILTER_VALIDATE_INT);
        $cupo = filter_var($datos['cupo_maximo'] ?? null, FILTER_VALIDATE_INT);
        $anio = trim((string) ($datos['año'] ?? ''));
        $division = trim((string) ($datos['division'] ?? ''));
        $inicio = (string) ($datos['fecha_inicio'] ?? '');
        $fin = (string) ($datos['fecha_fin'] ?? '');
        $fechaInicio = DateTimeImmutable::createFromFormat('!Y-m-d', $inicio);
        $fechaFin = DateTimeImmutable::createFromFormat('!Y-m-d', $fin);
        if (!$periodo || !$materia || !$docente || !$cupo || $cupo > 1000 || $anio === '' || $division === ''
            || strlen($anio) > 200 || strlen($division) > 200
            || !$fechaInicio || $fechaInicio->format('Y-m-d') !== $inicio
            || !$fechaFin || $fechaFin->format('Y-m-d') !== $fin || $fin < $inicio) {
            throw new InvalidArgumentException('Revisá los campos obligatorios y las fechas del curso.');
        }

        $this->conexion->beginTransaction();
        try {
            if ($id > 0) {
                $lock = $this->conexion->prepare('SELECT id_curso FROM curso WHERE id_curso = ? FOR UPDATE');
                $lock->execute([$id]);
                if (!$lock->fetchColumn()) {
                    throw new InvalidArgumentException('El curso ya no existe.');
                }
                $conteo = $this->conexion->prepare(
                    "SELECT COUNT(*) FROM inscripcion WHERE id_curso = ? AND estado = 'Confirmada'"
                );
                $conteo->execute([$id]);
                if ((int) $conteo->fetchColumn() > $cupo) {
                    throw new InvalidArgumentException('El cupo no puede ser menor que las inscripciones confirmadas.');
                }
                $query = $this->conexion->prepare(
                    'UPDATE curso SET id_periodo=?, id_materia=?, id_docente=?, `año`=?, division=?, modalidad=?, turno=?, cupo_maximo=?, fecha_inicio=?, fecha_fin=?, estado=? WHERE id_curso=?'
                );
                $values = [$periodo, $materia, $docente, $anio, $division, $datos['modalidad'], $datos['turno'], $cupo, $inicio, $fin, $datos['estado'], $id];
            } else {
                $query = $this->conexion->prepare(
                    'INSERT INTO curso (id_periodo, id_materia, id_docente, `año`, division, modalidad, turno, cupo_maximo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)'
                );
                $values = [$periodo, $materia, $docente, $anio, $division, $datos['modalidad'], $datos['turno'], $cupo, $inicio, $fin, $datos['estado']];
            }
            $query->execute($values);
            $this->conexion->commit();
        } catch (Throwable $exception) {
            if ($this->conexion->inTransaction()) {
                $this->conexion->rollBack();
            }
            throw $exception;
        }
    }

    public function cancelarCurso(int $id): void
    {
        $query = $this->conexion->prepare("UPDATE curso SET estado = 'Cancelado' WHERE id_curso = ?");
        $query->execute([$id]);
        if ($query->rowCount() === 0) {
            $exists = $this->conexion->prepare('SELECT COUNT(*) FROM curso WHERE id_curso = ?');
            $exists->execute([$id]);
            if ((int) $exists->fetchColumn() === 0) {
                throw new InvalidArgumentException('El curso ya no existe.');
            }
        }
    }
}
?>
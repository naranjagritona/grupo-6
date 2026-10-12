<?php
declare(strict_types=1);

require_once __DIR__ . '/database.php';

function moduleDefinitions(): array
{
    $statusOptions = [
        ['Activo', 'Activo'],
        ['Inactivo', 'Inactivo'],
    ];

    return [
        'alumnos' => [
            'title' => 'Alumnos',
            'description' => 'Administrá estudiantes, responsables y su estado académico.',
            'table' => 'alumno',
            'alias' => 'a',
            'id' => 'id_alumno',
            'idExpression' => 'a.id_alumno',
            'from' => 'FROM alumno AS a LEFT JOIN responsable AS r ON r.id_responsable = a.id_responsable',
            'listSql' => "SELECT a.id_alumno AS id, CONCAT(a.apellido, ', ', a.nombre) AS alumno,
                a.dni, a.matricula,
                CONCAT(COALESCE(r.nombre, ''), ' ', COALESCE(r.apellido, '')) AS responsable,
                a.estado,
                (SELECT ROUND(AVG(rm.promedio), 2)
                 FROM resultado_materia AS rm
                 INNER JOIN inscripcion AS i ON i.id_inscripcion = rm.id_inscripcion
                 WHERE i.id_alumno = a.id_alumno) AS promedio",
            'searchFields' => ['a.nombre', 'a.apellido', 'a.dni', 'a.matricula', 'r.nombre', 'r.apellido'],
            'statusExpression' => 'a.estado',
            'sortOptions' => [
                'alumno' => 'a.apellido',
                'dni' => 'a.dni',
                'matricula' => 'a.matricula',
                'estado' => 'a.estado',
            ],
            'statusOptions' => [['Activo', 'Activos'], ['Egresado', 'Egresados'], ['Inactivo', 'Inactivos']],
            'deactivateValue' => 'Inactivo',
            'newAccountRole' => 4,
            'uniqueFields' => ['dni' => 'DNI', 'matricula' => 'matrícula'],
            'summary' => [
                ['label' => 'Alumnos', 'sql' => 'SELECT COUNT(*) FROM alumno'],
                ['label' => 'Activos', 'sql' => "SELECT COUNT(*) FROM alumno WHERE estado = 'Activo'"],
                ['label' => 'Egresados', 'sql' => "SELECT COUNT(*) FROM alumno WHERE estado = 'Egresado'"],
                ['label' => 'Con responsable', 'sql' => 'SELECT COUNT(*) FROM alumno WHERE id_responsable > 0'],
            ],
            'columns' => [
                'alumno' => 'Alumno',
                'dni' => 'DNI',
                'matricula' => 'Matrícula',
                'responsable' => 'Responsable',
                'promedio' => 'Promedio',
                'estado' => 'Estado',
            ],
            'fields' => [
                'id_usuario' => [
                    'label' => 'Cuenta de acceso disponible',
                    'type' => 'select',
                    'required' => false,
                    'optionsSql' => "SELECT u.id_usuario AS id, CONCAT(u.nombre_usuario, ' (cuenta #', u.id_usuario, ')') AS label
                        FROM usuario AS u
                        WHERE u.id_rol = 4
                        AND NOT EXISTS (SELECT 1 FROM alumno AS a WHERE a.id_usuario = u.id_usuario AND a.id_alumno <> :record_id)
                        ORDER BY u.nombre_usuario",
                ],
                '_nuevo_usuario' => ['label' => 'Nombre de usuario nuevo (si no elegís cuenta existente)', 'type' => 'text', 'required' => false, 'maxLength' => 200],
                '_clave_usuario' => ['label' => 'Contraseña inicial (mínimo 12 caracteres)', 'type' => 'password', 'required' => false, 'maxLength' => 200],
                'id_responsable' => [
                    'label' => 'Responsable',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => "SELECT id_responsable AS id,
                        CONCAT(apellido, ', ', nombre, ' - ', tipo_relacion) AS label
                        FROM responsable ORDER BY apellido, nombre",
                ],
                'nombre' => ['label' => 'Nombre', 'type' => 'text', 'required' => true, 'maxLength' => 255],
                'apellido' => ['label' => 'Apellido', 'type' => 'text', 'required' => true, 'maxLength' => 255],
                'dni' => ['label' => 'DNI', 'type' => 'number', 'required' => true, 'min' => 1, 'max' => 2147483647],
                'matricula' => ['label' => 'Matrícula', 'type' => 'text', 'required' => true, 'maxLength' => 50],
                'estado' => ['label' => 'Estado', 'type' => 'select', 'required' => true, 'options' => [['Activo', 'Activo'], ['Egresado', 'Egresado'], ['Inactivo', 'Inactivo']]],
            ],
        ],
        'docentes' => [
            'title' => 'Docentes',
            'description' => 'Administrá docentes, cuentas de acceso y cursos asignados.',
            'table' => 'docente',
            'alias' => 'd',
            'id' => 'id_docente',
            'idExpression' => 'd.id_docente',
            'from' => 'FROM docente AS d',
            'listSql' => "SELECT d.id_docente AS id, CONCAT(d.apellido, ', ', d.nombre) AS docente,
                d.dni, d.legajo, d.estado,
                (SELECT COUNT(*) FROM curso AS c WHERE c.id_docente = d.id_docente AND c.estado = 'Activo') AS cursos",
            'searchFields' => ['d.nombre', 'd.apellido', 'd.dni', 'd.legajo'],
            'statusExpression' => 'd.estado',
            'sortOptions' => [
                'docente' => 'd.apellido',
                'dni' => 'd.dni',
                'legajo' => 'd.legajo',
                'estado' => 'd.estado',
            ],
            'statusOptions' => $statusOptions,
            'deactivateValue' => 'Inactivo',
            'newAccountRole' => 3,
            'uniqueFields' => ['dni' => 'DNI', 'legajo' => 'legajo'],
            'summary' => [
                ['label' => 'Docentes', 'sql' => 'SELECT COUNT(*) FROM docente'],
                ['label' => 'Activos', 'sql' => "SELECT COUNT(*) FROM docente WHERE estado = 'Activo'"],
                ['label' => 'Inactivos', 'sql' => "SELECT COUNT(*) FROM docente WHERE estado = 'Inactivo'"],
                ['label' => 'Cursos activos', 'sql' => "SELECT COUNT(*) FROM curso WHERE estado = 'Activo'"],
            ],
            'columns' => [
                'docente' => 'Docente',
                'dni' => 'DNI',
                'legajo' => 'Legajo',
                'cursos' => 'Cursos activos',
                'estado' => 'Estado',
            ],
            'fields' => [
                'id_usuario' => [
                    'label' => 'Cuenta de acceso docente disponible',
                    'type' => 'select',
                    'required' => false,
                    'optionsSql' => "SELECT u.id_usuario AS id, CONCAT(u.nombre_usuario, ' (cuenta #', u.id_usuario, ')') AS label
                        FROM usuario AS u
                        WHERE u.id_rol = 3
                        AND NOT EXISTS (SELECT 1 FROM docente AS d WHERE d.id_usuario = u.id_usuario AND d.id_docente <> :record_id)
                        ORDER BY u.nombre_usuario",
                ],
                '_nuevo_usuario' => ['label' => 'Nombre de usuario nuevo (si no elegís cuenta existente)', 'type' => 'text', 'required' => false, 'maxLength' => 200],
                '_clave_usuario' => ['label' => 'Contraseña inicial (mínimo 12 caracteres)', 'type' => 'password', 'required' => false, 'maxLength' => 200],
                'nombre' => ['label' => 'Nombre', 'type' => 'text', 'required' => true, 'maxLength' => 200],
                'apellido' => ['label' => 'Apellido', 'type' => 'text', 'required' => true, 'maxLength' => 200],
                'dni' => ['label' => 'DNI', 'type' => 'text', 'required' => true, 'maxLength' => 20],
                'legajo' => ['label' => 'Legajo', 'type' => 'text', 'required' => true, 'maxLength' => 50],
                'estado' => ['label' => 'Estado', 'type' => 'select', 'required' => true, 'options' => [['Activo', 'Activo'], ['Inactivo', 'Inactivo']]],
            ],
        ],
        'cursos' => [
            'title' => 'Cursos',
            'description' => 'Gestioná cursos y sus relaciones con períodos, materias y docentes.',
            'table' => 'curso',
            'alias' => 'c',
            'id' => 'id_curso',
            'idExpression' => 'c.id_curso',
            'from' => 'FROM curso AS c
                INNER JOIN materia AS m ON m.id_materia = c.id_materia
                INNER JOIN docente AS d ON d.id_docente = c.id_docente
                INNER JOIN periodo_lectivo AS p ON p.id_periodo_lectivo = c.id_periodo',
            'listSql' => "SELECT c.id_curso AS id,
                CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS curso,
                CONCAT(d.apellido, ', ', d.nombre) AS docente, p.nombre AS periodo,
                c.turno, c.modalidad, c.cupo_maximo,
                (SELECT COUNT(*) FROM inscripcion AS i WHERE i.id_curso = c.id_curso AND i.estado = 'Confirmada') AS inscriptos,
                c.estado",
            'searchFields' => ['c.`año`', 'c.division', 'm.nombre_materia', 'd.nombre', 'd.apellido', 'p.nombre'],
            'statusExpression' => 'c.estado',
            'sortOptions' => [
                'curso' => 'c.`año`',
                'periodo' => 'p.fecha_inicio',
                'docente' => 'd.apellido',
                'turno' => 'c.turno',
                'cupo_maximo' => 'c.cupo_maximo',
                'inscriptos' => 'inscriptos',
                'estado' => 'c.estado',
            ],
            'statusOptions' => [['Activo', 'Activos'], ['Finalizado', 'Finalizados'], ['Cancelado', 'Cancelados']],
            'deactivateValue' => 'Cancelado',
            'summary' => [
                ['label' => 'Cursos', 'sql' => 'SELECT COUNT(*) FROM curso'],
                ['label' => 'Activos', 'sql' => "SELECT COUNT(*) FROM curso WHERE estado = 'Activo'"],
                ['label' => 'Alumnos confirmados', 'sql' => "SELECT COUNT(*) FROM inscripcion WHERE estado = 'Confirmada'"],
                ['label' => 'Vacantes activas', 'sql' => "SELECT COALESCE(SUM(GREATEST(c.cupo_maximo - (SELECT COUNT(*) FROM inscripcion AS i WHERE i.id_curso = c.id_curso AND i.estado = 'Confirmada'), 0)), 0) FROM curso AS c WHERE c.estado = 'Activo'"],
            ],
            'columns' => [
                'curso' => 'Curso / materia',
                'periodo' => 'Período',
                'docente' => 'Docente',
                'turno' => 'Turno',
                'modalidad' => 'Modalidad',
                'cupo_maximo' => 'Cupo',
                'inscriptos' => 'Confirmados',
                'estado' => 'Estado',
            ],
            'fields' => [
                'id_periodo' => [
                    'label' => 'Período lectivo',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => 'SELECT id_periodo_lectivo AS id, nombre AS label FROM periodo_lectivo ORDER BY fecha_inicio DESC',
                ],
                'id_materia' => [
                    'label' => 'Materia',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => 'SELECT id_materia AS id, nombre_materia AS label FROM materia ORDER BY nombre_materia',
                ],
                'id_docente' => [
                    'label' => 'Docente',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => "SELECT d.id_docente AS id, CONCAT(d.apellido, ', ', d.nombre) AS label
                        FROM docente AS d
                        WHERE d.estado = 'Activo' OR d.id_docente = (
                            SELECT c.id_docente FROM curso AS c WHERE c.id_curso = :record_id
                        )
                        ORDER BY d.apellido, d.nombre",
                ],
                'año' => ['label' => 'Año / nivel', 'type' => 'text', 'required' => true, 'maxLength' => 200],
                'division' => ['label' => 'División', 'type' => 'text', 'required' => true, 'maxLength' => 200],
                'modalidad' => ['label' => 'Modalidad', 'type' => 'select', 'required' => true, 'options' => [['Presencial', 'Presencial'], ['Virtual', 'Virtual'], ['Híbrido', 'Híbrido']]],
                'turno' => ['label' => 'Turno', 'type' => 'select', 'required' => true, 'options' => [['mañana', 'Mañana'], ['tarde', 'Tarde'], ['vespertino', 'Vespertino']]],
                'cupo_maximo' => ['label' => 'Cupo máximo', 'type' => 'number', 'required' => true, 'min' => 1, 'max' => 1000],
                'fecha_inicio' => ['label' => 'Fecha de inicio', 'type' => 'date', 'required' => true],
                'fecha_fin' => ['label' => 'Fecha de fin', 'type' => 'date', 'required' => true],
                'estado' => ['label' => 'Estado', 'type' => 'select', 'required' => true, 'options' => [['Activo', 'Activo'], ['Finalizado', 'Finalizado'], ['Cancelado', 'Cancelado']]],
            ],
        ],
        'inscripciones' => [
            'title' => 'Inscripciones',
            'description' => 'Administrá la inscripción de alumnos a cursos y su estado.',
            'table' => 'inscripcion',
            'alias' => 'i',
            'id' => 'id_inscripcion',
            'idExpression' => 'i.id_inscripcion',
            'from' => 'FROM inscripcion AS i
                INNER JOIN alumno AS a ON a.id_alumno = i.id_alumno
                INNER JOIN curso AS c ON c.id_curso = i.id_curso
                INNER JOIN materia AS m ON m.id_materia = c.id_materia',
            'listSql' => "SELECT i.id_inscripcion AS id,
                CONCAT(a.apellido, ', ', a.nombre) AS alumno,
                CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS curso,
                c.turno, i.fecha_inscripcion, i.estado, i.importe_cuota",
            'searchFields' => ['a.nombre', 'a.apellido', 'a.matricula', 'm.nombre_materia', 'c.`año`', 'c.division'],
            'statusExpression' => 'i.estado',
            'sortOptions' => [
                'alumno' => 'a.apellido',
                'curso' => 'm.nombre_materia',
                'turno' => 'c.turno',
                'fecha_inscripcion' => 'i.fecha_inscripcion',
                'importe_cuota' => 'i.importe_cuota',
                'estado' => 'i.estado',
            ],
            'statusOptions' => [['Pendiente', 'Pendientes'], ['Confirmada', 'Confirmadas'], ['Finalizada', 'Finalizadas'], ['Cancelada', 'Canceladas']],
            'deactivateValue' => 'Cancelada',
            'summary' => [
                ['label' => 'Inscripciones', 'sql' => 'SELECT COUNT(*) FROM inscripcion'],
                ['label' => 'Confirmadas', 'sql' => "SELECT COUNT(*) FROM inscripcion WHERE estado = 'Confirmada'"],
                ['label' => 'Pendientes', 'sql' => "SELECT COUNT(*) FROM inscripcion WHERE estado = 'Pendiente'"],
                ['label' => 'Recaudación mensual', 'sql' => "SELECT COALESCE(SUM(importe_cuota), 0) FROM inscripcion WHERE estado = 'Confirmada' AND fecha_inscripcion >= DATE_FORMAT(CURRENT_DATE, '%Y-%m-01') AND fecha_inscripcion < DATE_FORMAT(CURRENT_DATE + INTERVAL 1 MONTH, '%Y-%m-01')"],
            ],
            'columns' => [
                'alumno' => 'Alumno',
                'curso' => 'Curso',
                'turno' => 'Turno',
                'fecha_inscripcion' => 'Fecha',
                'importe_cuota' => 'Cuota',
                'estado' => 'Estado',
            ],
            'fields' => [
                'id_alumno' => [
                    'label' => 'Alumno',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => "SELECT a.id_alumno AS id, CONCAT(a.apellido, ', ', a.nombre, ' (', a.matricula, ')') AS label
                        FROM alumno AS a
                        WHERE a.estado = 'Activo' OR a.id_alumno = (
                            SELECT i.id_alumno FROM inscripcion AS i WHERE i.id_inscripcion = :record_id
                        )
                        ORDER BY a.apellido, a.nombre",
                ],
                'id_curso' => [
                    'label' => 'Curso',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => "SELECT c.id_curso AS id, CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia, ' - ', p.nombre, ' (', c.turno, ')') AS label
                        FROM curso AS c
                        INNER JOIN materia AS m ON m.id_materia = c.id_materia
                        INNER JOIN periodo_lectivo AS p ON p.id_periodo_lectivo = c.id_periodo
                        WHERE c.estado = 'Activo' OR c.id_curso = (SELECT i.id_curso FROM inscripcion AS i WHERE i.id_inscripcion = :record_id)
                        ORDER BY p.fecha_inicio DESC, c.`año`, c.division, m.nombre_materia",
                ],
                'fecha_inscripcion' => ['label' => 'Fecha de inscripción', 'type' => 'date', 'required' => true, 'default' => date('Y-m-d')],
                'estado' => ['label' => 'Estado', 'type' => 'select', 'required' => true, 'options' => [['Pendiente', 'Pendiente'], ['Confirmada', 'Confirmada'], ['Finalizada', 'Finalizada'], ['Cancelada', 'Cancelada']]],
                'motivo_baja' => ['label' => 'Motivo de baja (si corresponde)', 'type' => 'textarea', 'required' => false, 'maxLength' => 255],
                'importe_cuota' => ['label' => 'Importe de cuota', 'type' => 'number', 'required' => true, 'min' => 0, 'max' => 99999999.99, 'step' => '0.01'],
            ],
        ],
        'calificaciones' => [
            'title' => 'Calificaciones',
            'description' => 'Cargá notas por inscripción y evaluación; las correcciones quedan auditadas.',
            'table' => 'calificacion',
            'alias' => 'g',
            'id' => 'id_calificacion',
            'idExpression' => 'g.id_calificacion',
            'from' => 'FROM calificacion AS g
                INNER JOIN inscripcion AS i ON i.id_inscripcion = g.id_inscripcion
                INNER JOIN alumno AS a ON a.id_alumno = i.id_alumno
                INNER JOIN columna_notas AS cn ON cn.id_columna_notas = g.id_columna_notas
                INNER JOIN curso AS c ON c.id_curso = cn.id_curso
                INNER JOIN materia AS m ON m.id_materia = c.id_materia',
            'listSql' => "SELECT g.id_calificacion AS id,
                CONCAT(a.apellido, ', ', a.nombre) AS alumno,
                CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS curso,
                cn.titulo AS evaluacion, g.nota, cn.nota_maxima,
                g.fecha_carga, g.observaciones,
                (SELECT COUNT(*) FROM calificacion_historial AS h WHERE h.id_calificacion = g.id_calificacion) AS correcciones",
            'searchFields' => ['a.nombre', 'a.apellido', 'a.matricula', 'm.nombre_materia', 'cn.titulo', 'g.observaciones'],
            'sortOptions' => [
                'alumno' => 'a.apellido',
                'curso' => 'm.nombre_materia',
                'evaluacion' => 'cn.titulo',
                'nota' => 'g.nota',
                'fecha_carga' => 'g.fecha_carga',
                'correcciones' => 'correcciones',
            ],
            'summary' => [
                ['label' => 'Calificaciones', 'sql' => 'SELECT COUNT(*) FROM calificacion'],
                ['label' => 'Promedio registrado', 'sql' => 'SELECT COALESCE(AVG(nota), 0) FROM calificacion'],
                ['label' => 'Evaluaciones', 'sql' => 'SELECT COUNT(*) FROM columna_notas'],
                ['label' => 'Correcciones auditadas', 'sql' => 'SELECT COUNT(*) FROM calificacion_historial'],
            ],
            'columns' => [
                'alumno' => 'Alumno',
                'curso' => 'Curso / materia',
                'evaluacion' => 'Evaluación',
                'nota' => 'Nota',
                'nota_maxima' => 'Máximo',
                'fecha_carga' => 'Cargada',
                'correcciones' => 'Correcciones',
            ],
            'auditReasonField' => true,
            'fields' => [
                'id_inscripcion' => [
                    'label' => 'Alumno / curso inscripto',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => "SELECT i.id_inscripcion AS id,
                        CONCAT(a.apellido, ', ', a.nombre, ' - ', c.`año`, '° ', c.division, ' - ', m.nombre_materia) AS label
                        FROM inscripcion AS i
                        INNER JOIN alumno AS a ON a.id_alumno = i.id_alumno
                        INNER JOIN curso AS c ON c.id_curso = i.id_curso
                        INNER JOIN materia AS m ON m.id_materia = c.id_materia
                        WHERE (
                            i.estado IN ('Confirmada', 'Finalizada')
                            AND CONCAT(a.apellido, ' ', a.nombre, ' ', a.matricula, ' ', c.`año`, ' ', c.division, ' ', m.nombre_materia) LIKE :related_search
                        )
                        OR i.id_inscripcion = (
                            SELECT g.id_inscripcion FROM calificacion AS g WHERE g.id_calificacion = :record_id
                        )
                        ORDER BY (i.id_inscripcion = (
                            SELECT g.id_inscripcion FROM calificacion AS g WHERE g.id_calificacion = :selected_record
                        )) DESC, a.apellido, a.nombre
                        LIMIT 100",
                    'searchableOptions' => true,
                    'validationSql' => "SELECT COUNT(*) FROM inscripcion AS i
                        WHERE i.id_inscripcion = :value
                        AND (
                            i.estado IN ('Confirmada', 'Finalizada')
                            OR i.id_inscripcion = (
                                SELECT g.id_inscripcion FROM calificacion AS g WHERE g.id_calificacion = :record_id
                            )
                        )",
                ],
                'id_columna_notas' => [
                    'label' => 'Evaluación',
                    'type' => 'select',
                    'required' => true,
                    'optionsSql' => "SELECT cn.id_columna_notas AS id,
                        CONCAT(c.`año`, '° ', c.division, ' - ', m.nombre_materia, ' - ', cn.titulo, ' (máximo ', cn.nota_maxima, ')') AS label
                        FROM columna_notas AS cn
                        INNER JOIN curso AS c ON c.id_curso = cn.id_curso
                        INNER JOIN materia AS m ON m.id_materia = c.id_materia
                        WHERE cn.estado = 'Abierta' OR cn.id_columna_notas = (
                            SELECT g.id_columna_notas FROM calificacion AS g WHERE g.id_calificacion = :record_id
                        )
                        ORDER BY cn.fecha_evaluacion DESC, cn.titulo",
                ],
                'nota' => ['label' => 'Nota', 'type' => 'number', 'required' => true, 'min' => 0, 'step' => '0.01'],
                'observaciones' => ['label' => 'Observaciones', 'type' => 'textarea', 'required' => false, 'maxLength' => 255],
                '_motivo_correccion' => ['label' => 'Motivo de corrección (obligatorio al modificar una nota)', 'type' => 'textarea', 'required' => false, 'maxLength' => 255],
            ],
        ],
        'materias' => [
            'title' => 'Materias',
            'description' => 'Administrá las materias relacionadas con los cursos de la academia.',
            'table' => 'materia',
            'alias' => 'm',
            'id' => 'id_materia',
            'idExpression' => 'm.id_materia',
            'from' => 'FROM materia AS m',
            'listSql' => 'SELECT m.id_materia AS id, m.nombre_materia AS materia,
                (SELECT COUNT(*) FROM curso AS c WHERE c.id_materia = m.id_materia) AS cursos',
            'searchFields' => ['m.nombre_materia'],
            'uniqueFields' => ['nombre_materia' => 'nombre de materia'],
            'sortOptions' => ['materia' => 'm.nombre_materia', 'cursos' => 'cursos'],
            'summary' => [
                ['label' => 'Materias', 'sql' => 'SELECT COUNT(*) FROM materia'],
                ['label' => 'Cursos relacionados', 'sql' => 'SELECT COUNT(*) FROM curso'],
                ['label' => 'Materias en uso', 'sql' => 'SELECT COUNT(DISTINCT id_materia) FROM curso'],
                ['label' => 'Sin cursos', 'sql' => 'SELECT COUNT(*) FROM materia AS m WHERE NOT EXISTS (SELECT 1 FROM curso AS c WHERE c.id_materia = m.id_materia)'],
            ],
            'columns' => ['materia' => 'Materia', 'cursos' => 'Cursos relacionados'],
            'deleteGuardSql' => 'SELECT COUNT(*) FROM curso WHERE id_materia = :record_id',
            'deleteGuardMessage' => 'No se puede eliminar la materia porque está relacionada con cursos.',
            'fields' => [
                'nombre_materia' => ['label' => 'Nombre de la materia', 'type' => 'text', 'required' => true, 'maxLength' => 200],
            ],
        ],
    ];
}

function moduleEscape(mixed $value): string
{
    return htmlspecialchars(is_scalar($value) ? (string) $value : '', ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function moduleRequestString(mixed $value): string
{
    return is_scalar($value) ? trim((string) $value) : '';
}

function moduleOptions(PDO $connection, array $field, ?int $recordId, string $relatedSearch = '', ?string $selectedValue = null): array
{
    if (isset($field['options'])) {
        return $field['options'];
    }

    if (!isset($field['optionsSql'])) {
        return [];
    }

    $query = $connection->prepare($field['optionsSql']);
    if (str_contains($field['optionsSql'], ':record_id')) {
        $query->bindValue(':record_id', $recordId ?? 0, PDO::PARAM_INT);
    }
    if (str_contains($field['optionsSql'], ':related_search')) {
        $query->bindValue(':related_search', '%' . $relatedSearch . '%', PDO::PARAM_STR);
    }
    if (str_contains($field['optionsSql'], ':selected_id')) {
        $query->bindValue(':selected_id', (int) ($selectedValue ?? 0), PDO::PARAM_INT);
    }
    if (str_contains($field['optionsSql'], ':selected_order')) {
        $query->bindValue(':selected_order', (int) ($selectedValue ?? 0), PDO::PARAM_INT);
    }
    if (str_contains($field['optionsSql'], ':selected_record')) {
        $query->bindValue(':selected_record', $recordId ?? 0, PDO::PARAM_INT);
    }
    $query->execute();
    return array_map(
        static fn (array $row): array => [(string) $row['id'], (string) $row['label']],
        $query->fetchAll()
    );
}

function moduleValidateValue(string $name, array $field, mixed $value, PDO $connection, ?int $recordId): string|int|float
{
    $value = is_scalar($value) ? trim((string) $value) : '';
    if (($field['required'] ?? false) && $value === '') {
        throw new InvalidArgumentException('Completá el campo «' . $field['label'] . '».');
    }
    if ($value === '') {
        return '';
    }

    if (($field['type'] ?? '') === 'select') {
        if (isset($field['validationSql'])) {
            $validationQuery = $connection->prepare($field['validationSql']);
            $validationQuery->execute([
                'value' => $value,
                'record_id' => $recordId ?? 0,
            ]);
            if ((int) $validationQuery->fetchColumn() === 0) {
                throw new InvalidArgumentException('Seleccioná una opción válida para «' . $field['label'] . '».');
            }
            return (int) $value;
        }

        $allowed = array_column(
            moduleOptions(
                $connection,
                $field,
                $recordId,
                trim((string) ($_POST['relacion'] ?? '')),
                $value
            ),
            0
        );
        if (!in_array($value, $allowed, true)) {
            throw new InvalidArgumentException('Seleccioná una opción válida para «' . $field['label'] . '».');
        }
        return (int) $value;
    }

    if (($field['type'] ?? '') === 'number') {
        if (!is_numeric($value)) {
            throw new InvalidArgumentException('Ingresá un número válido en «' . $field['label'] . '».');
        }
        $number = (float) $value;
        if (!str_contains((string) ($field['step'] ?? ''), '.') && floor($number) !== $number) {
            throw new InvalidArgumentException('Ingresá un número entero en «' . $field['label'] . '».');
        }
        if ($number < (float) ($field['min'] ?? -INF) || $number > (float) ($field['max'] ?? INF)) {
            throw new InvalidArgumentException('El valor de «' . $field['label'] . '» está fuera del rango permitido.');
        }
        return str_contains((string) ($field['step'] ?? ''), '.') ? $number : (int) $number;
    }

    if (($field['type'] ?? '') === 'date') {
        $date = DateTimeImmutable::createFromFormat('!Y-m-d', $value);
        if ($date === false || $date->format('Y-m-d') !== $value) {
            throw new InvalidArgumentException('Ingresá una fecha válida en «' . $field['label'] . '».');
        }
        return $value;
    }

    $length = preg_match_all('/./us', $value);
    if (isset($field['maxLength']) && $length > $field['maxLength']) {
        throw new InvalidArgumentException('«' . $field['label'] . '» supera la longitud permitida.');
    }
    return $value;
}

function moduleCurrentRow(PDO $connection, array $config, int $recordId): ?array
{
    $query = $connection->prepare(
        'SELECT * FROM `' . $config['table'] . '` WHERE `' . $config['id'] . '` = :record_id'
    );
    $query->execute(['record_id' => $recordId]);
    $row = $query->fetch();
    return $row === false ? null : $row;
}

function moduleSave(PDO $connection, array $config, int $recordId, array $input): void
{
    $values = [];
    foreach ($config['fields'] as $name => $field) {
        if (str_starts_with($name, '_')) {
            continue;
        }
        $values[$name] = moduleValidateValue($name, $field, $input[$name] ?? '', $connection, $recordId ?: null);
    }

    $newUsername = moduleRequestString($input['_nuevo_usuario'] ?? '');
    $newPassword = is_string($input['_clave_usuario'] ?? null) ? $input['_clave_usuario'] : '';
    $createAccount = isset($config['newAccountRole']) && ($values['id_usuario'] ?? '') === '';
    if ($createAccount) {
        if ($newUsername === '' || $newPassword === '') {
            throw new InvalidArgumentException('Elegí una cuenta disponible o completá usuario y contraseña inicial.');
        }
        if (preg_match_all('/./us', $newUsername) > 200) {
            throw new InvalidArgumentException('El nombre de usuario supera la longitud permitida.');
        }
        if (preg_match_all('/./us', $newPassword) < 12 || preg_match_all('/./us', $newPassword) > 200) {
            throw new InvalidArgumentException('La contraseña inicial debe tener entre 12 y 200 caracteres.');
        }
        $usernameQuery = $connection->prepare(
            'SELECT COUNT(*) FROM usuario WHERE LOWER(nombre_usuario) = LOWER(:username)'
        );
        $usernameQuery->execute(['username' => $newUsername]);
        if ((int) $usernameQuery->fetchColumn() > 0) {
            throw new InvalidArgumentException('Ese nombre de usuario ya existe.');
        }
    }
    if (!$createAccount && ($values['id_usuario'] ?? '') === '') {
        throw new InvalidArgumentException('Seleccioná una cuenta existente o creá una nueva.');
    }

    if (isset($values['fecha_inicio'], $values['fecha_fin']) && $values['fecha_fin'] < $values['fecha_inicio']) {
        throw new InvalidArgumentException('La fecha de fin debe ser igual o posterior a la fecha de inicio.');
    }

    $previous = $recordId > 0 ? moduleCurrentRow($connection, $config, $recordId) : null;
    if ($recordId > 0 && $previous === null) {
        throw new InvalidArgumentException('El registro que querés editar ya no existe.');
    }

    foreach ($config['uniqueFields'] ?? [] as $fieldName => $label) {
        $uniqueQuery = $connection->prepare(
            'SELECT COUNT(*) FROM `' . $config['table'] . '`
            WHERE LOWER(`' . $fieldName . '`) = LOWER(:value)
            AND `' . $config['id'] . '` <> :record_id'
        );
        $uniqueQuery->execute([
            'value' => $values[$fieldName],
            'record_id' => $recordId,
        ]);
        if ((int) $uniqueQuery->fetchColumn() > 0) {
            throw new InvalidArgumentException('Ya existe un registro con ese ' . $label . '.');
        }
    }

    if ($config['table'] === 'calificacion') {
        if ($previous !== null && (
            (int) $previous['id_inscripcion'] !== (int) $values['id_inscripcion']
            || (int) $previous['id_columna_notas'] !== (int) $values['id_columna_notas']
        )) {
            throw new InvalidArgumentException('No se puede cambiar el alumno o la evaluación de una calificación existente.');
        }

        $relationQuery = $connection->prepare(
            'SELECT i.id_curso AS curso_inscripcion, cn.id_curso AS curso_evaluacion, cn.nota_maxima, i.estado AS estado_inscripcion
            FROM inscripcion AS i
            INNER JOIN columna_notas AS cn ON cn.id_columna_notas = :column_id
            WHERE i.id_inscripcion = :enrollment_id'
        );
        $relationQuery->execute([
            'column_id' => $values['id_columna_notas'],
            'enrollment_id' => $values['id_inscripcion'],
        ]);
        $relation = $relationQuery->fetch();
        if ($relation === false || (int) $relation['curso_inscripcion'] !== (int) $relation['curso_evaluacion']) {
            throw new InvalidArgumentException('La evaluación elegida no pertenece al curso de esa inscripción.');
        }
        if ($recordId === 0 && !in_array($relation['estado_inscripcion'], ['Confirmada', 'Finalizada'], true)) {
            throw new InvalidArgumentException('Solo se pueden calificar inscripciones confirmadas o finalizadas.');
        }
        if ((float) $values['nota'] > (float) $relation['nota_maxima']) {
            throw new InvalidArgumentException('La nota no puede superar el máximo definido para la evaluación.');
        }
        if ($previous !== null && (float) $previous['nota'] !== (float) $values['nota']) {
            $reason = trim((string) ($input['_motivo_correccion'] ?? ''));
            if ($reason === '') {
                throw new InvalidArgumentException('Indicá el motivo para dejar constancia de la corrección de nota.');
            }
            if (preg_match_all('/./us', $reason) > 255) {
                throw new InvalidArgumentException('El motivo de corrección no puede superar los 255 caracteres.');
            }
        }
    }

    $connection->beginTransaction();
    try {
        if ($config['table'] === 'inscripcion') {
            $courseQuery = $connection->prepare(
                'SELECT cupo_maximo, estado FROM curso WHERE id_curso = :course_id FOR UPDATE'
            );
            $courseQuery->execute(['course_id' => $values['id_curso']]);
            $course = $courseQuery->fetch();
            if ($course === false) {
                throw new InvalidArgumentException('El curso seleccionado ya no existe.');
            }

            $duplicateQuery = $connection->prepare(
                "SELECT COUNT(*) FROM inscripcion
                WHERE id_alumno = :student_id
                AND id_curso = :course_id
                AND estado <> 'Cancelada'
                AND id_inscripcion <> :record_id"
            );
            $duplicateQuery->execute([
                'student_id' => $values['id_alumno'],
                'course_id' => $values['id_curso'],
                'record_id' => $recordId,
            ]);
            if ((int) $duplicateQuery->fetchColumn() > 0 && $values['estado'] !== 'Cancelada') {
                throw new InvalidArgumentException('El alumno ya tiene una inscripción vigente en ese curso.');
            }

            if ($values['estado'] === 'Confirmada') {
                if ($course['estado'] !== 'Activo') {
                    throw new InvalidArgumentException('Solo se puede confirmar una inscripción a un curso activo.');
                }
                $capacityQuery = $connection->prepare(
                    "SELECT COUNT(*) FROM inscripcion
                    WHERE id_curso = :course_id
                    AND estado = 'Confirmada'
                    AND id_inscripcion <> :record_id"
                );
                $capacityQuery->execute([
                    'course_id' => $values['id_curso'],
                    'record_id' => $recordId,
                ]);
                if ((int) $capacityQuery->fetchColumn() >= (int) $course['cupo_maximo']) {
                    throw new InvalidArgumentException('El curso alcanzó su cupo máximo y no admite más inscripciones confirmadas.');
                }
            }
        }

        if ($config['table'] === 'curso' && $recordId > 0) {
            $courseLock = $connection->prepare(
                'SELECT id_curso FROM curso WHERE id_curso = :record_id FOR UPDATE'
            );
            $courseLock->execute(['record_id' => $recordId]);
            if ($courseLock->fetchColumn() === false) {
                throw new InvalidArgumentException('El curso que querés editar ya no existe.');
            }
            $confirmedCount = $connection->prepare(
                "SELECT COUNT(*) FROM inscripcion
                WHERE id_curso = :record_id AND estado = 'Confirmada'"
            );
            $confirmedCount->execute(['record_id' => $recordId]);
            if ((int) $confirmedCount->fetchColumn() > (int) $values['cupo_maximo']) {
                throw new InvalidArgumentException('El cupo máximo no puede ser menor que la cantidad de inscripciones confirmadas.');
            }
        }

        if ($createAccount) {
            $account = $connection->prepare(
                'INSERT INTO usuario (id_rol, nombre_usuario, contraseña)
                VALUES (:role, :username, :password)'
            );
            $account->execute([
                'role' => $config['newAccountRole'],
                'username' => $newUsername,
                'password' => password_hash($newPassword, PASSWORD_DEFAULT),
            ]);
            $values['id_usuario'] = (int) $connection->lastInsertId();
        }

        if ($previous === null) {
            $columns = array_keys($values);
            $sql = 'INSERT INTO `' . $config['table'] . '` (`'
                . implode('`, `', $columns) . '`) VALUES (:'
                . implode(', :', $columns) . ')';
            $query = $connection->prepare($sql);
            $query->execute($values);
        } else {
            $sets = array_map(
                static fn (string $column): string => '`' . $column . '` = :' . $column,
                array_keys($values)
            );
            if ($config['table'] === 'inscripcion') {
                $sets[] = '`fecha_modificacion` = CURRENT_TIMESTAMP';
            }
            $values['record_id'] = $recordId;
            $sql = 'UPDATE `' . $config['table'] . '` SET ' . implode(', ', $sets)
                . ' WHERE `' . $config['id'] . '` = :record_id';
            $query = $connection->prepare($sql);
            $query->execute($values);

            if ($config['table'] === 'calificacion' && (float) $previous['nota'] !== (float) $values['nota']) {
                $history = $connection->prepare(
                    'INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva, motivo)
                    VALUES (:id, :previous, :next, :reason)'
                );
                $history->execute([
                    'id' => $recordId,
                    'previous' => $previous['nota'],
                    'next' => $values['nota'],
                    'reason' => trim((string) $input['_motivo_correccion']),
                ]);
            }
        }
        $connection->commit();
    } catch (Throwable $exception) {
        if ($connection->inTransaction()) {
            $connection->rollBack();
        }
        throw $exception;
    }
}

function moduleProcessPost(PDO $connection, array $config, string $route): ?string
{
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        return null;
    }

    $sentToken = is_string($_POST['csrf_token'] ?? null) ? $_POST['csrf_token'] : '';
    if (!hash_equals((string) $_SESSION['module_csrf'], $sentToken)) {
        return 'La sesión del formulario venció. Recargá la página e intentá nuevamente.';
    }

    $action = moduleRequestString($_POST['action'] ?? '');
    $recordId = filter_var($_POST['id'] ?? 0, FILTER_VALIDATE_INT);
    if ($recordId === false || $recordId < 0) {
        return 'El identificador enviado no es válido.';
    }

    try {
        if ($action === 'save') {
            moduleSave($connection, $config, $recordId, $_POST);
            $_SESSION['module_flash'] = $recordId > 0 ? 'Los cambios se guardaron correctamente.' : 'El registro se creó correctamente.';
            header('Location: ' . $route);
            exit;
        }

        if ($action === 'deactivate' && isset($config['deactivateValue'])) {
            $statusField = 'estado';
            $query = $connection->prepare(
                'UPDATE `' . $config['table'] . '` SET `' . $statusField . '` = :status WHERE `'
                    . $config['id'] . '` = :record_id'
            );
            $query->execute([
                'status' => $config['deactivateValue'],
                'record_id' => $recordId,
            ]);
            $_SESSION['module_flash'] = 'Se actualizó el estado del registro.';
            header('Location: ' . $route);
            exit;
        }

        if ($action === 'delete' && $config['table'] === 'calificacion') {
            $connection->beginTransaction();
            try {
                $lock = $connection->prepare(
                    'SELECT id_calificacion FROM calificacion WHERE id_calificacion = :record_id FOR UPDATE'
                );
                $lock->execute(['record_id' => $recordId]);
                if ($lock->fetchColumn() === false) {
                    $connection->rollBack();
                    return 'La calificación ya no existe.';
                }
                $history = $connection->prepare(
                    'SELECT COUNT(*) FROM calificacion_historial WHERE id_calificacion = :record_id'
                );
                $history->execute(['record_id' => $recordId]);
                if ((int) $history->fetchColumn() > 0) {
                    $connection->rollBack();
                    return 'No se puede eliminar esta calificación porque tiene correcciones auditadas.';
                }
                $delete = $connection->prepare(
                    'DELETE FROM calificacion WHERE id_calificacion = :record_id'
                );
                $delete->execute(['record_id' => $recordId]);
                $connection->commit();
            } catch (Throwable $exception) {
                if ($connection->inTransaction()) {
                    $connection->rollBack();
                }
                throw $exception;
            }
            $_SESSION['module_flash'] = 'La calificación se eliminó correctamente.';
            header('Location: ' . $route);
            exit;
        }

        if ($action === 'delete' && isset($config['deleteGuardSql'])) {
            $connection->beginTransaction();
            try {
                $lock = $connection->prepare(
                    'SELECT `' . $config['id'] . '` FROM `' . $config['table']
                        . '` WHERE `' . $config['id'] . '` = :record_id FOR UPDATE'
                );
                $lock->execute(['record_id' => $recordId]);
                if ($lock->fetchColumn() === false) {
                    $connection->rollBack();
                    return 'El registro ya no existe.';
                }
                $guard = $connection->prepare($config['deleteGuardSql']);
                $guard->execute(['record_id' => $recordId]);
                if ((int) $guard->fetchColumn() > 0) {
                    $connection->rollBack();
                    return $config['deleteGuardMessage'];
                }
                $delete = $connection->prepare(
                    'DELETE FROM `' . $config['table'] . '` WHERE `' . $config['id'] . '` = :record_id'
                );
                $delete->execute(['record_id' => $recordId]);
                $connection->commit();
            } catch (Throwable $exception) {
                if ($connection->inTransaction()) {
                    $connection->rollBack();
                }
                throw $exception;
            }
            $_SESSION['module_flash'] = 'El registro se eliminó correctamente.';
            header('Location: ' . $route);
            exit;
        }

        return 'La operación solicitada no está habilitada para este módulo.';
    } catch (InvalidArgumentException $exception) {
        return $exception->getMessage();
    } catch (PDOException $exception) {
        error_log('Module database error (' . $config['table'] . '): ' . $exception->getMessage());
        if ((string) $exception->getCode() === '23000') {
            return 'La operación no se pudo completar por una relación o duplicado existente.';
        }
        return 'No se pudo guardar el registro. Revisá la conexión con la base de datos e intentá nuevamente.';
    }
}

function modulePageData(PDO $connection, string $key): array
{
    $definitions = moduleDefinitions();
    if (!isset($definitions[$key])) {
        throw new InvalidArgumentException('Módulo desconocido.');
    }
    $config = $definitions[$key];
    $route = $key . '.php';
    $error = moduleProcessPost($connection, $config, $route);

    $search = moduleRequestString($_GET['q'] ?? '');
    $status = moduleRequestString($_GET['estado'] ?? '');
    $page = filter_var($_GET['pagina'] ?? 1, FILTER_VALIDATE_INT);
    $page = $page === false ? 1 : max(1, $page);
    $perPage = 25;

    $where = [];
    $parameters = [];
    if ($search !== '') {
        $searchParts = [];
        foreach ($config['searchFields'] as $index => $field) {
            $placeholder = 'search_' . $index;
            $searchParts[] = $field . ' LIKE :' . $placeholder;
            $parameters[$placeholder] = '%' . $search . '%';
        }
        $where[] = '(' . implode(' OR ', $searchParts) . ')';
    }
    if ($status !== '' && isset($config['statusExpression'])) {
        $allowedStatuses = array_column($config['statusOptions'], 0);
        if (in_array($status, $allowedStatuses, true)) {
            $where[] = $config['statusExpression'] . ' = :status_filter';
            $parameters['status_filter'] = $status;
        } else {
            $status = '';
        }
    }
    $whereSql = $where === [] ? '' : ' WHERE ' . implode(' AND ', $where);

    $countQuery = $connection->prepare(
        'SELECT COUNT(DISTINCT ' . $config['idExpression'] . ') ' . $config['from'] . $whereSql
    );
    $countQuery->execute($parameters);
    $totalRows = (int) $countQuery->fetchColumn();
    $totalPages = max(1, (int) ceil($totalRows / $perPage));
    $page = min($page, $totalPages);

    $sortKey = moduleRequestString($_GET['orden'] ?? '');
    $sortExpression = $config['sortOptions'][$sortKey] ?? $config['idExpression'];
    $direction = strtolower(moduleRequestString($_GET['direccion'] ?? 'desc')) === 'asc' ? 'ASC' : 'DESC';
    $listSql = $config['listSql'] . ' ' . $config['from'] . $whereSql
        . ' ORDER BY ' . $sortExpression . ' ' . $direction
        . ' LIMIT ' . $perPage . ' OFFSET ' . (($page - 1) * $perPage);
    $listQuery = $connection->prepare($listSql);
    $listQuery->execute($parameters);

    $summaries = [];
    foreach ($config['summary'] as $summary) {
        $summaries[] = [
            'label' => $summary['label'],
            'value' => $connection->query($summary['sql'])->fetchColumn(),
        ];
    }

    $recordIdSource = $_GET['editar'] ?? ($_SERVER['REQUEST_METHOD'] === 'POST' ? ($_POST['id'] ?? 0) : 0);
    $recordId = filter_var($recordIdSource, FILTER_VALIDATE_INT);
    $recordId = $recordId === false ? 0 : max(0, $recordId);
    $record = $recordId > 0 ? moduleCurrentRow($connection, $config, $recordId) : null;
    $showForm = isset($_GET['nuevo'])
        || $record !== null
        || ($_SERVER['REQUEST_METHOD'] === 'POST'
            && ($_POST['action'] ?? '') === 'save'
            && $error !== null);
    $formValues = $record ?? [];
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'save' && $error !== null) {
        $formValues = $_POST;
    }

    $options = [];
    $relationSearch = moduleRequestString($_GET['relacion'] ?? $_POST['relacion'] ?? '');
    foreach ($config['fields'] as $name => $field) {
        if (($field['type'] ?? '') === 'select') {
            $options[$name] = moduleOptions(
                $connection,
                $field,
                $recordId > 0 ? $recordId : null,
                $relationSearch,
                isset($formValues[$name]) ? (string) $formValues[$name] : null
            );
        }
    }

    $flash = (string) ($_SESSION['module_flash'] ?? '');
    unset($_SESSION['module_flash']);

    return compact(
        'config',
        'route',
        'error',
        'flash',
        'search',
        'status',
        'page',
        'perPage',
        'totalRows',
        'totalPages',
        'sortKey',
        'direction',
        'summaries',
        'recordId',
        'record',
        'showForm',
        'formValues',
        'options',
        'relationSearch',
        'listQuery',
        'connection'
    );
}

function renderModulePage(string $key): void
{
    if (session_status() !== PHP_SESSION_ACTIVE) {
        session_start();
    }
    if (!isset($_SESSION['module_csrf'])) {
        $_SESSION['module_csrf'] = bin2hex(random_bytes(32));
    }

    try {
        $connection = databaseConnection();
        $data = modulePageData($connection, $key);
    } catch (PDOException $exception) {
        error_log('Module page connection error: ' . $exception->getMessage());
        http_response_code(500);
        $data = null;
    }

    $pageTitle = isset($data['config']['title']) ? $data['config']['title'] : 'Módulo';
    require dirname(__DIR__) . '/modulos/_module_page.php';
}

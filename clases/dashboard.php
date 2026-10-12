<?php
declare(strict_types=1);

function getDashboardData(PDO $connection): array
{
    $stats = [
        'activeStudents' => (int) $connection->query(
            "SELECT COUNT(*) FROM alumno WHERE estado = 'Activo'"
        )->fetchColumn(),
        'confirmedEnrollments' => (int) $connection->query(
            "SELECT COUNT(*) FROM inscripcion WHERE estado = 'Confirmada'"
        )->fetchColumn(),
        'pendingEnrollments' => (int) $connection->query(
            "SELECT COUNT(*) FROM inscripcion WHERE estado = 'Pendiente'"
        )->fetchColumn(),
        'activeTeachers' => (int) $connection->query(
            "SELECT COUNT(*) FROM docente WHERE estado = 'Activo'"
        )->fetchColumn(),
        'activeCourses' => (int) $connection->query(
            "SELECT COUNT(*) FROM curso WHERE estado = 'Activo'"
        )->fetchColumn(),
        'totalEnrollments' => (int) $connection->query(
            'SELECT COUNT(*) FROM inscripcion'
        )->fetchColumn(),
    ];

    $periodQuery = $connection->query(
        "SELECT nombre FROM periodo_lectivo WHERE estado = 'Activo' ORDER BY fecha_inicio DESC LIMIT 1"
    );
    $periodName = $periodQuery->fetchColumn();

    $academicQuery = $connection->query(
        "SELECT
            AVG(rm.promedio) AS promedio,
            COALESCE(SUM(rm.resultado = 'Aprobada'), 0) AS aprobadas,
            COALESCE(SUM(rm.resultado = 'En curso'), 0) AS en_curso
        FROM resultado_materia AS rm
        INNER JOIN inscripcion AS i ON i.id_inscripcion = rm.id_inscripcion
        INNER JOIN curso AS c ON c.id_curso = i.id_curso
        INNER JOIN periodo_lectivo AS p ON p.id_periodo_lectivo = c.id_periodo
        WHERE p.estado = 'Activo'"
    );
    $academic = $academicQuery->fetch();

    $monthStart = (new DateTimeImmutable('first day of this month'))->modify('-5 months');
    $monthEnd = $monthStart->modify('+6 months');
    $monthlyEnrollments = [];
    for ($month = $monthStart; $month < $monthEnd; $month = $month->modify('+1 month')) {
        $monthlyEnrollments[$month->format('Y-m')] = [
            'month' => $month->format('Y-m'),
            'total' => 0,
            'confirmed' => 0,
        ];
    }

    $monthlyQuery = $connection->prepare(
        "SELECT
            DATE_FORMAT(fecha_inscripcion, '%Y-%m') AS mes,
            COUNT(*) AS total,
            COALESCE(SUM(estado = 'Confirmada'), 0) AS confirmadas
        FROM inscripcion
        WHERE fecha_inscripcion >= :desde AND fecha_inscripcion < :hasta
        GROUP BY DATE_FORMAT(fecha_inscripcion, '%Y-%m')
        ORDER BY mes"
    );
    $monthlyQuery->execute([
        'desde' => $monthStart->format('Y-m-d'),
        'hasta' => $monthEnd->format('Y-m-d'),
    ]);
    foreach ($monthlyQuery->fetchAll() as $row) {
        $monthlyEnrollments[$row['mes']]['total'] = (int) $row['total'];
        $monthlyEnrollments[$row['mes']]['confirmed'] = (int) $row['confirmadas'];
    }

    $recentQuery = $connection->query(
        'SELECT
            CONCAT(a.nombre, \' \', a.apellido) AS alumno,
            i.fecha_inscripcion,
            i.estado
        FROM inscripcion AS i
        INNER JOIN alumno AS a ON a.id_alumno = i.id_alumno
        ORDER BY i.fecha_inscripcion DESC, i.id_inscripcion DESC
        LIMIT 5'
    );
    $recentEnrollments = array_map(
        static fn (array $row): array => [
            'student' => $row['alumno'],
            'date' => $row['fecha_inscripcion'],
            'status' => $row['estado'],
        ],
        $recentQuery->fetchAll()
    );

    $statusQuery = $connection->query(
        'SELECT estado, COUNT(*) AS cantidad
        FROM inscripcion
        GROUP BY estado
        ORDER BY FIELD(estado, \'Pendiente\', \'Confirmada\', \'Finalizada\', \'Cancelada\')'
    );
    $enrollmentStatuses = array_map(
        static fn (array $row): array => [
            'status' => $row['estado'],
            'count' => (int) $row['cantidad'],
        ],
        $statusQuery->fetchAll()
    );

    return [
        'stats' => $stats,
        'academic' => [
            'periodName' => $periodName === false ? null : $periodName,
            'average' => $academic['promedio'] === null ? null : (float) $academic['promedio'],
            'approved' => (int) $academic['aprobadas'],
            'inProgress' => (int) $academic['en_curso'],
        ],
        'monthlyEnrollments' => array_values($monthlyEnrollments),
        'recentEnrollments' => $recentEnrollments,
        'enrollmentStatuses' => $enrollmentStatuses,
    ];
}

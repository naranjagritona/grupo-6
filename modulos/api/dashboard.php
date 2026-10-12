<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');

require_once dirname(__DIR__, 2) . '/clases/database.php';
require_once dirname(__DIR__, 2) . '/clases/dashboard.php';

try {
    $data = getDashboardData(databaseConnection());
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
} catch (PDOException $exception) {
    error_log('Dashboard database error: ' . $exception->getMessage());
    http_response_code(500);
    echo json_encode(
        ['error' => 'No fue posible consultar los datos del panel.'],
        JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR
    );
}

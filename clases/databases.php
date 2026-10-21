<?php
declare(strict_types=1);

function databaseConnection(): PDO
{
    $host = getenv('DB_HOST') ?: 'pma.torga.com.ar';
    $port = (int) (getenv('DB_PORT') ?: '3306');
    $database = getenv('DB_NAME') ?: 's23_academia_integral_xxi';
    $username = getenv('DB_USER') ?: 'u23_GZ8nhz7rRw';
    $password = getenv('DB_PASSWORD') ?: 'SGpFfP1KLWxw9Gv=ile+whK=';

    $dsn = sprintf(
        'mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4',
        $host,
        $port,
        $database
    );

    return new PDO($dsn, $username, $password === false ? '' : $password, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
}

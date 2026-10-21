<?php
session_start();
require_once dirname(__DIR__) . '/clases/dao/usuarioDAO.php';

$token = is_string($_POST['csrf_token'] ?? null) ? $_POST['csrf_token'] : '';
$tokenSesion = is_string($_SESSION['logout_csrf'] ?? null) ? $_SESSION['logout_csrf'] : '';
if ($_SERVER['REQUEST_METHOD'] !== 'POST' || $tokenSesion === '' || !hash_equals($tokenSesion, $token)) {
    http_response_code(400);
    exit('La solicitud para cerrar sesión no es válida. Recargá la página e intentá nuevamente.');
}

UsuarioDAO::cerrarSesion();
header('Location: login.php');
exit;

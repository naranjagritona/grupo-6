<?php
session_start();
require_once("../clases/dao/usuarioDAO.php");

$error = "";


if ($_SERVER["REQUEST_METHOD"] === "POST") {
    //obtenemos el valor del campo usuario y la contraseña 
    //que se envio desde el formulario
    $nombre_usuario = trim($_POST['nombre_usuario'] ?? '');
    $contrasena = trim($_POST['contraseña'] ?? '');

   
    if ($nombre_usuario !== '' && $contrasena !== '') {
        //aca instanciamos el objeto del dao para buscar el usuario
        $dao = new UsuarioDAO(databaseConnection());
        $resultado = $dao->autenticar($nombre_usuario, $contrasena);

        if ($resultado !== false) {
            session_regenerate_id(true);
            $_SESSION['usuario'] = $resultado['nombre_usuario'];
            $_SESSION['id_usuario'] = (int) $resultado['id_usuario'];
            $_SESSION['id_rol'] = (int) $resultado['id_rol'];
            $_SESSION['nombre_rol'] = (string) ($resultado['nombre_rol'] ?? '');
            $_SESSION['id_docente'] = isset($resultado['id_docente']) ? (int) $resultado['id_docente'] : null;
            $_SESSION['id_alumno'] = isset($resultado['id_alumno']) ? (int) $resultado['id_alumno'] : null;
            
            header('Location: ' . ($resultado['es_admin'] ? 'admin.php' : 'cursos.php'));
            exit();
        } else {
            $error = "Nombre de usuario o contraseña incorrectos.";
        }
    } else {
        $error = "Por favor, complete todos los campos obligatorios.";
    }
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - Academia Integral XXI</title>
    <link rel="stylesheet" href="estilos/login_estilo.css">
</head>
<body>
    <div class="login-container">
        
        <img src="../img/logo.png" alt="Logo Academia Integral XXI" class="logo">
        <h2>Academia Integral XXI</h2>
        <h2 class="login-titulo">Acceda a su cuenta</h2>
        <p class="login-subtitulo">Gestione sus datos académicos y administrativos</p>
            
        
        <form action="login.php" method="POST">
            <div class="form-group">
                <label for="nombre_usuario">Usuario</label>
                <input type="text" id="nombre_usuario" name="nombre_usuario" required autocomplete="off">
            </div>

            <div class="form-group">
                <label for="contraseña">Contraseña</label>
                <input type="password" id="contraseña" name="contraseña" required>
            </div>
            
            <button type="submit" class="btn-login">Ingresar</button>

            <?php if ($error !== ''): ?>
                <div class="error-message"><?php echo htmlspecialchars($error); ?></div>
            <?php endif; ?>
        </form>
    </div>
</body>
</html>
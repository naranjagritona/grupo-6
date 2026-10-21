<?php

require_once __DIR__ . '/../database.php';
require_once 'dao.php';

class UsuarioDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($usuario): void {
        $sql = "INSERT INTO usuario (id_rol, nombre_usuario, contraseña) VALUES (?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $usuario->getId_rol(),
            $usuario->getNombre_usuario(),
            $usuario->getContraseña()
        ]);
    }
    
    public function modificar($usuario) {
        $sql = "UPDATE usuario SET id_rol = ?, nombre_usuario = ?, contraseña = ? WHERE id_usuario = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $usuario->getId_rol(),
            $usuario->getNombre_usuario(),
            $usuario->getContraseña(),
            $usuario->getId_usuario()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM usuario WHERE id_usuario = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM usuario";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Usuario(
                $fila['id_usuario'],
                $fila['id_rol'],
                $fila['nombre_usuario'],
                $fila['contraseña']
            );
        }
        return $resultados;
    }

    public function autenticar($nombre_usuario, $contraseña) {
        $consulta = "SELECT u.*, r.nombre AS nombre_rol
            FROM usuario AS u
            LEFT JOIN rol AS r ON r.id_rol = u.id_rol
            WHERE u.nombre_usuario = ?";
        $stmt = $this->conexion->prepare($consulta);
        $stmt->execute([$nombre_usuario]);
        $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$usuario) {
            return false;
        }

        $contexto = $this->obtenerContexto((int) $usuario['id_usuario']);
        if ($contexto === null || !$contexto['activo']) {
            return false;
        }

        $contraseñaAlmacenada = (string) $usuario['contraseña'];
        $informacionHash = password_get_info($contraseñaAlmacenada);
        $contraseñaValida = $informacionHash['algo'] !== null
            ? password_verify($contraseña, $contraseñaAlmacenada)
            : hash_equals($contraseñaAlmacenada, $contraseña);

        if (!$contraseñaValida) {
            return false;
        }

        if ($informacionHash['algo'] === null) {
            $actualizar = $this->conexion->prepare(
                'UPDATE usuario SET contraseña = ? WHERE id_usuario = ?'
            );
            $actualizar->execute([
                password_hash($contraseña, PASSWORD_DEFAULT),
                $usuario['id_usuario'],
            ]);
        }

        return $usuario + $contexto;
    }

    public function obtenerContexto(int $idUsuario): ?array
    {
        $consulta = $this->conexion->prepare(
            "SELECT u.id_usuario, u.id_rol, u.nombre_usuario, r.nombre AS nombre_rol,
                d.id_docente, a.id_alumno,
                COALESCE(d.nombre, a.nombre) AS nombre,
                COALESCE(d.apellido, a.apellido) AS apellido,
                CASE
                    WHEN LOWER(COALESCE(r.nombre, '')) IN ('docente', 'profesor', 'profesora')
                        THEN COALESCE(d.estado, '')
                    WHEN LOWER(COALESCE(r.nombre, '')) IN ('alumno', 'estudiante')
                        THEN COALESCE(a.estado, '')
                    ELSE 'Activo'
                END AS estado_perfil
            FROM usuario AS u
            LEFT JOIN rol AS r ON r.id_rol = u.id_rol
            LEFT JOIN docente AS d ON d.id_usuario = u.id_usuario
            LEFT JOIN alumno AS a ON a.id_usuario = u.id_usuario
            WHERE u.id_usuario = ?
            LIMIT 1"
        );
        $consulta->execute([$idUsuario]);
        $usuario = $consulta->fetch(PDO::FETCH_ASSOC);
        if (!$usuario) {
            return null;
        }

        $rol = strtolower(trim((string) ($usuario['nombre_rol'] ?? '')));
        $usuario['es_admin'] = in_array($rol, ['admin', 'administrador'], true) || (int) $usuario['id_rol'] === 1;
        $usuario['es_docente'] = in_array($rol, ['docente', 'profesor', 'profesora'], true) || (int) $usuario['id_rol'] === 3;
        $usuario['es_alumno'] = in_array($rol, ['alumno', 'estudiante'], true) || (int) $usuario['id_rol'] === 4;
        $usuario['activo'] = $usuario['es_admin'] || (
            (!$usuario['es_docente'] || ($usuario['id_docente'] !== null && strcasecmp((string) $usuario['estado_perfil'], 'Activo') === 0))
            && (!$usuario['es_alumno'] || ($usuario['id_alumno'] !== null && strcasecmp((string) $usuario['estado_perfil'], 'Activo') === 0))
        );
        $usuario['nombre_completo'] = trim((string) ($usuario['nombre'] ?? '') . ' ' . (string) ($usuario['apellido'] ?? ''));

        return $usuario;
    }

    public function obtenerSesionActual(): array
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            session_start();
        }
        if (empty($_SESSION['id_usuario'])) {
            header('Location: login.php');
            exit;
        }

        $contexto = $this->obtenerContexto((int) $_SESSION['id_usuario']);
        if ($contexto === null || !$contexto['activo']) {
            self::cerrarSesion();
            header('Location: login.php');
            exit;
        }
        if (!$contexto['es_admin'] && !$contexto['es_docente'] && !$contexto['es_alumno']) {
            http_response_code(403);
            exit('El rol de esta cuenta no tiene acceso a los módulos.');
        }
        $_SESSION['id_rol'] = (int) $contexto['id_rol'];
        $_SESSION['nombre_rol'] = (string) ($contexto['nombre_rol'] ?? '');
        $contexto['name'] = $contexto['nombre_completo'] !== ''
            ? $contexto['nombre_completo']
            : (string) ($contexto['nombre_rol'] ?? 'Usuario');
        $contexto['role'] = (string) ($contexto['nombre_rol'] ?? 'Usuario');
        $contexto['isAdmin'] = $contexto['es_admin'];
        $contexto['isTeacher'] = $contexto['es_docente'];
        $contexto['isStudent'] = $contexto['es_alumno'];
        return $contexto;
    }

    // Función de consulta relacionada con usuario movida al DAO
    public function obtenerNombreReal(?int $idUsuario, ?string $rolSesion = null): string {
        if (!$idUsuario) {
            return 'Usuario';
        }

        $usuario = $this->obtenerContexto($idUsuario);
        if ($usuario && $usuario['nombre_completo'] !== '') {
            return $usuario['nombre_completo'];
        }

        if ($rolSesion !== null && $rolSesion !== '') {
            return $rolSesion;
        }

        return 'Usuario';
    }

    public static function cerrarSesion(): void
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            session_start();
        }

        $_SESSION = [];
        if (ini_get('session.use_cookies')) {
            $parametros = session_get_cookie_params();
            setcookie(
                session_name(),
                '',
                time() - 42000,
                $parametros['path'],
                $parametros['domain'],
                $parametros['secure'],
                $parametros['httponly']
            );
        }

        session_destroy();
    }
}
?>
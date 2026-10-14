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
        try {
            $conexion = databaseConnection();
            
            $consulta = "SELECT * FROM usuario WHERE nombre_usuario = ? AND contraseña = ?";
            $stmt = $conexion->prepare($consulta);
            $stmt->execute([$nombre_usuario, $contraseña]);
            
            $usuario = $stmt->fetch(PDO::FETCH_ASSOC);
            
            return $usuario ? $usuario : false;
            
        } catch (PDOException $e) {
            error_log("Error en autenticación: " . $e->getMessage());
            return false;
        }
    }

    // Función de consulta relacionada con usuario movida al DAO
    public function obtenerNombreReal(?int $idUsuario, ?string $usuarioSesion): string {
        if (!$idUsuario) {
            return $usuarioSesion ?? 'Usuario';
        }

        try {
            $stmtDocente = $this->conexion->prepare("SELECT nombre, apellido FROM docente WHERE id_usuario = ?");
            $stmtDocente->execute([$idUsuario]);
            $docente = $stmtDocente->fetch(PDO::FETCH_ASSOC);
            if ($docente) {
                return $docente['nombre'] . ' ' . $docente['apellido'];
            }

            $stmtAlumno = $this->conexion->prepare("SELECT nombre, apellido FROM alumno WHERE id_usuario = ?");
            $stmtAlumno->execute([$idUsuario]);
            $alumno = $stmtAlumno->fetch(PDO::FETCH_ASSOC);
            if ($alumno) {
                return $alumno['nombre'] . ' ' . $alumno['apellido'];
            }
        } catch (PDOException $e) {
            error_log("Error en obtenerNombreReal: " . $e->getMessage());
        }

        return $usuarioSesion ?? 'Usuario';
    }
}
?>
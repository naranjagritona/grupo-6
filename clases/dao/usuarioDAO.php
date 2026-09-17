<?php

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

    public function actualizar($usuario): void {
        $this->modificar($usuario);
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

    public function buscarPorId($id) {
        return $this->obtenerPorId($id);
    }

    public function obtenerPorId($id) {
        $sql = "SELECT * FROM usuario WHERE id_usuario = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
        $fila = $consulta->fetch(PDO::FETCH_ASSOC);
        if ($fila) {
            return new Usuario(
                $fila['id_usuario'],
                $fila['id_rol'],
                $fila['nombre_usuario'],
                $fila['contraseña']
            );
        }
        return null;
    }

    public function listar() {
        return $this->listarTodos();
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
}
?>
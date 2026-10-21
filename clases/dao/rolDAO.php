<?php

require_once 'dao.php';

class RolDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($rol): void {
        $sql = "INSERT INTO rol (nombre, descripcion_rol) VALUES (?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $rol->getNombre(),
            $rol->getDescripcion_rol()
        ]);
    }

    public function modificar($rol) {
        $sql = "UPDATE rol SET nombre = ?, descripcion_rol = ? WHERE id_rol = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $rol->getNombre(),
            $rol->getDescripcion_rol(),
            $rol->getId_rol()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM rol WHERE id_rol = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM rol";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Rol(
                $fila['id_rol'],
                $fila['nombre'],
                $fila['descripcion_rol']
            );
        }
        return $resultados;
    }
}
?>
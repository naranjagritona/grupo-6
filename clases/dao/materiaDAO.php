<?php

require_once 'dao.php';

class MateriaDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($materia): void {
        $sql = "INSERT INTO materia (nombre_materia) VALUES (?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $materia->getNombre_materia()
        ]);
    }

    public function modificar($materia) {
        $sql = "UPDATE materia SET nombre_materia = ? WHERE id_materia = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $materia->getNombre_materia(),
            $materia->getId_materia()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM materia WHERE id_materia = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM materia";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Materia(
                $fila['id_materia'],
                $fila['nombre_materia']
            );
        }
        return $resultados;
    }
}
?>
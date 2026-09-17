<?php

require_once 'dao.php';

class ResponsableDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($responsable): void {
        $sql = "INSERT INTO responsable (nombre, apellido, dni, tipo_relacion) VALUES (?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $responsable->getNombre(),
            $responsable->getApellido(),
            $responsable->getDni(),
            $responsable->getTipo_relacion()
        ]);
    }

    public function actualizar($responsable): void {
        $this->modificar($responsable);
    }

    public function modificar($responsable) {
        $sql = "UPDATE responsable SET nombre = ?, apellido = ?, dni = ?, tipo_relacion = ? WHERE id_responsable = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $responsable->getNombre(),
            $responsable->getApellido(),
            $responsable->getDni(),
            $responsable->getTipo_relacion(),
            $responsable->getId_responsable()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM responsable WHERE id_responsable = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function buscarPorId($id) {
        return $this->obtenerPorId($id);
    }

    public function obtenerPorId($id) {
        $sql = "SELECT * FROM responsable WHERE id_responsable = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
        $fila = $consulta->fetch(PDO::FETCH_ASSOC);
        if ($fila) {
            return new Responsable(
                $fila['id_responsable'],
                $fila['nombre'],
                $fila['apellido'],
                $fila['dni'],
                $fila['tipo_relacion']
            );
        }
        return null;
    }

    public function listar() {
        return $this->listarTodos();
    }

    public function listarTodos() {
        $sql = "SELECT * FROM responsable";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Responsable(
                $fila['id_responsable'],
                $fila['nombre'],
                $fila['apellido'],
                $fila['dni'],
                $fila['tipo_relacion']
            );
        }
        return $resultados;
    }
}
?>
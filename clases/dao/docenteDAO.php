<?php

require_once 'dao.php';

class DocenteDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($docente): void {
        $sql = "INSERT INTO docente (id_usuario, nombre, apellido, dni, legajo, estado) VALUES (?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $docente->getId_usuario(),
            $docente->getNombre(),
            $docente->getApellido(),
            $docente->getDni(),
            $docente->getLegajo(),
            $docente->getEstado()->value
        ]);
    }

    public function modificar($docente) {
        $sql = "UPDATE docente SET id_usuario = ?, nombre = ?, apellido = ?, dni = ?, legajo = ?, estado = ? WHERE id_docente = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $docente->getId_usuario(),
            $docente->getNombre(),
            $docente->getApellido(),
            $docente->getDni(),
            $docente->getLegajo(),
            $docente->getEstado()->value,
            $docente->getId_docente()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM docente WHERE id_docente = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM docente";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Docente(
                $fila['id_docente'],
                $fila['id_usuario'],
                $fila['nombre'],
                $fila['apellido'],
                $fila['dni'],
                $fila['legajo'],
                EstadoDocente::from($fila['estado'])
            );
        }
        return $resultados;
    }
}
?>
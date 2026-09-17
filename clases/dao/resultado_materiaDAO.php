<?php

require_once 'dao.php';

class ResultadoMateriaDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($resultado): void {
        $sql = "INSERT INTO resultado_materia (id_inscripcion, promedio, resultado, fecha_calculo) VALUES (?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $resultado->getId_inscripcion(),
            $resultado->getPromedio(),
            $resultado->getResultado()->value,
            $resultado->getFecha_calculo()
        ]);
    }

    public function actualizar($resultado): void {
        $this->modificar($resultado);
    }

    public function modificar($resultado) {
        $sql = "UPDATE resultado_materia SET id_inscripcion = ?, promedio = ?, resultado = ?, fecha_calculo = ? WHERE id_resultado = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $resultado->getId_inscripcion(),
            $resultado->getPromedio(),
            $resultado->getResultado()->value,
            $resultado->getFecha_calculo(),
            $resultado->getId_resultado()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM resultado_materia WHERE id_resultado = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function buscarPorId($id) {
        return $this->obtenerPorId($id);
    }

    public function obtenerPorId($id) {
        $sql = "SELECT * FROM resultado_materia WHERE id_resultado = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
        $fila = $consulta->fetch(PDO::FETCH_ASSOC);
        if ($fila) {
            return new ResultadoMateria(
                $fila['id_resultado'],
                $fila['id_inscripcion'],
                $fila['promedio'],
                EstadoResultadoMateria::from($fila['resultado']),
                $fila['fecha_calculo']
            );
        }
        return null;
    }

    public function listar() {
        return $this->listarTodos();
    }

    public function listarTodos() {
        $sql = "SELECT * FROM resultado_materia";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new ResultadoMateria(
                $fila['id_resultado'],
                $fila['id_inscripcion'],
                $fila['promedio'],
                EstadoResultadoMateria::from($fila['resultado']),
                $fila['fecha_calculo']
            );
        }
        return $resultados;
    }
}
?>
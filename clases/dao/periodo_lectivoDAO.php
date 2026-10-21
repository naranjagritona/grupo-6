<?php

require_once 'dao.php';

class PeriodoLectivoDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($periodo): void {
        $sql = "INSERT INTO periodo_lectivo (nombre, tipo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $periodo->getNombre(),
            $periodo->getTipo()->value,
            $periodo->getFecha_inicio(),
            $periodo->getFecha_fin(),
            $periodo->getEstado()->value
        ]);
    }
 
    public function modificar($periodo) {
        $sql = "UPDATE periodo_lectivo SET nombre = ?, tipo = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_periodo = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $periodo->getNombre(),
            $periodo->getTipo()->value,
            $periodo->getFecha_inicio(),
            $periodo->getFecha_fin(),
            $periodo->getEstado()->value,
            $periodo->getId_periodo()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM periodo_lectivo WHERE id_periodo = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM periodo_lectivo";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new PeriodoLectivo(
                $fila['id_periodo'],
                $fila['nombre'],
                TipoPeriodoLectivo::from($fila['tipo']),
                $fila['fecha_inicio'],
                $fila['fecha_fin'],
                EstadoPeriodoLectivo::from($fila['estado'])
            );
        }
        return $resultados;
    }
}
?>
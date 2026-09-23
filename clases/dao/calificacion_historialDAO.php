<?php

require_once 'dao.php';

class CalificacionHistorialDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($historial): void {
        $sql = "INSERT INTO calificacion_historial (id_calificacion, nota_anterior, nota_nueva, fecha_modificacion, motivo) VALUES (?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $historial->getId_calificacion(),
            $historial->getNota_anterior(),
            $historial->getNota_nueva(),
            $historial->getFecha_modificacion(),
            $historial->getMotivo()
        ]);
    }

    public function modificar($historial) {
        $sql = "UPDATE calificacion_historial SET id_calificacion = ?, nota_anterior = ?, nota_nueva = ?, fecha_modificacion = ?, motivo = ? WHERE id_historial = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $historial->getId_calificacion(),
            $historial->getNota_anterior(),
            $historial->getNota_nueva(),
            $historial->getFecha_modificacion(),
            $historial->getMotivo(),
            $historial->getId_historial()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM calificacion_historial WHERE id_historial = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM calificacion_historial";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new CalificacionHistorial(
                $fila['id_historial'],
                $fila['id_calificacion'],
                $fila['nota_anterior'],
                $fila['nota_nueva'],
                $fila['fecha_modificacion'],
                $fila['motivo']
            );
        }
        return $resultados;
    }
}
?>
<?php

require_once 'dao.php';

class CalificacionDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($calificacion): void {
        $sql = "INSERT INTO calificacion (id_inscripcion, id_columna_notas, nota, fecha_carga, observaciones) VALUES (?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $calificacion->getId_inscripcion(),
            $calificacion->getId_columna_notas(),
            $calificacion->getNota(),
            $calificacion->getFecha_carga(),
            $calificacion->getObservaciones()
        ]);
    }

    public function modificar($calificacion) {
        $sql = "UPDATE calificacion SET id_inscripcion = ?, id_columna_notas = ?, nota = ?, fecha_carga = ?, observaciones = ? WHERE id_calificacion = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $calificacion->getId_inscripcion(),
            $calificacion->getId_columna_notas(),
            $calificacion->getNota(),
            $calificacion->getFecha_carga(),
            $calificacion->getObservaciones(),
            $calificacion->getId_calificacion()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM calificacion WHERE id_calificacion = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM calificacion";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Calificacion(
                $fila['id_calificacion'],
                $fila['id_inscripcion'],
                $fila['id_columna_notas'],
                $fila['nota'],
                $fila['fecha_carga'],
                $fila['observaciones']
            );
        }
        return $resultados;
    }
}
?>
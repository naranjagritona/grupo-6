<?php

require_once 'dao.php';

class InscripcionDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($inscripcion): void {
        $sql = "INSERT INTO inscripcion (id_alumno, id_curso, fecha_inscripcion, estado, motivo_baja, importe_cuota, fecha_creacion, fecha_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $inscripcion->getId_alumno(),
            $inscripcion->getId_curso(),
            $inscripcion->getFecha_inscripcion(),
            $inscripcion->getEstado()->value,
            $inscripcion->getMotivo_baja(),
            $inscripcion->getImporte_cuota(),
            $inscripcion->getFecha_creacion(),
            $inscripcion->getFecha_modificacion()
        ]);
    }

    public function modificar($inscripcion) {
        $sql = "UPDATE inscripcion SET id_alumno = ?, id_curso = ?, fecha_inscripcion = ?, estado = ?, motivo_baja = ?, importe_cuota = ?, fecha_creacion = ?, fecha_modificacion = ? WHERE id_inscripcion = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $inscripcion->getId_alumno(),
            $inscripcion->getId_curso(),
            $inscripcion->getFecha_inscripcion(),
            $inscripcion->getEstado()->value,
            $inscripcion->getMotivo_baja(),
            $inscripcion->getImporte_cuota(),
            $inscripcion->getFecha_creacion(),
            $inscripcion->getFecha_modificacion(),
            $inscripcion->getId_inscripcion()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM inscripcion WHERE id_inscripcion = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM inscripcion";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Inscripcion(
                $fila['id_inscripcion'],
                $fila['id_alumno'],
                $fila['id_curso'],
                $fila['fecha_inscripcion'],
                EstadoInscripcion::from($fila['estado']),
                $fila['motivo_baja'],
                $fila['importe_cuota'],
                $fila['fecha_creacion'],
                $fila['fecha_modificacion']
            );
        }
        return $resultados;
    }
}
?>
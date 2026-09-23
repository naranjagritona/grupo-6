<?php

require_once 'dao.php';

class ColumnaNotasDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($columna): void {
        $sql = "INSERT INTO columna_notas (id_curso, titulo, tipo, nota_maxima, nota_aprobacion, fecha_evaluacion, fecha_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $columna->getId_curso(),
            $columna->getTitulo(),
            $columna->getTipo()->value,
            $columna->getNota_maxima(),
            $columna->getNota_aprobacion(),
            $columna->getFecha_evaluacion(),
            $columna->getFecha_cierre(),
            $columna->getEstado()->value
        ]);
    }

    public function modificar($columna) {
        $sql = "UPDATE columna_notas SET id_curso = ?, titulo = ?, tipo = ?, nota_maxima = ?, nota_aprobacion = ?, fecha_evaluacion = ?, fecha_cierre = ?, estado = ? WHERE id_columna_notas = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $columna->getId_curso(),
            $columna->getTitulo(),
            $columna->getTipo()->value,
            $columna->getNota_maxima(),
            $columna->getNota_aprobacion(),
            $columna->getFecha_evaluacion(),
            $columna->getFecha_cierre(),
            $columna->getEstado()->value,
            $columna->getId_columna_notas()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM columna_notas WHERE id_columna_notas = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM columna_notas";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new ColumnaNotas(
                $fila['id_columna_notas'],
                $fila['id_curso'],
                $fila['titulo'],
                TipoColumnaNotas::from($fila['tipo']),
                $fila['nota_maxima'],
                $fila['nota_aprobacion'],
                $fila['fecha_evaluacion'],
                $fila['fecha_cierre'],
                EstadoColumnaNotas::from($fila['estado'])
            );
        }
        return $resultados;
    }
}
?>
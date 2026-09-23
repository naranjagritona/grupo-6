<?php

require_once 'dao.php';

class CursoDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($curso): void {
        $sql = "INSERT INTO curso (id_periodo, id_materia, id_docente, año, division, modalidad, turno, cupo_maximo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $curso->getId_periodo(),
            $curso->getId_materia(),
            $curso->getId_docente(),
            $curso->getAño(),
            $curso->getDivision(),
            $curso->getModalidad()->value,
            $curso->getTurno()->value,
            $curso->getCupo_maximo(),
            $curso->getFecha_inicio(),
            $curso->getFecha_fin(),
            $curso->getEstado()->value
        ]);
    }

    public function modificar($curso) {
        $sql = "UPDATE curso SET id_periodo = ?, id_materia = ?, id_docente = ?, año = ?, division = ?, modalidad = ?, turno = ?, cupo_maximo = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_curso = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $curso->getId_periodo(),
            $curso->getId_materia(),
            $curso->getId_docente(),
            $curso->getAño(),
            $curso->getDivision(),
            $curso->getModalidad()->value,
            $curso->getTurno()->value,
            $curso->getCupo_maximo(),
            $curso->getFecha_inicio(),
            $curso->getFecha_fin(),
            $curso->getEstado()->value,
            $curso->getId_curso()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM curso WHERE id_curso = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM curso";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Curso(
                $fila['id_curso'],
                $fila['id_periodo'],
                $fila['id_materia'],
                $fila['id_docente'],
                $fila['año'],
                $fila['division'],
                ModalidadCurso::from($fila['modalidad']),
                TurnoCurso::from($fila['turno']),
                $fila['cupo_maximo'],
                $fila['fecha_inicio'],
                $fila['fecha_fin'],
                EstadoCurso::from($fila['estado'])
            );
        }
        return $resultados;
    }
}
?>
<?php

require_once 'dao.php';

class AlumnoDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($alumno): void {
        $sql = "INSERT INTO alumno (id_usuario, id_responsable, nombre, apellido, dni, matricula, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $alumno->getId_usuario(),
            $alumno->getId_responsable(),
            $alumno->getNombre(),
            $alumno->getApellido(),
            $alumno->getDni(),
            $alumno->getMatricula(),
            $alumno->getEstado()->value
        ]);
    }

    public function modificar($alumno) {
        $sql = "UPDATE alumno SET id_usuario = ?, id_responsable = ?, nombre = ?, apellido = ?, dni = ?, matricula = ?, estado = ? WHERE id_alumno = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $alumno->getId_usuario(),
            $alumno->getId_responsable(),
            $alumno->getNombre(),
            $alumno->getApellido(),
            $alumno->getDni(),
            $alumno->getMatricula(),
            $alumno->getEstado()->value,
            $alumno->getId_alumno()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM alumno WHERE id_alumno = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM alumno";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Alumno(
                $fila['id_alumno'],
                $fila['id_usuario'],
                $fila['id_responsable'],
                $fila['nombre'],
                $fila['apellido'],
                $fila['dni'],
                $fila['matricula'],
                EstadoAlumno::from($fila['estado'])
            );
        }
        return $resultados;
    }
}
?>
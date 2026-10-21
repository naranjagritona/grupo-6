<?php

require_once 'dao.php';

class InscripcionDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($inscripcion): void {
        $sql = "INSERT INTO inscripcion (id_alumno, id_curso, fecha_inscripcion, estado, importe_cuota, fecha_creacion) VALUES (?, ?, ?, ?, ?, NOW())";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $inscripcion->getId_alumno(),
            $inscripcion->getId_curso(),
            $inscripcion->getFecha_inscripcion(),
            $inscripcion->getEstado()->value,
            $inscripcion->getImporte_cuota()
        ]);
    }

    public function modificar($inscripcion) {
        $sql = "UPDATE inscripcion SET id_alumno = ?, id_curso = ?, fecha_inscripcion = ?, estado = ?, importe_cuota = ?, fecha_modificacion = NOW() WHERE id_inscripcion = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $inscripcion->getId_alumno(),
            $inscripcion->getId_curso(),
            $inscripcion->getFecha_inscripcion(),
            $inscripcion->getEstado()->value,
            $inscripcion->getImporte_cuota(),
            $inscripcion->getId_inscripcion()
        ]);
    }

    public function darDeBaja(int $idInscripcion, string $motivoBaja): bool {
        $sql = "UPDATE inscripcion SET estado = 'Finalizada', motivo_baja = ?, fecha_modificacion = NOW() WHERE id_inscripcion = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([$motivoBaja, $idInscripcion]);
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
                $fila['motivo_baja'] ?? '',
                $fila['importe_cuota'],
                $fila['fecha_creacion'] ?? '',
                $fila['fecha_modificacion'] ?? ''
            );
        }
        return $resultados;
    }

    public function listarDiezInscripcionesDetalladas() {
        $sql = "SELECT i.*, 
                       CONCAT(a.nombre, ' ', a.apellido) AS nombre_alumno,
                       CONCAT(c.año, '° ', c.division, ' - ', m.nombre_materia) AS nombre_curso
                FROM inscripcion i
                JOIN alumno a ON i.id_alumno = a.id_alumno
                JOIN curso c ON i.id_curso = c.id_curso
                JOIN materia m ON c.id_materia = m.id_materia
                ORDER BY i.fecha_inscripcion DESC
                LIMIT 10";
        $consulta = $this->conexion->query($sql);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }

    public function obtenerPorId(int $idInscripcion) {
        $sql = "SELECT * FROM inscripcion WHERE id_inscripcion = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$idInscripcion]);
        return $consulta->fetch(PDO::FETCH_ASSOC);
    }

    public function buscarCampo(string $terminoBusqueda) {
        $sql = "SELECT i.*, 
                       CONCAT(a.nombre, ' ', a.apellido) AS nombre_alumno,
                       CONCAT(c.año, '° ', c.division, ' - ', m.nombre_materia) AS nombre_curso
                FROM inscripcion i
                JOIN alumno a ON i.id_alumno = a.id_alumno
                JOIN curso c ON i.id_curso = c.id_curso
                JOIN materia m ON c.id_materia = m.id_materia
                WHERE a.nombre LIKE ? OR a.apellido LIKE ? OR a.dni LIKE ? OR m.nombre_materia LIKE ? OR i.estado LIKE ?
                LIMIT 10";
        $consulta = $this->conexion->prepare($sql);
        $parametroBusqueda = "%" . $terminoBusqueda . "%";
        $consulta->execute([$parametroBusqueda, $parametroBusqueda, $parametroBusqueda, $parametroBusqueda, $parametroBusqueda]);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }

    public function filtrarEstadoInscrip(string $estadoInscripcion) {
        if (empty($estadoInscripcion) || $estadoInscripcion === 'Todos') {
            return $this->listarDiezInscripcionesDetalladas();
        }
        $sql = "SELECT i.*, 
                       CONCAT(a.nombre, ' ', a.apellido) AS nombre_alumno,
                       CONCAT(c.año, '° ', c.division, ' - ', m.nombre_materia) AS nombre_curso
                FROM inscripcion i
                JOIN alumno a ON i.id_alumno = a.id_alumno
                JOIN curso c ON i.id_curso = c.id_curso
                JOIN materia m ON c.id_materia = m.id_materia
                WHERE i.estado = ?
                LIMIT 10";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$estadoInscripcion]);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }

    public function filtrarCurso(int $idCurso) {
        if ($idCurso <= 0) {
            return $this->listarDiezInscripcionesDetalladas();
        }
        $sql = "SELECT i.*, 
                       CONCAT(a.nombre, ' ', a.apellido) AS nombre_alumno,
                       CONCAT(c.año, '° ', c.division, ' - ', m.nombre_materia) AS nombre_curso
                FROM inscripcion i
                JOIN alumno a ON i.id_alumno = a.id_alumno
                JOIN curso c ON i.id_curso = c.id_curso
                JOIN materia m ON c.id_materia = m.id_materia
                WHERE c.id_curso = ?
                LIMIT 10";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$idCurso]);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }

    // Consultas para poblar los selectores en los formularios de inscripción
    public function listarAlumnosActivos() {
        $sql = "SELECT id_alumno, CONCAT(apellido, ', ', nombre) AS nombre_completo FROM alumno WHERE estado = 'Activo' ORDER BY apellido";
        $consulta = $this->conexion->query($sql);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }

    public function listarCursosActivos() {
        $sql = "SELECT c.id_curso, CONCAT(c.año, '° ', c.division, ' - ', m.nombre_materia) AS curso_nombre FROM curso c JOIN materia m ON c.id_materia = m.id_materia WHERE c.estado = 'Activo'";
        $consulta = $this->conexion->query($sql);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
<?php

require_once 'dao.php';

class InscripcionDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    //aca insertamos una nueva inscripcion en la base de datos
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

    //aca modificamos una inscripcion existente
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

    //aca damos de baja una inscripcion cambiando su estado a finalizada y guardando el motivo
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

    //aca listamos hasta 10 inscripciones detalladas para mostrar en la tabla principal
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

    //aca obtenemos una sola inscripcion por su id para editarla
    public function obtenerPorId(int $idInscripcion) {
        $sql = "SELECT * FROM inscripcion WHERE id_inscripcion = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$idInscripcion]);
        return $consulta->fetch(PDO::FETCH_ASSOC);
    }

    // --- FUNCIONES DE BÚSQUEDA Y FILTRADO ---

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

    // --- FUNCIONES DE VERIFICACIÓN ---

    public function hayCupo(int $idCurso): bool {
        $sqlInscritos = "SELECT COUNT(*) AS total_confirmadas FROM inscripcion WHERE id_curso = ? AND estado = 'Confirmada'";
        $consultaInscritos = $this->conexion->prepare($sqlInscritos);
        $consultaInscritos->execute([$idCurso]);
        $resultadoInscritos = $consultaInscritos->fetch(PDO::FETCH_ASSOC);
        $inscripcionesConfirmadas = (int)($resultadoInscritos['total_confirmadas'] ?? 0);

        $sqlCurso = "SELECT cupo_maximo FROM curso WHERE id_curso = ?";
        $consultaCurso = $this->conexion->prepare($sqlCurso);
        $consultaCurso->execute([$idCurso]);
        $datosCurso = $consultaCurso->fetch(PDO::FETCH_ASSOC);
        $cupoMaximo = (int)($datosCurso['cupo_maximo'] ?? 0);

        return $inscripcionesConfirmadas < $cupoMaximo;
    }

    public function yaInscripto(int $idAlumno, int $idCurso): bool {
        $sql = "SELECT COUNT(*) AS total FROM inscripcion WHERE id_alumno = ? AND id_curso = ? AND estado IN ('Confirmada', 'Pendiente')";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$idAlumno, $idCurso]);
        $resultado = $consulta->fetch(PDO::FETCH_ASSOC);
        return ((int)($resultado['total'] ?? 0)) > 0;
    }

    public function alumnoActivo(int $idAlumno): bool {
        $sql = "SELECT estado FROM alumno WHERE id_alumno = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$idAlumno]);
        $resultado = $consulta->fetch(PDO::FETCH_ASSOC);
        $estadoAlumno = $resultado['estado'] ?? '';
        return strcasecmp($estadoAlumno, 'Activo') === 0;
    }

    public function tieneMateriaAprobada(int $idAlumno, int $idMateria): bool {
        $sql = "SELECT COUNT(*) AS total_aprobadas 
                FROM resultado_materia rm
                JOIN inscripcion i ON rm.id_inscripcion = i.id_inscripcion
                JOIN curso c ON i.id_curso = c.id_curso
                WHERE i.id_alumno = ? AND c.id_materia = ? AND rm.resultado = 'Aprobada'";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$idAlumno, $idMateria]);
        $resultado = $consulta->fetch(PDO::FETCH_ASSOC);
        return ((int)($resultado['total_aprobadas'] ?? 0)) > 0;
    }

    public function puedeInscribirse(int $idAlumno, int $idCursoNuevo): bool {
        $sqlCursoNuevo = "SELECT turno, fecha_inicio, fecha_fin FROM curso WHERE id_curso = ?";
        $consultaNuevo = $this->conexion->prepare($sqlCursoNuevo);
        $consultaNuevo->execute([$idCursoNuevo]);
        $cursoNuevo = $consultaNuevo->fetch(PDO::FETCH_ASSOC);
        if (!$cursoNuevo) {
            return false;
        }

        $sqlCursosActuales = "SELECT c.turno, c.fecha_inicio, c.fecha_fin 
                              FROM inscripcion i
                              JOIN curso c ON i.id_curso = c.id_curso
                              WHERE i.id_alumno = ? AND i.estado = 'Confirmada'";
        $consultaActuales = $this->conexion->prepare($sqlCursosActuales);
        $consultaActuales->execute([$idAlumno]);
        $cursosActuales = $consultaActuales->fetchAll(PDO::FETCH_ASSOC);

        foreach ($cursosActuales as $cursoActual) {
            if ($cursoActual['turno'] === $cursoNuevo['turno']) {
                $inicioNuevo = strtotime($cursoNuevo['fecha_inicio']);
                $finNuevo = strtotime($cursoNuevo['fecha_fin']);
                $inicioActual = strtotime($cursoActual['fecha_inicio']);
                $finActual = strtotime($cursoActual['fecha_fin']);

                if ($inicioNuevo <= $finActual && $finNuevo >= $inicioActual) {
                    return false; 
                }
            }
        }
        return true;
    }
}
?>
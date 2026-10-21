<?php

require_once 'dao.php';

class MateriaDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($materia): void {
        $sql = "INSERT INTO materia (nombre_materia) VALUES (?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $materia->getNombre_materia()
        ]);
    }

    public function modificar($materia) {
        $sql = "UPDATE materia SET nombre_materia = ? WHERE id_materia = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $materia->getNombre_materia(),
            $materia->getId_materia()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM materia WHERE id_materia = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM materia";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Materia(
                $fila['id_materia'],
                $fila['nombre_materia']
            );
        }
        return $resultados;
    }

    public function listarMateriasDeUsuario(array $usuario, string $busqueda = ''): array
    {
        $cantidadCursos = 'SELECT COUNT(*) FROM curso c WHERE c.id_materia = m.id_materia';
        $conditions = [];
        $parameters = [];
        if ($usuario['isTeacher']) {
            $conditions[] = 'EXISTS (SELECT 1 FROM curso sc WHERE sc.id_materia = m.id_materia AND sc.id_docente = :scope)';
            $parameters['scope'] = (int) $usuario['id_docente'];
            $cantidadCursos .= ' AND c.id_docente = :course_teacher';
            $parameters['course_teacher'] = (int) $usuario['id_docente'];
        } elseif ($usuario['isStudent']) {
            $conditions[] = 'EXISTS (SELECT 1 FROM curso sc INNER JOIN inscripcion si ON si.id_curso = sc.id_curso WHERE sc.id_materia = m.id_materia AND si.id_alumno = :scope)';
            $parameters['scope'] = (int) $usuario['id_alumno'];
            $cantidadCursos .= ' AND EXISTS (SELECT 1 FROM inscripcion si WHERE si.id_curso = c.id_curso AND si.id_alumno = :course_student)';
            $parameters['course_student'] = (int) $usuario['id_alumno'];
        }
        if ($busqueda !== '') {
            $conditions[] = 'm.nombre_materia LIKE :search';
            $parameters['search'] = '%' . $busqueda . '%';
        }
        $sql = 'SELECT m.id_materia, m.nombre_materia AS materia, (' . $cantidadCursos . ') AS cursos FROM materia m';
        if ($conditions !== []) {
            $sql .= ' WHERE ' . implode(' AND ', $conditions);
        }
        $sql .= ' ORDER BY m.nombre_materia LIMIT 250';
        $query = $this->conexion->prepare($sql);
        $query->execute($parameters);
        return $query->fetchAll(PDO::FETCH_ASSOC);
    }

    public function obtenerMateria(int $id): ?array
    {
        $query = $this->conexion->prepare('SELECT id_materia, nombre_materia FROM materia WHERE id_materia = ?');
        $query->execute([$id]);
        $row = $query->fetch(PDO::FETCH_ASSOC);
        return $row ?: null;
    }

    public function guardarMateria(int $id, string $nombre): void
    {
        $nombre = trim($nombre);
        if ($nombre === '' || preg_match_all('/./us', $nombre) > 200) {
            throw new InvalidArgumentException('Ingresá un nombre de materia válido (máximo 200 caracteres).');
        }
        $duplicado = $this->conexion->prepare(
            'SELECT COUNT(*) FROM materia WHERE LOWER(nombre_materia) = LOWER(?) AND id_materia <> ?'
        );
        $duplicado->execute([$nombre, $id]);
        if ((int) $duplicado->fetchColumn() > 0) {
            throw new InvalidArgumentException('Ya existe una materia con ese nombre.');
        }
        if ($id > 0) {
            $query = $this->conexion->prepare('UPDATE materia SET nombre_materia = ? WHERE id_materia = ?');
            $query->execute([$nombre, $id]);
            return;
        }
        $query = $this->conexion->prepare('INSERT INTO materia (nombre_materia) VALUES (?)');
        $query->execute([$nombre]);
    }

    public function eliminarMateria(int $id): void
    {
        $cursos = $this->conexion->prepare('SELECT COUNT(*) FROM curso WHERE id_materia = ?');
        $cursos->execute([$id]);
        if ((int) $cursos->fetchColumn() > 0) {
            throw new InvalidArgumentException('No se puede eliminar la materia porque está relacionada con cursos.');
        }
        $query = $this->conexion->prepare('DELETE FROM materia WHERE id_materia = ?');
        $query->execute([$id]);
        if ($query->rowCount() === 0) {
            throw new InvalidArgumentException('La materia ya no existe.');
        }
    }
}
?>
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
                EstadoAlumno::tryFrom(strtolower(trim((string) $fila['estado'])))
                    ?? throw new UnexpectedValueException('Estado de alumno no reconocido.')
            );
        }
        return $resultados;
    }

    public function listarAdmin(): array
    {
        return $this->conexion->query(
            'SELECT a.id_alumno, a.id_usuario, a.id_responsable, a.nombre, a.apellido, a.dni,
                a.matricula, a.estado, u.nombre_usuario, r.nombre AS responsable_nombre,
                r.apellido AS responsable_apellido, r.dni AS responsable_dni, r.tipo_relacion
            FROM alumno a INNER JOIN usuario u ON u.id_usuario = a.id_usuario
            INNER JOIN responsable r ON r.id_responsable = a.id_responsable
            ORDER BY a.apellido, a.nombre'
        )->fetchAll(PDO::FETCH_ASSOC);
    }

    public function crearAdmin(array $datos): void
    {
        $this->validarAdmin($datos, true);
        $bloqueo = $this->conexion->query("SELECT GET_LOCK('academia_matricula', 10)");
        if ((int) $bloqueo->fetchColumn() !== 1) {
            throw new RuntimeException('No se pudo reservar una matrícula. Intentá nuevamente.');
        }
        try {
            $this->conexion->beginTransaction();
            try {
                $usuario = $this->conexion->prepare('INSERT INTO usuario (id_rol, nombre_usuario, contraseña) VALUES (4, ?, ?)');
                $usuario->execute([$datos['nombre_usuario'], password_hash($datos['contrasena'], PASSWORD_DEFAULT)]);
                $idUsuario = (int) $this->conexion->lastInsertId();
                $responsable = $this->conexion->prepare(
                    'INSERT INTO responsable (nombre, apellido, dni, tipo_relacion) VALUES (?, ?, ?, ?)'
                );
                $responsable->execute([$datos['resp_nombre'], $datos['resp_apellido'], $datos['resp_dni'], $datos['resp_relacion']]);
                $idResponsable = (int) $this->conexion->lastInsertId();
                $prefix = 'AIXX-' . date('Y') . '-';
                $next = $this->conexion->prepare(
                    "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(matricula, '-', -1) AS UNSIGNED)), 0) FROM alumno WHERE matricula LIKE ?"
                );
                $next->execute([$prefix . '%']);
                $matricula = $prefix . str_pad((string) ((int) $next->fetchColumn() + 1), 4, '0', STR_PAD_LEFT);
                $insert = $this->conexion->prepare(
                    "INSERT INTO alumno (id_usuario, id_responsable, nombre, apellido, dni, matricula, estado) VALUES (?, ?, ?, ?, ?, ?, 'Activo')"
                );
                $insert->execute([$idUsuario, $idResponsable, $datos['nombre'], $datos['apellido'], $datos['dni'], $matricula]);
                $this->conexion->commit();
            } catch (Throwable $exception) {
                if ($this->conexion->inTransaction()) {
                    $this->conexion->rollBack();
                }
                throw $exception;
            }
        } finally {
            $this->conexion->query("SELECT RELEASE_LOCK('academia_matricula')");
        }
    }

    public function actualizarAdmin(int $id, array $datos): void
    {
        $this->validarAdmin($datos, false);
        $this->conexion->beginTransaction();
        try {
            $query = $this->conexion->prepare('SELECT id_usuario, id_responsable FROM alumno WHERE id_alumno = ?');
            $query->execute([$id]);
            $actual = $query->fetch(PDO::FETCH_ASSOC);
            if (!$actual) {
                throw new InvalidArgumentException('El alumno ya no existe.');
            }
            $this->actualizarCuentaAdmin((int) $actual['id_usuario'], $datos);
            $resp = $this->conexion->prepare(
                'UPDATE responsable SET nombre=?, apellido=?, dni=?, tipo_relacion=? WHERE id_responsable=?'
            );
            $resp->execute([$datos['resp_nombre'], $datos['resp_apellido'], $datos['resp_dni'], $datos['resp_relacion'], $actual['id_responsable']]);
            $alumno = $this->conexion->prepare('UPDATE alumno SET nombre=?, apellido=?, dni=? WHERE id_alumno=?');
            $alumno->execute([$datos['nombre'], $datos['apellido'], $datos['dni'], $id]);
            $this->conexion->commit();
        } catch (Throwable $exception) {
            if ($this->conexion->inTransaction()) {
                $this->conexion->rollBack();
            }
            throw $exception;
        }
    }

    public function darDeBajaAdmin(int $id): void
    {
        $query = $this->conexion->prepare("UPDATE alumno SET estado = 'Inactivo' WHERE id_alumno = ?");
        $query->execute([$id]);
        if ($query->rowCount() === 0) {
            $exists = $this->conexion->prepare('SELECT COUNT(*) FROM alumno WHERE id_alumno = ?');
            $exists->execute([$id]);
            if ((int) $exists->fetchColumn() === 0) {
                throw new InvalidArgumentException('El alumno ya no existe.');
            }
        }
    }

    private function validarAdmin(array $datos, bool $crear): void
    {
        foreach (['nombre', 'apellido', 'dni', 'nombre_usuario', 'resp_nombre', 'resp_apellido', 'resp_dni', 'resp_relacion'] as $field) {
            if (trim((string) ($datos[$field] ?? '')) === '') {
                throw new InvalidArgumentException('Completá todos los datos obligatorios del alumno y su responsable.');
            }
        }
        if (!ctype_digit((string) $datos['dni']) || !ctype_digit((string) $datos['resp_dni'])) {
            throw new InvalidArgumentException('Los DNI deben contener solo números.');
        }
        if (strlen((string) $datos['nombre_usuario']) > 200 || strlen((string) $datos['nombre']) > 255
            || strlen((string) $datos['apellido']) > 255 || strlen((string) $datos['resp_nombre']) > 255
            || strlen((string) $datos['resp_apellido']) > 255 || strlen((string) $datos['resp_relacion']) > 100) {
            throw new InvalidArgumentException('Uno de los campos supera la longitud permitida.');
        }
        if ($crear && (strlen((string) ($datos['contrasena'] ?? '')) < 8 || strlen((string) $datos['contrasena']) > 200)) {
            throw new InvalidArgumentException('La contraseña debe tener entre 8 y 200 caracteres.');
        }
    }

    private function actualizarCuentaAdmin(int $idUsuario, array $datos): void
    {
        $check = $this->conexion->prepare('SELECT COUNT(*) FROM usuario WHERE LOWER(nombre_usuario)=LOWER(?) AND id_usuario<>?');
        $check->execute([$datos['nombre_usuario'], $idUsuario]);
        if ((int) $check->fetchColumn() > 0) {
            throw new InvalidArgumentException('Ese nombre de usuario ya está en uso.');
        }
        $password = (string) ($datos['contrasena'] ?? '');
        if ($password !== '' && (strlen($password) < 8 || strlen($password) > 200)) {
            throw new InvalidArgumentException('La contraseña debe tener entre 8 y 200 caracteres.');
        }
        if ($password === '') {
            $query = $this->conexion->prepare('UPDATE usuario SET nombre_usuario=? WHERE id_usuario=?');
            $query->execute([$datos['nombre_usuario'], $idUsuario]);
        } else {
            $query = $this->conexion->prepare('UPDATE usuario SET nombre_usuario=?, contraseña=? WHERE id_usuario=?');
            $query->execute([$datos['nombre_usuario'], password_hash($password, PASSWORD_DEFAULT), $idUsuario]);
        }
    }
}
?>
<?php

require_once 'dao.php';

class DocenteDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($docente): void {
        $sql = "INSERT INTO docente (id_usuario, nombre, apellido, dni, legajo, estado) VALUES (?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $docente->getId_usuario(),
            $docente->getNombre(),
            $docente->getApellido(),
            $docente->getDni(),
            $docente->getLegajo(),
            $docente->getEstado()->value
        ]);
    }

    public function modificar($docente) {
        $sql = "UPDATE docente SET id_usuario = ?, nombre = ?, apellido = ?, dni = ?, legajo = ?, estado = ? WHERE id_docente = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $docente->getId_usuario(),
            $docente->getNombre(),
            $docente->getApellido(),
            $docente->getDni(),
            $docente->getLegajo(),
            $docente->getEstado()->value,
            $docente->getId_docente()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM docente WHERE id_docente = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM docente";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new Docente(
                $fila['id_docente'],
                $fila['id_usuario'],
                $fila['nombre'],
                $fila['apellido'],
                $fila['dni'],
                $fila['legajo'],
                EstadoDocente::from($fila['estado'])
            );
        }
        return $resultados;
    }

    public function listarAdmin(): array
    {
        return $this->conexion->query(
            'SELECT d.id_docente, d.id_usuario, d.nombre, d.apellido, d.dni, d.legajo, d.estado, u.nombre_usuario
            FROM docente d INNER JOIN usuario u ON u.id_usuario = d.id_usuario
            ORDER BY d.apellido, d.nombre'
        )->fetchAll(PDO::FETCH_ASSOC);
    }

    public function crearAdmin(array $datos): void
    {
        $this->validarAdmin($datos, true);
        $lock = $this->conexion->query("SELECT GET_LOCK('academia_legajo', 10)");
        if ((int) $lock->fetchColumn() !== 1) {
            throw new RuntimeException('No se pudo reservar un legajo. Intentá nuevamente.');
        }
        try {
            $this->conexion->beginTransaction();
            try {
                $usuario = $this->conexion->prepare('INSERT INTO usuario (id_rol, nombre_usuario, contraseña) VALUES (3, ?, ?)');
                $usuario->execute([$datos['nombre_usuario'], password_hash($datos['contrasena'], PASSWORD_DEFAULT)]);
                $idUsuario = (int) $this->conexion->lastInsertId();
                $next = $this->conexion->query(
                    "SELECT COALESCE(MAX(CAST(SUBSTRING(legajo, 5) AS UNSIGNED)), 0) FROM docente WHERE legajo LIKE 'DOC-%'"
                );
                $legajo = 'DOC-' . str_pad((string) ((int) $next->fetchColumn() + 1), 4, '0', STR_PAD_LEFT);
                $insert = $this->conexion->prepare(
                    "INSERT INTO docente (id_usuario, nombre, apellido, dni, legajo, estado) VALUES (?, ?, ?, ?, ?, 'Activo')"
                );
                $insert->execute([$idUsuario, $datos['nombre'], $datos['apellido'], $datos['dni'], $legajo]);
                $this->conexion->commit();
            } catch (Throwable $exception) {
                if ($this->conexion->inTransaction()) {
                    $this->conexion->rollBack();
                }
                throw $exception;
            }
        } finally {
            $this->conexion->query("SELECT RELEASE_LOCK('academia_legajo')");
        }
    }

    public function actualizarAdmin(int $id, array $datos): void
    {
        $this->validarAdmin($datos, false);
        $this->conexion->beginTransaction();
        try {
            $query = $this->conexion->prepare('SELECT id_usuario FROM docente WHERE id_docente = ?');
            $query->execute([$id]);
            $idUsuario = $query->fetchColumn();
            if ($idUsuario === false) {
                throw new InvalidArgumentException('El docente ya no existe.');
            }
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
                $account = $this->conexion->prepare('UPDATE usuario SET nombre_usuario=? WHERE id_usuario=?');
                $account->execute([$datos['nombre_usuario'], $idUsuario]);
            } else {
                $account = $this->conexion->prepare('UPDATE usuario SET nombre_usuario=?, contraseña=? WHERE id_usuario=?');
                $account->execute([$datos['nombre_usuario'], password_hash($password, PASSWORD_DEFAULT), $idUsuario]);
            }
            $docente = $this->conexion->prepare('UPDATE docente SET nombre=?, apellido=?, dni=? WHERE id_docente=?');
            $docente->execute([$datos['nombre'], $datos['apellido'], $datos['dni'], $id]);
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
        $query = $this->conexion->prepare("UPDATE docente SET estado='Inactivo' WHERE id_docente=?");
        $query->execute([$id]);
        if ($query->rowCount() === 0) {
            $exists = $this->conexion->prepare('SELECT COUNT(*) FROM docente WHERE id_docente=?');
            $exists->execute([$id]);
            if ((int) $exists->fetchColumn() === 0) {
                throw new InvalidArgumentException('El docente ya no existe.');
            }
        }
    }

    private function validarAdmin(array $datos, bool $crear): void
    {
        foreach (['nombre', 'apellido', 'dni', 'nombre_usuario'] as $field) {
            if (trim((string) ($datos[$field] ?? '')) === '') {
                throw new InvalidArgumentException('Completá todos los datos obligatorios del docente.');
            }
        }
        if (strlen((string) $datos['nombre']) > 200 || strlen((string) $datos['apellido']) > 200
            || strlen((string) $datos['dni']) > 20 || strlen((string) $datos['nombre_usuario']) > 200) {
            throw new InvalidArgumentException('Uno de los campos supera la longitud permitida.');
        }
        if ($crear && (strlen((string) ($datos['contrasena'] ?? '')) < 8 || strlen((string) $datos['contrasena']) > 200)) {
            throw new InvalidArgumentException('La contraseña debe tener entre 8 y 200 caracteres.');
        }
    }
}
?>
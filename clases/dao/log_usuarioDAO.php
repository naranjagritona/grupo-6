<?php

require_once 'dao.php';

class LogUsuarioDAO implements dao {
    private $conexion;

    public function __construct($conexion) {
        $this->conexion = $conexion;
    }

    public function insertar($log): void {
        $sql = "INSERT INTO log_usuario (id_usuario, accion, campo_modificado, valor_anterior, valor_nuevo, fecha_hora) VALUES (?, ?, ?, ?, ?, ?)";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([
            $log->getId_usuario(),
            $log->getAccion(),
            $log->getCampo_modificado(),
            $log->getValor_anterior(),
            $log->getValor_nuevo(),
            $log->getFecha_hora()
        ]);
    }

    public function modificar($log) {
        $sql = "UPDATE log_usuario SET id_usuario = ?, accion = ?, campo_modificado = ?, valor_anterior = ?, valor_nuevo = ?, fecha_hora = ? WHERE id_log_usuario = ?";
        $consulta = $this->conexion->prepare($sql);
        return $consulta->execute([
            $log->getId_usuario(),
            $log->getAccion(),
            $log->getCampo_modificado(),
            $log->getValor_anterior(),
            $log->getValor_nuevo(),
            $log->getFecha_hora(),
            $log->getId_log_usuario()
        ]);
    }

    public function eliminar($id): void {
        $sql = "DELETE FROM log_usuario WHERE id_log_usuario = ?";
        $consulta = $this->conexion->prepare($sql);
        $consulta->execute([$id]);
    }

    public function listarTodos() {
        $sql = "SELECT * FROM log_usuario";
        $consulta = $this->conexion->query($sql);
        $resultados = [];
        while ($fila = $consulta->fetch(PDO::FETCH_ASSOC)) {
            $resultados[] = new LogUsuario(
                $fila['id_log_usuario'],
                $fila['id_usuario'],
                $fila['accion'],
                $fila['campo_modificado'],
                $fila['valor_anterior'],
                $fila['valor_nuevo'],
                $fila['fecha_hora']
            );
        }
        return $resultados;
    }

    public function listarLogConUsuarios(int $limite = 25): array {
        $sql = "SELECT l.*, u.nombre_usuario, r.nombre AS rol_nombre
                FROM log_usuario AS l
                LEFT JOIN usuario AS u ON u.id_usuario = l.id_usuario
                LEFT JOIN rol AS r ON r.id_rol = u.id_rol
                ORDER BY l.fecha_hora DESC, l.id_log_usuario DESC
                LIMIT " . (int) $limite;
        $consulta = $this->conexion->query($sql);
        return $consulta->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
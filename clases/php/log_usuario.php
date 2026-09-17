<?php

class LogUsuario {

    public function __construct(
        private int $id_log_usuario,
        private int $id_usuario,
        private string $accion,
        private string $campo_modificado,
        private string $valor_anterior,
        private string $valor_nuevo,
        private string $fecha_hora
    ) {
    }

    public function getId_log_usuario(): int {
        return $this->id_log_usuario;
    }

    public function setId_log_usuario(int $id_log_usuario): void {
        $this->id_log_usuario = $id_log_usuario;
    }

    public function getId_usuario(): int {
        return $this->id_usuario;
    }

    public function setId_usuario(int $id_usuario): void {
        $this->id_usuario = $id_usuario;
    }

    public function getAccion(): string {
        return $this->accion;
    }

    public function setAccion(string $accion): void {
        $this->accion = $accion;
    }

    public function getCampo_modificado(): string {
        return $this->campo_modificado;
    }

    public function setCampo_modificado(string $campo_modificado): void {
        $this->campo_modificado = $campo_modificado;
    }

    public function getValor_anterior(): string {
        return $this->valor_anterior;
    }

    public function setValor_anterior(string $valor_anterior): void {
        $this->valor_anterior = $valor_anterior;
    }

    public function getValor_nuevo(): string {
        return $this->valor_nuevo;
    }

    public function setValor_nuevo(string $valor_nuevo): void {
        $this->valor_nuevo = $valor_nuevo;
    }

    public function getFecha_hora(): string {
        return $this->fecha_hora;
    }

    public function setFecha_hora(string $fecha_hora): void {
        $this->fecha_hora = $fecha_hora;
    }
}
?>
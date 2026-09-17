<?php

class CalificacionHistorial {

    public function __construct(
        private int $id_historial,
        private int $id_calificacion,
        private float $nota_anterior,
        private float $nota_nueva,
        private string $fecha_modificacion,
        private string $motivo
    ) {
    }

    public function getId_historial(): int {
        return $this->id_historial;
    }

    public function setId_historial(int $id_historial): void {
        $this->id_historial = $id_historial;
    }

    public function getId_calificacion(): int {
        return $this->id_calificacion;
    }

    public function setId_calificacion(int $id_calificacion): void {
        $this->id_calificacion = $id_calificacion;
    }

    public function getNota_anterior(): float {
        return $this->nota_anterior;
    }

    public function setNota_anterior(float $nota_anterior): void {
        $this->nota_anterior = $nota_anterior;
    }

    public function getNota_nueva(): float {
        return $this->nota_nueva;
    }

    public function setNota_nueva(float $nota_nueva): void {
        $this->nota_nueva = $nota_nueva;
    }

    public function getFecha_modificacion(): string {
        return $this->fecha_modificacion;
    }

    public function setFecha_modificacion(string $fecha_modificacion): void {
        $this->fecha_modificacion = $fecha_modificacion;
    }

    public function getMotivo(): string {
        return $this->motivo;
    }

    public function setMotivo(string $motivo): void {
        $this->motivo = $motivo;
    }
}
?>
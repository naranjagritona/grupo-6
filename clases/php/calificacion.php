<?php

class Calificacion {

    public function __construct(
        private int $id_calificacion,
        private int $id_inscripcion,
        private int $id_columna_notas,
        private float $nota,
        private string $fecha_carga,
        private string $observaciones
    ) {
    }

    public function getId_calificacion(): int {
        return $this->id_calificacion;
    }

    public function setId_calificacion(int $id_calificacion): void {
        $this->id_calificacion = $id_calificacion;
    }

    public function getId_inscripcion(): int {
        return $this->id_inscripcion;
    }

    public function setId_inscripcion(int $id_inscripcion): void {
        $this->id_inscripcion = $id_inscripcion;
    }

    public function getId_columna_notas(): int {
        return $this->id_columna_notas;
    }

    public function setId_columna_notas(int $id_columna_notas): void {
        $this->id_columna_notas = $id_columna_notas;
    }

    public function getNota(): float {
        return $this->nota;
    }

    public function setNota(float $nota): void {
        $this->nota = $nota;
    }

    public function getFecha_carga(): string {
        return $this->fecha_carga;
    }

    public function setFecha_carga(string $fecha_carga): void {
        $this->fecha_carga = $fecha_carga;
    }

    public function getObservaciones(): string {
        return $this->observaciones;
    }

    public function setObservaciones(string $observaciones): void {
        $this->observaciones = $observaciones;
    }
}
?>
<?php

enum TipoColumnaNotas: string {
    case EVALUACION = 'Evaluación';
    case TRABAJO_PRACTICO = 'Trabajo Práctico';
    case RECUPERATORIO = 'Recuperatorio';
    case OTRO = 'Otro';
}


enum EstadoColumnaNotas: string {
    case ABIERTA = 'Abierta';
    case CERRADA = 'Cerrada';
}


class ColumnaNotas {

    public function __construct(
        private int $id_columna_notas,
        private int $id_curso,
        private string $titulo,
        private TipoColumnaNotas $tipo,
        private float $nota_maxima,
        private float $nota_aprobacion,
        private string $fecha_evaluacion,
        private string $fecha_cierre,
        private EstadoColumnaNotas $estado
    ) {
    }

    public function getId_columna_notas(): int {
        return $this->id_columna_notas;
    }

    public function setId_columna_notas(int $id_columna_notas): void {
        $this->id_columna_notas = $id_columna_notas;
    }

    public function getId_curso(): int {
        return $this->id_curso;
    }

    public function setId_curso(int $id_curso): void {
        $this->id_curso = $id_curso;
    }

    public function getTitulo(): string {
        return $this->titulo;
    }

    public function setTitulo(string $titulo): void {
        $this->titulo = $titulo;
    }

    public function getTipo(): TipoColumnaNotas {
        return $this->tipo;
    }

    public function setTipo(TipoColumnaNotas $tipo): void {
        $this->tipo = $tipo;
    }

    public function getNota_maxima(): float {
        return $this->nota_maxima;
    }

    public function setNota_maxima(float $nota_maxima): void {
        $this->nota_maxima = $nota_maxima;
    }

    public function getNota_aprobacion(): float {
        return $this->nota_aprobacion;
    }

    public function setNota_aprobacion(float $nota_aprobacion): void {
        $this->nota_aprobacion = $nota_aprobacion;
    }

    public function getFecha_evaluacion(): string {
        return $this->fecha_evaluacion;
    }

    public function setFecha_evaluacion(string $fecha_evaluacion): void {
        $this->fecha_evaluacion = $fecha_evaluacion;
    }

    public function getFecha_cierre(): string {
        return $this->fecha_cierre;
    }

    public function setFecha_cierre(string $fecha_cierre): void {
        $this->fecha_cierre = $fecha_cierre;
    }

    public function getEstado(): EstadoColumnaNotas {
        return $this->estado;
    }

    public function setEstado(EstadoColumnaNotas $estado): void {
        $this->estado = $estado;
    }
}
?>
<?php

enum TipoPeriodoLectivo: string {
    case ANUAL = 'Anual';
    case CUATRIMESTRAL = 'Cuatrimestral';
    case OTRO = 'Otro';
}

enum EstadoPeriodoLectivo: string {
    case PLANIFICADO = 'Planificado';
    case ACTIVO = 'Activo';
    case CERRADO = 'Cerrado';
}

class PeriodoLectivo {

    public function __construct(
        private int $id_periodo,
        private string $nombre,
        private TipoPeriodoLectivo $tipo,
        private string $fecha_inicio,
        private string $fecha_fin,
        private EstadoPeriodoLectivo $estado
    ) {
    }

    public function getId_periodo(): int {
        return $this->id_periodo;
    }

    public function setId_periodo(int $id_periodo): void {
        $this->id_periodo = $id_periodo;
    }

    public function getNombre(): string {
        return $this->nombre;
    }

    public function setNombre(string $nombre): void {
        $this->nombre = $nombre;
    }

    public function getTipo(): TipoPeriodoLectivo {
        return $this->tipo;
    }

    public function setTipo(TipoPeriodoLectivo $tipo): void {
        $this->tipo = $tipo;
    }

    public function getFecha_inicio(): string {
        return $this->fecha_inicio;
    }

    public function setFecha_inicio(string $fecha_inicio): void {
        $this->fecha_inicio = $fecha_inicio;
    }

    public function getFecha_fin(): string {
        return $this->fecha_fin;
    }

    public function setFecha_fin(string $fecha_fin): void {
        $this->fecha_fin = $fecha_fin;
    }

    public function getEstado(): EstadoPeriodoLectivo {
        return $this->estado;
    }

    public function setEstado(EstadoPeriodoLectivo $estado): void {
        $this->estado = $estado;
    }
}
?>
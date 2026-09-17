<?php

enum EstadoInscripcion: string {
    case PENDIENTE = 'Pendiente';
    case CONFIRMADA = 'Confirmada';
    case CANCELADA = 'Cancelada';
    case FINALIZADA = 'Finalizada';
}

class Inscripcion {

    public function __construct(
        private int $id_inscripcion,
        private int $id_alumno,
        private int $id_curso,
        private string $fecha_inscripcion,
        private EstadoInscripcion $estado,
        private string $motivo_baja,
        private float $importe_cuota,
        private string $fecha_creacion,
        private string $fecha_modificacion
    ) {
    }

    public function getId_inscripcion(): int {
        return $this->id_inscripcion;
    }

    public function setId_inscripcion(int $id_inscripcion): void {
        $this->id_inscripcion = $id_inscripcion;
    }

    public function getId_alumno(): int {
        return $this->id_alumno;
    }

    public function setId_alumno(int $id_alumno): void {
        $this->id_alumno = $id_alumno;
    }

    public function getId_curso(): int {
        return $this->id_curso;
    }

    public function setId_curso(int $id_curso): void {
        $this->id_curso = $id_curso;
    }

    public function getFecha_inscripcion(): string {
        return $this->fecha_inscripcion;
    }

    public function setFecha_inscripcion(string $fecha_inscripcion): void {
        $this->fecha_inscripcion = $fecha_inscripcion;
    }

    public function getEstado(): EstadoInscripcion {
        return $this->estado;
    }

    public function setEstado(EstadoInscripcion $estado): void {
        $this->estado = $estado;
    }

    public function getMotivo_baja(): string {
        return $this->motivo_baja;
    }

    public function setMotivo_baja(string $motivo_baja): void {
        $this->motivo_baja = $motivo_baja;
    }

    public function getImporte_cuota(): float {
        return $this->importe_cuota;
    }

    public function setImporte_cuota(float $importe_cuota): void {
        $this->importe_cuota = $importe_cuota;
    }

    public function getFecha_creacion(): string {
        return $this->fecha_creacion;
    }

    public function setFecha_creacion(string $fecha_creacion): void {
        $this->fecha_creacion = $fecha_creacion;
    }

    public function getFecha_modificacion(): string {
        return $this->fecha_modificacion;
    }

    public function setFecha_modificacion(string $fecha_modificacion): void {
        $this->fecha_modificacion = $fecha_modificacion;
    }
}
?>
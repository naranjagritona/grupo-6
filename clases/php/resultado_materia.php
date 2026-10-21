<?php

enum EstadoResultadoMateria: string {
    case EN_CURSO = 'En curso';
    case APROBADA = 'Aprobada';
    case DESAPROBADA = 'Desaprobada';
    case PENDIENTE = 'Pendiente';
}

class ResultadoMateria {

    public function __construct(
        private int $id_resultado,
        private int $id_inscripcion,
        private float $promedio,
        private EstadoResultadoMateria $resultado,
        private string $fecha_calculo
    ) {
    }

    public function getId_resultado(): int {
        return $this->id_resultado;
    }

    public function setId_resultado(int $id_resultado): void {
        $this->id_resultado = $id_resultado;
    }

    public function getId_inscripcion(): int {
        return $this->id_inscripcion;
    }

    public function setId_inscripcion(int $id_inscripcion): void {
        $this->id_inscripcion = $id_inscripcion;
    }

    public function getPromedio(): float {
        return $this->promedio;
    }

    public function setPromedio(float $promedio): void {
        $this->promedio = $promedio;
    }

    public function getResultado(): EstadoResultadoMateria {
        return $this->resultado;
    }

    public function setResultado(EstadoResultadoMateria $resultado): void {
        $this->resultado = $resultado;
    }

    public function getFecha_calculo(): string {
        return $this->fecha_calculo;
    }

    public function setFecha_calculo(string $fecha_calculo): void {
        $this->fecha_calculo = $fecha_calculo;
    }
}

?>
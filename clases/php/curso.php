<?php

enum ModalidadCurso: string {
    case PRESENCIAL = 'Presencial';
    case VIRTUAL = 'Virtual';
    case HIBRIDO = 'Híbrido';
}

enum TurnoCurso: string {
    case MANANA = 'mañana';
    case TARDE = 'tarde';
    case VESPERTINO = 'vespertino';
}

enum EstadoCurso: string {
    case ACTIVO = 'Activo';
    case FINALIZADO = 'Finalizado';
    case CANCELADO = 'Cancelado';
}


class Curso {

    public function __construct(
        private int $id_curso,
        private int $id_periodo,
        private int $id_materia,
        private int $id_docente,
        private string $año,
        private string $division,
        private ModalidadCurso $modalidad,
        private TurnoCurso $turno,
        private int $cupo_maximo,
        private string $fecha_inicio,
        private string $fecha_fin,
        private EstadoCurso $estado
    ) {
    }

    public function getId_curso(): int {
        return $this->id_curso;
    }

    public function setId_curso(int $id_curso): void {
        $this->id_curso = $id_curso;
    }

    public function getId_periodo(): int {
        return $this->id_periodo;
    }

    public function setId_periodo(int $id_periodo): void {
        $this->id_periodo = $id_periodo;
    }

    public function getId_materia(): int {
        return $this->id_materia;
    }

    public function setId_materia(int $id_materia): void {
        $this->id_materia = $id_materia;
    }

    public function getId_docente(): int {
        return $this->id_docente;
    }

    public function setId_docente(int $id_docente): void {
        $this->id_docente = $id_docente;
    }

    public function getAño(): string {
        return $this->año;
    }

    public function setAño(string $año): void {
        $this->año = $año;
    }

    public function getDivision(): string {
        return $this->division;
    }

    public function setDivision(string $division): void {
        $this->division = $division;
    }

    public function getModalidad(): ModalidadCurso {
        return $this->modalidad;
    }

    public function setModalidad(ModalidadCurso $modalidad): void {
        $this->modalidad = $modalidad;
    }

    public function getTurno(): TurnoCurso {
        return $this->turno;
    }

    public function setTurno(TurnoCurso $turno): void {
        $this->turno = $turno;
    }

    public function getCupo_maximo(): int {
        return $this->cupo_maximo;
    }

    public function setCupo_maximo(int $cupo_maximo): void {
        $this->cupo_maximo = $cupo_maximo;
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

    public function getEstado(): EstadoCurso {
        return $this->estado;
    }

    public function setEstado(EstadoCurso $estado): void {
        $this->estado = $estado;
    }
}
?>
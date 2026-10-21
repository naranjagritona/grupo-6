<?php

//definimos un enum para el estado del alumno
enum EstadoAlumno: string {
    case ACTIVO = 'activo';
    case INACTIVO = 'inactivo';
    case EGRESADO = 'egresado';
}

class Alumno {

    public function __construct(
        private int $id_alumno,
        private int $id_usuario,
        private int $id_responsable,
        private string $nombre,
        private string $apellido,
        private string $dni,
        private string $matricula,
        private EstadoAlumno $estado
    ) {
    }


//Gets y setters
    public function getId_alumno(): int {
        return $this->id_alumno;
    }

    public function setId_alumno(int $id_alumno): void {
        $this->id_alumno = $id_alumno;
    }

    public function getId_usuario(): int {
        return $this->id_usuario;
    }

    public function setId_usuario(int $id_usuario): void {
        $this->id_usuario = $id_usuario;
    }

    public function getId_responsable(): int {
        return $this->id_responsable;
    }

    public function setId_responsable(int $id_responsable): void {
        $this->id_responsable = $id_responsable;
    }

    public function getNombre(): string {
        return $this->nombre;
    }

    public function setNombre(string $nombre): void {
        $this->nombre = $nombre;
    }

    public function getApellido(): string {
        return $this->apellido;
    }

    public function setApellido(string $apellido): void {
        $this->apellido = $apellido;
    }

    public function getDni(): string {
        return $this->dni;
    }

    public function setDni(string $dni): void {
        $this->dni = $dni;
    }

    public function getMatricula(): string {
        return $this->matricula;
    }

    public function setMatricula(string $matricula): void {
        $this->matricula = $matricula;
    }

    public function getEstado(): EstadoAlumno {
        return $this->estado;
    }

    public function setEstado(EstadoAlumno $estado): void {
        $this->estado = $estado;
    }
}

?>
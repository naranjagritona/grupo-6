<?php

enum EstadoDocente: string {
    case ACTIVO = 'Activo';
    case INACTIVO = 'Inactivo';
}

class Docente {

    public function __construct(
        private int $id_docente,
        private int $id_usuario,
        private string $nombre,
        private string $apellido,
        private string $dni,
        private string $legajo,
        private EstadoDocente $estado
    ) {
    }

    public function getId_docente(): int {
        return $this->id_docente;
    }

    public function setId_docente(int $id_docente): void {
        $this->id_docente = $id_docente;
    }

    public function getId_usuario(): int {
        return $this->id_usuario;
    }

    public function setId_usuario(int $id_usuario): void {
        $this->id_usuario = $id_usuario;
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

    public function getLegajo(): string {
        return $this->legajo;
    }

    public function setLegajo(string $legajo): void {
        $this->legajo = $legajo;
    }

    public function getEstado(): EstadoDocente {
        return $this->estado;
    }

    public function setEstado(EstadoDocente $estado): void {
        $this->estado = $estado;
    }
}
?>
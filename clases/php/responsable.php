<?php

class Responsable {

    public function __construct(
        private int $id_responsable,
        private string $nombre,
        private string $apellido,
        private int $dni,
        private string $tipo_relacion
    ) {
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

    public function getDni(): int {
        return $this->dni;
    }

    public function setDni(int $dni): void {
        $this->dni = $dni;
    }

    public function getTipo_relacion(): string {
        return $this->tipo_relacion;
    }

    public function setTipo_relacion(string $tipo_relacion): void {
        $this->tipo_relacion = $tipo_relacion;
    }
}
?>
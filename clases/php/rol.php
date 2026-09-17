<?php

class Rol {

    public function __construct(
        private int $id_rol,
        private string $nombre,
        private string $descripcion_rol
    ) {
    }

    public function getId_rol(): int {
        return $this->id_rol;
    }

    public function setId_rol(int $id_rol): void {
        $this->id_rol = $id_rol;
    }

    public function getNombre(): string {
        return $this->nombre;
    }

    public function setNombre(string $nombre): void {
        $this->nombre = $nombre;
    }

    public function getDescripcion_rol(): string {
        return $this->descripcion_rol;
    }

    public function setDescripcion_rol(string $descripcion_rol): void {
        $this->descripcion_rol = $descripcion_rol;
    }
}
?>
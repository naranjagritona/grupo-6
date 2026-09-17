<?php

class Usuario {

    public function __construct(
        private int $id_usuario,
        private int $id_rol,
        private string $nombre_usuario,
        private string $contraseña
    ) {
    }

    public function getId_usuario(): int {
        return $this->id_usuario;
    }

    public function setId_usuario(int $id_usuario): void {
        $this->id_usuario = $id_usuario;
    }

    public function getId_rol(): int {
        return $this->id_rol;
    }

    public function setId_rol(int $id_rol): void {
        $this->id_rol = $id_rol;
    }

    public function getNombre_usuario(): string {
        return $this->nombre_usuario;
    }

    public function setNombre_usuario(string $nombre_usuario): void {
        $this->nombre_usuario = $nombre_usuario;
    }

    public function getContraseña(): string {
        return $this->contraseña;
    }

    public function setContraseña(string $contraseña): void {
        $this->contraseña = $contraseña;
    }
}

?>
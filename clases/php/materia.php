<?php

class Materia {

    public function __construct(
        private int $id_materia,
        private string $nombre_materia
    ) {
    }

    public function getId_materia(): int {
        return $this->id_materia;
    }

    public function setId_materia(int $id_materia): void {
        $this->id_materia = $id_materia;
    }

    public function getNombre_materia(): string {
        return $this->nombre_materia;
    }

    public function setNombre_materia(string $nombre_materia): void {
        $this->nombre_materia = $nombre_materia;
    }
}
?>
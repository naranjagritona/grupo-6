<?php

interface dao
{
    public function insertar($obj);

    public function modificar($obj);

    public function eliminar($id);

    public function listarTodos();
}
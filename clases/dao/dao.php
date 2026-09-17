<?php

interface dao {
    pblic function insertar($obj);
    
    public function modificar($obj);
    
    public function eliminar($id);
    
    public function listarTodos();
}

?>
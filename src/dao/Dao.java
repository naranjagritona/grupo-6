package dao;
import java.util.List;

public interface Dao<T, ID> {
    
    // Crear
    void insertar(T entidad);
    
    // Leer (todos los registros)
    List<T> obtenerTodos();
    
    // Actualizar
    void modificar(T entidad);
    
    // Eliminar
    void eliminar(ID id);
}
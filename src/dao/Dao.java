package dao;

import java.util.List;

// definimos las operaciones que comparten las clases de acceso a la base de datos
public interface Dao<T, ID> {

    // Crear
    void insertar(T entidad);

    T obtenerPorId(ID id);

    // Leer (todos los registros)
    List<T> obtenerTodos();

    // Actualizar
    void modificar(T entidad);

    // Eliminar
    void eliminar(ID id);
}

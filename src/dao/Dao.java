package dao;

import java.util.List;

// definimos las operaciones que comparten las clases de acceso a la base de datos
public interface Dao<T, ID> {

    // crear
    void insertar(T entidad);

    T obtenerPorId(ID id);

    // leer (todos los registros)
    List<T> obtenerTodos();

    // actualizar
    void modificar(T entidad);

    // eliminar
    void eliminar(ID id);
}

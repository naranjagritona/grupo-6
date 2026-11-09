package dao;

import java.util.List;

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

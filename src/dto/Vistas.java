package dto;

import java.beans.Introspector;
import java.time.*;
import java.util.*;

// Adaptación para las pantallas existentes, los DAO devuelven entidades, no mapas.
public final class Vistas {
    private Vistas() {}

    public static Map<String, Object> fila(Object objeto) {
        Map<String, Object> datos = new LinkedHashMap<>();
        if (objeto == null) return datos;
        try {
            // recorremos los métodos get para armar los datos que necesita la pantalla
            // se excluye la contraseña porque no debe llegar a los formularios ni a las tablas
            for (var propiedad :
                    Introspector.getBeanInfo(objeto.getClass(), Object.class)
                            .getPropertyDescriptors()) {
                String nombre = propiedad.getName();
                if (propiedad.getReadMethod() == null || nombre.equals("contraseña")) continue;
                Object valor = propiedad.getReadMethod().invoke(objeto);
                // conservamos el tipo de fecha que usan los filtros y las comparaciones del listado
                if (valor instanceof LocalDate fecha) valor = java.sql.Date.valueOf(fecha);
                else if (valor instanceof LocalDateTime fecha)
                    valor = java.sql.Timestamp.valueOf(fecha);
                datos.put(nombre, valor);
            }
            return datos;
        } catch (ReflectiveOperationException | java.beans.IntrospectionException e) {
            throw new IllegalStateException("No se pudieron preparar los datos de la pantalla.", e);
        }
    }

    // los datos de una pantalla pueden traer el ID como distintos tipos de número
    // lo pasamos a int antes de usarlo para buscar un registro
    public static int numero(Map<String, Object> fila, String campo) {
        return ((Number) fila.get(campo)).intValue();
    }
}

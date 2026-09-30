package main;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;

public class main {
    public static void main(String[] args) {
        System.out.println("Comprobando conexión con la base de datos...");

        try (Connection conexion = Conexion.getConnection()) {
            if (conexion != null && !conexion.isClosed()) {
                System.out.println("¡Conexión establecida correctamente!");
            } else {
                System.out.println("La conexión devolvió un valor nulo.");
            }
        } catch (SQLException error) {
            System.out.println("Error al intentar conectar:");
            error.printStackTrace();
        }
    }
}
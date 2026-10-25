package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mysql://pma.torga.com.ar:3306/s23_academia_integral_xxi";
    private static final String USER = "u23_GZ8nhz7rRw";
    private static final String PASSWORD = "SGpFfP1KLWxw9Gv=ile+whK=";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL + "?connectTimeout=10000&socketTimeout=15000", USER, PASSWORD);
        } catch (ClassNotFoundException error) {
            // Si falta el conector, avisamos el problema en vez de devolver una conexión vacía.
            throw new IllegalStateException("No se encontró el conector MySQL. Revisá las bibliotecas del proyecto en Eclipse.", error);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo conectar con la base de datos. Revisá Internet y la configuración de conexión.", error);
        }
    }
}

package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mysql://pma.torga.com.ar:3306/s23_academia_integral_xxi";
    private static final String USER = "u23_GZ8nhz7rRw";
    private static final String PASSWORD = "SGpFfP1KLWxw9Gv=ile+whK=";

    public static Connection getConnection() {
        Connection conexion = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException error) {
            error.printStackTrace();
        }
        return conexion;
    }
}
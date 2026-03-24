package conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class basedatos {
    public static Connection conectar() {
        try {
            String url = "jdbc:postgresql://localhost:5432/bd_inventario";
            // "jdbc:mysql://localhost:3306/tu_base"
            String user = "postgres";
            String password = "root";
            
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error conexion: " + e.getMessage());
            return null;
        }
    }
    
}

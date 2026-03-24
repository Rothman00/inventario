package conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class basedatos {
    public static Connection conectar() {
        try {
            String url = "jdbc:mysql://localhost:3306/bd_inventario?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String user = "root";
            String password = "root";
            
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error conexion: " + e.getMessage());
            return null;
        }
    }
    
}

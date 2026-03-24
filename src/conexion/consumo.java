package conexion;

import static conexion.basedatos.conectar;
import java.sql.*;
import java.util.*;

public class consumo {
    public static List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            Connection conexion = conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            setParams(ps, params);
            
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData data = rs.getMetaData();
            int columnas = data.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnas; i++) {
                    row.put(data.getColumnName(i), rs.getObject(i));
                }
                result.add(row);
            }
        } catch (Exception e) {
            System.out.println("Error en consulta: " + e.getMessage());
        }
        return result;
    }
    
    public static int execute(String sql, Object... params) {
        try {
            Connection conexion = conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            setParams(ps, params);
            
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error en execute: " + e.getMessage());
            return 0;
        }
    }
    
    private static void setParams(PreparedStatement ps, Object... params) {
        try {
            for (int i = 0; i < params.length; i++){
                ps.setObject(i + 1, params[i]);
            }
        } catch (Exception e) {
            System.out.println("Error en parametros: " + e.getMessage());
        }
    }
}

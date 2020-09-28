import java.net.*;
import java.sql.*;

public class Connect {
	public static final String USER = "";
	public static final String PASSWORD = "";
	public static final String HOST_IP = "45.79.55.190";
	public static final String SCHEMA_NAME = "cs3250_project";
	public static final int PORT = 3306;
	
	public static void main(String[] args) {
		Connection conn = null;
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			String url =
			 "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + SCHEMA_NAME;
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, USER, PASSWORD);
			System.out.println("Database connection established");
			Statement statement = conn.createStatement();
			int result = createTable("inventory", conn);
			String select = "SELECT * FROM inventory;";
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(conn != null) {
				try {
					conn.close();
					System.out.println("Database connection terminated");
				}
				catch(Exception e) { /* ignore close errors */ }
			}
		}
	}
	
	private static int deleteTable(String tableName, Connection conn) 
	 throws SQLException {
		return conn.createStatement().executeUpdate(
		 "DROP TABLE IF EXISTS " + tableName
		);
	}
	
	private static int createTable(String tableName, Connection conn) 
	 throws SQLException {
		deleteTable(tableName, conn);
		int result = conn.createStatement().executeUpdate(
		 "CREATE TABLE " + tableName + " (idx int(16) NOT NULL AUTO_INCREMENT,product_id VARCHAR(16)," +
		  "quantity int(8)," +
		  "wholesale_cost decimal (13,2)," +
		  "sale_price decimal (13,2)," +
		  "supplier_id VARCHAR(16)," +
		  "PRIMARY KEY (idx));");
		return result;
	}
}

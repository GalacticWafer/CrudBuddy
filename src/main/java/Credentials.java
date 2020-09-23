import java.sql.SQLException;

public class Credentials {
	static String userName = "OutbreakSource";
	static String password = "HHwp5r2)|j";
	static String ipAddress = "45.79.55.190";
	static String portNumber = "3306";
	static String databaseName = "cs3250_project";
	
	public static CRUDBuddy signIn() throws SQLException, ClassNotFoundException {
		return new CRUDBuddy(userName, password, ipAddress, portNumber, databaseName);
	}
}

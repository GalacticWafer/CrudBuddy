package customerrelationsmanagement;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.ParseException;

class GUITest {

	
	private static Crud crud;
	
	public static void invoke(CredentialsTest credentials)
	throws SQLException, ClassNotFoundException {
		crud = credentials.getCrud();
		new CrudTest();
	}
}

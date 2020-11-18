package customerrelationsmanagement;

import customerrelationsmanagement.Credentials;
import customerrelationsmanagement.Crud;
import customerrelationsmanagement.GUI;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.ParseException;

class GUITest {
	@Test public void assertFileCreatedTest() throws SQLException, ClassNotFoundException, ParseException {
		Crud crud = new Credentials().getCrud();
		String FileName = "gui.csv";
		GUI GUI = new GUI(crud);
	}
	
	private static Crud crud;
	
	public static void invoke(CredentialsTest credentials)
	throws SQLException, ClassNotFoundException {
		crud = credentials.getCrud();
		new CrudTest();
	}
}

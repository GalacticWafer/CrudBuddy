import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class GUITest {
	@Test public void assertFileCreatedTest() throws SQLException, ClassNotFoundException
	{
		Crud crud = Credentials.databaseLogin();
		String FileName = "gui.csv";
		GUI GUI = new GUI(crud);
		
		
	}
}

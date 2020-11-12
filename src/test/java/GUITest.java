import customerrelationsmanagement.Analytics;
import customerrelationsmanagement.Credentials;
import customerrelationsmanagement.Crud;
import customerrelationsmanagement.GUI;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class GUITest {
	@Test public void assertFileCreatedTest() throws SQLException, ClassNotFoundException
	{
		Crud crud = new Credentials().getCrud();
		String FileName = "gui.csv";
		GUI GUI = new GUI(crud, new Analytics(crud));
		
	}
}

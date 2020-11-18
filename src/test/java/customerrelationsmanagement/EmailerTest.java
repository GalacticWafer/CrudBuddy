package customerrelationsmanagement;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class EmailerTest {
	private static Crud crud;
	
	public static void invoke(CredentialsTest credentials)
	throws SQLException, ClassNotFoundException {
		crud = credentials.getCrud();
		new CrudTest();
	}
	
	@Test
	void processDailyEmails() {
	}
	
	@Test
	void sendMail() {
	}
}

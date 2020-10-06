import org.junit.Test;

import javax.mail.MessagingException;
import java.io.FileNotFoundException;
import java.sql.SQLException;

class SalesProcessorTest {

	@Test public void assertNewEmail() throws SQLException {
		
	}
	@Test public void assertNewCustomer(){
		
	}
	@Test public void assertOldCustomer(){
		
	}
	@Test public void assertNewDate(){
		
	}
	@Test public void assertNewProductId(){
		
	}
	
	public static void main(String[] args)
	throws SQLException, ClassNotFoundException, FileNotFoundException, MessagingException {
		Crud crud = Credentials.databaseLogin();
		SalesProcessor sp = new SalesProcessor(crud);
		new GUI(crud);
		sp.processItems("little_order_test.csv");
	}
}

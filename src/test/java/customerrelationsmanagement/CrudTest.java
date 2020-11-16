package customerrelationsmanagement;

import org.junit.jupiter.api.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class CrudTest {
	private static Crud crud;
	
	public static void invoke(CredentialsTest credentials)
	throws SQLException, ClassNotFoundException {
		crud = credentials.getCrud();
		Result result = JUnitCore.runClasses(customerrelationsmanagement.CrudTest.class);
		  for (Failure failure : result.getFailures()) {
			 System.out.println(failure.toString());
		  }
			
		  System.out.println(result.wasSuccessful());
	}
	
	public CrudTest() throws SQLException, ClassNotFoundException {
		// put the test methods you want to call in here
		arrayToCSV();
	}

	@Test
	public void arrayToCSV() {
		assertEquals(
		 String.join(",",new String[]{"abc", "'123'", "xyz"}),
		 "abc,'123',xyz");
	}
	
	@Test
	void deleteAllRecords() {
	}
	
	@Test
	void deleteRecord() {
	}
	
	@Test
	void deleteTable() {
	}
	
	
	@Test
	void getAssetTotal() {
	}
	
	@Test
	void getColumnCount() {
	}
	
	@Test
	void getColumnNames() throws SQLException {
		crud.setWorkingTable("inventory");
		assertArrayEquals(crud.getColumnNames(),
		 Tables.INVENTORY.columns());
		
		crud.setWorkingTable("sales");
		assertArrayEquals(crud.getColumnNames(),
		 Tables.STATUSED.columns());
		
		crud.setWorkingTable("inventory");
	}
	
	@Test
	void getColumnTypes() {
	}
	
	@Test
	void getDatabaseName() {
	}
	
	@Test
	void getRecords() {
	}
	
	@Test
	void getTableNames() {
	}
	
	@Test
	void getType() {
	}
	
	@Test
	void getWorkingTable() {
	}
	
	@Test
	void insertRecords() {
	}
	
	@Test
	void testInsertRecords() {
	}
	
	@Test
	void insertTable() {
	}
	
	@Test
	void insertTableFromGui() {
	}
	
	@Test
	void isClosed() {
	}
	
	@Test
	void mostOrderedProducts() {
	}
	
	@Test
	void mostValuableCustomers() {
	}
	
	@Test
	void query() {
	}
	
	@Test
	void quoteWrap() {
	}
	
	@Test
	void removeUTF8BOM() {
	}
	
	@Test
	void rowCountResults() {
	}
	
	@Test
	void setWorkingTable() {
	}
	
	@Test
	void size() {
	}
	
	@Test
	void toValueTuple() {
	}
	
	@Test
	void testToValueTuple() {
	}
	
	@Test
	void topNByCustomer() {
	}
	
	@Test
	void topNByDate() {
	}
	
	@Test
	void update() {
	}
	
	@Test
	void updateRow() {
	}
	
	@Test
	void writeToFile() {
	}
}

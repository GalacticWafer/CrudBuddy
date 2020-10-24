import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class CrudTest {
	Crud crud = Credentials.databaseLogin();

	CrudTest() throws SQLException, ClassNotFoundException {}

	@Test
	void arrayToCSV() {
		assertEquals(
		 crud.arrayToCSV(new String[]{"abc", "'123'", "xyz"}),
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
	void exists() throws SQLException {
		assertTrue(crud.exists("product_id","ZRDATK9CSM23"));
		assertFalse(crud.exists("product_id","ABRA_CADABRA"));
	}
	
	@Test
	void find() throws SQLException {
		assertTrue(crud.find("ZRDATK9CSM23", "product_id").length > 0);
		
		assertArrayEquals(crud.find("NOPENOPENOPE", "product_id"), 
		 new Object[]{null,null,null,null,null,null});
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
		 Crud.INVENTORY_COLUMNS);
		
		crud.setWorkingTable("sales");
		assertArrayEquals(crud.getColumnNames(),
		 Order.SALES_COLUMNS);
		
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

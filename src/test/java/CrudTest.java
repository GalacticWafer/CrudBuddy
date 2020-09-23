import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CrudTest {
	Crud crud = Credentials.signIn();
	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void arrayToCSV() throws SQLException {
		assertEquals(crud.arrayToCSV(COLUMN_NAMES), "product_id, quantity, wholesale_cost, sale_price, supplier_id");
	}

	@Test
	void find() throws SQLException {
		Object[] objects = new Object[]{"YJ8PRKB0LUPT",5655,430.58,563.2,"UKVXLLYL"};
		Object[] results = crud.find("YJ8PRKB0LUPT", "product_id");
		for(int i = 1; i < objects.length; i++) {
			//assertTrue(objects[i].toString().equals(results[i - 2].toString()));
		}
		
	}
	
	@Test
	void format() {
	}

	@Test
	void getAllRecords() {
	}

	@Test
	void getColumnCount() {
	}

	@Test
	void getColumnNames() {
	}

	@Test
	void getColumnTypes() {
	}

	@Test
	void getColumnsTuple() {
	}

	@Test
	void getDatabaseName() {
	}

	@Test
	void getTableNames() {
	}

	@Test
	void getType() {
	}

	@Test
	void getValues() {
	}

	@Test
	void getValuesTuple() {
	}

	@Test
	void insertRecords() {
	}

	@Test
	void insertTable() {
	}

	@Test
	void isProcessableOrder() {
	}

	@Test
	void query() {
	}

	@Test
	void queryF() {
	}

	@Test
	void setQuantityFromOrder() {
	}

	@Test
	void setWorkingTable() {
	}

	@Test
	void update() {
	}

	@Test
	void updateF() {
	}

	@Test
	void updateRow() {
	}

	@Test
	void writeToFile() {
	}

	CrudTest() throws SQLException, ClassNotFoundException {}
	
	public static final String[] COLUMN_NAMES =
	 new String[] {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};
}

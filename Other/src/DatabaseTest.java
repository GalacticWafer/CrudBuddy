

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

class DatabaseTest {
	private static final String FILE_NAME = "inventory_team4.csv";
	@Test public void databaseTest() throws FileNotFoundException {
		Database db = new Database();

		assertEquals(0, db.size());
		assertNotEquals(1, db.size());

		Table inventory = new Table(FILE_NAME, "table1");

		Table table1 = new Table("test1.csv", "table1");
		db.addTable(table1);

		Table table2 = new Table("test1.csv", "table2");
		db.addTable(table2);
		Assertions.assertNotNull(db.createEmptyTable("test2.csv", "table2"));

		Table table3 = new Table("test3.csv", "table3");
		db.addTable(table3);

		Table table4 = new Table("test4.csv", "table4");
		db.addTable(table4);

		Table table5 = new Table("test4.csv", "table5");
		db.addTable(table5);

		assertEquals(5, db.size());

		Assertions.assertEquals(table4, table5);
		Assertions.assertNotEquals(table3, table4);

		// first test row
		String[] row1 = "HR30BB21WKZW,4933,402.08,703.64,SSLDGFIO".split(",");
		DataRow entry1 = new DataRow(row1);

		// second test row
		String[] row2 = "53TUYLTAPU4U,627,36.21,53.81,MHPSLALJ".split(",");
		DataRow entry2 = new DataRow(row2);

		System.out
				.println(
						entry1.toReadableString() + "\n" + entry2.toReadableString() + "\n");

		// setQuantity() valid
		System.out.println("entry2.setQuantity(500):");
		entry2.setquantity(500);
		System.out
				.println("entry2.getQuantity() --> " + entry2.getQuantity() + "\n ");


		// setQuantity() invalid
		System.out.println("setting entry1 quantity to a negative value:" +
				"\nentry1.setQuantity(-1)   --> " +
				entry1.setquantity(-1) + "\n");

		// print updated readable string of entry1
		System.out.println(entry1.toReadableString());


		// Test update "test4.csv"
		table5.update("table5.csv", table2);


		db.deleteTable("table1");
		assertFalse(db.contains(table1.getName()));


	}
}


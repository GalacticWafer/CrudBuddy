/*
import javax.mail.MessagingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Main {
		public static final String[] COLUMN_NAMES =
	 new String[] {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};

	
	public static void main(String[] args)
	throws SQLException, ClassNotFoundException, FileNotFoundException {
		Crud crud = Credentials.databaseLogin();
		crud.setWorkingTable("inventory");
		crud.writeToFile("new_test.csv", crud.getColumnNames(), crud.getAllRecords());
	}
	
	private static void insertNewRecord(Crud crud, String tableName) throws SQLException {
		crud.setWorkingTable(tableName);
		Object[] insertFromOrder = crud.find("VBEUS2ETCKA4","product_id");
		System.out.println(Arrays.toString(insertFromOrder));
		String someTextField = "sales";
		int columnCount = crud.getColumnCount(someTextField);
		System.out.println(columnCount);
		Object[] newRecord = new Object[columnCount];
		for(int i = 0; i < newRecord.length; i++) {
			newRecord[i] = null*/
/*fixme, insertRecord all the names of your JtextFields and such variables*//*
;
		}
	}
	
	private static void dailyOrderCheck(Crud crud)
	throws MessagingException, IOException, SQLException {
		
		LinkedList<int[]> list = Emailer.processEmailOrders(crud);
		System.out.println(list);
	}
	
	private static void orderDemoSetup(Crud crud) throws SQLException {
		
		crud.setWorkingTable("inventory");
		Random rand = new Random();
		String[] testIds = new String[] {
		 "RHPXPHGBJS1P", "QSC2QT4FINCJ",
		 "64KOGZHOKLE4", "PHRXVC8ALL0F",
		 "VCNZ0DSD7YSZ", "G0L5248Q2Z3F",
		 "GZ7GOTL72VMS", "ILP04DM9GDK3",
		 "WVTYLB7W4GJ5", "B5LJR0ELKJXL",
		 "STPJASCJV013", "PYRQR50LBHXG",
		 "U9VRBFPT4DHP", "ZUVE5YZY005O"
		};
		String[] goodIds = new String[] {
		 "17TXG621YHM0", "5OAUECA58RWR",
		 "GSOB9EYF7O7G", "I5PDMXUJFVIY",
		 "B89RPM7ZZ7WG", "BVQT9IUZLOV0",
		 "198BKGR5C2YQ", "A1D5CZQM7PFA",
		 "CDTGG7B2FTIT", "GQ0WZUI7J6CT",
		 "J575EG1KGXDI", "NBGSOV8WIZ54",
		 "VJTR3EQFQZ5E", "6TWB0AUWETK7"
		};
		for(String testId: testIds) {
			crud
			 .updateRow(new String[] {"quantity"}, new Object[] {rand.nextInt(2000)},
			  "product_id", testId, "inventory");
		}
		
		for(String goodId: goodIds) {
			crud
			 .updateRow(new String[] {"quantity"}, new Object[] {999999999},
			  "product_id", goodId, "inventory");
		}
	}
}
*/

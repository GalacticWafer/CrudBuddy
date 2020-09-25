import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Random;

public class CRUDBuddyDriver {
	public static void main(String[] args)
	throws SQLException, ClassNotFoundException {
		invgui GUI = new invgui();
		CRUDBuddy crud = Credentials.signIn();
		
		// restocks a select group of products with random quantities
		orderDemoSetup(crud);
		
		// checks the emails, processes orders, responds to customers, and updates sales
		// & inventory tables
		dailyOrderCheck(crud);
	}
	
	private static void dailyOrderCheck(CRUDBuddy crud) {
		
		LinkedList<int[]> list = EmailReader.processEmailOrders(crud);
		System.out.println(list);
	}
	
	private static void orderDemoSetup(CRUDBuddy crud) throws SQLException {
		
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

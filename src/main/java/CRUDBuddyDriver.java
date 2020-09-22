import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class CRUDBuddyDriver {
	static String userName = "OutbreakSource";
	static String password = "HHwp5r2)|j";
	static String ipAddress = "45.79.55.190";
	static String portNumber = "3306";
	static String databaseName = "cs3250_project";
	static String tableName = "test";
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		
		CRUDBuddy crud =
		 new CRUDBuddy(userName, password, ipAddress, portNumber, databaseName);
		orderDemoSetup(crud); // restocks a select group of products with random quantities
		
		
		//crud.upLoadTable();
		//crud.upLoadTable("customers.csv");
		//System.out.println(crud.readColumnTypes("sales"));
		dailyOrderCheck(crud);
	}
	
	private static void dailyOrderCheck(CRUDBuddy crud) throws SQLException {
		
		LinkedList<Order[]> list = EmailReader.readEmailOrders();
		for(Order[] orders: list) {
			for(Order order: orders) {
				int result = crud.processOrder(order.getProductId(),
				 order.getQuantity());
				System.out.println(result);
				if(result >= 0) {
					crud.recordOrder(order);
				}
			}
		}
	}
	
	private static void orderDemoSetup(CRUDBuddy crud) throws SQLException {
		
		crud.setWorkingTable("inventory");
		Random rand = new Random();
		String[] testIds =
		 ("RHPXPHGBJS1P,QSC2QT4FINCJ,64KOGZHOKLE4,PHRXVC8ALL0F,VCNZ0DSD7YSZ,G0L5248Q2Z3F," +
		  "GZ7GOTL72VMS,ILP04DM9GDK3,WVTYLB7W4GJ5,B5LJR0ELKJXL,STPJASCJV013," +
		  "PYRQR50LBHXG,U9VRBFPT4DHP,ZUVE5YZY005O")
		  .split(",");
		for(String testId: testIds) {
		crud.updateRow(new String[] {"quantity"}, new Object[] {rand.nextInt(2000)}, "product_id",
		 testId, "inventory");
		}
	}
	
	private static void testReadColNames(CRUDBuddy crud) throws SQLException {
		
		ArrayList<String> headers = crud.readColumnNames("cs3250_project", "test");
		for(String header: headers) {
			System.out.println(header);
		}
	}
}

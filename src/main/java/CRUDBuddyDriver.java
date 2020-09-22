import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class CRUDBuddyDriver {
	static String userName = "OutbreakSource";
	static String password = "HHwp5r2)|j";
	static String ipAddress = "45.79.55.190";
	static String portNumber = "3306";
	static String databaseName = "cs3250_project";
	static String tableName = "test";
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		CRUDBuddy crud = new CRUDBuddy(userName, password, ipAddress, portNumber, databaseName);
		//crud.upLoadTable("sales.csv");
		//crud.upLoadTable("customers.csv");
		System.out.println(crud.readColumnTypes("sales"));
		dailyOrderCheck(crud);
	}
	
	private static void dailyOrderCheck(CRUDBuddy crud) throws SQLException {
		
		crud.setWorkingTable("inventory");
		crud.updateRow(new String[]{"quantity"},new Object[]{999},"product_id", "RHPXPHGBJS1P", "inventory");
		LinkedList<EmailOrder> list =  EmailReader.readAllEmails();
		for(EmailOrder order: list) {
			int result = crud.processOrder(order.getProductId(), order.getQuantity());
			System.out.println(result);
			if(result >= 0) {
				crud.recordOrder(order);
			}
		}
	}
	
	private static void testReadColNames(CRUDBuddy crud) throws SQLException {
		ArrayList<String> headers = crud.readColumnNames("cs3250_project", "test");
		for(String header: headers) {
			System.out.println(header);
		}
	}
}

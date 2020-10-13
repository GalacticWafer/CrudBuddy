import java.awt.*;

public class Main {
	private static Crud crud;
	private static GUI gui;
	private static int slide = 0;
	private static Object[][] testRecord;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	
	public static void main(String[] args)
	throws Exception {
		crud = Credentials.databaseLogin();
		//crud.setWorkingTable("sales");
		//Object x = crud.getAssetTotal(null);
		//System.out.println(x);
		GUI gui = new GUI(crud);
		String newTableName = crud.topNByCustomer(null, 30, true, gui);
		crud.setWorkingTable(newTableName);
		Object[][] description = crud.resultsToArray(crud.query("describe " + newTableName));
		String[] columnNames = new String[description.length];
		for(int i = 0; i < columnNames.length; i++) {
			columnNames[i] = String.valueOf(description[i][0]);
		}
		gui.setTempData(columnNames, crud.resultsToArray(crud.query("select * from " + newTableName)));
		//System.out.println(Arrays.deepToString(crud.resultsToArray(crud.query(
		// "select * from " + newTableName))));
		//System.out.println(Arrays.toString(crud.getColumnTypes("customers")));
		//Object[][] topCustomers =
		// crud
		//  .topNByCustomer("2020-01-03", "customer_email", "quantity", 5, true);
		//System.out.println(Arrays.deepToString(topCustomers));
		
		/*Object [][] topCustomers_firsTest =
			crud
			.topNbyDate("2020-01-03","date_accepted",5,true,"quantity");
		 System.out.println(Arrays.deepToString(topCustomers_firstTest));
		 */
		//gui = new GUI(crud);
		//SalesProcessor sp = new SalesProcessor(crud);
		//crud.deleteAllRecords("sales");
		//sp.processItems("customer_orders_A_team4.csv");
		
	}
	
}

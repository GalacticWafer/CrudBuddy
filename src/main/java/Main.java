import java.awt.*;
import java.util.Arrays;

public class Main {
	private static Crud crud;
	private static GUI gui;
	private static int slide = 0;
	private static Object[][] testRecord;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	
	public static void main(String[] args)
	throws Exception {
		new GUI(Credentials.databaseLogin());
		// topNByCustomer()
		crud = Credentials.databaseLogin();
		GUI gui = new GUI(crud);
		 String newTableName = crud.topNByCustomer(null, 30, true, gui);
		crud.setWorkingTable(newTableName);
		Object[][] description = crud.resultsToArray(crud.query("describe " + 
		newTableName));
			System.out.println(Arrays.deepToString(crud.resultsToArray(crud.query("Select * from " + newTableName))));
		// reload sales table
		/*
		crud = Credentials.databaseLogin();
		for(String s: new String[] {"sales", "customers", "inventory"}) {
			crud.setWorkingTable(s);
			crud.deleteAllRecords();
		new SalesProcessor(crud).processItems("customer_orders_A_team4.csv");
		new GUI(crud);
		}
		*/
	}
}

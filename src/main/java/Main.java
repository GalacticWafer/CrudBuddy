import java.awt.*;

public class Main {
	private static Crud crud;
	private static GUI gui;
	private static int slide = 0;
	private static Object[][] testRecord;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	
	public static void main(String[] args)
	throws Exception {
		Crud crud = Credentials.databaseLogin();
		//new GUI(crud);
		// topNByCustomer()
		/*
		crud = Credentials.databaseLogin();
		GUString newTableName =I gui = new GUI(crud);
		 crud.topNByCustomer(null, 30, true, gui);
		crud.setWorkingTable(newTableName);
		Object[][] description = crud.resultsToArray(crud.query("describe " + 
		newTableName));
		String[] columnNames = new String[description.length];
		for(int i = 0; i < columnNames.length; i++) {
			columnNames[i] = String.valueOf(description[i][0]);
		}
		gui.setTempData(columnNames, crud.resultsToArray(crud.query("select * 
		from " + newTableName)));
		*/
		/*
		crud = Credentials.databaseLogin();
		for(String s: new String[] {"sales", "customers", "inventory"}) {
			crud.setWorkingTable(s);
			crud.deleteAllRecords();
		new SalesProcessor(crud).processItems("customer_orders_A_team4.csv");
		new GUI(crud);
		}
		*/
		// reload sales table
		
		new Restoration(crud);
	}
}

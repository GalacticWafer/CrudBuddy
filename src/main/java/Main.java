import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
	private static final int DATE = 0;
	private static final int DOUBLE = 1;
	private static final int INTEGER = 2;
	private static final int VARCHAR = 3;
	private static Crud crud;
	private static GUI gui;
	private static int slide = 0;
	private static Object[][] testRecord;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	
	private static HashMap<Integer, String> getTypes(String[] firstColumn) {
		int[] columnTypes = new int[firstColumn.length];
		for(int i = 0; i < firstColumn.length; i++) {
			try {
				Integer.parseInt(firstColumn[i]);
				columnTypes[i] = INTEGER;
			}
			catch(Exception e1) {
				try {
					Double.parseDouble(firstColumn[i]);
					columnTypes[i] = DOUBLE;
				}
				catch(Exception e2) {
					try {
						LocalDate.parse(firstColumn[i]);
						columnTypes[i] = DATE;
					}
					catch(Exception e3) {
						columnTypes[i] = VARCHAR;
					}
				}
			}
		}
		HashMap<Integer, String> types = new HashMap<>();
		for(int i = 0; i < firstColumn.length; i++) {
			types.put(
			 i,
			 (
			  columnTypes[i] == INTEGER ? "int(16)" :
			   columnTypes[i] == DATE ? "DATE" :
				columnTypes[i] == DOUBLE ? "decimal(13,2)" :
				 "VARCHAR(100)"
			 ));
		}
		return types;
	}
	
	private static void loadInFile(String pathname, String tableName)
	throws FileNotFoundException, SQLException {
		Scanner scanner = new Scanner(new File(pathname));
		String[] columnNames = scanner.nextLine().split(",");
		String[] firstColumn = scanner.nextLine().split(",");
		
		crud.insertTable(tableName, columnNames, getTypes(firstColumn));
		
		String sql = "load data infile '" + pathname + "' \n" +
					 "   replace \n" +
					 "   into table " + tableName + " \n" +
					 "   columns terminated by '\\t' \n" +
					 "   ignore 1 lines";
		System.out.println("# of records inserted: " + crud.update(sql));
	}
	/*"The finance, marketing,and sales departments want to understand 
	customers better but need the historical data to do so. 
	They want to have a 
	database/pseudo-DB that stores all of the customer order information along 
	with the time and date of their orders.[This database will grow each 
	simulated “day”.]
	
	•The company’s marketing and sales departments wants to 
	receive daily reports showing which were the 10 most-ordered products, and
	 who were the 10 best customers (ordering the most by dollar amount).
	 
	 
	 •The company’s finance department wants to receive daily reports of: 
	 how much the company has in assets (sum of product quantities x their wholesale
	  prices) the total number customer orders coming in the total dollar 
	  amount of customer orders coming in
	  
	  */
	
	public static void main(String[] args)
	throws Exception {
		crud = Credentials.databaseLogin();
		loadInFile("customer_orders_A_team4.csv", "fucking_test_table");
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

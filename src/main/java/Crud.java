import com.mysql.cj.MysqlType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

class Crud {
	public static final int CUSTOMERS = 2;
	private static String DB_NAME;
	private static String HOST_IP;
	public static final String[] INVENTORY_COLUMNS = new String[]
	 {"product_id", "wholesale_cost", "sale_price", "supplier_id", "quantity"};
	private static String PORT;
	public static String PRIMARY_K = "idx";
	public static String PRIMARY_V = "int(16)";
	public static final int SALES = 0;
	private static Connection connection;
	private String currentTable = "inventory";
	public static TreeSet<String> temporaryTables = new TreeSet<>();
	
	/**
	 * Class that facilitates a connection to a database, and carries out CRUD
	 * operations
	 */
	public Crud
	(String userName, String passWord, String hostIP, String port,
	 String schema)
	throws SQLException, ClassNotFoundException {
		HOST_IP = hostIP;
		PORT = port;
		DB_NAME = schema;
		Class.forName("com.mysql.cj.jdbc.Driver");
		connection = DriverManager.getConnection(getURL(), userName, passWord);
		setWorkingTable("inventory");
		update("USE " + DB_NAME);
		//JOptionPane.showMessageDialog(null, "Connection OK with " + getURL
		// ());
	}

	/**
	 * Creates a gui to get user input on a new table to be uploaded to MySQL
	 * database.
	 */
	private void csvGuiLoad(String[] columns, String fileName) {
		uploadCsvGui gui = new uploadCsvGui(columns, fileName, this);
		gui.invoke();
	}
	
	/** Delete the record in the specified table */
	public int deleteRecord
	(String columnName, Object columnValue)
	throws SQLException {
		return update(
		 "DELETE FROM " + currentTable + " WHERE " + columnName + " = " +
		 quoteWrap(columnValue));
	}
	
	boolean exists(String columnName, Object columnValue)
	throws SQLException {
		String sql =
		 "SELECT EXISTS(SELECT * FROM " + currentTable + " WHERE " +
		 columnName + " = " + quoteWrap(columnValue) + ")";
		ResultSet rs = query(sql);
		rs.next();
		return rs.getInt(1) == 1;
	}
	
	public Object getAssetTotal(String onDate)
	throws SQLException {
		String query = (onDate == null) ?
		 "SELECT SUM(quantity * (sale_price - wholesale_cost))" +
		 "as assets from cs3250_project.inventory" :
		 "SELECT date_accepted, SUM(cs3250_project.sales.quantity * " +
		 "(sale_price - wholesale_cost)) \n" +
		 "        as assets from sales\n" +
		 "INNER JOIN inventory _inventory on sales.product_id = _inventory" +
		 ".product_id\n" +
		 "WHERE date_accepted = '" + onDate + "' GROUP BY date_accepted";
		ResultSet rs = query(query);
		rs.next();
		return rs.getObject(1);
	}
	
	/** Gets the number of columns in a table */
	public int getColumnCount(String tableName) throws SQLException {
		setWorkingTable(tableName);
		ResultSet rs = query("SELECT count(*) AS " + tableName +
							 "FROM information_schema.columns WHERE" +
							 " table_name = " + quoteWrap(tableName));
		if(rs.next()) {
			return rs.getInt(1);
		}
		return -1;
	}
	
	/** Gets an arraylist of the column names of a specific table */
	public String[] getColumnNames() throws SQLException {
		 ResultSet rs = query(
		 "SELECT column_name FROM information_schema.columns " +
		 "WHERE table_schema = '" + DB_NAME +
		 "' AND table_name = '" + getWorkingTable() + "'");
		String[] columnNames = new String[rowCountResults(rs)];
		for(int i = 0; i < columnNames.length; i++) {
			columnNames[i] = rs.getString(1);
		}
		return columnNames;
	}
	
	/** Gets an arrayList of column types from a table */
	public String[] getColumnTypes(String tableName)
	throws SQLException {
		ResultSet rs = query("SELECT * FROM " + tableName + " WHERE 1<0");
		ResultSetMetaData data = rs.getMetaData();
		data.getColumnCount();
		String[] list = new String[data.getColumnCount()];
		for(int i = 0; i < list.length; i++) {
			list[i] = (MysqlType.getByJdbcType(data.getColumnType(i + 1)) +
					   "");
		}
		return list;
	}
	
	/** Return the database name */
	public String getDatabaseName() throws SQLException {
		return (String)getRecords("SELECT DATABASE()")[0][0];
	}
	
	/** Get a 2d array of all the results of the query. */
	public Object[][] getRecords(String sql) throws SQLException {
		return getRecords(query(sql));
	}
	
	/** Private helper for public getRecords() method. */
	private Object[][] getRecords(ResultSet rs) throws SQLException {
		int columnCount = rs.getMetaData().getColumnCount();
		int rowCount = rowCountResults(rs);
		rs.beforeFirst();
		Object[][] objects = new Object[rowCount][columnCount];
		for(int i = 0; rs.next(); i++) {
			for(int j = 0; j < columnCount; j++) {
				objects[i][j] = rs.getObject(j + 1);
			}
		}
		return objects;
	}
	
	/** Get an array with all the table names */
	public String[] getTableNames() throws SQLException {
		ResultSet rs = query("SHOW TABLES");
		int count = 0;
		ArrayList<String> temp = new ArrayList<>();
		while(rs.next()) {
			count++;
			temp.add(rs.getString(1));
		}
		String[] tableNames = new String[count];
		count = 0;
		for(String s: temp) {
			tableNames[count++] = s;
		}
		return tableNames;
	}
	
	/** Get the URL by joining static variable together. */
	private static String getURL() {
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
	}
	
	@NotNull protected String getWorkingTable() {
		return currentTable.substring(currentTable.indexOf(".") + 1);
	}
	
	/** Insert one or more new records into a table from a 2d array. */
	public int insertRecords(String[] columnNames, Object[][] array)
	throws InputMismatchException {
		if(array.length == 0 || columnNames.length != array[0].length) {
			throw new InputMismatchException();
		}
		StringBuilder sb = new StringBuilder(
		 "INSERT INTO " + getWorkingTable() + "(" +
		 String.join(",", columnNames) + ")VALUES");
		for(int i = 0; i < array.length; i++) {
			sb.append(toValueTuple(array[i]));
			sb.append(i == array.length - 1 ? ";" : ",");
		}
		try {
			return update(sb + "");
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			return -1;
		}
	}
	
	public void insertRecords(String[] columnNames,
							  Iterator<Object[]> tableValues, int size) {
		Object[][] array = new Object[size][columnNames.length];
		for(int i = 0; tableValues.hasNext(); i++) {
			array[i] = tableValues.next();
		}
		insertRecords(columnNames, array);
	}
	
	/** Creates a blank Table */
	public void insertTable
	(String tableName, String[] columnNames, HashMap<Integer, String> typeMap)
	throws SQLException {
		update("DROP TABLE IF EXISTS " + tableName);
		StringBuilder sb = new StringBuilder(
		 "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
		 PRIMARY_K + " " + PRIMARY_V + " NOT NULL AUTO_INCREMENT,");
		int i = 0;
		for(; i < typeMap.size(); i++) {
			String line = columnNames[i] + " " + typeMap.get(i) + ",";
			sb.append(line);
		}
		sb.append(" PRIMARY KEY (").append(PRIMARY_K).append("))");
		update(sb.toString());
	}
	
	/**
	 * Starts the process of getting user input for information about the table
	 * to be created and
	 * uploaded, then summons a gui to collect the input.
	 */
	void insertTableFromGui() throws Exception {
		String filePath = JOptionPane.showInputDialog(null, "Open file:");
		if(!filePath.endsWith(".csv")) {
			//Todo: throw new FileNotSupportedException
			return;
		}
		
		Scanner scanner = new Scanner(new File(filePath));
		String[] columns = scanner.nextLine().split(",");
		columns[0] = removeUTF8BOM(columns[0]);
		csvGuiLoad(columns, filePath);
	}
	
	protected Boolean isClosed() throws SQLException {
		return Crud.connection.isClosed();
	}
	
	public Object[][] mostOrderedProducts
	 (int limit)
	throws SQLException {
		String query =
		 "select product_id, sum(quantity) as totalQuantity from sales " +
		 " group by \nproduct_id order by sum(quantity) desc limit " + limit;
		return getRecords(query(query));
	}
	
	public Object[][] mostValuableCustomers(int n) throws SQLException {
		String query =
		 "SELECT cust_email , SUM(sales.quantity * sale_price - " +
		 "wholesale_cost) AS revenue FROM sales\n" +
		 "    INNER JOIN\n" +
		 "    inventory i ON sales.product_id = i.product_id\n" +
		 "GROUP BY cust_email ORDER BY revenue DESC LIMIT " + n;
		return getRecords(query(query));
	}
	
	/** Sends a sql query string */
	public ResultSet query(String query) throws SQLException {
		Statement st = connection.createStatement(
		 ResultSet.TYPE_SCROLL_INSENSITIVE,
		 ResultSet.CONCUR_UPDATABLE);
		return st.executeQuery(query);
	}
	
	
	/** Wraps the given object in quotes if it is a string */
	static String quoteWrap(Object columnValue) {
		if(columnValue instanceof String
		   || columnValue instanceof Date) {
			return "'" + columnValue + "'";
		}
		return columnValue.toString();
	}
	
	/** Removes the (BOM byte-order mark) from the beginning of the string. */
	public static String removeUTF8BOM(String s) {
		if(s.startsWith("\uFEFF")) {
			s = s.substring(1);
		}
		return s;
	}
	
	/** retrieve the number of rows of in a ResultSet. */
	int rowCountResults(ResultSet rs) throws SQLException {
		rs.last();
		return rs.getRow();
	}
	
	/**
	 * Sets the static variable <code>tableName</code> as the table to make
	 * statements against.
	 */
	public void setWorkingTable(String tableName) {
		this.currentTable = DB_NAME + "." + tableName;
	}
	
	/** retrieve the number of rows in a table. */
	int size() throws SQLException {
		ResultSet rs = query("SELECT COUNT(*) FROM " + currentTable);
		rs.next();
		return rs.getInt(1);
	}
	
	/** Makes a tuple of values from an array for sql statements */
	public String toValueTuple(Object[] array,
							   HashMap<Integer, String> typeMap) {
		if(typeMap == null) {
			throw new NullPointerException();
		}
		Object[] values = new Object[array.length];
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < values.length; i++) {
			sb.append(typeMap.get(i).contains("VARCHAR")
					  || typeMap.get(i).contains("DATE") ?
			 quoteWrap(array[i]) : array[i]);
			if(i == values.length - 1) {
				sb.append(")");
			} else {sb.append(",");}
		}
		return sb.toString();
	}
	
	/** Makes a tuple of values from an array for sql statements */
	public String toValueTuple(Object[] array) {
		Object[] values = new Object[array.length];
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < values.length; i++) {
			sb.append(quoteWrap(array[i]));
			if(i == values.length - 1) {
				sb.append(")");
			} else {sb.append(",");}
		}
		return sb.toString();
	}
	
	/**
	 *
	 */
	public String topNByCustomer
	(String date, int limit, boolean isDescending, GUI gui)
	throws SQLException {
		setWorkingTable("customers");
		update(" DROP TABLE IF EXISTS top_customers");
		String newTableName = "Top_" + limit + "_Customers";
		String sql =
		 " CREATE TEMPORARY TABLE IF NOT EXISTS " + newTableName +
		 " AS (SELECT customer_email, SUM(sales.quantity * " +
		 "(sale_price - wholesale_cost)) AS revenue " +
		 " FROM sales " +
		 (date == null ? "" :
		  "WHERE date_accepted = '" + date + "'") +
		 " INNER JOIN customers _customers on sales.customer_email" +
		 " = _customers.email " +
		 " INNER JOIN inventory i on sales.product_id = i" +
		 ".product_id " +
		 " GROUP BY customer_email " +
		 " ORDER BY revenue " + (isDescending ? " DESC" : "ASC") +
		 " LIMIT " + limit + ")";
		update(sql);
		temporaryTables.add(newTableName);
		gui.addTable(newTableName);
		return newTableName;
	}
	
	/**
	 * @param date
	 *  that date you want to produce the analysis for
	 * @param columnName
	 *  the column name that identifies the data to analyze
	 * @param limit
	 *  where limit is the number of rows you want (i.e., top 5? 10? 1000?)
	 * @param isDescending
	 *  top- to bottom if true, bottom to top if false
	 * @param orderArg
	 *  if you want to order these results by a specific column,
	 *  include it here (i.e., "quantity" -> higher quantities will
	 *  be on the top of the results
	 *
	 * @return 2d array of all the results
	 *
	 * @throws SQLException
	 *  if the query was an incorrect string, according to sql syntax
	 */
	public Object[][] topNByDate(String date, String columnName,
								 int limit, boolean isDescending,
								 String orderArg) throws SQLException {
		return getRecords(query(
		 "SELECT * FROM sales WHERE " + columnName + " = '" + date +
		 "' ORDER BY " + orderArg + (isDescending ? " DESC" : "ASC") +
		 " LIMIT " + limit));
	}
	
	public int update(String sql) throws SQLException {
		Statement st = connection.createStatement();
		return st.executeUpdate(sql);
	}
	
	/** Update a row where some column name = some value */
	public void updateRow
	(String[] columns, Object[] values, Object columnName, String columnValue)
	throws SQLException {
		setWorkingTable(currentTable);
		StringBuilder sf =
		 new StringBuilder("UPDATE " + currentTable + " SET ");
		for(int i = 0; i < columns.length; i++) {
			String str = columns[i] + "=" + quoteWrap(values[i]) +
						 (i < columns.length - 1 ? "," : "");
			sf.append(str);
		}
		sf.append(" WHERE ").append(columnName).append("=")
		  .append(quoteWrap(columnValue));
		update(sf + "");
	}
	
	/** Write a table from the database to a file */
	public File writeToFile
	(String path, String[] columns, ResultSet results)
	throws FileNotFoundException, SQLException {
		if(columns.length == 0) {throw new InputMismatchException();}
		File file = new File(path);
		PrintWriter pw = new PrintWriter(file);
		pw.println(String.join(",", columns));
		while(results.next()) {
			for(int i = 0; i < columns.length; i++) {
				pw.print(results.getObject(columns[i]));
				if(i < columns.length - 1) {pw.print(",");}
			}
			pw.println();
		}
		pw.close();
		return file;
	}
}

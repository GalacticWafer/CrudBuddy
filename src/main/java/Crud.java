import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

class Crud {
	private static String DB_NAME;
	private static String HOST_IP;
	public static final String[] INVENTORY_COLUMNS = new String[]
	 {"product_id", "wholesale_cost", "sale_price", "supplier_id", "quantity"};
	private static String PORT;
	public static String PRIMARY_K = "idx";
	public static String PRIMARY_V = "int(16)";
	private static Connection connection;
	private String currentTable = "inventory";
	public static TreeSet<String> temporaryTables = new TreeSet<>();
	
	/**
	 * Class that facilitates a connection to a database, and carries out CRUD
	 * operations to a MySQL database.
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
	} // End constructor
	
	/**
	 * Creates a gui to get user input on a new table to be uploaded to MySQL
	 * database.
	 */
	private void csvGuiLoad(String[] columns, String fileName) {
		
		uploadCsvGui gui = new uploadCsvGui(columns, fileName, this);
		gui.invoke();
	} // End csvGuiLoad
	
	/**
	 * Delete the record in the specified table
	 *
	 * @param columnName
	 *  name of the column to match.
	 * @param columnValue
	 *  value to match in the column.
	 *
	 * @return the number of rows affected by the update.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public int deleteRecord
	(String columnName, Object columnValue)
	throws SQLException {
		
		return update(
		 "DELETE FROM " + currentTable + " WHERE " + columnName + " = " +
		 quoteWrap(columnValue));
	} // End deleteRecord
	
	/**
	 * Check if a record exists with the specified criteria.
	 *
	 * @param columnName
	 *  name of the column to match.
	 * @param columnValue
	 *  value to match in the column.
	 *
	 * @return true if a record matches the criteria. Otherwise false.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	boolean exists(String columnName, Object columnValue)
	throws SQLException {
		
		String sql =
		 "SELECT EXISTS(SELECT * FROM " + currentTable + " WHERE " +
		 columnName + " = " + quoteWrap(columnValue) + ")";
		
		ResultSet rs = query(sql);
		rs.next();
		return rs.getInt(1) == 1;
	} // End exits
	
	/**
	 * TODO: David & Uriel
	 *
	 * @param onDate TODO: David & Uriel
	 *
	 * @return TODO: David & Uriel
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
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
	} // End getAssetTotal
	
	/**
	 * Gets the number of columns in a table.
	 *
	 * @param tableName
	 *  the table to check
	 *
	 * @return the number of columns in the table if it exists. Otherwise, -1.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public int getColumnCount(String tableName) throws SQLException {
		
		setWorkingTable(tableName);
		
		ResultSet rs = query("SELECT count(*) AS " + tableName +
							 "FROM information_schema.columns WHERE" +
							 " table_name = " + quoteWrap(tableName));
		
		if(rs.next()) {
			return rs.getInt(1);
		} // End if
		
		return -1;
	} // End setWorkingTable
	
	/**
	 * @return an array with the column names from the the
	 *  <code>currentTable</code>
	 */
	public String[] getColumnNames() throws SQLException {
		
		String query = "SELECT * FROM " + getCurrentTable();
		ResultSet rs = query(query);
		ResultSetMetaData rsMetaData = rs.getMetaData();
		
		int count = rsMetaData.getColumnCount();
		String[] columnNames = new String[count];
		
		for(int i = 0; i < count; i++) {
			columnNames[i] = rsMetaData.getColumnName(i + 1);
		} // End for
		return columnNames;
	} // End getColumnNames
	
	@NotNull protected String getCurrentTable() {
		
		return currentTable.substring(currentTable.indexOf(".") + 1);
	} // End getCurrentTable
	
	/** @return the database name */
	public String getDatabaseName() throws SQLException {
		
		return (String)getRecords("SELECT DATABASE()")[0][0];
	} // End getDatabaseName
	
	/**
	 * Get all matching rows from an sql query.
	 *
	 * @param sql
	 *  string to query.
	 *
	 * @return a 2d array representing all rows returned from a query.
	 */
	public Object[][] getRecords(String sql) throws SQLException {
		
		return getRecords(query(sql));
	} // End getRecords
	
	/** Private helper for public getRecords() method. */
	private Object[][] getRecords(ResultSet rs) throws SQLException {
		
		int columnCount = rs.getMetaData().getColumnCount();
		int rowCount = rowCountResults(rs);
		rs.beforeFirst();
		Object[][] objects = new Object[rowCount][columnCount];
		
		for(int i = 0; rs.next(); i++) {
			for(int j = 0; j < columnCount; j++) {
				objects[i][j] = rs.getObject(j + 1);
			} // End for
		} // End for
		return objects;
	} // End getRecords
	
	/**
	 * @return an array with all the table names from the database matching
	 *  <code>DB_NAME</code>.
	 */
	public String[] getTableNames() throws SQLException {
		
		ResultSet rs = query("SHOW TABLES");
		int count = 0;
		ArrayList<String> temp = new ArrayList<>();
		
		while(rs.next()) {
			count++;
			temp.add(rs.getString(1));
		} // End while
		
		String[] tableNames = new String[count];
		count = 0;
		for(String s: temp) {
			tableNames[count++] = s;
		} // End for
		return tableNames;
	} // End getTableNames
	
	/** Get the URL by joining static variable together. */
	private static String getURL() {
		
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
	} // End getUrl
	
	/**
	 * Insert one or more new records into a table from an
	 * <code>Object[][]</code>.
	 *
	 * @param columnNames
	 *  the columns to insert data into.
	 * @param tableValues
	 *  the <code>Object[][]</code>. of rows to be inserted into the columns.
	 *
	 * @return the number of rows affected by the update.
	 *
	 * @throws InputMismatchException
	 *  if <code>columnNames</code> and <code>tableValues[0]</code> have
	 *  different
	 *  lengths, or if either of their lengths are zero.
	 */
	public int insertRecords(String[] columnNames, Object[][] tableValues)
	throws InputMismatchException {
		
		if(tableValues.length == 0 ||
		   columnNames.length != tableValues[0].length) {
			throw new InputMismatchException();
		} // End if
		
		StringBuilder sb = new StringBuilder(
		 "INSERT INTO " + getCurrentTable() + "(" +
		 String.join(",", columnNames) + ")VALUES");
		for(int i = 0; i < tableValues.length; i++) {
			sb.append(toValueTuple(tableValues[i]));
			sb.append(i == tableValues.length - 1 ? ";" : ",");
		} // End for
		
		try {
			return update(sb + "");
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return -1;
		} // End try-catch
	} // end insertRecords
	
	/**
	 * Overloaded version of <code>insertRecords</code> that takes an
	 * <code>Iterator<Object[]></code> instead of an <code>Object[][]</code>.
	 *
	 * @param columnNames
	 *  the columns to insert data into.
	 * @param tableValues
	 *  an iterator whose <code>Object[]</code> elements are rows to be
	 *  inserted.
	 * @param size
	 *  the size of the collection from with the <code>Iterator</code> came, to
	 *  make the correctly-sized array.
	 */
	public void insertRecords
	(String[] columnNames, Iterator<Object[]> tableValues, int size) {
		
		Object[][] array = new Object[size][columnNames.length];
		for(int i = 0; tableValues.hasNext(); i++) {
			array[i] = tableValues.next();
		} // End for
		
		insertRecords(columnNames, array);
	} // insertRecords
	
	/**
	 * Creates a blank Table with the specified type table name, column names,
	 * and type mappings.
	 *
	 * @param tableName
	 *  the name of the new table.
	 * @param columnNames
	 *  a <code>String[]</code> of the column names.
	 * @param typeMap
	 *  a <code>HashMap<Integer, String></code> where the integer is used to
	 *  decide which String should be used to decide the SQL type of the
	 *  corresponding column index.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public void insertTable
	(String tableName, String[] columnNames, HashMap<Integer, String> typeMap)
	throws SQLException {
		
		update("DROP TABLE IF EXISTS " + tableName);
		StringBuilder builder = new StringBuilder(
		 "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
		 PRIMARY_K + " " + PRIMARY_V + " NOT NULL AUTO_INCREMENT,");
		
		for(int i = 0; i < typeMap.size(); i++) {
			String line = columnNames[i] + " " + typeMap.get(i) + ",";
			builder.append(line);
		} // End for
		
		builder.append(" PRIMARY KEY (").append(PRIMARY_K).append("))");
		update(builder.toString());
	} // End insertTable
	
	/**
	 * Starts the process of getting user input for information about the table
	 * to be created and uploaded, then summons a gui to collect the input.
	 */
	void insertTableFromGui() throws Exception {
		
		String filePath = JOptionPane.showInputDialog(null, "Open file:");
		if(!filePath.endsWith(".csv")) {
			//Todo: throw new FileNotSupportedException
			return;
		} // End if
		
		Scanner scanner = new Scanner(new File(filePath));
		String[] columns = scanner.nextLine().split(",");
		columns[0] = removeUTF8BOM(columns[0]);
		csvGuiLoad(columns, filePath);
	} // End insertTableFromGui
	
	/**
	 * Checks whether the connection has closed.
	 *
	 * @return true if the connection is close. Otherwise false.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	protected Boolean isClosed() throws SQLException {
		
		return Crud.connection.isClosed();
	} // End isClosed
	
	/**
	 * TODO: David & Uriel
	 *
	 * @param limit TODO: David & Uriel
	 *
	 * @return TODO: David & Uriel
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public Object[][] mostOrderedProducts(int limit)
	throws SQLException {
		
		String query =
		 "select product_id, sum(product_quantity) as totalQuantity from sales " +
		 " group by \nproduct_id order by sum(product_quantity) desc limit " + limit;
		return getRecords(query(query));
	} // End mostOrderedProducts
	
	/**
	 * TODO: David & Uriel
	 *
	 * @param rowResultLimit TODO: David & Uriel
	 *
	 * @return TODO: David & Uriel
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public Object[][] mostValuableCustomers(int rowResultLimit) throws SQLException {
		
		String query =
		 "SELECT cust_email , SUM(sales.product_quantity * sale_price - " +
		 "wholesale_cost) AS revenue FROM sales\n" +
		 "    INNER JOIN\n" +
		 "    inventory i ON sales.product_id = i.product_id\n" +
		 "GROUP BY cust_email ORDER BY revenue DESC LIMIT " + rowResultLimit;
		return getRecords(query(query));
	} // End mostValuableCustomers
	
	/**
	 * TODO: David & Uriel
	 *
	 * @param query TODO: David & Uriel
	 *
	 * @return TODO: David & Uriel
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public ResultSet query(String query) throws SQLException {
		
		Statement st = connection.createStatement(
		 ResultSet.TYPE_SCROLL_INSENSITIVE,
		 ResultSet.CONCUR_UPDATABLE);
		return st.executeQuery(query);
	} // End query
	
	/**
	 * Wraps the given object in quotes if it is a <code>String</code> or
	 * <code>Date</code> object, for sql injection.
	 *
	 * @param columnValue
	 *  the value whose <code>String</code> representation might be surrounded
	 *  with single quotes.
	 *
	 * @return The valid SQL <code>String</code> representation of the object.
	 */
	static String quoteWrap(Object columnValue) {
		
		if(columnValue instanceof String
		   || columnValue instanceof Date) {
			return "'" + columnValue + "'";
		} // End if
		return columnValue.toString();
	} // End quoteWrap
	
	/**
	 * Removes the (BOM byte-order mark) from the beginning of the string.
	 * Helpful for reading from a scanner an removing a BOM from the beginning
	 * of the first line of a .csv file.
	 *
	 * @return a string with no BOM.
	 */
	public static String removeUTF8BOM(String s) {
		
		if(s.startsWith("\uFEFF")) {
			s = s.substring(1);
		} // End if
		return s;
	} // End removeUTF8BOM
	
	/**
	 * Count the number of rows from a SQL query.
	 *
	 * @param resultSet
	 *  the <code>ResultSet</code> Object to count rows from.
	 *
	 * @return the number of rows of in a ResultSet.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	int rowCountResults(ResultSet resultSet) throws SQLException {
		
		resultSet.last();
		return resultSet.getRow();
	} // End rowCountResults
	
	/**
	 * Sets the static variable <code>tableName</code> as the table to make
	 * statements against.
	 */
	public void setWorkingTable(String tableName) {
		
		this.currentTable = DB_NAME + "." + tableName;
	} // End setWorkingTable
	
	/**
	 * Find out the length of a SQL table.
	 *
	 * @return the number of rows in a table.
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	int size() throws SQLException {
		
		ResultSet rs = query("SELECT COUNT(*) FROM " + currentTable);
		rs.next();
		return rs.getInt(1);
	} // End size
	
	/**
	 * Creates a tuple of information to use for SQL insertion.
	 *
	 * @param columnValues
	 *  the values to construct the tuple with.
	 * @param typeMap
	 *  the HashMap<Integer, String> to decide whether or not each value should
	 *  be surrounded in single quotes for SQL injection.
	 *
	 * @return a tuple of values from an array for sql statements
	 */
	public String toValueTuple(Object[] columnValues,
							   HashMap<Integer, String> typeMap) {
		
		if(typeMap == null) {
			throw new NullPointerException();
		} // End if
		
		Object[] values = new Object[columnValues.length];
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < values.length; i++) {
			sb.append(typeMap.get(i).contains("VARCHAR")
					  || typeMap.get(i).contains("DATE") ?
			 quoteWrap(columnValues[i]) : columnValues[i]);
			if(i == values.length - 1) {
				sb.append(")");
			} else {
				sb.append(",");
			} // End if
		} // End for
		
		return sb.toString();
	} // End toValueTuple
	
	/**
	 * Overloaded version of <code>toValueTuple</code> creates a a tuple of
	 * information to use for SQL insertion. This version simply calls the
	 * <code>quoteWrap</code> method on each value instead of using a
	 * HashMap<Integer, String> to decide which values should be surrounded 
	 * with
	 * single quotes.
	 *
	 * @param columnValues
	 *  the values to construct the tuple with.
	 *
	 * @return the sql String representing a tuple of data to insert.
	 */
	public String toValueTuple(Object[] columnValues) {
		
		Object[] values = new Object[columnValues.length];
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < values.length; i++) {
			sb.append(quoteWrap(columnValues[i]));
			if(i == values.length - 1) {
				sb.append(")");
			} else {
				sb.append(",");
			} // End if
		} // End for
		return sb.toString();
	} // End toValueTuple
	
	/**
	 * TODO: David & Uriel
	 *
	 * @param date TODO: David & Uriel
	 * @param limit TODO: David & Uriel
	 * @param isDescending TODO: David & Uriel
	 * @param gui TODO: David & Uriel
	 *
	 * @return TODO: David & Uriel
	 *
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public String topNByCustomer
	(String date, int limit, boolean isDescending, GUI gui)
	throws SQLException {
		
		setWorkingTable("customers");
		update(" DROP TABLE IF EXISTS top_customers");
		String newTableName = "Top_" + limit + "_Customers";
		
		String dateClause;
		if(date != null) {
			dateClause = "WHERE date_accepted = '" + date + "'";
		} else {
			dateClause = "";
		} // End if
		
		String sql =
		 " CREATE TEMPORARY TABLE IF NOT EXISTS " + newTableName +
		 " AS (SELECT customer_email, SUM(sales.product_quantity * " +
		 "(sale_price - wholesale_cost)) AS revenue " +
		 " FROM sales " +
		 
		 dateClause +
		 
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
	} // End topNByCustomer 
	
	/**
	 * TODO: David & Uriel
	 *
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
	} // End topNByDate
	
	public int update(String sql) throws SQLException {
		
		Statement st = connection.createStatement();
		return st.executeUpdate(sql);
	} // End update
	
	/**
	 * Update a row where some column name = some value 
	 * @param columns The columns to be updated
	 * @param values The values to update into the specified columns in a parallel array.
	 * @param identifyingColumn The column to identify which rows should be updated.
	 * @param identifyingValue The value in the identifying column to indicate which rows should be updated.
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public void updateRow
	(String[] columns, Object[] values, Object identifyingColumn, String identifyingValue)
	throws SQLException {
		
		setWorkingTable(currentTable);
		StringBuilder builder = new StringBuilder(
		 "UPDATE " + currentTable + " SET ");
		
		for(int i = 0; i < columns.length; i++) {
			String str = columns[i] + "=" + quoteWrap(values[i]) +
						 (i < columns.length - 1 ? "," : "");
			builder.append(str);
		} // End for
		
		builder.append(" WHERE ").append(identifyingColumn).append("=")
			   .append(quoteWrap(identifyingValue));
		update(builder + "");
	} // End updateRow
	
	/**
	 * Write a table from the database to a csv file 
	 * @param path The file path relative to this project structure, in which to save the file.
	 * @param columns The String[] to make up the column headers of the csv file.
	 * @param results The ResultSet to extract row information from.
	 * @return the csv file.
	 * @throws FileNotFoundException if the file writing or saving causes an error.
	 * @throws SQLException
	 *  if there is an issue with the sql command or connection.
	 */
	public File writeToFile
	(String path, String[] columns, ResultSet results)
	throws FileNotFoundException, SQLException {
		
		if(columns.length == 0) {
			throw new InputMismatchException();
		} // End if
		
		File file = new File(path);
		PrintWriter pw = new PrintWriter(file);
		pw.println(String.join(",", columns));
		
		while(results.next()) {
			for(int i = 0; i < columns.length; i++) {
				pw.print(results.getObject(columns[i]));
				if(i < columns.length - 1) {pw.print(",");}
			} // End for
			pw.println();
		} // End while
		pw.close();
		return file;
	} // End writeToFile
} // End class Crud

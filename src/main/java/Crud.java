import com.mysql.cj.MysqlType;

import javax.mail.MessagingException;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;

class Crud {
	public static final int CUSTOMERS = 2;
	private static String DB_NAME;
	private static String HOST_IP;
	public static final int INVENTORY = 1;
	private static String PORT;
	private static final Pair<String, String> PRIMARY_KEY =
	 new Pair("idx", "int(16)");
	public static final int QUANTITY_SHORTAGE = -2;
	public static final int SALES = 0;
	public static final Map<Integer, String[]> RECORD_STRINGS = Map.ofEntries(
	 Map.entry(SALES, new String[] {"sales", "order_id"}),
	 Map.entry(INVENTORY, new String[] {"inventory", "product_id"}),
	 Map.entry(CUSTOMERS, new String[] {"customers", "email"})
	);
	public static final int UNKNOWN_PRODUCT = -1;
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
		//JOptionPane.showMessageDialog(null, "Connection OK with " + getURL
		// ());
	}
	
	/** Creates a .csv-compatible line from an ArrayList<String> */
	public String arrayToCSV
	(String[] array) {
		String a = Arrays.toString(array);
		return a.substring(1, a.length() - 1);
	}
	
	/**
	 * Creates a gui to get user input on a new table to be uploaded to MySQL
	 * database.
	 */
	private uploadCsvGui csvGuiLoad(String[] columns, String fileName) {
		uploadCsvGui gui = new uploadCsvGui(columns, fileName, this);
		gui.invoke();
		return gui;
	}
	
	/** Deletes all records from a table, but the table remains */
	public int deleteAllRecords() throws SQLException {
		return update("DELETE FROM " + currentTable);
	}
	
	/** Delete the record in the specified table */
	public int deleteRecord
	(String table, String idColumn, Object idValue)
	throws SQLException {
		return updateF("DELETE FROM %s WHERE %s = %s",
		 table, idColumn, quoteWrap(idValue));
	}
	
	/** Deletes an entire table */
	public void deleteTable(String tableName) throws SQLException {
		update("DROP TABLE IF EXISTS " + tableName);
	}
	
	private boolean exists(String columnName, Object columnValue)
	throws SQLException {
		ResultSet rs = queryF(
		 "SELECT EXISTS(SELECT * FROM %s WHERE %s = %s);", currentTable,
		 columnName, quoteWrap(columnValue));
		return rs.getFetchSize() > 0;
	}
	
	/** Find a specific record */
	public Object[] find(String idValue, String idColumnName)
	throws SQLException {
		String sql = format("select * from %s where %s = '%s';",
		 currentTable, idColumnName, idValue);
		var result = queryF(sql);
		Object[] record = new Object[result.getMetaData().getColumnCount()];
		while(result.next()) {
			for(int i = 0; i < record.length; i++) {
				record[i] = result.getObject(i + 1);
			}
		}
		return record;
	}
	
	/** Format a string for sql statement */
	public String format(String format, Object... args) {
		return String.format(format, args);
	}
	
	/** Get a ResultSet of an entire table */
	public ResultSet getAllRecords()
	throws SQLException {
		return query("SELECT * FROM " + currentTable);
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
		String sql =
		 format("SELECT count(*) AS %s FROM information_schema.columns WHERE" +
				" table_name = '%s';",
		  tableName, tableName);
		ResultSet rs = queryF(sql);
		if(rs.next()) {
			return rs.getInt(1);
		}
		return -1;
	}
	// first_name, last_name]
	
	/** Gets an arraylist of the column names of a specific table */
	public String[] getColumnNames() throws SQLException {
		String sql = format(
		 "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
		 " WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'", DB_NAME,
		 currentTable);
		ResultSet rs = query(sql);
		ArrayList<String> list = new ArrayList<>();
		while(rs.next()) {
			if(!rs.getString(1).equals("idx")) {
				list.add(rs.getString(1));
			}
		}
		String[] columnNames = new String[list.size()];
		Iterator<String> it = list.iterator();
		for(int i1 = 0; it.hasNext(); i1++) {
			columnNames[i1] = it.next();
		}
		return columnNames;
	}
	
	/** Gets an arrayList of column types from a table */
	public String[] getColumnTypes(String tableName)
	throws SQLException {
		ResultSet rs = query("select * from " + tableName + " where 1<0");
		ResultSetMetaData data = rs.getMetaData();
		data.getColumnCount();
		String[] list = new String[data.getColumnCount()];
		for(int i = 0; i < list.length; i++) {
			list[i] = (MysqlType.getByJdbcType(data.getColumnType(i + 1)) +
					   "");
		}
		return list;
	}
	
	/** Create the tuple of column names as a String to be sent as sql code. */
	public String getColumnsTuple(String[] columnNames) {
		if(columnNames.length == 0) {return null;}
		String str = Arrays.toString(columnNames);
		return "(" + str.substring(1, str.length() - 1) + ")";
		//StringBuilder sb = new StringBuilder("(");
		//int i = 0;
		//for(; i < columnNames.length - 1; i++) {
		//	sb.append(columnNames[i]).append(",");
		//}
		//return sb.append(columnNames[i]).append(")").toString();
	}// (email, first_name, last_name)    array.toString() -> [email,
	
	/**
	 * Helper method to clean up code when concatenating commas for sql code
	 * .
	 */
	private static String getComma(int length, int i, String lastChar) {
		if(i < length - 1) {return ", ";}
		return lastChar + "";
	}
	
	/** Return the database name */
	public Object getDatabaseName() {
		return DB_NAME;
	}
	
	/** Get a ResultSet of an entire table */
	public ResultSet getRecord(String columnVal, String columnName)
	throws SQLException {
		return query("SELECT * FROM " + currentTable + " where " + columnName +
					 " equals " + columnVal);
	} // End getRecord
	
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
	
	/** Get the sql string representing the name of a java data type */
	public String getType(String name) {
		return uploadCsvGui.J_TO_SQL2.get(name);
	}
	
	/** Get the URL by joining static variable together. */
	private static String getURL() {
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
	}
	
	protected String getWorkingTable() {
		return this.currentTable;
	}
	
	/** Create a new record in the specified table from order information. */
	public void insertFromOrder(TransactionItem od, int table)
	throws SQLException {
		String[] recordStrings = RECORD_STRINGS.get(table);
		setWorkingTable(recordStrings[0]);
		if(!exists(recordStrings[1], od.getMatchValue(table))) {
			insertRecords(
			 getColumnNames(),
			 new Object[][] {od.toArray(table)});
		}
	}
	
	/** Insert one or more new records into a table from a 2d array. */
	public int insertRecords(String[] columnNames, Object[][] tableValues)
	throws InputMismatchException {
		if(tableValues.length == 0 || columnNames.length == 0) {
			throw new InputMismatchException();
		}
		StringBuilder sb = new StringBuilder(format(
		 "INSERT INTO %s %s VALUES",
		 currentTable, getColumnsTuple(columnNames)));
		for(int i = 0; i < tableValues.length; i++) {
			sb.append(toValueTuple(tableValues[i]));
			sb.append(i == tableValues.length - 1 ? ";" : ",");
		}
		try {
			return update(sb + "");
		}
		catch(Exception e) {
			System.out.println(sb + " must have a huge email address...");
			return -1;
		}
	}
	
	/** Creates a blank Table */
	public void insertTable
	(String tableName, String[] columnNames, HashMap<Integer, String> typeMap)
	throws SQLException {
		deleteTable(tableName);
		StringBuilder sb = new StringBuilder(format(
		 "CREATE TABLE %s(%s %s NOT NULL AUTO_INCREMENT,",
		 tableName, PRIMARY_KEY.getKey(),
		 PRIMARY_KEY.getValue()));
		int i = 0;
		for(; i < typeMap.size(); i++) {
			sb.append(format("%s %s,",
			 columnNames[i].trim(), typeMap.get(i)));
		}
		sb.append(format(" PRIMARY KEY (%s));", PRIMARY_KEY.getKey()));
		update(sb.toString());
	}
	
	/**
	 * Starts the process of getting user input for information about the table
	 * to be created and
	 * uploaded, then summons a gui to collect the input.
	 */
	void insertTableFromGui() throws Exception {
		String filePath = JOptionPane.showInputDialog(null, "Open file:");
		int[] count = null;
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
	
	/**
	 * Return an integer indicating if the order can be processed, and if not,
	 * why
	 */
	public int isProcessableOrder(TransactionItem transactionItem)
	throws SQLException, MessagingException {
		setWorkingTable("inventory");
		String productId = transactionItem.getProductId();
		ResultSet rs =
		 queryF("select quantity from %s where product_id = '%s';",
		  currentTable, productId);
		if(!rs.next()) {
			transactionItem.setResultString(
			 transactionItem.getProductId() + " : unknown product id");
			System.out.println(
			 transactionItem.getProductId() + " : unknown product id");
			return UNKNOWN_PRODUCT;
		}
		int currentQuantity = rs.getInt(1);
		if(currentQuantity < transactionItem.getQuantity()) {
			transactionItem.setResultString(format("%s : %d - %d",
			 transactionItem.getProductId(), transactionItem
			  .getQuantity(),
			 transactionItem.getQuantity() + currentQuantity));
			return QUANTITY_SHORTAGE;
		}
		transactionItem.setResultString(
		 transactionItem.getProductId() + " : " +
		 transactionItem.getQuantity());
		transactionItem.setCurrentQuantity(currentQuantity);
		return currentQuantity;
	}
	
	/** Sends a sql query string */
	public ResultSet query(String query) throws SQLException {
		Statement st = connection.createStatement(
		 ResultSet.TYPE_SCROLL_INSENSITIVE,
		 ResultSet.CONCUR_UPDATABLE);
		return st.executeQuery(query);
	}
	
	/** Formats, then sends a sql query string */
	public ResultSet queryF(String format, Object... args) throws SQLException {
		return query(format(format, args));
	}
	
	/** Wraps the given object in quotes if it is a string */
	static String quoteWrap(Object columnValue) {
		if(columnValue instanceof String
		   // quoteWrap(999) -> 999     ||   quoteWrap(07-05-1990) ->
		   // "07-05-1990"
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
	
	/** Restock a given product. */
	public void restock(String productId) throws SQLException {
		updateRow(new String[] {"quantity"}, new Object[] {500}, "product_id",
		 productId,
		 "inventory");
	}
	
	public Object[][] resultsToArray(ResultSet rs) throws SQLException {
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
	
	/** retrieve the number of rows of in a ResultSet. */
	int rowCountResults(ResultSet rs) throws SQLException {
		rs.last();
		return rs.getRow();
	}
	
	/** retrieve the number of rows of a table. */
	int rowCountTable() throws SQLException {
		ResultSet rs = query("SELECT COUNT(*) FROM " + currentTable);
		try {
			rs.next();
			return rs.getInt(1);
		}
		catch(SQLSyntaxErrorException sex) {
			System.out.println("the table does not exist");
			return -1;
		}
	}
	
	/** Updates a product's quantity from an order */
	public void setQuantityFromOrder(TransactionItem transactionItem)
	throws SQLException {
		int newQuantity =
		 transactionItem.getCurrentQuantity() - transactionItem
		  .getQuantity();
		updateF("UPDATE %s SET quantity = %s WHERE product_id = '%s';",
		 currentTable, newQuantity, transactionItem.getProductId());
	}
	
	/**
	 * Sets the static variable <code>tableName</code> as the table to make
	 * statements against.
	 */
	public void setWorkingTable(String tableName) {
		this.currentTable = tableName;
	}
	
	;
	//public static final String[] SALES_COLUMNS = new String[]
	// {"product_id", "quantity", "wholesale_cost", "sale_price",
	// "supplier_id"};;
	//public static final String[] CUSTOMERS_COLUMNS = new String[]
	// {"product_id", "quantity", "wholesale_cost", "sale_price",
	// "supplier_id"};;
	
	private boolean shouldQuote(String s) {
		return s.contains("VARCHAR")
			   || s.contains("DATE");
	}
	
	/** retrieve the number of rows of a column. */
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
			if(shouldQuote(typeMap.get(i))) {
				array[i] = "'" + array[i] + "'";
			}
			sb.append(array[i]);
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
	
	public Object[][] mostOrderedProducts
	 (int limit)
	throws SQLException {
		String query =
		 "select product_id, sum(quantity) as totalQuantity from sales " +
		 " group by \nproduct_id order by sum(quantity) desc limit " + limit;
		return resultsToArray(query(query));
	}
	
	
	
	/**
	 * 10 best customers (by dollar amount)
	 */
	public Object[][] topNByDate(String date, String columnName,
								 int limit, boolean isDescending,
								 String orderArg) throws SQLException {
		return resultsToArray(query(
		 "Select * FROM sales WHERE " + columnName + " = '" + date +
		 "' ORDER BY " + orderArg + (isDescending ? " DESC" : "ASC") +
		 " LIMIT " + limit));
	}
	
	public int update(String sql) throws SQLException {
		Statement st = connection.createStatement(
		 ResultSet.TYPE_SCROLL_INSENSITIVE,
		 ResultSet.CONCUR_UPDATABLE);
		return st.executeUpdate(sql);
	}
	
	/** Format an sql executeUpdate() statement */
	public int updateF(String format, Object... args) throws SQLException {
		return update(format(format, args));
	}
	
	/** Update a row where some column name = some value */
	public void updateRow
	(String[] columns, Object[] values, Object columnName, String columnValue,
	 String tableName)
	throws SQLException {
		setWorkingTable(tableName);
		StringBuilder sf =
		 new StringBuilder(format("UPDATE %s SET ", tableName));
		for(int i = 0; i < columns.length; i++) {
			String column = columns[i];
			String value = quoteWrap(values[i]);
			String comma = getComma(columns.length, i, "");
			sf.append(format("%s = %s%s", column, value, comma));
		}
		sf.append(format(" where %s=%s", columnName, quoteWrap(columnValue)));
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

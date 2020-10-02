import com.mysql.cj.MysqlType;

import javax.mail.MessagingException;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

class Crud {
	public static final int QUANTITY_SHORTAGE = - 2;
	public static final int UNKNOWN_PRODUCT = - 1;
	private static final Pair<String, String> PRIMARY_KEY = new Pair("idx", "int(16)");
	private static String HOST_IP;
	private static String PORT;
	private static String DB_NAME;
	private static Connection connection;
	private String tableName = "inventory";
	public static final int SALES = 0;
	public static final int INVENTORY = 1;
	public static final int CUSTOMERS = 2;
	public static final Map<Integer, String[]> RECORD_STRINGS = Map.ofEntries(
	 Map.entry(SALES, new String[] {"sales", "order_id"}),
	 Map.entry(INVENTORY, new String[] {"inventory", "product_id"}),
	 Map.entry(CUSTOMERS, new String[] {"customers", "email"})
	);
	
	/**
	 * Class that facilitates a connection to a database, and carries out CRUD
	 * operations
	 */
	public Crud
	(String userName, String passWord, String hostIP, String port, String schema)
	throws SQLException, ClassNotFoundException {
		HOST_IP = hostIP;
		PORT = port;
		DB_NAME = schema;
		Class.forName("com.mysql.cj.jdbc.Driver");
		connection = DriverManager.getConnection(getURL(), userName, passWord);
		//JOptionPane.showMessageDialog(null, "Connection OK with " + getURL());
	}
	
	/** Creates a .csv-compatible line from an ArrayList<String> */
	public String arrayToCSV
	(String[] array) {
		String a = Arrays.toString(array);
		return a.substring(1, a.length() - 1);
	}

	
	/** Creates a gui to get user input on a new table to be uploaded to MySQL database. */
	private uploadCsvGui csvGuiLoad(String[] columns, String fileName) {
		uploadCsvGui gui = new uploadCsvGui(columns, fileName, this);
		gui.invoke();
		return gui;
	}
	
	/** Deletes all records from a table, but the table remains */
	public int deleteAllRecords(String table) throws SQLException {
		return update("DELETE FROM " + table);
	}
	
	/** Delete the record in the specified table */
	public int deleteRecord
	(String table, String idColumn, Object idValue)
	throws SQLException {
		return updateF("DELETE FROM %s WHERE %s = %s",
		 table, idColumn, idValue);
	}
	
	/** Deletes an entire table */
	public void deleteTable(String tableName) throws SQLException {
		update("DROP TABLE IF EXISTS " + tableName);
	}
	
	/** Find a specific record */
	public Object[] find(String idValue, String idColumnName) throws SQLException {
		String sql = format("select * from %s where %s = '%s';",
		 tableName, idColumnName, idValue);
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
		return query("SELECT * FROM " + tableName);
	}
	
	/** Gets the number of columns in a table */
	public int getColumnCount(String tableName) throws SQLException {
		setWorkingTable(tableName);
		String sql =
		 format("SELECT count(*) AS %s FROM information_schema.columns WHERE table_name = '%s';",
		  tableName, tableName);
		ResultSet rs = queryF(sql);
		while(rs.next()) {
			return rs.getInt(1);
		}
		return - 1;
	}
	
	/** Gets an arraylist of the column names of a specific table */
	public String[] getColumnNames() throws SQLException {
		String sql = format(
		 "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
		 " WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'", DB_NAME, tableName);
		ResultSet rs = query(sql);
		ArrayList<String> temp = new ArrayList<>();
		int size = 0;
		while(rs.next()) {
			if(! rs.getString(1).equals("idx")) {
				temp.add(rs.getString(1));
				size++;
			}
		}
		String[] columnNames = new String[size];
		Iterator<String> it = temp.iterator();
		for(int i1 = 0; i1 < size; i1++) {
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
			list[i] = (MysqlType.getByJdbcType(data.getColumnType(i + 1)) + "");
		}
		return list;
	}
	
	/** Create the tuple of column names as a String to be sent as sql code. */
	public String getColumnsTuple(String[] columnNames) {
		if(columnNames.length == 0) {return null;}
		StringBuilder sb = new StringBuilder("(");
		int i = 0;
		for(; i < columnNames.length - 1; i++) {
			sb.append(columnNames[i]).append(",");
		}
		return sb.append(columnNames[i]).append(")").toString();
	}
	
	/** Helper method to clean up code when concatenating commas for sql code. */
	private static String getComma(int length, int i, String lastChar) {
		if(i < length - 1) {return ", ";}
		return lastChar + "";
	}
	
	/** Return the database name */
	public Object getDatabaseName() {
		return DB_NAME;
	}
	
	/** Get an array with all the table names */
	public String[] getTableNames() throws SQLException {
		ResultSet rs = query("SHOW tables");
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
	public String getType(String name) { return uploadCsvGui.J_TO_SQL2.get(name); }
	
	/** Get the URL by joining static variable together. */
	private static String getURL() {
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
	}
	
	/** Create a new record in the specified table from order information. */
	public void insertFromOrder(Order od, int table) throws SQLException {
		String[] recordStrings = RECORD_STRINGS.get(table);
		setWorkingTable(recordStrings[0]);
		if(!exists(recordStrings[1], od.getMatchValue(table))) {
			insertRecords(getColumnNames(), new Object[][] {od.toArray(table)});
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
		 tableName, getColumnsTuple(columnNames)));
		for(int i = 0; i < tableValues.length; i++) {
			if(tableValues[i].length != columnNames.length) {
				throw new InputMismatchException();
			}
			sb.append(toValueTuple(tableValues[i]));
			sb.append(i == tableValues.length - 1 ? ";" : ",");
		}
		try {
			return update(sb + "");
		}
		catch(Exception e) {
			System.out.println(sb + " must have a huge email address...");
			return - 1;
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
	 * Starts the process of getting user input for information about the table to be created and
	 * uploaded, then summons a gui to collect the input.
	 */
	private void insertTableFromGui() throws Exception {
		String filePath = JOptionPane.showInputDialog(null, "Open file:");
		int[] count = null;
		if(! filePath.endsWith(".csv")) {
			//Todo: throw new FileNotSupportedException
			return;
		}
		
		Scanner scanner = new Scanner(new File(filePath));
		String[] columns = scanner.nextLine().split(",");
		columns[0] = removeUTF8BOM(columns[0]);
		csvGuiLoad(columns, filePath);
	}
	
	/** Return an integer indicating if the order can be processed, and if not, why */
	public int isProcessableOrder(Order order) throws SQLException, MessagingException {
		setWorkingTable("inventory");
		String productId = order.getProductId();
		ResultSet rs = queryF("select quantity from %s where product_id = '%s';",
		 tableName, productId);
		if(! rs.next()) {
			order.setResultString(order.getProductId() + " : unknown product id");
			System.out.println(order.getProductId() + " : unknown product id");
			return UNKNOWN_PRODUCT;
		}
		int currentQuantity = rs.getInt(1);
		if(currentQuantity < order.getQuantity()) {
			order.setResultString(format("%s : %d - %d",
			 order.getProductId(), order.getQuantity(), order.getQuantity() + currentQuantity));
			return QUANTITY_SHORTAGE;
		}
		order.setResultString(order.getProductId() + " : " + order.getQuantity());
		order.setCurrentQuantity(currentQuantity);
		return currentQuantity;
	}
	
	/** Sends a sql query string */
	public ResultSet query(String query) throws SQLException {
		return connection.createStatement().executeQuery(query);
	}
	
	/** Formats, then sends a sql query string */
	public ResultSet queryF(String format, Object... args) throws SQLException {
		return query(format(format, args));
	}
	
	/** Wraps the given object in quotes if it is a string */
	static String quoteWrap(Object columnValue) {
		if(columnValue instanceof String 
		   || columnValue instanceof Date) {
			return "'" + columnValue + "'";
		}
		return columnValue.toString();
	}
	
	private boolean exists(String columnName, Object columnValue) throws SQLException {
		ResultSet rs = queryF(
		 "SELECT EXISTS(SELECT * FROM %s WHERE %s = %s);", tableName,
		 columnName, quoteWrap(columnValue));
		return rs.getFetchSize() > 0;
	}
	
	/** Restock a given product. */
	public void restock(String productId) throws SQLException {
		updateRow(new String[] {"quantity"}, new Object[] {500}, "product_id", productId,
		 "inventory");
	}
	
	/** Removes the (BOM byte-order mark) from the beginning of the string. */
	private static String removeUTF8BOM(String s) {
		if(s.startsWith("\uFEFF")) {
			s = s.substring(1);
		}
		return s;
	}
	
	/** retrieve the number of rows of a column. */
	int size() throws SQLException {
		ResultSet rs = query("SELECT COUNT(*) FROM " + tableName);
		rs.next();
		return rs.getInt(1);
	}
	
	/** Updates a product's quantity from an order */
	public void setQuantityFromOrder(Order order) throws SQLException {
		int newQuantity = order.getCurrentQuantity() - order.getQuantity();
		updateF("UPDATE %s SET quantity = %s WHERE product_id = '%s';",
		 tableName, newQuantity, order.getProductId());
	}
	
	/** Sets the static variable <code>tableName</code> as the table to make statements against. */
	public void setWorkingTable(String tableName) { this.tableName = tableName; }
	
	/** Makes a tuple of values from an array for sql statements */
	public String toValueTuple(Object[] array, HashMap<Integer, String> typeMap) {
		if(typeMap == null) {
			throw new NullPointerException();
		}
		Object[] values = new Object[array.length];
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < values.length; i++) {
			if(shouldQuote(typeMap.get(i))){
				array[i] = "'" + array[i] + "'";
			}
			sb.append(array[i]);
			if(i == values.length - 1) {
				sb.append(")");
			} else {sb.append(",");}
		}
		return sb.toString();
	}
	
	private boolean shouldQuote(String s) {
		return s.contains("VARCHAR")
		|| s.contains("DATE");
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
	
	/** Send an sql executeUpdate() statement */
	public int update(String sql) throws SQLException {
		return connection.createStatement().executeUpdate(sql);
	}
	
	/** Format an sql executeUpdate() statement */
	public int updateF(String format, Object... args) throws SQLException {
		return update(format(format, args));
	}
	
	/** Update a row where some column name = some value */
	public void updateRow
	(String[] columns, Object[] values, Object columnName, String columnValue, String tableName)
	throws SQLException {
		setWorkingTable(tableName);
		StringBuilder sf = new StringBuilder(format("UPDATE %s SET ", tableName));
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

	/**Get a ResultSet of an entire table */
	public ResultSet getRecord(String columnVal, String columnName)
			throws SQLException
	{
		return query("SELECT * FROM " + tableName + " where " + columnName +
				" equals " + columnVal);
	} // End getRecord

	protected Boolean isClosed() throws SQLException {
		return Crud.connection.isClosed();
	}

	protected String getWorkingTable() {
		return this.tableName;
	}
	
	public static final String[] INVENTORY_COLUMNS = new String[] {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};;
	//public static final String[] SALES_COLUMNS = new String[] {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};;
	//public static final String[] CUSTOMERS_COLUMNS = new String[] {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};;
}

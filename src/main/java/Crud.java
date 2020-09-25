import com.mysql.cj.MysqlType;

import javax.mail.MessagingException;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

class Crud {
	public static final int QUANTITY_SHORTAGE = - 2;
	public static final int UNKNOWN_PRODUCT = - 1;
	private static final Pair<String, String> PRIMARY_KEY = new Pair("idx", "int(16)");
	private static String HOST_IP;
	private static String PORT;
	private static String DB_NAME;
	private static Connection connection;
	private String tableName = "inventory";
	
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
	
	/** creates a .csv-compatible line from an ArrayList<String> */
	public String arrayToCSV
	(String[] array) {
		String a = Arrays.toString(array);
		return a.substring(1, a.length() - 1);
	}
	
	/** Creates a gui to get user input on a new table to be uploaded to MySQL database. */
	uploadCsvGui csvGuiLoad(String[] columns, String fileName) {
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
	
	/** deletes an entire table */
	public void deleteTable(String tableName) throws SQLException {
		update("DROP TABLE IF EXISTS " + tableName);
	}
	
	/** find a specific record */
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
	
	/** format a string for sql statement */
	public String format(String format, Object... args) {
		return String.format(format, args);
	}
	
	/** get a ResultSet of an entire table */
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
		// Gets an arraylist of the column names of a specific table
		String sql = format(
		 "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
		 " WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'", DB_NAME, tableName);
		ResultSet rs = query(sql);
		ArrayList<String> temp = new ArrayList<>();
		int i = 0;
		for(; rs.next(); i++) {
			temp.add(rs.getString(1));
		}
		String[] columnNames = new String[i];
		Iterator<String> it = temp.iterator();
		for(int i1 = 0; i1 < columnNames.length; i1++) {
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
	
	/** create the tuple of column names as a String to be sent as sql code. */
	public String getColumnsTuple(String[] columnNames) {
		if(columnNames.length == 0) {return null;}
		StringBuilder sb = new StringBuilder("(");
		int i = 0;
		for(; i < columnNames.length - 1; i++) {
			sb.append(columnNames[i]).append(",");
		}
		return sb.append(columnNames[i]).append(")").toString();
	}
	
	/** helper method to clean up code when concatenating commas for sql code. */
	private static String getComma(int length, int i, String lastChar) {
		if(i < length - 1) {return ", ";}
		return lastChar + "";
	}
	
	/** return the database name */
	public Object getDatabaseName() {
		return DB_NAME;
	}
	
	/** get an array with all the table names */
	public String[] getTableNames() throws SQLException {
		ResultSet rs = query("SHOW tables");
		int count = 0;
		ArrayList<String> temp = new ArrayList<>();
		while(rs.next()){
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
	
	/** get the sql string representing the name of a java data type */
	public String getType(String name) { return uploadCsvGui.J_TO_SQL2.get(name); }
	
	/** get the URL by joining static variable together. */
	private static String getURL() {
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
	}
	
	/** Insert one or more new records into a table */
	public int insertRecords(String[] columnNames, String[][] values)
	throws SQLException {
		StringBuilder sb = new StringBuilder(format(
		 "INSERT INTO %s %s VALUES",
		 tableName, getColumnsTuple(columnNames)));
		for(int i = 0; i < values.length; i++) {
			sb.append(toValueTuple(values[i], null));
			sb.append(i == values.length - 1 ? ";" : ",");
		}
		return update(sb + "");
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
		String productId = order.getProductId();
		ResultSet rs = queryF("select quantity from %s where product_id = '%s';",
		 tableName, productId);
		if(! rs.next()) {
			order.setResultString(order.getProductId() + " : unknown product id");
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
	
	/** wraps the given object in quotes if it is a string */
	private static String quoteWrap(Object columnValue) {
		if(columnValue instanceof String) {return "'" + columnValue + "'";}
		return columnValue.toString();
	}
	
	/** Removes the (BOM byte-order mark) from the beginning of the string. */
	private static String removeUTF8BOM(String s) {
		if(s.startsWith("\uFEFF")) {
			s = s.substring(1);
		}
		return s;
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
	public String toValueTuple(String[] array, HashMap<Integer, String> typeMap) {
		Object[] values = new Object[array.length];
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < values.length; i++) {
			if(typeMap != null && typeMap.get(i).contains("CHAR")) {
				array[i] = "'" + array[i] + "'";
			}
			sb.append(array[i]);
			if(i == values.length - 1) {
				sb.append(")");
			}
			else {sb.append(",");}
		}
		return sb.toString();
	}
	
	/**send an sql executeUpdate() statement*/
	public int update(String sql) throws SQLException {
		return connection.createStatement().executeUpdate(sql);
	}
	
	/**format an sql executeUpdate() statement*/
	public int updateF(String format, Object... args) throws SQLException {
		return update(format(format, args));
	}
	
	/**update a row where some column name = some value*/
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
}

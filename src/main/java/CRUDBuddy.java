import com.mysql.cj.MysqlType;

import javax.mail.MessagingException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;
import java.util.*;

import static java.util.Map.entry;

class CRUDBuddy {
	public static final int QUANTITY_SHORTAGE = - 2;
	public static final int UNKNOWN_PRODUCT = - 1;
	private static final Pair<String, String> PRIMARY_KEY = new Pair("idx", "int(16)");
	private static String HOST_IP;
	private static String PORT;
	private static String DB_NAME;
	private static HashMap<Integer, String> typeMap;
	private static Connection connection;
	private String tableName = "inventory";
	
	/**
	 * Class that facilitates a connection to a database, and carries out CRUD
	 * operations
	 */
	public CRUDBuddy
	(String userName, String passWord, String hostIP, String port, String schema)
	throws SQLException, ClassNotFoundException {
		HOST_IP = hostIP;
		PORT = port;
		DB_NAME = schema;
		Class.forName("com.mysql.cj.jdbc.Driver");
		connection = DriverManager.getConnection(getURL(), userName, passWord);
		//JOptionPane.showMessageDialog(null, "Connection OK with " + getURL());
	}
	
	// Creates a blank Table
	public void createBlankTable
	(String tableName, String[] columnNames, HashMap<Integer, String> typeMap)
	throws SQLException {
		deleteTable(tableName);
		StringFormat sb = new StringFormat(
		 "CREATE TABLE %s(%s %s NOT NULL AUTO_INCREMENT,",
		 tableName, PRIMARY_KEY.getKey(),
		 PRIMARY_KEY.getValue());
		int i = 0;
		for(; i < typeMap.size(); i++) {
			sb.appendf("%s %s,",
			 columnNames[i].trim(), typeMap.get(i));
		}
		sb.appendf(" PRIMARY KEY (%s));", PRIMARY_KEY.getKey());
		update(sb.toString());
	}
	
	// Gets an arraylist of the column names of a specific table
	public ArrayList<String> readColumnNames
	(String dbName, String tableName)
	throws SQLException {
		StringFormat sf = new StringFormat(
		 "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
		 " WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'", dbName, tableName);
		ResultSet rs = query(sf.toString());
		ArrayList<String> columnNames = new ArrayList<>();
		while(rs.next()) {
			columnNames.add(rs.getString(1));
		}
		return columnNames;
	}
	
	// Gets an arrayList of column types from a table
	public ArrayList<String> readColumnTypes
	(String tableName)
	throws SQLException {
		ArrayList<String> list = new ArrayList<>();
		ResultSet rs = query("select * from " + tableName + " where 1<0");
		ResultSetMetaData data = rs.getMetaData();
		 data.getColumnCount();
		for(int i = 0; i < data.getColumnCount(); i++) {
			if(!data.isAutoIncrement(i + 1)) {
				list.add(MysqlType.getByJdbcType(data.getColumnType(i)) + "");
			}
		}
		return list;
	}
	
	// Queries a table for the specified criteria. The key is a column name (String), and the 
	// value is
	// the value from that cell in the table.
	public HashMap<String, Object> readColumnValues
	(String tableName, String[] columnNames, String idValue, String idColumnName)
	throws SQLException {
		if(columnNames.length == 0) {return null;}
		StringFormat sf = new StringFormat("SELECT %s FROM %s WHERE %s = '%s'",
		 arrayToCSV(columnNames), tableName,
		 idColumnName, idValue);
		ResultSet rs = query(sf.toString());
		HashMap<String, Object> objectMap = new HashMap<>();
		while(rs.next()) {
			for(int i = 0; i < columnNames.length; i++) {
				objectMap.put(columnNames[i], rs.getObject(columnNames[i]));
				System.out.println(i);
			}
		}
		return objectMap;
	}
	
	// get a ResultSet of an entire table
	public ResultSet readAllRecords(String table)
	throws SQLException {
		return query("SELECT * FROM " + table);
	}
	
	// Write a table from the database to a file
	public File writeToFile
	(String path, ArrayList<String> columns, ResultSet results)
	throws FileNotFoundException, SQLException {
		boolean isIndexed = PRIMARY_KEY.getKey().equals("idx");
		List<String> newColumns;
		int start;
		if(columns.size() == 0) {throw new InputMismatchException();}
		start = isIndexed ? 1 : 0;
		newColumns = columns.subList(start, columns.size());
		File file = new File(path);
		PrintWriter pw = new PrintWriter(file);
		pw.println(String.join(",", newColumns));
		while(results.next()) {
			Iterator<String> it = newColumns.iterator();
			while(it.hasNext()) {
				pw.print(results.getObject(it.next()));
				if(it.hasNext()) {pw.print(",");}
			}
			pw.println();
		}
		pw.close();
		return file;
	}
	
	// creates a .csv-compatible line from an ArrayList<String>
	public String arrayToCSV
	(String[] array) {
		String a = Arrays.toString(array);
		return a.substring(1, a.length() - 1);
	}
	
	// overloaded version takes arraylist
	public String arrayToCSV
	(ArrayList<String> array) {
		String a = array.toString();
		return a.substring(1, a.length() - 1);
	}
	
	// updates a record
	public void updateRow
	(String tableName, String[] columnNames, String[] newValues, String idValue,
	 String idColumnName, boolean[] isString, boolean idIsString) throws SQLException {
		setWorkingTable(tableName);
		idValue = quoteWrap(idValue, idIsString);
		for(int i = 0; i < newValues.length; i++) {
			String newValue = quoteWrap(newValues[i], isString[i]);
			updateF("UPDATE %s SET %s = %s WHERE %s = %s;",
			 tableName, columnNames[i], newValue,
			 idColumnName, idValue);
		}
	}
	
	// overloaded version of above takes different parameters
	public void updateRow
	(String[] columns, Object[] values, Object id, String idValue, String tableName)
	throws SQLException {
		setWorkingTable(tableName);
		StringFormat sf = new StringFormat("UPDATE %s SET ", tableName);
		for(int i = 0; i < columns.length; i++) {
			String column = columns[i];
			String value = quoteWrap(values[i]);
			String comma = getComma(columns.length, i, "");
			sf.appendf("%s = %s%s", column, value, comma);
		}
		String idx = quoteWrap(id);
		sf.appendf(" where %s=%s", id, quoteWrap(idValue));
		update(sf + "");
	}
	
	// creates one sql string for an entire csv file, to create and populate a table.
	private void batchSqlString
	(String tableName, String[] columns, Scanner scanner)
	throws SQLException {
		Statement statement = connection.createStatement();
		if(typeMap != null) {
			createBlankTable(tableName, columns, typeMap);
			
			StringFormat sf = new StringFormat("INSERT INTO %s %s"
			 , tableName, getcolumnTuple(columns));
			String sqlDeclaration = sf.toString();
			
			if(scanner.hasNextLine()) {
				scanner.nextLine();
				int MAX_LOOPS = 100000;
				int i = 1;
				for(; i < MAX_LOOPS && scanner.hasNextLine(); i++) {
					
					String[] line = scanner.nextLine().split(",");
					String nextInsertion = getInsertionString(line);
					boolean isLastIteration = i == MAX_LOOPS - 1;
					sf.append(nextInsertion);
					
					if(i == MAX_LOOPS - 1) {sf.append(";");}
					else {sf.append(",");}
					
					if(isLastIteration) {
						statement.executeUpdate(sf.toString());
						i = 0;
						sf.setLength(0);
						sf.append(sqlDeclaration);
					}
				}
				if(i > 1) {
					sf.replace(sf.length() - 1, sf.length(), ";");
					statement.executeUpdate(sf.toString());
				}
			}
			scanner.close();
			
			JOptionPane.showMessageDialog(null, format(
			 "The csv file has been exported to %s in the %s database.",
			 tableName, DB_NAME));
		}
	}
	
	//Creates a gui to get user input on a new table to be uploaded to MySQL database.
	private void guiUpload(String[] columns, String fileName) {
		GridBagConstraints constraints = new GridBagConstraints();
		int gridy = 0;
		GridBagLayout layout = new GridBagLayout();
		
		JFrame frame = new JFrame();
		JScrollPane scrollPane = new JScrollPane();
		JPanel panel = new JPanel(layout);
		
		Object[] boxOptions = J_TO_SQL.values().toArray();
		
		JRadioButton[] radioButtons = new JRadioButton[columns.length + 1];
		JLabel primaryColumnLabel = new JLabel("Primary Column:");
		
		JComboBox[] boxes = new JComboBox[columns.length];
		JLabel[] labels = new JLabel[columns.length];
		
		JTextField fileField = new JTextField(fileName);
		JLabel fileLabel = new JLabel("File Name:");
		fileField.setColumns(30);
		constraints.gridx = 0;
		panel.add(fileLabel, constraints);
		constraints.gridx = 1;
		panel.add(fileField, constraints);
		constraints.gridy = ++ gridy;
		
		constraints.gridx = 0;
		JLabel nameLabel = new JLabel("Table Name:");
		panel.add(nameLabel, constraints);
		JTextField nameField = new JTextField();
		nameField.setColumns(20);
		constraints.gridx = 1;
		panel.add(nameField, constraints);
		
		constraints.gridy = ++ gridy;
		constraints.gridx = 3;
		panel.add(primaryColumnLabel, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.gridy = ++ gridy;
		++ gridy;
		constraints.gridwidth = 1;
		ButtonGroup buttonGroup = new ButtonGroup();
		int i = 0;
		for(; i < boxes.length; i++) {
			constraints.gridy = ++ gridy;
			constraints.gridx = 0;
			
			labels[i] = new JLabel(columns[i]);
			panel.add(labels[i], constraints);
			
			constraints.gridx = 1;
			
			boxes[i] = new JComboBox(boxOptions);
			boxes[i].setEditable(true);
			boxes[i].setSelectedItem("VARCHAR(16)");
			panel.add(boxes[i], constraints);
			
			constraints.gridx = 3;
			
			radioButtons[i] = new JRadioButton("", false);
			buttonGroup.add(radioButtons[i]);
			panel.add(radioButtons[i], constraints);
		}
		
		radioButtons[i] = new JRadioButton("Add index column", true);
		buttonGroup.add(radioButtons[i]);
		constraints.gridy = i + ++ gridy;
		
		panel.add(radioButtons[i], constraints);
		
		JButton ok = new JButton("Ok");
		ok.addActionListener(e -> {
			
			if(nameField.getText().length() >
			   0 /*&& fileField.getText().length() > 0*/) {
				//Todo: create fileField to get fileName
				
				String tableName = nameField.getText().trim();
				final String finalFileName = fileField.getText().trim();
				Enumeration<AbstractButton> bs = buttonGroup.getElements();
				boolean foundButton = false;
				for(int j = 0; j < radioButtons.length - 1; j++) {
					JRadioButton radioButton = (JRadioButton)bs.nextElement();
					if(radioButton.isSelected() && bs.hasMoreElements()) {
						PRIMARY_KEY.setValue(J_TO_SQL.get(j));
						foundButton = true;
					}
				}
				if(! foundButton) {
					PRIMARY_KEY.setValue("idx".trim());
					PRIMARY_KEY.setValue("int(16)".trim());
				}
				typeMap = new HashMap<>();
				for(int j = 0; j < boxes.length; j++) {
					typeMap.put(j, (boxes[j].getSelectedItem() + "").trim());
				}
				try {
					batchSqlString(tableName, columns,
					 new Scanner(new File(finalFileName)));
				}
				catch(SQLException | FileNotFoundException throwables) {
					throwables.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++ gridy;
		panel.add(ok, constraints);
		constraints.gridx = 1;
		JButton cancel = new JButton("Cancel");
		Dimension dim = frame.getContentPane().getPreferredSize();
		scrollPane.setMaximumSize(new Dimension(dim.width - 1, dim.height - 1));
		cancel.addActionListener(e -> {
			frame.remove(panel);
			frame.setVisible(false);
		});
		panel.add(cancel, constraints);
		scrollPane.getViewport().add(panel);
		frame.getContentPane().add(scrollPane);
		int sizeW = panel.getPreferredSize().width + 50;
		int sizeH = panel.getPreferredSize().height + 50;
		frame.setSize(new Dimension(sizeW, sizeH));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	// Formats the String to be sent as sql code from an array of values
	private static String getInsertionString(String[] array) {
		Object[] values = new Object[array.length];
		StringFormat sf = new StringFormat("(");
		for(int i = 0; i < values.length; i++) {
			if(typeMap.get(i).contains("CHAR")) {
				array[i] = "'" + array[i] + "'";
			}
			sf.append(array[i]);
			if(i == values.length - 1) {
				sf.append(")");
			}
			else {sf.append(",");}
		}
		return sf.toString();
	}
	
	// create the tuple of column names as a String to be sent as sql code. return the column 
	// titles, comma separated, in parentheses start.e., (c1, c2,...cn)
	private static String getcolumnTuple(String[] columnNames) {
		if(columnNames.length == 0) {return null;}
		StringFormat sf = new StringFormat("(");
		int i = 0;
		for(; i < columnNames.length - 1; i++) {
			sf.append(columnNames[i]).append(",");
		}
		return sf.append(columnNames[i]).append(")").toString();
	}
	
	// public method invokes the internal call to createBlankTable()
	public void batchSqlString() {
		try { createBlankTable(); }
		catch(Exception e) { e.printStackTrace(); }
	}
	
	//Starts the process of getting user input for information about the table to be created and
	// uploaded, then summons a gui to collect the input.
	private void createBlankTable() throws Exception {
		String filePath = JOptionPane.showInputDialog(null, "Open file:");
		int[] count = null;
		if(! filePath.endsWith(".csv")) {
			//Todo: throw new FileNotSupportedException
			return;
		}
		
		Scanner scanner = new Scanner(new File(filePath));
		String[] columns = scanner.nextLine().split(",");
		columns[0] = removeUTF8BOM(columns[0]);
		guiUpload(columns, filePath);
	}
	
	// Removes the (BOM byte-order mark) from the beginning of the string.
	private static String removeUTF8BOM(String s) {
		if(s.startsWith("\uFEFF")) {
			s = s.substring(1);
		}
		return s;
	}
	
	// Delete the record in the specified table
	public int deleteRecord
	(String table, String idColumn, Object idValue)
	throws SQLException {
		return updateF("DELETE FROM %s WHERE %s = %s",
		 table, idColumn, idValue);
	}
	
	// Deletes all records from a table, but the table remains
	public int deleteAllRecords(String table) throws SQLException {
		return update("DELETE FROM " + table);
	}
	
	// deletes an entire table
	public static void deleteTable(String tableName) throws SQLException {
		update("DROP TABLE IF EXISTS " + tableName);
	}
	
	// helper method to clean up code when concatenating commas for sql code.
	private static String getComma(int length, int i, String lastChar) {
		if(i < length - 1) {return ", ";}
		return lastChar + "";
	}
	
	// wraps the given object in quotes if it is a string
	private static String quoteWrap(Object columnValue) {
		if(columnValue instanceof String) {return "'" + columnValue + "'";}
		return columnValue.toString();
	}
	
	// overload of above uses a known boolean value
	public String quoteWrap(String value, boolean isString) {
		if(isString) {return "'" + value + "'";}
		return value;
	}
	
	// get the names of all tables
	public ArrayList<String> getTables() throws SQLException {
		ResultSet rs = query("SHOW tables");
		ArrayList<String> tables = new ArrayList<>();
		while(rs.next()) {tables.add(rs.getString(1));}
		return tables;
	}
	
	// get the URL by joining static variable together.
	private static String getURL() {
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
	}
	
	public String getType(String name) { return J_TO_SQL2.get(name); }
	
	// return an integer indicating if the order can be processed,
	// and if not, why
	public int canProcessOrder(Order order) throws SQLException, MessagingException {
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
	
	public void setWorkingTable(String tableName) { this.tableName = tableName; }
	
	public void recordOrder(Order order) throws SQLException {
		update("INSERT INTO `sales` (quantity, date, customer_email, customer_location, " +
		 "product_id, order_id) VALUES" + order.getSalesTuple());
	}
	
	public void processOrder(Order order) throws SQLException {
		int newQuantity = order.getCurrentQuantity() - order.getQuantity();
		updateF("UPDATE %s SET quantity = %s WHERE product_id = '%s';",
		 tableName, newQuantity, order.getProductId());
	}
	
	public ResultSet query(String query) throws SQLException {
		return connection.createStatement().executeQuery(query);
	}
	
	public static int update(String sql) throws SQLException {
		return connection.createStatement().executeUpdate(sql);
	}
	
	public String format(String format, Object... args) {
		return String.format(format, args);
	}
	
	public ResultSet queryF(String format, Object... args) throws SQLException {
		return query(format(format, args));
	}
	
	private int updateF(String format, Object... args) throws SQLException {
		return update(format(format, args));
	}
	public static final int STRING = 1;
	public static final int CHAR = 2;
	public static final int LONGVARCHAR = 3;
	public static final int BOOLBIT = 4;
	public static final int NUMERIC = 5;
	public static final int TINYINT = 6;
	public static final int SMALLINT = 7;
	public static final int INTEGER = 8;
	public static final int BIGINT = 9;
	public static final int REAL = 10;
	public static final int FLOAT = 11;
	public static final int DOUBLE = 12;
	public static final int VARBINARY = 13;
	public static final int BINARY = 14;
	public static final int DATE = 15;
	public static final int TIME = 16;
	public static final int TIMESTAMP = 17;
	public static final int CLOB = 18;
	public static final int BLOB = 19;
	public static final int ARRAY = 20;
	public static final int REF = 21;
	public static final int STRUCT = 22;
	public static final Map<Integer, String> J_TO_SQL = Map
	 .ofEntries(
	  entry(STRING, "VARCHAR(16)"),
	  entry(CHAR, "CHAR"),
	  entry(LONGVARCHAR, "VARCHAR(32)"),
	  entry(BOOLBIT, "BIT"),
	  entry(NUMERIC, "NUMERIC"),
	  entry(TINYINT, "int(2)"),
	  entry(SMALLINT, "int(4)"),
	  entry(INTEGER, "int(8)"),
	  entry(BIGINT, "int(16)"),
	  entry(REAL, "REAL"),
	  entry(FLOAT, "FLOAT"),
	  entry(DOUBLE, "decimal(13,2)"),
	  entry(VARBINARY, "VARBINARY"),
	  entry(BINARY, "BINARY"),
	  entry(DATE, "DATE"),
	  entry(TIME, "TIME"),
	  entry(TIMESTAMP, "TIMESTAMP"),
	  entry(CLOB, "CLOB"),
	  entry(BLOB, "BLOB"),
	  entry(ARRAY, "ARRAY"),
	  entry(REF, "REF"),
	  entry(STRUCT, "STRUCT"));
	public static final Map<String, String> J_TO_SQL2 = Map
	 .ofEntries(
	  entry("VARCHAR", "VARCHAR(16)"),
	  entry("CHAR", "CHAR"),
	  entry("LONGVARCHAR", "VARCHAR(32)"),
	  entry("BOOLBIT", "BIT"),
	  entry("INT", "int(8)"),
	  entry("DOUBLE", "decimal(13,2)"),
	  entry("DATE", "DATE"),
	  entry("TIME", "TIME"),
	  entry("TIMESTAMP", "TIMESTAMP"));
}

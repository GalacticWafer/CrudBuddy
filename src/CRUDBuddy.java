import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.util.*;
import javax.swing.*;

import static java.util.Map.entry;

class CRUDBuddy {
	private static Pair<String, String> PRIMARY_KEY = new Pair("idx", "int(16)");
	private static Pair<String, String> USER_AND_PASS;
	private static String HOST_IP;
	private static String PORT;
	private static String DB_NAME;
	private static HashMap<Integer, String> typeMap;
	private static String USER_NAME;
	private static String PASSWORD;
	private static HashMap<String, String[]> tables;
	private static Connection connection;
	private static String tableName;
	private static Scanner scanner;
	private static String[] headers;
	private static Pair<String, String> primaryKey = new Pair<>("idx", "int(16)");
	
	/**
	 * Class that facilitates a connection to a database, and carries out CRUD operations
	 *
	 * @param userName your github account name
	 * @param passWord check your messages
	 * @param hostIP   check your messages
	 * @param port     3306
	 * @param schema   the database that we are working in is "cs3250_project"
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public CRUDBuddy(String userName, String passWord, String hostIP, String port, String schema) throws SQLException, ClassNotFoundException {
		USER_NAME = userName;
		PASSWORD = passWord;
		HOST_IP = hostIP;
		PORT = port;
		DB_NAME = schema;
		tables = new HashMap<>();
		Class.forName("com.mysql.cj.jdbc.Driver");
		connection = DriverManager.getConnection(getURL(), userName, passWord);
		JOptionPane.showMessageDialog(null, "Connection OK with " + getURL());
	}
	
	public int createTable(String tableName, String[] headers, HashMap<Integer, String> typeMap) throws SQLException {
		
		deleteTable(tableName);
		StringFormat sb = new StringFormat(
		 "CREATE TABLE %s(%s %s) NOT NULL AUTO_INCREMENT,",
		 tableName, PRIMARY_KEY.getKey(), PRIMARY_KEY.getValue());
		
		for(int i = 0; i < typeMap.size(); i++) {
			sb.appendf(String.format("%s %s,", 
			 headers[i], typeMap.get(i)));
		}
		sb.appendf(" PRIMARY KEY (%s));", PRIMARY_KEY.getKey());
		return connection.createStatement()
		 .executeUpdate(sb.toString());
	}

	/**
	 * Specify a table name and array of column names, along with a HashMap indicating the data
	 * type for each column.  Use the constants included at the bottom of this class to specify
	 * the string representation of MySQL's equivalent data type. For example,
	 * the following are all true:
	 * <p>
	 * typeMap.get(INTEGER).equals("int(16)");
	 * typeMap.get(DOUBLE).equals("decimal(13,2)";
	 * typeMap.get(STRING).equals("VARCHAR(32)")";
	 * <p>
	 * <p>
	 * <p>
	 * Example:
	 * <p>
	 * LinkedList<Integer> columnTypes = new LinkedList<>();
	 * columnTypes.add(INTEGER);
	 * columnTypes.add(STRING);
	 * columnTypes.add(BOOLEAN);
	 * columnTypes.add(DOUBLE);
	 * String[] columnNames = new String[]{"my_int", "my_string", "my_bool", "my_dubs"}
	 * <p>
	 * createTable("table_x", columnNames, columnTypes)
	 * <p>
	 * //////////!!!!!!!!!!!!!!!!!!!!!!!!!\\\\\\\\\\\\
	 * ADD CONSTANTS TO 'columnTypes' IN THE SAME ORDER
	 * AS THE ARRAY 'columnNames' SO THEY MATCH UP!!!
	 * //////////!!!!!!!!!!!!!!!!!!!!!!!!!\\\\\\\\\\\\
	 */
	public void createTable(String tableName, String[] headers, LinkedList<Integer> columnTypes) throws SQLException {
		deleteTable(tableName);
		
		StringFormat sf = new StringFormat(
		 "CREATE TABLE %s(%s %s) NOT NULL AUTO_INCREMENT,",
		 tableName, PRIMARY_KEY.getKey(), PRIMARY_KEY.getValue());
		
		int i = 0;
		Iterator<Integer> itr = columnTypes.iterator();
		for(; itr.hasNext(); i++) {
			Integer columnType = columnTypes.get(itr.next());
			sf.appendf("%s %s, ", headers[i], columnType);
		}
		
		sf.appendf(" PRIMARY KEY (%S));", PRIMARY_KEY.getKey());
		connection.createStatement().executeUpdate(sf.toString());
	}
	
	/**
	 * Gets an arraylist of the column names of a specific table
	 *
	 * @param dbName    name of the desired database (shouldn't change for this project)
	 * @param tableName name of the target table
	 * @return Arraylist of Strings for all column names in the specified table.
	 * @throws SQLException
	 */
	public ArrayList<String> readColumnNames(String dbName, String tableName) throws SQLException {
		
		StringFormat sf = new StringFormat(
		 "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
		 " WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'",dbName, tableName);
		
		ResultSet rs = connection.createStatement().executeQuery(sf.toString());
		
		ArrayList<String> headers = new ArrayList<>();
		
		while(rs.next()) {
			headers.add(rs.getString(1));
		}
		return headers;
	}
	/**
	 * Gets an arrayList of column types from a table
	 *
	 * @param tableName name of the target table
	 * @return
	 * @throws SQLException
	 */
	public ResultSet readColumnTypes(String tableName) throws SQLException {
		StringFormat sf = new StringFormat(
		 "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = %s AND " +
		  "COLUMN_NAME = 'product_id'", tableName);
		return connection.createStatement().executeQuery(sf.toString());
	}
	
	/**
	 * Queries a table for the specified criteria. The key is a column name (String), and the value
	 * is the value from that cell in the table.
	 *
	 * @param tableName    the name of the table to query.
	 * @param columnNames  array of column names to query.
	 * @param idColumnName the column name of the target record.
	 * @param idValue      the unique value for the target record.
	 * @return null if the columnNames array is null or empty. Otherwise, a Hashmap of results
	 * (HashMap<String, Object>). Each String is a column header, and each value is an Object of
	 * whichever type is associated with the column.
	 * @throws SQLException
	 */
	public HashMap<String, Object> readColumnValues(String tableName, String[] columnNames,
	 String idValue, String idColumnName) throws SQLException {
		if(columnNames.length == 0) {
			return null;
		}
		
		StringFormat sf = new StringFormat("SELECT %s FROM %s WHERE %s = '%s'", 
		 arrayToCSV(columnNames), tableName, idColumnName, idValue);
		
		ResultSet rs = connection.createStatement().executeQuery(sf.toString());
		
		HashMap<String, Object> objectMap = new HashMap<>();
		while(rs.next()) {
			for(int i = 0; i < columnNames.length; i++) {
				objectMap.put(columnNames[i], rs.getObject(columnNames[i]));
				System.out.println(i);
			}
		}
		return objectMap;
	}
	
	public String arrayToCSV(String[] array) {
		String a = Arrays.toString(array);
		return a.substring(1, a.length() - 1);
	}
	
	/**
	 * @param tableName    Name of the table in which to update values
	 * @param columnNames  Array of strings for all columns to be updated
	 * @param newValues    The values to replace for every element in columnNames[] array
	 * @param idValue      The value to to be identified (most likely a product_id), but can be any
	 *                     information about the row that is desired)
	 * @param idColumnName String name of the column of the identifying value
	 * @param isString     indicates which elements from <code>newValues</code> need to be 
	 *                        wrapped in
	 *                     in quotes. If newValues[i] is a represents SQL VARCHAR type, then
	 *                     isString[i] should be true. Otherwise, false.
	 * @param idIsString   indicates whether or not the row-identifying value is a should be
	 *                     wrapped in quotes, for the same reason as isString[] array.
	 * @return returnIntegers for the feedback received from MySQL
	 * @throws SQLException
	 */
	public int[] updateRow(String tableName, String[] columnNames, 
	 String[] newValues, String idValue,
	 String idColumnName, boolean[] isString,
	 boolean idIsString) throws SQLException {
		
		idValue = quoteWrap(idValue, idIsString);
		int[] returnIntegers = new int[columnNames.length];
		
		for(int i = 0; i < newValues.length; i++) {
			String newValue = quoteWrap(newValues[i], isString[i]);
			
			String sql = String.format("UPDATE %s SET %s = %s WHERE %s = %s;", 
			 tableName, columnNames[i], newValue, idColumnName, idValue);
			
			returnIntegers[i] = connection.createStatement().executeUpdate(sql);
		}
		return returnIntegers;
	}
	
	public String quoteWrap(String value, boolean valueIsString) {
		if(valueIsString) {
			return "'" + value + "'";
		}
		return value;
	}
	
	/**
	 * @param columnNames  to update
	 * @param columnValues to be assigned
	 * @param id           value
	 * @param tableName
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Object updateRow(String[] columnNames, Object[] columnValues, Object id,
	 String tableName, Connection conn) throws SQLException {
		
		StringFormat sf = new StringFormat("UPDATE %s SET ", tableName);
		
		for(int i = 0; i < columnNames.length; i++) {
			sf.appendf("%s = %s%s", columnNames[i], 
			 formatValue(columnValues[i]), getComma(columnNames.length, i, ""));
		}
		sf.appendf(" where %s=%s",PRIMARY_KEY, formatValue(id));
		return conn.createStatement().executeUpdate(sf + "");
	}
	
	
	
	private long[] batchLoadTable(String tableName, String[] headers, String fileName,
	 Scanner scanner) throws SQLException {
		
		long[] count = null;
		Statement statement = connection.createStatement();
		if(typeMap != null) {
			createTable(tableName, headers, typeMap);
			
			StringFormat sf = new StringFormat("INSERT INTO %s %s)VALUES"
			 ,tableName, getHeaderTuple(headers));
			String sqlDeclaration = sf.toString();
			
			scanner.nextLine();
			int MAX_LOOPS = 100000;
			for(int i = 1; i < MAX_LOOPS && scanner.hasNextLine(); i++) {
				String[] line = scanner.nextLine().split(",");
				String nextInsertion = getInsertionString(line);
				boolean isLastIteration = i == MAX_LOOPS - 1;
				String lastChar = getLastChar(isLastIteration, ";", ",");
				
				sf.append(nextInsertion).append(lastChar);
				
				if(isLastIteration) {
					statement.executeUpdate(sf.toString());
					i = 0;
					sf.setLength(0);
					sf.append(sqlDeclaration);
				}
			}
			sf.replace(sf.length() - 1, sf.length(), ";");
			statement.executeUpdate(sf.toString());
			scanner.close();
			JOptionPane.showMessageDialog(null, String.format(
			 "The csv file has been exported to %sqlDeclaration in the %sqlDeclaration database.",
			 tableName, DB_NAME));
		}
		return count;
	}
	private JFrame uploadTableGUI(String[] headers, Scanner scanner) {
		GridBagConstraints constraints = new GridBagConstraints();
		GridBagLayout layout = new GridBagLayout();
		
		JFrame frame = new JFrame();
		JScrollPane scrollPane= new JScrollPane();
		JPanel panel = new JPanel(layout);
		
		Object[] boxOptions = J_TO_SQL.values().toArray();
		
		JRadioButton[] radioButtons = new JRadioButton[headers.length + 1];
		JLabel primaryColumnLabel = new JLabel("Primary Column:");
		
		JComboBox[] boxes = new JComboBox[headers.length];
		JLabel[] labels = new JLabel[headers.length];
		
		JLabel nameLabel = new JLabel("Table Name:");
		JTextField nameField = new JTextField();
		nameField.setColumns(20);
		
		JTextField fileField = new JTextField();
		JLabel fileLabel = new JLabel("Table Name:");
		fileField.setColumns(30);
		
		constraints.gridx = 0;
		panel.add(nameLabel, constraints);
		constraints.gridx = 1;
		constraints.gridx = 3;
		panel.add(primaryColumnLabel, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		panel.add(nameField, constraints);
		constraints.gridwidth = 1;
		ButtonGroup buttonGroup = new ButtonGroup();
		int i = 0;
		for(; i < boxes.length; i++) {
			constraints.gridy = i + 1;
			constraints.gridx = 0;
			
			labels[i] = new JLabel(headers[i]);
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
		constraints.gridy = i + 1;
		panel.add(radioButtons[i], constraints);
		
		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			/**
			 * Invoked when an action occurs.
			 *
			 * @param e the event to be processed
			 */
			@Override public void actionPerformed(ActionEvent e) {
				if(nameField.getText().length() > 0 /*&& fileField.getText().length() > 0*/) {
					//Todo: create fileField to get fileName
					
					String tableName = nameField.getText();
					String fileName = fileField.getText();
					Enumeration<AbstractButton> bs = buttonGroup.getElements();
					boolean foundButton = false;
					for(int j = 0; j < radioButtons.length - 1; j++) {
						JRadioButton radioButton = (JRadioButton)bs.nextElement();
						if(radioButton.isSelected() && bs.hasMoreElements()) {
							PRIMARY_KEY.setValue(J_TO_SQL.get(j));
							foundButton = true;
						}
					}
					if(!foundButton) {
						PRIMARY_KEY.setValue("idx");
						PRIMARY_KEY.setValue("int(16)");
					}
					typeMap = new HashMap<>();
					for(int j = 0; j < boxes.length; j++) {
						typeMap.put(j, boxes[j].getSelectedItem() + "");
					}
					try {
						batchLoadTable(tableName, headers, "inventory_team4.csv", scanner);
					}
					catch(SQLException throwables) {
						throwables.printStackTrace();
					}
					frame.setVisible(false);
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = i + 1;
		panel.add(ok, constraints);
		constraints.gridx = 1;
		JButton cancel = new JButton("Cancel");
		Dimension dim = frame.getContentPane().getPreferredSize();
		scrollPane.setMaximumSize(new Dimension(dim.width - 1, dim.height - 1));
		cancel.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				frame.remove(panel);
				frame.setVisible(false);
			}
		});
		panel.add(cancel, constraints);
		scrollPane.getViewport().add(panel);
		frame.getContentPane().add(scrollPane);
		int sizeW = panel.getPreferredSize().width + 50;
		int sizeH = panel.getPreferredSize().height + 50;
		frame.setSize(new Dimension(sizeW, sizeH));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		return frame;
	}
	/**
	 * Formats the String to be sent as sql code
	 * <p>
	 * insert the
	 * new row into
	 *
	 * @param array the values of each column
	 *              type of
	 *              data each column is
	 * @return sql String query to execute
	 */
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
			} else {
				sf.append(",");
			}
		}
		return sf.toString();
	}
	

	/**
	 * create the tuple of headers as a String to be sent
	 * as sql code
	 *
	 * @return the column titles, comma separated, in
	 * parentheses
	 * i.e., (c1, c2,...cn)
	 */
	private static String getHeaderTuple(String[] columnNames) {
		if(columnNames.length ==0) {
			return null;
		}
		
		StringFormat sf = new StringFormat("(");
		int i = 0;
		for(; i < columnNames.length - 1; i++) {
			sf.append(columnNames[i]).append(",");
		}
		
		return sf.append(columnNames[i]).append(")").toString();
	}
	
	public void loadTable(String fileName) {
		try {
			loadRemoteTable(fileName);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		private void loadRemoteTable(String filePath) throws Exception {
		int[] count = null;
		if(!filePath.endsWith(".csv")) {
			//Todo: throw new FileNotSupportedException
			return;
		}
		scanner = new Scanner(new File(filePath));
		String[] headers = scanner.nextLine().split(",");
		uploadTableGUI(headers, scanner);
	}
	
	
	
	
	
	/**
	 * Delete the row  in the specified table
	 *
	 * @param table
	 * @param idColumn
	 * @param idValue
	 * @return
	 * @throws SQLException
	 */
	public int deleteRecord(String table, String idColumn, Object idValue) throws SQLException {
		ArrayList<String> arrays = new ArrayList<>();
		return connection.createStatement().executeUpdate(
		 "DELETE FROM " + table + " WHERE " + idColumn + " = " + idValue);
	}
	
	/**
	 * Deletes all records from a table, but the table remains
	 *
	 * @param table to delete all records in
	 * @return
	 * @throws SQLException
	 */
	public int deleteAllRecords(String table) throws SQLException {
		return connection.createStatement().executeUpdate("DELETE FROM " + table);
	}
	
	/**
	 * deletes an entire table
	 *
	 * @param tableName to be deleted
	 * @return
	 * @throws SQLException
	 */
	public static int deleteTable(String tableName) throws SQLException {
		return connection.createStatement().executeUpdate("DROP TABLE IF EXISTS " + tableName);
	}
	
	
	/**
	 * helper method to clean up code when concatenating commas for sql code.
	 *
	 * @param length          the length of the array to check against
	 * @param i               string to append to the end of a list of sql elements in a query. 
	 *                           (usually a
	 *                        parentheses or empty string).
	 * @param endingCharacter the
	 * @return either a comma if there are more elements left to iterate through (as calculated
	 * by <code>length</code>, or the ending character
	 */
	private static String getComma(int length, int i, String endingCharacter) {
		if(i < length - 1) {
			return ", ";
		}
		return endingCharacter + "";
	}
	
	private String getLastChar(boolean isLastIteration, String optionTrue, String optionFalse) {
		if(isLastIteration) {
			return optionTrue;
		}
		return optionFalse;
	}
	
	/**
	 * 
	 * @param columnValue
	 * @return
	 */
	private static String formatValue(Object columnValue) {
		if(columnValue instanceof String) {
			return "'" + columnValue + "'";
		}
		return columnValue.toString();
	}
	
	/**
	 * @return the beginning of the  string for the url (without username and password)
	 */
	private static String getURL() {
		return "jdbc:mysql://" + HOST_IP + ":" + PORT + "/" + DB_NAME;
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
	public static final int NO_INDEX_COLUMN = -1;
	private static Map<Integer, String> J_TO_SQL = Map
	 .ofEntries(entry(STRING, "VARCHAR(16)"), entry(CHAR, "CHAR"), entry(LONGVARCHAR, "VARCHAR(32)"
	 ), entry(BOOLBIT, "BIT"), entry(NUMERIC, "NUMERIC"), entry(TINYINT, "int(2)"), entry(SMALLINT
	  , "int(4)"), entry(INTEGER, "int(8)"), entry(BIGINT, "int(16)"), entry(REAL, "REAL"),
	  entry(FLOAT, "FLOAT"), entry(DOUBLE, "decimal(13,2)"), entry(VARBINARY, "VARBINARY"),
	  entry(BINARY, "BINARY"), entry(DATE, "DATE"), entry(TIME, "TIME"), entry(TIMESTAMP, 
	   "TIMESTAMP"), entry(CLOB, "CLOB"), entry(BLOB, "BLOB"), entry(ARRAY, "ARRAY"), entry(REF, 
	   "REF"), entry(STRUCT, "STRUCT"));
	private static Map<String, Integer> SQL_TO_J = Map
	 .ofEntries(entry("VARCHAR(16)", STRING), entry("CHAR", CHAR), entry("VARCHAR(32)",
	  LONGVARCHAR), entry("BIT", BOOLBIT), entry("NUMERIC", NUMERIC), entry("int(2)", TINYINT),
	  entry("int(4)", SMALLINT), entry("int(8)", INTEGER), entry("int(16)", BIGINT), entry("REAL",
	   REAL), entry("FLOAT", FLOAT), entry("decimal(13,2)", DOUBLE), entry("VARBINARY", VARBINARY)
	  , entry("BINARY", BINARY), entry("DATE", DATE), entry("TIME", TIME), entry("TIMESTAMP",
	   TIMESTAMP), entry("CLOB", CLOB), entry("BLOB", BLOB), entry("ARRAY", ARRAY), entry("REF",
	   REF), entry("STRUCT", STRUCT));
	
	/**
	 * tester method
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public ResultSet query(String query) throws SQLException {
		return connection.createStatement().executeQuery(query);
	}
}
	

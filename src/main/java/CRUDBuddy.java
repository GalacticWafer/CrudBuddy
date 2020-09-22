import com.mysql.cj.MysqlType;

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
	private static Pair<String, String> PRIMARY_KEY = new Pair("idx", "int(16)");
	private static String HOST_IP;
	public static final String UTF8_BOM = "\uFEFF";
	private static String PORT;
	private static String DB_NAME;
	private static HashMap<Integer, String> typeMap;
	private static String USER_NAME;
	private static String PASSWORD;
	private static HashMap<String, String[]> tables;
	private static Connection connection;
	private static String tableName;
	private static Scanner scanner;
	private static String[] columnNames;
	
	/**
	 * Class that facilitates a connection to a database, and carries out CRUD
	 * operations
	 *
	 * @param userName
	 *  your github account name
	 * @param passWord
	 *  check your messages
	 * @param hostIP
	 *  check your messages
	 * @param port
	 *  3306
	 * @param schema
	 *  the database that we are working in is "cs3250_project"
	 *
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public CRUDBuddy
	(String userName, String passWord, String hostIP, String port, String schema)
	throws SQLException, ClassNotFoundException {
		
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
	
	/**
	 * Creates a blank Table
	 *
	 * @param tableName
	 *  name of the table.
	 * @param columnNames
	 *  String[] of tables.
	 * @param typeMap
	 *  HashMap where each index in <code>columnNames</code> has
	 *  the string representation of its data type as the value
	 *
	 * @return either (1) the row count for SQL Data Manipulation Language (DML)
	 *  statements or (2) 0 for SQL statements that return nothing
	 *
	 * @throws SQLException
	 *  if a database access error occurs
	 */
	public int createBlankTable
	(String tableName, String[] columnNames, HashMap<Integer, String> typeMap)
	throws SQLException {
		
		deleteTable(tableName);
		StringFormat sb = new StringFormat(
		 "CREATE TABLE %s(%s %s NOT NULL AUTO_INCREMENT,".trim(),
		 tableName.trim(), PRIMARY_KEY.getKey().trim(),
		 PRIMARY_KEY.getValue().trim());
		
		int i = 0;
		for(; i < typeMap.size(); i++) {
			sb.appendf(String.format("%s %s,",
			 columnNames[i].trim(), typeMap.get(i).trim()));
		}
		sb.appendf(" PRIMARY KEY (%s));", PRIMARY_KEY.getKey());
		return connection.createStatement()
						 .executeUpdate(sb.toString());
	}
	
	/**
	 * Specify a table name and array of column names, along with a HashMap
	 * indicating the data
	 * type for each column.  Use the constants included at the bottom of this
	 * class to specify
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
	 * String[] columnNames = new String[]{"my_int", "my_string", "my_bool",
	 * "my_dubs"}
	 * <p>
	 * createBlankTable("table_x", columnNames, columnTypes)
	 * <p>
	 * //////////!!!!!!!!!!!!!!!!!!!!!!!!!\\\\\\\\\\\\
	 * ADD CONSTANTS TO 'columnTypes' IN THE SAME ORDER
	 * AS THE ARRAY 'columnNames' SO THEY MATCH UP!!!
	 * //////////!!!!!!!!!!!!!!!!!!!!!!!!!\\\\\\\\\\\\
	 */
	public void createBlankTable
	(String tableName, String[] columnNames, LinkedList<Integer> columnTypes)
	throws SQLException {
		
		deleteTable(tableName);
		
		StringFormat sf = new StringFormat(
		 "CREATE TABLE %s(%s %s) NOT NULL AUTO_INCREMENT,",
		 tableName, PRIMARY_KEY.getKey(), PRIMARY_KEY.getValue());
		
		int i = 0;
		Iterator<Integer> itr = columnTypes.iterator();
		for(; itr.hasNext(); i++) {
			Integer columnType = columnTypes.get(itr.next());
			sf.appendf("%s %s, ", columnNames[i], columnType);
		}
		
		sf.appendf(" PRIMARY KEY (%S));", PRIMARY_KEY.getKey());
		connection.createStatement().executeUpdate(sf.toString());
	}
	
	/**
	 * Gets an arraylist of the column names of a specific table
	 *
	 * @param dbName
	 *  name of the desired database (shouldn't change for this
	 *  project)
	 * @param tableName
	 *  name of the target table
	 *
	 * @return Arraylist of Strings for all column names in the specified table.
	 *
	 * @throws SQLException
	 */
	public ArrayList<String> readColumnNames
	(String dbName, String tableName)
	throws SQLException {
		
		StringFormat sf = new StringFormat(
		 //"Describe %s;",  tableName);
		 "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
		 " WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s'", dbName, tableName);
		ResultSet rs = connection.createStatement().executeQuery(sf.toString());
		
		ArrayList<String> columnNames = new ArrayList<>();
		
		while(rs.next()) {
			columnNames.add(rs.getString(1));
		}
		return columnNames;
	}
	
	/**
	 * Gets an arrayList of column types from a table
	 *
	 * @param tableName
	 *  name of the target table
	 *
	 * @return an ArrayList of column types, as their SQL string alias
	 *
	 * @throws SQLException
	 */
	public ArrayList<String> readColumnTypes
	(String tableName)
	throws SQLException {
		
		ArrayList<String> arr = new ArrayList<>();
		ResultSet rs = connection.createStatement().executeQuery(
		 "select * from " + tableName + " where 1<0");
		
		ResultSetMetaData md = rs.getMetaData();
		
		boolean firstIsIndex = md.isAutoIncrement(1);
		int dataColumnCount = firstIsIndex?md.getColumnCount() - 1:
		 md.getColumnCount();
		
		String[] columnTypes = new String[dataColumnCount];
		
		for(int i = 0; i < md.getColumnCount(); i++) {
			if(!md.isAutoIncrement(i + 1)) {
				arr.add(MysqlType.getByJdbcType(md.getColumnType(i)) + "");
			}
		}
		
		return arr;
	}
	
	/**
	 * Queries a table for the specified criteria. The key is a column name
	 * (String), and the value
	 * is the value from that cell in the table.
	 *
	 * @param tableName
	 *  the name of the table to query.
	 * @param columnNames
	 *  array of column names to query.
	 * @param idColumnName
	 *  the column name of the target record.
	 * @param idValue
	 *  the unique value for the target record.
	 *
	 * @return null if the columnNames array is null or empty. Otherwise, a Hashmap
	 *  of results
	 *  (HashMap<String, Object>). Each String is a column name, and each value is
	 *  an Object of
	 *  whichever type is associated with the column.
	 *
	 * @throws SQLException
	 */
	public HashMap<String, Object> readColumnValues
	(String tableName, String[] columnNames, String idValue, String idColumnName)
	throws SQLException {
		
		if(columnNames.length == 0) {
			return null;
		}
		
		StringFormat sf = new StringFormat("SELECT %s FROM %s WHERE %s = '%s'",
		 arrayToCSV(columnNames), tableName,
		 idColumnName, idValue);
		
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
	
	/**
	 * @param table
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	public ResultSet readAllRecords
	(String table)
	throws SQLException {
		
		return connection.createStatement().executeQuery("SELECT * FROM " + table);
	}
	
	/**
	 * @param path
	 * @param columns
	 * @param results
	 *
	 * @return
	 *
	 * @throws FileNotFoundException
	 * @throws SQLException
	 */
	public File writeToFile
	(String path, ArrayList<String> columns, ResultSet results)
	throws FileNotFoundException, SQLException {
		
		boolean isIndexed = PRIMARY_KEY.getKey().equals("idx");
		List<String> newColumns;
		int start;
		
		if(columns.size() == 0) {
			throw new InputMismatchException();
		}
		
		start = isIndexed?1:0;
		newColumns = columns.subList(start, columns.size());
		
		File file = new File(path);
		PrintWriter pw = new PrintWriter(file);
		pw.println(String.join(",", newColumns));
		
		while(results.next()) {
			Iterator<String> it = newColumns.iterator();
			
			while(it.hasNext()) {
				pw.print(results.getObject(it.next()));
				if(it.hasNext()) {
					pw.print(",");
				}
			}
			pw.println();
		}
		
		pw.close();
		return file;
	}
	
	/**
	 * creates a .csv-compatible line from a String[]
	 *
	 * @param array
	 *  the array to convert to a .csv format (typically column names)
	 *
	 * @return the .csv line for <code>array</code>
	 */
	public String arrayToCSV
	(String[] array) {
		
		String a = Arrays.toString(array);
		return a.substring(1, a.length() - 1);
	}
	
	/**
	 * creates a .csv-compatible line from an ArrayList<String>
	 *
	 * @param array
	 *  the array to convert to a .csv format (typically column names)
	 *
	 * @return the .csv line for <code>array</code>
	 */
	public String arrayToCSV
	(ArrayList<String> array) {
		
		String a = array.toString();
		return a.substring(1, a.length() - 1);
	}
	
	/**
	 * @param tableName
	 *  Name of the table in which to update values
	 * @param columnNames
	 *  Array of strings for all columns to be updated
	 * @param newValues
	 *  The values to replace for every element in columnNames[]
	 *  array
	 * @param idValue
	 *  The value to to be identified (most likely a product_id)
	 *  , but can be any
	 *  information about the row that is desired)
	 * @param idColumnName
	 *  String name of the column of the identifying value
	 * @param isString
	 *  indicates which elements from <code>newValues</code>
	 *  need to be
	 *  wrapped in
	 *  in quotes. If newValues[start] is a represents SQL
	 *  VARCHAR
	 *  type, then
	 *  isString[start] should be true. Otherwise, false.
	 * @param idIsString
	 *  indicates whether or not the row-identifying value is a
	 *  should be
	 *  wrapped in quotes, for the same reason as isString[]
	 *  array.
	 *
	 * @return returnIntegers for the feedback received from MySQL
	 *
	 * @throws SQLException
	 */
	public int[] updateRow
	(String tableName, String[] columnNames, String[] newValues, String idValue,
	 String idColumnName, boolean[] isString, boolean idIsString)
	throws SQLException {
		
		idValue = quoteWrap(idValue, idIsString);
		int[] returnIntegers = new int[columnNames.length];
		
		for(int i = 0; i < newValues.length; i++) {
			String newValue = quoteWrap(newValues[i], isString[i]);
			
			String sql = String.format("UPDATE %s SET %s = %s WHERE %s = %s;",
			 tableName, columnNames[i], newValue,
			 idColumnName, idValue);
			
			returnIntegers[i] = connection.createStatement().executeUpdate(sql);
		}
		return returnIntegers;
	}
	
	/**
	 * update a record with new information
	 *
	 * @param columns
	 *  The String[] of columns in the specified record to update
	 * @param values
	 *  The Object[] of new values to be assigned to column names.
	 *  <code>columnNames</code> and <code>values</code> should be
	 *  the same length
	 * @param id
	 *  Object whose value indicates the correct rowvalue
	 * @param tableName
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	public Object updateRow
	(String[] columns, Object[] values, Object id, String idValue, String tableName)
	throws SQLException {
		
		StringFormat sf = new StringFormat("UPDATE %s SET ", tableName);
		
		for(int i = 0; i < columns.length; i++) {
			String column = columns[i];
			String value = quoteWrap(values[i]);
			String comma = getComma(columns.length, i, "");
			
			sf.appendf("%s = %s%s", column, value, comma);
		}
		
		String idx = quoteWrap(id);
		sf.appendf(" where %s=%s", id, quoteWrap(idValue));
		
		return connection.createStatement().executeUpdate(sf + "");
	}
	
	/**
	 * @param tableName
	 *  name of the table
	 * @param columns
	 *  name of all the table's columns
	 * @param fileName
	 *  name of the file
	 * @param scanner
	 *  scanner passed in to read each record of the .csv file
	 *
	 * @throws SQLException
	 */
	//TODO: actually use the fileName
	private boolean upLoadTable
	(String tableName, String[] columns, String fileName, Scanner scanner)
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
					
					if(i == MAX_LOOPS - 1) {
						sf.append(";");
					}
					else {
						sf.append(",");
					}
					
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
			
			JOptionPane.showMessageDialog(null, String.format(
			 "The csv file has been exported to %s in the %s database.",
			 tableName, DB_NAME));
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a gui to get user input on a new table to be uploaded to MySQL
	 * database.
	 *
	 * @param columns
	 *  String[] of column names to be used, from the first line of
	 *  a .csv file
	 *  The Scanner passed on to this method has already read te
	 *  first line and crated <code>columns</code> from it.
	 */
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
		constraints.gridy = ++gridy;
		
		constraints.gridx = 0;
		JLabel nameLabel = new JLabel("Table Name:");
		panel.add(nameLabel, constraints);
		JTextField nameField = new JTextField();
		nameField.setColumns(20);
		constraints.gridx = 1;
		panel.add(nameField, constraints);
		
		constraints.gridy = ++gridy;
		constraints.gridx = 3;
		panel.add(primaryColumnLabel, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.gridy = ++gridy;
		++gridy;
		constraints.gridwidth = 1;
		ButtonGroup buttonGroup = new ButtonGroup();
		int i = 0;
		for(; i < boxes.length; i++) {
			constraints.gridy = ++gridy;
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
		constraints.gridy = i + ++gridy;
		;
		panel.add(radioButtons[i], constraints);
		
		JButton ok = new JButton("Ok");
		ok.addActionListener(e->{
			
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
				if(!foundButton) {
					PRIMARY_KEY.setValue("idx".trim());
					PRIMARY_KEY.setValue("int(16)".trim());
				}
				typeMap = new HashMap<>();
				for(int j = 0; j < boxes.length; j++) {
					typeMap.put(j, (boxes[j].getSelectedItem() + "").trim());
				}
				try {
					upLoadTable(tableName, columns, finalFileName,
					 new Scanner(new File(finalFileName)));
				}
				catch(SQLException | FileNotFoundException throwables) {
					throwables.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++gridy;
		panel.add(ok, constraints);
		constraints.gridx = 1;
		JButton cancel = new JButton("Cancel");
		Dimension dim = frame.getContentPane().getPreferredSize();
		scrollPane.setMaximumSize(new Dimension(dim.width - 1, dim.height - 1));
		cancel.addActionListener(e->{
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
	
	/**
	 * Formats the String to be sent as sql code
	 * <p>
	 * insert the
	 * new row into
	 *
	 * @param array
	 *  the values of each column
	 *  type of
	 *  data each column is
	 *
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
			}
			else {
				sf.append(",");
			}
		}
		return sf.toString();
	}
	
	/**
	 * create the tuple of column names as a String to be sent
	 * as sql code
	 *
	 * @return the column titles, comma separated, in
	 *  parentheses start.e., (c1, c2,...cn)
	 */
	private static String getcolumnTuple(String[] columnNames) {
		
		if(columnNames.length == 0) {
			return null;
		}
		
		StringFormat sf = new StringFormat("(");
		int i = 0;
		for(; i < columnNames.length - 1; i++) {
			sf.append(columnNames[i]).append(",");
		}
		
		return sf.append(columnNames[i]).append(")").toString();
	}
	
	/**
	 * public method invokes te internal call to <code>createBlankTable</code>.
	 */
	public void upLoadTable() {
		
		try {
			createBlankTable();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the process of getting user input for information about the table to
	 * be created and uploaded. Summons a gui to collect the input.
	 *
	 * the .csv file to make a table from.
	 *
	 * @throws Exception
	 *  if the file type is not supported or found.
	 */
	private void createBlankTable() throws Exception {
		
		String filePath = JOptionPane.showInputDialog(null, "Open file:");
		int[] count = null;
		if(!filePath.endsWith(".csv")) {
			//Todo: throw new FileNotSupportedException
			return;
		}
		
		scanner = new Scanner(new File(filePath));
		String[] columns = scanner.nextLine().split(",");
		columns[0] = removeUTF8BOM(columns[0]);
		guiUpload(columns, filePath);
	}
	
	/**
	 * Removes the (BOM byte-order mark) from the beginning of the string.
	 *
	 * @param s
	 *  the first string of the file
	 *
	 * @return the original string if there is no byte mark. otherwise a substring
	 *  with the byte mark removed
	 */
	private static String removeUTF8BOM(String s) {
		
		if(s.startsWith("\uFEFF")) {
			s = s.substring(1);
		}
		return s;
	}
	
	/**
	 * Delete the record in the specified table
	 *
	 * @param table
	 *  contains the target record
	 * @param idColumn
	 *  the column which has a unique value to identify the record
	 * @param idValue
	 *  the value of the identifying column
	 *
	 * @return int = either the row count for SQL Data Manipulation Language (DML)
	 *  statements, or 0 for SQL statements that return nothing
	 *
	 * @throws SQLException
	 *  database access errors
	 */
	public int deleteRecord
	(String table, String idColumn, Object idValue)
	throws SQLException {
		
		ArrayList<String> arrays = new ArrayList<>();
		return connection.createStatement().executeUpdate(String.format(
		 "DELETE FROM %s WHERE %s = %s",
		 table, idColumn, idValue));
	}
	
	/**
	 * Deletes all records from a table, but the table remains
	 *
	 * @param table
	 *  to delete all records in
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	public int deleteAllRecords(String table) throws SQLException {
		
		return connection.createStatement().executeUpdate("DELETE FROM " + table);
	}
	
	/**
	 * deletes an entire table
	 *
	 * @param tableName
	 *  to be deleted
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	public static int deleteTable(String tableName) throws SQLException {
		
		return connection.createStatement().executeUpdate(
		 "DROP TABLE IF EXISTS " + tableName);
	}
	
	/**
	 * helper method to clean up code when concatenating commas for sql code.
	 *
	 * @param length
	 *  the length of the array to check against
	 * @param lastChar
	 *  string to append to the end of a list of sql elements
	 *  in a query.
	 *  (usually a
	 *  parentheses or empty string).
	 * @param lastChar
	 *  the
	 *
	 * @return either a comma if there are more elements left to iterate through
	 *  (as calculated
	 *  by <code>length</code>, or the ending character
	 */
	private static String getComma(int length, int i, String lastChar) {
		
		if(i < length - 1) {
			return ", ";
		}
		return lastChar + "";
	}
	
	/**
	 * wraps the given object in quotes if it is a string
	 *
	 * @param columnValue
	 *  some value to check
	 *
	 * @return a new string with quotes around it, if it is a String. Otherwise,
	 *  the string representation of the object.
	 */
	private static String quoteWrap(Object columnValue) {
		
		if(columnValue instanceof String) {
			return "'" + columnValue + "'";
		}
		return columnValue.toString();
	}
	
	/**
	 * wraps the given String in quotes if it actually represents String object.
	 * Similar to the above method, but used in cases where all object types are
	 * being handled as strings.
	 *
	 * @param value
	 *  some value to check
	 * @param isString
	 *  some boolean value to determine if <code>value</code>
	 *  should be wrapped in quotes
	 *
	 * @return a new string with quotes around it, if the <code>value</code>
	 *  actually represents a String. Otherwise, the original string.
	 */
	public String quoteWrap(String value, boolean isString) {
		
		if(isString) {
			return "'" + value + "'";
		}
		return value;
	}
	
	public ArrayList<String> getTables() throws SQLException {
		
		ResultSet rs = connection.createStatement().executeQuery("SHOW tables");
		
		ArrayList<String> tables = new ArrayList<String>();
		
		while(rs.next()) {
			tables.add(rs.getString(1));
		}
		return tables;
	}
	
	/**
	 * get the URL by joining static variable together.
	 *
	 * @return the beginning of the  string for the url (without username and
	 *  password)
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
	public static final Map<Integer, String> J_TO_SQL = Map
	 .ofEntries(
	  entry(
	   STRING, "VARCHAR(16)"),
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
	  entry(
	   "VARCHAR", "VARCHAR(16)"),
	  entry("CHAR", "CHAR"),
	  entry("LONGVARCHAR", "VARCHAR(32)"),
	  entry("BOOLBIT", "BIT"),
	  entry("INT", "int(8)"),
	  entry("DOUBLE", "decimal(13,2)"),
	  entry("DATE", "DATE"),
	  entry("TIME", "TIME"),
	  entry("TIMESTAMP", "TIMESTAMP"));
	
	/**
	 * tester method
	 *
	 * @param query
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	public ResultSet query(String query) throws SQLException {
		
		return connection.createStatement().executeQuery(query);
	}
	
	/**
	 * @param name
	 *
	 * @return
	 */
	public String getType(String name) {
		
		return J_TO_SQL2.get(name);
	}
	
	/**
	 * subtract the desired amount of given product from quantity to make a sale
	 *
	 * @param productId
	 *  the target product
	 * @param desiredQuantity
	 *  the quantity the buyer wants to buy
	 *
	 * @return -1 if the product id is unknown (not in the table). -2 if there is not
	 *  enough in stock to complete the order. otherwise, the new quantity after
	 *  subtracting the desired quantity
	 *
	 * @throws SQLException
	 */
	public int processOrder(String productId, int desiredQuantity) throws SQLException {
		
		String sql = new StringFormat(
		 "select quantity from %s where product_id = '%s';", tableName, productId) + "";
		ResultSet rs = connection.createStatement().executeQuery(sql);
		if(!rs.next()) {
			System.out.println("unknown product id");
			return -1;
		}
		int currentQuantity = rs.getInt(1);
		if(currentQuantity < desiredQuantity) {
			System.out.println(
			 "not enough inventory of product " + productId + " to fulfill order of " +
			 desiredQuantity);
			return -2;
		}
		currentQuantity -= desiredQuantity;
		sql = new StringFormat("UPDATE %s SET quantity = %s WHERE product_id" +
							   " = '%s';", tableName, currentQuantity, productId) + "";
		connection.createStatement().executeUpdate(sql);
		return currentQuantity;
	}
	
	public void setWorkingTable(String tableName) {
		
		CRUDBuddy.tableName = tableName;
	}
	
	public void recordOrder(Order order) throws SQLException {
		
		String sql =
		 "INSERT INTO `sales` (quantity, date, customer_email, customer_location, " +
		 "product_id, order_id) VALUES" +
		 order.getSalesTuple();
		connection.createStatement().executeUpdate(sql);
	}
}
	

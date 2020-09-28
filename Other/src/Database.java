import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database class holds a collection of Table objects for relational operations,
 * and a buffer for deleted entries. A private class HistoryEntry is used
 * to store the name, and entry.
 */

/*
 * TODO:
 *  -Add catch for try, throw catch
 *  -Specify any or all data being returned from a function/ method
 *  (@return the table and all data)
 *  -
 *  - Store which table each entry in the buffer came from
 *  (can be completed later)
 *
 * Todod:
 * -Add all "End ..." comments to classes/ functions (The functions
 *  does NOT return a string)
 * */

public class Database {
	/**
	 * public class Database:
	 *
	 * holds a HashMap of Table objects for future relational 
	 * operations between them.
	 */
	/**
	 * private class HistoryEntry:
	 * <p>
	 * holds on to deleted DataEntry productIds and
	 * entries
	 * oldEntries - holds an array DataEntry objects that have been pushed out
	 * MAX_HISTORY_ENTRIES - the capacity of the buffer.
	 * bufferIndex - the index of where to insert the next HistoryEntry objet
	 */
	private HashMap<String, Table> tables;
	private HistoryEntry[] oldEntries;
	private int bufferIndex;
	public static final int MAX_HISTORY_ENTRIES = 400;
	
	/**
	 * checks to see if the specified table name already exists.
	 *
	 * @param tableName table name to check
	 * @return true if the name is taken. Otherwise, false
	 */
	boolean contains(String tableName) {
		return tables.containsKey(tableName);
	} // End contains

	public int size() {
		return tables.size();
	}

	private class HistoryEntry {
		private String productId;
		private DataRow entry;
		
		// Todod: Be consistent with variable names in HistoryEntry
		public HistoryEntry(String name, DataRow entry) {
			this.productId = name;
			this.entry = entry;
		} // End HistoryEntry
	} // End private class HistoryEntry
	
	public Database() {
		tables = new HashMap<>();
		oldEntries = new HistoryEntry[MAX_HISTORY_ENTRIES];
		bufferIndex = 0;
	} // End Database
	
	/**
	 * adds a preexisting table to the DataBase
	 *
	 * @param newTable the table to add to the database.
	 * @return the old Table if the table name was already taken.
	 * otherwise
	 * null.
	 */
	public Table addTable(Table newTable) {
		String tableName = newTable.getName();
		Table oldTable = null;
		if(contains(newTable.getName())) {
			oldTable = tables.get(tableName);
		}
		tables.put(tableName, newTable);
		return oldTable;
	} // End addTable


	/**
	 * Create a new table an empty Table, and call the other createBlankTable(Table newTable)
	 * method to do it.
	 *
	 * @param name unique name of the table
	 * @return null if the table name is not taken. otherwise, the old Table
	 */
	public Table createEmptyTable(String name) {
		return addTable(new Table(name));
	} // End createEmptyTable(name)
	
	/**
	 * Create a new table an filled Table with the specified file, and call 
	 * the createBlankTable(Table newTable) method to do it.
	 *
	 * @param tableName unique name of the table
	 * @return null if the table name is not taken. otherwise, the old Table
	 */
	public Table createEmptyTable(String fileName, String tableName) throws FileNotFoundException {
		return addTable(new Table(fileName, tableName));
	} // End createEmptyTable(fileName, tableName)
	
	/**
	 * Creates a new entry in the specified table.
	 *
	 * @param tableName name of an existing table.
	 * @param entry     The new DataEntry object to insert into the specified
	 *                  table.
	 * @return null if the productId is not present in the specified Table.
	 * Otherwise, the old DataEntry object that was pushed out.
	 */
	public DataRow createEntry(String tableName, DataRow entry) {
		DataRow oldEntry = null;
		if(tables.containsKey(tableName)) {
			Table table = tables.get(tableName);
			oldEntry = table.create(entry.getProductId(), entry);
			dataToHistory(oldEntry);
		} // End if
		return oldEntry;
	} // End createEntry
	
	/**
	 * Selects and returns the string representation of an entire table
	 *
	 * @param tableName name of an existing table.
	 * @return string representation the table
	 * @throws NullPointerException if the specified table does not exist.
	 */
	public String readTable(String tableName) {
		if(tables.containsKey(tableName)) {
			return tables.get(tableName).toString();
		} // End if
		System.out
		 .println("Error: The table " + tableName + " does not exist.");
		throw new NullPointerException();
	} // End ReadTable
	
	/**
	 * Selects and returns the string representation of the specified entry in
	 * the
	 * specified table.
	 *
	 * @param tableName name of an existing table.
	 * @param productId name of an existing entry in the specified table.
	 * @return string representation the entry
	 */
	// Todod: Use "productID" when referencing string variables for data entry
	// objects
	public String readEntry(String tableName, String productId) {
		if(tables.containsKey(tableName)) {
			Table table = tables.get(tableName);
			return table.read(productId).toReadableString();
		} // End if
		System.out
		 .println("Error: The table " + tableName + " does not exist.");
		throw new NullPointerException();
	} // End readEntry
	
	/**
	 * Generates new .csv files for every table in the Database object.
	 *
	 * @return an array of File objets to write to the disk.
	 * @throws FileNotFoundException because IntelliJ said so.
	 */
	//Todod: -Rename above functions so confusion is avoided, ie 
	// "UpadateALLTables"
	public File[] updateAllTables() throws FileNotFoundException {
		File[] files = new File[tables.size()];
		int i = 0;
		for(Table table: tables.values()) {
			files[i++] = updateTable(table);
		} // End for
		return files;
	} // End updateAllTables()
	
	/**
	 * Generates new .csv file for one table in the Database object.
	 *
	 * @return a File objet to write to the disk.
	 * @throws FileNotFoundException because IntelliJ said so.
	 */
	// Todod:  Write accurate documentation for, "updateTable" & 
	// "updateAllTables"
	public File updateTable(Table table) throws FileNotFoundException {
		return table.update(table.getName() + ".csv");
	} // End updateTable
	
	/**
	 * Looks in all tables to find the DataEntry.
	 *
	 * @param entry the DataEntry to search for in all tables
	 * @return an ArrayList of the tables that contain the specified entry
	 */
	public ArrayList<Table> find(DataRow entry) {
		ArrayList<Table> foundTables = new ArrayList<>();
		for(Table table: tables.values()) {
			if(table.contains(entry)) {
				foundTables.add(table);
			} // End for
		} // End if
		return foundTables;
	} // End find
	
	/**
	 * Removes the specified table from the Database collection of Table
	 * objects if
	 * it exists.
	 *
	 * @param tableName name of a specified table.
	 * @return the table that was removed from the database, if it exists.
	 * Otherwise
	 * null.
	 */
	public Table deleteTable(String tableName) {
		if(tables.containsKey(tableName)) {
			Table table = tables.get(tableName);
			tables.remove(tableName);
			return table;
		} // End if
		return null;
	} // End deleteTable
	
	/**
	 * Deletes the specified entry in the specified table.
	 *
	 * @param tableName name of an existing table.
	 * @param entryName name of an existing entry in the specified table.
	 * @return the entry which was deleted
	 */
	// Todod: -Change @ return name in deleteEntry to explicitly return the 
	// deleted data
	public DataRow deleteEntry(String tableName, String entryName) {
		if(tables.containsKey(tableName)) {
			Table table = tables.get(tableName);
			DataRow oldEntry = table.delete(entryName);
			dataToHistory(oldEntry);
			return oldEntry;
		} // End if
		return null;
	} // End deletEntry
	
	/**
	 * Stores removed data entries in a buffer, if there is capacity left.
	 *
	 * @param oldEntry DataEntry object to be stored
	 *                 false.
	 */
	private void dataToHistory(DataRow oldEntry) {
		if(oldEntry != null) {
			oldEntries[bufferIndex++] =
			 new HistoryEntry(oldEntry.getProductId(), oldEntry);
			bufferIndex %= MAX_HISTORY_ENTRIES;
		} // End if
	} // End dataTo History
	
	/**
	 * Creates a new Table from, given a .csv file and name for the new Table
	 * object
	 * Calls a constructor in Table.java to read all entries from the .csv
	 * file, adds this new Table to the HashMap called Tables, and returns the
	 *
	 * @param fileName  the name of he .csv file to read from.
	 * @param tableName the name of the table, which must not be the same as a
	 *                  preexisting table.
	 * @return table that was constructed.
	 */
	public Table createFromCsv(String fileName, String tableName) throws FileNotFoundException {
		Table table = new Table(fileName, tableName);
		tables.put(tableName, table);
		return table;
	} // End createFromCsv
} // End class DataBase 

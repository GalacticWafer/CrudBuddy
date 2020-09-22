/*
 * TODO:
 *  -Format everything in this file to 80 characters long
 *  -Add description on what this "Table.java" file is doing
 *  -Add proper documentation for the Table class
 *  -Add proper documentation for Table constructor
 *  -Format "Datarow create" and add a viable description for what this
 * function is doing
 *   (consider renaming this function to something more descriptive)
 *   (add @description at beginning)
 *  -Add proper documentation for all "Datarow"'s
 *   (is this a helper or reagular function or... )
 *   (add a description if not present)
 *   add documentation for helper method delete
 *   format helper method delete
 *  -Add ! on "if (data.containsKey(row))" from delete method
 *  -Add proper documentation for "toString" method
 *  -Add proper documentation for "Iterator<T> iterator"
 *  -Add proper documentation for "size" and "get name"
 *  -Expand upon documentation for "contains"
 * */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @param <T>
 */
public class Table<T> implements Iterable<T> {
	private Hashtable<String, DataRow> data;
	private String name;
	
	/**
	 * A new table in the database
	 *
	 * @param name name of the table
	 */
	public Table(String name) {
		data = new Hashtable<>();
		this.name = name;
	}
	
	/**
	 * Constructor makes a new Table object from a file name and table name. 
	 * Reads the file twice. The first time, counts lines to make a large 
	 * enough HashTable for speed of insertions.
	 * The second time, and makes new entries for every line.
	 *
	 * @param fileName  the name of the file to be created
	 * @param tableName the name of the newly-created table
	 * @throws FileNotFoundException if the .csv file name is invalid or not 
	 * found
	 */
	public Table(String fileName, String tableName) throws FileNotFoundException {
		File file = new File(fileName);
		Scanner scanner = new Scanner(file);
		int lineCount;
		for(lineCount = 0; scanner.hasNextLine(); lineCount++) {
			scanner.nextLine();
		}
		this.name = tableName;
		data = new Hashtable<>(lineCount);
		scanner = new Scanner(file);
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String[] temp = scanner.nextLine().split(",");
			String PRODUCT_ID = temp[0];
			Integer QUANTITY = Integer.parseInt(temp[1]);
			double WHOLESALE_COST = Double.parseDouble(temp[2]);
			double SALE_PRICE = Float.parseFloat(temp[3]);
			String SUPPLIER_ID = temp[4];
			create(PRODUCT_ID, new DataRow(
			 PRODUCT_ID,
			 QUANTITY,
			 WHOLESALE_COST,
			 SALE_PRICE,
			 SUPPLIER_ID
			));
			if(scanner.hasNextLine()) {
				scanner.nextLine();
			}
		}
	}
	
	/**
	 * @param productId TODO: finish
	 * @param dataRow     TODO: finish
	 * @return the the previous Datarow associated with productId, or null if
	 * there was no mapping for productId . (A null return can also indicate
	 * that the map previously associated null with key.)
	 */
	public DataRow create(String productId, DataRow dataRow) {
		return (data.put(dataRow.getProductId(), dataRow));
	}
	
	/**
	 * @param outputPath the directory in which to write the new .csv file
	 * @throws IllegalArgumentException if the file is not .csv
	 * @throws FileNotFoundException    if for some reason the file could
	 *                                  not be
	 *                                  located
	 */
	public File update(String outputPath) throws FileNotFoundException,
	 IllegalArgumentException {
		if(!outputPath.endsWith(".csv")) {
			throw new IllegalArgumentException(
			 "The file name must end with \"" +
			  ".csv\"");
		}
		File outputFile = new File(outputPath);
		PrintWriter writer = new PrintWriter(outputFile);
		writer.print(toString() + "\n");
		writer.close();
		return outputFile;
	}
	
	/**
	 * @param row the row to attempt to delete
	 * @return an row if it was deleted. Otherwise null.
	 */
	public DataRow delete(String row) {
		if(data.containsKey(row)) {
			return null;
		}
		DataRow temp = data.get(row);
		data.remove(row);
		return temp;
	}
	
	public DataRow delete(DataRow dataRow) {
		return delete(dataRow.getProductId());
	}
	
	/**
	 * @param id key of the row to return.
	 * @return null if the productID is not associated with a Datarow.
	 */
	public DataRow read(String id) {
		return data.get(id);
	}
	
	/**
	 * @return the String representation of the underlying TreeMap
	 */
	@Override
	public String toString() {
		if(data.isEmpty()) {
			return "";
		} // End if
		StringBuilder s = new StringBuilder();
		for(DataRow dataRow : data.values()) {
			s.append(dataRow).append("\n");
		} // End for
		s.setLength(s.length() - 1);
		return s.toString();
	}
	
	/**
	 * @return a HashTable iterator for enhanced for loops
	 */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			final Iterator<Map.Entry<String, DataRow>> iterator =
			 data.entrySet().iterator();
			Map.Entry<String, DataRow> current;
			
			@Override
			public boolean hasNext() {
				return current != null;
			}
			
			@Override
			public T next(){
				Map.Entry<String, DataRow> temp = iterator.next();
				current = iterator.next();
				return (T)temp;
			}
		};
	}
	
	public int size() {
		return data.size();
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * See if the table has an row
	 *
	 * @param dataRow Datarow that should be compared for equality
	 * @return true if data has an equivalent row. Otherwise false.
	 */
	public boolean contains(DataRow dataRow) {
		if(data.containsKey(dataRow.getProductId())) {
			DataRow matchedrow = data.get(dataRow.getProductId());
			return dataRow.equals(matchedrow);
		}
		return false;
	}

	@Override public boolean equals(Object obj) {
		if(obj instanceof Table) {
			Table other = (Table)obj;
			return this.data.equals(((Table<?>) obj).data);
		}
		return false;
	}
	
	public void update(String fileName, Table table) throws FileNotFoundException {
		table.update(fileName);
	}
}

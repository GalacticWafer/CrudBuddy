import com.mysql.cj.MysqlType;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import static org.junit.Assert.*;

class CRUDBuddyTest extends JPanel {
	public static final JFrame JF_1 = new JFrame();
	static JTable jTable;
	static JScrollPane scrollPane;
	static String userName = "GalacticWafer";
	static String password = "7!qaJ|B[t$";
	static String ipAddress = "45.79.55.190";
	static String portNumber = "3306";
	static String databaseName = "cs3250_project";
	static String tableName = "inventory";
	private CRUDBuddy crud =
	 new CRUDBuddy(userName, password, ipAddress, portNumber, databaseName);
	
	public static void main(String[] args)
	throws SQLException, ClassNotFoundException {
		
		//assertTableViewerGiuTest();
		
	}
	
	CRUDBuddyTest()
	throws SQLException, ClassNotFoundException {
		
	}
	
	@Test void assertTables() throws SQLException {
		
		ArrayList<String> list = new ArrayList<>();
		list.add("customers");
		list.add("inventory");
		list.add("sales");
		assertEquals(list, crud.getTables());
	}
	
	@Test void assertTableCreatedTest()
	throws SQLException, FileNotFoundException {
		
		String[] columnNames =
		 new String[] {
		  "product_id", "quantity", "wholesale_cost", "sale_price",
		  "supplier_id"
		 };
		HashMap<Integer, String> typeMap = new HashMap<>();
		typeMap.put(0, crud.getType(MysqlType.VARCHAR.getName()));
		typeMap.put(1, crud.getType(MysqlType.INT.getName()));
		typeMap.put(2, crud.getType(MysqlType.DOUBLE.getName()));
		typeMap.put(3, crud.getType(MysqlType.DOUBLE.getName()));
		typeMap.put(4, crud.getType(MysqlType.VARCHAR.getName()));
		crud.createBlankTable("inventory", columnNames, typeMap);
	}
	
	@Test void assertReadColumnNamesTest()
	throws SQLException {
		
		ArrayList<String> correctList = new ArrayList<>();
		correctList.add("idx");
		correctList.add("product_id");
		correctList.add("quantity");
		correctList.add("sale_price");
		correctList.add("supplier_id");
		correctList.add("wholesale_cost");
		
		ArrayList<String> columnNames =
		 crud.readColumnNames(databaseName, tableName);
		
		System.out.println(columnNames);
		
		assertEquals(columnNames, correctList);
	}
	
	@Test void assertReadColumnTypesTest()
	throws SQLException {
		
		ArrayList<String> columnTypes = crud.readColumnTypes(tableName);
		
		System.out.println(columnTypes);
	}
	
	@Test void assertTableToCSVTest()
	throws SQLException, FileNotFoundException {
		
		ArrayList<String> columns = crud.readColumnNames(databaseName, tableName);
		ResultSet results = crud.readAllRecords(tableName);
		File file = crud.writeToFile("sample_output.csv", columns, results);
		Scanner sc = new Scanner(file);
		
		while(sc.hasNextLine()) {
			System.out.println(sc.nextLine());
		}
	}
	
	@Test void assertReadAndUpdateTest()
	throws SQLException {
		// row identity information
		String identifierColumn = "product_id";
		String identifierValue = "ZRDATK9CSM23";
		
		// A few of the column names to make sure we can updateRow their values
		String[] columnNames = {"quantity", "wholesale_cost", "sale_price"};
		
		HashMap<String, Object> results =
		 crud.readColumnValues(tableName, columnNames, identifierValue,
		  identifierColumn);
		System.out.println(results.toString());
		
		// random new values to updateRow
		DecimalFormat df = new DecimalFormat("0.00");
		String[] newValues = {
		 (new Random().nextInt() % 10000) + "",
		 (df.format(new Random().nextDouble())),
		 (df.format(new Random().nextDouble()))
		};
		
		// because "product_id" is a string, but this may not be the thing we identify in
		// every case in the future
		boolean idIsString = true;
		
		// array indicating if the same index in newValues above, is a String
		boolean[] valueIsString = {false, false, false};
		
		crud.updateRow(tableName, columnNames, newValues, identifierValue,
		 identifierColumn, valueIsString, idIsString);
		
		System.out.println(
		 "After updateRow: " + crud.readColumnValues(
		  tableName, columnNames, identifierValue, identifierColumn).toString());
	}
	
	@Test void assertUploadTableGuiTest()
	throws SQLException, ClassNotFoundException {
		
		CRUDBuddy crud = new CRUDBuddy(
		 userName,
		 password,
		 ipAddress,
		 portNumber,
		 databaseName);
		//crud.upLoadTable();
	}
	
	@Test public static void assertTableViewerGiuTest(String tb, CRUDBuddy crud)
	throws SQLException, ClassNotFoundException {
		
		tableName = tb;
		CRUDBuddyTest t = new CRUDBuddyTest();
		//CRUDBuddy crud = new CRUDBuddy(userName,
		// password,
		// ipAddress,
		// portNumber,
		// databaseName);
		ArrayList<String> temp = crud.readColumnNames(databaseName, tableName);
		String columnNames = (temp + "").substring(1, (temp + "").length() - 1);
		ResultSet rs = crud.query("Select " + columnNames + " from " + tableName);
		
		//JTable jt;
		int i = 0;
		ArrayList<Object[]> rows = new ArrayList<>();
		while(rs.next()) {
			i++;
			var colItr = temp.iterator();
			
			Object[] objects = new Object[temp.size()];
			for(int j = 0; colItr.hasNext(); j++) {
				objects[j] = rs.getObject(colItr.next());
			}
			rows.add(objects);
		}
		Iterator<Object[]> row_it = rows.iterator();
		Object[][] data = new Object[i][temp.size()];
		for(int i1 = 0; i1 < data.length; i1++) {
			data[i1] = row_it.next();
		}
		TableFormatter tf = new TableFormatter(data, columnNames.split(","), crud);
		jTable = tf.getTable();
		jTable.setModel(new DefaultTableModel(data, columnNames.split(",")));
		for(int j = 0; j < 6; j++) {
			//TableColumn tColumn = table.getColumnModel().getColumn(j);
			//tColumn.setCellRenderer(new ColumnColorRenderer(Color.black, (j == 0 ?
			// Color.yellow : Color.white)));
		}
		tf.setData();
		
		//table.getTableHeader().setBackground(Color.black);
		//table.getTableHeader().setForeground(Color.yellow);
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(jTable);
		//scrollPane.getVerticalScrollBar().setBackground(Color.BLACK);
		//scrollPane.getVerticalScrollBar().setForeground(Color.yellow);
		
		scrollPane.setMinimumSize(tf.getScrollPanelSize());
		scrollPane.setPreferredSize(tf.getScrollPanelSize());
		JF_1.add(scrollPane);
		
		JF_1.setTitle("Test");
		Dimension onlySize = new Dimension(
		 t.scrollPane.getPreferredSize().width + 50,
		 t.scrollPane.getPreferredSize().height + 50);
		JF_1.setMinimumSize(onlySize);
		JF_1.setMaximumSize(onlySize);
		JF_1.setVisible(true);
		JF_1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JF_1.add(t);
	}
	
	private class ColumnColorRenderer extends DefaultTableCellRenderer {
		Color backgroundColor, foregroundColor;
		
		public ColumnColorRenderer(Color backgroundColor,
								   Color foregroundColor) {
			
			super();
			this.backgroundColor = backgroundColor;
			this.foregroundColor = foregroundColor;
		}
		
		public Component getTableCellRendererComponent(JTable table,
													   Object value,
													   boolean isSelected,
													   boolean hasFocus,
													   int row,
													   int column) {
			
			Component cell =
			 super.getTableCellRendererComponent(
			  table,
			  value,
			  isSelected,
			  hasFocus,
			  row,
			  column);
			cell.setBackground(backgroundColor);
			cell.setForeground(foregroundColor);
			return cell;
		}
	}
}


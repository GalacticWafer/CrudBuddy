import com.mysql.cj.MysqlType;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

class CrudTest extends JPanel {
	public static final JFrame JF_1 = new JFrame();
	private static final String[] CORRECT_TABLE_NAMES =
	 new String[] {"inventory", "sales", "customers"};
	private static final String[] CORRECT_COLUMN_NAMES =
	 new String[] {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};
	private static final int LENGTH = CORRECT_COLUMN_NAMES.length;
	
	static JTable jTable;
	static JScrollPane scrollPane;
	static String userName = "GalacticWafer";
	static String password = "7!qaJ|B[t$";
	static String ipAddress = "45.79.55.190";
	static String portNumber = "3306";
	static String databaseName = "cs3250_project";
	static String tableName = "inventory";
	private static Crud crud;
	
	static {
		try {
			crud = Credentials.databaseLogin();
		}
		catch(SQLException | ClassNotFoundException throwables) {
			throwables.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	throws SQLException {
		assertTableViewerGiuTest(tableName, crud);
	}
	
	CrudTest() {}
	
	@Test void assertTables() throws SQLException {
		String[] CORRECT_TABLE_NAMES = new String[] {"inventory", "sales", "customers"};
		assertArrayEquals(CORRECT_TABLE_NAMES, crud.getTableNames());
	}
	
	@Test void assertTableCreatedTest()
	throws SQLException {
		HashMap<Integer, String> typeMap = new HashMap<>();
		typeMap.put(0, crud.getType(MysqlType.VARCHAR.getName()));
		typeMap.put(1, crud.getType(MysqlType.INT.getName()));
		typeMap.put(2, crud.getType(MysqlType.DOUBLE.getName()));
		typeMap.put(3, crud.getType(MysqlType.DOUBLE.getName()));
		typeMap.put(4, crud.getType(MysqlType.VARCHAR.getName()));
		crud.insertTable("inventory", CORRECT_COLUMN_NAMES, typeMap);
	}
	
	@Test void assertReadColumnNamesTest()
	throws SQLException {
		String[] columnNames = crud.getColumnNames();
		System.out.println(Arrays.toString(columnNames));
		assertEquals(columnNames, CORRECT_COLUMN_NAMES);
	}
	
	@Test void assertReadColumnTypesTest()
	throws SQLException {
		
		String[] columnTypes = crud.getColumnTypes(tableName);
		
		System.out.println(Arrays.toString(columnTypes));
	}
	
	@Test void assertTableToCSVTest()
	throws SQLException, FileNotFoundException {
		crud.setWorkingTable("inventory");
		String[] columns = crud.getColumnNames();
		ResultSet results = crud.getAllRecords();
		File file = crud.writeToFile("sample_output.csv", columns, results);
		Scanner sc = new Scanner(file);
		
		while(sc.hasNextLine()) {
			System.out.println(sc.nextLine());
		}
	}
	
	@Test void assertUploadTableGuiTest() {
	}
	
	@Test public static void assertTableViewerGiuTest(String tb, Crud crud)
	throws SQLException {
		
		tableName = tb;
		CrudTest t = new CrudTest();
		String columnNames = crud.arrayToCSV(CORRECT_COLUMN_NAMES);
		ResultSet rs = crud.getAllRecords();
		
		//JTable jt;
		int i = 0;
		ArrayList<Object[]> rows = new ArrayList<>();
		while(rs.next()) {
			i++;
			for(String columnName: CORRECT_COLUMN_NAMES) {
				Object[] objects = new Object[LENGTH];
				for(int j = 0; j < LENGTH; j++) {
					objects[j] = rs.getObject(columnName);
				}
				rows.add(objects);
			}
			Iterator<Object[]> row_it = rows.iterator();
			Object[][] data = new Object[i][LENGTH];
			for(int i1 = 0; i1 < data.length; i1++) {
				data[i1] = row_it.next();
			}
			TableFormatter tf = new TableFormatter(data, columnNames.split(","), crud);
			jTable = tf.getTable();
			jTable.setModel(new DefaultTableModel(data, columnNames.split(",")));
			tf.setData();
			scrollPane = new JScrollPane();
			scrollPane.getViewport().add(jTable);
			scrollPane.setMinimumSize(tf.getScrollPanelSize());
			scrollPane.setPreferredSize(tf.getScrollPanelSize());
			JF_1.add(scrollPane);
			
			JF_1.setTitle("Test");
			Dimension onlySize = new Dimension(
			 scrollPane.getPreferredSize().width + 50,
			 scrollPane.getPreferredSize().height + 50);
			JF_1.setMinimumSize(onlySize);
			JF_1.setMaximumSize(onlySize);
			JF_1.setVisible(true);
			JF_1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JF_1.add(t);
		}
	}
}


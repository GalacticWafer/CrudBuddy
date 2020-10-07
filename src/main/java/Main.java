import javax.mail.MessagingException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Main {
	private static Crud crud;
	private static int i = 0;
	private static Object[][] testRecord;
	
	private static void dailyOrderCheck(Crud crud)
	throws MessagingException, IOException, SQLException {
		
		LinkedList<int[]> list = Emailer.processEmailOrders(crud);
		System.out.println(list);
	}
	
	private static void insertNewRecord(Crud crud, String tableName)
	throws SQLException {
		crud.setWorkingTable(tableName);
		Object[] insertFromOrder = crud.find("VBEUS2ETCKA4", "product_id");
		System.out.println(Arrays.toString(insertFromOrder));
		String someTextField = "sales";
		int columnCount = crud.getColumnCount(someTextField);
		System.out.println(columnCount);
		Object[] newRecord = new Object[columnCount];
		for(int i = 0; i < newRecord.length; i++) {
			newRecord[i] = null
			//fixme, insertRecord all the names of your JtextFields and such 
			// variables
			;
		}
	}
	
	public static void main(String[] args)
	throws Exception {
		crud = Credentials.databaseLogin();
		testRecord =
		 new Object[][] {{"A1B2C3D4E5F6", 500, 149.99, "TEST123", 299.99}};
		crud.insertRecords(crud.getColumnNames(), testRecord);
		crud.deleteAllRecords("sales");
		crud.deleteAllRecords("inventory");
		new GUI(crud);
		
		msgBox("First let's make sure the inventory table is in the original" +
			   "\nform given to us by uploading the table. Notice that " +
			   "\nthe sales table is  empty.");
	}
	
	public static void msgBox(String message) {
		message = "<html>" + message.replace("\n", "<br>") + "</html>";
		msgInvoke(new JLabel(message, JLabel.CENTER));
	}
	
	public static void msgBox(String message, String iconPath) {
		msgInvoke(new JLabel(message, new ImageIcon(iconPath), JLabel.CENTER));
	}
	
	public static void msgInvoke(JLabel label) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(
				 UIManager.getSystemLookAndFeelClassName());
			}
			catch(Exception ignored) {}
			label.setFont(new Font("Liberation Mono", Font.BOLD, 30));
			JFrame f = new JFrame();
			Container pane = f.getContentPane();
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridwidth = 15;
			constraints.insets = new Insets(10, 10, 10, 10);
			pane.setLayout(new GridBagLayout());
			pane.add(label, constraints);
			constraints.gridx = 8;
			constraints.gridwidth = 1;
			constraints.weightx = 1;
			JButton ok = new JButton("OK");
			pane.add(ok, constraints);
			ok.addActionListener(e -> {
				f.dispose();
				try {
					nextSlide();
				}
				catch(SQLException | FileNotFoundException throwables) {
					throwables.printStackTrace();
				}
			});
			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		});
	}
	
	private static void nextSlide() throws SQLException, FileNotFoundException {
		switch(i) {
			case 0:
				crud.setWorkingTable("inventory");
				msgBox("Check the inventory table to show test record.");
				break;
			case 1:
				crud.insertRecords(crud.getColumnNames(), testRecord);
				msgBox("Check the sales table to see the sales have been added.");
				break;
			case 2:
				crud.setWorkingTable("sales");
				new SalesProcessor(crud).processItems("little_order_test.csv");
				msgBox("Now for the full customer orders list, we'll upload the file.");
				break;
			case 3:
				crud.deleteRecord("inventory", "product_id", "A1B2C3D4E5F6");
				new SalesProcessor(crud).processItems("customer_orders_A_team4.csv");
				break;
		}
		i++;
	}
	
	private static void orderDemoSetup(Crud crud) throws SQLException {
		
		crud.setWorkingTable("inventory");
		Random rand = new Random();
		String[] testIds = new String[] {
		 "RHPXPHGBJS1P", "QSC2QT4FINCJ",
		 "64KOGZHOKLE4", "PHRXVC8ALL0F",
		 "VCNZ0DSD7YSZ", "G0L5248Q2Z3F",
		 "GZ7GOTL72VMS", "ILP04DM9GDK3",
		 "WVTYLB7W4GJ5", "B5LJR0ELKJXL",
		 "STPJASCJV013", "PYRQR50LBHXG",
		 "U9VRBFPT4DHP", "ZUVE5YZY005O"
		};
		String[] goodIds = new String[] {
		 "17TXG621YHM0", "5OAUECA58RWR",
		 "GSOB9EYF7O7G", "I5PDMXUJFVIY",
		 "B89RPM7ZZ7WG", "BVQT9IUZLOV0",
		 "198BKGR5C2YQ", "A1D5CZQM7PFA",
		 "CDTGG7B2FTIT", "GQ0WZUI7J6CT",
		 "J575EG1KGXDI", "NBGSOV8WIZ54",
		 "VJTR3EQFQZ5E", "6TWB0AUWETK7"
		};
		for(String testId: testIds) {
			crud
			 .updateRow(new String[] {"quantity"},
			  new Object[] {rand.nextInt(2000)},
			  "product_id", testId, "inventory");
		}
		
		for(String goodId: goodIds) {
			crud
			 .updateRow(new String[] {"quantity"}, new Object[] {999999999},
			  "product_id", goodId, "inventory");
		}
	}
}

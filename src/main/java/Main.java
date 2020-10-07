import org.apache.commons.text.WordUtils;

import javax.mail.MessagingException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class Main {
	private static Crud crud;
	private static GUI gui;
	private static int slide = 0;
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	private static Object[][] testRecord;
	
	public static void main(String[] args)
	throws Exception {
		crud = Credentials.databaseLogin();
		crud.deleteAllRecords("sales");
		gui = new GUI(crud);
		nextSlide();
	}
	
	public static void msgBox(String message) {
		message = "<html><pre>" +
				  WordUtils.wrap(message, 80)
						   .replaceAll("\t", "    ")
						   .replace("\n", "<br>") + 
				  "</pre></html>";
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
			label.setFont(new Font("Fira Code Retina", Font.BOLD, 24));
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
			int screenHeight =
			 toolkit.getScreenSize().height;
			JFrame gf = gui.getFrame();
			int width = f.getWidth();
			f.setLocation(
			 gf.getX() + (gf.getWidth() / 2) - (width / 2),
			 screenHeight - f.getHeight() - 40
			);
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		});
	}
	
	private static void nextSlide() throws SQLException,
										   FileNotFoundException {
		switch(slide) {
			case 0: {
				crud.update("Delete from inventory where product_id = " +
							"'A1B2C3D4E5F6'");
				crud.deleteAllRecords("sales");
				crud.setWorkingTable("inventory");
				crud.insertRecords(
				 crud.getColumnNames(),
				 new Object[][] {
				  {
				   "A1B2C3D4E5F6", 500, 149.99, "TEST123",
				   299.99
				  }
				 });
				msgBox(
				 "We have one tester item in the inventory table at the " +
				 "bottom, and a blank sales table. Notice that we only have " +
				 "500 quantity, but the second transaction calls for 1,000. " +
				 "Let's run three test transactions to see how it works.");
				break;
			}
			case 1: {
				LinkedList<Object[]> sales =
				 new SalesProcessor(crud).processItems("little_order_test" +
													   ".csv");
				String str = "";
				for(Object[] sale: sales) {
					TransactionItem item = new TransactionItem();
					String email = (String)sale[0];
					String location = (String)sale[1];
					LocalDate ld = LocalDate.parse((String)sale[2]);
					LocalDate accepted = LocalDate.parse((String)sale[3]);
					String productId = (String)sale[4];
					int quantity = (int)sale[5];
					item.setFields(ld, email, location, productId, quantity);
					item.setDateAccepted(accepted);
					str += item.toString() + "\n\n";
				}
				msgBox("We just ran three transactions:\n\n" +
				 str + "    Check the sales table to see the" +
				 " sales have been added.");
				break;
			}
			case 2: {
				crud.setWorkingTable("sales");
				msgBox("We could not fulfill the second order for an extra " +
					   "week due to the restocking delay time, but we could " +
					   "fulfill the third order immediately.");
				break;
			}
			case 3: {
				crud.deleteAllRecords("sales");
				crud.deleteTable("temp_table");
				msgBox("Now for the full customer orders list," +
					   "we'll remove these three records");
				break;
			}
			case 4: {
				crud.deleteAllRecords("sales");
				crud.deleteRecord("inventory", "product_id", "A1B2C3D4E5F6");
				msgBox("And now here are the results of the entire simulation " +
					   "file.");
			}
			case 5: {
				new SalesProcessor(crud)
				 .processItems("customer_orders_A_team4.csv");
			}
		}
		slide++;
	}
}

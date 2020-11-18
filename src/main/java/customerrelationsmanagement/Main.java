package customerrelationsmanagement;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class Main {
	public static final String INVENTORY_PATH = "inventory_team4.csv";
	public static final String ORDERS_PATH =
	 "customer_orders_A_team4.csv,"+
	// "customer_orders_A_team4_x3.csv,"+
	"customer_orders_A_team4_x5.csv," +
	  "customer_orders_A_team4_x7.csv";
	//null;
	public static final boolean START_GUI =
	 true;
	//false;
	public static final boolean START_MAIL =
	 // true;
	 false;
	public static final String analyticsDir =
	 //"analytics";
	 null;
	private static Credentials credentials;
	private static Crud crud;
	private static Emailer mailer;
	private static OrderProcessor orderProcessor;
	private static Restoration rest;
	
	public Main(Credentials credentials, Crud queryMaker)
	throws IOException, SQLException, ParseException {
		startServices(credentials);
	}
	
	public static void main(String[] args) {
		
		credentials = new Credentials();
	}
	
	public void startServices(Credentials credentials)
	throws IOException, SQLException, ParseException {
		
		crud = credentials.getCrud();
		if(START_MAIL) {
			mailer = new Emailer(credentials);
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override public void run() {
					try {
						mailer.processEmails(crud);
					} catch(MessagingException | SQLException | IOException e) {
						e.printStackTrace();
					}
				}
			}, 5000);
		}
		if(INVENTORY_PATH != null && !INVENTORY_PATH.equals("")) {
			long start = System.nanoTime();
			rest =
			 new Restoration(crud, INVENTORY_PATH, "customer_orders_A_team4" +
												   ".csv", true, analyticsDir);
			long l = System.nanoTime() - start;
			System.out.printf(
			 "%.2f seconds to delete the database and start over the gui\n",
			 (l / 1000000000.0));
		}
		if(START_GUI) {
			long start = System.nanoTime();
			new GUI(crud);
			long l = System.nanoTime() - start;
			System.out.printf(
			 "%.2f seconds to build the gui\n", (l / 1000000000.0));
		}
		if(ORDERS_PATH != null && !ORDERS_PATH.equals("")) {
			String[] fileNames = ORDERS_PATH.split(",");
			long[] timeValues = new long[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				long start = System.nanoTime();
				OrderProcessor.runFileOrders(crud, fileNames[i]);
				long end = System.nanoTime();
				long timeInterval = end - start;
				System.out
				 
				 .printf("%.2f seconds to process and insert %d new records", (
				   timeInterval /
				   1000000000.0),
				  (Integer.parseInt(crud
				   .getRecords("select count(*) from " + Tables.STATUSED)[0][0]
				   .toString())) +
				  (Integer.parseInt(crud
				   .getRecords("select count(*) from " + Tables.ANALYTICS)[0][0]
				   .toString())));
				timeValues[i] = timeInterval;
			}
			JFrame frame = new JFrame("Speed Test");
			ChartMaker maker = new ChartMaker(crud);
			JFreeChart chart = maker.getChart("string", ChartType.SPEED_TEST, timeValues,
			 new int[]{1455,7275,10185});
			ChartPanel cp = new ChartPanel(chart);
			frame.getContentPane().add(cp);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.pack();
			frame.setVisible(true);
		}
	}
}

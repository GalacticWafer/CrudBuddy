package customerrelationsmanagement;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class Main {
	public static final String INVENTORY_PATH =
	 //"inventory_team4.csv";
	null;
	
	public static final String ORDERS_PATH =
	 //"bad_orders.csv";
	 //"customer_orders_A_team4.csv";
	 //"customer_orders_final_team4.csv";
	 null;
	
	public static final int USERNAME = 0;
	public static final int USER_PASSWORD = 1;
	public static final int IP_ADDRESS = 2;
	public static final int PORT = 3;
	public static final int DATABASE_NAME = 4;
	public static final int SERVICE_EMAIL = 5;
	public static final int SERVICE_PASSWORD = 6;
	
	private static Crud crud;
	private static Emailer mailer;
	private static OrderProcessor orderProcessor;
	private static Restoration rest;
	public static final boolean runSpeedTest =
	 //true;
	 false;
	
	public static void main(String[] args)
	throws IOException, SQLException, ClassNotFoundException, ParseException,
		   MessagingException {
	/*	ArrayList<String> list = new ArrayList<>();
		Scanner scanner = new Scanner(new File("products.csv"));
		while(scanner.hasNextLine()) {
			list.add(scanner.nextLine());
		}
		scanner = new Scanner(new File("inventory_team4.csv"));
		scanner.nextLine();
		PrintWriter writer = new PrintWriter(new File("new_inventory_team4.csv"));
		Iterator<String> it = list.iterator();
		while(scanner.hasNextLine()) {
			if(!it.hasNext()){
				it = list.iterator();
			}
			String[] a = scanner.nextLine().split(",");
			writer.println(
			 a[0] + "," +
			 it.next() + "," +
			 a[1] + "," +
			 a[2] + "," +
			 a[3] + "," +
			 a[4]);
			
		}
		writer.close();
		
		*/
		String[] credentials = new String[7];
		Scanner configFile = new Scanner(new File("credentials.config"));
		for(int i = 0; configFile.hasNextLine(); i++) {
			credentials[i] = configFile.nextLine();
		}
		
		crud = new Crud(
		 credentials[USERNAME],
		 credentials[USER_PASSWORD],
		 credentials[IP_ADDRESS],
		 credentials[PORT],
		 credentials[DATABASE_NAME]);
		
		mailer = new Emailer(
		 crud,
		 credentials[SERVICE_EMAIL],
		 credentials[SERVICE_PASSWORD]
		);
		
		if(INVENTORY_PATH != null && !INVENTORY_PATH.equals("")) {
			rest =
			 new Restoration(crud, INVENTORY_PATH, true);
		}
		GUI.show(crud);
		
		if(ORDERS_PATH != null && !ORDERS_PATH.equals("")) {
			OrderProcessor.runFileOrders(crud, mailer, ORDERS_PATH);
		}

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override public void run() {
				try {
					System.out
					 .println("Check");// Shows how many times it's looped
					mailer.processEmails(crud);
				} catch(MessagingException | SQLException | IOException e) {
					e.printStackTrace();
				}
				try {
					OrderProcessor.checkUnstatusedSales(crud, mailer);
				} catch(SQLException | IOException | MessagingException e) {
					e.printStackTrace();
				}
			}
		}, 50, 5000);
	}
}

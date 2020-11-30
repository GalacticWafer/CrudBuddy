package customerrelationsmanagement;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
	//public static final String INVENTORY_PATH = "inventory_team4.csv";
	public static final String INVENTORY_PATH = null;
	
	//public static final String analyticsDir = "analytics";
	public static final String analyticsDir = null;
	
	//public static final String ORDERS_PATH = "customer_orders_A_team4.csv";
	public static final String ORDERS_PATH = "";
	
	public static final boolean START_GUI = true;
	//public static final boolean START_GUI = false;
	
	public static final boolean START_MAIL = false;
	//public static final boolean START_MAIL = true;
	
	private static Credentials credentials;
	private static Crud crud;
	private static Emailer mailer;
	private static OrderProcessor orderProcessor;
	private static Restoration rest;
	
	public Main(Credentials credentials, Crud queryMaker)
	throws IOException, SQLException, ParseException, MessagingException {
		startServices(credentials);
	}
	
	public static void main(String[] args) {
		credentials = new Credentials(); //DON'T WRITE ANYTHING IN THIS MAIN, EMAIL RUNS IN STARTSERVICES
	}
	
	
	public void startServices(Credentials credentials)
	throws IOException, SQLException, ParseException, MessagingException {
		
		
		
		this.crud = credentials.getCrud();
		if(INVENTORY_PATH != null && !INVENTORY_PATH.equals("")) {
			rest = new Restoration(crud, INVENTORY_PATH, "customer_orders_A_team4.csv",true, analyticsDir);
		}
		if(START_MAIL) {
			mailer = new Emailer(credentials);
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				int i = 0;
				@Override public void run() {
					try {
						//System.out.println("Check #" + i++); Shows how many times it's looped
						mailer.processEmails(crud);
					} catch(MessagingException | SQLException | IOException e) {
						e.printStackTrace();
					}
				}
			}, 50, 1000);
		}
		if(START_GUI) {
			new GUI(crud);
		}
		if(ORDERS_PATH != null && !ORDERS_PATH.equals("")) {
			OrderProcessor.runFileOrders(crud, ORDERS_PATH);
		}
		
		
		
	}
}

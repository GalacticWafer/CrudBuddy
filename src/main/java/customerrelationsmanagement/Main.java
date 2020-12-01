package customerrelationsmanagement;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
	public static final String INVENTORY_PATH = 
	 "inventory_team4.csv";
	 //null;
	
	public static final String ORDERS_PATH = 
	 "SupplierEvents.csv";
	//null;
	
	public static final boolean START_GUI = 
	 true;
	//false;
	
	public static final boolean START_MAIL = 
	 true;
	//false;
	
	
	public static final String analyticsDir = 
	 "analytics";
	//null;
	
	public static Credentials credentials;
	private static Crud crud;
	private static Emailer mailer;
	
	public Main(Credentials credentials)
	throws IOException, SQLException, ParseException {
		startServices(credentials);
	}
	
	public static void main(String[] args) {
		credentials = new Credentials();
	}
	
	public void startServices(Credentials credentials)
	throws IOException, SQLException, ParseException {
		
		crud = credentials.getCrud();
		
		if(INVENTORY_PATH != null) {
			new Restoration(crud, INVENTORY_PATH, 
			 "customer_orders_A_team4.csv", true, analyticsDir);
		}
		if(START_MAIL) {
			mailer = new Emailer(credentials);
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override public void run() {
					try {
						System.out.println("running");
						mailer.processEmails(crud);
						OrderProcessor.checkUnstatusedSales(crud);
					} catch(MessagingException | SQLException | IOException e) {
						e.printStackTrace();
					}
				}
			}, 1000, 3000);
		}
		if(START_GUI) {
			new GUI(crud);
		}
		if(ORDERS_PATH != null) {
			OrderProcessor.runFileOrders(crud, ORDERS_PATH);
		}
	}
}

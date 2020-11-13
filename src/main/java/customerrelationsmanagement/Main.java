package customerrelationsmanagement;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
	public static final String INVENTORY_PATH = "inventory_team4.csv";
	//public static final String INVENTORY_PATH = null;
	
	public static final String analyticsDir = "analytics";
	//public static final String analyticsDir = null;
	
	public static final String ORDERS_PATH = "customer_orders_A_team4.csv";
	//public static final String ORDERS_PATH = "";
	
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
	throws IOException, SQLException {
		startServices(credentials);
	}
	
	public static void main(String[] args) {
		
		System.out.println("This is the first line of main.");
		credentials = new Credentials();
	}
	
	public void startServices(Credentials credentials)
	throws IOException, SQLException {
		
		this.crud = credentials.getCrud();
		if(START_MAIL) {
			mailer = new Emailer(credentials);
		}
		if(INVENTORY_PATH != null && !INVENTORY_PATH.equals("")) {
			rest = new Restoration(crud, INVENTORY_PATH, "customer_orders_A_team4.csv",true, analyticsDir);
		}
		if(START_GUI) {
			new GUI(crud);
		}
		if(ORDERS_PATH != null && !ORDERS_PATH.equals("")) {
			OrderProcessor.runFileOrders(crud, ORDERS_PATH);
		}
	}
}

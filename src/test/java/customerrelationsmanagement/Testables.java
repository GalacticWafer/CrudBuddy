package customerrelationsmanagement;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class Testables {
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
	
	private static CredentialsTest credentials;
	private static Crud crud;
	private static EmailerTest mailer;
	private static OrderProcessorTest orderProcessor;
	private static Restoration rest;
	
	public Testables(CredentialsTest credentials, Crud queryMaker)
			throws IOException, SQLException, ParseException {
	}
	
	public static void main(String[] args) {
		
		System.out.println("This is the first line of main.");
		credentials = new CredentialsTest(CredentialsTest.Testers.CrudTester);
	}
}

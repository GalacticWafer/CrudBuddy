package customerrelationsmanagement;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

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
			throws IOException, SQLException, ParseException {
		startServices(credentials);
	}
	
	public static void main(String[] args) {
		
		//getHistogram();
		System.out.println("This is the first line of main.");
		credentials = new Credentials();
	}
	
	private static void getHistogram() {
		HistogramDataset dataset = new HistogramDataset();
		double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
		dataset.addSeries("H1", values, 10, 0.0, 10.0);
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		
		panel.add(new ChartPanel(ChartFactory.createHistogram(
		 "histogram title","x label", "y label", dataset, 
		 PlotOrientation.VERTICAL,true, true, false)));
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public void startServices(Credentials credentials)
			throws IOException, SQLException, ParseException {
		
		this.crud = credentials.getCrud();
		if(START_MAIL) {
			mailer = new Emailer(credentials);
		}
		if(INVENTORY_PATH != null && !INVENTORY_PATH.equals("")) {
			rest = new Restoration(crud, INVENTORY_PATH, "customer_orders_A_team4.csv",true, analyticsDir);
		}
		if(START_GUI) {
			new GUI(crud, new Analytics(crud));
		}
		if(ORDERS_PATH != null && !ORDERS_PATH.equals("")) {
			OrderProcessor.runFileOrders(crud, ORDERS_PATH);
		}
	}
}

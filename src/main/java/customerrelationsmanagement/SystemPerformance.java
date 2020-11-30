package customerrelationsmanagement;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.sql.SQLException;

public class SystemPerformance {
	Crud crud;
	String[] fileNames;
	int[] fileLengths;
	
	public SystemPerformance(Crud crud) {
		this.crud = crud;
		fileNames = new String[] {
		 "customer_orders_A_team4.csv",
		 "customer_orders_A_team4_x3.csv",
		 "customer_orders_A_team4_x5.csv",
		 "customer_orders_A_team4_x7.csv",
		 "customer_orders_A_team4_1000x.csv"
		};
		fileLengths = new int[]{1455, 7275, 10185, 1048576};
	}
	
	public void runTest(boolean fullTest) throws SQLException, FileNotFoundException {
		int count = fullTest ? fileNames.length : fileNames.length - 2;
		long[] timeValues = new long[fileNames.length];
		for(int i = 0; i < count; i++) {
			long start = System.nanoTime();
			OrderProcessor.runFileOrders(crud, fileNames[i]);
			long end = System.nanoTime();
			long timeInterval = end - start;
			System.out
			 
			 .printf("%.2f seconds to process and insert %d new records\n", (
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
		JFreeChart chart =
		 maker.getChart("string", ChartType.SPEED_TEST, timeValues,fileLengths,
		  count);
		ChartPanel cp = new ChartPanel(chart);
		frame.getContentPane().add(cp);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
}

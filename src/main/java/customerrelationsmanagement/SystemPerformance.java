package customerrelationsmanagement;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SystemPerformance {
	Crud crud;
	String[] fileNames;
	int[] fileLengths;
	
	
	public SystemPerformance(Crud crud) {
		this.crud = crud;
		fileNames =  new String[] {
		 "customer_orders_final_team4.csv" ,
		 "customer_orders_final_team4x5.csv",
		 "customer_orders_final_team4x15.csv",
		 "customer_orders_final_team4x50.csv",
		 "customer_orders_final_team4x200.csv"
		};
		fileLengths = new int[]{6295,31475,94425,314750,1259000};
	}
	
	/**
	 * @param fullTest
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * runs either a full test with all file lengths or
	 * a smaller test with less files in OrderProcessor
	 */
	public void runTest(boolean fullTest) throws SQLException, FileNotFoundException {
		int count = fullTest ? fileNames.length : fileNames.length - 1;
		long[] timeValues = new long[fileNames.length];
		int i = 0;
		for(; i < count; i++) {
			long start = System.nanoTime();
			OrderProcessor.runFileOrders(crud, fileNames[i]);
			long end = System.nanoTime();
			long timeInterval = end - start;
			long convertedInterval = TimeUnit.SECONDS.convert(timeInterval, TimeUnit.NANOSECONDS);
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
			timeValues[i] = convertedInterval;
		}
		JFrame frame = new JFrame("Speed Test");
		ChartMaker maker = new ChartMaker(crud);
		
		BigDecimal[] ratios = new BigDecimal[i];
		for(i = 0; i < ratios.length; i ++) {
			ratios[i] = BigDecimal.valueOf(fileLengths[i])
				.multiply(BigDecimal.valueOf(1.0 / timeValues[i]));
		}
		JFreeChart chart =
		 maker.getBarChart(ratios,fileLengths);
		ChartPanel cp = new ChartPanel(chart);
		frame.getContentPane().add(cp);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		System.out.println("done");
	}
}

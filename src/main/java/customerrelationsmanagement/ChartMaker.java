package customerrelationsmanagement;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.*;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;

public class ChartMaker {
	public static final double BAR_THICCKNESS = 4.0;
	public static final NumberFormat CURRENCY =
	 NumberFormat.getCurrencyInstance();
	Crud crud;
	
	public ChartMaker(Crud crud) {
		
		this.crud = crud;
	}
	
	private boolean checkValidity(String time) {
		
		/* TODO: Daniel, check the validity
		    check if time is valid
		    check if row exists for the "time"
		 */
		return true;
	}
	
	/**
	 * generates a new bar chart with default settings, can
	 * be used in multiple settings with different data and
	 * type specified
	 * @param time time value to be passed in to be analyzed
	 * @param type type of chart to be generated
	 * @return a bar chart instance
	 * @throws SQLException
	 */
	
	private JFreeChart getBarChart
	 (String time, ChartType type)
	throws SQLException {
		
		Object[][] data = crud.getRecords(
		 "SELECT " + type.getColumn() + " FROM daily_analysis " +
		 "WHERE fiscal_date = '" + time + "'");
		String str = data[0][0] + "";
		if(str.contains("null")) {
			str = str.substring(0, str.indexOf("null") - 1);
		}
		String[] strArray = str.stripTrailing().split("\t");
		String[] labels = new String[strArray.length];
		double[] values = new double[strArray.length];
		
		for(int i = 0; i < strArray.length; i++) {
			String[] datum = strArray[i].split(" ");
			labels[i] = datum[0];
			
			values[i] = Double.parseDouble(datum[1]);
		}
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for(int i = 0; i < values.length; i++) {
			dataset.addValue(values[i], labels[i], labels[i]);
		}
		
		JFreeChart barChart = ChartFactory
		 .createBarChart(
		  type.toString() + " on " + time, type.getColumn(), type
		   .getYAxis(), dataset,
		  PlotOrientation.VERTICAL, true, true, false);
		
		((BarRenderer)barChart.getCategoryPlot().getRenderer())
		 .setItemMargin(-values.length / BAR_THICCKNESS);
		
		((NumberAxis)barChart.getCategoryPlot().getRangeAxis())
		 .setNumberFormatOverride(CURRENCY);
		
		return barChart;
	}
	
	/**
	 * generates a new bar chart for system performance
	 * takes in an arrray of ratios and graphs them
	 * accordingly
	 * @param ratios the ratio of file size to a time interval
	 * @param labels necessary x and legend labels
	 * @return a bar chart instance with specific bounds
	 */
	
	public JFreeChart getBarChart(BigDecimal[] ratios, int[] labels) {
		DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
		for(int i = 0; i < ratios.length; i++) {
			dataSet.addValue(ratios[i], labels[i], labels[i]);
		}
		int x = ratios.length;
		JFreeChart barChart = ChartFactory.createBarChart(
		 "System Performance", "Number of Orders", "Orders Per Second", dataSet,
		 PlotOrientation.VERTICAL, true, true, false);
		
		((BarRenderer)barChart.getCategoryPlot().getRenderer())
		 .setItemMargin(-ratios.length / BAR_THICCKNESS);
		
		NumberAxis range = (NumberAxis) barChart.getCategoryPlot().getRangeAxis();
		range.setRange(0,1000);
		range.setTickUnit(new NumberTickUnit(50));
		((NumberAxis)barChart.getCategoryPlot().getRangeAxis()).
		 setNumberFormatOverride(NumberFormat.getInstance());
		return barChart;
	}
	
	/**
	 * generates a specific chart depeding on what analysis is chosen
	 * @param time a time to be passed in, most likely a query
	 * @param type chooses the chart type
	 * @return a chart instance depending on type chosen
	 * @throws SQLException
	 */
	public JFreeChart getChart(String time, ChartType type)
	throws SQLException {
		
		if(!checkValidity(time)) { return null; }
		switch(type) {
			case TOP_CUSTOMERS, TOP_PRODUCTS -> {
				return getBarChart(time, type);
			}
			case DAILY_ASSETS, DAILY_ORDER_COUNTS, DAILY_INCOME, DAILY_REVENUE -> {
				return getTimeSeriesChart(time, type);
			}
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * generates a line plot
	 * used mostly for time complexity analysis
	 * throws excpetion if bad data added
	 * @param time a time value, can be a query
	 * @param type the chart type to be made
	 * @param intervals a time interval
	 * @param numberOfLines the total file size
	 * @param count limits how much is added to dataset
	 * @return returns a line plot instance
	 */
	public JFreeChart getChart(String time, ChartType type, long[] intervals,
							   int[] numberOfLines, int count) {
		int maxNumberOfFiles = 0;
		final TimeSeries series = new TimeSeries(type.toString());
		for(int i = 0; i < count; i++) {
			maxNumberOfFiles = Math.max(maxNumberOfFiles, numberOfLines[i]);
			LocalDateTime t = (new Timestamp(intervals[i])).toLocalDateTime();
			int minute = t.getMinute();
			int hour = t.getHour();
			int day = t.getDayOfMonth();
			int month = t.getMonthValue();
			int year = t.getYear();
			try {
				series.add(
				 new Minute(minute, hour, day, month, year),
				 BigInteger.valueOf(numberOfLines[i]));
			}
			catch(SeriesException e) {
				System.err.println("Error adding to series");
			}
		}
		return
		 
		 ChartFactory.createTimeSeriesChart(
		  type.toString(), "Date", "Total Orders",
		  new TimeSeriesCollection(series),
		  false, false, false);
	}
	
	/**
	 * generates a time series chart based on what data'
	 * is selected, shows a comparison of records to
	 * finance over time
	 * @param time a time value to be analyzed
	 * @param type specifies chart type
	 * @return a time series chart instance
	 * @throws SQLException if query incorrect or time out happens
	 */
	private JFreeChart getTimeSeriesChart(String time, ChartType type)
	throws SQLException {
		
		Object[][] data = crud.getRecords(
		 "SELECT fiscal_date," + type.getColumn() +
		 " FROM daily_analysis WHERE fiscal_date <= '" + time + "'");
		final TimeSeries series = new TimeSeries(type.toString());
		
		MathContext context = new MathContext(13);
		for(Object[] datum: data) {
			LocalDateTime t = ((Timestamp)datum[0]).toLocalDateTime();
			int minute = t.getMinute();
			int hour = t.getHour();
			int day = t.getDayOfMonth();
			int month = t.getMonthValue();
			int year = t.getYear();
			
			switch(type) {
				case DAILY_ORDER_COUNTS -> series.add(
				 new Minute(minute, hour, day, month, year),
				 ((Integer)datum[1])
				);
				
				case DAILY_ASSETS, DAILY_INCOME, DAILY_REVENUE, TOP_CUSTOMERS,
				 TOP_PRODUCTS -> series
				 .add(
				  new Minute(minute, hour, day, month, year),
				  ((BigDecimal)datum[1]).round(context)
				 );
			}
		}
		JFreeChart timeSeriesChart =
		 ChartFactory.createTimeSeriesChart(
		  type.toString(), "Date", "Total",
		  new TimeSeriesCollection(series),
		  false, false, false);
		
		((NumberAxis)timeSeriesChart.getXYPlot().getRangeAxis())
		 .setNumberFormatOverride(CURRENCY);
		
		return timeSeriesChart;
	}
	
}

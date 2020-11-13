package customerrelationsmanagement;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;

public class ChartMaker {
	public static final NumberFormat CURRENCY =
	 NumberFormat.getCurrencyInstance();
	public static final double BAR_THICCKNESS = 4.0;
	Crud crud;
	String title;
	
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
	
	private JFreeChart getBarChart
	 (String time, String columnName, String yAxis, String chartTitle)
	throws SQLException {
		
		Object[][] data = crud.getRecords(
		 "SELECT " + columnName + " FROM daily_analysis " +
		 "WHERE fiscal_date = '" + time + "'");
		String[] strArray = (data[0][0] + "").split("\t");
		String[] labels = new String[strArray.length];
		double[] values = new double[strArray.length];
		
		for(int i = 0; i < strArray.length; i++) {
			String[] datum = strArray[i].split(" ");
			labels[i] = datum[0];
			
			values[i] = this.title.equals("top_customers") ?
			 Integer.parseInt(datum[1]) / 1.0 :
			 Double.parseDouble(datum[1]);
		}
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for(int i = 0; i < values.length; i++) {
			dataset.addValue(values[i], labels[i], labels[i]);
		}
		
		JFreeChart barChart = ChartFactory
		 .createBarChart(chartTitle, columnName, yAxis, dataset,
		  PlotOrientation.VERTICAL, true, true, false);
		
		((BarRenderer)barChart.getCategoryPlot().getRenderer())
		 .setItemMargin(-values.length / BAR_THICCKNESS);
		
		((NumberAxis)barChart.getCategoryPlot().getRangeAxis())
		 .setNumberFormatOverride(CURRENCY);
		
		return barChart;
	}
	
	public JFreeChart getChart(String time, ChartType type)
	throws SQLException {
		
		if(!checkValidity(time)) { return null; }
		switch(type) {
			case BAR_TOP_CUSTOMERS -> {
				return getBarChart(
				 time, "top_customers",
				 "Order Total on " + time, "Top Customers For " + time);
			}
			case BAR_TOP_PRODUCTS -> {
				return getBarChart(
				 time, "top_products",
				 "Product Count on " + time, "Top Products For " + time);
			}
			case YTD_ASSETS -> {
				return getTimeSeriesChart(time, "asset_total"
				);
			}
			case YTD_CUSTOMER -> {
				return getTimeSeriesChart(time, "top_customers");
			}
			case YTD_REVENUE -> {
				return getTimeSeriesChart(time, "top_products");
			}
		}
		throw new IllegalArgumentException();
	}
	
	private JFreeChart getTimeSeriesChart(String time, String columnName)
	throws SQLException {
		
		Object[][] data = crud.getRecords(
		 "SELECT fiscal_date," + columnName +
		 " FROM daily_analysis WHERE fiscal_date <= '" + time + "'`");
		final TimeSeries series = new TimeSeries("Assets");
		
		MathContext context = new MathContext(13);
		for(Object[] datum: data) {
			LocalDateTime t = ((Timestamp)datum[0]).toLocalDateTime();
			int minute = t.getMinute();
			int hour = t.getHour();
			int day = t.getDayOfMonth();
			int month = t.getMonthValue();
			int year = t.getYear();
			if(datum[1] instanceof Long) {
				series.add(
				 new Minute(minute, hour, day, month, year),
				 ((Long)datum[1])
				);
			} else {
				series.add(
				 new Minute(minute, hour, day, month, year),
				 ((BigDecimal)datum[1]).round(context)
				);
			}
		}
		
		JFreeChart timeSeriesChart = ChartFactory.createTimeSeriesChart(
		 "Assets", "Date", "Total",
		 new TimeSeriesCollection(series),
		 false, false, false);
		
		((NumberAxis)timeSeriesChart.getXYPlot().getRangeAxis())
		 .setNumberFormatOverride(CURRENCY);
		
		return timeSeriesChart;
	}
	
}

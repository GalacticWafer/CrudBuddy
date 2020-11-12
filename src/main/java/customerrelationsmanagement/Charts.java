package customerrelationsmanagement;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

public class Charts {
	Crud crud;
	String title;
	
	public Charts(Crud crud) {
		
		this.crud = crud;
	}
	
	private boolean checkValidity(String time) {
		
		/* TODO: Daniel, check the validity
		    check if time is valid
		    check if row exists for the "time"
		 */
		return false;
	}
	
	private JFreeChart getBarChart(String time, String columnName,
								   String yAxis,
								   String chartTitle)
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
		
		CategoryPlot categoryPlot = barChart.getCategoryPlot();
		BarRenderer br = (BarRenderer)categoryPlot.getRenderer();
		if(columnName.equals("top_customers")) {
			NumberFormat currency = NumberFormat.getCurrencyInstance();
			NumberAxis rangeAxis = (NumberAxis)categoryPlot.getRangeAxis();
			rangeAxis.setNumberFormatOverride(currency);
		}
		br.setItemMargin(-values.length / 4.0);
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
		
		List<Object[]> data = Arrays.asList(crud.getRecords(
		 "SELECT fiscal_date," + columnName +
		 " FROM daily_analysis WHERE fiscal_date <= '" + time + "'`"));
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
		
		return ChartFactory.createTimeSeriesChart(
		 "Assets", "Date", "Total",
		 new TimeSeriesCollection(series),
		 false, false, false);
	}
}

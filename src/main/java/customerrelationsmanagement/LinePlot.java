package customerrelationsmanagement;
import org.jfree.chart.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.function.ObjDoubleConsumer;

public class LinePlot extends ApplicationFrame {
    String dateAxis = "Month";
    private JFreeChart chart;
    private File directory;
    String chartTitle;
    String title;
    List data;
    ChartPanel chartPanel;
    Crud crud;
    public static final int ASSET = 0;
    public static final int CUSTOMER = 1;
    public static final int PRODUCT = 2;

    public LinePlot(final String title,Crud crud) {

        super(title);
        this.data = data;
        this.title = title;
        this.chartTitle = chartTitle;
        data = new ArrayList<>();
        this.crud = crud;
       // ChartUtilities save = null;
        //ChartUtilities.save
    }

    public JFreeChart updateChart() {
        String valueAxisLabel;
        switch(chartTitle) {
            case "Assets":
                valueAxisLabel = "Assets";
                break;
            case "Customer Orders":
                valueAxisLabel = "Order #";
                break;
            default:
                valueAxisLabel = "Total";
        }
        final XYDataset dataset = getDatasets(data, chartTitle);
        return createChart(dataset, chartTitle, dateAxis, valueAxisLabel);
    }
    public void displayChart() {
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }
    
    private JFreeChart createChart(final XYDataset dataset, String title,
                                   String timeAxisLabel,
                                   String valueAxisLabel) {

        return ChartFactory.createTimeSeriesChart(
                title, timeAxisLabel, valueAxisLabel, dataset, false, false, false);
    }

    private XYDataset getDatasets(List<Object[]> data, String name) {

        final TimeSeries series = new TimeSeries(name);

        MathContext context = new MathContext(13);
        for(Object[] datum: data) {
            LocalDateTime time = ((Timestamp)datum[0]).toLocalDateTime();
            int minute = time.getMinute();
            int hour = time.getHour();
            int day = time.getDayOfMonth();
            int month = time.getMonthValue();
            int year = time.getYear();
            if(datum[1] instanceof Long){
                series.add(
                        new Minute(minute, hour, day, month, year),
                        ((Long)datum[1])
                );
            }
            else{
                series.add(
                        new Minute(minute, hour, day, month, year),
                        ((BigDecimal)datum[1]).round(context)
                );
            }

        }
        
        return new TimeSeriesCollection(series);
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    
        public void addData(Object[] data) {
            this.data.add(data);
        }
    
        
        public JFreeChart generateAssetPlot(String time, int type) throws SQLException {
            //check if time is valid
            //check if row exists for the "time"
            //^^ pass both statements, then make the chart
        
        switch(type){
            case ASSET:
                return getjFreeChart(time, "asset_total");
            case CUSTOMER:
                return getjFreeChart(time, "top_customers");
            case PRODUCT:
                return getjFreeChart(time, "top_products");
                
        }
            throw new InputMismatchException();
        }
    
    private JFreeChart getjFreeChart(String time, String columnName) throws SQLException {
        
        String sql = "SELECT fiscal_date," + columnName + " FROM daily_analysis WHERE fiscal_date <= '" +
                     time + "'";
        
        Object[][] objects = crud.getRecords(sql);
        List<Object[]> points = new ArrayList<>();
        
        points.addAll(Arrays.asList(objects));
        
        XYDataset dataset = getDatasets(points, "Assets");
        return ChartFactory.createTimeSeriesChart(
         "Assets", "Date", "Total",dataset, false, false, false);
    }
}

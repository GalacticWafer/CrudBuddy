import org.jfree.chart.*;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LinePlot extends ApplicationFrame {
    String dateAxis = "Month";
    private JFreeChart chart;
    private File directory;
    String chartTitle;
    String title;
    List data;
    ChartPanel chartPanel;

    public LinePlot(final String title) {

        super(title);
        this.data = data;
        this.title = title;
        this.chartTitle = chartTitle;
        data = new ArrayList<>();
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
        final XYDataset dataset = createDataset(data, chartTitle);
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

    private XYDataset createDataset(List<Object[]> data, String name) {

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

}

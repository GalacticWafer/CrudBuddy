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
import java.util.List;

public class LinePlot extends ApplicationFrame {
    String dateAxis = "Month";
    private JFreeChart chart;
    private File directory;

    public LinePlot(final String title, String chartTitle, List<Object[]> data) {

        super(title);

        directory = new File("charts");
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

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
        chart = createChart(dataset, chartTitle, dateAxis, valueAxisLabel);
        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
       // ChartUtilities save = null;

        //ChartUtilities.save
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
            series.add(
                    new Minute(minute, hour, day, month, year),
                    ((BigDecimal)datum[1]).round(context)
            );
        }
        return new TimeSeriesCollection(series);
    }

    public void save(String filename) throws IOException {
        final File output = new File(directory + "/" +filename);
        final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        try{
            ChartUtilities.saveChartAsPNG(output, chart, 560, 370, info );
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("-- saved");
    }
}
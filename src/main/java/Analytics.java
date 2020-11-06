import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Analytics {
    private Crud crud;
    private final File directory;
    LinePlot assetsOT;

    public Analytics(Crud crud) {
        this.crud = crud;
        directory = createDirs("analytics");
        assetsOT = new LinePlot("", "");
    }

    private File createDirs(String pathName) {
        File directory = new File(pathName);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
        return directory;
    }
    public void generateTimePlot() {
        Object[][] yearToDateData = null;
        try {
            yearToDateData = crud.getAssetTotal("");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        List<Date> dates = new ArrayList<>();
        Date Jan = StringToDate("2020-02-31");
        Date previous = null;
        for (int i = 0; i < yearToDateData.length; i++) {
            assetsOT.addData(yearToDateData[i]);
            Date current = (Date)yearToDateData[i][0];
            dates.add(current);
            if (current.compareTo(Jan) > 0 && current.compareTo(previous) > 0) {
                try {
                    assetsOT.setTitle("Assets");
                    String dateString = DateToString(current);
                    String chartTitle = "Assets" + dateString;
                    assetsOT.setChartTitle(chartTitle);
                    String subDirectory =  directory + "/" + dateString;
                    createDirs(subDirectory);
                    save(subDirectory  + "/Assets.png", assetsOT.updateChart());
                    //Todo: save a text file with a PrintWriter that has all
                    // of the info for the daily assets. Use the same
                    // subDirectory with "/staistics.txt" as the file name
                } catch (IOException throwables) {
                    throwables.printStackTrace();
                }
            }
            previous = current;
        }

    }
    public File save(String filename, JFreeChart chart) throws IOException {
        final File output = new File( filename);
        final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        try{
            ChartUtilities.saveChartAsPNG(output, chart, 560, 370, info );
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("-- saved");
        return output;
    }

    public static Date StringToDate(String s) {
        Date result = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            result = dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String DateToString(Date d) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(d);
        return date;
    }

}



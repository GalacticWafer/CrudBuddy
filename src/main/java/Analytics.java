import org.jfree.ui.RefineryUtilities;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Analytics {
    private Crud crud;

    public Analytics(Crud crud){
        this.crud = crud;
    }

    public void generateTimePlot(){
        Object[][] data = new Object[0][];
        try {
            data = crud.getAssetTotal("");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println(data[0][0]);
        java.util.List<Object[]> yearToDateData = new ArrayList<>();
        List<Date> dates = new ArrayList<>();
        Date Jan = StringToDate("2020-02-31");
        for (int i = 0; i < data.length; i++) {
            yearToDateData.add(data[i]);
            dates.add((Date)data[i][0]);
            if (dates.get(i).compareTo(Jan) > 0 && dates.get(i).compareTo(dates.get(i-1)) > 0 ){
                try {
                    makePlot("MYCROWSAWFT", "Assets", yearToDateData, dates.get(i));
                } catch (SQLException | IOException throwables) {
                    throwables.printStackTrace();
                }
                //save();
            }
        }
    }

    private static void makePlot(String title, String chartTitle, List<Object[]> list, Date date)
            throws SQLException, IOException {

        LinePlot plot = new LinePlot(title, chartTitle, list);
        plot.pack();
        RefineryUtilities.positionFrameRandomly(plot);
        plot.setVisible(false);
        plot.save(chartTitle + "_" + DateToString(date) + ".png");
    }
    public static Date StringToDate(String s){
        Date result = null;
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            result  = dateFormat.parse(s);
        }
        catch(ParseException e){
            e.printStackTrace();
        }
        return result ;
    }

    public static String DateToString(Date d){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(d);
        return date;
    }

}



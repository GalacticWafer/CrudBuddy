import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;

import javax.crypto.spec.OAEPParameterSpec;
import javax.swing.text.Position;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Analytics {
    private class DailyAnalysis {
        private final Date date;
        private final BigDecimal dailyRevenueArray;
        private final int numberOfOrders;
        private ArrayList<BigDecimal> assetTotal;

        public DailyAnalysis(Date date, BigDecimal dailyRevenueSum) {
            this.date = date;
            this.dailyRevenueArray = dailyRevenueSum;
            this.numberOfOrders = 0;
            this.assetTotal = new ArrayList<>();
        }

        public void add(BigDecimal dailyRevenueSum) {
            dailyRevenueArray.add(dailyRevenueSum);
        }

        public Date getDate() {
            return date;
        }

        public ArrayList<BigDecimal> getAssetTotal() {
            return assetTotal;
        }


        public int getNumberOfOrders() {
            return numberOfOrders;
        }

        public Iterator<BigDecimal> iterator() {
            return assetTotal.iterator();
        }
    }

    private Crud crud;
    private final File directory;
    LinePlot assetPlot;
    LinePlot revenuePlot;
    LinePlot orderCountPlot;

    public Analytics(Crud crud) {
        this.crud = crud;
        directory = createDirs("analytics");

        assetPlot = new LinePlot( "Assets");
        revenuePlot = new LinePlot("Daily Revenue");
        orderCountPlot = new LinePlot("Daily Number of Orders");
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

    public void generateTimePlot() throws SQLException {
        DailyAnalysis da = null;
        Object[][] dailyStats = dailyStats();

        Date Jan = StringToDate("2020-01-31");
        assetPlot.setTitle("Assets");

        for (int i = 0; i < dailyStats.length; i++) {
            Timestamp stamp = (Timestamp)dailyStats[i][0];
            Date current = new Date(stamp.getTime());
            BigDecimal revenue = (BigDecimal)dailyStats[i][1];
            Long numProductsSold = (long)dailyStats[i][2];
            BigDecimal dailyAssetTotal = (BigDecimal)dailyStats[i][3];

            assetPlot.addData(new Object[] {stamp, dailyAssetTotal});
            revenuePlot.addData(new Object[] {stamp, revenue});
            orderCountPlot.addData(new Object[] {stamp, numProductsSold});
            if (!(current.compareTo(Jan) > 0)) {
                continue; // if it's not february yet...
            }
            try {
                String dateString = DateToString(current);
                String assetTitle = "Assets" + dateString;
                assetPlot.setChartTitle(assetTitle);
                String revenueTitle = "Revenue" + dateString;
                revenuePlot.setChartTitle(revenueTitle);
                String orderCountTitle = "Products Sold" + dateString;
                orderCountPlot.setChartTitle(orderCountTitle);
                String subDirectory = directory + "/" + dateString;
                createDirs(subDirectory);
                save(subDirectory + "/Assets.png", assetPlot.updateChart());
                save(subDirectory + "/Revenue.png", revenuePlot.updateChart());
                save(subDirectory + "/ProductsSold.png", orderCountPlot.updateChart());
            } catch (IOException throwables) {
                throwables.printStackTrace();
            }

        }
    }


    public File save(String filename, JFreeChart chart) throws IOException {
        final File output = new File(filename);
        final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        try {
            ChartUtilities.saveChartAsPNG(output, chart, 560, 370, info);
        } catch (Exception e) {
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

    public Object[][] dailyStats() throws SQLException {
        String query = "SELECT date_accepted, SUM(sales.product_quantity * \n" +
                "inventory.sale_price) as result, COUNT(sales.product_id) \n" +
                "FROM sales join inventory on sales.product_id = inventory.product_id \n" +
                "GROUP BY date_accepted ORDER BY date_accepted ASC";
        Object[] assetTotals = getAssetTotal("");
        ResultSet rs = crud.query(query);
        query = "Select SUM(sales.product_quantity * " +
                        "(sale_price - wholesale_cost)) " +
                        "        as assets from sales " +
                        "INNER JOIN inventory on sales.product_id = inventory.product_id " +
                        "GROUP BY date_accepted " +
                        "ORDER BY date_accepted ASC";
        ResultSet rs2 = crud.query(query);

        Object[][] returnArray = new Object[crud.rowCountResults(rs)][4];
        for (int i = 0; rs.next() && rs2.next(); i++) {
            returnArray[i][0] =rs.getTimestamp(1);
            returnArray[i][1] =rs.getBigDecimal(2);
            returnArray[i][2] =rs.getLong(3);
            returnArray[i][3] =rs2.getBigDecimal(1);
        }
        return returnArray;
    }



    /**
     * TODO: David & Uriel
     *
     * @param onDate TODO: David & Uriel
     * @return TODO: David & Uriel
     * @throws SQLException if there is an issue with the sql command or connection.
     */
    public Object[] getAssetTotal(String onDate)
            throws SQLException {

        String query = (onDate == null) ?
                "SELECT SUM(quantity * (sale_price - wholesale_cost))" +
                        "as assets from inventory" :
                "Select SUM(sales.product_quantity * " +
                        "(sale_price - wholesale_cost)) " +
                        "        as assets from sales " +
                        "INNER JOIN inventory on sales.product_id = inventory.product_id " +
                        "GROUP BY date_accepted " +
                        "ORDER BY date_accepted ASC";

        ResultSet rs = crud.query(query);
        rs.next();

        return crud.getRecords(rs);
    } // End getAssetTotal

}



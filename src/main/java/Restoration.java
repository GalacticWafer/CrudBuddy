import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Restoration {
	public static void rebuild(Crud crud, String filePath)
	throws FileNotFoundException, SQLException {
		for(String tableName: crud.getTableNames()) {
			crud.update(" drop table if exists " + tableName);
		}
		rebuildTables(crud);
		Scanner scanner = new Scanner(new File(filePath));
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO inventory ("
				   + Crud.removeUTF8BOM(scanner.nextLine()) + ")VALUES");
		int i =0 ;
		
		while(scanner.hasNextLine()) {
			String[] l = scanner.nextLine().split(",");
			sql.append("('").append(l[0]).append("',")
			   .append(Integer.parseInt(l[1])).append(",")
			   .append(Double.parseDouble(l[2])).append(",")
			   .append(Double.parseDouble(l[3])).append(",'").append(l[4])
			   .append("')").append(scanner.hasNextLine() ? "," : "");
			i++;
		}
		System.out.println(i);
		crud.update(sql.toString());
	}
	
	public static void rebuildTables(Crud crud) throws SQLException {
		crud.update("CREATE TABLE IF NOT EXISTS inventory(" +
					"idx INT(16)    	NOT NULL AUTO_INCREMENT," +
					"product_id     	VARCHAR(12)," +
					"quantity       	INT(16)," +
					"wholesale_cost 	DECIMAL(13, 2)," +
					"sale_price     	DECIMAL(13, 2)," +
					"supplier_id    	VARCHAR(32)," +
					"PRIMARY KEY 		(idx))");
		crud.update("CREATE TABLE IF NOT EXISTS sales(" +
					"idx int(16) 		NOT NULL AUTO_INCREMENT," +
					"order_id 			VARCHAR(10)," +
					"cust_email 		VARCHAR(60)," +
					"cust_location 		VARCHAR(100)," +
					"product_id     	VARCHAR(12)," +
					"product_quantity   INT(16)," +
					"date_ordered 		DATE," +
					"date_accepted 		DATE," +
					"PRIMARY KEY 		(idx))");
		crud.update("CREATE TABLE IF NOT EXISTS back_orders(" +
					"idx int(16) 		NOT NULL AUTO_INCREMENT," +
					"order_id 			VARCHAR(10)," +
					"cust_email 		VARCHAR(60)," +
					"cust_location 		VARCHAR(100)," +
					"product_id			VARCHAR(12)," +
					"product_quantity   INT(16)," +
					"date_ordered 		DATE," +
					"PRIMARY KEY 		(idx))");
	}
}

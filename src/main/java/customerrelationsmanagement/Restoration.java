package customerrelationsmanagement;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;

import java.util.*;

public class Restoration {
	public Restoration(Crud crud, String inventoryPath, String ordersPath,
					   boolean doTableRebuild, String directory)
	throws IOException, SQLException {
		
		rebuild(crud, inventoryPath, ordersPath, doTableRebuild, directory);
	}
	
	private void deleteDirectory(String pathname) throws IOException {
		
		File directory = new File(pathname);
		if(directory.exists()) {
			FileUtils.cleanDirectory(directory);
		}
	}
	
	private void rebuild(Crud crud, String inventoryPath, String ordersPath,
						 boolean doTableRebuild,
						 String directory)
	throws IOException, SQLException {
		
		String[] list = crud.getTableNames();
		for(String tableName: list) {
			crud.update(" DROP TABLE IF EXISTS " + tableName);
		}
		if(doTableRebuild) {
			rebuildTables(crud);
		}
		if(directory != null) {
			deleteDirectory(directory);
		}
		Scanner scanner = new Scanner(new File(inventoryPath));
		StringBuilder sql = new StringBuilder();
		String str = "INSERT INTO " + crud.getDatabaseName() + ".inventory ("
					 + Crud.removeUTF8BOM(scanner.nextLine()) + ")VALUES";
		sql.append(str);
		while(scanner.hasNextLine()) {
			String[] l = scanner.nextLine().split(",");
			sql.append("('").append(l[0]).append("',")
			   .append(Integer.parseInt(l[1])).append(",")
			   .append(Double.parseDouble(l[2])).append(",")
			   .append(Double.parseDouble(l[3])).append(",'").append(l[4])
			   .append("')").append(scanner.hasNextLine() ? "," : "");
		}
		crud.update(sql.toString());
		
		if(!ordersPath.equals("")) {
			scanner = new Scanner(new File(ordersPath));
			scanner.nextLine();
			sql = new StringBuilder();
			str = "INSERT INTO " + crud.getDatabaseName() + ".unstatused_sales (" +
				  "date_ordered,cust_email,cust_location,product_id,product_quantity)VALUES";
			sql.append(str);
			while(scanner.hasNextLine()) {
				String[] l = scanner.nextLine().split(",");
				sql.append("('").append(l[0]).append("','")
				   .append(l[1]).append("','")
				   .append(l[2]).append("','")
				   .append(l[3]).append("',")
				   .append(Integer.parseInt(l[4]))
				   .append(")").append(scanner.hasNextLine() ? "," : "");
			}
			crud.update(sql.toString());
		}
	}
	
	private void rebuildTables(Crud crud) throws SQLException {
		
		crud.update("CREATE TABLE IF NOT EXISTS inventory(" +
					"idx INT(16)    	NOT NULL AUTO_INCREMENT," +
					"product_id     	VARCHAR(12)," +
					"quantity       	INT(16)," +
					"wholesale_cost 	DECIMAL(13, 2)," +
					"sale_price     	DECIMAL(13, 2)," +
					"supplier_id    	VARCHAR(32)," +
					"PRIMARY KEY 		(idx))");
		crud.update("CREATE TABLE IF NOT EXISTS statused_sales(" +
					"idx int(16) 		NOT NULL AUTO_INCREMENT," +
					"order_id 			VARCHAR(10)," +
					"cust_email 		VARCHAR(60)," +
					"cust_location 		VARCHAR(100)," +
					"product_id     	VARCHAR(12)," +
					"product_quantity   INT(16)," +
					"date_ordered 		DATETIME," +
					"date_accepted 		DATETIME," +
					"order_status 		int(1)," +
					"PRIMARY KEY 		(idx))");
		crud.update("CREATE TABLE IF NOT EXISTS unstatused_sales(" +
					"idx              	INT(16) NOT NULL AUTO_INCREMENT," +
					"date_ordered     	DATETIME," +
					"cust_email       	VARCHAR(100)," +
					"cust_location    	VARCHAR(200)," +
					"product_id       	Varchar(12)," +
					"product_quantity 	int(8)," +
					"PRIMARY KEY (idx))");
		crud.update("create table if not exists daily_analysis(" +
					"fiscal_date  		DATETIME," +
					"asset_total  		DECIMAL(13, 2)," +
					"top_customers		VARCHAR(1000)," +
					"top_products 		VARCHAR(1000)," +
					"order_count  		INT(6)," +
					"product_count		INT(6))");
	}
}

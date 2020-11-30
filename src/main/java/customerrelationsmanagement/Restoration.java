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
		
		for(Tables table: Tables.values()) {
			crud.update(table.creationString());
		}
	}
}

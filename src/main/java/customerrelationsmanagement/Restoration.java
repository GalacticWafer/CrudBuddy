package customerrelationsmanagement;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.io.FileUtils;
import java.util.*;

public class Restoration {
	public Restoration(Crud crud, String filePath, boolean doTableRebuild, String directory)
			throws IOException, SQLException {
		rebuild(crud, filePath, doTableRebuild, directory);
	}
	 private void rebuild(Crud crud, String filePath, boolean doTableRebuild,
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
		Scanner scanner = new Scanner(new File(filePath));
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
		crud.update("CREATE TABLE IF NOT EXISTS sales(" +
					"idx int(16) 		NOT NULL AUTO_INCREMENT," +
					"order_id 			VARCHAR(10)," +
					"cust_email 		VARCHAR(60)," +
					"cust_location 		VARCHAR(100)," +
					"product_id     	VARCHAR(12)," +
					"product_quantity   INT(16)," +
					"date_ordered 		DATETIME," +
					"date_accepted 		DATETIME," +
					"Status		 		int(1)," +
					"PRIMARY KEY 		(idx))");
		crud.update("CREATE TABLE IF NOT EXISTS stats(" +
					"idx int(16) 		NOT NULL AUTO_INCREMENT," +
					"fiscal_date 		DATETIME," +
					"top_customers  VARCHAR(1000)," +
					"top_products  	VARCHAR(1000)," +
					"asset_total   	DECIMAL(13,2)," +
					"PRIMARY KEY 		(idx))");
	}

	private void deleteDirectory(String pathname) throws IOException {
		File directory = new File(pathname);
		if(directory.exists()) {
			FileUtils.cleanDirectory(directory);
		}
	}
}

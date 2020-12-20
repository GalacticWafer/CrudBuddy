package customerrelationsmanagement;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.io.FileUtils;
import java.util.*;

public class Restoration {
	public Restoration(Crud crud, String inventoryPath,
					   boolean doTableRebuild)
	throws IOException, SQLException {
		
		rebuild(crud, inventoryPath, doTableRebuild);
	}
	
	private void rebuild(Crud crud, String inventoryPath,
						 boolean doTableRebuild)
	throws IOException, SQLException {
		
		String[] list = crud.getTableNames();
		for(String tableName: list) {
			crud.update(" DROP TABLE IF EXISTS " + tableName);
		}
		if(doTableRebuild) {
			rebuildTables(crud);
		}

		Scanner scanner = new Scanner(new File(inventoryPath));
		StringBuilder sql = new StringBuilder();
		String str = "INSERT INTO " + crud.getDatabaseName() + ".inventory ("
					 + Crud.removeUTF8BOM(scanner.nextLine()) + ")VALUES";
		sql.append(str);
		while(scanner.hasNextLine()) {
			String[] l = scanner.nextLine().split(",");
			String line = "('" + l[0] + "'," + l[1] + "," + Double.parseDouble(l[2]) + "," + Double.parseDouble(l[3]) + ",'" + l[4] + "','" + l[5] + "','" + l[6].replace("'", "\\'") + "')" + (scanner.hasNextLine() ? "," : "");
			sql.append(line);
		}
		crud.update(sql.toString());

	}
	
	private void rebuildTables(Crud crud) throws SQLException {
		
		for(Tables table: Tables.values()) {
			crud.update(table.creationString());
		}
	}
}

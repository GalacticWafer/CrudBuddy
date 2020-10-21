import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Restoration {
	public Restoration(Crud crud) throws SQLException, FileNotFoundException {
		deleteAllTables(crud);
		Restoration.rebuildInventory(crud);
		new SalesProcessor(crud).processItems("customer_orders_A_team4.csv");
	}
	
	public static void rebuildInventory(Crud crud)
	throws FileNotFoundException, SQLException {
		Scanner scanner = new Scanner(new File("inventory_team4.csv"));
		String[] headers = scanner.nextLine().split(",");
		headers[0] = crud.removeUTF8BOM(headers[0]);
		//product_id,quantity,wholesale_cost,sale_price,supplier_id
		HashMap<Integer, String> types = new HashMap<>();
		types.put(0, "VARCHAR(12)");
		types.put(1, "INT(16)");
		types.put(2, "DECIMAL(13,2)");
		types.put(3, "DECIMAL(13,2)");
		types.put(4, "VARCHAR(8)");
		crud.insertTable("inventory", headers,types );
		
		ArrayList<Object[]> list = new ArrayList<>();
		while(scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(",");
			//IKQHDHWV0FN3,1445,134.22,183.88,DYCUYQFX
			String productId = line[0];
			int quantity = Integer.parseInt(line[1]);
			double wholesaleCost = Double.parseDouble(line[2]);
			double salePrice = Double.parseDouble(line[3]);
			String supplierId = line[4];
			list
			 .add(new Object[] {productId, quantity, wholesaleCost, salePrice,
								supplierId});
		}
		Object[][] inventory = new Object[list.size()][headers.length];
		Iterator<Object[]> it = list.iterator();
		int i = 0;
		while(it.hasNext()) {
			inventory[i] = it.next();
			i++;
		}
		crud.setWorkingTable("inventory");
		crud.insertRecords(headers, inventory);
	}
	
	public static void deleteAllTables(Crud crud) throws SQLException {
		for(String tableName: crud.getTableNames()) {
			if(tableName.equals("inventory")) {
				crud.deleteTable("inventory");
			} else {
				crud.setWorkingTable(tableName);
				crud.deleteAllRecords();
			}
		}
	}
}

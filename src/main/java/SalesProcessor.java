import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

class SalesProcessor {
	Queue<Object[]> backOrders;
	private final Crud crud;
	private HashMap<String, String> customers;
	Queue<Integer> idxList;
	HashMap<Integer, String> indexMap;
	LocalDate newItemDate;
	private HashMap<String, Integer> quantityMap;
	ArrayList<Object[]> sales;
	
	public SalesProcessor(Crud crud) throws SQLException {
		this.crud = crud;
		ResultSet rs =
		 crud.query("SELECT quantity,idx,product_id FROM inventory");
		crud.setWorkingTable("inventory");
		int size = crud.size();
		sales = new ArrayList<>(size); // list of Object[]'s for
		idxList = new ArrayDeque<>(size); // ordered list of idx's
		indexMap = new HashMap<>(size); // map from idx -> product_id
		quantityMap = new HashMap<>(size); // map from product_id -> quantity
		backOrders = new ArrayDeque<>(size); // anything we couldn't fulfill

		while(rs.next()) {
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			quantityMap.put(productId, quantity);
			indexMap.put(idx, productId);
			idxList.add(idx);
		} // End while
	} // End Constructor

	public ArrayList<Boolean> canProcessOrder(File file)
	throws FileNotFoundException {
		// Create scanner taking in a file of orders for processing
		Scanner scanner = new Scanner(file);

		// skip the header
		scanner.nextLine();
		ArrayList<Boolean> canProcessItems = new ArrayList<>();
		while(scanner.hasNextLine()) {
			// create place holder for tuple of product id and quantity
			String[] line = scanner.nextLine().split(",");
			String productId = line[3];
			int requestedQuantity = Integer.parseInt(line[4]);
			// check if product id exists by comparing to inventory table
			int currentQuantity = quantityMap.get(productId);
			if(requestedQuantity > currentQuantity) {
				canProcessItems.add(false);
			} else {
				canProcessItems.add(true);
			}
		}
		return canProcessItems;
	}

	public void insertBackOrders(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			TransactionItem item = new TransactionItem();
			//date,cust_email,cust_location,product_id,product_quantity
			String[] line = scanner.nextLine().split(",");
			item.setFields(
			 LocalDate.parse(line[0]),
			 line[1],
			 line[2],
			 line[3],
			 Integer.parseInt(line[4])
			);
			backOrders.add(item.toBackOrderArray());
		}
		crud.setWorkingTable("back_orders");
		crud.insertRecords(TransactionItem.BACK_ORDER_COLUMNS, backOrders
		 .iterator());
	}

	public void processOrder(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			TransactionItem item = new TransactionItem();
			//date,cust_email,cust_location,product_id,product_quantity
			String[] line = scanner.nextLine().split(",");
			item.setFields(
			 LocalDate.parse(line[0]),
			 line[1],
			 line[2],
			 line[3],
			 Integer.parseInt(line[4])
			);
			String productId = item.getProductId();
			Integer inventoryQuantity = quantityMap.get(productId);
			int requestedQuantity = item.getQuantity();

			int newQuantity = inventoryQuantity - requestedQuantity;
			quantityMap.put(productId, newQuantity);
			sales.add(item.toSalesArray());
		}
		// Todo: update sales table
		crud.setWorkingTable("sales");
		crud.insertRecords(TransactionItem.SALES_COLUMNS, sales.iterator());
		for(Integer idx: idxList) {
			String productId = indexMap.get(idx);
			int quanitiy = quantityMap.get(productId);
		}
		// Todo: update inventory table
		// Todo: send an email saying we can do it.
		// Todo: send an email saying we can't do it.
		// Todo: put back orders into back_orders table.
	}
}

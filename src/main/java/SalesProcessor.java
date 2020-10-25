import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

class SalesProcessor {
	private ArrayList<Object[]> acceptedSales;
	private ArrayList<Object[]> backOrders;
	private final Crud crud;
	private Queue<Integer> idxList;
	private HashMap<Integer, String> indexMap;
	private Order order;
	private HashMap<String, Integer> quantityMap;
	
	public SalesProcessor(Crud crud) throws SQLException {
		this.crud = crud;
		ResultSet rs =
		 crud.query("SELECT quantity,idx,product_id FROM inventory");
		crud.setWorkingTable("inventory");
		int size = crud.size();
		acceptedSales = new ArrayList<>(size); // list of Object[]'s for
		backOrders = new ArrayList<>(size); // Object[]'s we couldn't fulfill
		idxList = new ArrayDeque<>(size); // ordered list of idx's
		indexMap = new HashMap<>(size); // map from idx -> product_id
		quantityMap = new HashMap<>(size); // map from product_id -> quantity
		
		while(rs.next()) {
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			quantityMap.put(productId, quantity);
			indexMap.put(idx, productId);
			idxList.add(idx);
		} // End while
	} // End Constructor
	
	/* Get a list of booleans showing which items can be fulfilled.
	 * This list is parallel with the list of items from an order.
	 * */
	private boolean canProcessOrder() {
		ArrayList<Boolean> boolArray = new ArrayList<>();
		Iterator<TransactionItem> it = order.iterator();
		while(it.hasNext()) {
			TransactionItem item = it.next();
			Integer currentQuantity = quantityMap.get(item.getProductId());
			if(currentQuantity != null &&
			   currentQuantity >= item.getQuantity()) {
				boolArray.add(true);
				item.setIsProcessable(true);
			} else {
				boolArray.add(false);
				item.setIsProcessable(false);
			}
		}
		order.setBoolArray(boolArray);
		boolean canProcessOrder = !boolArray.contains(false);
		if(canProcessOrder) {
			changeQuantities();
		} else {
			insertBackOrders();
		}
		return canProcessOrder;
	}
	
	/* Change relevant quantities from a given order,
	 and put all items into the acceptedSales list.*/
	private void changeQuantities() {
		ArrayList<TransactionItem> items = order.getItems();
		order.setDateAccepted(LocalDate.now());
		for(TransactionItem item: items) {
			String productId = item.getProductId();
			quantityMap.put(productId, quantityMap.get(productId)
									   - item.getQuantity());
		}
		acceptedSales.addAll(order.toSalesArray());
	}
	
	/* Put all items from an order into the backOrders queue. */
	private void insertBackOrders() {
		backOrders.addAll(order.toBackOrderArray());
	}
	
	public static void processFileOrders(Crud crud, String pathname)
	throws SQLException, FileNotFoundException {
		SalesProcessor processor = new SalesProcessor(crud);
		Scanner scanner = new Scanner(new File(pathname));
		scanner.nextLine();
		String[] line = scanner.nextLine().split(",");
		Order order = new Order(LocalDate.parse(line[0]),
		 true, line[2], Order.generateId());
		order.setEmail(line[0]);
		processor.setOrder(order);
		while(line != null) {
			TransactionItem item = new TransactionItem(
			 line[3], Integer.parseInt(line[4]));
			LocalDate date = LocalDate.parse(line[0]);
			if(!(order.getEmail().equals(line[0]) &&
				 order.getLocation().equals(line[2]) &&
				 order.getDateOrdered().isEqual(date) &&
				 scanner.hasNextLine())) {
				processor.processOrder();
				order = new Order(LocalDate.parse(line[0]),
				 true, line[2], Order.generateId());
				order.setEmail(line[0]);
				processor.setOrder(order);
			}
			order.add(item);
			line = scanner.hasNextLine() ?
			 scanner.nextLine().split(",") : null;
		}
		processor.updateAndClose();
	}
	
	public boolean processOrder() {
		boolean canProcessOrder = canProcessOrder();
		assert order != null;
		StringBuilder builder = new StringBuilder();
		Iterator<TransactionItem> itemIt = order.iterator();
		while(itemIt.hasNext()) {
			builder.append(itemIt.next().toString() + "\n\n");
		}
		order.setText((canProcessOrder ?
		 "The following products have been processed:" :
		 "The following products could not be processed:") +
					  "\n\n" + builder.toString() + "\n" +
					  (canProcessOrder ?
					   "Thank you for using our service."
					   : "We are currently unable to fulfill this order."));
		order.setSubject(canProcessOrder);
		return canProcessOrder;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	/* Update all the tables after orders have been processed. */
	public void updateAndClose() throws SQLException {
		crud.setWorkingTable("sales");
		
		if(acceptedSales.size() > 0) {
			crud.insertRecords(Order.SALES_COLUMNS,
			 acceptedSales.iterator(), acceptedSales.size());
		}
		
		if(backOrders.size() > 0) {
			crud.insertRecords(Order.BACK_ORDER_COLUMNS,
			 backOrders.iterator(), backOrders.size());
		}
		// update the inventory table to effectively close the processor.
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> idxItr = idxList.iterator();
		while(idxItr.hasNext()) {
			Integer idx = idxItr.next();
			String productId = indexMap.get(idx);
			Integer quantity = quantityMap.get(productId);
			sb.append("('").append(productId).append("',")
			  .append(quantity).append(")")
			  .append(idxItr.hasNext() ? "," : ";");
		}
		crud.update("Drop table if exists temp_table");
		crud.update(
		 "CREATE TEMPORARY TABLE temp_table(" +
		 "quantity int(16)," +
		 "product_id varchar(16));");
		
		crud.update(
		 "INSERT INTO temp_table(product_id,quantity)VALUES" + sb.toString());
		
		crud.update(
		 "CREATE TABLE temp2 SELECT " +
		 "inventory.product_id," +
		 "inventory.wholesale_cost," +
		 "inventory.sale_price," +
		 "inventory.supplier_id," +
		 "inventory.idx," +
		 "temp_table.quantity " +
		 "FROM inventory INNER JOIN temp_table " +
		 "ON inventory.product_id = temp_table.product_id"
		);
		crud.update("DROP TABLE inventory");
		crud.update("ALTER TABLE temp2 RENAME TO inventory");
		
		//
		//crud.setWorkingTable("temp_table");
		//String dbName = crud.getDatabaseName();
		//crud.update
		// ("UPDATE " + dbName + ".inventory i " +
		//  "INNER JOIN temp_table ON i.product_id = temp_table.product_id " +
		//  "SET i.quantity = temp_table.quantity " +
		//  "WHERE i.product_id = temp_table.product_id");
	}
}

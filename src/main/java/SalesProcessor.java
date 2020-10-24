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
	
	/* Update all the tables after orders have been processed. */
	public void closeProcessor() throws SQLException {
		crud.setWorkingTable("sales");
		crud.insertRecords(
		 Order.SALES_COLUMNS,
		 acceptedSales.iterator(), acceptedSales.size());
		crud.insertRecords(
		 Order.BACK_ORDER_COLUMNS,
		 backOrders.iterator(), backOrders.size());
		
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

		crud.setWorkingTable("temp_table");
		crud.update("Drop table if exists temp_table");
		crud.update("CREATE TEMPORARY TABLE temp_table(" +
			   "quantity int(16), product_id varchar(16));");
		crud.update(
		 "insert into temp_table(product_id,quantity)VALUES" + sb.toString());
		String dbName = crud.getDatabaseName();
		crud.update
		 ("UPDATE " + dbName + ".inventory i " +
		  "INNER JOIN " + dbName + ".temp_table t ON i.product_id = t.product_id " +
		  "SET i.quantity = t.quantity " +
		  "WHERE i.product_id = t.product_id");
	}
	
	/* Put all items from an order into the backOrders queue. */
	private void insertBackOrders() {
		backOrders.addAll(order.toBackOrderArray());
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
}

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

class SalesProcessor {
	ArrayList<Object[]> backOrders;
	private final Crud crud;
	Queue<Integer> idxList;
	HashMap<Integer, String> indexMap;
	Order order;
	private HashMap<String, Integer> quantityMap;
	ArrayList<Object[]> sales;

	public SalesProcessor(Crud crud) throws SQLException {
		this.crud = crud;
		this.order = order;
		ResultSet rs =
		 crud.query("SELECT quantity,idx,product_id FROM inventory");
		crud.setWorkingTable("inventory");
		int size = crud.size();
		sales = new ArrayList<>(size); // list of Object[]'s for
		idxList = new ArrayDeque<>(size); // ordered list of idx's
		indexMap = new HashMap<>(size); // map from idx -> product_id
		quantityMap = new HashMap<>(size); // map from product_id -> quantity
		backOrders = new ArrayList<>(size); // anything we couldn't fulfill

		while(rs.next()) {
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			quantityMap.put(productId, quantity);
			indexMap.put(idx, productId);
			idxList.add(idx);
		} // End while
	} // End Constructor

	public ArrayList<Boolean> canProcessOrder() {
		ArrayList<Boolean> canProcessItems = new ArrayList<>();
		Iterator<TransactionItem> it = order.iterator();
		while(it.hasNext()) {
			TransactionItem item = it.next();
			Integer currentQuantity = quantityMap.get(item.getProductId());
			if(currentQuantity != null &&
			   currentQuantity >= item.getQuantity()) {
				canProcessItems.add(false);
			} else {
				canProcessItems.add(true);
			}
		}
		return canProcessItems;
	}
	
	public void close() throws SQLException {

		crud.insertRecords(
		 Order.BACK_ORDER_COLUMNS,
		 sales.iterator(), sales.size());
		crud.insertRecords(
		 Order.BACK_ORDER_COLUMNS,
		 backOrders.iterator(), backOrders.size());
		updateInventoryTable();
	}
	
	public void insertBackOrders(Order order) {
		crud.setWorkingTable("back_orders");

		ArrayList<Object[]> array = order.toBackOrderArray();

		crud.insertRecords(Order.BACK_ORDER_COLUMNS,
		 array.iterator(), array.size());
	}
	
	public void processOrder(Order order) {
		ArrayList<TransactionItem> items = order.getItems();
		order.setDateAccepted(LocalDate.now());
		for(TransactionItem item: items) {
			String productId = item.getProductId();
			quantityMap.put(productId, quantityMap.get(productId)
									   - item.getQuantity());
		}
		sales.addAll(order.toSalesArray());
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	private void updateInventoryTable() throws SQLException {
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
		
		crud.update("CREATE TEMPORARY TABLE temp_table(" +
		 "quantity int(16), product_id varchar(16));");
		sb.append("insert into temp_table(product_id,quantity)values");
		crud.update(sb.toString());
		crud.update("CREATE TABLE temp2 SELECT inventory.product_id," +
					"inventory.wholesale_cost,inventory.sale_price," +
					"inventory.supplier_id,inventory.idx," +
					"temp_table.quantity " +
					"FROM inventory INNER JOIN temp_table " +
					"ON inventory.product_id = temp_table.product_id");
		crud.update("drop table inventory");
		crud.update("alter table temp2 rename to inventory");
		
		/*
		* c1 c2 c3
		* a   b  c2
		
		* 
		* 
		* c3
		* c2
		* 
		* c1 c2 c3
		* a   b  c
		* * */
		
		
		
	}
}

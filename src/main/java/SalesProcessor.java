import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

class SalesProcessor {
	private final ArrayList<Object[]> acceptedOrders;
	private final Crud crud;
	private final Queue<Integer> idxList;
	private final HashMap<Integer, String> indexMap;
	private Order order;
	private final HashMap<String, Integer> quantityMap;
	
	/**
	 * SalesProcessor uses an in-memory copy of three inventory columns.
	 *
	 * @param crud
	 *  query and update.
	 *
	 * @throws SQLException
	 *  if you mess up.
	 *  <code>idxList</code> preserves the order of rows when using HashMaps.
	 *  <code>indexMap</code> maps idx to product_id.
	 *  <code>idxList</code> maps product_id to quantity.
	 *  <code>SalesProcessor</code> is intended to be used by running orders
	 *  through it,
	 *  then updating quantities from the inventory upon closure.
	 */
	public SalesProcessor(Crud crud) throws SQLException {
		this.crud = crud;
		ResultSet rs =
		 crud.query("SELECT quantity,idx,product_id FROM inventory");
		crud.setWorkingTable("inventory");
		int size = crud.size();
		idxList = new ArrayDeque<>(size); // ordered list of idx's
		indexMap = new HashMap<>(size); // map from idx -> product_id
		quantityMap = new HashMap<>(size); // map from product_id -> quantity
		acceptedOrders = new ArrayList<>(size); // list of Object[]'s for
		
		while(rs.next()) {
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			quantityMap.put(productId, quantity);
			indexMap.put(idx, productId);
			idxList.add(idx);
		} // End while
	} // End Constructor
	
	/**
	 * @return true if the entire order can be fulfilled
	 */
	private Boolean canProcessOrder() {
		ArrayList<Boolean> boolArray = new ArrayList<>();
		boolean isSale = order.isSale();
		for(Iterator<Product> it = order.iterator(); it.hasNext(); ) {
			Product prod = it.next();
			String productId = prod.getId();
			Integer quantity = quantityMap.get(prod.getId());
			int request = prod.getQuantity();
			boolean validQuantity = prod.getQuantity() > 0;
			boolean productNotNull = productId != null;
			boolean isProcessable = validQuantity && productNotNull &&
									(!isSale || quantity >= request);
			boolArray.add(isProcessable);
			prod.setIsProcessable(isProcessable, quantity);
		}
		boolean canProcessOrder = !boolArray.contains(false);
		if(canProcessOrder || !isSale) {
			order.setStatus(Order.PROCESSED);
			changeQuantities();
			acceptedOrders.addAll(order.toArray());
		} else {
			order.setStatus(Order.QUANTITY_SHORTAGE);
		}
		
		return canProcessOrder;
	}
	
	/* Change relevant quantities from a given order,
	 and put all items into the acceptedSales list.*/
	private void changeQuantities() {
		order.setDateAccepted(LocalDate.now());
		Iterator<Product> it = order.iterator();
		while(it.hasNext()) {
			Product prod = it.next();
			String productId = prod.getId();
			if(prod.isProcessable()) {
				Integer currentQuantity = quantityMap.get(productId);
				int eventQuantity =
				 prod.getQuantity() * (order.isSale() ? -1 : 1);
				int newQuantity = currentQuantity + eventQuantity;
				quantityMap.put(productId, newQuantity);
			}
		}
	}
	
	/**
	 * Process a single order.
	 */
	public boolean processOrder() {
		if(order.isProcessed()) {
			return canProcessOrder();
		}
		boolean canProcessOrder = canProcessOrder();
		assert order != null;
		StringBuilder builder = new StringBuilder();
		for(Iterator<Product> it = order.iterator(); it.hasNext();) {
			builder.append(it.next().toString()).append("\n");
		}
		String orderNumber = "Order #" + order.getId() ;
		String prefix = orderNumber + (canProcessOrder ?
		 "The following products have been processed:" :
		 "The following products could not be processed:");
		String suffix = canProcessOrder ?
		 "Thank you for using our service." :
		 "We are currently unable to fulfill this order.";
		order.setText(prefix + "\n\n" + builder.toString() + "\n" + suffix);
		order
		 .setSubject(orderNumber + " " + order.getStatusString());
		order.setCanProcess(canProcessOrder);
		return canProcessOrder;
	}
	
	/**
	 * Process all orders from a file.
	 *
	 * @param pathname
	 *  of the file to read.
	 *
	 * @return
	 *
	 * @throws SQLException
	 *  if you mess up.
	 * @throws FileNotFoundException
	 *  obvious.
	 */
	public void runFileOrders(String pathname)
	throws SQLException, FileNotFoundException {
		Scanner scanner = new Scanner(new File(pathname));
		scanner.nextLine();
		String[] line = scanner.nextLine().split(",");
		Order order = new Order(LocalDate.parse(line[0]),
		 true, line[2]);
		order.setEmail(line[1]);
		setOrder(order);
		while(line != null) {
			Product item = new Product(
			 line[3], Integer.parseInt(line[4]));
			LocalDate date = LocalDate.parse(line[0]);
			if(!(order.getCustomerEmail().equals(line[1]) &&
				 order.getLocation().equals(line[2]) &&
				 order.getDateOrdered().isEqual(date) &&
				 scanner.hasNextLine())) {
				order.add(item);
				processOrder();
				order = new Order(LocalDate.parse(line[0]),
				 true, line[2]);
				order.setEmail(line[1]);
				setOrder(order);
			}
			order.add(item);
			line = scanner.hasNextLine() ?
			 scanner.nextLine().split(",") : null;
		}
		updateAndClose();
	}
	
	/**
	 * @param order
	 *  the next order to be processed.
	 */
	public void setOrder(Order order) {
		this.order = order;
	}
	
	/* Update all the tables after orders have been processed. */
	public void updateAndClose() throws SQLException {
		crud.setWorkingTable("sales");
		
		if(acceptedOrders.size() > 0) {
			crud.insertRecords(Order.SALES_COLUMNS,
			 acceptedOrders.iterator(), acceptedOrders.size());
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
			  .append(idxItr.hasNext()?",":";");
		}
		crud.update("Drop table if exists temp_table");
		crud.update("Drop table if exists temp2");
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
	}
}

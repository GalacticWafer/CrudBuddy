package customerrelationsmanagement;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

class OrderProcessor {
	private final ArrayList<Object[]> acceptedOrders;
	private final Crud crud;
	private Order nextOrder;
	private final Queue<Integer> idxList;
	private final HashMap<Integer, String> indexMap;
	private final Stack<Order> orderStack;
	/* Change relevant quantities from a given order,
	 and put all items into the acceptedSales list.*/
	Timestamp orderStamp = nextOrder.getTimeOrdered();
	private final HashMap<String, Integer> quantityMap;
	
	/**
	 * OrderProcessor uses an in-memory copy of three inventory columns.
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
	public OrderProcessor(Crud crud) throws SQLException {
		
		this.crud = crud;
		
		crud.setWorkingTable("inventory");
		
		ResultSet rs = crud.query(
		 "SELECT quantity,idx,product_id FROM inventory");
		
		int size = crud.size();
		idxList = new ArrayDeque<>(size); // ordered list of idx's
		indexMap = new HashMap<>(size); // map from idx -> product_id
		quantityMap = new HashMap<>(size); // map from product_id -> quantity
		acceptedOrders = new ArrayList<>(size); // list of Object[]'s for
		orderStack = new Stack<>();
		
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
		
		ArrayList<Boolean> canFulfillProducts = new ArrayList<>();
		boolean isSale = nextOrder.isSale();
		
		for(Iterator<Product> productIter = nextOrder.productIterator();
			productIter.hasNext(); ) {
			
			Product product = productIter.next();
			String productId = product.getId();
			Integer inventoryQuantity = quantityMap.get(product.getId());
			int requestedQuantity = product.getQuantity();
			boolean validQuantity = product.getQuantity() > 0;
			boolean productNotNull = productId != null;
			boolean hasSufficientQuantity;
			
			hasSufficientQuantity =
			 !isSale || inventoryQuantity >= requestedQuantity;
			
			boolean canFulfillCurrentProduct
			 = validQuantity
			   && productNotNull
			   && hasSufficientQuantity;
			
			canFulfillProducts.add(canFulfillCurrentProduct);
			
			product.setIsProcessable(
			 canFulfillCurrentProduct, inventoryQuantity);
		} // End for
		
		boolean canProcessOrder = !canFulfillProducts.contains(false);
		
		if(canProcessOrder || !isSale) {
			nextOrder.setStatus(Order.PROCESSED);
			changeQuantities();
		} else {
			nextOrder.setStatus(Order.QUANTITY_SHORTAGE);
		} // End if
		acceptedOrders.addAll(nextOrder.toArray());
		
		return canProcessOrder;
	} // End canProcessOrder
	
	private void changeQuantities() {
		
		nextOrder.setTimeAccepted();
		Iterator<Product> it = nextOrder.productIterator();
		
		while(it.hasNext()) {
			Product prod = it.next();
			String productId = prod.getId();
			
			if(prod.isProcessable()) {
				Integer nextQuantity = quantityMap.get(productId);
				int eventQuantity =
				 prod.getQuantity() * (nextOrder.isSale() ? -1 : 1);
				int newQuantity = nextQuantity + eventQuantity;
				quantityMap.put(productId, newQuantity);
			} // End if
		} // End while
	} // End changeQuantities
	
	/**
	 * Processes the <code>nextOrder</code> to set its fields according to
	 * all Products it contains.
	 *
	 * @return true if all <code>Product</code>s from <code>nextOrder
	 *  .iterator()</code>
	 *  can be processed. Otherwise, false.
	 */
	public boolean processOrder() {
		
		if(nextOrder.isProcessed()) {
			return canProcessOrder();
		} // End if.
		
		boolean canProcessOrder = canProcessOrder();
		assert nextOrder != null;
		StringBuilder builder = new StringBuilder();
		
		for(Iterator<Product> it = nextOrder.productIterator();
			it.hasNext(); ) {
			
			builder.append(it.next().toString()).append("\n");
		} // End for.
		
		String orderNumber = "Order #" + nextOrder.getId();
		
		String responsePrefix = orderNumber;
		String responseSuffix;
		
		if(canProcessOrder) {
			responsePrefix += "The following products have been processed: ";
			responseSuffix = "Thank you for using our service.";
			nextOrder.setStatus(Order.PROCESSED);
		} else {
			responsePrefix += "The following products could not be processed:";
			responseSuffix = "We are nextly unable to fulfill this order.";
			nextOrder.setStatus(Order.CANCELLED);
		} // End if.
		
		nextOrder.setText(responsePrefix + "\n\n"
							 + builder.toString()
							 + "\n" + responseSuffix);
		
		nextOrder.setSubject(orderNumber + " "
								+ nextOrder.getStatusString());
		
		if(!orderStack.isEmpty() &&
		   (orderStack.peek().getTimeOrdered()
					  .before(nextOrder.getTimeOrdered()))) {
			// run analytics on today's orders
			orderStack.clear();
		} else {
			orderStack.push(nextOrder);
		}
		return canProcessOrder;
	} // End processOrder
	
	/**
	 * Process all orders from a file.
	 *
	 * @param pathname
	 *  the file to read, which should be a csv file.
	 *
	 * @throws SQLException
	 *  if you mess up.
	 * @throws FileNotFoundException
	 *  if the file was not found or
	 *  TODO: if the file is not a csv file
	 *  or
	 *  TODO: if any line in the csv is not in the proper format: 2020-01-02,
	 *  saust@hotmail.com,38813,3R8YXZCS820Y,2
	 */
	public void runFileOrders(String pathname)
	throws SQLException, FileNotFoundException {
		
		Scanner scanner = new Scanner(new File(pathname));
		scanner.nextLine();
		String[] line = scanner.nextLine().split(",");
		
		Order order = new Order(
		 Timestamp.valueOf(line[0]), true, line[2]);
		
		order.setEmail(line[1]);
		this.setCurrentOrder(order);
		int i = 2;
		while(line != null) {
			
			Timestamp nextTime = Timestamp.valueOf(line[0]);
			String nextEmail = line[1];
			String nextLocation = line[2];
			String nextProductId = line[3];
			int nextRequestedQuantity = Integer.parseInt(line[4]);
			
			Product nextProduct = new Product(
			 nextProductId, nextRequestedQuantity);
			
			boolean isNewDate = order.getTimeOrdered().before(nextTime);
			boolean isNewEmail =
			 !order.getCustomerEmail().equals(nextEmail);
			boolean isNewLocation =
			 !order.getLocation().equals(nextLocation);
			
			boolean isNewOrder = isNewEmail
								 || isNewLocation
								 || isNewDate;
			
			if(isNewOrder || !scanner.hasNextLine()) {
				
				if(!scanner.hasNextLine() && isNewOrder) {
					order.addProduct(nextProduct);
				} // End if
				
				processOrder();
				if(orderStack.isEmpty()) {
					orderStack.push(order);
				} else {
					Timestamp lastOrderTime =
					 orderStack.peek().getTimeOrdered();
					if(lastOrderTime.compareTo(order.getTimeAccepted()) == 0) {
						orderStack.push(order);
					} else {
						// Analyze the all the orders in the stack
							// record the top ten items of all orders to an ArrayList
								// if there are ten 
							// record the top ten customers of all orders to an ArrayList
							// record the next date to an ArrayList
							// record the daily sum of assets to a double
						// save all daily statistics to a Queue<DailyStats>
						
						orderStack.clear();
						orderStack.push(order);
					}
				}
				order = new Order(
				 Timestamp.valueOf(line[0]), true, nextLocation);
				
				order.setEmail(nextEmail);
				setCurrentOrder(order);
				
				if(!scanner.hasNextLine()) {
					break;
				} // End if
			} // End if
			
			order.addProduct(nextProduct);
			
			if(scanner.hasNextLine()) {
				line = scanner.nextLine().split(",");
			} else {
				line = null;
			} // End if
			i++;
		} // End while
		updateAndClose();
	} // End runFileOrders
	
	public static void runFileOrders(Crud crud, String ordersPath)
	throws SQLException, FileNotFoundException {
		
		new OrderProcessor(crud).runFileOrders(ordersPath);
	}
	
	/**
	 * Changes nextOrder to a new Order object which should be processed.
	 *
	 * @param order
	 *  the next order to be processed.
	 */
	public void setCurrentOrder(Order order) {
		
		this.nextOrder = order;
	} // End seCurrentOrder
	
	/* Update all the tables after orders have been processed. */
	public void updateAndClose() throws SQLException {
		
		crud.setWorkingTable("sales");
		JOptionPane
		 .showMessageDialog(null, "put the breakpoint on line 269 in Crud" +
								  ".java");
		if(acceptedOrders.size() > 0) {
			crud.insertRecords(Order.SALES_COLUMNS,
			 acceptedOrders.iterator(), acceptedOrders.size());
		} // End if
		
		// update the inventory table to effectively close the processor.
		StringBuilder builder = new StringBuilder();
		Iterator<Integer> idxItr = idxList.iterator();
		
		while(idxItr.hasNext()) {
			Integer idx = idxItr.next();
			String productId = indexMap.get(idx);
			Integer quantity = quantityMap.get(productId);
			
			builder.append("('").append(productId).append("',")
				   .append(quantity).append(")")
				   .append(idxItr.hasNext() ? "," : ";");
		} // End while
		
		crud.update("Drop table if exists temp_table");
		crud.update("Drop table if exists temp2");
		crud.update(
		 "CREATE TEMPORARY TABLE temp_table(" +
		 "quantity int(16)," +
		 "product_id varchar(16));");
		
		crud.update(
		 "INSERT INTO temp_table(product_id,quantity)VALUES" +
		 builder.toString());
		
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
	} // End updateAndClose
} // End SalesProcessor
	

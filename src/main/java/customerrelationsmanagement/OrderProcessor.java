package customerrelationsmanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

class OrderProcessor {
	private final ArrayList<Object[]> acceptedOrders;
	private BigDecimal assetTotal;
	private final Crud crud;
	private final ArrayList<Object[]> dailyAnalytics;
	private final Stack<Order> dailyOrderStack;
	private final Queue<Integer> idxList;
	private final HashMap<Integer, String> indexMap;
	private Order nextOrder;
	/* Change relevant quantities from a given order,
	 and put all items into the acceptedSales list.*/
	private final HashMap<String, Integer> quantityMap;
	private int recordCount;
	private final HashMap<String, BigDecimal> salePriceMap;
	private final ArrayList<Object[]> supplierEvents;
	private final HashMap<String, String> supplierMap;
	private final HashMap<String, BigDecimal> wholesaleMap;
	public static final int MAX_ROWS = 150000;
	
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
		 "SELECT quantity,idx,product_id,wholesale_cost,sale_price FROM " +
		 "inventory");
		
		int size = crud.size();
		idxList = new ArrayDeque<>(size); // ordered list of idx's
		indexMap = new HashMap<>(size); // map from idx -> product_id
		quantityMap = new HashMap<>(size); // map from product_id -> quantity
		wholesaleMap = new HashMap<>(size); // map from product_id -> quantity
		salePriceMap = new HashMap<>(size); // map from product_id -> quantity
		acceptedOrders = new ArrayList<>(size); // list of Object[]'s for
		supplierEvents = new ArrayList<>(); // list of all supplier events
		dailyOrderStack =
		 new Stack<>(); // list of rows with matching date_ordered
		dailyAnalytics =
		 new ArrayList<>(); //list of analytics created from dailyOrderStack's
		// contents
		supplierMap = new HashMap<>(); // map of <product_id, supplier_id> 
		assetTotal = BigDecimal.ZERO;
		recordCount = 0;
		while(rs.next()) {
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			BigDecimal wholesaleCost = rs.getBigDecimal(4);
			BigDecimal salePrice = rs.getBigDecimal(5);
			
			quantityMap.put(productId, quantity);
			wholesaleMap.put(productId, wholesaleCost);
			salePriceMap.put(productId, salePrice);
			indexMap.put(idx, productId);
			idxList.add(idx);
			assetTotal =
			 assetTotal.add(salePrice.multiply(BigDecimal.valueOf(quantity)));
			//Todo Adam, add the product id and the key as the value in 
			// supplierMap
		} // End while
	} // End Constructor
	
	private void analyzeOrders() {
		
		dailyAnalytics.add(new DailyStats().toArray());
		recordCount += 1;
		if(recordCount > MAX_ROWS) {
			update();
		}
		dailyOrderStack.clear();
	}
	
	/**
	 * @return true if the entire order can be fulfilled
	 */
	private Boolean canProcessOrder() {
		
		ArrayList<Boolean> canFulfillProducts = new ArrayList<>();
		boolean isSale = nextOrder.eventType() == EventType.BUYER;
		
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
			nextOrder.setStatus(Status.PROCESSED);
			changeQuantities();
		} else {
			nextOrder.setStatus(Status.QUANTITY_SHORTAGE);
		} // End if
		ArrayList<Object[]> records = nextOrder.toArray();
		if(records != null) {
			acceptedOrders.addAll(records);
			recordCount += records.size();
			if(recordCount > 150000) {
				update();
			}
		}
		
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
				 prod.getQuantity() *
				 (nextOrder.eventType() == EventType.BUYER ? -1 : 1);
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
		
		if(nextOrder.getStatus() != Status.PROCESSED
		   && nextOrder.getStatus() != Status.QUANTITY_SHORTAGE) {
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
			nextOrder.setStatus(Status.PROCESSED);
			for(Iterator<Product> it = nextOrder.productIterator();
				it.hasNext(); ) {
				Product product = it.next();
				
				assetTotal = assetTotal.subtract(
				 salePriceMap.get(product.getId()).multiply(
				  BigDecimal.valueOf(product.getQuantity())));
			}
		} else {
			
			for(Iterator<Product> it = nextOrder.productIterator();
				it.hasNext(); ) {
				Product product = it.next();
				String productId = product.getId();
				Integer inventoryQuantity = quantityMap.get(productId);
				Integer requestedQuantity = product.getQuantity();
				if(inventoryQuantity < requestedQuantity) {
					// Todo Adam refactor the code below by introducing
					//  an int variable (from the nextInt() call)
					//  check this link for instructions on completing this 
					//  action
					// https://www.jetbrains.com/help/idea/extract-variable
					// .html
					quantityMap.put(productId, requestedQuantity
											   + new Random().nextInt(450) +
											   50);
					//Todo Adam the productId to get the appropriate 
					// supplierId from a supplierMap 
					// use the date, supplierId, and 
					
					// Todo Adam add a new Object[] to supplier with:
					//  the supplierId 
					//  the productId
					//  the int variable you introduced,
					//  the time "order" was ordered (look in Order.java for 
					//  the appropriate getter method)
					
				}
			}
			responsePrefix += "The following products could not be processed:";
			responseSuffix = "We are currently unable to fulfill this order.";
			nextOrder.setStatus(Status.CANCELLED);
		} // End if.
		
		nextOrder.setText(responsePrefix + "\n\n"
						  + builder.toString()
						  + "\n" + responseSuffix);
		
		nextOrder.setSubject(orderNumber + " "
							 + nextOrder.getStatusString());
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
	 *  or
	 *  saust@hotmail.com,38813,3R8YXZCS820Y,2
	 */
	public void runFileOrders(String pathname)
	throws SQLException, FileNotFoundException {
		
		Scanner scanner = new Scanner(new File(pathname));
		scanner.nextLine();
		String[] line = scanner.nextLine().split(",");
		
		Order order = new Order(
		 Timestamp.valueOf(LocalDate.parse(line[0]).atStartOfDay()),
		 EventType.BUYER,
		 line[2]
		);
		
		order.setEmail(line[1]);
		this.setCurrentOrder(order);
		int i = 2;
		while(line != null && !line[0].equals("")) {
			Timestamp nextTime =
			 Timestamp.valueOf(LocalDate.parse(line[0]).atStartOfDay());
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
				if(dailyOrderStack.isEmpty()) {
					dailyOrderStack.push(order);
				} else {
					Timestamp lastOrderTime =
					 dailyOrderStack.peek().getTimeOrdered();
					Timestamp nextOrderTime = order.getTimeOrdered();
					if(lastOrderTime.compareTo(nextOrderTime) == 0) {
						dailyOrderStack.push(order);
					} else {
						analyzeOrders();
						dailyOrderStack.clear();
						dailyOrderStack.push(order);
					}
				}
				order = new Order(
				 Timestamp.valueOf(LocalDate.parse(line[0]).atStartOfDay()),
				 EventType.BUYER,
				 nextLocation
				);
				
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
		
		if(!dailyOrderStack.isEmpty()) {
			analyzeOrders();
		}
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
	
	private void update() {
		crud.setWorkingTable(Tables.STATUSED.toString());
		if(acceptedOrders.size() > 0) {
			crud.insertRecords(Tables.STATUSED.columns(),
			 acceptedOrders.iterator(), acceptedOrders.size());
		} // End if
		acceptedOrders.clear();
		crud.setWorkingTable(Tables.ANALYTICS.toString());
		if(dailyAnalytics.size() > 0) {
			crud.insertRecords(Tables.ANALYTICS.columns(),
			 dailyAnalytics.iterator(), dailyAnalytics.size());
		} // End if
		dailyAnalytics.clear();
		recordCount = 0;
	}
	
	/** Update all the tables after orders have been processed. */
	public void updateAndClose() throws SQLException {
		
		// Todo Adam aet the working table to your new suppler table, 
		//  and insert the records exactly the same way it was done 
		//  immediately above this comment
		update();
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
	
	private class DailyStats {
		Date fiscalDate;
		BigDecimal incomeTotal;
		BigDecimal revenueTotal;
		Object[][] topCustomers;
		Object[][] topProducts;
		
		public DailyStats() {
			
			this.fiscalDate =
			 new Date(dailyOrderStack.peek().getTimeOrdered().getTime());
			calculateRevenueTotal();
			this.topCustomers = calculateTopCustomers();
			this.topProducts = calculateTopProducts();
		}
		
		private void calculateRevenueTotal() {
			
			revenueTotal = BigDecimal.ZERO;
			incomeTotal = BigDecimal.ZERO;
			for(Order order: dailyOrderStack) {
				if(order.getStatus().compareTo(Status.UNPROCESSED) < 0) {
					continue;
				}
				var it = order.productIterator();
				while(it.hasNext()) {
					Product product = it.next();
					String id = product.getId();
					BigDecimal salePrice = salePriceMap.get(id);
					BigDecimal quantity =
					 new BigDecimal(product.getQuantity());
					revenueTotal = revenueTotal
					 .add((salePrice.subtract(wholesaleMap.get(id)))
					  .multiply(quantity));
					incomeTotal = incomeTotal.add(salePrice);
				}
			}
		}
		
		private Object[][] calculateTopCustomers() {
			
			HashMap<String, BigDecimal> dailyCustomers = new HashMap<>();
			for(Order order: dailyOrderStack) {
				if(!(order.getStatus().compareTo(Status.UNPROCESSED) > 0)) {
					continue;
				}
				
				BigDecimal orderTotal = BigDecimal.ZERO;
				Iterator<Product> it = order.productIterator();
				while(it.hasNext()) {
					Product product = it.next();
					BigDecimal salePrice = salePriceMap.get(product.getId());
					BigDecimal quantity =
					 BigDecimal.valueOf(product.getQuantity());
					orderTotal = orderTotal.add(salePrice.multiply(quantity));
				}
				dailyCustomers.put(order.getCustomerEmail(), orderTotal);
			}
			List<Map.Entry<String, BigDecimal>> list =
			 new LinkedList<>(dailyCustomers.entrySet());
			// Sort the list 
			list.sort(Map.Entry.comparingByValue());
			
			Object[][] tops = new Object[list.size()][2];
			Iterator<Map.Entry<String, BigDecimal>> it = list.iterator();
			for(int i = 0; i < list.size(); i++) {
				Map.Entry<String, BigDecimal> entry = it.next();
				tops[i] = new Object[] {entry.getKey(), entry.getValue()};
			}
			return tops;
		}
		
		private Object[][] calculateTopProducts() {
			
			HashMap<String, Integer> dailyQuantities = new HashMap<>();
			for(Order order: dailyOrderStack) {
				if(!(order.getStatus().compareTo(Status.UNPROCESSED) > 0)) {
					continue;
				}
				Iterator<Product> it = order.productIterator();
				while(it.hasNext()) {
					Product product = it.next();
					if(dailyQuantities.containsKey(product.getId())) {
						
						dailyQuantities.put(
						 product.getId(),
						 product.getQuantity() +
						 dailyQuantities.get(product.getId()));
					} else {
						dailyQuantities
						 .put(product.getId(), product.getQuantity());
					}
				}
			}
			List<Map.Entry<String, Integer>> list =
			 new LinkedList<>(dailyQuantities.entrySet());
			HashMap<String, Integer> temp = new LinkedHashMap<>();
			// Sort the list 
			String str = "";
			Object[][] tops = new Object[list.size()][2];
			list.sort(Map.Entry.comparingByValue());
			Iterator<Map.Entry<String, Integer>> it = list.iterator();
			for(int i = 0; i < list.size() && i < 10; i++) {
				Map.Entry<String, Integer> entry = it.next();
				temp.put(entry.getKey(), entry.getValue());
				tops[i] = new Object[] {entry.getKey(), entry.getValue()};
			}
			return tops;
		}
		
		private String getTopCustomers() {
			
			String s = "";
			for(Object[] topCustomer: topCustomers) {
				s += topCustomer[0] + " " + topCustomer[1] + "\t";
			}
			return s;
		}
		
		private String getTopProducts() {
			
			String s = "";
			for(Object[] topProduct: topProducts) {
				s += topProduct[0] + " " + topProduct[1] + "\t";
			}
			return s;
		}
		
		public Object[] toArray() {
			
			int dailyProductCount = 0;
			for(Order order: dailyOrderStack) {
				if(order.getStatus().compareTo(Status.UNPROCESSED) <= 0) {
					continue;
				}
				dailyProductCount += order.size();
			}
			return new Object[] {
			 fiscalDate,              // "fiscal_date", 
			 assetTotal,              // "asset_total", 
			 incomeTotal,             // "daily_income",
			 revenueTotal,            // "daily_revenue", 
			 getTopCustomers() + "",  // "top_customers",
			 getTopProducts() + "",   // "top_products", 
			 dailyOrderStack.size(),  // "order_count", 
			 dailyProductCount,       // "product_count"
			};
		}
	}
} // End SalesProcessor
	
	
	  
	  
	  
	  
	  
	  
	  
	  
	

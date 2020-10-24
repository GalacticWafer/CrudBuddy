import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.time.temporal.ChronoUnit;

class SalesProcessor {
	private final Crud crud;
	private HashMap<String, String> customers;
	private HashMap<String, Integer> quantityMap;
	LocalDate newItemDate;
	Queue<TransactionItem> backOrders;
	LinkedList<Object[]> sales;
	HashMap<Integer, String> indexMap;
	ArrayList<Integer> idxList;
	
	public SalesProcessor(Crud crud) throws SQLException
	{
		this.crud = crud;
		/* Holds successful transaction information. */
		sales = new LinkedList<>();
		customers = new HashMap<>();
		/* Get a copy product_id, quantity, and idx columns, and put them in 
		relational hashmaps. */
		crud.setWorkingTable("inventory");
		ResultSet rs =
		 crud.query("SELECT quantity,idx,product_id FROM inventory");
		int size = crud.size();
		quantityMap = new HashMap<>(size);
		indexMap = new HashMap<>(size);
		idxList = new ArrayList<>(size);
		/* Use a FIFO collection for back-orders */
		backOrders = new ArrayDeque<>();
		
		while(rs.next())
		{
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			quantityMap.put(productId, quantity);
			indexMap.put(idx, productId);
			idxList.add(idx);
		} // End while
		
		
		newItemDate = LocalDate.parse("2020-01-01");
	} // End Constructor
	
	private void checkBackOrders(LinkedList<Object[]> sales, LocalDate today,
	 Queue<TransactionItem> backOrders)
	{
		if(!backOrders.isEmpty())
		{
			LocalDate backDate;
			for(backDate = backOrders.peek().getDateOrdered().plusDays(7);
			 backOrders.peek() != null && (!backDate.isAfter(today));
			 backDate = backOrders.peek().getDateOrdered().plusDays(7))
			{
				TransactionItem item = backOrders.remove();
				String productId = item.getProductId();
				int quantity = quantityMap.get(productId);
				quantityMap.put(productId, quantity + 500);
				processOrder(item, sales, today, backOrders);
				if(backOrders.isEmpty())
				{
					break;
				}
			}
		}
	}
	
	/**
	 * Carries out all the orders from a CSV file over the given time.
	 *
	 * @return
	 */
	public LinkedList<Object[]> processItems(String csvPath)
	 throws SQLException, FileNotFoundException
	{
		/* Get a scanner on the csv, and skip the column names.*/
		Scanner orderScanner = new Scanner(new File(csvPath));
		orderScanner.nextLine();
		for(LocalDate today = newItemDate;
		 orderScanner.hasNextLine() || !backOrders.isEmpty();
		 today = today.plusDays(1))
		{
			String[] line = new String[0];
			if(orderScanner.hasNextLine())
			{
				line = orderScanner.nextLine().split(",");
				newItemDate = LocalDate.parse(line[0]);
				if(newItemDate.isBefore(today))
				{
					today = newItemDate;
				}
			}
			else
			{
				assert backOrders.peek() != null;
				if(backOrders.peek().getDateOrdered().plusDays(7)
				 .isBefore(today))
				{
					checkBackOrders(sales, today, backOrders);
				}
			}
			checkBackOrders(sales, today, backOrders);
			while(newItemDate.isAfter(today))
			{
				today = today.plusDays(1);
			}
			TransactionItem newItem = new TransactionItem();
			if(orderScanner.hasNextLine() || line.length > 0)
			{
				newItem.setFields(
				 newItemDate, line[1], line[2], line[3],
				 Integer.parseInt(line[4])
				);
				processOrder(newItem, sales, today, backOrders);
			}
		}
		StringBuilder sb = new StringBuilder();
		crud.update("Drop table if exists temp_table");
		
		crud.update("CREATE TEMPORARY TABLE temp_table(" +
		 "quantity int(16), product_id varchar(16));");
		sb.append("insert into temp_table(product_id,quantity)values");
		
		Iterator<Integer> idxItr = idxList.iterator();
		while(idxItr.hasNext())
		{
			Integer idx = idxItr.next();
			String productId = indexMap.get(idx);
			Integer quantity = quantityMap.get(productId);
			sb.append("('").append(productId).append("',")
			 .append(quantity).append(")")
			 .append(idxItr.hasNext()?",":";");
		}
		crud.update(sb.toString());
		crud.update("CREATE TABLE temp2 SELECT inventory.product_id," +
		 "inventory.wholesale_cost,inventory.sale_price," +
		 "inventory.supplier_id,inventory.idx," +
		 "temp_table.quantity " +
		 "FROM inventory INNER JOIN temp_table " +
		 "ON inventory.product_id = temp_table.product_id");
		crud.update("drop table inventory");
		crud.update("alter table temp2 rename to inventory");
		
		crud.setWorkingTable("customers");
		Object[][] customerEntries = new Object[customers.size()][2];
		Iterator<Map.Entry<String, String>> custItr = customers.entrySet().iterator();
		for(int i = 0; i < customerEntries.length; i++)
		{
			Map.Entry<String, String> customer = custItr.next();
			customerEntries[i] = new Object[] {customer.getKey(), customer.getValue()};
		}
		crud.insertRecords(new String[] {"email", "location"}, customerEntries);
		/* Update the  database with the recorded  sales.*/
		sb.delete(0, sb.length());
		crud.setWorkingTable("sales");
		String[] salesColumns = crud.getColumnNames();
		Object[][] salesEntries = new Object[sales.size()][salesColumns.length];
		Iterator<Object[]> salesItr = sales.iterator();
		for(int i = 0; i < salesEntries.length; i++)
		{
			salesEntries[i] = salesItr.next();
		}
		crud.insertRecords(TransactionItem.SALES_COLUMNS, salesEntries);
		return sales;
	} // End processItems
	
	public LinkedList<Object[]> processItemsEmail(String csvPath)
	throws SQLException, FileNotFoundException
	{
		/* Get a scanner on the csv, and skip the column names.*/
		Scanner orderScanner = new Scanner(new File(csvPath));
		orderScanner.nextLine();
		for(LocalDate today = newItemDate;
			orderScanner.hasNextLine() || !backOrders.isEmpty();
			today = today.plusDays(1))
		{
			String[] line = new String[0];
			if(orderScanner.hasNextLine())
			{
				line = orderScanner.nextLine().split(",");
				newItemDate = LocalDate.parse(line[5]);
				if(newItemDate.isBefore(today))
				{
					today = newItemDate;
				}
			}
			else
			{
				assert backOrders.peek() != null;
				if(backOrders.peek().getDateOrdered().plusDays(7)
							 .isBefore(today))
				{
					checkBackOrders(sales, today, backOrders);
				}
			}
			checkBackOrders(sales, today, backOrders);
			while(newItemDate.isAfter(today))
			{
				today = today.plusDays(1);
			}
			TransactionItem newItem = new TransactionItem();
			if(orderScanner.hasNextLine() || line.length > 0)
			{
				if(line[3].contains("<")){
					String a = line[3];
					a = a.substring(a.indexOf("<") + 1);
					a = a.substring(0, a.indexOf(">"));
					line[3] = a;
					
				}
				newItem.setFields(
				 newItemDate, line[3], line[6], line[0],
				 Integer.parseInt(line[2])
				);
				processOrder(newItem, sales, today, backOrders);
			}
		}
		StringBuilder sb = new StringBuilder();
		crud.update("Drop table if exists temp_table");
		
		crud.update("CREATE TEMPORARY TABLE temp_table(" +
					"quantity int(16), product_id varchar(16));");
		sb.append("insert into temp_table(product_id,quantity)values");
		
		Iterator<Integer> idxItr = idxList.iterator();
		while(idxItr.hasNext())
		{
			Integer idx = idxItr.next();
			String productId = indexMap.get(idx);
			Integer quantity = quantityMap.get(productId);
			sb.append("('").append(productId).append("',")
			  .append(quantity).append(")")
			  .append(idxItr.hasNext()?",":";");
		}
		crud.update(sb.toString());
		crud.update("CREATE TABLE temp2 SELECT inventory.product_id," +
					"inventory.wholesale_cost,inventory.sale_price," +
					"inventory.supplier_id,inventory.idx," +
					"temp_table.quantity " +
					"FROM inventory INNER JOIN temp_table " +
					"ON inventory.product_id = temp_table.product_id");
		crud.update("drop table inventory");
		crud.update("alter table temp2 rename to inventory");
		
		crud.setWorkingTable("customers");
		Object[][] customerEntries = new Object[customers.size()][2];
		Iterator<Map.Entry<String, String>> custItr = customers.entrySet().iterator();
		for(int i = 0; i < customerEntries.length; i++)
		{
			Map.Entry<String, String> customer = custItr.next();
			customerEntries[i] = new Object[] {customer.getKey(), customer.getValue()};
		}
		crud.insertRecords(new String[] {"email", "location"}, customerEntries);
		/* Update the  database with the recorded  sales.*/
		sb.delete(0, sb.length());
		crud.setWorkingTable("sales");
		String[] salesColumns = crud.getColumnNames();
		Object[][] salesEntries = new Object[sales.size()][salesColumns.length];
		Iterator<Object[]> salesItr = sales.iterator();
		for(int i = 0; i < salesEntries.length; i++)
		{
			salesEntries[i] = salesItr.next();
		}
		crud.insertRecords(TransactionItem.SALES_COLUMNS, salesEntries);
		return sales;
	} // End processItems
	
	/**
	 * Attempts to process orders, and adds them to the list of back-orders if
	 * needed.
	 */
	private void processOrder(TransactionItem item, // Creates Tran.Item object
	 
	 //Create Linked List of Object arrays titled sales
	 LinkedList<Object[]> sales,
	 
	 // Create LocalDate Object called today
	 LocalDate today,
	 
	 // Create Queu of Trans.Item Objects
	 Queue<TransactionItem> backOrders)
	{
		// customers = Hash map of <String, String>
		customers.put(item.getEmail(), item.getLocation()); // put email/ location into hash map
		int check = quantityCheck(item); // check = number of item quantities
		
		// If there's a shortage...
		if(check == Crud.QUANTITY_SHORTAGE)
		{
			
			// ...And more than 6 days between orders, restock  
			if(ChronoUnit.DAYS.between(today, item.getDateOrdered()) >= 7)
			{
				restock(item);
			}
			// ...And there's no  backorder, add to backorders
			else if(!backOrders.contains(item))
			{
				backOrders.add(item);
			}
		}
		else if(check >= 0)
		{
			/*otherwise, the sale was made, so add this info to the sales 
			table */
			item.setDateAccepted(today);
			sales.add(item.toArray(Crud.SALES)); // Add row into Object array
		}// End else if
	} // End processOrder
	
	/**
	 * Checks to make sure there is enough quantity to fulfill the order.
	 * if yes, @return is the quantity remaining, otherwise, a self-explanatory
	 * Crud constant, which is a negative number.
	 */
	// Change back to private
	public int quantityCheck(TransactionItem item)
	{
		String productId = item.getProductId();

		/* If the product is not something we actually sell, return 
		UNKNOWN_PRODUCT*/
		if(!quantityMap.containsKey(productId))
		{
			item.setResultString(productId + " : unknown product id");
			System.out.println(productId + " : unknown product id");
			return Crud.UNKNOWN_PRODUCT;
		} // End if
		
		int requestQuantity = item.getRequestedQuantity();
		int inventoryQuantity = quantityMap.get(productId);
		
		/* If the requested quantity is too large, return QUANTITY_SHORTAGE*/
		if(requestQuantity > inventoryQuantity)
		{
			item.setResultString(productId + ": " + inventoryQuantity
			 + " - " +
			 (inventoryQuantity + requestQuantity));
			return Crud.QUANTITY_SHORTAGE;
		} // End if

		/* Otherwise, subtract requested from inventory's quantity, and return
		 this number. */
		item.setResultString(productId + " : " + requestQuantity);
		item.setCurrentQuantity(requestQuantity);
		int newQuantity = inventoryQuantity - requestQuantity;
		quantityMap.put(productId, newQuantity);
		return newQuantity;
	}
	
	private void restock(TransactionItem item)
	{
		String id = item.getProductId();
		Integer quantity = quantityMap.get(id);
		quantityMap.put(id, quantity + 500);
	}
}

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.time.temporal.ChronoUnit;

class SalesProcessor {
	private final Crud crud;
	private HashMap<String, Integer> quantityMap;

	public SalesProcessor(Crud crud) {
		this.crud = crud;
	} // End Constructor

	private void checkBackOrders(LinkedList<Object[]> sales, LocalDate today,
								 Queue<TransactionItem> backOrders) {
		if(!backOrders.isEmpty()) {
			LocalDate backDate;
			for(backDate = backOrders.peek().getDateOrdered().plusDays(7);
				backOrders.peek() != null && (!backDate.isAfter(today));
				backDate = backOrders.peek().getDateOrdered().plusDays(7)) {
				TransactionItem item = backOrders.remove();
				String productId = item.getProductId();
				int quantity = quantityMap.get(productId);
				quantityMap.put(productId, quantity + 500);
				processOrder(item, sales, today, backOrders);
				if(backOrders.isEmpty()) {
					break;
				}
			}
		}
	}

	/** Carries out all the orders from a CSV file over the given time. */
public void processItems(String csvPath)
	throws SQLException, FileNotFoundException {
		/* Holds successful transaction information. */
		LinkedList<Object[]> sales = new LinkedList<>();

		/* Get a copy product_id, quantity, and idx columns, and put them in 
		relational hashmaps. */
		crud.setWorkingTable("inventory");
		ResultSet rs =
		 crud.query("SELECT quantity,idx,product_id FROM inventory");
		int size = crud.size();
		quantityMap = new HashMap<>(size);
		HashMap<Integer, String> indexMap = new HashMap<>(size);
		ArrayList<Integer> idxList = new ArrayList<>(size);
		/* Use a FIFO collection for back-orders */
		Queue<TransactionItem> backOrders = new ArrayDeque<>();

		while(rs.next()) {
			int quantity = rs.getInt(1);
			int idx = rs.getInt(2);
			String productId = rs.getString(3);
			quantityMap.put(productId, quantity);
			indexMap.put(idx, productId);
			idxList.add(idx);
		} // End while

		/* Get a scanner on the csv, and skip the column names.*/
		Scanner orderScanner = new Scanner(new File(csvPath));
		orderScanner.nextLine();
		LocalDate newItemDate = LocalDate.parse("2020-01-01");

		for(LocalDate today = newItemDate;
			orderScanner.hasNextLine() || !backOrders.isEmpty();
			today = today.plusDays(1)) {
			String[] line = new String[0];
			if(orderScanner.hasNextLine()) {
				line = orderScanner.nextLine().split(",");
				newItemDate = LocalDate.parse(line[0]);
			} else {
				assert backOrders.peek() != null;
				if(backOrders.peek().getDateOrdered().plusDays(7)
							 .isBefore(today)) {
					checkBackOrders(sales, today, backOrders);
				}
			}
			checkBackOrders(sales, today, backOrders);
			while(newItemDate.isAfter(today)) {
				today = today.plusDays(1);
			}
			TransactionItem newItem = new TransactionItem();
			if(orderScanner.hasNextLine() || line.length > 0) {
				newItem.setFields(
				 newItemDate, line[1], line[2], line[3],
				 Integer.parseInt(line[4])
				);
				processOrder(newItem, sales, today, backOrders);
			}
		}

		StringBuilder sb = new StringBuilder();
		crud.update("CREATE TEMPORARY TABLE temp_table(" +
					"quantity int(16), product_id varchar(16));");
		sb.append("insert into temp_table(product_id,quantity)values");

		Iterator<Integer> idxItr = idxList.iterator();
		while(idxItr.hasNext()) {
			Integer idx = idxItr.next();
			String productId = indexMap.get(idx);
			Integer quantity = quantityMap.get(productId);

			sb.append(String.format("('%s',%d)%s",
			 productId, quantity, idxItr.hasNext() ? "," : ";"));
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

		crud.setWorkingTable("sales");

		/* Update the  database with the recorded  sales.*/
		sb.delete(0, sb.length());
		sb.append("insert into sales(" +
				  "customer_email,customer_location,date_ordered, date_accepted,product_id, " +
				  "quantity)" +
				  "values");

		for(Object[] sale: sales) {
			sb.append(crud.toValueTuple(sale))
			  .append(",");
		} // End for
		crud.update(sb.substring(0, sb.length() - 1) + ";");
	} // End processItems

	/**
	 * Attempts to process orders, and adds them to the list of back-orders if
	 * needed.
	 */
	private void processOrder(TransactionItem item,
							  LinkedList<Object[]> sales, LocalDate today,
							  Queue<TransactionItem> backOrders) {
		int check = quantityCheck(item);
		if(check == Crud.QUANTITY_SHORTAGE) {
			if(ChronoUnit.DAYS.between(today, item.getDateOrdered()) >= 7) {
				restock(item);
			} else if(!backOrders.contains(item)) {
				backOrders.add(item);
			}
		} else if(check >= 0) {
			/*otherwise, the sale was made, so add this info to the sales 
			table */
			item.setDateAccepted(today);
			sales.add(item.toArray(Crud.SALES));
		}// End if
	}

	/**
	 * Checks to make sure there is enough quantity to fulfill the order.
	 * if yes, @return is the quantity remaining, otherwise, a self-explanatory
	 * Crud constant, which is a negative number.
	 */
	private int quantityCheck(TransactionItem item) {
		String productId = item.getProductId();

		/* If the product is not something we actually sell, return 
		UNKNOWN_PRODUCT*/
		if(!quantityMap.containsKey(productId)) {
			item.setResultString(productId + " : unknown product id");
			System.out.println(productId + " : unknown product id");
			return Crud.UNKNOWN_PRODUCT;
		} // End if

		int requestQuantity = item.getRequestedQuantity();
		int inventoryQuantity = quantityMap.get(productId);

		/* If the requested quantity is too large, return QUANTITY_SHORTAGE*/
		if(requestQuantity > inventoryQuantity) {
			item.setResultString(crud.format(
			 "%s : %d - %d",
			 productId, inventoryQuantity,
			 inventoryQuantity + requestQuantity));
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

	private void restock(TransactionItem item) {
		String id = item.getProductId();
		Integer quantity = quantityMap.get(id);
		quantityMap.put(id, quantity + 500);
	}
}

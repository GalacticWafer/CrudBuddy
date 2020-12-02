package customerrelationsmanagement;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import static org.junit.Assert.*;

public class OrderProcessorTest {
	int[] buyerQuantities = new int[] {2, 1, 1, 2};
	private static Crud crud;
	private final DateTime date = DateTime.parse("2020-01-02");
	private final String location = "27934";
	String[] productIds = new String[] {
	 "3R8YXZCS820Y",
	 "UF91WPMROFIW",
	 "UMV8BFU4R3CW",
	 "KM75FS36T2Y4"
	};
	private Restoration rest;
	int[] sellerQuantities = new int[] {90, 400, 760, 25};
	OrderProcessorTest() {}

	@Test
	public void assertRestock() {
		
		Order sellerEventOrder =
		 new Order(date, EventType.isSale(true), location);
		sellerEventOrder.setEmail("some_supplier@somewhere.com");
		simulateOrder("little_inventory.csv", sellerQuantities,
		 sellerEventOrder);
	}
	
	@Test
	public void assertSold() {
		
		Order buyerEventOrder =
		 new Order(date, EventType.isSale(true), location);
		buyerEventOrder.setEmail("some_buyer@somewhere.com");
		simulateOrder("little_inventory.csv", buyerQuantities,
		 buyerEventOrder);
	}
	
	@NotNull private HashMap<String, Integer> getInts(int length)
	throws SQLException {
		
		String sql = "SELECT quantity FROM inventory WHERE product_id = ";
		HashMap<String, Integer> oldQuantities = new HashMap<>();
		for(int i = 0; i < length; i++) {
			String query = sql + "'" + productIds[i] + "'";
			oldQuantities.put(productIds[i], Integer
			 .parseInt(crud.getRecords(query)[0][0] + ""));
		}
		return oldQuantities;
	}
	
	public static void invoke(CredentialsTest credentials)
	throws SQLException, ClassNotFoundException {
		
		crud = credentials.getCrud();
		new CrudTest();
	}
	
	@Test
	void processOrder() {
		
	}
	
	@Test
	void runFileOrders()
	throws SQLException, IOException {
		
		new Restoration(new Credentials()
		 .getCrud(), "little_inventory.csv", "", true, null);
		OrderProcessor processor = new OrderProcessor(crud);
		HashMap<String, Integer> oldQuantities = getInts(productIds.length);
		processor.runFileOrders("little_order_test.csv");
		HashMap<String, Integer> newQuantities = getInts(productIds.length);
		Scanner scanner = new Scanner(new File("little_order_test.csv"));
		scanner.nextLine();
		
		while(scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(",");
			int requestedQuantity = Integer.parseInt(line[4]);
			String productId = line[3];
			int oldQuantity = oldQuantities.get(productId);
			int newQuantity = newQuantities.get(productId);
			System.out.println(
			 oldQuantity + " - " + requestedQuantity + " = " + newQuantity);
			assertEquals(oldQuantity, newQuantity + requestedQuantity);
		}
	}
	
	@Test
	void setOrder() {
		
	}
	
	private void simulateOrder
	 (String filePath, int[] eventQuantities, Order eventOrder) {
	/*	Restoration.rebuild(crud, filePath);
		SalesProcessor salesProcessor = new SalesProcessor(crud);
		salesProcessor.setCurrentOrder(eventOrder);
		crud.setWorkingTable("inventory");
		//int[] oldQuantities = getInts(eventQuantities.length);
		for(int i = 0; i < eventQuantities.length; i++) {
			eventOrder.addProduct(new Product(productIds[i], 
			eventQuantities[i]));
		}
		salesProcessor.runFileOrders("little_order_test.csv");
		salesProcessor.updateAndClose();
		crud.setWorkingTable("inventory");
		int[] newQuantities = getInts(eventQuantities.length);
		for(int i = 0; i < newQuantities.length; i++) {
			int oldNum = oldQuantities[i];
			int newNum = newQuantities[i];
			int change = (eventOrder.isSale() ? -1 : 1) * eventQuantities[i];
			System.out.println(newNum + " = (" + change + ") + " + oldNum);
			assertEquals(newNum, oldNum + change);
		}
	*/
	}
	
	@Test
	void testProcessOrder() {
		
	}
	
	@Test
	void testRunFileOrders() {
		
	}
	
	@Test
	void testSetOrder() {
		
	}
	
	@Test
	void testUpdateAndClose() {
		
	}
	
	@Test
	void updateAndClose() {
		
	}
}
/*
 
 * */


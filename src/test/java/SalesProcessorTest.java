import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Scanner;

import static org.junit.Assert.*;

public class SalesProcessorTest {
	private Restoration rest;
	
	SalesProcessorTest() throws SQLException, ClassNotFoundException {}
	int[] buyerQuantities = new int[] {2, 1, 1, 2};
	private final Crud crud = Credentials.databaseLogin();
	private final LocalDate date = LocalDate.parse("2020-01-02");
	private final String location = "27934";
	String[] productIds = new String[] {
	 "3R8YXZCS820Y",
	 "UF91WPMROFIW",
	 "UMV8BFU4R3CW",
	 "KM75FS36T2Y4"
	};
	
	int[] sellerQuantities = new int[] {90, 400, 760, 25};
	
	@Test
	public void assertRestock() throws SQLException, FileNotFoundException {
		Order sellerEventOrder =
		 new Order(date, false, location);
		sellerEventOrder.setEmail("some_supplier@somewhere.com");
		simulateOrder("little_inventory.csv", sellerQuantities,
		 sellerEventOrder);
	}
	
	@Test
	public void assertSold() throws SQLException, FileNotFoundException {
		Order buyerEventOrder =
		 new Order(date, true, location);
		buyerEventOrder.setEmail("some_buyer@somewhere.com");
		simulateOrder("little_inventory.csv", buyerQuantities,
		 buyerEventOrder);
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
	
	@NotNull private HashMap<String, Integer> getInts(int length)
	throws SQLException {
		String sql = "SELECT quantity FROM inventory WHERE product_id = ";
		HashMap<String, Integer> oldQuantities = new HashMap<>();
		for(int i = 0; i < length; i++) {
			String query = sql + "'" + productIds[i] + "'";
			oldQuantities.put(productIds[i], Integer.parseInt(crud.getRecords(query)[0][0] + ""));
		}
		return oldQuantities;
	}
	
	@Test
	void runFileOrders()
	throws SQLException, FileNotFoundException, ClassNotFoundException {
		new Restoration(Credentials.databaseLogin(), "little_inventory.csv", true);
		SalesProcessor processor = new SalesProcessor(crud);
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
			System.out.println(oldQuantity + " - " + requestedQuantity + " = " + newQuantity);
			assertEquals(oldQuantity, newQuantity + requestedQuantity);
		}
	}
	
	@Test
	void processOrder() {
	}
	
	@Test
	void setOrder() {
	}
	
	private void simulateOrder(String filePath, int[] eventQuantities,
							   Order eventOrder)
	throws FileNotFoundException, SQLException {
	/*	Restoration.rebuild(crud, filePath);
		SalesProcessor salesProcessor = new SalesProcessor(crud);
		salesProcessor.setOrder(eventOrder);
		crud.setWorkingTable("inventory");
		//int[] oldQuantities = getInts(eventQuantities.length);
		for(int i = 0; i < eventQuantities.length; i++) {
			eventOrder.add(new Product(productIds[i], eventQuantities[i]));
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
	void updateAndClose() {
	}
}
/*
2020-01-03,sneo@icloud.com,73417,3HZZHZA20TIR,2
2020-01-03,ryct@gmail.com,60583,9NPM2LBKYI9K,1
2020-01-02,saust@hotmail.com,38813,3R8YXZCS820Y,2
2020-01-02,saust@hotmail.com,38813,UF91WPMROFIW,2
2020-01-02,tuh@live.com,01436,UMV8BFU4R3CW,1
2020-01-02,tuh@live.com,01436,KM75FS36T2Y4,1
2020-01-02,yoi@live.com,27934,PSPEFL36KB3O,2
2020-01-02,yoi@live.com,27934,MFEP629NADRP,1
2020-01-02,yoi@live.com,27934,4QPNIX0SJ6RR,1
2020-01-02,yoi@live.com,27934,XFCS4PX4DRK6,2
2020-01-02,fluf@yahoo.com,24145,LSKQQL8O2MUB,3
2020-01-03,grai@hotmail.com,51373,HWERB4V73WVK,2
2020-01-03,grai@hotmail.com,51373,GJORFTY4T4HU,1
2020-01-03,grai@hotmail.com,51373,7EUUAQBNTYPW,1
2020-01-03,grai@hotmail.com,51373,PI3LA8P5R68Z,1
2020-01-03,waask@hotmail.com,64712,IPWFXMR8SGA2,3
2020-01-03,waask@hotmail.com,64712,UF91WPMROFIW,2
* */


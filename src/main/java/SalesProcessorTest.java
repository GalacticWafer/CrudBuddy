import org.junit.Test;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

class SalesProcessorTest {
	
	private HashMap<String, String> customers;

	@Test public void assertNewEmail() throws SQLException {
		
	}
	@Test public void assertNewCustomer(){
		
	}
	@Test public void assertOldCustomer(){
		
	}
	@Test public void assertNewDate(){
		
	}
	@Test public void assertNewProductId(){
		
	}
	
	// My test method
	HashMap<String, String> customersHashMapTest;
	Order itemTest;
	LinkedList<Object[]> salesTest;
	LocalDate dateTest = LocalDate.now();
	Queue<Order> backOrdersTest;
	@Test
	public void assertProcessOrderTest() 
	{
		// Put email & location into hash map
		customersHashMapTest.put(itemTest.getEmail(), itemTest.getLocation());
		
		//int checkTest = assertCheckTest();
		
		///*// If there's a shortage...
		//if(checkTest == Crud.QUANTITY_SHORTAGE) 
		//{
		//	
		//	// ...And more than 6 days between orders, restock  
		//	if(ChronoUnit.DAYS.between(dateTest, itemTest.getDateOrdered()) >= 7) {
		//		restock(itemTest); // How import restock?
		//	} 
		//	// ...And there's no  backorder, add to backorders
		//	else if(!backOrdersTest.contains(itemTest)) {
		//		backOrdersTest.add(itemTest);
		//	}
		//} 
		//else if(checkTest >= 0) {
		//	otherwise, the sale was made, so add this info to the sales 
		//	table 
		//	itemTest.setDateAccepted(dateTest);
		//	salesTest.add(itemTest.toArray(Crud.SALES));
		//}// End else if*/
		
	} // End assertProcessOrderTest
	
	@Test
	public void assertCheckTest() throws SQLException, ClassNotFoundException
	{
	/*	Crud crud = Credentials.databaseLogin();
		SalesProcessor processor = new SalesProcessor(crud);
		*//*Test if an unknown item being processed
		returns Crud.UNKNOWN_PRODUCT*//*
		TransactionItem item = new TransactionItem();
		//2020-01-01,yo@outlook.com,85637,2MA4NTR90D3T,1
		
		item.setFields(
		 LocalDate.parse("2020-01-01"),
		 "yo@outlook.com",
		 "85637", 
		 "AAAAAAAAAAA",
		 1);
		int checkTest = processor.quantityCheck(item);
		assertEquals(Crud.UNKNOWN_PRODUCT, checkTest);
		*//*Test if insufficient product
		returns Crud.QUANTITY_SHORTAGE*//*
		
		
		item.setFields(
		 LocalDate.parse("2020-01-01"),
		 "yo@outlook.com",
		 "85637", 
		 "ZRDATK9CSM2", 
		 Integer.MAX_VALUE); 
		checkTest = processor.quantityCheck(item);
		assertEquals(Crud.QUANTITY_SHORTAGE, checkTest); // change to Crud.QUANTITY_SHORTAGE
		
		
		item.setFields(
		 LocalDate.parse("2020-01-01"),
		 "yo@outlook.com",
		 "85637", 
		 "ZRDATK9CSM2", 
		 1);
		
		checkTest = processor.quantityCheck(item);
		 assertTrue(checkTest >= 0);	*/
		
	}
	
	public static void main(String[] args)
	throws SQLException, ClassNotFoundException, FileNotFoundException { 
		
		new SalesProcessorTest();
	}
}

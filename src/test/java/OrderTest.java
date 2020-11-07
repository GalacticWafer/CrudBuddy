import customerrelationsmanagement.Order;
import customerrelationsmanagement.Product;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
	private Order buyerOd;
	Order sellerOd = new Order(LocalDate.now(), true, "55555");
	
	@Test
	void add() {
		buyerOd = new Order(LocalDate.now(), true, "55555");
		buyerOd.addProduct(new Product("TEST1234", 500));
		assertEquals(1, buyerOd.size());
	}
	
	@Test
	void canProcess() {
		buyerOd = new Order(LocalDate.now(), true, "55555");
		buyerOd.addProduct(new Product("TEST1234", 500));
		assertTrue(buyerOd.canProcess());
	}
	
	@Test
	void cancel() {
		buyerOd = new Order(LocalDate.now(), true, "55555");
		buyerOd.setStatus(Order.CANCELLED);
		assertEquals(buyerOd.getStatus(), Order.CANCELLED);
	}
	
	@Test
	void getCustomerEmail() {
		buyerOd = new Order(LocalDate.now(), true, "55555");
		assertNull(buyerOd.getCustomerEmail());
		buyerOd.setEmail("whatever");
		assertEquals("whatever", buyerOd.getCustomerEmail());
	}
	
	@Test
	void getDateAccepted() {
		LocalDate now = LocalDate.now();
		buyerOd = new Order(now, true, "55555");
		assertNull(buyerOd.getDateAccepted());
		buyerOd.setDateAccepted(now);
		assertEquals(buyerOd.getDateAccepted(), now);
	}
	
	@Test
	void getDateOrdered() {
		buyerOd = new Order(LocalDate.now(), true, "55555");
		assertNotNull(buyerOd.getDateOrdered());
	}
	
	@Test
	void getId() {
		buyerOd = new Order(LocalDate.now(), true, "55555");
		assertNotNull(buyerOd.getId());
	}
	
	@Test
	void getItems() {
		
	}
	
	@Test
	void getLocation() {
	}
	
	@Test
	void getMessageText() {
	}
	
	@Test
	void getStatus() {
	}
	
	@Test
	void getStatusString() {
	}
	
	@Test
	void getSubject() {
	}
	
	@Test
	void hasNext() {
	}
	
	@Test
	void isProcessed() {
	}
	
	@Test
	void isSale() {
	}
	
	@Test
	void iterator() {
	}
	
	@Test
	void next() {
	}
	
	@Test
	void setCanProcess() {
	}
	
	@Test
	void setDateAccepted() {
	}
	
	@Test
	void setEmail() {
	}
	
	@Test
	void setStatus() {
	}
	
	@Test
	void setSubject() {
	}
	
	@Test
	void setText() {
	}
	
	@Test
	void testToString() {
	}
	
	@Test
	void toArray() {
	}
}

import customerrelationsmanagement.Order;
import customerrelationsmanagement.Product;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
	private Order buyerOd;
	Order sellerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
	
	@Test
	void add() {
		buyerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
		buyerOd.addProduct(new Product("TEST1234", 500));
		assertEquals(1, buyerOd.size());
	}
	
	@Test
	void canProcess() {
		buyerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
		buyerOd.addProduct(new Product("TEST1234", 500));
		assertTrue(buyerOd.canProcess());
	}
	
	@Test
	void cancel() {
		buyerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
		buyerOd.setStatus(Order.CANCELLED);
		assertEquals(buyerOd.getStatus(), Order.CANCELLED);
	}
	
	@Test
	void getCustomerEmail() {
		buyerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
		assertNull(buyerOd.getCustomerEmail());
		buyerOd.setEmail("whatever");
		assertEquals("whatever", buyerOd.getCustomerEmail());
	}
	
	@Test
	void getDateAccepted() {
		Timestamp now = Timestamp.valueOf(LocalDate.now() + "");
		buyerOd = new Order(now, true, "55555");
		assertNull(buyerOd.getTimeAccepted());
		buyerOd.setTimeAccepted(now);
		assertEquals(buyerOd.getTimeAccepted(), now);
	}
	
	@Test
	void getDateOrdered() {
		buyerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
		assertNotNull(buyerOd.getTimeOrdered());
	}
	
	@Test
	void getId() {
		buyerOd = new Order(Timestamp.valueOf(LocalDate.now().atStartOfDay()), true, "55555");
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

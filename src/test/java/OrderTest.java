//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//
//public class OrderTest {
//	@Test public void assertEmailFormat() {
//		String test = "END392ATO82E,400,true,38756";
//		String[] array = test.split(",");
//		Order ep = new Order(test,"1234567890","\"Malcolm Johnson\" <mjohn@somewhere.e>");
//		assertEquals(ep.getProductId(), array[0]);
//		assertEquals(ep.getQuantity() + "", array[1]);
//		assertEquals(ep.isSale() + "", array[2]);
//	}
//	
//	/*
//	* Todo: if the email represents a sale, make sure the productId is valid
//	*  if not, send a message "we do not currently carry this item.")      
//	*  If we do (and see if we have enough to sell of course),
//	*  update the correct row with (<current quantity> - ep.getQuantity())
//	*  using CRUDBuddy methods, then send order confirmed email back
//	* 
//	* Todo: if we don't have enough to sell, then send a message 
//	*  "sorry, we do not have enough in stock to fulfil this order"
//	* 
//	* Todo: if not a sale (restock) simply increase the quantity by the amount specified 
//	*  (<current quantity> + ep.getQuantity()) 
//	* */
//}

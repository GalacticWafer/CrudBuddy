package customerrelationsmanagement;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

public class Order {
	/**
	 * <code>Represents a collection of items purchased together, and the
	 * features of an order. </code>
	 */
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	public static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	public static final Pattern EMAIL_PATTERN =
	 Pattern.compile(".*<(?<email>\\w+@\\w+.\\w+)>");
	public static final int MAX_WAIT_TIME = 5;
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING =
	 CHAR_LOWER + CHAR_UPPER + NUMBER;
	public static final String[] ORDER_FILE_COLUMNS =
	 new String[] {
	  "date", "cust_email", "cust_location", "product_id",
	  "product_quantity"
	 };
	private String email;
	private EventType eventType;
	private final String location;
	private String messageText;
	public String orderId;
	private ArrayList<Product> products;
	private static final SecureRandom rand = new SecureRandom();
	private Status status;
	private String subject;
	private DateTime timeAccepted;
	private final DateTime timeOrdered;
	
	public Order(DateTime date,
				 EventType eventType, String location) {
		
		timeOrdered = date;
		this.eventType = eventType;
		this.location = location;
		this.orderId = generateId();
		status = Status.UNPROCESSED;
		products = new ArrayList<>();
	} // End constructor
	
	/** Add an item to this order. */
	public void addProduct(Product item) {
		
		products.add(item);
	} // End addProduct
	
	/** @return true if the order can be processed. */
	public boolean canProcess() {
		
		return status == Status.UNPROCESSED;
	} // End canProcess
	
	/**
	 * Cancel the order and return true if the order can be cancelled.
	 * Otherwise, return false.
	 */
	public boolean cancel() {
		
		DateTime dateOneHourBack = DateTime.now().minusHours(1);
		if(dateOneHourBack.isAfter(DateTime.parse(getTimeAccepted() + ""))) {
			return false;
		}
		setStatus(Status.CANCELLED);
		return true;
	}
	
	private String generateId() {
		
		int ORDER_ID_LENGTH = 10;
		StringBuilder sb = new StringBuilder(ORDER_ID_LENGTH);
		for(int i = 0; i < ORDER_ID_LENGTH; i++) {
			int randomIndex = rand.nextInt(DATA_FOR_RANDOM_STRING.length());
			char randomChar = DATA_FOR_RANDOM_STRING.charAt(randomIndex);
			sb.append((randomChar + "").toUpperCase());
		}
		return sb.toString();
	} // End generateId
	
	public String getCustomerEmail() {return email;} // End getCustomerEmail
	
	/** @return the unique order ID for all items in this Order. */
	public String getId() {return orderId;} // End getId
	// getTimeOrdered
	
	public String getLocation() {return location;} // End getLocation
	
	public String getMessageText() { return messageText; } // End
	
	public String getResponseSubject() { return subject; } // End
	
	public Status getStatus() {
		
		return status;
	} // End getStatus
	// getMessageText
	
	@NotNull public String getStatusString() {
		
		return isCancelled() ? "Cancelled" :
		 isProcessed() ? "Processed" :
		  canProcess() ? "Being Processed" :
		   "Cannot Be Processed";
	} // End getStatusString
	
	/** @return null if the order has not been processed or accepted. */
	public DateTime getTimeAccepted() {
		
		return timeAccepted;
	} // End getTimeAccepted
	
	public DateTime getTimeOrdered() {return timeOrdered;} // End
	// getResponseSubject
	
	private boolean isCancelled() { return status == Status.CANCELLED; } //
	// isCancelled
	
	public boolean isProcessed() {
		return status == Status.PROCESSED;
	} // End isProcessed
	
	public EventType eventType() {return eventType;} // End eventType
	
	public Iterator<Product> productIterator() {
		
		return products.iterator();
	} // End productIterator
	
	public void setEmail(String email) {this.email = email;} // End setEmail
	
	public void setStatus(Status status) {
		switch(status){
			case INVALID, CANCELLED: switch(this.status) {
				case INVALID: this.status = status; return;
			}
		}
		switch(this.status) {
			case INVALID, CANCELLED -> {return;}
			default -> {
				if(status.compareTo(this.status) > 0) {
					this.status = status;
				}
			}
		}
	} // End setEmail
	
	public void setSubject(String subject) {
		
		this.subject = subject;
	} // End setSubject
	
	public void setText(String s) {
		
		this.messageText = s;
	} // End setText
	
	public void setTimeAccepted(DateTime today) {
		this.timeAccepted = today;
	} // End setTimeAccepted
	
	public void setTimeAccepted() {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(getTimeOrdered().toDate());
		cal
		 .add(Calendar.DAY_OF_WEEK, new Random().nextInt(Order.MAX_WAIT_TIME));
		setTimeAccepted(new DateTime(cal.getTime().getTime()));
	} // End setTimeAccepted
	
	public int size() {
		
		return products.size();
	} // End size
	
	/**
	 * @return the Object array to use as an new row in the SQL sales table
	 *  .
	 */
	public ArrayList<Object[]> toArray() {
		
		if(timeAccepted == null) {return null;}
		ArrayList<Object[]> array = new ArrayList<>();
		for(Iterator<Product> it = productIterator(); it.hasNext(); ) {
			Product p = it.next();
			array
			 .add(new Object[] {
			  getId(),                 // "order_id",
			  getCustomerEmail(),      // "cust_email",
			  getLocation(),           // "cust_location",
			  p.getId(),               // "product_id",
			  p.getQuantity(),         // "product_quantity",
			  dtf.print(timeOrdered),  // "date_ordered",
			  dtf.print(timeAccepted), // "date_accepted",
			  status.toString(),       // "status",
			 });
		} // End for
		return array;
	} // End toString
	
	@Override public String toString() {
		
		return
		 getStatusString() + ',' +
		 
		 (eventType == EventType.SELLER ? "sale"
		  : "restock") + ',' +
		 
		 "Id: " + orderId + ',' +
		 email + ',' +
		 "dateOrdered: " + timeOrdered +
		 "dateAccepted: " + timeAccepted +
		 "location: " + location;
	} // End toString
} // Ond class Order
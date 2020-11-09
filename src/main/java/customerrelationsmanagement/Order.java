package customerrelationsmanagement;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class Order {
	/**
	 * <code>Represents a collection of items purchased together, and the
	 * features of an order. </code>
	 */
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	public static final SimpleDateFormat DATE_FORMAT =
	 new SimpleDateFormat("yyyy-MM-dd");
	public static final Pattern EMAIL_PATTERN =
	 Pattern.compile(".*<(?<email>\\w+@\\w+.\\w+)>");
	public static final int INVALID = -2, CANCELLED = -1, UNPROCESSED = 0,
	 QUANTITY_SHORTAGE = 1,
	 PROCESSED = 2, SUGGESTED_EMAIL = 3, FULFILLED = 4;
	public static final int MAX_WAIT_TIME = 5;
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING =
	 CHAR_LOWER + CHAR_UPPER + NUMBER;
	
	public static final String[] SALES_COLUMNS = new String[] {
	 "order_id",
	 "cust_email",
	 "cust_location",
	 "product_id",
	 "product_quantity",
	 "date_ordered",
	 "date_accepted",
	 "status",
	 };
	
	private Timestamp dateAccepted;
	private final Timestamp dateOrdered;
	private String email;
	private boolean isSale;
	private ArrayList<Product> products;
	private final String location;
	private String messageText;
	public String orderId;
	private static final SecureRandom rand = new SecureRandom();
	private int status;
	private String subject;
	
	public Order(Timestamp date,
				 boolean isSale, String location) {
		dateOrdered = date;
		this.isSale = isSale;
		this.location = location;
		this.orderId = generateId();
		status = UNPROCESSED;
		products = new ArrayList<>();
	} // End constructor
	
	/** Add an item to this order. */
	public void addProduct(Product item) {
		products.add(item);
	} // End addProduct
	
	/** @return true if the order can be processed. */
	public boolean canProcess() {
		return status == UNPROCESSED;
	} // End canProcess
	
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
	
	/** @return null if the order has not been processed or accepted. */
	public Timestamp getDateAccepted() {
		return dateAccepted;
	} // End getDateAccepted
	
	public Timestamp getDateOrdered() {return dateOrdered;} // End getDateOrdered
	
	/** @return the unique order ID for all items in this Order. */
	public String getId() {return orderId;} // End getId
	
	public String getLocation() {return location;} // End getLocation
	
	public String getMessageText() { return messageText; } // End getMessageText 
	
	public int getStatus() {
		return status;
	} // End getStatus
	
	@NotNull public String getStatusString() {
		return isCancelled() ? "Cancelled" :
		 isProcessed() ? "Processed" :
		  canProcess() ? "Being Processed" :
		   "Cannot Be Processed";
	} // End getStatusString
	
	public String getResponseSubject() { return subject; } // End getResponseSubject
	
	private boolean isCancelled() { return status == CANCELLED; } // isCancelled
	
	public boolean isProcessed() { return status == PROCESSED; } // End isProcessed
	
	public boolean isSale() {return isSale;} // End isSale
	
	public Iterator<Product> productIterator() {
		return products.iterator();
	} // End productIterator
	
	public void setDateAccepted(Timestamp today) { this.dateAccepted = today; } // End setDateAccepted
	public void setDateAccepted() { 
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDateOrdered());
		cal.add(Calendar.DAY_OF_WEEK, new Random().nextInt(Order.MAX_WAIT_TIME));
		setDateAccepted(new Timestamp(cal.getTime().getTime()));
	} // End setDateAccepted
	
	public void setEmail(String email) {this.email = email;} // End setEmail
	
	public void setStatus(int status) {
		if(status == INVALID || status == CANCELLED) {
			if(this.status == 0) {
				this.status = status;
				return;
			}
		}
		//JOptionPane.showMessageDialog(null,getStatus + "==" + this.getStatus
		// + "-> " +(getStatus == INVALID));
		if(this.status == INVALID || this.status == CANCELLED) {
			return;
		}
		this.status = Math.max(status, this.status);
	} // End setEmail
	
	public void setSubject(String subject) {
		this.subject = subject;
	} // End setSubject
	
	public void setText(String s) {
		this.messageText = s;
	} // End setText 
	
	public int size() {
		return products.size();
	} // End size
	
	/** @return the Object array to use as an new row in the SQL sales table
	 * . */
	public ArrayList<Object[]> toArray() {
		ArrayList<Object[]> array = new ArrayList<>();
		for(Iterator<Product> it = productIterator(); it.hasNext();) {
			Product p = it.next();
			array.add(new Object[] {  // public static final String[] SALES_COLUMNS =
			 getId(),                 // "order_id",
			 getCustomerEmail(),      // "cust_email",
			 getLocation(),           // "cust_location",
			 p.getId(),               // "product_id",
			 p.getQuantity(),         // "product_quantity",
			 dateOrdered.toString(),  // "date_ordered",
			 dateAccepted.toString(), // "date_accepted",
			 status,                  // "status",
			 });
		} // End for
		return array;
	} // End toArray
	
	@Override public String toString() {
		return
		 getStatusString() + ',' +
		 
		 (isSale ? "sale"
		  : "restock") + ',' +
		 
		 "Id: " + orderId + ',' +
		 email + ',' +
		 "dateOrdered: " + dateOrdered +
		 "dateAccepted: " + dateAccepted +
		 "location: " + location;
	} // End toString
} // Ond class Order

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.security.SecureRandom;
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
	 PROCESSED = 2, FULFILLED = 4;
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING =
	 CHAR_LOWER + CHAR_UPPER + NUMBER;
	public static final String[] SALES_COLUMNS = new String[] {
	 "order_id",
	 "date_ordered",
	 "date_accepted",
	 "cust_email",
	 "cust_location",
	 "product_id",
	 "product_quantity",
	 "status",
	 };
	private Boolean canProcess;
	private Product current;
	private LocalDate dateAccepted;
	private LocalDate dateOrdered;
	private String email;
	private boolean isSale;
	private ArrayList<Product> products;
	private Iterator<Product> iterator;
	private Iterator<Product> itr;
	private final String location;
	private String messageText;
	public String orderId;
	private static final SecureRandom rand = new SecureRandom();
	private int status;
	private String subject;
	
	public Order(LocalDate date,
				 boolean isSale, String location) {
		dateOrdered = date;
		this.isSale = isSale;
		this.location = location;
		this.orderId = generateId();
		canProcess = null;
		status = UNPROCESSED;
		products = new ArrayList<>();
	}
	
	/** Add an item to this order. */
	public void add(Product item) {
		products.add(item);
	}
	
	/** @return true if the order can be processed. */
	public boolean canProcess() {
		return status == UNPROCESSED;
	}
	
	public void cancel() {
		status = CANCELLED;
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
	}
	
	public String getCustomerEmail() {return email;}
	
	/** @return null if the order has not been processed or accepted. */
	public LocalDate getDateAccepted() {
		return dateAccepted;
	}
	
	public LocalDate getDateOrdered() {return dateOrdered;}
	
	/** @return the unique order ID for all items in this Order. */
	public String getId() {return orderId;}
	
	public ArrayList<Product> getProducts() { return products; }
	
	public String getLocation() {return location;}
	
	public String getMessageText() { return messageText; }
	
	public int getStatus() {
		return status;
	}
	
	@NotNull public String getStatusString() {
		return isCancelled() ? "Cancelled" :
		 isProcessed() ? "Processed" :
		  canProcess() ? "Being Processed" :
		   "Cannot Be Processed";
	}
	
	public String getSubject() { return subject; }
	
	private boolean isCancelled() { return status == CANCELLED; }
	
	public boolean isProcessed() { return status == PROCESSED; }
	
	public boolean isSale() {return isSale;}
	
	public Iterator<Product> iterator() {
		return products.iterator();
	}
	
	public void setCanProcess(boolean bool) { canProcess = bool; }
	
	public void setDateAccepted(LocalDate today) { this.dateAccepted = today; }
	
	public void setEmail(String email) {this.email = email;}
	
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
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setText(String s) {
		this.messageText = s;
	}
	
	public int size() {
		return products.size();
	}
	
	/** @return the Object array to use as an new row in the SQL sales table
	 * . */
	public ArrayList<Object[]> toArray() {
		ArrayList<Object[]> array = new ArrayList<>();
		ArrayList<Object> test = new ArrayList<>();
		for(Iterator<Product> it = iterator(); it.hasNext();) {
			Product p = it.next();
			test.add(getId());
			test.add(dateOrdered.plusDays(1).toString());
			test.add(dateAccepted.plusDays(1).toString());
			test.add(getCustomerEmail());
			test.add(getLocation());
			test.add(p.getId());
			test.add(p.getQuantity());
			test.add(status);
			array.add(new Object[] {
			 getId(),
			 dateOrdered.plusDays(1).toString(),
			 dateAccepted.plusDays(1).toString(),
			 getCustomerEmail(),
			 getLocation(),
			 p.getId(),
			 p.getQuantity(),
			 status,
			 });
		}
		return array;
	}
	
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
	}
}

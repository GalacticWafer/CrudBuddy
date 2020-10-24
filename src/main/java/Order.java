import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class Order {
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	public static final SimpleDateFormat DATE_FORMAT =
	 new SimpleDateFormat("yyyy-MM-dd");
	public static final Pattern EMAIL_PATTERN =
	 Pattern.compile(".*<(?<email>\\w+@\\w+.\\w+)>");
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING =
	 CHAR_LOWER + CHAR_UPPER + NUMBER;

	private boolean canProcess;
	private LocalDate dateAccepted;
	private LocalDate dateOrdered;
	private final String email;
	private boolean isSale;
	private ArrayList<TransactionItem> items;
	private final String location;
	public String orderId;
	private static final SecureRandom rand = new SecureRandom();
	
	public Order(LocalDate date,
				 String email, boolean isSale, String location,
				 String orderId, ArrayList<TransactionItem> items) {
		dateOrdered = date;
		this.items = items;
		this.email = email;
		this.isSale = isSale;
		this.location = location;
		this.orderId = orderId;
	}

	public void add(TransactionItem item) {
		items.add(item);
	}
	
	public boolean canProcess() {
		return canProcess;
	}
	
	static String generateId() {
		int ORDER_ID_LENGTH = 10;
		StringBuilder sb = new StringBuilder(ORDER_ID_LENGTH);
		for(int i = 0; i < ORDER_ID_LENGTH; i++) {
			int randomIndex = rand.nextInt(DATA_FOR_RANDOM_STRING.length());
			char randomChar = DATA_FOR_RANDOM_STRING.charAt(randomIndex);
			sb.append((randomChar + "").toUpperCase());
		}
		return sb.toString();
	}
	
	
	public LocalDate getDateAccepted() {
		return dateAccepted;
	}
	
	public LocalDate getDateOrdered() {return dateOrdered;}
	
	public String getEmail() {return email;}
	
	public ArrayList<TransactionItem> getItems() {
		return items;
	}
	
	public String getLocation() {return location;}
	
	public Object getMatchValue(int table) {
		switch(table) {
			case Crud.SALES: { return getOrderId(); }
			case Crud.CUSTOMERS: { return getEmail(); }
		}
		throw new InputMismatchException();
	}
	
	public String getOrderId() {return orderId;}
	
	public String getSqlDate(LocalDate localDate) {
		return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
	
	public boolean isSale() {return isSale;}
	
	public Iterator<TransactionItem> iterator() {
		return items.iterator();
	}
	
	public void setDateAccepted(LocalDate today) {
		this.dateAccepted = today;
	}
	
	public void setItems(ArrayList<TransactionItem> items) {
		this.items = items;
	}
	
	public static final String[] BACK_ORDER_COLUMNS = new String[] {
	 "order_id", 
	 "date_ordered", 
	 "cust_email", 
	 "cust_location", 
	 "product_id", 
	 "product_quantity"
	};
	public ArrayList<Object[]> toBackOrderArray() {
		ArrayList<Object[]> array = new ArrayList<>();
		for(TransactionItem item: items) {
			array.add(new Object[] {
			 getOrderId(),
			 getSqlDate(dateOrdered.plusDays(1)),
			 getEmail(),
			 getLocation(),
			 item.getProductId(),
			 item.getQuantity(),
			 });
		}
		return array;
	}
	
	
	public static final String[] SALES_COLUMNS = new String[] {
	 "order_id", 
	 "date_ordered", 
	 "date_accepted", 
	 "cust_email",
	 "cust_location",
	 "product_id", 
	 "product_quantity"
	};
	public ArrayList<Object[]> toSalesArray() {
		ArrayList<Object[]> array = new ArrayList<>();
		for(TransactionItem item: items) {
			array.add(new Object[] {
			 getOrderId(),
			 getSqlDate(dateOrdered.plusDays(1)),
			 getSqlDate(dateAccepted.plusDays(1)),
			 getEmail(),
			 getLocation(),
			 item.getProductId(),
			 item.getQuantity(),
			});
		}
		return array;
	}
}

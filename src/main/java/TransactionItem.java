import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionItem {
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	public static final SimpleDateFormat DATE_FORMAT =
	 new SimpleDateFormat("yyyy-MM-dd");
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING =
	 CHAR_LOWER + CHAR_UPPER + NUMBER;
	private static int ORDER_ID_LENGTH = 10;
	private static final Pattern PATTERN = Pattern.compile(
	 "\"(?<lastName>\\w+)," +
	 "\\s(?<firstName>\\w+)\"\\s" +
	 "<(?<email>\\w+@\\w+.\\w+)>");
	private int currentQuantity;
	private LocalDate dateAccepted;
	private LocalDate dateOrdered;
	private String email;
	private String firstName;
	private boolean isSale;
	private String lastName;
	private String location;
	private static Matcher m;
	public String orderId;
	private String productId;
	private int quantity;
	private static SecureRandom rand = new SecureRandom();
	private String resultString;
	
	public TransactionItem(String content, String orderId, String senderInfo) {
		String[] s = content.split(",");
		productId = s[0].trim();
		quantity = Integer.parseInt(s[1].trim());
		isSale = Boolean.parseBoolean(s[2].trim());
		setCustomerInfo(senderInfo);
		this.location = s[3].trim();
		this.orderId = orderId;
	}
	
	public TransactionItem() {}
	
	public static String generateId() {
		if(ORDER_ID_LENGTH < 1) {
			throw new IllegalArgumentException();
		}
		StringBuilder sb = new StringBuilder(ORDER_ID_LENGTH);
		for(int i = 0; i < ORDER_ID_LENGTH; i++) {
			int randomIndex = rand.nextInt(DATA_FOR_RANDOM_STRING.length());
			char randomChar = DATA_FOR_RANDOM_STRING.charAt(randomIndex);
			sb.append((randomChar + "").toUpperCase());
		}
		return sb.toString();
	}
	
	public int getCurrentQuantity() {return currentQuantity;}
	
	public String[] getCustomerInfo() {
		return new String[] {
		 firstName,
		 lastName, email
		};
	}
	
	public LocalDate getDateAccepted() {
		return dateAccepted;
	}
	
	public LocalDate getDateOrdered() {return dateOrdered;}
	
	public String getEmail() {return email;}
	
	public String getFirstName() {return firstName;}
	
	public String getLastName() {return lastName;}
	
	public String getLocation() {return location;}
	
	public Object getMatchValue(int table) {
		switch(table) {
			case Crud.SALES: { return getOrderId(); }
			case Crud.CUSTOMERS: { return getEmail(); }
		}
		throw new InputMismatchException();
	}
	
	public String getOrderId() {return orderId;}
	
	public String getProductId() {return productId;}
	
	public int getQuantity() {return quantity;}
	
	public int getRequestedQuantity() {
		return quantity;
	}
	
	public String getResultString() {return resultString;}
	
	public String getSalesTuple() {
		return String.format("(%s,'%s','%s','%s','%s','%s')",
		 getQuantity(), getSqlDate(getDateAccepted()),
		 Arrays.toString(getCustomerInfo()),
		 getLocation(), getProductId(), getOrderId());
	}
	
	public String getSqlDate(LocalDate localDate) {
		return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
	
	public boolean isSale() {return isSale;}
	
	public void setCurrentQuantity(int currentQuantity) {
		this.currentQuantity = currentQuantity;
	}
	
	public void setCustomerInfo(String customerInfo) {
		Matcher m = PATTERN.matcher(customerInfo);
		if(m.find()) {
			firstName = m.group("firstName");
			lastName = m.group("lastName");
			email = m.group("email");
		}
	}
	
	public void setDate(Date sentDate) {
		this.dateOrdered = LocalDate.parse(DATE_FORMAT.format(sentDate));
	}
	
	public void setDateAccepted(LocalDate today) {
		this.dateAccepted = today;
	}
	
	public void setDateOrdered(LocalDate dateOrdered) {
		this.dateOrdered =
		 dateOrdered;
	}
	
	public void setEmail(String email) {this.email = email;}
	
	public void setFields(Object[] customer, String productId, int quantity,
						  LocalDate date,
						  boolean b) {
		setEmail((String)customer[0]);
		setFirstName((String)customer[1]);
		setLastName((String)customer[2]);
		setLocation((String)customer[3]);
		setQuantity(quantity);
		setDateOrdered(date);
		setProductId(productId);
		isSale = b;
		if(orderId == null) {
			setOrderId(generateId());
		}
	}
	
	public void setFields(LocalDate date, String cust_email,
						  String cust_location, String productId,
						  int quantity) {
		this.dateOrdered = date;
		this.email = cust_email;
		this.location = cust_location;
		this.productId = productId;
		this.quantity = quantity;
		if(orderId == null) { 
			setOrderId(generateId());
		}
	}
	
	public void setFirstName(String firstName) {this.firstName = firstName;}
	
	public void setLastName(String lastName) {this.lastName = lastName;}
	
	public void setLocation(String location) {this.location = location;}
	
	public void setOrderId(String d) {this.orderId = d;}
	
	public void setProductId(String productId) {this.productId = productId;}
	
	public void setQuantity(int quantity) {this.quantity = quantity;}
	
	public void setResultString(String resultString) {
		this.resultString = resultString;
	}
	
	public void setSale(boolean sale) {isSale = sale;}
	
	public Object[] toArray(int table) {
		switch(table) {
			case Crud.SALES -> { return toSalesArray(); }
			case Crud.CUSTOMERS -> { return toCustomerArray(); }
		}
		throw new InputMismatchException();
	}
	
	private Object[] toCustomerArray() {
		return new Object[] {
		 getEmail(), 
		 getFirstName(), 
		 getLastName(),
		 getLocation(),
		};
	}

	private Object[] toSalesArray() {
		if(getOrderId() == null) { setOrderId(generateId()); }
		return new Object[] {
		 getProductId(),
		 getOrderId(),
		 getCurrentQuantity(),
		 getEmail(),
		 getSqlDate(dateOrdered.plusDays(1)),
		 getSqlDate(dateAccepted.plusDays(1)),
		 getLocation()
		 };
	}
	@Override public String toString() {
		return "TransactionItem{" +
			   "\n\tproductId='" + productId + '\'' +
			   "\n\tdateOrdered=" + dateOrdered +
			   "\n\tdateAccepted=" + dateAccepted +
			   "\n\tquantity=" + quantity +
			   "\n}";
	}
}

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Order {
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final Pattern PATTERN = Pattern.compile(
	 "\"(?<lastName>\\w+),\\s(?<firstName>\\w+)\"\\s<(?<email>\\w+@\\w+.\\w+)>");
	private static int ORDER_ID_LENGTH = 10;
	private static SecureRandom rand = new SecureRandom();
	private static Matcher m;
	public String d;
	private String productId;
	private String email;
	private String firstName;
	private String lastName;
	private String location;
	private Date date;
	private int quantity;
	private boolean isSale;
	private String resultString;
	private int currentQuantity;
	
	public Order(String content, String d, String senderInfo) {
		String[] s = content.split(",");
		productId = s[0].trim();
		quantity = Integer.parseInt(s[1].trim());
		isSale = Boolean.parseBoolean(s[2].trim());
		setCustomerInfo(senderInfo);
		this.location = s[3].trim();
		this.d = d;
	}
	
	public Order() {}
	
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
	
	public String[] getCustomerInfo() {return new String[] {firstName, lastName, email};}
	
	public int getCurrentQuantity() {return currentQuantity;}
	
	public Date getDate() {return date;}
	
	public String getEmail() {return email;}
	
	public String getFirstName() {return firstName;}
	
	public String getLastName() {return lastName;}
	
	public String getLocation() {return location;}
	
	public String getOrderId() {return d;}
	
	public String getProductId() {return productId;}
	
	public int getQuantity() {return quantity;}
	
	public String getResultString() {return resultString;}
	
	public String getSalesTuple() {
		return String.format("(%s,'%s','%s','%s','%s','%s')",
		 getQuantity(), getSqlDate(), Arrays.toString(getCustomerInfo()),
		 getLocation(), getProductId(), getOrderId());
	}
	
	public String getSqlDate() {return DATE_FORMAT.format(date);}
	
	public boolean isSale() {return isSale;}
	
	public void setCurrentQuantity(int currentQuantity) {this.currentQuantity = currentQuantity;}
	
	public void setCustomerInfo(String customerInfo) {
		Matcher m = PATTERN.matcher(customerInfo);
		if(m.find()) {
			firstName = m.group("firstName");
			lastName = m.group("lastName");
			email = m.group("email");
		}
	}
	
	public void setDate(Date date) {this.date = date;}
	
	public void setEmail(String email) {this.email = email;}
	
	public void setFields(Object[] customer, String productId, int quantity, Date date,
						  boolean b) {
		setEmail((String)customer[0]);
		setFirstName((String)customer[1]);
		setLastName((String)customer[2]);
		setLocation((String)customer[3]);
		setQuantity(quantity);
		setDate(date);
		setProductId(productId);
		isSale = b;
		if (getOrderId() == null){
			setOrderId(generateId());
		}

	}
	
	public void setFirstName(String firstName) {this.firstName = firstName;}
	
	public void setLastName(String lastName) {this.lastName = lastName;}
	
	public void setLocation(String location) {this.location = location;}
	
	public void setOrderId(String d) {this.d = d;}
	
	public void setProductId(String productId) {this.productId = productId;}
	
	public void setQuantity(int quantity) {this.quantity = quantity;}
	
	public void setResultString(String resultString) {this.resultString = resultString;}
	
	public void setSale(boolean sale) {isSale = sale;}
	
	public Object[] toCustomerArray() {
		return new Object[] {getEmail(), getFirstName(), getLastName(), getLocation()};
	}
	
	public Object[] toOrderArray() {
		return new Object[] {
		 getEmail(), getLocation(), getDate(), getOrderId(), getProductId(), getQuantity()
		};
	}
	
	public Object[] toArray(int table) {
		switch(table){
			case Crud.SALES: { return toOrderArray(); }
			case Crud.CUSTOMERS: { return toCustomerArray(); }
		}
		throw new InputMismatchException();
	}
	public Object getMatchValue(int table) {
		switch(table){
			case Crud.SALES: { return getOrderId(); }
			case Crud.CUSTOMERS: { return getEmail(); }
		}
		throw new InputMismatchException();
	}
}

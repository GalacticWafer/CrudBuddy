import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Order {
	public String d;
	private String productId;
	private String email;
	private String location;
	private Date date;
	private int quantity;
	private boolean isSale;
	private static int ORDER_ID_LENGTH = 10;
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	private static final String NUMBER = "0123456789";
	private static final String DATA_FOR_RANDOM_STRING =
	 CHAR_LOWER + CHAR_UPPER + NUMBER;
	private static SecureRandom rand = new SecureRandom();
	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	public Order(String content, String d) {
		
		String[] s = content.split(",");
		productId = s[0].trim();
		quantity = Integer.parseInt(s[1].trim());
		isSale = Boolean.parseBoolean(s[2].trim());
		this.location = s[3].trim();
		this.d = d;
	}
	
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
	
	public String getProductId() {
		
		return productId;
	}
	
	public void setProductId(String productId) {
		
		this.productId = productId;
	}
	
	public int getQuantity() {
		
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		
		this.quantity = quantity;
	}
	
	public boolean isSale() {
		
		return isSale;
	}
	
	public void setSale(boolean sale) {
		
		isSale = sale;
	}
	
	public String getLocation() {
		
		return location;
	}
	
	public void setLocation(String location) {
		
		this.location = location;
	}
	
	public Date getDate() {
		
		return date;
	}
	
	public String getSqlDate() {
		
		return DATE_FORMAT.format(date);
	}
	
	public void setDate(Date date) {
		
		this.date = date;
	}
	
	public String getEmail() {
		
		return email;
	}
	
	public String getOrderId() {
		
		return d;
	}
	
	public void setOrderId(String d) {
		
		this.d = d;
	}
	
	public void setEmail(String email) {
		
		String pattern = "\"\\w+,\\s\\w+\"\\s<(\\w+@\\w+.\\w+)>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(email);
		if(m.find()) {
			this.email = m.group(1);
		}
	}
	
	public String getSalesTuple() {
		
		return "(" + getQuantity() + ",'" + getSqlDate() + "','" + getEmail() + "','" +
			   getLocation() + "','" + getProductId() + "','" + getOrderId() + "')";
	}
}

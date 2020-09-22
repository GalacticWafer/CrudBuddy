import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailOrder {
	private String productId;
	private String email;
	private String location;
	private Date date;
	private int quantity;
	private boolean isSale;
	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	public EmailOrder(String content) {
		
		String[] s = content.split(",");
		productId = s[0].trim();
		quantity = Integer.parseInt(s[1].trim());
		isSale = Boolean.parseBoolean(s[2].trim());
		this.location = s[3].trim();
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
	
	public void setEmail(String email) {
		
		String pattern = "\"\\w+,\\s\\w+\"\\s<(\\w+@\\w+.\\w+)>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(email);
		if(m.find()) {
			this.email = m.group(1);
		}
	}
}

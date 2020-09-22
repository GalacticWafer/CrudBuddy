import java.util.Scanner;

public class EmailParser {
	private String productId;
	private int quantity;
	private boolean isSale;
	
	public EmailParser(String content) {
		String[] s = content.split(",");
		productId = s[0];
		quantity = Integer.parseInt(s[1]);
		isSale = Boolean.parseBoolean(s[2]);
	}
	
	public String getProductId()
	{
		return productId;
	}
	
	public void setProductId(String productId)
	{
		this.productId = productId;
	}
	
	public int getQuantity()
	{
		return quantity;
	}
	
	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}
	
	public boolean isSale()
	{
		return isSale;
	}
	
	public void setSale(boolean sale)
	{
		isSale = sale;
	}
}

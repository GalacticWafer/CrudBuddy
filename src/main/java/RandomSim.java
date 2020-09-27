import javax.mail.MessagingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

class RandomSim extends Random {
	private final Crud crud;
	private final String[] productIds;
	private Scanner emails;
	private final Scanner dates;
	public static final String emailFileName = "emails.csv";
	public static final String dateFileName = "dates.csv";
	
	public RandomSim(Crud crud) throws SQLException, FileNotFoundException {
		this.crud = crud;
		ResultSet rs;
		rs = crud.query("select product_id from inventory;");
		crud.setWorkingTable("inventory");
		productIds = new String[crud.size()];
		while(true) {
			for(int i = 0; rs.next(); i++) {
				productIds[i] = rs.getString(1);
			}
			break;
		}
		emails = new Scanner(new File(emailFileName));
		dates = new Scanner(new File(dateFileName));
	}
	
	public void simulateOrders() throws SQLException, FileNotFoundException, MessagingException {
		crud.setWorkingTable("customers");
		for(int i = 0; i < 2000000; i++) {
			Order order = new Order();
			order.setFields(nextCustomer(), nextProduct(), nextQuantity(), nextDate(), true);
			if(!(crud.isProcessableOrder(order) >= 0)) {
				crud.restock(order.getProductId());
			} else {
				crud.setQuantityFromOrder(order);
			}
			crud.insertFromOrder(order, Crud.CUSTOMERS);
		}
	}
	
	private int nextQuantity() {
		return Math.abs((int)(nextGaussian() * 500)) + 1;
	}
	
	private Date nextDate() {
		return new Date(dates.nextLine());
	}
	
	@org.jetbrains.annotations.NotNull
	private Object[] nextCustomer()
	throws SQLException, FileNotFoundException {
		int chance = (int)(Math.round(nextGaussian() * (Integer.MAX_VALUE - 1)) % 10);
		if(firstCustomer || chance > 3 || chance < - 3) {
			firstCustomer = false;
			return nextNewCustomer();
		} else {
			return nextOldCustomer();
		}
	}
	
	private Object[] nextNewCustomer() throws FileNotFoundException {
		String email = nextEmail();
		String parts = (email.substring(0, email.indexOf("@")));
		String firstName = parts.substring(0, parts.length() / 2);
		String lastName = parts.substring(parts.length() / 2 + 1);
		String location = Math.abs((nextInt() + 1000000) % 100000) + "";
		return new Object[] {email, firstName, lastName, location};
	}
	
	// use CRUDBuddy to get a randomly chosen existing entry from sales table
	private Object[] nextOldCustomer() throws SQLException, FileNotFoundException {
		crud.setWorkingTable("customers");
		int size = crud.size();
		if(size == 0) { return nextNewCustomer(); }
		ResultSet rs = crud.query("select * from customers where idx = " + size);
		rs.next();
		try {
		return new Object[] { rs.getString(2), rs.getString(3), rs.getString(4),rs.getString(5)};
		} catch(Exception e){
			return nextNewCustomer();
		}
	}
	
	private String nextProduct() throws SQLException {
		crud.setWorkingTable("inventory");
		return productIds[nextInt(crud.size())];
	}
	
	private String nextEmail() throws FileNotFoundException {
		if(! emails.hasNextLine()) {
			emails = new Scanner(new File("emails.csv"));
		}
		return emails.nextLine();
	}
	
	private boolean firstCustomer = true;
}

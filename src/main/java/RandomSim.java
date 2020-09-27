import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

class RandomSim extends Random {
	private final Crud crud;
	private String[] productIds;
	private int size;
	private Scanner emailScanner;
	private Scanner dates;
	public static final String emailFileName = "emails.csv";
	public static final String dateFileName = "dates.csv";
	
	public RandomSim(Crud crud) throws SQLException {
		this.crud = crud;
		size = 0;
		ResultSet rs = crud.query("select product_id from inventory;");
		productIds = new String[rs.getMetaData().getColumnCount()];
		for(int i = 0; i < productIds.length && rs.next(); i++) {
			productIds[i] = rs.getString(1);
			size++;
		}
		emailScanner = new Scanner(emailFileName);
		dates = new Scanner(dateFileName);
	}
	
	public void simulateOrders(String dateQuantityFile) throws FileNotFoundException,
															   SQLException {
		Scanner scanner = new Scanner(new File(dateQuantityFile));
		
		while(scanner.hasNextLine()) {
			Order order = new Order();
			order.setFields(nextCustomer(), nextProduct(), nextQuantity(), nextDate(), true);
			//TODO: make sure it doesn't try to send emails for this part specifically lol
		}
	}
	

	
	private int nextQuantity() {
		int gaussian = (int)nextGaussian() * 500;
		int quantity = gaussian < 1 ? - 1 * gaussian : gaussian;
		return quantity;
	}
	
	private Date nextDate() {
		return new Date(dates.nextLine());
	}
	@org.jetbrains.annotations.NotNull
	private Object[] nextCustomer()
	throws SQLException {
		int chance = (int)Math.round(nextGaussian()) * 10;
		if(chance > 3 || chance < - 3) {
			return nextNewCustomer();
		} else {
			return nextOldCustomer();
		}
	}	

	private Object[] nextNewCustomer() {
		String email = nextEmail();
		String parts = (email.substring(0, email.indexOf("@")));
		String firstName = parts.substring(0,parts.length() / 2); 
		String lastName = parts.substring(parts.length()/2+1);
		String location = ((nextInt() + 100000) % 10000) + "";
		return new Object[] {email, firstName, lastName, location};
	}
	// use CRUDBuddy to get a randomly chosen existing entry from sales table
	private Object[] nextOldCustomer() throws SQLException {
		int index = nextInt(crud.query("select email from customers;").getMetaData()
		 .getColumnCount() + 1);
		ResultSet rs = crud.query("select * from customers where idx = " + index);
		rs.next();
		return new Object[] {rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)};
	}
	
	
	public boolean shouldCreateNewCustomer(int i) {
		int number = nextInt(1000000);
		return (number >= 2 && i < 1000000)
			   || (number >= 5 && i < 5000000)
			   || (number >= 8 && i < 7500000);
	}
	
	public String nextProduct() {
		return productIds[nextInt(size)];
	}
	
	public String nextEmail() {
		if(! emailScanner.hasNextLine()) {
			emailScanner = new Scanner("emails.csv");
		}
		return emailScanner.nextLine();
	}
}

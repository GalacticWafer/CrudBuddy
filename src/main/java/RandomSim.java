import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

class RandomSim extends Random {
	private static final Date PAST_DATE = new Date();
	public static final int NUMBER_OF_CUSTOMER_FIELDS = 4;
	private final Crud crud;
	private final ArrayList<String> prodictIdList = new ArrayList<>();
	private int size;
	
	public RandomSim(Crud crud) throws SQLException
	{
		
		this.crud = crud;
		size = 0;
		ResultSet rs = crud.query("select product_id from inventory;");
		while(rs.next()){
			prodictIdList.add(rs.getString(1));
			size++;
		}
	}
	
	
	public void simulateOrders(String dateQuantityFile) throws FileNotFoundException, SQLException {
		Scanner scanner = new Scanner(new File(dateQuantityFile));
		
		for(int i = 0; scanner.hasNextLine(); i++) {
			
			String[] dateAndQuantity = scanner.nextLine().split(",");
			Date date = new Date(dateAndQuantity[0]);
			int quantity = Integer.parseInt(dateAndQuantity[1]);
			Object[] customer = new Object[NUMBER_OF_CUSTOMER_FIELDS];
			if(nextInt(10) + 1 > 5) {
				customer = nextNewCustomer();
			}
			else {
				customer = nextOldCustomer();
			}
			Order order = new Order();
			order.setFields(customer, nextProduct(), quantity, date, true);
			//TODO: make sure it doesn't try to send emails for this part specifically lol
		}
	}
	
	// use CRUDBuddy to get a randomly chosen existing entry from sales table
	private Object[] nextOldCustomer() throws SQLException {
		return new Object[]{nextEmail(), "malcolm", "johnson", "80003"};
	}
	
	private Object[] nextNewCustomer() {
		return new Object[]{nextEmail(), "malcolm", "johnson", "80003"};
	}
	
	public boolean shouldCreateNewCustomer(int i) {
		int number = nextInt(1000000);
		return (number >= 2 && i < 1000000)
			   || (number >= 5 && i < 5000000)
			   || (number >= 8 && i < 7500000);
	}
	
	public String nextProduct() {
		return prodictIdList.get(nextInt(size));
	}
	public String nextEmail() {
		return "mjohn314@msudenver.edu";
	}
}

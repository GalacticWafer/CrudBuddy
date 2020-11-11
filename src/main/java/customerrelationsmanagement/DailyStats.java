package customerrelationsmanagement;

import java.sql.Timestamp;
import java.util.ArrayList;

public class DailyStats {
	double asset_total;
	Timestamp fiscal_date;
	ArrayList<String> top_customers;
	ArrayList<String> top_products;
	
	public DailyStats(Timestamp fiscal_date,
					  ArrayList<String> top_customers,
					  ArrayList<String> top_products, double asset_total) {
		
		this.fiscal_date = fiscal_date;
		this.top_customers = top_customers;
		this.top_products = top_products;
		this.asset_total = asset_total;
	}
	
	public Object[] toArray() {
		
		return new Object[] {
		 fiscal_date,
		 String.join(" ", top_customers),
		 String.join(" ", top_products),
		 asset_total,
		};
	}
}

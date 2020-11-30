package customerrelationsmanagement;

public enum ChartType {
	DAILY_ASSETS,
	DAILY_ORDER_COUNTS,
	DAILY_INCOME,
	DAILY_REVENUE,
	TOP_CUSTOMERS,
	TOP_PRODUCTS,
	SPEED_TEST;
	
	public ChartType get(int i) {
		switch(i) {
			case 0 -> {return DAILY_ASSETS;}
			case 1 -> {return DAILY_ORDER_COUNTS;}
			case 2 -> {return DAILY_INCOME;}
			case 3 -> {return DAILY_REVENUE;}
			case 4 -> {return TOP_CUSTOMERS;}
			case 5 -> {return TOP_PRODUCTS;}
			case 6 -> {return SPEED_TEST;}
		}
		throw new IndexOutOfBoundsException();
	}
	
	public String getColumn() {
		
		switch(this) {
			case DAILY_ASSETS -> { return "asset_total";}
			case DAILY_ORDER_COUNTS -> { return "order_count";}
			case DAILY_INCOME -> { return "daily_income";}
			case DAILY_REVENUE -> { return "daily_revenue";}
			case TOP_CUSTOMERS -> { return "top_customers";}
			case TOP_PRODUCTS -> { return "top_products";}
			case SPEED_TEST -> { return "";}
		}
		return "";
	}
	
	public String getXAxis() {
		switch(this) {
			case SPEED_TEST -> {return "N amount";}
			default -> {return "Year-to-date";}
		}
	}
	
	public String getYAxis() {
		
		switch(this) {
			case SPEED_TEST -> {return "Number of Rows";}
			default -> {return "Dollar Amount";}
		}
	}
	
	public static int size() {
		
		return values().length;
	}
	
	@Override public String toString() {
		
		switch(this) {
			case DAILY_ASSETS -> { return "Year-to-date Asset Totals";}
			case DAILY_ORDER_COUNTS -> { return "Orders Per Day";}
			case DAILY_INCOME -> { return "Income Per Day";}
			case DAILY_REVENUE -> { return "Revenue Per Day";}
			case TOP_CUSTOMERS -> { return "Customers of the Day";}
			case TOP_PRODUCTS -> { return "Products of the Day";}
			case SPEED_TEST -> { return "Results";}
		}
		return "";
	}
}

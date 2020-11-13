package customerrelationsmanagement;

public enum ChartType {
		YTD_ASSETS,
		YTD_CUSTOMER,
		YTD_REVENUE,
		BAR_TOP_CUSTOMERS,
		BAR_TOP_PRODUCTS;
	
	public static int size() {
		return values().length;
	}
}

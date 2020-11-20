package customerrelationsmanagement;

import java.util.InputMismatchException;

public enum Tables {
	INVENTORY,
	UNSTATUSED,
	STATUSED,
	ANALYTICS,
	SUPPLIER
	;
	
	public String[] columns() {
		
		switch(this) {
			case INVENTORY -> {
				return new String[] {
				 "product_id",
				 "wholesale_cost",
				 "sale_price",
				 "supplier_id",
				 "quantity"
				};
			}
			case UNSTATUSED -> {
				return new String[] {
				 "date_ordered",
				 "cust_email",
				 "cust_location",
				 "product_id",
				 "product_quantity"
				};
			}
			case STATUSED -> {
				return new String[] {
				 "order_id",
				 "cust_email",
				 "cust_location",
				 "product_id",
				 "product_quantity",
				 "date_ordered",
				 "date_accepted",
				 "order_status",
				 };
			}
			case ANALYTICS -> {
				return new String[] {
				 "fiscal_date",
				 "asset_total",
				 "daily_income",
				 "daily_revenue",
				 "top_customers",
				 "top_products",
				 "order_count",
				 "product_count"
				};
			}
			case SUPPLIER -> {
				return new String[] {
				 "supplier_id",
				 "product_id",
				 "restock_quantity",
				 "time_accepted"
				};
			}
		}
		throw new InputMismatchException();
	}
	
	public String creationString() {
		
		String[] types = this.typesArray();
		String[] names = this.columns();
		StringBuilder builder =
		 new StringBuilder("CREATE TABLE IF NOT EXISTS " + this)
		  .append("(idx INT(16) NOT NULL AUTO_INCREMENT,");
		for(int i = 0; i < names.length; i++) {
			builder.append(names[i]).append(" ")
				   .append(types[i]).append(",");
		}
		builder.append("PRIMARY KEY (idx))");
		return builder.toString();
	}
	
	@Override public String toString() {
		
		switch(this) {
			case INVENTORY -> {return "inventory";}
			case UNSTATUSED -> {return "unstatused_sales";}
			case STATUSED -> {return "statused_sales";}
			case ANALYTICS -> {return "daily_analysis";}
			//Todo Adam return the name of your new table instead of null
			case SUPPLIER -> {return null;}
		}
		throw new InputMismatchException();
	}
	
	private String[] typesArray() {
		
		switch(this) {
			case INVENTORY -> {
				return new String[] {
				 "VARCHAR(12)",
				 "DECIMAL(13,2)",
				 "DECIMAL(13,2)",
				 "VARCHAR(10)",
				 "INT(6)",
				 };
			}
			case UNSTATUSED -> {
				return new String[] {
				 "DATETIME",
				 "VARCHAR(100)",
				 "VARCHAR(200)",
				 "Varchar(12)",
				 "int(8)"
				};
			}
			case STATUSED -> {
				return new String[] {
				 "VARCHAR(12)",
				 "VARCHAR(100)",
				 "VARCHAR(5)",
				 "VARCHAR(12)",
				 "int(8)",
				 "DATETIME",
				 "DATETIME",
				 "int(2)"
				};
			}
			case ANALYTICS -> {
				return new String[] {
				 "DATETIME",
				 "DECIMAL(13, 2)",
				 "DECIMAL(13, 2)",
				 "DECIMAL(13, 2)",
				 "VARCHAR(1000)",
				 "VARCHAR(1000)",
				 "INT(6)",
				 "INT(6)"
				};
			}
			// Todo Adam return the string array of mysql types 
			//  for each column in your new table instead of null
			case SUPPLIER -> {return null;}
		}
		throw new InputMismatchException();
	}
}

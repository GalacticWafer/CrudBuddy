public class Product {
	private final String productId;
	private final int requestedQuantity;
	private int currentQuantity;
	private Boolean canProcess;
	private boolean status;
	
	public Product(String productId, int requestedQuantity){
				this.productId  = productId;
				this.requestedQuantity = requestedQuantity;
				canProcess = null;
	}
	
	public boolean isProcessable() {
		return canProcess;
	}

	public void setIsProcessable(boolean canProcess, int currentQuantity) {
		this.canProcess = canProcess;
		this.currentQuantity = currentQuantity;
	}
	
	public String getId() {
		return productId;
	}
	
	public int getQuantity() {
		return requestedQuantity;
	}
	
	public boolean status() {
		return status;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	@Override public String toString() {
		return productId + ": " + requestedQuantity
			   + (canProcess!= null && !canProcess ? " <-- ONLY " + currentQuantity + " LEFT IN STOCK!" : "");
	}
}


public class TransactionItem {
	private final String productId;
	private final int requestedQuantity;
	private int currentQuantity;
	
	public TransactionItem(String productId, int requestedQuantity){
				this.productId  = productId;
				this.requestedQuantity = requestedQuantity;
	}
	
	
	public void setCurrentQuantity(int currentQuantity) {
		this.currentQuantity = currentQuantity;
	}
	
	public String getProductId() {
		return productId;
	}
	
	public int getQuantity() {
		return requestedQuantity;
	}
}


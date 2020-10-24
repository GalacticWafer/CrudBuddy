public class TransactionItem {
	private final String productId;
	private final int requestedQuantity;
	private int currentQuantity;
	private boolean canProcess;
	
	public TransactionItem(String productId, int requestedQuantity){
				this.productId  = productId;
				this.requestedQuantity = requestedQuantity;
	}
	
	public boolean isProcessable() {
		return canProcess;
	}
	
	public void setIsProcessable(boolean canProcess) {
		this.canProcess = canProcess;
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
	
	@Override public String toString() {
		return "product: " + productId + ":\n" + (isProcessable() ? 
		  "Amount Ordered: " + requestedQuantity
		 :"requested quantity: " + requestedQuantity +
		  "\navailable quantity: " + currentQuantity);
	}
}


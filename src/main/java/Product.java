public class Product {
	private Boolean canProcess;
	private int currentQuantity;
	private final String productId;
	private final int requestedQuantity;
	
	/**
	 * Represents one product in an Order of products.
	 *
	 * @param productId
	 *  the product_id to be checked in the database.
	 * @param requestedQuantity
	 *  the quantity requested.
	 */
	public Product(String productId, int requestedQuantity) {
		
		this.productId = productId;
		this.requestedQuantity = requestedQuantity;
		canProcess = null;
	} // End Constructor
	
	public String getId() {
		
		return productId;
	} // End getId
	
	public int getQuantity() {
		
		return requestedQuantity;
	} // End getQuantity
	
	/**
	 * Represents the stored result of w
	 *
	 * @return true if the product was previously determined to be of
	 *  sufficient
	 *  quantity and is a known product in our database. Otherwise, false.
	 */
	public boolean isProcessable() {
		
		return canProcess;
	} // End isProcessable
	
	/**
	 * Set the field <code>boolean isProcessable</code> based and records what
	 * the current quantity for the product is.
	 *
	 * @param canProcess
	 *  will be used to set <code>canProcess</code>
	 * @param currentQuantity
	 *  will be used to set <code>currentQuantity</code>
	 */
	public void setIsProcessable(boolean canProcess, int currentQuantity) {
		
		this.canProcess = canProcess;
		this.currentQuantity = currentQuantity;
	} // End setIsProcessable
	
	/**
	 * String representation helps create meaningful messages to respond to
	 * requested orders.
	 *
	 * @return the productId, requestedQuantity, and how many units are
	 *  available if there are not enough units to fulfill the order.
	 */
	@Override public String toString() {
		
		return productId + ": " + requestedQuantity
			   + (canProcess != null && !canProcess ?
		 " <-- ONLY " + currentQuantity + " LEFT IN STOCK!" : "");
	} // End toString
} // End class Product


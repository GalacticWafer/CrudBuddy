package customerrelationsmanagement;

/**
 * Reflects the current state of an order.
 */
public enum Status {
	INVALID,
	CANCELLED, 
	UNPROCESSED,
	QUANTITY_SHORTAGE, 
	PROCESSED,
	SUGGESTED_EMAIL,
	FULFILLED;
}

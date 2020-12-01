package customerrelationsmanagement;


/**
 * Defines whether the transaction is a buyer or seller event.
 */

public enum EventType {
	BUYER,
	SELLER,
	;
	
	public static EventType parse(String string) {
		if(Boolean.parseBoolean(string)) {
			return BUYER;
		}
		return SELLER;
	}
	public static EventType isSale(boolean isSale) {
		return isSale ? BUYER : SELLER;
	}
}

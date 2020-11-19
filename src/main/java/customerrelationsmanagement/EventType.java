package customerrelationsmanagement;

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

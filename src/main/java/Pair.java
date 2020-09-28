import java.util.Objects;

public class Pair<K, V>
{
	K key;
	V value;
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	public void setKey(K newKey) {
		this.key = newKey;
	}
	public void setValue(V newValue) {
		this.value = newValue;
	}
	public K getKey() {
		return key;
	}
	public V getValue() {
		return value;
	}
	
	@Override public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		Pair<?, ?> pair = (Pair<?, ?>)o;
		return getKey().equals(pair.getKey());
	}
	
	@Override public int hashCode() {
		return Objects.hash(getKey());
	}
	
	@Override public String toString() { return key.toString(); }
}

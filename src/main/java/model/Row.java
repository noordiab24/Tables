package model;

import java.io.Serializable;
import java.util.List;

public record Row(String key, List<Object> fields) implements Serializable {
	@Override
	public int hashCode() {
		return this.key.hashCode() ^ this.fields.hashCode();
	}
	//use the exclusive or: mixes the two numbers together
	//--> the hash code for the key and fields
}
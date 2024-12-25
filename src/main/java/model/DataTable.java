package model;

public interface DataTable extends Table {
	public int capacity();

	public default boolean isFull() {
		return size()==capacity();
	}

	public default double loadFactor() {
		return  size()/(double) capacity();
	}
}

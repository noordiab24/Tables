package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public interface Table extends Iterable<Row> {
	
	
	
	public void clear();

	public List<Object> put(String key, List<Object> fields);

	public List<Object> get(String key);

	public List<Object> remove(String key);

	
	//implemented method
	public default boolean contains(String key) {
		//returns true if HIT and returns false if MISS
		return this.get(key)!=null;
		
		
	}

	public int degree();

	public int size();

	public default boolean isEmpty() {
		return size()==0;
	}

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	@Override
	public Iterator<Row> iterator();

	public String name();

	public List<String> columns();

	@Override
	public String toString();

	public default String toPrettyString() {
		int[] widths = new int[degree()];
		for (int i = 0; i < columns().size(); i++)
			widths[i] = Math.max(widths[i], columns().get(i).length());
		for (Row row: this) {
			widths[0] = Math.max(widths[0], row.key().length());
			for (int i = 0; i < row.fields().size(); i++)
				widths[i+1] = Math.max(widths[i+1], Objects.requireNonNullElse(row.fields().get(i), "").toString().length());
		}

		StringJoiner head2 = new StringJoiner(" | ", "| ", " |\n");
		for (int i = 0; i < columns().size(); i++)
			head2.add(("%-"+widths[i]+"s").formatted(columns().get(i)));

		String head1 = ("| %-"+(head2.length()-4)+"s|\n").formatted(name());

		StringBuilder body = new StringBuilder();
		for (Row row: this) {
			StringJoiner current = new StringJoiner(" | ", "| ", " |\n");
			current.add(("%-"+widths[0]+"s").formatted(row.key()));
			for (int i = 0; i < row.fields().size(); i++)
				current.add(("%-"+widths[i+1]+"s").formatted(Objects.requireNonNullElse(row.fields().get(i), ""), widths[i+1]));
			body.append(current);
		}

		String line = "+" + "-".repeat(head2.length()-3) + "+\n";

		return line + head1 + line + head2 + line + body + line;
	}

	/*
	 * public default DataTable filter(String columnName)
	 * {
	 * 	Iterator <Row> iterator= iterator();
	 * 	List<Row> row= new ArrayList <> ();
	 * 	while(iterator.hasNext())
	 * 	{
	 * 		var row= iterator.next();
	 * 		var fields= row.fields();
	 * 		for(int i=0; i<fields.size(); i++)
	 * 		{
	 * 			var fieldElement= fields.get(i);
	 * 			if(fieldElement.equals(columnName))
	 * 			{
	 * 				Row rowToAdd= new Row(row.key(), row.fields());
	 * 				rows.add(rowToAdd);
	 * 				break;
	 * 			}
	 * 		}
	 * 	}
	 * 	return null;
	 * }
	 */
}

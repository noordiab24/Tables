package tables;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import model.DataTable;
import model.Row;
import model.Table;

public class SymbolTable implements DataTable {
	
	private String tableName; //keeps track of the table name
	private List<String> columns; //the List of columns names
	private Row [] rows; //an array that contains elements that are Lists of fields
	private int rowSize; //keeps track of the row size
	private int fingerPrint;

	
	//Constructor
	public SymbolTable(String name, List<String> columns) {
		this.tableName= name; //initialize tableName
		this.columns= columns; //initialize columns
		clear(); //call clear method to initialize the rows array, rowSize, and fingerPrint
	}

	@Override
	public void clear() {
		rows= new Row[52]; //set the row to 52 slots
		rowSize=0; //set rowSize to 0
		fingerPrint=0;
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		//on put miss, create row: increase fingerprint by new row's hash
				//on put hit, update row:
						//subtract off what was contributed by the old row
						//increase fingerprint by the new row's hash
		
		
		//the guard conditions are found in the indexHelper method
		//this guard condition checks if the number of fields provided
		//fits the number of columns we have
		//when a row is too wide or too narrow
		if(fields.size()!=degree()-1)
		{
			throw new  IllegalArgumentException("The row is too wide or too narrow.");
		}
		
		
		int index= indexHelper(key); //calls indexHelper to obtain index
		//MISS
		if(rows[index]==null)
		{
			rows[index]= new Row(key, fields); //create a new Row
			fingerPrint+=rows[index].hashCode(); //add new row's hash
			rowSize++; //increment rowSize
			return null;
		}
		//HIT
		else
		{
			List<Object> oldFields= rows[index].fields(); //obtain the oldFields
			fingerPrint= fingerPrint-rows[index].hashCode(); //subtract off old row's hash
			rows[index]= new Row(key, fields); //update row
			fingerPrint+=rows[index].hashCode(); //add fingerPrint by new row's hash
			return oldFields; //return the old list of fields
		}
		
		
	}

	@Override
	public List<Object> get(String key) {
		
			int index= indexHelper(key); //calls the indexHelper to obtain the index
			//MISS
			if(rows[index]==null) 
			{
				return null; //returns null
			}
			//HIT
			else
			{
				List<Object> oldFields= rows[index].fields(); //obtains the oldFields
				return oldFields; //returns the oldFields
			}
		
	}

	@Override
	public List<Object> remove(String key) {
		
		
		//on remove hit: decrease fingerprint by row's hash
		int index= indexHelper(key); //obtains the index from indexHelper
		//MISS
		if(rows[index]==null)
		{
			return null; //returns null
		}
		//HIT
		else
		{
			List<Object> oldFields= rows[index].fields(); //obtains the oldFields
			rowSize--; //decrement rowSize
			fingerPrint= fingerPrint-rows[index].hashCode(); //decrease fingerprint by row's hashCode
			rows[index]= null; //deletes the old row
			return oldFields; //returns the oldFields
		}
	}

	@Override
	public int degree() {
		return columns.size(); //use the pre-existing size method of the List
	}

	@Override
	public int size() {
		return rowSize; //returns the row size
	}

	@Override
	public int capacity() {
		return 52; //this is the max number of rows in our table
	}

	@Override
	public int hashCode() {
		
		//the sum of the hashes of all the rows
		//fingerprint is the table's hashCode
		return fingerPrint;
	}

	@Override
	public boolean equals(Object obj) {
		
		//the given object has the same type as this table + the given object
		//has the same fingerprint (hashCode) as this table
		//then return true
		//otherwise return false
		return ((obj instanceof Table) && this.hashCode()==obj.hashCode());
	}

	@Override
	public Iterator<Row> iterator() {
		return new Iterator<>() {

			//the JUnit Test tests how many actual rows (that are not null) that are in the array
			//so example, we return true 4 times if there are 4 rows that are not null
			//initialization (private variable)
			private Iterator<Row> Iterator;
			int max= capacity();
			int index=0;
	
			
			//skipping if possible
			@Override
			public boolean hasNext() {
				//maintenance condition 
				//differs based on the 2 designs --> must consider gaps
				
				while(index<max && rows[index]==null)
				{
					index++;
			
				}
				return index<max;
			}

			//if has next is false, next will never get called
			//blindly advance by one
			@Override
			public Row next() {
				
				//uses the element (by returning it)
				//progress -> go to the next element // incrementation
				//throw new UnsupportedOperationException("Implement iterator's next for Module 1");
				
				
					Row temp= rows[index];
					index++;
					return temp;	
				
			}
			
		};
	}

	@Override
	public String name() {
		return this.tableName; //returns the tableName
	}

	@Override
	public List<String> columns() {
		return this.columns; //returns the list of columns
	}

	@Override
	public String toString() {
		
		return this.toPrettyString(); //returns the table printed
	}
	
	////provides the index!!
	public int indexHelper(String key)
	{
		
		int num=0;
		char letter= key.charAt(0); //only used if it passes the guard conditions
		//guard condition
		if(key.length()>1) //checks if the key is not a single variable
		{
			throw new  IllegalArgumentException("Invalid key: the key is not a single variable.");
		}
		//guard condition
		
		else if(!(letter>='A' && letter<='Z') && !(letter>='a' && letter<='z')) //checks if the key is not a letter
		{
			throw new  IllegalArgumentException("Invalid Key: the key is not a letter.");
		}
		
		else if(letter>='A' && letter<='Z') //checks upperCase letters
		{
			num= (letter-'A') +26 ; //calculation for upper case letters
		}
		else
		{
			num= letter-'a' ; //calculation for lower case letters
		}
		
		return num; //returns the index
		
		
	}
}

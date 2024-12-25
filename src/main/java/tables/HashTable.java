package tables;

import java.util.Iterator;
import java.util.List;

import model.DataTable;
import model.Row;
import model.Table;

public class HashTable implements DataTable {
	
	private String tableName;
	private Row[] rows;
	private List<String> columns;
	private int rowSize;
	private int capacity;
	private int fingerPrint;
	private static final Row SENTINEL= new Row(null, null);
	
	public HashTable(String name, List<String> columns) {
		this.tableName= name;
		this.columns= columns;
		clear();
	}

	@Override
	public void clear() {
		capacity= 32;
		rowSize=0;
		fingerPrint=0;
		rows= new Row[capacity]; 
		
	}


	private int hashFunction(String key) {
		//salt means to concatenate 
		key= key+"noor";
		int hash=0;
		
		//polynomial rolling hash
		
		for(int index=0; index<key.length(); index++)
		{
			//deterministic: (add char value to capacity/2)*31 each time
			hash= 31*(key.charAt(0)+(capacity/2))+hash;
			key= key.substring(1); //decrement the key 
		}
		
		return Math.floorMod(hash, capacity); //return hash
	}

	//must not return 0(or anything that would mod to 0)
	private int doubleHash(String key)
	{
		key+="noor";
		int FNVOFFSETBASIS= 0x811c9dc5; //the FNV offset basis
		int hash= FNVOFFSETBASIS; //set hash = to FNV offset basis
		int FNVPRIME= 16777619; //the FNV prime
		byte [] keyValues= key.getBytes(); // byte array
		for(byte value: keyValues) //FNV-1a hash
		{
			hash= hash ^ value;
			hash= hash * FNVPRIME;
			
		}
		
		
		return (Math.floorMod(hash, capacity-1)+1); //return hash (hash mod capacity-1) +1
		
		//example of ^
		//0 0 1
		//1 0 1
		//returns 1 0 0
	}
	
	@Override
	public List<Object> put(String key, List<Object> fields) {
		//Main difference between hash table and symbol table:
			//in symbol table, indexes always contain the same keys [0-52]
			//in hash table, indexes contain different keys each time
				//so much check for HIT if keys.equals(key) 
		//guard condition for an invalid key
		if(key==null)
		{
			throw new IllegalArgumentException("Invalid Key!");
		}
		//guard condition for fields which are too wide or narrow
		else if(fields.size()!=degree()-1)
		{
			throw new  IllegalArgumentException("The row is too wide or too narrow.");
		}
		
		int hash1= hashFunction(key); //obtain hash1
		int hash2= doubleHash(key); //obtain hash2
		int index=hash1; //set the index to hash1
		int sentinel= -1;
		
		
		//keep looping as long as there isn't a hit or a miss
		for(int step=0; step<capacity; step++)
		{
			
			//fixes the fall through 
			if(hash2%2==0)
			{
				hash2 = hash2+1;
			}
			
			index= Math.floorMod(hash1+hash2*step, capacity); //obtain the index
			
			//in the first run, it will basically be hash1%capacity
			
			//miss
			//consider null fields 
			if(rows[index]==null)
			{
				if(sentinel==-1) //if there isn't a sentinel, then add it to the null location
				{
				rows[index]= new Row(key, fields); //create a new row
				fingerPrint+=rows[index].hashCode(); //update fingerPrint
				}
				
				else //if there is a sentinel, add it to the sentinel location
				{
				rows[sentinel]= new Row(key, fields);
				fingerPrint+=rows[sentinel].hashCode(); //update fingerPrint
				}
				
				rowSize++; //increment rowSize
				
				if(loadFactor() >= 0.75)
				{
					capacity= capacity*2;
					rehash();
				}
				
				return null; //return null
			}
			
			
			//check if sentinel index
			else if(rows[index].key()==null)
			{
				
				if(sentinel==-1) //indicates this is the first sentinel found
				{
					sentinel= index; //set the sentinel = to index
				}
			}
			
			//hit
			else if(rows[index].key().equals(key))
			{
				List<Object> oldFields= rows[index].fields(); //save the oldFields
				fingerPrint= fingerPrint- rows[index].hashCode(); //update fingerPrint
				rows[index]= new Row(key, fields); //update the oldRow
				fingerPrint+=rows[index].hashCode(); //update fingerPrint
				return oldFields; //return oldFields
			}	
		}
		
		throw new IllegalStateException("There has been an unexpected fall-through."); //throw exception
	}

	
	@Override
	public List<Object> get(String key) {
		
		int hash1= hashFunction(key); //obtain hash1
		int hash2= doubleHash(key); //obtain hash2
		
		int index= hash1;
		
		//keep looping as long as there is no hit or miss
		for(int step=0; step<capacity; step++)
		{
			//fixes the fall through 
			if(hash2%2==0)
			{
				hash2= hash2+1;
			}
			
			
			index=Math.floorMod(hash1+hash2*step, capacity); //loops through a new index
			
			//miss
			if(rows[index]==null)
			{
				return null; //return null
			}
			else if(rows[index].key()==null) //this indicates the presence of a sentinel
											//this helps prevent a null pointer exception in the method below
			{
				continue;
			}
			//hit
			else if(rows[index].key().equals(key)) 
			{
				List<Object> fields= rows[index].fields(); 
				return fields; //return the fields
			}
			
		}
		
		throw new IllegalStateException("There has been an unexpected fall-through."); //throw an exception if no hit or miss 


	}

	@Override
	public List<Object> remove(String key) {
		int hash1= hashFunction(key); //obtain hash1
		int hash2= doubleHash(key); //obtain hash2
		int index=hash1; //set the index to hash1
		
		//keep looping as long as there isn't a hit or a miss
		for(int step=0; step<capacity; step++)
		{
			//fixes the fall through 
			if(hash2%2==0)
			{
				hash2= hash2+1;
			}
			
			
			index= Math.floorMod(hash1+hash2*step, capacity); //obtain the index
			//in the first run, it will basically be hash1%capacity
			
			//miss
			if(rows[index]==null)
			{
				return null; //return null
			}
			else if(rows[index].key()==null) //this indicates the presence of a sentinel
											//and helps prevent a null error below
			{
				continue;
			}
			//hit
			else if(rows[index].key().equals(key))
			{
				List<Object> oldFields= rows[index].fields(); //obtain the oldFields
				fingerPrint= fingerPrint-rows[index].hashCode(); //update fingerPrint
				rowSize--; //decrement rowSize
				rows[index]= SENTINEL; //replace oldRow with sentinel
				return oldFields; //return oldFields
			}
			
		}
		
		throw new IllegalStateException("There has been an unexpected fall-through."); //throw an exception if no hit or miss
		
		
	}
	
	public void rehash()
	{
		fingerPrint=0;
		rowSize=0;
		Row [] row2= rows; //set row2 equal to rows
		rows= new Row [capacity]; //re-create the rows array so that is contains the new capacity, but it is completely empty
		
		for(int i=0; i<row2.length; i++)
		{
			if(row2[i]!=null && row2[i].key()!=null ) //if the fields are NOT null and there is NOT a sentinel 
			{
				this.put(row2[i].key(), row2[i].fields()); //put the key/fields into the rows array
				//and then our put will update our rowSize and fingerPrint properties
			}
		}
		//this will help us eliminate sentinels
	}
	
	@Override
	public int degree() {
		return columns.size(); //returns the total number of columns
	}

	@Override
	public int size() {
		return rowSize; //returns the current number of rows
	}

	@Override
	public int capacity() {
		return capacity; //returns the capacity
	}

	@Override
	public int hashCode() {
		return fingerPrint; //return fingerPrint
	}

	@Override
	public boolean equals(Object obj) {
		//checks if object is an instance of Table AND if fingerPrint of Object== fingerPrint of table
		return ((obj instanceof Table) && this.hashCode()==obj.hashCode());
	}

	@Override
	public Iterator<Row> iterator() {
		return new Iterator<>() {
			//Similar to module 1

			private Iterator<Row> Iterator;
			int max= capacity();
			int index=0;
	

			@Override
			public boolean hasNext() {
				//consider null rows and sentinel filled rows
				while(index<max && rows[index]==null || index<max && rows[index].key()==null)
				{
					index++; //keep incrementing (jumping over) as long as there is a null or sentinel filled row
			
				}
				return index<max; 
			}

			@Override
			public Row next() {
				Row temp= rows[index]; 
				index++;
				return temp; //returns a row with a value
			}
		};
	}

	@Override
	public String name() {
		return tableName; //returns the table name;
	}

	@Override
	public List<String> columns() {
		return columns; //returns the list of column names
	}

	@Override
	public String toString() {
		//identical to module 1
		return this.toPrettyString(); //returns the table printed
	}
}

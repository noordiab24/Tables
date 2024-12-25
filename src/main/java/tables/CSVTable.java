package tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import model.DataTable;
import model.FileTable;
import model.Row;
import model.Table;


public class CSVTable implements FileTable {
	public static final Path base= Paths.get("db", "tables");
	private Path file;	
	
	
	public CSVTable(String name, List<String> columns) {
		//create new table
		
		
		try {
			Files.createDirectories(base); //create the base directory (folder)
			file= base.resolve(name+".csv"); 
			if(Files.notExists(file)) //check that we don't override 
			{
				Files.createFile(file); 
			}
			
			
			//store the column name in the header of the file
			
			var header= String.join(",", columns); //use the .join() to join
			Files.writeString(file, header ); //writes the header into the file
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	
	}

	public CSVTable(String name) {
		//reopen existing file
		file= base.resolve(name+".csv"); //path is abstract
		if(Files.notExists(file)) //check if the file exists
		{
				
					// TODO Auto-generated catch block
					throw new RuntimeException("DNE Exist.");
				
			
		}
	}

	@Override
	public void clear() {
		try {
			//missing data
			String name= name();
			List<String> columns= columns();
			
			//then delete file info
			Files.delete(file);
			
			//actually create the file now
			file= base.resolve(name+".csv"); 
			
			if(Files.notExists(file)) //check that we don't override 
			{
				Files.createFile(file); 
			}

			var header= String.join(",", columns); //use the .join() to join
			Files.writeString(file, header); //writes the header into the file
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		if(key==null) //guard condition
		{
			throw new IllegalArgumentException("Invalid Key!");
		}
		if(fields.size()!= degree()-1) //guard condition 
		{
			throw new  IllegalArgumentException("The row is too wide or too narrow.");
		}
		else
		{
			try {
				var lines= Files.readAllLines(file); //reads lines into a list 
				
				//start at index 1 because index 0 is the header!!!
				for(int index=1; index<lines.size(); index++)
				{
					var currentRow= decode((lines.get(index))); //decodes that CSV (row) into a Row object
					//hit
					if(currentRow.key().equals(key))
					{
						List<Object> oldFields= currentRow.fields(); //store the oldFields
						Row row= new Row(key, fields); //make new row object --> doesn't matter because it's not actually part of the file
						String CSV= encode(row); //turn the row object into a CSV
						lines.set(index, CSV); //update the list
						
						
						//apply move-to-front heuristic:
						lines.remove(index); //remove CSV at current index
						lines.add(1, CSV); //add it to the front
						Files.write(file,lines); //write the updated list into the file
						return oldFields; //return the oldFields
					}
				}
				
				//miss
				Row row= new Row(key, fields); //make new Row object
				String CSV= encode(row);
				lines.add(1, CSV);  //create a new element in the list that contains this CSV
				Files.write(file, lines ); //write it into the file
				return null; //return null
				
				
				
			}
			catch (IOException e) 
			{
				throw new RuntimeException(e);
			}
			
		}
	}

	@Override
	public List<Object> get(String key) {
		try {
			var lines= Files.readAllLines(file); //reads the lines into a list of Strings
			for(int index=1; index<lines.size(); index++)
			{
				var currentRow= decode((lines.get(index))); //decodes that CSV (row) into a Row object
				//hit
				if(currentRow.key().equals(key)) //if it hits
				{
					List<Object> oldFields= currentRow.fields(); //obtain the oldFields
					//apply move-to-front heuristic
					lines.remove(index); //remove the key
					String CSV= encode(currentRow); //obtains the CSV of the currentRow
					lines.add(1, CSV); //adds it to the front of the list
					Files.write(file, lines); //updates the file)
					//
					return oldFields; //return the oldFields
				}
			}
			//miss
			return null; //return null at miss
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public List<Object> remove(String key) {
		try {
			var lines= Files.readAllLines(file); //reads the lines into a list of Strings
			for(int index=1; index<lines.size(); index++)
			{
				var currentRow= decode((lines.get(index))); //decodes that CSV (row) into a Row object
				//hit
				if(currentRow.key().equals(key)) //if it hits
				{
					List<Object> oldFields= currentRow.fields(); //obtain the oldFields
					String CSV= encode(currentRow); //obtain CSV
					lines.remove(CSV); //remove it from lines
					Files.write(file, lines); //actually write the removal into the file
					return oldFields; //return the oldFields
				}
			}
			//miss
			return null; //return null at miss
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
	}

	@Override
	//the total number of columns (key plus fields)
	public int degree() {
		
		try {
			var lines= Files.readAllLines(file); 
			var columns= lines.get(0); //get the header
			String [] columnsList= columns.split(","); //makes a list
			return columnsList.length; //return the number of columns+key
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	//returns the number of rows
	public int size() {
		try {
			var lines= Files.readAllLines(file); 
			return lines.size()-1; //return the rows (considering the header is the first row, so -1)
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public int hashCode() {
	
		try {
			var lines = Files.readAllLines(file);
			int hash=0;
			for(int i=1; i<lines.size(); i++)
			{
				var currentRow= decode(lines.get(i)); //obtains the row object from the CSV string provided
				hash= hash + currentRow.hashCode(); //obtains the hash of the current Row
			}
			return hash;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		//checks if object is an instance of Table AND if fingerPrint of Object== fingerPrint of table
		return ((obj instanceof Table) && this.hashCode()==obj.hashCode());
	}

	@Override
	public Iterator<Row> iterator() {
		//Iterator <Row> iterator;
		int max= size(); //provides us with the total number of rows
		
		try {
			var lines= Files.readAllLines(file); //read lines into CSV list
			List<Row> rowList= new ArrayList<>(); //Instantiate Row list
			for(int i=1; i<=max; i++) 
			{
				var currentRow= decode(lines.get(i)); //obtain currentRow
				rowList.add(currentRow); //add the currentRow into the rowList
			}
			
			return rowList.iterator(); //call iterator() 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		

	}

	@Override
	//returns the table name
	public String name() {
		String pathName= file.getFileName() + "" ; //reads the file name: ex --> m3_table1.csv
		String name= pathName.substring(0,pathName.indexOf(".")); //removes the .csv
		//this should work because it accesses the farthest element in the hierarchy
		return name; //returns the table name
	}

	@Override
	//returns the list of column name
	public List<String> columns() {
		try {
			var lines= Files.readAllLines(file); //reads the lines into a list
			String header= lines.get(0); //obtains the header
			String headerArray [] = header.split(","); //makes the header into an array
			List<String> columns= Arrays.asList(headerArray); //turn the array into a list
			return columns; //return the list of columns name
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return this.toPrettyString();
	}
	
	//helper method --> turns a row into a CSV String
	private static String encode(Row row)
	{
		var sj= new StringJoiner(","); //creates a String, where values are separated by commas
		sj.add("\""+  row.key() + "\""); //must use quotes around the key
		//loops through the elements of the row List
		for(int i=0; i<row.fields().size(); i++)
		{
			if((row.fields().get(i)) instanceof String) //checks if the element is a String
			{
			sj.add("\"" + row.fields().get(i)+ "\""); //enclose strings with quotation marks
			}
			else
			{
				sj.add(row.fields().get(i) + ""); //makes other types (booleans, doubles, etc) into Strings
			}
		}
		
		return sj.toString();
	}
	
	private static Row decode(String CSV)
	{
		String rowArray[]= CSV.split(","); //splits the CSV into an array
		String key= rowArray[0].substring(1, rowArray[0].length()-1); //removes the quotes around the key
		
		List<Object> fields= new ArrayList<> ();
		for(int i=1; i<rowArray.length; i++) //iterates through the fields
		{
			if(rowArray[i].charAt(0)=='"' && rowArray[i].charAt(rowArray[i].length()-1) == '"')
			{
				fields.add(rowArray[i].substring(1,rowArray[i].length()-1));
			}
			else if(rowArray[i].equals("null")) 
			{
				fields.add(null);
			}
			else if(rowArray[i].equals("true") || rowArray[i].equals("false"))
			{
				boolean currentValue= Boolean.parseBoolean(rowArray[i]);
				fields.add(currentValue);
			}
			else if(rowArray[i].contains(".")) //period indicates a double
			{
				fields.add(Double.parseDouble(rowArray[i]));
			}
			else 
			{
				fields.add(Integer.parseInt(rowArray[i]));
			}
			
		}
	
		Row rows= new Row(key, fields);
		return rows;	
		
	}
	
	
	
	public static CSVTable factory(Table oldTable)
	{
		if(oldTable instanceof DataTable)
		{
			CSVTable newTable= new CSVTable(oldTable.name(), oldTable.columns()); //instantiates the newTable
			for(Row r: oldTable)
			{
				newTable.put(r.key(), r.fields()); //row by row translation
			}
			return newTable; //returns the new table
		}
		return null; //otherwise, return null
	}
	
	
}
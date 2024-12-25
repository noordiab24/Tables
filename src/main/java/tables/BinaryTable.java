package tables;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;

import model.FileTable;
import model.Row;
import model.Table;

public class BinaryTable implements FileTable {
	private static final Path base = Paths.get("db", "tables");
	private final Path rootDir;

	public BinaryTable(String name, List<String> columns) {
		try {
			//already given
			//db/tables/m5_table1
			rootDir = base.resolve(name);
			Files.createDirectories(rootDir);
			
			
			//attempt
			//db/tables/m5_table1/data
				//resolve the data folder underneath the rootDir
				//create that data directory
			var data= rootDir.resolve("data");
			Files.createDirectories(data);
			
			//db/tables/m5_table1/metadata
				//resolve the metadata folder underneath the rootDirr
				//create that metadata directory
			var metadata= rootDir.resolve("metadata");
			Files.createDirectories(metadata);
				////db/tables/m5_table1/metadata/columns
				//save the columns to a file
				//recommend: using a data stream, ie Serializable
				//it's fine to use a data stream even if you want to use the Byte Buffer
			
			
			
			
				var columnNames= metadata.resolve("columns"); 
				if(!Files.exists(columnNames))
				{
					Files.createFile(columnNames); //creates the column File
				}
				
				var out= new DataOutputStream(Files.newOutputStream(columnNames)); //writes into the column File
				for(int i=0; i<columns.size(); i++)
				{
					out.writeUTF(columns.get(i));
					out.flush();
				}
				out.close();
				
				
	
				
				var size= metadata.resolve("size");
				
				if(!Files.exists(size))
				{
					Files.createFile(size); //creates the size File
				}
				
				var outSize= new DataOutputStream(Files.newOutputStream(size));
				outSize.writeInt(0); //writes into the size File
				outSize.flush();
				outSize.close();
				
				
				var fingerprint= metadata.resolve("fingerprint");
				if(!Files.exists(fingerprint))
				{
					Files.createFile(fingerprint); //creates the fingerprint File
				}
				var outFingerprint= new DataOutputStream(Files.newOutputStream(fingerprint));
				outFingerprint.writeInt(0); //writes into the fingerprint File
				outFingerprint.flush();
				outFingerprint.close();
		
			
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	//this is done
	//reload= know where the root folder is
	public BinaryTable(String name) {
		rootDir = base.resolve(name);

		if (Files.notExists(rootDir))
			throw new IllegalArgumentException("Missing table: " + name);
	}

	@Override
	public void clear() {
		
		try {
			//resolve the data folder underneath the rootDir
			var data= rootDir.resolve("data");
			//pipeline starts here:
				//data(SKIPS)
				//data/01
				//data/01/23456
				//data/01/abcde
				//data/ab/cd456
			//so reverse pipeline: get from leaf up
				//data/ab/cd456
					//deletes cd456
				//data/ab
					//deletes ab
				//data/01/abcde
					//deletes abcde
				//data/01/23456
					//deletes 23456
				//data/01
					//deletes 01
		
			Files.walk(data)  //this stream is like a "pipeline"
				.skip(1) //skips the first folder (data)
				.sorted(Comparator.reverseOrder()) //flips the pipeline, now leaves will get deletes off first
				.forEach(path -> path.toFile().delete()); //for each path --> convert it to a file and delete it
			//--> you can filter the pipeline to only let things pass through, like an assembly line
			//when we give it a folder, it gives us a pipeline of the folder and all of its sub-folders
			//we will use this to delete everything under the data folder --> so skip the first folder (data) and delete the ones under it
			
			//resolve the metadata folder underneath the rootDir
				//root directory= db/tables/m5_tables/
				//so only need to resolve once to get metadata
			var metadata= rootDir.resolve("metadata"); //by resolving metadata, we can get the size and fingerprint files
	
			
			
			//db/tables/m5_tables/metadata/size
			//create the size file, initialize 0
			// using the same technique you used for columns
			//ask metadata to resolve to get size
			var size= metadata.resolve("size");
			var outSize= new DataOutputStream(Files.newOutputStream(size));
			outSize.writeInt(0);
			outSize.flush();
			outSize.close();
			
			//db/tables/m5_tables/metadata/fingerprint
			//create the fingerprint file, initialize 0
			// using the same technique you used for columns
			//ask metadata to resolve to get fingerprint
			var fingerprint= metadata.resolve("fingerprint");
			var outFingerprint= new DataOutputStream(Files.newOutputStream(fingerprint));
			outFingerprint.writeInt(0);
			outFingerprint.flush();
			outFingerprint.close();
			
			
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private String digestFunction(String key) {
		
		try {
			var sha1= MessageDigest.getInstance("SHA-1"); 
			//basically says take the salt and the key and hash it
			sha1.update("Noor".getBytes());
			sha1.update(key.getBytes());
			
			//ask the sha1 object to digest all that into an integer
			var num= sha1.digest();
			//ask the HexFormat class to get you a hex format obj --> it is a static method, a factory method
			HexFormat hex= HexFormat.of();
			//ask the hex format object to convert the integer to a formatted hex string
			var hexString= hex.formatHex(num);
				//return that
			return hexString;
			
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new IllegalStateException(e);
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
		
		try {
			//preparing variables
			var hashedKey= digestFunction(key); //hashes the key
			
			var directory= hashedKey.substring(0,2); //get the directory: ex: ab
			var subdirectory= hashedKey.substring(2);  //get the full subdirectory
			
			var data= rootDir.resolve("data"); 
				var folder= data.resolve(directory); //folder path
				var file= folder.resolve(subdirectory); //file path
				
			var metadata= rootDir.resolve("metadata"); 
				var fingerprint= metadata.resolve("fingerprint"); //fingerprint path
				var size= metadata.resolve("size"); //size path
			
			
			//hit
			if(Files.exists(file))
			{
				//the whole point here is to get the fingerprint of the oldFields
				var oldFieldsObject= writeFields(file); //calls helper method to get the fields
				Row oldRow= new Row(key, oldFieldsObject); //makes a Row object
				int oldRowFingerprint= oldRow.hashCode(); //prepares the hashCode of the oldRow
				
				
				//overrides the fields
				writeIntoFile(key, file, fields); //writes the fields into the currentRow using the helper method
				
				
				//updates the fingerprint
				int currentFingerprint= hashCode(); //prepares the currentFingerprint
				Row row= new Row(key, fields); //makes the new Row to obtain its fingerprint
				int newRowFingerprint= row.hashCode(); //prepares the fingerprint of the the newRow
				var outFingerprint= new DataOutputStream(Files.newOutputStream(fingerprint)); //makes an output stream 
				
				outFingerprint.writeInt((currentFingerprint-oldRowFingerprint) + newRowFingerprint); //updates the fingerprint file
				//outFingerprint.flush();
				outFingerprint.close();
				
				return oldFieldsObject; //return the old fields
				
			}
			else
			{
			
			//miss
			
				//Structure:
					//m5_table1
						//data
							//folderName(2 letters)
								//hashedKey(actual hash) --> file that contains the fields
				
				//makes a new folder and file
				var newFolder= data.resolve(directory); 
				Files.createDirectories(newFolder); //makes a newFolder based on the first 2 characters of the key's hashcode
				Path newFile= newFolder.resolve(subdirectory); 
				Files.createFile(newFile); //makes a new File based on the key's hash
				
			
				//writes the fields into the contents File of the new row using the helper method
				writeIntoFile(key,newFile, fields); 
				
				//updates the size file
				int savedSize= size(); //prepares the currentSize
				var outSize= new DataOutputStream(Files.newOutputStream(size)); 
				outSize.writeInt(savedSize+ 1); //updates the size File
				//outSize.flush();
				outSize.close();
				
				//updates the fingerprint file
				Row newRow= new Row(key, fields); //makes a new Row object to get its fingerprint
				int newRowFingerprint= newRow.hashCode(); //prepares the new Row's fingerprint
				int oldFingerprint= hashCode(); //get the current fingerprint 
				var outFingerprint= new DataOutputStream(Files.newOutputStream(fingerprint));
				outFingerprint.writeInt(oldFingerprint+newRowFingerprint); //updates the fingerprint file
				//outFingerprint.flush();
				outFingerprint.close();
				
				return null;
			
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException(e);
		}
		
		
		//throw new UnsupportedOperationException("Implement put for Module 5");
	}
	

	public void writeIntoFile(String key, Path path, List<Object> fields)
	{
		try {
			var out= new DataOutputStream(Files.newOutputStream(path)); 
			out.writeByte((byte) key.length()); //the flag of the key is it's length as a byte
			out.writeUTF(key); //writes in the key
		
			for(int i=0; i<fields.size(); i++)
			{
				var currentValue= fields.get(i); //gets the current value
				
				//all the following methods write in a flag and a value based on the type
				
				if(currentValue instanceof String)
				{
					out.writeByte(-3);
					out.writeUTF((String)(currentValue));
					//out.flush();
				}
				else if(currentValue instanceof Boolean)
				{

					out.writeByte(-4);
					out.writeBoolean((boolean)(currentValue));
					//out.flush();
				}
				else if(currentValue instanceof Integer)
				{
					out.writeByte(-5);
					out.writeInt((int)(currentValue));
					//out.flush();
				}
				else if(currentValue instanceof Double)
				{
					out.writeByte(-6);
					out.writeDouble((double) (currentValue));
					//out.flush();
				}
				else if(currentValue == null)
				{
					out.writeByte(-2);
					//out.flush();
				}
				/*
				else if(currentValue instanceof List) {	
					//calls the method that writes a list into the file!!
					writeOnlyListIntoFile(out, (List<Object>) currentValue);
					
					out.flush();
				}
				*/
				
				else 
				{
					throw new IllegalStateException("Exception in writeIntoFile!");
				}
			}
			
			out.close();
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
	}
	
	//helper method used in the hit of put and remove to get the oldFields
	public List<Object> writeFields(Path path)
	{
	
		try {
			var in= new DataInputStream(Files.newInputStream(path));
			
			List<Object> fields= new ArrayList<> ();
		
			while( in.available()>0 )
			{
				var flag= in.readByte(); //gets the flag
				
				//based on the flag, we know what to read in
				
				if(flag == -3)
				{
					var current= in.readUTF();
					fields.add(current);
				}
				else if(flag == -5)
				{
					var current= in.readInt();
					fields.add(current);
					
				}
				else if(flag == -6)
				{
					var current= in.readDouble();
					fields.add(current);	
				}
				else if(flag == -4)
				{
					var current= in.readBoolean();
					fields.add(current);
				}
				else if(flag == -2)
				{
					fields.add(null);
				}
				else if(flag>=0) //this flag signifies a key
				{
					continue;
					
				}
				else
				{
					throw new IllegalStateException("Undesignated Flag!");
				}
				
			}
			in.close();		
			
			return fields;
		} 
		catch (IOException e) 
		{
			throw new IllegalStateException(e);
		}
	}
	
	
	//helper method - not used
	public String typeHelper(Object field)
	{
		if(field instanceof String)
		{
			return "String";
		}
		else if(field instanceof Double) 
		{
			return "Double";
		}
		else if(field instanceof Boolean )
		{
			return "Boolean";
		}
		else if(field instanceof Integer)
		{
			return "Integer";
		}
		else if(field instanceof List)
		{
			return "List";
		}
		else 
		{
			return "null";
		}
	}
	
	
	@Override
	public List<Object> get(String key) {
			
		var hashedKey= digestFunction(key); //gets the hash of the key
		var directory= hashedKey.substring(0,2); //get its directory --> first 2 letters
		var subdirectory= hashedKey.substring(2); // gets its sub-directory --> all the hash
		
		var data= rootDir.resolve("data");
			var folder= data.resolve(directory); // folder path
			var file= folder.resolve(subdirectory); //file path
		
			//if the file exists
			if(Files.exists(file))
			{
				var oldFieldsObject= writeFields(file); //get the oldFieldsObject by calling the helper method
				return oldFieldsObject; //return the oldFieldsObject
			}
			else
			{
				return null; //return null
			}
		
		
		
		
		//throw new UnsupportedOperationException("Implement get for Module 5");
	}

	@Override
	public List<Object> remove(String key) {
		
		try {
			
			var hashedKey= digestFunction(key);
			var directory= hashedKey.substring(0,2);
			var subdirectory= hashedKey.substring(2);
			
			var data= rootDir.resolve("data");
				var folder= data.resolve(directory); //folder path
				var file= folder.resolve(subdirectory); //file path
				
			var metadata= rootDir.resolve("metadata");
				var fingerprint= metadata.resolve("fingerprint");
				var size= metadata.resolve("size");
			
				if(Files.exists(file))
				{
					var oldFieldsObject=  writeFields(file); //uses the helper method to get the oldFields from the file
					
					Row oldRow= new Row(key, oldFieldsObject); //makes a Row object to obtain the hash
					int oldRowFingerprint= oldRow.hashCode(); //returns the hashCode
					
					
					//delete the file
					Files.delete(file); //deletes the file
				
					//if there are no more files in the folder, then delete the folder
					if(!folder.iterator().hasNext())
					{
						Files.delete(folder);
					}
					
					//updates the fingerprint
					int currentFingerprint= hashCode(); //prepares the currentFingerprint
					var outFingerprint= new DataOutputStream(Files.newOutputStream(fingerprint));
					outFingerprint.writeInt(currentFingerprint- oldRowFingerprint); //updates the fingerprint file
					//outFingerprint.flush();
					outFingerprint.close();
					
					
					//updates the size
					int currentSize= size(); //prepares the currentSize
					var outSize= new DataOutputStream(Files.newOutputStream(size));
					outSize.writeInt(currentSize-1); //updates the size file
					//outSize.flush();
					outSize.close();
					
					
					
					return oldFieldsObject; //returns the oldFields
				}
				else
				{
					return null;
				}
			
			}
			catch(IOException e)
			{
				throw new IllegalStateException(e);
			}
		
		//throw new UnsupportedOperationException("Implement remove for Module 5");
	}

	
	
	
	
	@Override
	public int degree() {
		
		return columns().size();
		
	}

	@Override
	//MIGHT BE WRONG!!!
	public int size() {
		
		try {
			var metadata= rootDir.resolve("metadata");
			var size= metadata.resolve("size");
			var in= new DataInputStream(Files.newInputStream(size));
			var sizeCount= in.readInt(); //reads in the size from the size file
			return sizeCount;
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
		
		
		//throw new UnsupportedOperationException("Implement size for Module 5");
	}

	@Override
	public int hashCode() {
		

		try {
			var metadata= rootDir.resolve("metadata");
			var fingerprint= metadata.resolve("fingerprint");
			DataInputStream in = new DataInputStream(Files.newInputStream(fingerprint));
			var fingerPrintCount= in.readInt(); //reads in the fingerprint from the fingerprint file
			return fingerPrintCount;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
		//throw new UnsupportedOperationException("Implement table's hashCode for Module 5");
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Table &&
			this.hashCode() == obj.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
		//copied from the clear method [shown in class last time]
		//needs a try catch for check exceptipn
		
		try {
			var data= rootDir.resolve("data"); 
			return Files.walk(data) //walks the entire hierarchy under data, including data itself
				.filter(path -> !Files.isDirectory(path)) //only iterates through the files
				.map(path -> returnRowObject(path)) //passes in the file into the helper method, which returns a Row object
				.iterator();
			
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
		
		//throw new UnsupportedOperationException("Implement iterator for Module 5");
	}
	
	//helper method for iterator 
	public Row returnRowObject(Path path)
	{
		try {
			var in= new DataInputStream(Files.newInputStream(path));
			
			String key="";
			List<Object> fields= new ArrayList<> ();
			
				while( in.available()>0 )
				{
					var flag= in.readByte();
					
					//flag indicates what type we read in
					
					if(flag== -3)
					{
						var current= in.readUTF();
						fields.add(current);
					}
					else if(flag == -5)
					{
						var current= in.readInt();
						fields.add(current);
						
					}
					else if(flag == -6)
					{
						var current= in.readDouble();
						fields.add(current);	
					}
					else if(flag == -4)
					{
						var current= in.readBoolean();
						fields.add(current);
					}
					else if(flag == -2)
					{
						fields.add(null);
					}
					else if(flag>=0)
					{
						key= in.readUTF();
						
					}
					else
					{
						throw new IllegalStateException("Undesignated Flag!");
					}
					
				}
				
				Row row= new Row(key, fields); //creates a Row object
				return row; //returns the row Object
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException(e);
		}

	}

	@Override
	public String name() {
		String pathName= rootDir.getFileName() + "";
		return pathName;

	}

	@Override
	public List<String> columns() {
		
		try {
			var metadata= rootDir.resolve("metadata");
			var columns= metadata.resolve("columns");
			var in= new DataInputStream(Files.newInputStream(columns));
	
			List<String> columnsList= new ArrayList();
			//gets the columns by reading the column File
			while(in.available()>0)
			{
				columnsList.add(in.readUTF());
			}
			in.close();
			return columnsList;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public String toString() {
		return toPrettyString();
	}
}
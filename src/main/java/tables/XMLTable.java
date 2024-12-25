package tables;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Element;

import model.DataTable;
import model.FileTable;
import model.Row;
import model.Table;

public class XMLTable implements FileTable {
	private static final Path base = Paths.get("db", "tables");
	private final Path xmlFile;

	private static final DocumentFactory helper = DocumentFactory.getInstance();
	private final Document doc;

	public XMLTable(String name, List<String> columns) {
		try {
			Files.createDirectories(base);

			xmlFile = base.resolve(name + ".xml");
			if (Files.notExists(xmlFile))
				Files.createFile(xmlFile);

			doc = helper.createDocument();

			var root= doc.addElement("table");
			
			var metadata= root.addElement("metadata"); //root adds metadata object
			var columnsRow= metadata.addElement("columns"); //metadata adds columns object
			//columns add its elements
			for(int i=0; i<columns.size(); i++)
			{
				var col= columnsRow.addElement("column").addAttribute("name", columns.get(i));
			}
			var data= root.addElement("data"); //root adds data objects
			
			
			flush();
			//initializes the file structure 
			
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	public XMLTable(String name) {
		try {
			xmlFile = base.resolve(name + ".xml");

			if (Files.notExists(xmlFile))
				throw new IllegalArgumentException("Missing table: " + name);

			doc = new SAXReader().read(xmlFile.toFile());
		}
		catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void clear() {
		
			//deletes all the elements other than the root
			var root= doc.getRootElement(); //obtain the root element
			var data= root.element("data");
			data.clearContent(); //deletes all the data content
			
			flush(); //actually writes it into storage		
		
	}

	@Override
	//one liner
	public void flush() {
		try {
			var writer = new XMLWriter(new FileWriter(xmlFile.toFile()), OutputFormat.createPrettyPrint());
	        writer.write(doc);
	        writer.close();
	        
	      //OutputFormat format= OutputFormat.createPrettyPrint();
	      //writer= new XMLWriter(System.out, format);
	      //writer.write(doc);
	       //writer.close();       
	  
		}
		catch (IOException e) {
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
		
		
		var root= doc.getRootElement(); //obtains the root element 
		var data= root.element("data"); //obtains the data element
		var dataElements= data.elements("row"); //obtains row elements
		//loops through the rows
		for(int index=0; index<dataElements.size(); index++)
		{
			var currentRow= dataElements.get(index); //obtains the currentRow element
			var currentKey= currentRow.element("key").getText(); //obtains the current key
			List<Object> oldFields= new ArrayList<> (); //prepares an oldFields to be returned 
			var oldFieldsElementList= currentRow.elements("field"); //obtains a List of the elements that contain "field" attribute
			//HIT
			if(currentKey.equals(key)) 
			{
				//loops through the field elements of the row
				for(int i=0; i<fields.size(); i++)
				{
					//updating the oldFields
					var currentFieldElement= oldFieldsElementList.get(i); //obtains the currentField
					oldFields.add(helper(currentFieldElement.attributeValue("type", "="), currentFieldElement.getText())); //obtains the old fields
					
					//actually putting it in
					var currentType= typeHelper(fields.get(i)); //calls helper method
					currentFieldElement.setAttributeValue("type", currentType); //updates the attribute value
					currentFieldElement.setText(fields.get(i)+""); // updates the text
				}
				flush(); 
				return oldFields;
			}
			
		}
		
		//MISS
		var row= data.addElement("row"); //adds row element
		row.addElement("key").addAttribute("type", "String").addText(key); //adds the attribute and text to the key
		for(int i=0; i<fields.size(); i++) //adds the fields, and their corresponding attributes and texts
		{
			var currentItem= fields.get(i);
			row.addElement("field").addAttribute("type", (typeHelper(currentItem))).addText(currentItem+"");
			
		}
		flush();
		return null; 
		//throw new UnsupportedOperationException("Implement put for Module 4b");
	}

	
	
	
	@Override
	public List<Object> get(String key) {
		var root= doc.getRootElement(); //obtains the root element 
		var data= root.element("data"); //obtains the data element
		var dataElements= data.elements("row"); //obtains row elements
		//loop through the row elements:
		for(int index=0; index<dataElements.size(); index++)
		{
			var currentRow= dataElements.get(index); //obtains the currentRow element
			var currentKey= currentRow.element("key").getText(); //obtains the current key
			List<Object> oldFields= new ArrayList<> (); //prepares an oldFields to be returned 
			var oldFieldsElementList= currentRow.elements("field"); //obtains a List of the elements that contain "field" attribute
			//HIT
			if(currentKey.equals(key)) 
			{
				//update the fields
				for(int i=0; i<oldFieldsElementList.size(); i++)
				{
					var currentElement= (oldFieldsElementList.get(i));
					var type= currentElement.attributeValue("type", "="); //gets the type
					String fieldValue= currentElement.getText(); //gets the value stored in the fields
					oldFields.add(helper(type, fieldValue)); //obtains the old fields
				}
				return oldFields;
			}
		}
		return null;
		//throw new UnsupportedOperationException("Implement get for Module 4b");
	}

	@Override
	public List<Object> remove(String key) {
		var root= doc.getRootElement(); //obtains the root element 
		var data= root.element("data"); //obtains the data element
		var dataElements= data.elements("row"); //obtains row elements
		//loops through the row elements:
		for(int index=0; index<dataElements.size(); index++)
		{
			var currentRow= dataElements.get(index); //obtains the currentRow element
			var currentKey= currentRow.element("key").getText(); //obtains the current key
			List<Object> oldFields= new ArrayList <> (); //prepares an oldFields to be returned 
			var oldFieldsElementList= currentRow.elements("field"); //obtains a List of the elements that contain "field" attribute
			//HIT
			if(currentKey.equals(key)) 
			{
				//update the fields
				for(int i=0; i<oldFieldsElementList.size(); i++)
				{
					var currentElement= (oldFieldsElementList.get(i));
					var type= currentElement.attributeValue("type", "="); //gets the type
					String fieldValue= currentElement.getText(); //gets the value stored in the fields
					oldFields.add(helper(type, fieldValue)); //obtains the old fields
				}
				data.remove(currentRow); //removes the entire row
				flush(); 
				return oldFields;
			}
		}
		return null;
		//throw new UnsupportedOperationException("Implement remove for Module 4b");
	}

	
	@Override
	public int degree() {
		
		var root= doc.getRootElement();
		var metaData= root.element("metadata");
		var mainColumn= metaData.element("columns");
		var colElements= mainColumn.elements();
	
		return colElements.size();
	}

	@Override
	public int size() {
		var root= doc.getRootElement();
		var data= root.element("data");
		var rowList= data.elements("row");
		return rowList.size();
	}

	@Override
	public int hashCode() {
		var root= doc.getRootElement(); //gets the root element
		var data= root.element("data"); //gets the data element
		var rows= data.elements("row"); //gets the rows elements List
		int hashCode=0;
		//loops through the row elements List:
		for(int i=0; i<rows.size(); i++)
		{
			var currentRow= rows.get(i); //gets the current Row
			var currentKey= currentRow.element("key").getText(); //obtains the current key
			var currentFieldsList= currentRow.elements("field"); //obtains the current Fields
			List<Object> fieldsFinalList= new ArrayList <>(); //prepares an ArrayList
			for(int index=0; index<currentFieldsList.size(); index++) //adds currentElementFields into array list!!
			{
				var currentElement= currentFieldsList.get(index); 
				var type= currentElement.attributeValue("type", "=");
				String fieldValue= currentElement.getText();
				fieldsFinalList.add(helper(type, fieldValue));
			}
			
			Row row= new Row(currentKey, fieldsFinalList); //makes a row object
			//fingerprint of Row
			hashCode= hashCode+ row.hashCode(); //gets the Hash Code of the row object
		}
		return hashCode;
		//throw new UnsupportedOperationException("Implement table's hashCode for Module 4b");
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Table &&
			this.hashCode() == obj.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
		int max= size();
		var root= doc.getRootElement();
		var data= root.element("data");
		var rows= data.elements("row");
		
		List<Row> rowList= new ArrayList<> ();
		//loops through rows List:
		for(int i=0; i<rows.size(); i++)
		{
			var currentRow= rows.get(i); //gets the current Row
			var currentKey= currentRow.element("key").getText(); //obtains the current key
			var currentFieldsList= currentRow.elements("field"); //obtains the current Fields
			List<Object> fieldsFinalList= new ArrayList <>(); //prepares an ArrayList
			for(int index=0; index<currentFieldsList.size(); index++) //adds currentElementFields into array list!!
			{
				var currentElement= currentFieldsList.get(index); 
				var type= currentElement.attributeValue("type", "=");
				String fieldValue= currentElement.getText();
				fieldsFinalList.add(helper(type, fieldValue));
			}
			
			Row row= new Row(currentKey, fieldsFinalList); //makes a row object
			rowList.add(row);
		}
		
		return rowList.iterator();
		//throw new UnsupportedOperationException("Implement iterator for Module 4b");

	}

	@Override
	public String name() {
		String pathName= xmlFile.getFileName() + "";
		String name= pathName.substring(0,pathName.indexOf("."));
		return name;
	}

	@Override
	public List<String> columns() {
	
		List<String> columns= new ArrayList <String> (); //makes a list of columns
		var root= doc.getRootElement(); //obtains the root object
		var metadata= root.element("metadata"); //obtains the metadata object
		var mainColumn= metadata.element("columns"); //obtains the columns object
		
	
		var columnNodes= mainColumn.elements(); //obtains the elements under the columns object
		for(var col: columnNodes) //loops through those elements
		{
			columns.add(col.attributeValue("name")); //adds the name= "" to the columns list
		}
		
		return columns; //returns the columns list
		
	}

	@Override
	public String toString() {
		return toPrettyString();
	}
	
	//returns the ACTUAL TYPE of the given field
	public Object helper(String type, String fieldValue)
	{
		if(type.equals("String"))
		{
			return fieldValue;
		}
		else if(type.equals("Boolean"))
		{
			return Boolean.parseBoolean(fieldValue);
		}
		else if(type.equals("Integer"))
		{
			return Integer.parseInt(fieldValue);
		}
		else if(type.equals("Double"))
		{
			return Double.parseDouble(fieldValue);
		}
		else if(type.equals("null"))
		{
			return null;
		}
		return fieldValue;
	}
	
	//returns the type of the given field
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
		else
		{
			return "null";
		}
					
	}
	
	//will be like year released: "2017"
	//string column and Object value
	public XMLTable filter(String columnName, Object value)
	{
		//preparation step:
		var root= doc.getRootElement();
		var columns= columns();
		
		var data= root.element("data");
		var rows= data.elements("row");
		
		
		//gets the column index
		int columnIndex=0;
		for(int c=0; c<columns.size(); c++)
		{
			var currentCol= columns.get(c);
			if(currentCol.equals(columnName))
			{
				columnIndex=c;
				break;
			}
		}
		
		XMLTable table= new XMLTable(name(), columns()); //instantiates a new table
		
		for(int i=0; i<rows.size(); i++) //loops through all the rows
		{
			var currentRow= rows.get(i); //gets the currentRow
			var currentRowFields= currentRow.elements("field"); //gets the FIELDS (not the key)
			var desiredField= currentRowFields.get(columnIndex-1); //goes to the desired index, must subtract one, because key is NOT considered in the currentRowFields
			
			//checks if the desiredField == value
			if((desiredField.getText()).equals(value+""))
			{
				List<Object> fields= new ArrayList<> (); //prepare a list of fields to PUT into the table
				for(int index=0; index<currentRowFields.size(); index++) //obtains all the fields and translates them into List<Object> type 
				{
					//applies similar process used in put, remove, and get
					var currentElement= currentRowFields.get(index);
					var type= currentElement.attributeValue("type");
					String fieldValue= currentElement.getText();
					fields.add(helper(type, fieldValue));
				}
				table.put(currentRow.element("key").getText(), fields); //puts the key and the fields into the table
			}
			
		}
		return table; //returns the table!
		
	}
	
	
}
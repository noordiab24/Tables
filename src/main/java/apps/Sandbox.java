package apps;

import java.util.Arrays;
import java.util.List;

import tables.BinaryTable;
import tables.CSVTable;
import tables.HashTable;

public class Sandbox {
	public static void main(String[] args) {
		/*
		Table example1 = new SymbolTable("variables", List.of("name", "type", "value", "comment"));
		example1.put("B", Arrays.asList("string", "World!", null));
		example1.put("A", Arrays.asList("string", "Hello,", null));
		example1.put("x", List.of("number", 1, "first coordinate"));
		example1.put("y", List.of("number", 20, "second coordinate"));
		example1.put("z", List.of("number", 300, "third coordinate"));
		example1.put("k", List.of("boolean", true, "lowercase name"));
		example1.put("K", List.of("boolean", false, "uppercase name"));
		System.out.println(example1);
		*/
		
		//My favorite TV Shows 
		System.out.println("Table 1 Description: Lists my favorite TV Shows, providing the name, number of times I've watched the show, and my personal rating for the show.");
		CSVTable example2= new CSVTable("TV Shows", List.of("Name","# Of Times Watched","Rating"));
		example2.put("Gossip Girl", Arrays.asList("6", "100/10"));
		example2.put("The OC", Arrays.asList("1", "8/10" ));
		example2.put("Gilmore Girls", Arrays.asList( "3", "8/10"));
		example2.put("Downton Abbey", Arrays.asList( "2", "9/10"));
		example2.put("Emily in Paris", Arrays.asList( "2", "7/10"));
		example2.put("Young Sheldon", Arrays.asList( "1", "4/10"));
		example2.put("The Vampire Diaries", Arrays.asList( "1", "6/10"));
		example2.put("The Outer Banks", Arrays.asList( "1", "7/10"));
		example2.put("One Tree Hill", Arrays.asList( "2", "5/10"));
		System.out.println(example2);
		
		//Favorite Songs 
		System.out.println("Table 2 Description: Lists my favorite songs. The table provides the name of each song, the artist of the song, release year, and the number of years the song has been on my favorites list.");
		HashTable example3= new HashTable("Favorite Songs", List.of("Name", "Artist", "# Of Years In My Fav List", "Release Year"));
		example3.put("BIRDS OF A FEATHER", Arrays.asList( "Billie Eilish", "0.5", "2024"));
		example3.put("Video Games", Arrays.asList( "Lana Del Rey", "4", "2012"));
		example3.put("Vienna", Arrays.asList( "Billie Joel", "0.5", "1977"));
		example3.put("feelslikeimfallinginlove", Arrays.asList( "ColdPlay", "0.5", "2024"));
		example3.put("august", Arrays.asList( "Taylor Swift","3", "2020"));
		example3.put("28", Arrays.asList( "Zach Bryan", "0.25", "2024"));
		example3.put("Out Of The Woods (Taylor's Version)", Arrays.asList( "Taylor Swift", "1", "2023"));
		example3.put("What Was I Made For?", Arrays.asList( "Billie Eilish", "1", "2023"));
		example3.put("Viva La Vida", Arrays.asList( "Coldplay", "7", "2008"));
		example3.put("All Too Well (10 Minute Version)", Arrays.asList( "Taylor Swift", "4", "2021"));
		example3.put("Overdrive",  Arrays.asList("Post Malone", "1", "2023"));
		example3.put("Getaway Car", Arrays.asList( "Taylor Swift", "2", "2017"));
		example3.put("You Belong With Me (Taylor's Version)", Arrays.asList( "Taylor Swift", "2", "2021"));
		example3.put("C'est la vie", Arrays.asList( "Khaled", "4", "2012"));
		System.out.println(example3);
		
		//Pinterest Posts
		System.out.println("Table 3 Description: In my free time, I enjoy pinning and posting on my Pinterest boards. This table lists out the preformance of my posts, providing the name, date of publication, reaction count, impression count, monthly impression rate, and the pin clicks of each post. ");
		BinaryTable example4= new BinaryTable("Pinterest Posts", List.of("Name of Post", "Date of Publication","Reactions", "Impression Count", "Monthly Impression Rate", "Pin Clicks"));
		example4.put("Taylor Swift Concert", Arrays.asList( "3/2/2024","1", "14", "-67%", "1"));
		example4.put("Tennis Photoshoot", Arrays.asList( "6/1/2020","0", "7", "-50%", "0"));
		example4.put("Baking With Friends", Arrays.asList( "6/23/2020", "0", "5", "+150%", "2"));
		example4.put("Bday Picnic", Arrays.asList( "6/10/2022", "0", "22", "+15%", "0"));
		example4.put("Istanbul", Arrays.asList("8/9/2022","0","4","+300%","0"));
		example4.put("Brunch Outfit", Arrays.asList("8/9/2022","0","8","+60%","0"));
		example4.put("Beach outfit inspo", Arrays.asList("8/9/2022","1","8","+100%","0"));
		example4.put("Gossip Girl and Gilmore Girls Halloween Costume inspo", Arrays.asList("8/13/2022","1","192","+93%","3"));
		example4.put("Sunset Pic!", Arrays.asList( "9/15/2023", "0","8","+166%","2"));
		example4.put("California Aesthetic", Arrays.asList( "5/18/2024","1","19","+111%","1"));
		example4.put("Cats!!", Arrays.asList( "6/9/2024", "0","21","+31%","2"));
		example4.put("Alhambra Spain ", Arrays.asList("6/19/2024", "0","23","+91%","3"));
		example4.put("Cordoba Spain", Arrays.asList( "8/31/2024","0","34","0%","6"));
		example4.put("Pretty Nature", Arrays.asList( "5/9/2024","2","27","+200%","1"));
		System.out.println(example4);
		
		
		
		System.out.println("Table 4 descriptions is the same as the Table 1 description.");
		BinaryTable example5= new BinaryTable("TV Shows");
		example5.put("Gossip Girl", Arrays.asList("6", "100/10"));
		example5.put("The OC", Arrays.asList("1", "8/10" ));
		example5.put("Gilmore Girls", Arrays.asList( "3", "8/10"));
		example5.put("Downton Abbey", Arrays.asList( "2", "9/10"));
		example5.put("Emily in Paris", Arrays.asList( "2", "7/10"));
		example5.put("Young Sheldon", Arrays.asList( "1", "4/10"));
		example5.put("The Vampire Diaries", Arrays.asList( "1", "6/10"));
		example5.put("The Outer Banks", Arrays.asList( "1", "7/10"));
		example5.put("One Tree Hill", Arrays.asList( "2", "5/10"));
		System.out.println(example5);
		

		

	}
}

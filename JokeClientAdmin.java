

/*--------------------------------------------------------

1. Name / Date: Giries Hattar 4/17/2020

2. Java version used, if not the official version for the class:

same as class version

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java JokeClient.java JokeClientAdmin.java


4. Precise examples / instructions to run this program:


In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

to change modes in JokeClientAdmin enter "joke" for joke mode without "", "prov" for proverbs mode without "", "maint" for maintenance mode without ""
All acceptable commands are displayed on the various consoles.



5. List of files needed for running the program.

 
 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:

I got everything to work except for the secondary server


----------------------------------------------------------*/
import java.io.*;
import java.net.*;

public class JokeClientAdmin { // admin client class similar to JokeClient with slight modifications 

	public static void main(String args[]) {
		String serverName;
		if (args.length < 1)
			serverName = "localhost"; // if there is no user input use "localhost" as default
		else
			serverName = args[0]; // else use user input...
		System.out.println("Giries Hattar's Joke Client Admin, 1.0\n");
		System.out.println("Using server: " + serverName);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // format the input

		try {
			String option; // allocating a string object

			do {
				System.out.print(
						"To choose Joke , Proverb or Maintenance type joke/prov/maint and press enter. to quit type (quit) and press enter: "); 
				option = in.readLine().toLowerCase(); // option = the first line from input 'in' and get the lower case
														// version of it.
				if (option.indexOf("quit") < 0) // change the server mode if user did not input 'quit'
				{
					System.out.println("Your wish is my command");
					if(option.equals("maint") || option.equals("joke") || option.equals("prov"))
					changeMode(option, serverName);
					else {System.out.println("Incorrect input: " + option + " please try again");}
					
				} 

			} while (option.indexOf("quit") < 0); // if user enters 'quit' break the loop
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	static void changeMode(String option, String serverName) { // this method will connect us to the server on port 5050 and request for it to change it's mode to the mode inserted by admin 'option'

		Socket sock; // allocating a socket object
		BufferedReader fromServer; // format input from server
		PrintStream toServer; // format output 
		String textFromServer; // allocating a string object

		try {
			sock = new Socket(serverName, 5050); // Open connection to server port
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); // create input stream
			toServer = new PrintStream(sock.getOutputStream()); // create output stream

			toServer.println(option); // send option chosen to Joke Server
			toServer.flush();

			textFromServer = fromServer.readLine(); // set textFromServer = to input from server
			if (textFromServer != null) { // if there is an input from server print it
				System.out.println(textFromServer);
			}

			System.out.println();
			sock.close(); // close connection
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}

}

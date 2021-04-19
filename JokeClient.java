
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



----------------------------------------------------------*/
import java.io.*;  
import java.net.*; 
import java.util.*;
public class JokeClient {
	private static final double UID = Math.random(); // uid for client
	
	public static int jokeNum = -1;
	public static int provNum = -1;
	public static String server_Mode = "joke";

	public static void main(String args[]) {
        String serverName;
		if (args.length < 1)
			serverName = "localhost"; // if there is no user input use "localhost" as default
		else
			serverName = args[0]; // else use user input...
		System.out.println("Giries Hattar's Joke Client, 1.0\n");
		System.out.println("Using server: " + serverName + ", Port: 4545");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // format the input

		try {
            String quitOrCnt; // allocating a string object
            String userName; // allocating a string object
            System.out.println("Please enter your name: ");
            userName = extracted().nextLine(); // read user input and put value as userName

			do {
				System.out.print(userName + ", press enter for joke or proverb, to end please type (quit) and then press enter: "); // asks user to enter host name																		// or IP address
				System.out.flush(); // flush out the "print" stream
				quitOrCnt = in.readLine(); // name = the first line from input 'in'
				if (quitOrCnt.indexOf("quit") < 0) // get the joke or proverb if user did not input 'quit'
					getJokeOrProverb(userName, serverName);
			} while (quitOrCnt.indexOf("quit") < 0); // if user enters 'quit' break the loop
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	private static Scanner extracted() {
		return new Scanner(System.in);
	}
	
	

	static void getJokeOrProverb(String name, String serverName) {
		Socket sock; // allocating a socket object
		BufferedReader fromServer; // format input from server
		PrintStream toServer; // format output 
		String textFromServer; // allocating a string object
		String textToPrint;
		
		/* put something that will tell the server who you are and what jokes/proverbs you have seen
		 * like an index. "hey server retrieve my state such as a 'cookie' or UID of the state"*/

		try {
			sock = new Socket(serverName, 4545); // Open connection to server port

	
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); // create input stream
			toServer = new PrintStream(sock.getOutputStream()); // create output stream

			
			toServer.println(name + "." + jokeNum + ">" + provNum + "<" + server_Mode); //send client information to server 
			toServer.flush(); // flush out the server
			


			for (int i = 1; i <= 2; i++) {
				textFromServer = fromServer.readLine(); // set textFromServer = to input from server
				
				// here we are formating the inputed data from server, we will retrieve the cookies sent from the server for our client to save the servers state 
				if(textFromServer != null && textFromServer.length() > 0) {
					if(textFromServer.startsWith("J") || textFromServer.startsWith("P") ) {
						textToPrint = textFromServer.substring(0, textFromServer.indexOf("*"));
						jokeNum = Integer.parseInt(textFromServer.substring(textFromServer.indexOf("*") + 1, textFromServer.indexOf(">")));
						provNum = Integer.parseInt(textFromServer.substring(textFromServer.indexOf(">") + 1, textFromServer.indexOf("<")));
						server_Mode = textFromServer.substring(textFromServer.indexOf("<") + 1, textFromServer.length());
						System.out.println(textToPrint);
					}
					else
					if (textFromServer != null)
						System.out.println(textFromServer); // print input
				}
				
					
				
			}
			sock.close(); // close the connection to the server socket
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
}

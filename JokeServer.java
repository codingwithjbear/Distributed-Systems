
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


class Worker extends Thread {
	Socket sock; // allocating a socket object
	public static int jokeNum = -1;
	public static int provNum = -1;

	public static String[] jokeArr = { "JA  Why did the chicken cross the road? To get to the other side!",
			"JB  Knock Knock? Who's there? Orange. Orange who? Orange you glad I didn't say banana",
			"JC  Knock Knock? Who's there? Tank. Tank who? You're Welcome!",
			"JD  Knock Knock? Who's there? Says. Says who? Says me!" };

	public static String[] provArr = { "PA  Home is where the heart is", "PB  Be patient good things will come",
			"PC  Give me a computer and I'll make a program", "PD  Practice Practice Practice and you will get it" };

	Worker(Socket s) { // constructor
		sock = s;
	}

	public void run() { // associated with start and it will run when the class starts

		PrintStream out = null; // format output 'out'
		BufferedReader in = null; // format input 'in'
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // in is given through the server
																					// socket
			out = new PrintStream(sock.getOutputStream()); // gets output from listening server

			try {
				String input;
				String name;
				String joken;
				String provn;
				
				// formating the input from the client to set the values for the above variables 
				input = in.readLine();
				name = input.substring(0, input.indexOf("."));	
				joken = input.substring(input.indexOf(".") + 1, input.indexOf(">"));
				provn = input.substring(input.indexOf(">") + 1, input.indexOf("<"));
				
				
				String server_mode = input.substring(input.indexOf("<") + 1, input.length()); // client provided server mode
			
				int jokeI = Integer.parseInt(joken); // parse the user input for the integer value of joken
				int provI = Integer.parseInt(provn); // same as above but for provn
				
				// setting our client data cookies yum yum
				jokeNum = jokeI;
				provNum = provI;
				
				if(!JokeServer.adminDID) {changeMode(out, server_mode);} // if the admin didn't change the server mode set it to the one provided by client
				printjokeOrProverb(name, out);
			} catch (IOException x) { // catch any exception if there is an error with the socket
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); // close the connection to the server socket
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	static void printjokeOrProverb(String name, PrintStream out) { // method for printing out a joke or proverb also can say that the server is in maintenance mode
	
		// getting current server mode
		String server_mode = null;
		if(JokeServer.provMode) server_mode = "prov";
		if(JokeServer.jokeMode) server_mode = "joke";
		if(JokeServer.maintMode) server_mode = "maint";
		
		
		if (!JokeServer.maintMode) { // if the server is not undergoing maintenance continue to print/send joke/proberb
			if (JokeServer.jokeMode) { // if in jokeMode print/send joke
				jokeNum++;
				if (jokeNum < 4) {
				
					// formating the output Joke string
					String start = jokeArr[jokeNum].substring(0, 3) + name + ":";
					out.println(start + " " + jokeArr[jokeNum].substring(3) + "*" + jokeNum + ">" + provNum + "<" + server_mode); // formats joke and sends out current sever state for client to save "cookies"  
					out.flush(); // send one line at a time to the client 
					if (jokeNum == 3) { // if at last joke tell the client
						out.println("Last JOKE! get ready for random ordered jokes :)"); 
					}
				} else {
					int tmp = (int) Math.floor(Math.random() * 4); // creating a random integer from 0 upto but not including 4 to retrieve random jokes from jokeArr
					//out.println(name + " " + jokeArr[tmp]);
					
					// formating the output JOKE 
					String start = jokeArr[tmp].substring(0, 3) + name + ":"; 
					out.println(start + " " + jokeArr[tmp].substring(3) + "*" + jokeNum + ">" + provNum + "<" + server_mode ); // formats random joke and sends out current sever state for client to save "cookies"
					out.flush();
				}
			}
			if (!JokeServer.jokeMode && JokeServer.provMode) { // if not in jokeMode but in provMode send/print proverb
				provNum++;
				if (provNum < 4) {
					// formating the output proverb string
					String start = provArr[provNum].substring(0, 3) + name + ":";
					out.println(start + " " + provArr[provNum].substring(3) + "*" + jokeNum + ">" + provNum + "<" + server_mode); // formats proverb and sends out current sever state for client to save "cookies"
					out.flush();
					if(provNum == 3) { // if at last proverb tell the client
						out.println("Last Proverb! get ready for random ordered proverbs :)");
					}
				} else {
					int tmp = (int) Math.floor(Math.random() * 4); // creating a random integer from 0 upto but not including 4 to retrieve random proverbs from provArr
					// formating string output for proverb
					String start = provArr[tmp].substring(0, 3) + name + ":";
					out.println(start + " " + provArr[tmp].substring(3) + "*" + jokeNum + ">" + provNum + "<" + server_mode); // formats the random proverb and sends out current sever state for client to save "cookies"
					out.flush();
				}
			}
		} else { // if JokeServer is in maintenance mode don't give joke/proverb and tell client that the server is undergoing maintenance
			out.println("Server is currently undergoing maintenance");
			out.flush();
		}
	
			
		
	}
	
	private static void changeMode(PrintStream out, String option) {
		if (option.equals("joke")) { // if the option given by user is joke set the jokeMode to TRUE
			JokeServer.provMode = JokeServer.maintMode = false; // set the other modes to flase
			JokeServer.jokeMode = true;

		}
		if (option.equals("prov")) { // if the option given is prov set the provMode to TRUE
			JokeServer.maintMode = JokeServer.jokeMode = false; // set the other modes to flase
			JokeServer.provMode = true;
		
		}
		if (option.contentEquals("maint")) { // if the option given is maint set the maintMode to TRUE
			JokeServer.provMode = JokeServer.jokeMode = false; // set the other modes to flase
			JokeServer.maintMode = true;
		
		}
	}

}


class AdminLooper implements Runnable {
	public static boolean adminControlSwitch = true;

	public void run() {
		System.out.println("In the admin looper thread");

		int q_len = 6;
		int port = 5050; // We are listening at a different port for Admin client
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(port, q_len);
			while (adminControlSwitch) {
				
				sock = servsock.accept();
				new AdminWorker(sock).start();
			}

		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

class AdminWorker extends Thread {
	Socket sock;

	public AdminWorker(Socket s) {
		sock = s;
	}

	public void run() { // associated with start and it will run when the class starts

		PrintStream out = null; // format output 'out'
		BufferedReader in = null; // format input 'in'
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // in is given through the server socket
			out = new PrintStream(sock.getOutputStream()); // gets output from listening server

			try {
				String option;
				option = in.readLine();
				
				changeMode(out, option); // call changeMode and alter the server mode base on admin option

			} catch (IOException x) { // catch any exception if there is an error with the socket
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); // close the connection to the server socket
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	private static void changeMode(PrintStream out, String option) {
		JokeServer.adminDID = true;
		if (option.equals("joke")) { // if the option given by user is joke set the jokeMode to TRUE
			JokeServer.provMode = JokeServer.maintMode = false; // set the other modes to flase
			JokeServer.jokeMode = true;

			out.println("Joke mode activated"); 
		}
		if (option.equals("prov")) { // if the option given is prov set the provMode to TRUE
			JokeServer.maintMode = JokeServer.jokeMode = false; // set the other modes to flase
			JokeServer.provMode = true;
			out.println("Proverbs mode activated");
		}
		if (option.contentEquals("maint")) { // if the option given is maint set the maintMode to TRUE
			JokeServer.provMode = JokeServer.jokeMode = false; // set the other modes to flase
			JokeServer.maintMode = true;
			out.println("Maintenance mode activated");
		}
	}
}



public class JokeServer { // Declaring the server class
	public static boolean adminDID = false; // tells us if admin was and made a change to server mode...
	public static boolean jokeMode = true; // joke Mode initially set to true
	public static boolean maintMode = false; // maintenance mode initially false
	public static boolean provMode = false;// proverb mode initially false

	
	
	public static void main(String a[]) throws IOException {
		int q_len = 6; // if it gets more than 6 queue it will ignore the ones after...
		int port = 4545; // don't go below 1025
		Socket sock; // allocating a socket object
		
		AdminLooper AL = new AdminLooper(); // create a different thread "clarks comment"
		Thread t = new Thread(AL);
		t.start(); // ...and start it, waiting for administration input

		ServerSocket servsock = new ServerSocket(port, q_len); // allocating a ServerSocket object 'servsock'

		System.out.println("Giries Hattar's Joke server 1.0 starting up, listening at port 4545.\n");
		while (true) {
			sock = servsock.accept(); // wait and then accept the next client connection
			new Worker(sock).start(); // handle the new connection
		}
	}
}

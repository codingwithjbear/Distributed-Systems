
/*--------------------------------------------------------

1. Name / Date:  Giries Hattar 

2. Java version used, if not the official version for the class:

same as class version

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java


4. Precise examples / instructions to run this program:


In separate shell windows:

> java MyWebServer
start fireFox and go to localhost:2540




5. List of files needed for running the program.

 
 a. MyWebServer.java

5. Notes:

This was fun to play with!


----------------------------------------------------------*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

class WebWorker extends Thread { // declaring the worker thread for the web server this is where the magic
									// happens
	Socket sock;

	WebWorker(Socket s) { // constructor for the worker class
		sock = s; // sock creates a socket connection
	}

	public void run() {

		PrintStream out = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());

			try {
				String name, request, fileName, fileType, version;
				String[] data;

				/*
				 * formating the data from the input stream if name != null (this is to avoid
				 * the null pointer exception that may occur on separate threads)
				 */
				name = in.readLine();
				if (name != null) {
					data = name.split(" ");
					request = data[0];
					fileName = data[1];
					version = data[2];

					/*
					 * the following is used to get MIME types and show the file/directory adapted
					 * from ideas gained by browsing through MyWebServerTips
					 */
					if (request.equals("GET")) {
						if (!fileName.contains(".ico")) {
							if (fileName.contains("..") || fileName.contains("/.ht") || fileName.endsWith("~")) {

								/*
								 * formating and sending output for forbidden request also sending server log
								 * info to server console
								 */

								String send = "<html>" + "<pre> <h1>" + "403 Forbidden" + " </h1> " + "<h2>"
										+ "You don't have permission to access the requested URL." + "</h2></html>";

								System.err.println(new Date() + " [" + sock.getInetAddress().getHostAddress() + ":"
										+ sock.getPort() + "] "
										+ "You don't have permission to access the requested URL.");
								System.out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + send.length()
										+ " Content-Type: " + "text/html" + "\r\n\r\n");

								out.print("HTTP/1.1 200 OK\r\n" + "Content-Length: " + send.length() + " Content-Type: "
										+ "text/html" + "\r\n\r\n");
								out.flush();
								out.print(send);
								out.flush();
							} else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
								fileType = "text/html";
								show(fileName, fileType, out, version, false);
							} else if (fileName.endsWith(".txt") || fileName.endsWith(".java")) {
								fileType = "text/plain";
								show(fileName, fileType, out, version, false);
							} else if (fileName.endsWith("/") && !fileName.endsWith("../")) {
								fileType = "text/html";
								show(fileName, fileType, out, version, true);
							} else if (fileName.endsWith(".gif")) {
								fileType = "image/gif";
								show(fileName, fileType, out, version, false);
							} else if (fileName.endsWith(".class")) {
								fileType = "application/octet-stream";
								show(fileName, fileType, out, version, false);
							} else if (fileName.contains(".fake-cgi")) {
								fileType = "text/html";
								addnums(out, fileType, fileName);

							}
						}

						sock.close(); // this will close the connection to the server socket
					}
				}

			} catch (IOException x) { // if there is an error with the socket send a msg to user/console
				System.out.println("Server read error");
				x.printStackTrace();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	/*
	 * modified version of readFile, to show directory and files also utilized hints
	 * gained from the MyWebServerTips resource 150 line web server may be of some
	 * background us
	 */
	static void show(String fileName, String fileType, PrintStream out, String version, boolean dir)
			throws IOException {
		/* if this is a directory open and show it */
		if (dir) {
			String firstDir = "./" + fileName + "/";
			File f1 = new File(firstDir);

			File[] strFilesDirs = f1.listFiles();

			BufferedWriter show = new BufferedWriter(new FileWriter("index.html"));

			/*
			 * following was based on a example from
			 * "https://condor.depaul.edu/elliott/435/hw/programs/program-webserver.html":
			 * 
			 */
			show.write("<html>");// Declaring a html block
			show.write("<pre>");
			show.write("<h1> Index of " + f1.getAbsolutePath() + "</h1>");

			// find all files and directories under this path
			for (int i = 0; i < strFilesDirs.length; i++) {
				String tmpName = strFilesDirs[i].getName();
				if (strFilesDirs[i].isDirectory()) {
					// System.out.println("directory Name: " + tmpName);
					show.write("<a href=\"" + tmpName + "/\">" + tmpName + "</a><br>"); // make link for directory

				}

				else if (strFilesDirs[i].isFile()) {
					show.write("<a href=\"" + tmpName + "\">" + tmpName + "</a> <br>"); // make link for the file

				}
				show.flush();

			}
			show.write("</html>");// closing the html block

			File f2 = new File("index.html");
			InputStream IN = new FileInputStream(f2);

			String outPut = "HTTP/1.1 200 OK\r\n" + "Content-Length: " + f2.length() + " Content-Type: " + fileType
					+ "\r\n\r\n"; // formating output string
			out.print(outPut);// send data to browser
			out.flush(); // flush the browser stream
			System.out.println(outPut); // print data to server console

			// allocating bytes for directory
			byte[] fBytes = new byte[10000];
			int lenBytes = IN.read(fBytes);

			out.write(fBytes, 0, lenBytes); // write the directory bytes to the browser
			out.flush();
			/* close what was opened and delete the created html file */
			show.close();
			IN.close();
			f2.delete();

		} else { // if it's not a directory just open the file
			if (fileName.startsWith("/"))
				fileName = fileName.substring(1);
			File f1 = new File(fileName);
			InputStream IN = new FileInputStream(fileName);

			String outPut = "HTTP/1.1 200 OK\r\n" + "Content-Length: " + f1.length() + " Content-Type: " + fileType
					+ "\r\n\r\n";
			out.print(outPut); // send data to browser
			out.flush();
			System.out.println(outPut); // print data to server console

			// allocating space for file
			byte[] fBytes = new byte[10000];
			int lenBytes = IN.read(fBytes);

			out.write(fBytes, 0, lenBytes); // write the file onto the browser
			out.flush(); // flush the browser stream
			IN.close();
		}
	}

	/*
	 * in addnums we are formating the inputed "data" and calculating the sum of a +
	 * b then creating the html output for the browser
	 */
	private static void addnums(PrintStream out, String type, String data) {
		/*
		 * http://localhost:2540/cgi/addnums.fake-cgi?person=YourName&num1=4&num2=5 used
		 * this input to format the data into the required fields
		 */
		int ans, a, b;
		String userName, input;
		input = data.substring(data.indexOf("?") + 1);
		userName = input.substring(input.indexOf("=") + 1, input.indexOf("&"));
		input = input.substring(input.indexOf("&") + 1);
		a = Integer.parseInt(input.substring(input.indexOf("=") + 1, input.indexOf("&")));
		input = input.substring(input.indexOf("num2=") + 5);
		b = Integer.parseInt(input);
		ans = a + b;

		/*
		 * formating and sending data for browser to show and a log for the server
		 * console
		 */
		String outPut = "Dear " + userName + ", " + "the sum of " + a + " and " + b + " is " + ans;

		String send = "<html>" + "<pre> <h1>" + "addNum results:" + " </h1> " + "<h2>" + outPut + "</h2></html>";

		System.out.println(
				"HTTP/1.1 200 OK\r\n" + "Content-Length: " + send.length() + " Content-Type: " + type + "\r\n\r\n");
		System.out.println(outPut);

		out.print("HTTP/1.1 200 OK\r\n" + "Content-Length: " + send.length() + " Content-Type: " + type + "\r\n\r\n");
		out.flush();
		out.print(send);
		out.flush();

	}

}

public class MyWebServer {
	public static void main(String args[]) throws IOException {

		int q_len = 6; // if it gets more than 6 queue it will ignore the ones after...
		int port = 2540;
		Socket sock; // allocating a socket object

		ServerSocket servsock = new ServerSocket(port, q_len); // allocating a ServerSocket object 'servsock'

		System.out.println("Giries Hattar's Web Server 1.0 starting up, listening at port: " + port);
		while (true) {
			sock = servsock.accept(); // wait and then accept the next client connection
			new WebWorker(sock).start(); // handle the new connection
		}
	}
}

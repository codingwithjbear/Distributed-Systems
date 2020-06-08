
/*--------------------------------------------------------

1. Name / Date: Giries Hattar / 05/23/2020

2. Java version used, if not the official version for the class:

same as class version

3. Precise command-line compilation examples / instructions:

>javac -cp "gson-2.8.2.jar" *.java

4. Precise examples / instructions to run this program:

BlockMaster works with (start java -cp ".;gson-2.8.2.jar" Blockchain 0 / start java -cp ".;gson-2.8.2.jar" Blockchain 1 / java -cp ".;gson-2.8.2.jar" Blockchain 2)

OR manually 

>start java -cp ".;gson-2.8.2.jar" Blockchain 0

>start java -cp ".;gson-2.8.2.jar" Blockchain 1

>java -cp ".;gson-2.8.2.jar" Blockchain 2


5. List of files needed for running the program.

a. 	BlockInput0.txt
b.	BlockInput1.txt
c.	BlockInput2.txt
d.  Blockchain.java

optional:
BlockMaster with (start java -cp ".;gson-2.8.2.jar" Blockchain 0 / start java -cp ".;gson-2.8.2.jar" Blockchain 1 / java -cp ".;gson-2.8.2.jar" Blockchain 2)


5. Notes:
The web sources:

http://www.fredosaurus.com/notes-java/data/strings/96string_examples/example_stringToArray.html
Good explanation of linked lists:
https://beginnersbook.com/2013/12/linkedlist-in-java-with-example/
Priority queue:
https://www.javacodegeeks.com/2013/07/java-priority-queue-priorityqueue-example.html
Thanks: http://www.javacodex.com/Concurrency/PriorityBlockingQueue-Example
workB:
  https://www.quickprogrammingtips.com/java/how-to-generate-sha256-hash-in-java.html  @author JJ
  https://dzone.com/articles/generate-random-alpha-numeric  by Kunal Bhatia  ·  Aug. 09, 12 · Java Zone


----------------------------------------------------------*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.bind.DatatypeConverter;

import java.security.MessageDigest;
import java.util.Scanner;
import java.util.Arrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Blockchain {
	static String serverName = "localhost";
	static String blockchain = "[First block]";
	static int numProcesses = 3;
	static int PID = 0; // process Identifier

	public void MultiSend() throws Exception { // Multicast data to every processes.
		Socket sock;
		PrintStream toServer;


		try {
			/* here we are sending the key to each process */
			for (int i = 0; i < numProcesses; i++) {
				sock = new Socket(serverName, Ports.KeyServerPortBase + (i * 1000));
				toServer = new PrintStream(sock.getOutputStream());
				toServer.println("FakeKeyProcess" + Blockchain.PID);
				toServer.flush();
				sock.close();
			}
			Thread.sleep(1000); // wait and give time for the keys to process

			/* generate and numerically sort identifiable blockIDs */
			String fakeBlockA = "(Block#" + Integer.toString(((Blockchain.PID + 1) * 10) + 4) + " from P"
					+ Blockchain.PID + ")";
			String fakeBlockB = "(Block#" + Integer.toString(((Blockchain.PID + 1) * 10) + 3) + " from P"
					+ Blockchain.PID + ")";
			/* here we are sending an unverified block A to each server */
			for (int i = 0; i < numProcesses; i++) {
				sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + (i * 1000));
				toServer = new PrintStream(sock.getOutputStream());
				toServer.println(fakeBlockA);
				toServer.flush();
				sock.close();
			}
			/* here we are sending an unverified block B to each server */

			for (int i = 0; i < numProcesses; i++) {
				sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + (i * 1000));
				toServer = new PrintStream(sock.getOutputStream());
				toServer.println(fakeBlockB);
				toServer.flush();
				sock.close();
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	

	public static void main(String args[]) throws Exception {
		int q_len = 6;
		PID = (args.length < 1) ? 0 : Integer.parseInt(args[0]);
		System.out.println("Giries Hattars's BlockFramework control-c to quit.\n");
		System.out.println("Using processID " + PID + "\n");
		/*
		 * creating a queue to store unverified blocks and establishing the port #
		 * scheme based on the process ID
		 */
		final BlockingQueue<String> queue = new PriorityBlockingQueue<>();
		new Ports().setPorts();

		/*
		 * here we are creating new threads for incoming public keys, unverified blocks,
		 * and new blockchains
		 */
		new Thread(new PublicKeyServer()).start();
		new Thread(new UnverifiedBlockServer(queue)).start();
		new Thread(new BlockchainServer()).start();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		/*
		 * now we are going to multicast new unverified blocks to all servers and then
		 * wait for them to be processed
		 */
		new Blockchain().MultiSend();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}

		/* creating a new thread to consume the queue that we have filled */
		new Thread(new UnverifiedBlockConsumer(queue)).start();
	}
}

/*
 * here we are defining a class "Ports" that will be increased by "1000"
 * whenever a process is added to the multicast
 */
class Ports {
	public static int KeyServerPortBase = 4710;
	public static int UnverifiedBlockServerPortBase = 4820;
	public static int BlockchainServerPortBase = 4930;

	public static int KeyServerPort;
	public static int UnverifiedBlockServerPort;
	public static int BlockchainServerPort;

	public void setPorts() {
		KeyServerPort = KeyServerPortBase + (Blockchain.PID * 1000);
		UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (Blockchain.PID * 1000);
		BlockchainServerPort = BlockchainServerPortBase + (Blockchain.PID * 1000);
	}
}

/*
 * this is the public key worker looper thread to listen for incoming public
 * keys
 */
class PublicKeyWorker extends Thread {
	Socket sock; // socket for public key worker thread declaration

	PublicKeyWorker(Socket s) {
		sock = s;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String data = in.readLine();
			System.out.println("Got key: " + data);
			sock.close();
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
}

/*
 * this is the server that will be utilized for the processing of the public key
 */
class PublicKeyServer implements Runnable {
	private ServerSocket servsock;

	public void run() {
		int q_len = 6;
		Socket sock;
		System.out.println("Starting Key Server input thread using " + Integer.toString(Ports.KeyServerPort));
		try {
			servsock = new ServerSocket(Ports.KeyServerPort, q_len);
			while (true) {
				sock = servsock.accept();
				new PublicKeyWorker(sock).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

/*
 * here we are implementing a priority queue to place the unverified blocks each
 * block will be added to the queue by the order that it came in although the
 * order that they would be retrieved would be decided by the consumer process
 */
class UnverifiedBlockServer implements Runnable {
	BlockingQueue<String> queue;
	private ServerSocket servsock;

	UnverifiedBlockServer(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	/*
	 * this is the unverified blocks worker looper thread to listen for incoming
	 * unverified blocks
	 */

	class UnverifiedBlockWorker extends Thread { // Class definition
		Socket sock; // Class member, socket, local to Worker.

		UnverifiedBlockWorker(Socket s) {
			sock = s;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String data = in.readLine();
				System.out.println("Put in priority queue: " + data + "\n");
				queue.put(data);
				sock.close();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	public void run() {
		int q_len = 6;
		Socket sock;
		System.out.println("Starting the Unverified Block Server input thread using "
				+ Integer.toString(Ports.UnverifiedBlockServerPort));
		try {
			servsock = new ServerSocket(Ports.UnverifiedBlockServerPort, q_len);
			while (true) {
				sock = servsock.accept();
				new UnverifiedBlockWorker(sock).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

/*here we are declaring the consumer thread and doing real work utilizing "workb.java" code/methods*/
class UnverifiedBlockConsumer implements Runnable {
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	static String someText = "one two three";
	static String randString;
	private static final int iFNAME = 0;
	private static final int iLNAME = 1;
	private static final int iDOB = 2;
	private static final int iSSNUM = 3;
	private static final int iDIAG = 4;
	private static final int iTREAT = 5;
	private static final int iRX = 6;

	private static String FILENAME;

	Queue<BlockRecord> ourPriorityQueue = new PriorityQueue<>(4, BlockTSComparator);

	public static Comparator<BlockRecord> BlockTSComparator = new Comparator<BlockRecord>() {
		@Override
		public int compare(BlockRecord b1, BlockRecord b2) {
			String s1 = b1.getTimeStamp();
			String s2 = b2.getTimeStamp();
			if (s1 == s2) {
				return 0;
			}
			if (s1 == null) {
				return -1;
			}
			if (s2 == null) {
				return 1;
			}
			return s1.compareTo(s2);
		}
	};

	BlockingQueue<String> queue;
	int PID;

	UnverifiedBlockConsumer(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	/* this method will create a "random" alpha numeric string for the work alg */
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	public void ListExample(int pID) throws Exception {

		LinkedList<BlockRecord> recordList = new LinkedList<BlockRecord>();

		int pnum;
		int UnverifiedBlockPort;
		int BlockChainPort;

		pnum = pID;

		UnverifiedBlockPort = 4710 + pnum;
		BlockChainPort = 4820 + pnum;

		System.out.println("Process number: " + pnum + " Ports: " + UnverifiedBlockPort + " " + BlockChainPort + "\n");

		switch (pnum) {
		case 1:
			FILENAME = "BlockInput1.txt";
			break;
		case 2:
			FILENAME = "BlockInput2.txt";
			break;
		default:
			FILENAME = "BlockInput0.txt";
			break;
		}

		System.out.println("Using input file: " + FILENAME);

		try {
			BufferedReader br = new BufferedReader(new FileReader(FILENAME));
			String[] tokens = new String[10];
			String InputLineStr;
			String suuid;
			UUID idA;
			BlockRecord tempRec;

			StringWriter sw = new StringWriter();

			int n = 0;

			while ((InputLineStr = br.readLine()) != null) {

				BlockRecord BR = new BlockRecord(); /*
													 * creating a new object of type block record to utilize in our json
													 * file.
													 */

				/*
				 * here we are setting the timestamp for every block entry into the file. * we
				 * try to make it so that no timestamp collisions can occur!, * we also make in
				 * a way that we can priority sort by the TimeStamp of a block
				 */

				try {
					Thread.sleep(1001);
				} catch (InterruptedException e) {
				}
				Date date = new Date();
				String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
				String TimeStampString = T1 + "." + pnum;
				System.out.println("Timestamp: " + TimeStampString);
				BR.setTimeStamp(TimeStampString);

				/*
				 * here we will utilize the UUID library to create unique block IDs that will
				 * then be signend * by the creating process
				 */
				suuid = new String(UUID.randomUUID().toString());
				BR.setBlockID(suuid);

				/*
				 * here we will insert the data into block records utilizing the token fields
				 * created
				 */
				tokens = InputLineStr.split(" +");
				BR.setFname(tokens[iFNAME]);
				BR.setLname(tokens[iLNAME]);
				BR.setSSNum(tokens[iSSNUM]);
				BR.setDOB(tokens[iDOB]);
				BR.setDiag(tokens[iDIAG]);
				BR.setTreat(tokens[iTREAT]);
				BR.setRx(tokens[iRX]);

				recordList.add(BR);
				n++;
			}
			System.out.println(n + " records read." + "\n");
			System.out.println("Records in the linked list:");

			/*
			 * here we are creating an object of type iterator to iterate through our linked
			 * list* and show the names from each record and read them into the linked list
			 */
			Iterator<BlockRecord> iterator = recordList.iterator();
			while (iterator.hasNext()) {
				tempRec = iterator.next();
				System.out.println(tempRec.getTimeStamp() + " " + tempRec.getFname() + " " + tempRec.getLname());
			}
			System.out.println("");

			iterator = recordList.iterator();

			System.out.println("The shuffled list:");
			Collections.shuffle(recordList);
			while (iterator.hasNext()) {
				tempRec = iterator.next();
				System.out.println(tempRec.getTimeStamp() + " " + tempRec.getFname() + " " + tempRec.getLname());
			}
			System.out.println("");

			iterator = recordList.iterator();

			System.out.println("Placing shuffled records in our priority queue...\n");
			while (iterator.hasNext()) {
				ourPriorityQueue.add(iterator.next());
			}

			System.out.println("Priority Queue (restored) Order:");

			while (true) {

				tempRec = ourPriorityQueue.poll();
				if (tempRec == null)
					break;
				System.out.println(tempRec.getTimeStamp() + " " + tempRec.getFname() + " " + tempRec.getLname());
			}
			System.out.println("\n\n");

		} catch (Exception e) {
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		/*
		 * here we will create a JSON string from the java object "recordList"* we will
		 * then write the json object to a file called "BlockchainLedger"
		 *
		 */
		String json = gson.toJson(recordList);

		System.out.println("\nJSON (suffled) String list is: " + json);

		try (FileWriter writer = new FileWriter("BlockchianLedger.json")) {
			gson.toJson(recordList, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String data;
		PrintStream toServer;
		Socket sock;
		String newblockchain;
		String fakeVerifiedBlock;

		System.out.println("Starting the Unverified Block Priority Queue Consumer thread.\n");
		try {
			while (true) {// keep looping
				data = queue.take();
				System.out.println("Consumer got unverified: " + data);

				String concatString = ""; 
				String stringOut = ""; 				
				
				String stringIn = data;

				randString = randomAlphaNumeric(8);
				System.out.println("Our example random seed string is: " + randString + "\n");
				System.out.println("Concatenated with the \"data\": " + stringIn + randString + "\n");

				System.out.println("Number will be between 0000 (0) and FFFF (65535)\n");
				int workNumber = 0; // Number will be between 0000 (0) and FFFF (65535), here's proof:
				workNumber = Integer.parseInt("0000", 16); // Lowest hex value
				System.out.println("0x0000 = " + workNumber);

				workNumber = Integer.parseInt("FFFF", 16); // Highest hex value
				System.out.println("0xFFFF = " + workNumber + "\n");

				try {

					for (int i = 1; i < 20; i++) { 
						randString = randomAlphaNumeric(8); 
						concatString = stringIn + randString; 
						MessageDigest MD = MessageDigest.getInstance("SHA-256"); //setting SHA-256 hash digest
						byte[] bytesHash = MD.digest(concatString.getBytes("UTF-8")); 
						stringOut = DatatypeConverter.printHexBinary(bytesHash); 
						System.out.println("Hash is: " + stringOut);
						workNumber = Integer.parseInt(stringOut.substring(0, 4), 16);
						System.out.println("First 16 bits in Hex and Decimal: " + stringOut.substring(0, 4) + " and "
								+ workNumber);
						if (!(workNumber < 20000)) { 
							System.out.format("%d is not less than 20,000 so we did not solve the puzzle\n\n",
									workNumber);
						}
						if (workNumber < 20000) {
							System.out.format("%d IS less than 20,000 so puzzle solved!\n", workNumber);
							System.out.println("The seed (puzzle answer) was: " + randString);
							break;
						}
						
						Thread.sleep(500);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (Blockchain.blockchain.indexOf(data.substring(1, 9)) < 0) {
					ListExample(Blockchain.PID);
					fakeVerifiedBlock = "[" + data + " verified by P" + Blockchain.PID + " at time "
							+ Integer.toString(ThreadLocalRandom.current().nextInt(100, 1000)) + "]\n";
					System.out.println(fakeVerifiedBlock);
					String tempblockchain = fakeVerifiedBlock + Blockchain.blockchain;
					for (int i = 0; i < Blockchain.numProcesses; i++) {
						sock = new Socket(Blockchain.serverName, Ports.BlockchainServerPortBase + (i * 1000));
						toServer = new PrintStream(sock.getOutputStream());
						toServer.println(tempblockchain);
						toServer.flush();
						sock.close();
					}
				}
				Thread.sleep(1500);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

/*
 * this is the updated blockchains worker looper thread to listen for incoming
 * updated blockchains
 */

class BlockchainWorker extends Thread {
	Socket sock;

	BlockchainWorker(Socket s) {
		sock = s;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String data = "";
			String data2;
			while ((data2 = in.readLine()) != null) {
				data = data + data2;
			}
			Blockchain.blockchain = data;
			System.out.println("         --NEW BLOCKCHAIN--\n" + Blockchain.blockchain + "\n\n");
			sock.close();
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
}

class BlockchainServer implements Runnable {
	private ServerSocket servsock;

	public void run() {
		int q_len = 6;
		Socket sock;
		System.out.println(
				"Starting the blockchain server input thread using " + Integer.toString(Ports.BlockchainServerPort));
		try {
			servsock = new ServerSocket(Ports.BlockchainServerPort, q_len);
			while (true) {
				sock = servsock.accept();
				new BlockchainWorker(sock).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

class BlockRecord {
	String BlockID;
	String TimeStamp;
	String VerificationProcessID;
	String PreviousHash;
	UUID uuid;
	String Fname;
	String Lname;
	String SSNum;
	String DOB;
	String RandomSeed;
	String WinningHash;
	String Diag;
	String Treat;
	String Rx;

	public String getBlockID() {
		return BlockID;
	}

	public void setBlockID(String BID) {
		this.BlockID = BID;
	}

	public String getTimeStamp() {
		return TimeStamp;
	}

	public void setTimeStamp(String TS) {
		this.TimeStamp = TS;
	}

	public String getVerificationProcessID() {
		return VerificationProcessID;
	}

	public void setVerificationProcessID(String VID) {
		this.VerificationProcessID = VID;
	}

	public String getPreviousHash() {
		return this.PreviousHash;
	}

	public void setPreviousHash(String PH) {
		this.PreviousHash = PH;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID ud) {
		this.uuid = ud;
	}

	public String getLname() {
		return Lname;
	}

	public void setLname(String LN) {
		this.Lname = LN;
	}

	public String getFname() {
		return Fname;
	}

	public void setFname(String FN) {
		this.Fname = FN;
	}

	public String getSSNum() {
		return SSNum;
	}

	public void setSSNum(String SS) {
		this.SSNum = SS;
	}

	public String getDOB() {
		return DOB;
	}

	public void setDOB(String RS) {
		this.DOB = RS;
	}

	public String getDiag() {
		return Diag;
	}

	public void setDiag(String D) {
		this.Diag = D;
	}

	public String getTreat() {
		return Treat;
	}

	public void setTreat(String Tr) {
		this.Treat = Tr;
	}

	public String getRx() {
		return Rx;
	}

	public void setRx(String Rx) {
		this.Rx = Rx;
	}

	public String getRandomSeed() {
		return RandomSeed;
	}

	public void setRandomSeed(String RS) {
		this.RandomSeed = RS;
	}

	public String getWinningHash() {
		return WinningHash;
	}

	public void setWinningHash(String WH) {
		this.WinningHash = WH;
	}

}

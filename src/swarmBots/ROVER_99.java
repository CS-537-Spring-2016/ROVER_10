package swarmBots;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.RoverListenPorts;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_99 {

	// original instance vars
	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int SWARM_SERVER_PORT_ADDRESS = 9537;

	// instance vars implemented in p2p branch
	private int listenPort = RoverListenPorts.ROVER_99.getPort();
	private ServerSocket roverServerSocket;
	private int roverPeerNo;
	private List<RoverPeer> connectedPeers;

	public ROVER_99() {
		// constructor
		System.out.println("ROVER_99 rover object constructed");
		rovername = "ROVER_99";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_99(String serverAddress) {
		// constructor
		System.out.println("ROVER_99 rover object constructed");
		rovername = "ROVER_99";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 200; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	// ROVER_99 is also a server so it can listen to connects coming
	// from other Rovers.
	public void startRoverServer() throws IOException {

		roverPeerNo = 0; // intially 0, since no rovers are connected to it yet.
		connectedPeers = new ArrayList<RoverPeer>();

		// create the server server socket that is listening on this port
		roverServerSocket = new ServerSocket(listenPort);

		System.out.println(this.getClass().getSimpleName() + " server started!");
		System.out.println(this.getClass().getSimpleName() + " is listening on port: " + this.listenPort);

		// "serve" each rover (peer) concurrently
		new Thread(() -> {
			while (true) {
				try {
					// wait for a peer to connect
					Socket connectionSocket = roverServerSocket.accept();

					// once there is a connection, serve them
					new Thread(new RoverPeerHandler(connectionSocket)).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

		// // open "shell" mode
		// startInput();
	}

	/**
	 * open an IO stream for each RoverPeer connected to the host display all
	 * the messages send to the host rover by the peer rover
	 */
	class RoverPeerHandler implements Runnable {

		private Socket roverPeerSocket;

		public RoverPeerHandler(Socket socket) {
			this.roverPeerSocket = socket;
		}

		public void run() {

			try {
				// start input stream
				BufferedReader input = new BufferedReader(new InputStreamReader(roverPeerSocket.getInputStream()));

				// print all messages send to the host by continuously reading
				// whatever that is in the input stream
				while (true) {
					String clientMsg = input.readLine();

					try {

						// when a client connects to the host, the client will
						// send the host it's IP and port.
						// the host will save this information.
						if (clientMsg.startsWith("listen")) {

							System.out.println("\na client has connected to you");
							System.out.print("-> ");

							// parse the LISTEN message and return the port.
							// message will be in the format: "listen <port>"
							String strPort = clientMsg.split(" ")[1];
							int port = Integer.parseInt(strPort);

							String host = roverPeerSocket.getInetAddress().getHostAddress();

							// add the client information into an array list
							RoverPeer peer = new RoverPeer(++roverPeerNo, host, port);
							connectedPeers.add(peer);

							// all "send" message are display here and its
							// related information.
						} else if (clientMsg.startsWith("SEND")) {
							clientMsg = clientMsg.substring(4);

							System.out.println(
									"\nMessage received from " + roverPeerSocket.getInetAddress().getHostAddress());
							System.out.println("Sender's Port: " + roverPeerSocket.getPort());
							System.out.println("Message: " + clientMsg + "\n");

							// "->" doesn't display after the user receive a
							// message
							System.out.print("-> ");
						}
					} catch (Exception e) {
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// connects to another rover peer socket on a separate thread
	public void connect(String address, int port) {
		PeerConnector peerConnector = new PeerConnector(address, port);
		new Thread(peerConnector).start();
	}

	// connect to a user (peer) on a different thread
	// we probably don't need to do it like this but threading is cool
	public class PeerConnector implements Runnable {

		private String peerHost;
		private int peerPort;
		private Socket peerSocket;

		public PeerConnector(String host, int port) {
			this.peerHost = host;
			this.peerPort = port;
		}

		@Override
		public void run() {

			int attemptCounter = 1;
			final int MAX_ATTEMPTS = 5;
			final int SLEEP_TIME = 1000;

			// try to connect but will stop after MAX_ATTEMPTS
			do {
				try {
					peerSocket = new Socket(peerHost, peerPort);
				} catch (IOException e) {
					System.out.println("\n### connection failed...attempt: " + attemptCounter++);
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			} while (peerSocket == null && attemptCounter <= MAX_ATTEMPTS);

			// stop here if fail too many times
			if (attemptCounter > MAX_ATTEMPTS) {
				System.out.println("connection was unsuccessful, please try again later");
				System.out.print("-> ");
				return;
			}

			// if reach here then connection was successful
			// add (save) the socket so they can be use later
			System.out.println("\nyou connected to the client ...");
			System.out.print("-> ");
			connectedPeers.add(new RoverPeer(++roverPeerNo, peerHost, peerPort));

			// tell the rover your host address and port number
			try {
				DataOutputStream dos = new DataOutputStream(peerSocket.getOutputStream());
				dos.writeBytes("listen " + listenPort + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send a message to some specific rover
	 * @param id id of the rover in connectedPeers
	 * @param message The message you want to send to the other rover..
	 */
	public void sendMessage(int id, String message) {
		try {
			for (RoverPeer p : connectedPeers) {
				if (p.getId() == id) {
					// "\r\n" so when readLine() is called,
					// it knows when to stop
					p.getOutput().writeBytes(message + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connects to the swarm server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		// Make connection and initialize streams
		// TODO - need to close this socket
		Socket socket = new Socket(SERVER_ADDRESS, SWARM_SERVER_PORT_ADDRESS); // set
																				// port
																				// here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Process all messages from server, wait until server requests Rover ID
		// name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); // This sets the name of this instance
										// of a swarmBot for identifying the
										// thread to the server
				break;
			}
		}

		// ******** Rover logic *********
		// int cnt=0;
		String line = "";

		boolean goingSouth = false;
		boolean stuck = false; // just means it did not change locations between
								// requests,
								// could be velocity limit or obstruction etc.
		boolean blocked = false;

		String[] cardinals = new String[4];
		cardinals[0] = "N";
		cardinals[1] = "E";
		cardinals[2] = "S";
		cardinals[3] = "W";

		String currentDir = cardinals[0];
		Coord currentLoc = null;
		Coord previousLoc = null;

		// start Rover controller process
		while (true) {

			// currently the requirements allow sensor calls to be made with no
			// simulated resource cost

			// **** location call ****
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println("ROVER_99 check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				// loc = line.substring(4);
				currentLoc = extractLOC(line);
			}
			System.out.println("ROVER_99 currentLoc at start: " + currentLoc);

			// after getting location set previous equal current to be able to
			// check for stuckness and blocked later
			previousLoc = currentLoc;

			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			// System.out.println("ROVER_99 equipment list results drive " +
			// equipment.get(0));
			System.out.println("ROVER_99 equipment list results " + equipment + "\n");

			// ***** do a SCAN *****
			// System.out.println("ROVER_99 sending SCAN request");
			this.doScan();
			scanMap.debugPrintMap();

			// ***** MOVING *****
			// try moving east 5 block if blocked
			if (blocked) {
				for (int i = 0; i < 5; i++) {
					out.println("MOVE E");
					// System.out.println("ROVER_99 request move E");
					Thread.sleep(300);
				}
				blocked = false;
				// reverses direction after being blocked
				goingSouth = !goingSouth;
			} else {

				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				if (goingSouth) {
					// check scanMap to see if path is blocked to the south
					// (scanMap may be old data by now)
					if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
							|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE) {
						blocked = true;
					} else {
						// request to server to move
						out.println("MOVE S");
						// System.out.println("ROVER_99 request move S");
					}

				} else {
					// check scanMap to see if path is blocked to the north
					// (scanMap may be old data by now)
					// System.out.println("ROVER_99
					// scanMapTiles[2][1].getHasRover() " +
					// scanMapTiles[2][1].getHasRover());
					// System.out.println("ROVER_99
					// scanMapTiles[2][1].getTerrain() " +
					// scanMapTiles[2][1].getTerrain().toString());

					if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
							|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE) {
						blocked = true;
					} else {
						// request to server to move
						out.println("MOVE N");
						// System.out.println("ROVER_99 request move N");
					}
				}
			}

			// another call for current location
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println("ROVER_99 check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				currentLoc = extractLOC(line);
			}

			// System.out.println("ROVER_99 currentLoc after recheck: " +
			// currentLoc);
			// System.out.println("ROVER_99 previousLoc: " + previousLoc);

			// test for stuckness
			stuck = currentLoc.equals(previousLoc);

			// System.out.println("ROVER_99 stuck test " + stuck);
			System.out.println("ROVER_99 blocked test " + blocked);

			// TODO - logic to calculate where to move next

			Thread.sleep(sleepTime);

			System.out.println("ROVER_99 ------------ bottom process control --------------");
		}

	}

	// ################ Support Methods ###########################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_99 clearing readLine()");
			String garbage = in.readLine();
		}
	}

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		// System.out.println("ROVER_99 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		// System.out.println("ROVER_99 incomming EQUIPMENT result - first
		// readline: " + jsonEqListIn);

		if (jsonEqListIn.startsWith("EQUIPMENT")) {
			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
				if (jsonEqListIn == null) {
					break;
				}
				// System.out.println("ROVER_99 incomming EQUIPMENT result: " +
				// jsonEqListIn);
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
				// System.out.println("ROVER_99 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}

		String jsonEqListString = jsonEqList.toString();
		ArrayList<String> returnList;
		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>() {
		}.getType());
		// System.out.println("ROVER_99 returnList " + returnList);

		return returnList;
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	public void doScan() throws IOException {
		// System.out.println("ROVER_99 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonScanMapIn == null) {
			System.out.println("ROVER_99 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_99 incomming SCAN result - first readline: " + jsonScanMapIn);

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				// System.out.println("ROVER_99 incomming SCAN result: " +
				// jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				// System.out.println("ROVER_99 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		// System.out.println("ROVER_99 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file
		// new MyWriter( jsonScanMapString, 0); //gives a strange result -
		// prints the \n instead of newline character in the file

		// System.out.println("ROVER_99 convert from json back to ScanMap
		// class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
	}

	// this takes the LOC response string, parses out the x and x values and
	// returns a Coord object
	public static Coord extractLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_99 client = new ROVER_99();
		client.run();
		client.startRoverServer();
		client.connect("localhost", RoverListenPorts.ROVER_10.getPort());
	}
}
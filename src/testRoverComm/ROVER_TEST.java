package testRoverComm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import common.ScanMap;
import enums.RoverListenPorts;
import swarmBots.RoverPeer;

//class used to test rover p2p communications, exclusive of the swarmserver for now.
public class ROVER_TEST {
	// original instance vars
	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int SWARM_SERVER_PORT_ADDRESS = 9537;

	// instance vars implemented in p2p branch
	private int listenPort;
	private ServerSocket roverServerSocket;
	private int roverPeerNo;
	private List<RoverPeer> connectedPeers;

	public ROVER_TEST() {
		// constructor
		System.out.println("ROVER_10_TEST rover object constructed");
		rovername = "ROVER_10_TEST";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

//	public ROVER_TEST(String serverAddress) {
//		// constructor
//		System.out.println("ROVER_10_TEST rover object constructed");
//		rovername = "ROVER_10_TEST";
//		SERVER_ADDRESS = serverAddress;
//		sleepTime = 200; // in milliseconds - smaller is faster, but the server
//							// will cut connection if it is too small
//	}

	public ROVER_TEST(String roverName) {
		this.listenPort = RoverListenPorts.getEnum(roverName).getPort();
	}
	
	// ROVER_10_TEST is also a server so it can listen to connects coming
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
						} else if (clientMsg.startsWith("ROVER")) {
							clientMsg = clientMsg.substring(5);

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

	public void displayList() {
		if (connectedPeers.isEmpty() || connectedPeers == null)
			System.out.println("No peers connected.");
		else {
			System.out.println("id:   IP Address     Port No.");
			for (RoverPeer p : connectedPeers) {
				System.out.println(p.getId() + "    " + p.getHost() + "     " + p.getPort());
			}
			System.out.println("Total Peers: " + connectedPeers.size());
		}
	}
	
	 public String getIP() {
	        String hostAddress = null;
	        try {
	            hostAddress = InetAddress.getLocalHost().getHostAddress();
	        } catch (UnknownHostException e) {
	            e.printStackTrace();
	        }
	        return hostAddress;
	 }
	 
	/**
	 * Send a message to some specific rover
	 * 
	 * @param id
	 *            id of the rover in connectedPeers
	 * @param message
	 *            The message you want to send to the other rover..
	 */
	public void sendMessage(int id, String message) {
		try {
			for (RoverPeer p : connectedPeers) {
				if (p.getId() == id) {
					// "\r\n" so when readLine() is called,
					// it knows when to stop
					System.out.println("Rover connected on port: " + p.getPort() + " found.");
				
					p.getOutput().writeBytes(message + "\r\n");
				} else {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import common.Coord;
import common.MapTile;
import common.ScanMap;
import common.LiveMap;
import enums.Terrain;
import enums.RoverToolType;
import enums.RoverDriveType;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_10 {

	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int SWARM_SERVER_PORT_ADDRESS = 9537;

	ArrayList<String> radioactiveLocations = new ArrayList<String>();

	public ROVER_10() {
		System.out.println("ROVER_10 rover object constructed");
		rovername = "ROVER_10";
		SERVER_ADDRESS = "localhost";
		// in milliseconds - smaller is faster, but the server will cut connection if too small
		sleepTime = 300; 
	}

	/**
	 * Connects to the swarm server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		Socket socket = new Socket(SERVER_ADDRESS, SWARM_SERVER_PORT_ADDRESS);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); 
				break;
			}
		}

		out.println("TARGET_LOC");
		String line = in.readLine();
		Coord targetLoc = null;
		if (line.startsWith("TARGET_LOC")) {
			targetLoc = extractLOC(line);
		}
		
		// Get start loc.
		out.println("START_LOC");
		line = in.readLine();
		Coord startLoc = null;
		
		if (line.startsWith("START_LOC")) {
			startLoc = extractLOC(line);
		}
		
		LiveMap live = new LiveMap(1000, 1000, startLoc, targetLoc);

		// ******** Rover logic *********
		// int cnt=0;

		boolean stuck = false;
		boolean blocked = false;

		// start moving south
		Coord currentLoc = null;
		Coord previousLoc = null;

		// start Rover controller process
		while (true) {

			// **** location call ****
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println("ROVER_10 check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				// loc = line.substring(4);
				currentLoc = extractLOC(line);
			}
			System.out.println("ROVER_10 currentLoc at start: " + currentLoc);

			// after getting location set previous equal current to be able to
			// check for stuckness and blocked later
			previousLoc = currentLoc;

			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			System.out.println("ROVER_10 equipment list results " + equipment + "\n");

			// ***** do a SCAN *****
			// System.out.println("ROVER_10 sending SCAN request");
			this.doScan();
			// could probably be dynamic, called from an EQUIPMENT call (at start) and fed through RoverToolType.getEnum(String),  but I'm lazy.
			live.addScanMap(scanMap, currentLoc, RoverToolType.RADIATION_SENSOR, RoverToolType.RANGE_BOOTER); // this																										
			live.debugPrintRevealCounts(currentLoc, RoverToolType.RADIATION_SENSOR, RoverToolType.RANGE_BOOTER);
			scanMap.debugPrintMap();
			
			// Calculating coordinates and adding the radioactive elements
			// locations to an new arraylist using a function
			ArrayList<String> radioactiveFetch = scanMap.radioactiveLocations();
			radiation_sensor(currentLoc.currentCoord(), radioactiveFetch);
			List<Coord> radioActCoords = radListToCoords(this.radioactiveLocations);
						
			MapTile[][] scanMapTiles = scanMap.getScanMap();
			int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
			// ***** MOVING *****
			char dir = live.findPath(currentLoc, targetLoc, RoverDriveType.WHEELS);
			if (dir != 'U') {
				out.println("MOVE " + dir);
			}
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println("ROVER_10 check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				currentLoc = extractLOC(line);
			}

			stuck = currentLoc.equals(previousLoc);

			System.out.println("ROVER_10 blocked test " + blocked);
			Thread.sleep(sleepTime);
			System.out.println("ROVER_10 ------------ bottom process control --------------");
		}
	}

	// populates this.radioactiveLocations w/ loc's of radioactive elements.
	private void radiation_sensor(String currentLoc, ArrayList<String> radioactiveFetch) {

		// declaring variables for current x & y , chemical x & y
		int x_Current = 0, y_Current = 0, x_radioactiveelements = 0, y_radioactiveelements = 0;

		boolean duplicate = false;

		String radioactiveLocation = null;

		// extracting the current coordinates and putting into integer variables
		String[] currentCoordinates = currentLoc.split(" ");
		x_Current = Integer.parseInt(currentCoordinates[0]);
		y_Current = Integer.parseInt(currentCoordinates[1]);

		// iterating the radioactiveelementFetch array list for all the
		// radioactive locations
		for (String s : radioactiveFetch) {
			// extracting the radioactive coordinates and putting into integer
			// variables
			String[] radioactiveCoordinates = s.split(" ");
			x_radioactiveelements = Integer.parseInt(radioactiveCoordinates[0]);
			y_radioactiveelements = Integer.parseInt(radioactiveCoordinates[1]);

			// checking the x value of radioactive coordinate in the scan map
			// least will be 0 and max will 10 while 5 will be median
			switch (x_radioactiveelements) {
			case 0:
				x_radioactiveelements = x_Current - 5;
				break;
			case 1:
				x_radioactiveelements = x_Current - 4;
				break;
			case 2:
				x_radioactiveelements = x_Current - 3;
				break;
			case 3:
				x_radioactiveelements = x_Current - 2;
				break;
			case 4:
				x_radioactiveelements = x_Current - 1;
				break;
			case 5:
				x_radioactiveelements = x_Current;
				break;
			case 6:
				x_radioactiveelements = x_Current + 1;
				break;
			case 7:
				x_radioactiveelements = x_Current + 2;
				break;
			case 8:
				x_radioactiveelements = x_Current + 3;
				break;
			case 9:
				x_radioactiveelements = x_Current + 4;
				break;
			case 10:
				x_radioactiveelements = x_Current + 5;
				break;
			}

			// checking the y value of radioactive coordinate in the scan map
			// least will be 0 and max will 10 while 5 will be median
			switch (y_radioactiveelements) {
			case 0:
				y_radioactiveelements = y_Current - 5;
				break;
			case 1:
				y_radioactiveelements = y_Current - 4;
				break;
			case 2:
				y_radioactiveelements = y_Current - 3;
				break;
			case 3:
				y_radioactiveelements = y_Current - 2;
				break;
			case 4:
				y_radioactiveelements = y_Current - 1;
				break;
			case 5:
				y_radioactiveelements = y_Current;
				break;
			case 6:
				y_radioactiveelements = y_Current + 1;
				break;
			case 7:
				y_radioactiveelements = y_Current + 2;
				break;
			case 8:
				y_radioactiveelements = y_Current + 3;
				break;
			case 9:
				y_radioactiveelements = y_Current + 4;
				break;
			case 10:
				y_radioactiveelements = y_Current + 5;
				break;
			}

			// checking whether coordinates are not negative
			if (x_radioactiveelements >= 0 && y_radioactiveelements >= 0) {
				// creating a string form of coordinates to store in arraylist
				radioactiveLocation = x_radioactiveelements + "," + y_radioactiveelements;
				// iterating through existing coordinates arraylist for
				// duplicates
				for (String loc : this.radioactiveLocations) {
					if (loc.equals(radioactiveLocation)) {
						duplicate = true;
						break;
					}
				}

				// adding to arraylist if no duplicates found above
				if (!duplicate)
					this.radioactiveLocations.add(radioactiveLocation);
				duplicate = false;
			}
		}
		JSONObject obj = new JSONObject();
	    JSONArray jarray = new JSONArray();
	    try {
	        JSONObject obj1 = new JSONObject();
	        for (int i = 0; i < radioactiveLocations.size(); i++) {
	            obj1=new JSONObject();
	            obj1.put("location", radioactiveLocations.get(i));
	            jarray.add(obj1);
	        }
	        obj1.put("Thong tin", jarray);
	    } catch (JsonIOException e) {
	        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	    }
	}

	// ################ Support Methods ###########################
	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_10 clearing readLine()");
			String garbage = in.readLine();
		}
	}

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		// System.out.println("ROVER_10 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		// System.out.println("ROVER_10 incomming EQUIPMENT result - first
		// readline: " + jsonEqListIn);

		if (jsonEqListIn.startsWith("EQUIPMENT")) {
			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
				if (jsonEqListIn == null) {
					break;
				}
				// System.out.println("ROVER_10 incomming EQUIPMENT result: " +
				// jsonEqListIn);
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
				// System.out.println("ROVER_10 doScan() bottom of while");
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
		// System.out.println("ROVER_10 returnList " + returnList);

		return returnList;
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	public void doScan() throws IOException {
		// System.out.println("ROVER_10 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonScanMapIn == null) {
			System.out.println("ROVER_10 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_10 incomming SCAN result - first readline: " + jsonScanMapIn);

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				// System.out.println("ROVER_10 incomming SCAN result: " +
				// jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				// System.out.println("ROVER_10 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		// System.out.println("ROVER_10 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file
		// new MyWriter( jsonScanMapString, 0); //gives a strange result -
		// prints the \n instead of newline character in the file

		// System.out.println("ROVER_10 convert from json back to ScanMap
		// class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
	}

	// this takes the LOC response string, parses out the x and x values and
	// returns a Coord object
	public static Coord extractLOC(String sStr) {
		String[] subStrs = sStr.split(" ");
		if (subStrs.length > 2) {
			String xStr = subStrs[subStrs.length - 2];
			// System.out.println("extracted xStr " + xStr);

			String yStr = subStrs[subStrs.length - 1];
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}
	private List<Coord> radListToCoords(List<String> radioactiveLocations) {
		List<Coord> radioActCoords = new ArrayList<>();
		
		if (radioactiveLocations != null && !radioactiveLocations.isEmpty()) {
			for (String loc : radioactiveLocations) {
				String[] coordArr = loc.split(",");
				Coord coord = new Coord(Integer.valueOf(coordArr[0]), Integer.valueOf(coordArr[1]));				
				radioActCoords.add(coord);
			}
		}		
		return radioActCoords;
	}
	
	private void viewRadioactives(List<String> radioactiveLocations) {
		for (String location: radioactiveLocations) {
			System.out.println(location);
		}
	}

	public static void main(String[] args) throws Exception {
		ROVER_10 rover10 = new ROVER_10();
		rover10.run();
	}
}

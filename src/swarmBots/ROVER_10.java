package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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
			targetLoc = new Coord(28, 9);
		}
		
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_10 check connection to server");
			line = "";
		}
		// Get start loc.
		out.println("START_LOC");
		line = in.readLine();
		Coord startLoc = null;
		
		if (line.startsWith("START_LOC")) {
			startLoc = extractLOC(line);			
		}
		
		LiveMap live = new LiveMap(1000, 1000, startLoc, targetLoc);
		
		Coord currentLoc = null;
		boolean goldMineReached = false;
		boolean unknownMineReached = true;
		
		Coord unknownTargetLoc = null;
		while (true) {
			// **** location call ****
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println("ROVER_10 check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				currentLoc = extractLOC(line);
			}
			
			System.out.println("ROVER_10 currentLoc at start: " + currentLoc);
			
			//we've reached our primary loc, so no need to consider it anymore.
			if (currentLoc.equals(targetLoc)) {
				goldMineReached = true;
				System.out.println("reached");
			}
			
			if (currentLoc.equals(unknownTargetLoc)) {
				System.out.println("Indeed, current Loc equals unknownTargetLoc");
//				Thread.sleep(2000);
				unknownMineReached = true;
			}
			
			this.doScan();
			live.addScanMap(scanMap, currentLoc, RoverToolType.RADIATION_SENSOR, RoverToolType.RANGE_BOOTER); // this																										
			scanMap.debugPrintMap();
			
			radiation_sensor(currentLoc.currentCoord());
			
			char dir = ' ';
			if (!goldMineReached) {
				System.out.println("Target loc: " + targetLoc);
				//get dir to targetLoc so that goldMineReached will be true, eventually
				dir = live.findPath(currentLoc, targetLoc, RoverDriveType.WHEELS);
			}
			else if (unknownMineReached){
				System.out.println("Det new loc");
//				Thread.sleep(5000);
				unknownTargetLoc = determineNextCoord(live, currentLoc);
				dir = live.findPath(currentLoc, unknownTargetLoc, RoverDriveType.WHEELS);
				unknownMineReached = false;
			} else {
				System.out.println("Still trying to go to : " + unknownTargetLoc.toString());
//				Thread.sleep(1000);
				dir = live.findPath(currentLoc, unknownTargetLoc, RoverDriveType.WHEELS);
				while (dir == 'U') {
					unknownTargetLoc = determineNextCoord(live, currentLoc);
					dir = live.findPath(currentLoc, unknownTargetLoc, RoverDriveType.WHEELS);
				}
			}
			if (dir != 'U') {
				out.println("MOVE " + dir);
			}
			System.out.println("Livepath dir: " + dir); 
			
						
			Thread.sleep(sleepTime);
			System.out.println("ROVER_10 ------------ bottom process control --------------");
		}
	}
	
	public Coord determineNextCoord(LiveMap live, Coord currentLoc) {
		char[] cardinalDirs = new char[4];
		cardinalDirs[0] = 'S';
		cardinalDirs[1] = 'N';
		cardinalDirs[2] = 'W';
		cardinalDirs[3] = 'E';
		Coord targetCoord = null;
		for (int i = 0; i < cardinalDirs.length; i++) {
			char dir = cardinalDirs[i];
			for (int j = 1; j <= 7; j++) {
				System.out.println("Try to go " + dir + " " + j + " times...");

				targetCoord = calcTargetLoc(currentLoc, dir, j);

				char potentialDir = live.findPath(currentLoc, targetCoord, RoverDriveType.WHEELS);
				if (potentialDir != 'U') {
					System.out.println("Target dest is reachble.., going to " + targetCoord.toString());
					return targetCoord;
				}
				System.out.println("After return targetCoord");
			}
			System.out.println("After inner for loop...");
		}
		return targetCoord;
	}
	
	public Coord calcTargetLoc(Coord currentLoc, char dir, int j) {
		char targetDir = dir;
		Coord deltaCoord = null;
		Coord targetCoord;
		switch (targetDir) {
		case 'N':
			deltaCoord = new Coord(0, -1 * j);
			break;
		case 'W':
			deltaCoord = new Coord(-1 * j, 0);
			break;
		case 'S':
			deltaCoord = new Coord(0, 1 * j);
			break;
		case 'E':
			deltaCoord = new Coord(1 * j, 0);
			break;
		}
		
		targetCoord = new Coord(currentLoc.xpos + deltaCoord.xpos, currentLoc.ypos + deltaCoord.ypos);
		System.out.println("currentloc: " + currentLoc.toString());
		System.out.println("targetCoord: " + targetCoord.toString());
//		Thread.sleep(5000);
		return targetCoord;
	}

	private void radiation_sensor(String currentLoc) throws IOException {

		int x_Current = 0, y_Current = 0, x_radioactiveelements = 0, y_radioactiveelements = 0;

		String[] currentCoordinates = currentLoc.split(" ");
		x_Current = Integer.parseInt(currentCoordinates[0]);
		y_Current = Integer.parseInt(currentCoordinates[1]);

		MapTile[][] locArray = null;
		for(int i = x_Current-5;i<=x_Current+5;i++){
			for(int j=y_Current-5;j<=y_Current+5;j++){
				if(x_radioactiveelements == i && y_radioactiveelements == j){
					if(locArray[i][j].getScience().toString().equals("RADIOACTIVE")){
					JSONObject obj = new JSONObject();

					obj.put("x_radioactiveelements", x_radioactiveelements);
					obj.put("y_radioactiveelements", y_radioactiveelements);

					StringWriter out = new StringWriter();
					obj.writeJSONString(out);
					}
				}
			}
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


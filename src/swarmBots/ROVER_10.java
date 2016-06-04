package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_10 check connection to server");
			line = "";
		}

		out.println("START_LOC");
		line = in.readLine();
		Coord startLoc = null;
		
		if (line.startsWith("START_LOC")) {
			startLoc = extractLOC(line);			
		}
		
		LiveMap live = new LiveMap(1000, 1000, startLoc, targetLoc);
		
		Coord currentLoc = null;
		boolean destReached = false;
		char dir = ' ';
		int counter = 1;

		char[] dirs = new char[]{'N', 'E', 'S', 'W'};
		Random rand = new Random();
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
			
			if (currentLoc.equals(targetLoc)) {
				destReached = true;
			}
			
			System.out.println("Current Loc: " + currentLoc.toString());

			this.doScan();
			live.addScanMap(scanMap, currentLoc, RoverToolType.RADIATION_SENSOR, RoverToolType.RANGE_BOOTER); // this																										
			scanMap.debugPrintMap();
			
			//used to gather coordinates for radioactive locs. don't really need it right now.
			//could be useful in the future though
//			radiation_sensor(currentLoc.currentCoord());
			
			if (!destReached) {
				dir = live.findPath(currentLoc, targetLoc, RoverDriveType.WHEELS);
			} else {
				if (counter % 20 == 0) {
					List<String> dirsConsidered = new ArrayList<>();
					char dirOpposite = getOpposite(dir);
					for (int i = 0; i < dirs.length; i++) {
						if (dirs[i] != dirOpposite) {
							dirsConsidered.add(String.valueOf(dirs[i]));
						}
					}
					dir = dirsConsidered.get(rand.nextInt(3)).charAt(0);					
				}
				counter++;
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				System.out.println("Current dir: " + dir);
				switch (dir) {
				case 'N':
					if (isDirBlocked(scanMapTiles, centerIndex, 'N')) {
						dir = resolveNorth(scanMapTiles, centerIndex);
					}
					break;
				case 'S':
					if (!isDirBlocked(scanMapTiles, centerIndex, 'S')) {
						dir = resolveSouth(scanMapTiles, centerIndex);
					}
					break;
				case 'E':
					if (!isDirBlocked(scanMapTiles, centerIndex, 'E')) {
						dir = resolveEast(scanMapTiles, centerIndex);
					}
					break;
				case 'W':
					if (!isDirBlocked(scanMapTiles, centerIndex, 'W')) {
						dir = resolveWest(scanMapTiles, centerIndex);
					}
					break;
				}
			}
			if (dir != 'U') {
				out.println("MOVE " + dir);
			} else {
				System.out.println("Unreachable location.");
				Thread.sleep(5000);
			}
				
			Thread.sleep(sleepTime);
			System.out.println("ROVER_10 ------------ bottom process control --------------");
		}
	}
	
	public char getOpposite(char dir) {
		char opposite = ' ';
		switch (dir) {
		case 'N':
			opposite = 'S';
			break;
		case 'S':
			opposite = 'N';
			break;
		case 'E':
			opposite = 'W';
			break;
		case 'W':
			opposite = 'E';
			break;
		}
		System.out.println("Opposite of " + dir + " is " + opposite);
		return opposite;
	}
	
	public char resolveNorth(MapTile[][] scanMapTiles, int centerIndex) {
		String currentDir = "N";
		if (!isDirBlocked(scanMapTiles, centerIndex, 'E'))
			currentDir = "E";
		else if (!isDirBlocked(scanMapTiles, centerIndex, 'W'))
			currentDir = "W";
		else
			currentDir = "S";
		return currentDir.charAt(0);
	}

	public char resolveSouth(MapTile[][] scanMapTiles, int centerIndex) {
		String currentDir = "S";
		if (!isDirBlocked(scanMapTiles, centerIndex, 'W'))
			currentDir = "W";
		else if (!isDirBlocked(scanMapTiles, centerIndex, 'E'))
			currentDir = "E";
		else {
			currentDir = "N";
		}
		return currentDir.charAt(0);
	}

	public char resolveEast(MapTile[][] scanMapTiles, int centerIndex) {
		String currentDir = "E";
		if (!isDirBlocked(scanMapTiles, centerIndex, 'S'))
			currentDir = "S";
		else if (!isDirBlocked(scanMapTiles, centerIndex, 'N'))
			currentDir = "N";
		else
			currentDir = "W";
		return currentDir.charAt(0);
	}

	public char resolveWest(MapTile[][] scanMapTiles, int centerIndex) {
		String currentDir = "W";
		if (!isDirBlocked(scanMapTiles, centerIndex, 'N'))
			currentDir = "N";
		else if (!isDirBlocked(scanMapTiles, centerIndex, 'S'))
			currentDir = "S";
		else
			currentDir = "E";
		return currentDir.charAt(0);
	}
	
	public boolean isDirBlocked(MapTile[][] scanMapTiles, int centerIndex, char dir) {
		int xDelta = 0;
		int yDelta = 0;
		switch(dir) {
		case 'N':
			yDelta = -1;
			break;
		case 'S':
			yDelta = 1;
			break;
		case 'W':
			xDelta = -1;
			break;
		case 'E':
			xDelta = 1;
			break;			
		}
		
		return (scanMapTiles[centerIndex + xDelta][centerIndex + yDelta].getHasRover()
				|| scanMapTiles[centerIndex + xDelta][centerIndex + yDelta].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + xDelta][centerIndex + yDelta].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + xDelta][centerIndex + yDelta].getTerrain() == Terrain.SAND);
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


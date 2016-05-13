package enums;

public enum RoverListenPorts {
	ROVER_10(5000), ROVER_11(5001), ROVER_12(5002), ROVER_13(5003), ROVER_14(5004), ROVER_15(5005), ROVER_16(
			5006), ROVER_17(5007), ROVER_18(5008), DEFAULT(10000);

	private int port;

	public int getPort() {
		return this.port;
	}

	private RoverListenPorts(int port) {
		this.port = port;
	}

	public static RoverListenPorts getEnum(String input) {
		RoverListenPorts output;

		switch (input) {
		case "ROVER_10":
			output = RoverListenPorts.ROVER_10;
			break;
		case "ROVER_11":
			output = RoverListenPorts.ROVER_11;
			break;
		case "ROVER_12":
			output = RoverListenPorts.ROVER_12;
			break;
		case "ROVER_13":
			output = RoverListenPorts.ROVER_13;
			break;
		case "ROVER_14":
			output = RoverListenPorts.ROVER_14;
			break;
		case "ROVER_15":
			output = RoverListenPorts.ROVER_15;
			break;
		case "ROVER_16":
			output = RoverListenPorts.ROVER_16;
			break;
		case "ROVER_17":
			output = RoverListenPorts.ROVER_17;
			break;
		case "ROVER_18":
			output = RoverListenPorts.ROVER_18;
			break;
		default:
			output = RoverListenPorts.DEFAULT;
		}
		return output;
	}
}

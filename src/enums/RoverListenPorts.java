package enums;

public enum RoverListenPorts {
	ROVER_00 (4000),
	ROVER_99 (4001),
	ROVER_10 (4002),
	DEFAULT (10000);
	
	private int port;
	
	public int getPort() {
		return this.port;
	}
	
	private RoverListenPorts(int port) {
		this.port = port;
	}
	
	public static RoverListenPorts getEnum(String input){
    	RoverListenPorts output;
    	
    	switch(input){
    	case "ROVER_10":
    		output = RoverListenPorts.ROVER_10;
    		break;
    	case "ROVER_00":
    		output = RoverListenPorts.ROVER_00;
    		break;
    	case "ROVER_99":
    		output = RoverListenPorts.ROVER_99;
    		break;   	
    	default:
    		output = RoverListenPorts.DEFAULT;
    	}	
    	return output;
    }
}

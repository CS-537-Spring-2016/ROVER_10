package enums;

//port #'s for each of rovers 10 through 18
public enum RoverListenPorts {
	ROVER_10 (4000),
	ROVER_11(5000),
	ROVER_12(6000),
	ROVER_13(7000),
	ROVER_14(8000),
	ROVER_15(9000),
	ROVER_16(10000),
	ROVER_17(11000),
	ROVER_18(12000),
	DEFAULT (12345);
	
	private int port;
	
	public int getPort() {
		return this.port;
	}
	
	private RoverListenPorts(int port) {
		this.port = port;
	}
	
	public static RoverListenPorts getEnum(String input){
    	RoverListenPorts roverPort;
    	
    	switch(input){
    	case "ROVER_10":
    		roverPort = RoverListenPorts.ROVER_10;
    		break;
    	case "ROVER_11":
    		roverPort = RoverListenPorts.ROVER_11;
    		break;
    	case "ROVER_12":
    		roverPort = RoverListenPorts.ROVER_12;
    		break; 
    	case "ROVER_13":
    		roverPort = RoverListenPorts.ROVER_13;
    		break;
       	case "ROVER_14":
    		roverPort = RoverListenPorts.ROVER_14;
    		break;    		
       	case "ROVER_15":
    		roverPort = RoverListenPorts.ROVER_15;
    		break;
     	case "ROVER_16":
    		roverPort = RoverListenPorts.ROVER_16;
    		break;  		
     	case "ROVER_17":
    		roverPort = RoverListenPorts.ROVER_17;
    		break;
     	case "ROVER_18":
    		roverPort = RoverListenPorts.ROVER_18;
    		break;
    	default:
    		roverPort = RoverListenPorts.DEFAULT;
    	}	
    	return roverPort;
    }
}

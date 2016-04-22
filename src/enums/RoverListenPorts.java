package enums;

public enum RoverListenPorts {
	ROVER_00 (4000),
	ROVER_99 (4001),
	ROVER_10 (4002);
	
	private int port;
	
	public int getPort() {
		return this.port;
	}
	
	private RoverListenPorts(int port) {
		this.port = port;
	}
}

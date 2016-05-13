package testRoverComm;

import enums.RoverListenPorts;

public class R2rApp {
	public static void main(String[] args) throws Exception {
		ROVER_TEST rover10 = new ROVER_TEST("ROVER_10");
		ROVER_TEST rover11 = new ROVER_TEST("ROVER_11");
		ROVER_TEST rover12 = new ROVER_TEST("ROVER_12");
		ROVER_TEST rover13 = new ROVER_TEST("ROVER_13");
		ROVER_TEST rover14 = new ROVER_TEST("ROVER_14");
		ROVER_TEST rover15 = new ROVER_TEST("ROVER_15");
		ROVER_TEST rover16 = new ROVER_TEST("ROVER_16");
		ROVER_TEST rover17 = new ROVER_TEST("ROVER_17");
		ROVER_TEST rover18 = new ROVER_TEST("ROVER_18");
		
		//start the rover servers
		rover10.startRoverServer();
		rover11.startRoverServer();
		rover12.startRoverServer();
		rover13.startRoverServer();
		rover14.startRoverServer();
		rover15.startRoverServer();
		rover16.startRoverServer();
		rover17.startRoverServer();
		rover18.startRoverServer();
		
		String ipAddr = rover10.getIP();
		System.out.println("my ip address..." + ipAddr);
		//test if a rover can connect to other rovers and send msg to them
		rover10.connect(ipAddr, RoverListenPorts.ROVER_11.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_12.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_13.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_14.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_15.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_16.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_17.getPort());
		rover10.connect(ipAddr, RoverListenPorts.ROVER_18.getPort());
				
		//allow some time to connect before showing what rovers rover10 is connected to
		Thread.sleep(1000);
		
		System.out.println("Rover 10's peers: ");
		rover10.displayList();

		Thread.sleep(2000);
//		System.out.println("rover 99's connected peers...");

		rover10.sendMessage(1, "ROVER hello rover 00");
		rover11.sendMessage(1, "ROVER oh hello rover 10 from rover 11!");
		rover12.sendMessage(1, "ROVER hello rover 10 from rover 12!");
	}
}

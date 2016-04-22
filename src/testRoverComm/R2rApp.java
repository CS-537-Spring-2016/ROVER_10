package testRoverComm;

import enums.RoverListenPorts;

public class R2rApp {
	public static void main(String[] args) throws Exception {
		ROVER_TEST rover10 = new ROVER_TEST("ROVER_10");
		ROVER_TEST rover00 = new ROVER_TEST("ROVER_00");
		ROVER_TEST rover99 = new ROVER_TEST("ROVER_99");
		rover10.startRoverServer();
		rover00.startRoverServer();
		rover99.startRoverServer();
		
		String ipAddr = rover10.getIP();
		System.out.println("my ip address..." + ipAddr);
		//test if a rover can connect to other rovers and send msg to them
		rover10.connect(ipAddr, RoverListenPorts.ROVER_00.getPort());
		
		//allow some time to connect before showing what rovers rover10 is connected to
		Thread.sleep(2000);
		rover10.displayList();
		//by the time rover10 is connceted, rover00 will too, so the list should display
		rover00.displayList();
	
		rover10.connect(ipAddr, RoverListenPorts.ROVER_99.getPort());
		Thread.sleep(2000);
//		System.out.println("rover 99's connected peers...");
		rover99.displayList();
		rover10.sendMessage(1, "ROVER hello rover 00");
		rover00.sendMessage(1, "ROVER oh hello rover 10!");
	}
}

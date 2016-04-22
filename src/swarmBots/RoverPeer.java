package swarmBots;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class RoverPeer {
	
    private int id;
    private String host;
    private int port;
    private Socket socket;
    private DataOutputStream output;

    public RoverPeer(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;

        try {
            socket = new Socket(host, port);
            output = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataOutputStream getOutput() {
        return output;
    }

}

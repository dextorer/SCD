package it.unipd.scd.scdcommunication;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class SCDComm extends WebSocketClient {

	private CommInterface mInterface;

    public static final String MANAGER_VISITORS_ENDPOINT = "managerVisitors/registerForStatistics";
    public static final String MANAGER_HOME_ENDPOINT = "managerHome/registerForStatistics";
    public static final String FIELD_ENDPOINT = "field/registerForEvents";

    public SCDComm(String host, String port, String endpoint, CommInterface iface) {
		super(URI.create("ws://" + host + ":" + port + "/" + endpoint));

//        super(URI.create("ws://localhost:28000/field/registerForEvents"));

		mInterface = iface;
	}
	
	public SCDComm(URI serverURI, CommInterface iface) {
		super(serverURI);
		
		mInterface = iface;
	}

	public void initConnection() {
		System.out.println("Trying to connect to " + getURI());
		this.connect();
	}
	
	public void terminateConnection() {
		this.close();
	}

    public void terminateConnection(String message) {
        this.close(1000, message);
    }

	@Override
	public void onClose(int code, String reason, boolean remote) {
		mInterface.onCommMessage("Connection with the server was closed, please reconnect.\n\nReason: " + reason);
	}

	@Override
	public void onError(Exception e) {
		e.printStackTrace();
		mInterface.onCommMessage(e.getMessage());
	}

	@Override
	public void onMessage(String message) {
		mInterface.onMessage(message);
	}

	@Override
	public void onOpen(ServerHandshake handshake) {
		mInterface.onCommMessage("Connection with the server successfully opened!");
	}
}

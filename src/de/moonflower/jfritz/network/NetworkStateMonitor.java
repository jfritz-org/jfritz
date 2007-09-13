package de.moonflower.jfritz.network;

import java.util.Vector;

public class NetworkStateMonitor  {

	public static ServerConnectionThread serverConnection;

	public static ClientConnectionListener clientConnectionListener;

	public static void startServer(){
		if(clientConnectionListener == null){
			clientConnectionListener = new ClientConnectionListener();
			clientConnectionListener.setDaemon(true);
			clientConnectionListener.setName("Client listener thread");
			clientConnectionListener.start();
		}

		clientConnectionListener.startListening();
	}

	public static void stopServer(){
		clientConnectionListener.stopListening();
	}

	private static Vector<NetworkStateListener> listeners = new Vector<NetworkStateListener>();

	public static void addListener(NetworkStateListener listener){
		listeners.add(listener);
	}

	public static void removeListener(NetworkStateListener listener){
		listeners.remove(listener);
	}

	public static void clientStateChanged(){
		for(NetworkStateListener listener: listeners)
			listener.clientStateChanged();
	}

	public static void serverStateChanged(){
		for(NetworkStateListener listener: listeners)
			listener.serverStateChanged();
	}

	public static boolean isConnectedToServer(){
		return ServerConnectionThread.isConnected();
	}

	public static boolean isListening(){
		return ClientConnectionListener.isListening();
	}

	public static void startClient(){
		if(serverConnection == null){
			serverConnection = new ServerConnectionThread();
			serverConnection.setDaemon(true);
			serverConnection.setName("Server connection thread");
			serverConnection.start();
		}

		serverConnection.connectToServer();
	}

	public static void stopClient(){
		if(serverConnection != null){
			serverConnection.disconnectFromServer();
		}
	}

	public static void requestLookupFromServer(){
		serverConnection.requestLookup();
	}

	public static void requestGetCallListFromServer(){
		serverConnection.requestGetCallList();
	}

}

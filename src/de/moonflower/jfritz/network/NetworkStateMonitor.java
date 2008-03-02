package de.moonflower.jfritz.network;

import java.util.Vector;

import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * This class is used as a sort of static back end for accessing and changing
 * settings for the client oder server.
 *
 * This is also used a central way for the connection threads to notify
 * the GUI of changes to their state
 *
 * @author brian
 *
 */
public class NetworkStateMonitor  {

	public static ServerConnectionThread serverConnection;

	public static ClientConnectionListener clientConnectionListener;

	private static Vector<NetworkStateListener> listeners = new Vector<NetworkStateListener>();

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

	public static void requestSpecificLookupFromServer(PhoneNumber number, String siteName){
		serverConnection.requestSpecificLookup(number, siteName);
	}

	public static void requestGetCallListFromServer(){
		serverConnection.requestGetCallList();
	}

	public static void requestDeleteList(){
		serverConnection.requestDeleteList();
	}

	/**
	 * This code here should take care of the case when the settings
	 * have changed while the server is currently running.
	 *
	 * Right now client priviledges are checked dynamically on a per request basis
	 * so no to reset anything there. The only thing that needs to be checked for
	 * is the the port being used, and the max number of connections.
	 *
	 */
	public static void serverSettingsChanged(){

		clientConnectionListener.settingsChanged();

	}

}

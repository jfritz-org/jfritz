package de.moonflower.jfritz.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;

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
	private final static Logger log = Logger.getLogger(NetworkStateMonitor.class);

	public static ServerConnectionThread serverConnection;

	public static ClientConnectionListener clientConnectionListener;

	private static Vector<NetworkStateListener> listeners = new Vector<NetworkStateListener>();

	protected static PropertyProvider properties = PropertyProvider.getInstance();

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

	public static void requestSpecificLookupFromServer(PhoneNumberOld number, String siteName){
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

	/**
	 * Check if direct dialing is available, if we are connected to a server
	 * or if we have a valid firmware
	 *
	 * @return whether direct dialing is available
	 */
	public static boolean hasAvailablePorts(){
		if(properties.getProperty("option.clientCallList").equals("true")
				&& isConnectedToServer())
		{
			return serverConnection.hasAvailablePorts();
		}

		else if(JFritz.getBoxCommunication().getAvailablePorts() != null)
		{
			return true;
		}

		return false;
	}

	public static Vector<Port> getAvailablePorts(){
		log.info("Fix getAvailablePorts() in NetworkStateMonitor");
//		if(Main.getProperty("option.clientCallList").equals("true")
//				&& isConnectedToServer())
//		{
//			return serverConnection.getAvailablePorts();
//		}

		return JFritz.getBoxCommunication().getAvailablePorts();
	}

	/**
	 * send the direct dial request to the server only if we are connected
	 * otherwise send it directly to the box
	 * @throws IOException
	 * @throws WrongPasswordException
	 * @throws UnsupportedEncodingException
	 *
	 */
	public static void doCall(String number, Port port) throws UnsupportedEncodingException, WrongPasswordException, IOException {
		if(properties.getProperty("option.clientCallList").equals("true")
				&& isConnectedToServer())
		{
			serverConnection.requestDoCall(new PhoneNumberOld(properties, number, false, false), port);
		}
		else
		{
			JFritz.getBoxCommunication().doCall(new PhoneNumberOld(properties, number, false, false), port);
		}
	}

	public static void hangup(Port port) throws IOException, WrongPasswordException
	{
		if(properties.getProperty("option.clientCallList").equals("true")
				&& isConnectedToServer())
		{
			serverConnection.requestHangup(port);
		}
		else
		{
			JFritz.getBoxCommunication().hangup(port);
		}
	}
}

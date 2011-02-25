package de.moonflower.jfritz.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Vector;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;

/**
 * This class is responsible for listening for new client connections
 * on the user specified port. On an incoming connection this thread
 * creates a new ClientConnectionThread to handle the communication
 * with the new client.
 *
 * So how does the whole network code work?
 *
 * Its probably best to first describe the general architecture of
 * the network code. JFritz uses a client / server model, where the
 * information flow is controlled by server. All information is exchanged
 * asynchronously between the server and the client, with the exception
 * of the authentication protocol :). The server and client use object
 * streams to exchange information, the server sends DataChange objects
 * and the client sends ClientDataRequest, ClientActionRequest objects.
 *
 * What code is responsible for what function?
 *
 * The ClientConnectionListener listens for new connections on the specified
 * port in the options dialog. When an incoming connection is received, a new
 * ClientConnectionThread is spawned. This thread handles the authentication with
 * the client and then is responsible for listening for incoming requests
 * from the client. Once successfully authenticated the ClienConnectionThread
 * spawns a new ServerSenderThread, whose job it is to filter and send the data
 * to the client. The ClientConnectionThread communicates with the ServerSenderThread
 * by using a queue.
 *
 * Once the the SenderThread is started the ClientConnectionThread registers
 * itself in CallerList as a CallerListListener, in the PhoneBook as a
 * PhoneBookListener and as a CallMonitorListener. These Interfaces specify that
 * the ClientConnectionThread is notified (using various function calls) of changes
 * to the data to the phonebook and the call list. The ClientConnectionThread then
 * packs these changes into DataChange objects and puts them in the output queue of the
 * SenderThread, that is if the client has the appropriate rights to receive
 * this information.
 *
 * The client on the other hand communicates with the server using only one
 * thread, as writing objects to only one socket won't have a noticeable
 * drop in the gui reaction time. Once connected and authenticated with
 * the server, the ServerConnectionThread then listens for all incoming
 * changes, and depending upon whether the client should use the phone book,
 * call monitor and call list from the server and not use its own, the client sends
 * requests for reverse lookups, updating the call list to the server instead of
 * trying to access a fritz box.The client also registers itself as a callerListListener
 * and a PhoneBookListener and writes all changes to its personal data to the server,
 * however the server will determine whether to accept or ignore these changes
 * based on the clients rights.
 *
 * How does the protocol work?
 *
 * Upon connection the server writes a hello string in plain text containing
 * its name and version number, right now this is "JFRITZ SERVER 1.0". Then
 * the server writes the request for the clients login name, "login:". The
 * client then responds in clear writing its login name. The server then writes
 * the client the data key (the randomly generated key, that will be used to
 * transfer data throughout the rest of the connection) encrypted using an
 * md5 hash of the clients password as the key (referred to as the auth key).
 * The client then decrypts the data key using its copy of the password.
 * The client then writes an ok response to the server, encrypted using the
 * newly decrypted data key, currently this string is
 * "OK". The server responds to this string "OK". If either of the two
 * don't understand the others response, they close the connection, as
 * there has been a problem with authentication (most likely both passwords
 * don't match). If everything went alright the communication continues
 * asynchronuously encrypted using the data key.
 *
 * The exchange of encrypted objects is done by packing regular objects
 * into SecureObjects. For the rest of the connection information is
 * exchanged on demand between the two. The Server does however send
 * keep alive messages to the client, currently every 60 seconds,
 * so that a defunct connection can be identified and terminated, or
 * restarted if wished. This is handled by the server, using the
 * ServerKeepAliveTask to send the keep alive string. The client then
 * has 30 seconds to repsond to the keep alive string.When either of
 * the two want to end the connection they send the string "JFRITZ CLOSE".
 * For even more detailed information see the classes named above.
 *
 * @author Brian
 *
 */
public class ClientConnectionListener extends Thread {

	private static boolean isListening = false;

	private static boolean listen = false;

	private static Vector<ClientConnectionThread> connectedClients;

	private static ServerSocket serverSocket;

	private static boolean quit = false;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public void run(){
		Debug.netMsg("client connection listener started");
		connectedClients = new Vector<ClientConnectionThread>();
		while(!quit){
			if(!listen){
				try{
					synchronized(this){
						wait();
					}

				}catch(InterruptedException e){
					Debug.error("Server thread was interuppted!");
		        	Thread.currentThread().interrupt();
				}
			}else {

				try{
					serverSocket = new ServerSocket(Integer.parseInt(
							properties.getProperty("clients.port")));

					Debug.netMsg("Listening for client connections on: "+
							properties.getProperty("clients.port"));
					isListening = true;
					NetworkStateMonitor.serverStateChanged();

					while(listen){

						//make sure we dont exceed our maximum amount of connections
						if(Integer.parseInt(properties.getProperty("max.Connections")) <= connectedClients.size()){

							synchronized(this){
								try{
									Debug.netMsg("Max number of clients reached, waiting for one to quit");
									wait();
								}catch(InterruptedException e){
									Debug.error("Client listener interrupted while waiting for connection to close!");
						        	Thread.currentThread().interrupt();
								}
							}
						}else{
							// we have at least one more connection slot free
							Debug.netMsg("Client Connection Listener waiting for incoming connection");
							ClientConnectionThread connection = new ClientConnectionThread(serverSocket.accept(), this);
							connection.setName("Client connection listener");
							connection.setDaemon(true);

							synchronized(this){
								connectedClients.add(connection);
							}

							connection.start();
						}
					}

					serverSocket.close();

				}catch(SocketException e){
					if(e.getMessage().equals("Socket closed"))
						Debug.netMsg("Server socket closed");
					else{
						Debug.error(e.toString());
						e.printStackTrace();
					}

				}catch(IOException e){
					Debug.errDlg(messages.getMessage("error_binding_port") + ": " + properties.getProperty("clients.port"));
					Debug.error(e.toString());
					e.printStackTrace();
				}

				isListening = false;
				listen = false;

				Debug.netMsg("Closing all open client connections");
				synchronized(this){
					for(ClientConnectionThread client: connectedClients)
						client.closeConnection();
				}

				NetworkStateMonitor.serverStateChanged();
			}
		}

		//Always display a message, so we know the thread ended cleanly
		Debug.netMsg("ClientConnectionListener has exited cleanly");

	}

	/** Used by the gui code to determine if the server is listening
	 *
	 * @return status of the client listener thread
	 */
	public static boolean isListening(){
		return isListening;
	}

	/**
	 * Wakes the thread up which proceeds to open a serverSocket
	 * connection to listen for new incoming connections.
	 *
	 */
	public synchronized void startListening(){
		listen = true;
		notify();
	}

	/**
	 * This function is called by  a ClientConnectionThread to indicate
	 * that a particular connection has ended
	 *
	 * @param connection that has ended
	 */
	public synchronized void clientConnectionEnded(ClientConnectionThread connection){
			connectedClients.remove(connection);
			notify();
	}

	/**
	 * This function stops the ClientConnectionListioner and puts the
	 * thread to sleep
	 *
	 */
	public synchronized void stopListening(){
		listen = false;
		quit = true;
		Debug.netMsg("Stopping client listener!");
		try{
			serverSocket.close();

		}catch(IOException e){
			Debug.error("Error closing server socket: " + e.toString());
			e.printStackTrace();
		}

		notify();
	}

	/**
	 * This function must be called to terminate the thread when the
	 * application is going to terminate
	 *
	 */
	public synchronized void quitThread(){
		quit = true;
		notify();
	}

	/**
	 * Is called to notify the listener that settings have changed.
	 * Code to act on this information should go here
	 *
	 */
	public synchronized void settingsChanged(){
		notify();

	}

}

package de.moonflower.jfritz.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.CallerListListener;
import de.moonflower.jfritz.phonebook.PhoneBookListener;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;

/**
 * This class is responsible for seting up a connection to a
 * JFritz server. All communication between server and client
 * are asynchronus in nature.
 *
 * All communication from client to server
 * uses either ClientDataRequest, ClientActionRequest or String objects,
 * whereas the String objects are intended only to pass messages to the
 * server (like client closing the connection).
 *
 * All communication from server to client uses either DataChange or
 * String objects, where the String objects are also used to pass messages.
 *
 * @author brian
 *
 */
public class ServerConnectionThread extends Thread implements CallerListListener,
		PhoneBookListener {

	private static boolean isConnected = false;

	private static boolean connect = false;

	private Socket socket;

	private ObjectInputStream objectIn;

	private ObjectOutputStream objectOut;

	private ClientDataRequest<Call> callListRequest;

	private ClientDataRequest<Person> phoneBookRequest;

	private ClientActionRequest actionRequest;

	private boolean quit = false;

	private boolean callsAdded = false, callsRemoved=false, callUpdated=false,
		contactsAdded=false, contactsRemoved=false, contactUpdated=false;

	/**
	 * Returns the current state of this thread
	 *
	 * @return the state of the connection to the server
	 */
	public static boolean isConnected(){
		return isConnected;
	}

	/**
	 * Starts the thread and attempts to build a connection to the
	 * user specified server
	 *
	 */
	public synchronized void connectToServer(){
		connect = true;
		notify();
	}

	/**
	 * This method is used to cleanly kill a connection and put the current
	 * thread into sleep mode
	 *
	 */
	public synchronized void disconnectFromServer(){
		try{
			Debug.msg("Writing disconnect message to the server");
			objectOut.writeObject("JFRITZ CLOSE");
			objectOut.flush();
			objectOut.close();
			objectIn.close();
			socket.close();
		}catch(IOException e){
			Debug.err("Error writing disconnect message to server");
			Debug.err(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * This is where the connection is initiated and the client is
	 * synchronized with the server
	 *
	 */
	public void run(){
		while(!quit){
			if(!connect){
				try{
					synchronized(this){
						wait();
					}
				}catch(InterruptedException e){
					Debug.err("SeverConnection Thread was interrupted!");
				}
			}else{
				String server, user, password;
				int port;

				server = Main.getProperty("server.name", "");
				port = Integer.parseInt(Main.getProperty("server.port", "0"));
				user = Main.getProperty("server.login", "");
				password = Encryption.decrypt(Main.getProperty("server.password", ""));

				Debug.msg("Attempting to connect to server");
				Debug.msg("Server: "+ server);
				Debug.msg("Port: "+port);
				Debug.msg("User: "+user);
				Debug.msg("Pass: "+password);

				try{
					socket = new Socket(server, port);
					Debug.msg("successfully connected to server, authenticating");
					objectOut = new ObjectOutputStream(socket.getOutputStream());
					objectIn = new ObjectInputStream(socket.getInputStream());

					if(authenticateWithServer(user, password)){
						Debug.msg("Successfully authenticated with server");
						isConnected = true;
						NetworkStateMonitor.clientStateChanged();

						callListRequest = new ClientDataRequest<Call>();
						callListRequest.destination = ClientDataRequest.Destination.CALLLIST;

						phoneBookRequest = new ClientDataRequest<Person>();
						phoneBookRequest.destination = ClientDataRequest.Destination.PHONEBOOK;

						actionRequest = new ClientActionRequest();

						JFritz.getCallerList().addListener(this);
						JFritz.getPhonebook().addListener(this);

						synchronizeWithServer();
						listenToServer();

						JFritz.getCallerList().removeListener(this);
						JFritz.getPhonebook().removeListener(this);

					}else{
						Debug.msg("Authentication failed!");
						Debug.errDlg(Main.getMessage("authentification_failed"));

					}

					objectOut.close();
					objectIn.close();
					socket.close();

				}catch(ConnectException e){

					Debug.errDlg(Main.getMessage("connection_server_refused"));
					Debug.err("Error connecting to the server");
					Debug.err(e.toString());
					e.printStackTrace();

				}catch(IOException e){
					Debug.err(e.toString());
					e.printStackTrace();
				}

				isConnected = false;
				NetworkStateMonitor.clientStateChanged();
				connect = false;

			}

			Debug.msg("Connection to server closed");
			//TODO: Cleanup code here!
		}
	}

	/**
	 * function attempts to login to the user specified server
	 *
	 * @param user username of the account on the server
	 * @param password password of the account on the server
	 * @return whether the client successfully connected to the server or not
	 */
	private boolean authenticateWithServer(String user, String password){
		Object o;
		String response;
		try{
			//set timeout in case server thread is not functioning properly
			socket.setSoTimeout(15000);
			o = objectIn.readObject();
			if(o instanceof String){

				response = (String) o;
				Debug.msg("Connected to JFritz Server: "+response);

				for(int i=0; i < 3; i++){
					objectOut.writeObject(user);
					objectOut.writeObject(password);
					objectOut.flush();
					o = objectIn.readObject();

					if(o instanceof String){
						response = (String) o;
						if(response.equals("JFRITZ 1.0 OK")){
							//remove timeout, no need since all communcation is asynchronous anyways
							socket.setSoTimeout(0);
							return true;
						}

						else if(response.equals("JFRITZ 1.0 INVALID"))
							Debug.msg("login attempt refused by server");
						else
							Debug.msg("unrecognized response from server: "+response);
					}else
						Debug.msg("unexpected object received from server: "+o.toString());

				}
			}else
				Debug.msg("Server identification invalid, canceling login attempt: "+o.toString());

		}catch(ClassNotFoundException e){
			Debug.err("Server authentication response invalid!");
			Debug.err(e.toString());
			e.printStackTrace();
		}catch(EOFException e){
			Debug.err("Server closed Stream unexpectedly!");
			Debug.err(e.toString());
			e.printStackTrace();
		}catch(SocketTimeoutException e){
			Debug.err("Read timeout while authenticating with server!");
			Debug.err(e.toString());
			e.printStackTrace();
		}catch(IOException e){
			Debug.err("Error reading response during authentication!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * function gets all calls newer than the newest call in the call list
	 * and gets all contacts from the server.
	 *
	 */
	private synchronized void synchronizeWithServer(){

		Debug.msg("Requesting updates from server");
		try{
			callListRequest.operation = ClientDataRequest.Operation.GET;
			callListRequest.timestamp = JFritz.getCallerList().getLastCallDate();
			objectOut.writeObject(callListRequest);
			objectOut.flush();
			objectOut.reset(); //reset the streams object cache!

			phoneBookRequest.operation = ClientDataRequest.Operation.GET;
			objectOut.writeObject(phoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing synchronizing request to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * function listens to commands issued by the server, can only
	 * be exited by closing the object streams or receiving a
	 * close request from the server
	 *
	 */
	private void listenToServer(){
		Vector<Call> vCalls;
		Vector<Person> vPersons;
		DataChange change;
		Object o;
		String message;

		Debug.msg("Listening for commands from server");
		while(true){
			try{
				o = objectIn.readObject();
				if(o instanceof DataChange){

					change = (DataChange) o;
						if(change.destination == DataChange.Destination.CALLLIST){
							if(change.operation == DataChange.Operation.ADD){

								vCalls = (Vector<Call>) change.data;
								Debug.msg("Received request to add "+vCalls.size()+" calls");

								//lock the call list so the new entries don't ping pong back and forth
								synchronized(JFritz.getCallerList()){
									callsAdded = true;
									JFritz.getCallerList().addEntries(vCalls);
									callsAdded = true;
								}

							}else if(change.operation == DataChange.Operation.REMOVE){

								vCalls = (Vector<Call>) change.data;
								Debug.msg("Received request to remove "+vCalls.size()+" calls");

								synchronized(JFritz.getCallerList()){
									callsRemoved = true;
									JFritz.getCallerList().removeEntries(vCalls);
									callsRemoved = false;
								}

							}else if(change.operation == DataChange.Operation.UPDATE){

								Debug.msg("Received request to upate a call");
								synchronized(JFritz.getCallerList()){
									callUpdated=true;
									JFritz.getCallerList().updateEntry((Call) change.original, (Call) change.updated);
									callUpdated=false;
								}
							}

						}else if(change.destination == DataChange.Destination.PHONEBOOK){
							if(change.operation == DataChange.Operation.ADD){

								vPersons = (Vector<Person>) change.data;
								Debug.msg("Received request to add "+vPersons.size()+" contacts");

								synchronized(JFritz.getCallerList()){
									contactsAdded = true;
									JFritz.getPhonebook().addEntries(vPersons);
									contactsAdded = false;
								}

							}else if(change.operation == DataChange.Operation.REMOVE){

								vPersons = (Vector<Person>) change.data;
								Debug.msg("Received request to remove "+vPersons.size()+" contacts");

								synchronized(JFritz.getPhonebook()){
									contactsRemoved = true;
									JFritz.getPhonebook().removeEntries(vPersons);
									contactsRemoved = false;
								}

							}else if(change.operation == DataChange.Operation.UPDATE){

								Debug.msg("Recieved request to update a contact");

								synchronized(JFritz.getPhonebook()){
									contactUpdated = true;
									JFritz.getPhonebook().updateEntry((Person) change.original, (Person) change.updated);
									contactUpdated = false;
								}
							}

						}else{
							Debug.msg("destination not chosen for incoming data, ignoring!");
						}
				}else if(o instanceof String){ //message received from the server

					message = (String) o;
					Debug.msg("Received message from server: "+message);

					if(message.equals("JFRITZ CLOSE")){
						Debug.msg("Closing connection with server!");
						disconnect();
						return;
					} //TODO: Add other messages here if necessary

				}else {
					Debug.msg(o.toString());
					Debug.msg("received unexpected object, ignoring!");
				}

			}catch(ClassNotFoundException e){
				Debug.err("Response from server contained unkown object!");
				Debug.err(e.toString());
				e.printStackTrace();
			}catch(SocketException e){
				if(e.getMessage().equals("Socket closed")){
					Debug.msg("Socket closed");	//we closed the socket as requested by the user
				}else{
					Debug.err(e.toString());
					e.printStackTrace();
				}
				return;
			}catch(EOFException e ){
				Debug.err("Server closed stream unexpectedly!");
				Debug.err(e.toString());
				e.printStackTrace();
				return;
			}catch(IOException e){
				Debug.err(e.toString());
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Called when the server send a close request. This code makes sure that
	 * we aren't writing a request to the server as the streams are closed
	 *
	 */
	private synchronized void disconnect(){
		try{
			objectOut.close();
			objectIn.close();
			socket.close();
		}catch(IOException e){
			Debug.err("Error disconnecting from server");
			Debug.err(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Function used to quit this thread, should be called on application exit
	 *
	 */
	public synchronized void quitThread(){
		quit = true;
		notify();
	}


	public synchronized void requestLookup(){
		actionRequest.doLookup = true;
		try{
			objectOut.writeObject(actionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing lookup request to server");
			Debug.err(e.toString());
			e.printStackTrace();
		}
		actionRequest.getCallList = false;
	}

	public synchronized void requestGetCallList(){
		actionRequest.getCallList = true;

		try{

			objectOut.writeObject(actionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing do get list request");
			Debug.err(e.toString());
			e.printStackTrace();
		}
		actionRequest.getCallList = false;
	}

	public synchronized void callsAdded(Vector<Call> newCalls){

		//this thread added the new calls, so we don't need to write them back
		if(callsAdded)
			return;

		Debug.msg("Notifying the server of added calls, size: "+newCalls.size());
		callListRequest.data = newCalls;
		callListRequest.operation = ClientDataRequest.Operation.ADD;

		try{
			objectOut.writeObject(callListRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing new calls to the server");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		//remove reference to the data
		callListRequest.data = null;
	}

	public synchronized void callsRemoved(Vector<Call> removedCalls){

		//this thread removed the calls, no need to write them back
		if(callsRemoved)
			return;

		Debug.msg("Notifying the server of removed calls, size: "+removedCalls.size());
		callListRequest.data = removedCalls;
		callListRequest.operation = ClientDataRequest.Operation.REMOVE;

		try{
			objectOut.writeObject(callListRequest);
			objectOut.flush();
			objectOut.reset();
		}catch(IOException e){
			Debug.err("Error writing removed calls to the server");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		//remove reference to the data
		callListRequest.data = null;
	}

	public synchronized void callsUpdated(Call original, Call updated){

		if(callUpdated)
			return;

		Debug.msg("Notifying server of updated call");
		callListRequest.operation = ClientDataRequest.Operation.UPDATE;
		callListRequest.original = original;
		callListRequest.updated = updated;

		try{

			objectOut.writeObject(callListRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing updated call to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		//wipe out uneeded contents for later
		callListRequest.original = null;
		callListRequest.updated = null;

	}

	public synchronized void contactsAdded(Vector<Person> newContacts){

		//This thread added the contacts, no need to write them back
		if(contactsAdded)
			return;

		Debug.msg("Notifying the server of added contacts, size: "+newContacts.size());
		phoneBookRequest.data = newContacts;
		phoneBookRequest.operation = ClientDataRequest.Operation.ADD;

		try{
			objectOut.writeObject(phoneBookRequest);
			objectOut.flush();
			objectOut.reset();
		}catch(IOException e){
			Debug.err("Error writing new contacts to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.data = null;
	}

	public synchronized void contactsRemoved(Vector<Person> removedContacts){

		//This thread removed the contacts, no need to write them back
		if(contactsRemoved)
			return;

		Debug.msg("Notifying the server of removed contacts, size: "+removedContacts.size());
		phoneBookRequest.data = removedContacts;
		phoneBookRequest.operation = ClientDataRequest.Operation.REMOVE;

		try{

			objectOut.writeObject(phoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing removed contacts to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.data = null;
	}

	public synchronized void contactUpdated(Person original, Person updated){

		if(contactUpdated)
			return;

		phoneBookRequest.operation = ClientDataRequest.Operation.UPDATE;
		phoneBookRequest.original = original;
		phoneBookRequest.updated = updated;

		try{

			objectOut.writeObject(phoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing updated contact to server");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.original = null;
		phoneBookRequest.updated = null;
	}
}

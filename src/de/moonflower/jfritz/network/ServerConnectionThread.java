package de.moonflower.jfritz.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

import java.util.Vector;

import javax.crypto.*;
import javax.crypto.spec.*;

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
 *  @see de.moonflower.jfritz.network.ClientConnectionListener
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

	private Cipher inCipher;

	private Cipher outCipher;

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

		if(!isConnected)
			return;

		try{
			Debug.netMsg("Writing disconnect message to the server");
			SealedObject sealed_object = new SealedObject("JFRITZ CLOSE", outCipher);

			objectOut.writeObject(sealed_object);
			objectOut.flush();
			objectOut.close();
			objectIn.close();
			connect = false;
		}catch(IOException e){
			Debug.err("Error writing disconnect message to server");
			Debug.err(e.toString());
			e.printStackTrace();
		}catch(IllegalBlockSizeException e){
			Debug.err("Problems with the block size");
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

				Debug.netMsg("Attempting to connect to server");
				Debug.netMsg("Server: "+ server);
				Debug.netMsg("Port: "+port);
				Debug.netMsg("User: "+user);
				Debug.netMsg("Pass: "+password);

				try{
					socket = new Socket(server, port);
					Debug.netMsg("successfully connected to server, authenticating");

					//set timeout in case server thread is not functioning properly
					socket.setSoTimeout(20000);
					objectOut = new ObjectOutputStream(socket.getOutputStream());
					objectIn = new ObjectInputStream(socket.getInputStream());

					if(authenticateWithServer(user, password)){
						Debug.netMsg("Successfully authenticated with server");
						isConnected = true;
						NetworkStateMonitor.clientStateChanged();

						//reset the keep alive settings to more reasonable level
						socket.setSoTimeout(70000);

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
						Debug.netMsg("Connection to server closed");

					}else{
						Debug.netMsg("Authentication failed!");
						Debug.errDlg(Main.getMessage("authentification_failed"));
						connect = false;

					}

					objectOut.close();
					objectIn.close();

				}catch(ConnectException e){

					Debug.errDlg(Main.getMessage("connection_server_refused"));
					Debug.err("Error connecting to the server");
					Debug.err(e.toString());
					e.printStackTrace();
					connect = false;

				}catch(IOException e){
					Debug.errDlg(Main.getMessage("connection_server_refused"));
					Debug.err(e.toString());
					e.printStackTrace();
				}

				isConnected = false;
				NetworkStateMonitor.clientStateChanged();

				//if a connection is still wished, the socket was closed for some
				//reason, wait a short delay before retrying
				if(connect){

					synchronized(this){

						try{
							Debug.netMsg("Waiting 15 secs for retry attempt");
							wait(15000);
						}catch(InterruptedException e){
							Debug.err("ServerConnectionThread interrupted waiting to reconnect!");
							Debug.err(e.toString());
							e.printStackTrace();
						}
					}
				}

			}

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
		byte[] dataKey;

		try{

			o = objectIn.readObject();
			if(o instanceof String){

				//write out the username to the server and close the stream to free all resources
				response = (String) o;
				Debug.netMsg("Connected to JFritz Server: "+response);
				if(!response.equals("JFRITZ SERVER 1.0")){
					Debug.netMsg("Unkown Server version, newer JFritz protocoll version?");
					Debug.netMsg("Canceling login attempt!");
				}
				objectOut.writeObject(user);
				objectOut.flush();

				// compute the password md5 hash to get our authentication key
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(password.getBytes());

				// create our first private key, the auth key for authentication with the client
				DESKeySpec desKeySpec = new DESKeySpec(md.digest());
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
				SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

				// create the first cipher
				Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
				desCipher.init(Cipher.DECRYPT_MODE, secretKey);

				// read in our data key, encoded with the authentication key
				// and then close all resources associated with it
				SealedObject sealedObject = (SealedObject)objectIn.readObject();
				o = sealedObject.getObject(desCipher);
				if(o instanceof byte[]){
					dataKey = (byte[]) o;

					//create the second private key,  the data key
					desKeySpec = new DESKeySpec(dataKey);
					secretKey = keyFactory.generateSecret(desKeySpec);

					//prepare the two data key ciphers
					inCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
					outCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
					inCipher.init(Cipher.DECRYPT_MODE, secretKey);
					outCipher.init(Cipher.ENCRYPT_MODE, secretKey);

					//write the server our OK encoded with our new data key
					SealedObject sealed_ok = new SealedObject("OK", outCipher);
					objectOut.writeObject(sealed_ok);

					//read "OK" response from server
					SealedObject sealed_response = (SealedObject)objectIn.readObject();
					o = sealed_response.getObject(inCipher);
					if(o instanceof String){
						if(o.equals("OK")){		//server unstands us and we understand it
							return true;
						}else{
							Debug.netMsg("Server sent wrong string as response to authentication challenge!");
						}
					}else{
						Debug.netMsg("Server sent wrong object as response to authentication challenge!");
					}


				}else {
					Debug.netMsg("Server sent wrong type for data key!");
				}
			}

		}catch(ClassNotFoundException e){
			Debug.err("Server authentication response invalid!");
			Debug.err(e.toString());
			e.printStackTrace();

		}catch(NoSuchAlgorithmException e){
			Debug.netMsg("MD5 Algorithm not present in this JVM!");
			Debug.err(e.toString());
			e.printStackTrace();

		}catch(InvalidKeySpecException e){
			Debug.netMsg("Error generating cipher, problems with key spec?");
			Debug.err(e.toString());
			e.printStackTrace();

		}catch(InvalidKeyException e){
			Debug.netMsg("Error genertating cipher, problems with key?");
			Debug.err(e.toString());
			e.printStackTrace();

		}catch(NoSuchPaddingException e){
			Debug.netMsg("Error generating cipher, problems with padding?");
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
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Debug.err("Bad padding exception!");
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

		Debug.netMsg("Requesting updates from server");
		try{
			callListRequest.operation = ClientDataRequest.Operation.GET;
			callListRequest.timestamp = JFritz.getCallerList().getLastCallDate();
			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset(); //reset the streams object cache!

			phoneBookRequest.operation = ClientDataRequest.Operation.GET;
			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing synchronizing request to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
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

		Debug.netMsg("Listening for commands from server");
		while(true){
			try{
				SealedObject sealed_object = (SealedObject)objectIn.readObject();
				o = sealed_object.getObject(inCipher);
				if(o instanceof DataChange){

					change = (DataChange) o;
						if(change.destination == DataChange.Destination.CALLLIST){
							if(change.operation == DataChange.Operation.ADD){

								vCalls = (Vector<Call>) change.data;
								Debug.netMsg("Received request to add "+vCalls.size()+" calls");

								//lock the call list so the new entries don't ping pong back and forth
								synchronized(JFritz.getCallerList()){
									callsAdded = true;
									JFritz.getCallerList().addEntries(vCalls);
									callsAdded = true;
								}

							}else if(change.operation == DataChange.Operation.REMOVE){

								vCalls = (Vector<Call>) change.data;
								Debug.netMsg("Received request to remove "+vCalls.size()+" calls");

								synchronized(JFritz.getCallerList()){
									callsRemoved = true;
									JFritz.getCallerList().removeEntries(vCalls);
									callsRemoved = false;
								}

							}else if(change.operation == DataChange.Operation.UPDATE){

								Debug.netMsg("Received request to upate a call");
								synchronized(JFritz.getCallerList()){
									callUpdated=true;
									JFritz.getCallerList().updateEntry((Call) change.original, (Call) change.updated);
									callUpdated=false;
								}
							}

						}else if(change.destination == DataChange.Destination.PHONEBOOK){
							if(change.operation == DataChange.Operation.ADD){

								vPersons = (Vector<Person>) change.data;
								Debug.netMsg("Received request to add "+vPersons.size()+" contacts");

								synchronized(JFritz.getCallerList()){
									contactsAdded = true;
									JFritz.getPhonebook().addEntries(vPersons);
									contactsAdded = false;
								}

							}else if(change.operation == DataChange.Operation.REMOVE){

								vPersons = (Vector<Person>) change.data;
								Debug.netMsg("Received request to remove "+vPersons.size()+" contacts");

								synchronized(JFritz.getPhonebook()){
									contactsRemoved = true;
									JFritz.getPhonebook().removeEntries(vPersons);
									contactsRemoved = false;
								}

							}else if(change.operation == DataChange.Operation.UPDATE){

								Debug.netMsg("Recieved request to update a contact");

								synchronized(JFritz.getPhonebook()){
									contactUpdated = true;
									JFritz.getPhonebook().updateEntry((Person) change.original, (Person) change.updated);
									contactUpdated = false;
								}
							}

							//Call monitor event from the server
						}else if(change.destination == DataChange.Destination.CALLMONITOR
								&& Boolean.parseBoolean(Main.getProperty("option.callmonitorStarted", "false"))
								&& Main.getProperty("option.callMonitorType", "0").equals("6")){

							Debug.netMsg("Call monitor event received from server");
							//call in or disconnect event received
							String[] ignoredMSNs = Main.getProperty("option.callmonitor.ignoreMSN","").trim().split(";");
							boolean ignoreIt = false;

							if(change.original != null){

								Call c = (Call) change.original;

								// see if we need to ignore this call
								for (int i = 0; i < ignoredMSNs.length; i++) {
						            Debug.netMsg(ignoredMSNs[i]);
						            if (!ignoredMSNs[i].equals(""))
						                if (c.getRoute()
						                        .equals(ignoredMSNs[i])) {

						                    ignoreIt = true;
						                    break;
						                }
						        }

								if(ignoreIt)
									continue;

								//Pending call in event
								if(change.operation == DataChange.Operation.ADD &&
										Boolean.parseBoolean(Main.getProperty(
						                        "option.callmonitor.monitorIncomingCalls", "true"))){

									JFritz.getCallMonitorList().invokeIncomingCall(c);

									//Established call in event
								} else if(change.operation == DataChange.Operation.UPDATE &&
										Boolean.parseBoolean(Main.getProperty(
						                        "option.callmonitor.monitorIncomingCalls", "true"))){

									JFritz.getCallMonitorList().invokeIncomingCallEstablished(c);

									//Disconnect call event
								} else if(change.operation == DataChange.Operation.REMOVE){
									JFritz.getCallMonitorList().invokeDisconnectCall(c);
								}

								// call out event received
							} else if( change.updated != null && Boolean.parseBoolean(Main.getProperty(
										"option.callmonitor.monitorOutgoingCalls", "false"))){

								Call c = (Call) change.updated;

								//see if we need to ingnore the call
								for (int i = 0; i < ignoredMSNs.length; i++) {
						            Debug.msg(ignoredMSNs[i]);
						            if (!ignoredMSNs[i].equals(""))
						                if (c.getRoute()
						                        .equals(ignoredMSNs[i])) {

						                    ignoreIt = true;
						                    break;
						                }
						        }

								if(ignoreIt)
									continue;

								// call out pending event received
								if(change.operation == DataChange.Operation.ADD){
									JFritz.getCallMonitorList().invokeOutgoingCall(c);

								// call out established event received
								}else if(change.operation == DataChange.Operation.UPDATE){
									JFritz.getCallMonitorList().invokeOutgoingCallEstablished(c);
								}

							}

						}else{
							Debug.netMsg("destination not chosen for incoming data, ignoring!");
						}

				}else if(o instanceof String){ //message received from the server

					message = (String) o;

					if(message.equals("JFRITZ CLOSE")){
						Debug.netMsg("Closing connection with server!");
						disconnect();
						connect = false;
						return;
					}else if(message.equals("Party on, Wayne!")){
						Debug.netMsg("Received keep alive message from server");
						replyToKeepAlive();
					}else{
						Debug.netMsg("Received message from server: "+message);
					}

					//TODO: Add other messages here if necessary

				}else {
					Debug.netMsg(o.toString());
					Debug.netMsg("received unexpected object, ignoring!");
				}

			}catch(ClassNotFoundException e){
				Debug.err("Response from server contained unkown object!");
				Debug.err(e.toString());
				e.printStackTrace();
			}catch(SocketException e){
				if(e.getMessage().equals("Socket closed")){
					Debug.netMsg("Socket closed");	//we closed the socket as requested by the user
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
			} catch (IllegalBlockSizeException e) {
				Debug.err("Illegal block size exception!");
				Debug.err(e.toString());
				e.printStackTrace();
			} catch (BadPaddingException e) {
				Debug.err("Bad padding exception!");
				Debug.err(e.toString());
				e.printStackTrace();
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

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing lookup request to server");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}
		actionRequest.doLookup = false;
	}

	public synchronized void requestGetCallList(){
		actionRequest.getCallList = true;

		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing do get list request");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}
		actionRequest.getCallList = false;
	}

	public synchronized void callsAdded(Vector<Call> newCalls){

		//this thread added the new calls, so we don't need to write them back
		if(callsAdded)
			return;

		Debug.netMsg("Notifying the server of added calls, size: "+newCalls.size());
		callListRequest.data = newCalls;
		callListRequest.operation = ClientDataRequest.Operation.ADD;

		try{
			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing new calls to the server");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
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

		Debug.netMsg("Notifying the server of removed calls, size: "+removedCalls.size());
		callListRequest.data = removedCalls;
		callListRequest.operation = ClientDataRequest.Operation.REMOVE;

		try{
			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset();
		}catch(IOException e){
			Debug.err("Error writing removed calls to the server");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		//remove reference to the data
		callListRequest.data = null;
	}

	public synchronized void callsUpdated(Call original, Call updated){

		if(callUpdated)
			return;

		Debug.netMsg("Notifying server of updated call");
		callListRequest.operation = ClientDataRequest.Operation.UPDATE;
		callListRequest.original = original;
		callListRequest.updated = updated;

		try{

			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing updated call to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
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

		Debug.netMsg("Notifying the server of added contacts, size: "+newContacts.size());
		phoneBookRequest.data = newContacts;
		phoneBookRequest.operation = ClientDataRequest.Operation.ADD;

		try{
			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();
		}catch(IOException e){
			Debug.err("Error writing new contacts to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.data = null;
	}

	public synchronized void contactsRemoved(Vector<Person> removedContacts){

		//This thread removed the contacts, no need to write them back
		if(contactsRemoved)
			return;

		Debug.netMsg("Notifying the server of removed contacts, size: "+removedContacts.size());
		phoneBookRequest.data = removedContacts;
		phoneBookRequest.operation = ClientDataRequest.Operation.REMOVE;

		try{

			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing removed contacts to server!");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
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

			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing updated contact to server");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.original = null;
		phoneBookRequest.updated = null;
	}

	/**
	 * This function replies to a keep alive message sent form
	 * the server
	 *
	 */
	public  void replyToKeepAlive(){
		try{

			Debug.netMsg("Replying to servers keep alive message");
			SealedObject sealedPhoneBookRequest = new SealedObject("Party on, Garth!", outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			Debug.err("Error writing updated contact to server");
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}
	}


}

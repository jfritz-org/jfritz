package de.moonflower.jfritz.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.callerlist.CallerListListener;
import de.moonflower.jfritz.callmonitor.CallMonitorListener;
import de.moonflower.jfritz.phonebook.PhoneBookListener;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;

/**
 * This class is responsible for interacting with a JFritz client.
 * All communications between client and server are asynchronous.
 *
 * Communication between client and server is done using either
 * ClientRequest or String objects. Communication between server
 * and client is done using DataChange or String objects.
 *
 * This thread exits automatically once the connection has been closed.
 *
 * @author brian
 *
 */
public class ClientConnectionThread extends Thread implements CallerListListener,
			PhoneBookListener, CallMonitorListener {

	private Socket socket;

	private Login login;

	private InetAddress remoteAddress;

	private ObjectInputStream objectIn;

	private ObjectOutputStream objectOut;

	private ClientConnectionListener connectionListener;

	private DataChange<Call> callsAdd, callsRemove, callUpdate, callMonitor;

	private DataChange<Person> contactsAdd, contactsRemove, contactUpdate;

	private boolean callsAdded=false, callsRemoved=false, callUpdated = false,
		contactsAdded=false, contactsRemoved=false, contactUpdated=false;

	private ServerSenderThread sender;

	public ClientConnectionThread(Socket socket, ClientConnectionListener connectionListener){
		super("Client connection for "+socket.getInetAddress());
		this.socket = socket;
		this.connectionListener = connectionListener;
		remoteAddress = socket.getInetAddress();
	}

	public void run(){

		Debug.msg("Accepted incoming connection from "+remoteAddress);

		try{
			objectOut = new ObjectOutputStream(socket.getOutputStream());
			objectIn = new ObjectInputStream(socket.getInputStream());

			if((login = authenticateClient()) != null){

				Debug.msg("Authentication for client "+remoteAddress+" successful!");
				callsAdd = new DataChange<Call>();
				callsAdd.destination = DataChange.Destination.CALLLIST;
				callsAdd.operation = DataChange.Operation.ADD;
				callsRemove = new DataChange<Call>();
				callsRemove.destination = DataChange.Destination.CALLLIST;
				callsRemove.operation = DataChange.Operation.REMOVE;
				callUpdate = new DataChange<Call>();
				callUpdate.destination = DataChange.Destination.CALLLIST;
				callUpdate.operation = DataChange.Operation.UPDATE;
				callMonitor = new DataChange<Call>();
				callMonitor.destination = DataChange.Destination.CALLMONITOR;

				contactsAdd = new DataChange<Person>();
				contactsAdd.destination = DataChange.Destination.PHONEBOOK;
				contactsAdd.operation = DataChange.Operation.ADD;
				contactsRemove = new DataChange<Person>();
				contactsRemove.destination = DataChange.Destination.PHONEBOOK;
				contactsRemove.operation = DataChange.Operation.REMOVE;
				contactUpdate = new DataChange<Person>();
				contactUpdate.destination = DataChange.Destination.PHONEBOOK;
				contactUpdate.operation = DataChange.Operation.UPDATE;

				// create the sender thread, start it up, and set it for the min priority
				sender = new ServerSenderThread(objectOut, remoteAddress, login);
				sender.start();
				sender.setPriority(Thread.MIN_PRIORITY);

				JFritz.getCallerList().addListener(this);
				JFritz.getPhonebook().addListener(this);
				JFritz.getCallMonitorList().addCallMonitorListener(this);
				waitForClientRequest();

				JFritz.getCallerList().removeListener(this);
				JFritz.getPhonebook().removeListener(this);
				JFritz.getCallMonitorList().removeCallMonitorListener(this);
			}

		}catch(IOException e){
			Debug.err(e.toString());
			e.printStackTrace();
		}

		connectionListener.clientConnectionEnded(this);
	}

	/**
	 * this function listens for client requests until the
	 * connection is ended.
	 *
	 */
	public void waitForClientRequest(){
		Object o;
		ClientDataRequest dataRequest;
		ClientActionRequest actionRequest;
		String message;

		while(true){
			try{

				//currently only call list and phone book update
				//requests are supported
				o = objectIn.readObject();
				Debug.msg("received request from "+remoteAddress);
				if(o instanceof ClientDataRequest){

					dataRequest = (ClientDataRequest) o;
					if(dataRequest.destination == ClientDataRequest.Destination.CALLLIST){

						//determine what operation to carry out, if applicable
						if(dataRequest.operation == ClientDataRequest.Operation.GET){

							if(dataRequest.timestamp != null){
								Debug.msg("Received call list update request from "+remoteAddress);
								Debug.msg("Timestamp: "+dataRequest.timestamp.toString());
								callsAdded(JFritz.getCallerList().getNewerCalls(dataRequest.timestamp));
							}else{
								Debug.msg("Received complete call list request from "+remoteAddress);
								callsAdded(JFritz.getCallerList().getUnfilteredCallVector());
							}

						}else if(dataRequest.operation == ClientDataRequest.Operation.ADD && login.allowAddList){

							Debug.msg("Received request to add "+dataRequest.data.size()+" calls from "+remoteAddress);
							synchronized(JFritz.getCallerList()){
								callsAdded = true;
								JFritz.getCallerList().addEntries(dataRequest.data);
								callsAdded = false;
							}

						}else if(dataRequest.operation == ClientDataRequest.Operation.REMOVE && login.allowRemoveList){

							Debug.msg("Received request to remove "+dataRequest.data.size()+" calls from "+remoteAddress);
							synchronized(JFritz.getCallerList()){
								callsRemoved = true;
								JFritz.getCallerList().removeEntries(dataRequest.data);
								callsRemoved = false;
							}

						}else if(dataRequest.operation == ClientDataRequest.Operation.UPDATE && login.allowUpdateList){

							Debug.msg("Received request to update a call from "+remoteAddress);
							synchronized(JFritz.getCallerList()){
								callUpdated = true;
								JFritz.getCallerList().updateEntry((Call) dataRequest.original, (Call) dataRequest.updated);
								callUpdated = false;
							}
						}

					}else if(dataRequest.destination == ClientDataRequest.Destination.PHONEBOOK){

						//determine what operation to carry out, if applicable
						if(dataRequest.operation == ClientDataRequest.Operation.GET){
							Debug.msg("Received complete phone book request from "+remoteAddress);
							contactsAdded(JFritz.getPhonebook().getUnfilteredPersons());

						}else if(dataRequest.operation == ClientDataRequest.Operation.ADD && login.allowAddBook){

							Debug.msg("Received request to add "+dataRequest.data.size()+" contacts from "+remoteAddress);
							synchronized(JFritz.getPhonebook()){
								contactsAdded = true;
								JFritz.getPhonebook().addEntries(dataRequest.data);
								contactsAdded = false;
							}

						}else if(dataRequest.operation == ClientDataRequest.Operation.REMOVE && login.allowRemoveBook){

							Debug.msg("Received request to remove "+dataRequest.data.size()+" contacts from "+remoteAddress);
							synchronized(JFritz.getPhonebook()){
								contactsRemoved = true;
								JFritz.getPhonebook().removeEntries(dataRequest.data);
								contactsRemoved = false;
							}
						}else if(dataRequest.operation == ClientDataRequest.Operation.UPDATE && login.allowUpdateBook){

							Debug.msg("Received request to update a contact from "+remoteAddress);
							synchronized(JFritz.getPhonebook()){
								contactUpdated = true;
								JFritz.getPhonebook().updateEntry((Person) dataRequest.original, (Person) dataRequest.updated);
								contactUpdated = false;
							}
						}

					}else{
						Debug.msg("Request from "+remoteAddress+" contained no destination, ignoring");
					}
				}else if(o instanceof ClientActionRequest){
					//client has requested to perform an action
					actionRequest = (ClientActionRequest) o;
					if(actionRequest.doLookup && login.allowLookup){
						Debug.msg("Received request to do reverse lookup from "+remoteAddress);
						JFritz.getJframe().doLookupButtonClick();
					}
					if(actionRequest.getCallList && login.allowGetList){
						Debug.msg("Received request to get call from the box from "+remoteAddress);
						JFritz.getJframe().doFetchButtonClick();
					}

				}else if(o instanceof String){
					message = (String) o;
					Debug.msg("Received message from client "+remoteAddress+": "+message);
					if(message.equals("JFRITZ CLOSE")){
						Debug.msg("Client is closing the connection, closing this thread");
						disconnect();
					}

				}else{
					Debug.msg("Received unexpected object from "+remoteAddress+" ignoring");
				}


			}catch(ClassNotFoundException e){
				Debug.err("unrecognized class received as request from client");
				Debug.err(e.toString());
				e.printStackTrace();
			}catch(SocketException e){
				if(e.getMessage().equals("Socket closed")){
					Debug.msg("socket for "+remoteAddress+" was closed!");
				}else{
					Debug.err(e.toString());
					e.printStackTrace();
				}
				return;
			}catch(EOFException e){
				Debug.err("client "+remoteAddress+" closed stream unexpectedly");
				Debug.err(e.toString());
				e.printStackTrace();
				return;
			}catch (IOException e){
				Debug.msg("IOException occured reading client request");
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Authenticate client and record which login client used
	 * logins are used to determine permissions and eventually
	 * filter settings.
	 *
	 * @return login used by client
	 */
	public Login authenticateClient(){

		Object o;
		//Login login;
		String user, password;
		Vector<Login> clientLogins = ClientLoginsTableModel.getClientLogins();

		try{
			//set timeout in case client implementation is broken
			socket.setSoTimeout(15000);
			objectOut.writeObject("JFRITZ SERVER 1.0");
			objectOut.flush();
			for(int i = 0; i < 3; i++){
				o = objectIn.readObject();
				if(o instanceof String){
					user = (String) o;
					o = objectIn.readObject();
					if(o instanceof String){
						password = (String) o;
						for(Login login: clientLogins){
							if(login.getUser().equals(user) &&
									login.getPassword().equals(password)){

								objectOut.writeObject("JFRITZ 1.0 OK");
								objectOut.flush();
								objectOut.reset();
								//client properly authenticated itself, no need for timer any more
								socket.setSoTimeout(0);
								return login;
							}else{
								objectOut.writeObject("JFRITZ 1.0 INVALID");
								objectOut.flush();
								objectOut.reset();
							}
						}
					}else
						Debug.msg("received unexpected object from client: "+o.toString());

				}else
					Debug.msg("received unexpected object from client: "+o.toString());
			}

		}catch(ClassNotFoundException e){
			Debug.err("received unrecognized object from client!");
			Debug.err(e.toString());
			e.printStackTrace();
		}catch(IOException e){
			Debug.err("Error authenticating client!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

		return null;

	}
	/**
	 * Called internally when client signals that it is going to end
	 * the connection. Is sychronized with all other write requests,
	 * so queued writes should still be written out.
	 *
	 */
	private synchronized void disconnect(){
		try{
			sender.stopThread();
			objectOut.flush();
			objectOut.close();
			objectIn.close();
			socket.close();

		}catch(IOException e){
			Debug.err(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * called when the user has chosen to kill all network connections
	 *
	 */
	public synchronized void closeConnection(){
		try{
			Debug.msg("Notifying client "+remoteAddress+" to close connection");
			objectOut.writeObject("JFRITZ CLOSE");
			objectOut.flush();
			sender.stopThread();
			objectOut.close();
			objectIn.close();
			socket.close();
		}catch(SocketException e){
			Debug.msg("Error closing socket");
			Debug.err(e.toString());
			e.printStackTrace();
		}catch(IOException e){
			Debug.err("Error writing close request to client!");
			Debug.err(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Called when new calls have been added to the call list.
	 * Eventually filters based on login will be applied
	 */
	public synchronized void callsAdded(Vector<Call> newCalls){

		//this thread added calls, no need to write them back
		if(callsAdded){
			callsAdded = false;
			return;
		}

		Debug.msg("Notifying client "+remoteAddress+" of added calls, size: "+newCalls.size());
		callsAdd.data =  newCalls;

		sender.addChange(callsAdd.clone());

	}

	/**
	 * Called when calls have been removed from the call list.
	 * Eventually filters based on login will be applied.
	 */
	public synchronized void callsRemoved(Vector<Call> removedCalls){

		//this thread removed calls no need to add them back
		if(callsRemoved){
			callsRemoved = false;
			return;
		}

		Debug.msg("Notifying client "+remoteAddress+" of removed calls, size: "+removedCalls.size());
		callsRemove.data = removedCalls;

		sender.addChange(callsRemove.clone());
	}

	/**
	 * called when a call has been updated (comment changed)
	 */
	public synchronized void callsUpdated(Call original, Call updated){

		if(callUpdated)
			return;

		Debug.msg("Notifying client "+remoteAddress+" of updated call");
		callUpdate.original = original;
		callUpdate.updated = updated;

		sender.addChange(callUpdate.clone());

	}

	/**
	 * Called when contacts have been added to the call list.
	 * Eventually filters will be applied based on login.
	 *
	 */
	public synchronized void contactsAdded(Vector<Person> newContacts){

		if(contactsAdded)
			return;

		Debug.msg("Notifying client "+remoteAddress+" of added contacts, size: "+newContacts.size());
		contactsAdd.data = newContacts;

		sender.addChange(contactsAdd.clone());

	}

	/**
	 * Called when contacts have been removed from the call list.
	 * Eventually filters will be applied based on login.
	 *
	 */
	public synchronized void contactsRemoved(Vector<Person> removedContacts){

		if(contactsRemoved)
			return;

		Debug.msg("Notifying client "+remoteAddress+" of removed contacts, size: "+removedContacts.size());
		contactsRemove.data = removedContacts;

		sender.addChange(contactsRemove.clone());

	}

	/**
	 * called when a contact has been updated by the user
	 * Eventually filters will be applied based on login
	 */
	public synchronized void contactUpdated(Person original, Person updated){

		if(contactUpdated)
			return;

		Debug.msg("Notifying client "+remoteAddress+" of updated contact");
		contactUpdate.original = original;
		contactUpdate.updated = updated;

		sender.addChange(contactUpdate.clone());
	}

	/**
	 * part of CallMonitorListener
	 */
    public void pendingCallIn(Call call){
		Debug.msg("Notifying client "+remoteAddress+" of pending call in");
		callMonitor.original = call;
		callMonitor.operation = DataChange.Operation.ADD;

		sender.addChange(callMonitor.clone());
		callMonitor.original = null;
    }

	/**
	 * part of CallMonitorListener
	 */
    public void establishedCallIn(Call call){
		Debug.msg("Notifying client "+remoteAddress+" of established call in");
		callMonitor.original = call;
		callMonitor.operation = DataChange.Operation.UPDATE;

		sender.addChange(callMonitor.clone());
		callMonitor.original = null;
    }

	/**
	 * part of CallMonitorListener
	 */
    public void pendingCallOut(Call call){
		Debug.msg("Notifying client "+remoteAddress+" of pending call out");
		callMonitor.updated = call;
		callMonitor.operation = DataChange.Operation.ADD;

		sender.addChange(callMonitor.clone());
		callMonitor.updated = null;
    }

	/**
	 * part of CallMonitorListener
	 */
    public void establishedCallOut(Call call){
		Debug.msg("Notifying client "+remoteAddress+" of pending call");
		callMonitor.updated = call;
		callMonitor.operation = DataChange.Operation.UPDATE;

		sender.addChange(callMonitor.clone());
		callMonitor.updated = null;
    }

	/**
	 * part of CallMonitorListener
	 */
    public void endOfCall(Call call){
		Debug.msg("Notifying client "+remoteAddress+" of pending call");
		callMonitor.original = call;
		callMonitor.operation = DataChange.Operation.REMOVE;

		sender.addChange(callMonitor);
		callMonitor.original = null;
    }
}

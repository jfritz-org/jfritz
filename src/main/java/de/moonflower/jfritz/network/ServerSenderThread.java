package de.moonflower.jfritz.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;

/**
 * This class is used for filtering and sending data to a client.
 * It contains a queue of current changes to be sent / filtered
 * and it called by the main ClientConnectionThread running for
 * the connection. It uses a minimal locking to make sure that
 * a slow connection doesnt block any other parts of jfritz.
 *
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author brian
 *
 */
public class ServerSenderThread extends Thread {
	private final static Logger log = Logger.getLogger(ServerSenderThread.class);

	private boolean stop;

	private DataChange<?> change;

	private DataChange<Call> callChange;

	@SuppressWarnings("unused")
	private DataChange<Person> contactChange;

	private InetAddress remoteAddress;

	private ObjectOutputStream objectOut;

	private Cipher outCipher;

	@SuppressWarnings("rawtypes")
	private ConcurrentLinkedQueue<DataChange> changedObjects;

	private Login login;

	private Vector<Call> filteredCalls;

	@SuppressWarnings("unused")
	private String contactFilter;

	@SuppressWarnings("unused")
	private Vector<Person> filteredContacts;

	@SuppressWarnings("rawtypes")
	public ServerSenderThread(ObjectOutputStream oos, InetAddress rAddress, Login login, Cipher cipher){

		objectOut = oos;
		outCipher = cipher;
		remoteAddress = rAddress;
		changedObjects = new ConcurrentLinkedQueue<DataChange>();
		this.login = login;
		contactFilter = login.contactFilter;
	}

	@SuppressWarnings("unchecked")
	public void run(){

		log.info("NETWORKING: Sender thread for "+remoteAddress+" started up");
		while(!stop){

			// if no changes are present then sleep
			if(changedObjects.size() == 0){
				try{
					synchronized(this){
						wait();
					}
				}catch(InterruptedException e){
					log.info("NETWORKING: A Server sender thread was interrupted!");
		        	Thread.currentThread().interrupt();
				}

			} else {

				// Iterate over all changes to be sent
				// while is used instead of for because the size of the queue
				// may change during each iteration as the queue is only locked
				// for a short period of time
				while(changedObjects.size() > 0) {

					// first take the head of the queue while locking it
					synchronized(this) {
						change = changedObjects.poll();
					}

					if(change.destination == DataChange.Destination.CALLLIST){
						//set the reference to DataChange<Call> so we can modify members
						callChange = (DataChange<Call>) change;
						filterCallData((Vector<Call>) change.data);
					}

					log.info("NETWORKING: Writing filtered data to client "+remoteAddress);

					// now write it accross the socket connection while leaving the queue open for writing
					try{

						SealedObject sealed_object = new SealedObject(change, outCipher);
						objectOut.writeObject(sealed_object);
						objectOut.flush();
						objectOut.reset();

					}catch(IOException e){
						log.error("Error writing change information to client! host: "+remoteAddress);
						log.error(e.toString());
						e.printStackTrace();
					} catch (IllegalBlockSizeException e) {
						log.error("Illegal block size exception!");
						log.error(e.toString());
						e.printStackTrace();
					}

					// implicitly remove the reference to the change object
					change = null;

				}

			}
		}

		log.info("NETWORKING: Sender Thread for "+remoteAddress+" stopping");

	}

	/**
	 * Adds the changes to the queue
	 *
	 * @param changes to sent to the client
	 */
	public synchronized void addChange(DataChange<?>  changes){
		changedObjects.add(changes);
		notify();
	}

	/**
	 * Is called to close down the this thread
	 *
	 */
	public synchronized void stopThread(){
		stop = true;
		notify();
	}


	/**
	 * This function filters the calls
	 * before they are sent to the client
	 *
	 * @param calls to be filtered
	 */
	public void filterCallData(Vector<Call> calls){

		boolean passed = true;
		filteredCalls = new Vector<Call>();

		// always get the callFilters reference directly from the login object
		// because this may change through user editing
		Vector<CallFilter> callFilters = login.callFilters;

		log.info("NETWORKING: Filtering outgoing call data for: "+this.remoteAddress
				+" size of calls: "+calls.size());
		for(Call call: calls){

			for(CallFilter cf: callFilters){
				if(!cf.passFilter(call)){
					passed = false;
					break;
				}
			}
			if(passed){
				filteredCalls.add(call);
			}
			passed = true;
		}

		callChange.data = filteredCalls;
	}

}

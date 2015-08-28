package de.moonflower.jfritz.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.utils.Debug;

/**
 * This class makes sure that the connection to the client
 * is still active and able to correctly transfer data
 *
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author brian
 *
 */
public class ServerKeepAliveTask extends TimerTask {
	private final static Logger log = Logger.getLogger(ServerKeepAliveTask.class);

	private ObjectOutputStream objectOut;

	private InetAddress remoteAddress;

	private Cipher outCipher;

	private ClientConnectionThread connection;

	public ServerKeepAliveTask(ClientConnectionThread cct, ObjectOutputStream oos, InetAddress ia, Cipher c){
		super();
		objectOut = oos;
		remoteAddress = ia;
		outCipher = c;
		connection = cct;
	}


	public void run() {

		try{

				//check if client responded to the last keep alive
			if(connection.isClientAlive()){
				log.info("NETWORKING: Sending keep alive string to "+remoteAddress);

				SealedObject sealed_object = new SealedObject("Party on, Wayne!", outCipher);
				objectOut.writeObject(sealed_object);
				objectOut.flush();
				objectOut.reset();

				connection.resetKeepAlive();

				// if not kill the connection
			}else{
				log.info("NETWORKING: Client has responded to last keep alive, killing the connection");
				connection.closeConnection();
			}

		}catch(IOException e){
			Debug.error(log, "Error writing change information to client! host: "+remoteAddress);
			Debug.error(log, e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.error(log, "Illegal block size exception!");
			Debug.error(log, e.toString());
			e.printStackTrace();
		}

	}

}

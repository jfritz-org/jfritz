package de.moonflower.jfritz.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;

import de.moonflower.jfritz.utils.Debug;

/**
 * This class makes sure that the connection to the client
 * is still active and able to correctly transfer data
 *
 * @author brian
 *
 */
public class ServerKeepAliveTask extends TimerTask {

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
				Debug.netMsg("Sending keep alive string to "+remoteAddress);

				SealedObject sealed_object = new SealedObject("Party on, Wayne!", outCipher);
				objectOut.writeObject(sealed_object);
				objectOut.flush();
				objectOut.reset();

				connection.resetKeepAlive();

				// if not kill the connection
			}else{
				Debug.netMsg("Client has responded to last keep alive, killing the connection");
				connection.closeConnection();
			}

		}catch(IOException e){
			Debug.err("Error writing change information to client! host: "+remoteAddress);
			Debug.err(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Debug.err("Illegal block size exception!");
			Debug.err(e.toString());
			e.printStackTrace();
		}

	}

}

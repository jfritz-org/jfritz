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
 * This class makes sure that the connection to the server
 * is still active and able to transfer data
 *
 * @author brian
 *
 */
public class ServerKeepAliveTask extends TimerTask {

	private ObjectOutputStream objectOut;

	private InetAddress remoteAddress;

	private Cipher outCipher;

	public ServerKeepAliveTask(ObjectOutputStream oos, InetAddress ia, Cipher c){
		super();
		objectOut = oos;
		remoteAddress = ia;
		outCipher = c;
	}


	public void run() {
		Debug.netMsg("Sending keep alive string to "+remoteAddress);

		try{

			SealedObject sealed_object = new SealedObject("Party on, Wayne!", outCipher);
			objectOut.writeObject(sealed_object);
			objectOut.flush();
			objectOut.reset();

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

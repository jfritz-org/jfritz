package de.moonflower.jfritz.callmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.utils.Debug;

/**
 * Thread, listens on a TCP-Port on YAC Messages Message format:
 * @CALLname~number or: message
 *
 * @author Robert Palmer
 *
 */
public class YACCallMonitor extends Thread implements CallMonitorInterface{
	private final static Logger log = Logger.getLogger(YACCallMonitor.class);

	private boolean isRunning = false;

	private int port;

	private ServerSocket serverSocket;

	private boolean connected;

	private String boxName;

	private Vector<CallMonitorStatusListener> stateListener;
	protected MessageProvider messages = MessageProvider.getInstance();

	public YACCallMonitor(String boxName, int port, Vector<CallMonitorStatusListener> stateListener) {
		super();
		this.boxName = boxName;
		this.port = port;
		this.stateListener = stateListener;
		start();
	}

	public void run() {
		startYACListener();
	}

	public void startYACListener() {
		isRunning = true;
		try {
			log.info("Starting YAC-Monitor"); //$NON-NLS-1$
			serverSocket = new ServerSocket(port);
			connected = true;
			this.setConnectedStatus();
            log.info("YAC-Monitor ready"); //$NON-NLS-1$
			while (isRunning) {
				Socket socket = serverSocket.accept();
				BufferedReader input = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				while (isRunning) {
					String[] splitList;
					String msg = input.readLine();
					if (msg == null)
						break;
					// parsing incoming DATA
					Debug.debug(log, "Got YAC-Data: " + msg); //$NON-NLS-1$
					// if last character is $00, delete it
					if (msg.length() > 0 && msg.charAt(msg.length() - 1) == 0) {
						msg = msg.substring(0, msg.length() - 1);
					}
					String outputString = ""; //$NON-NLS-1$
					if (msg.indexOf('~') > -1) { //$NON-NLS-1$
						if (msg.startsWith("@CALL")) { //$NON-NLS-1$
							msg = msg.substring(5);
							splitList = msg.split("~"); //$NON-NLS-1$
//							String name = ""; //$NON-NLS-1$
//							String number = ""; //$NON-NLS-1$
							if (splitList.length == 0) {
//								name = ""; //$NON-NLS-1$
//								number = ""; //$NON-NLS-1$
							}
							if (splitList.length == 1) {
								if (!splitList[0].equals("")) { //$NON-NLS-1$
//									name = splitList[0];
//									number = ""; //$NON-NLS-1$
								}
							}
							if (splitList.length == 2) {
								if (splitList[0].equals("")) { //$NON-NLS-1$
//									name = ""; //$NON-NLS-1$
								} else{
//									name = splitList[0];
//								number = splitList[1];
								}
							}

                            // TODO: Add call to CallMonitorList and show message if number is not in ignore list
                            //JFritz.getCallMonitorList().displayCallInMsg(number, "", name); //$NON-NLS-1$

						} else {
							outputString = messages.getMessage("yac_message") //$NON-NLS-1$
									+ ":\n" + msg; //$NON-NLS-1$
							JFritz.infoMsg(outputString);
						}
					} else {
						outputString = messages.getMessage("yac_message") + ":\n" //$NON-NLS-1$,  //$NON-NLS-2$
								+ msg;
						JFritz.infoMsg(outputString);
					}

				}
				socket.close();
				connected = false;
				this.setDisconnectedStatus();
			}
			serverSocket.close();
			connected = false;
			this.setDisconnectedStatus();
		} catch (IOException e) {
			Debug.error(log, e.toString());
			connected = false;
			this.setDisconnectedStatus();
		}
	}

	public void stopCallMonitor() {
		log.info("Stopping YACListener"); //$NON-NLS-1$
		try {
			if (serverSocket != null)
			{
				serverSocket.close();
			}
			connected = false;
		} catch (IOException e) {
			Debug.error(log, "Fehler beim Schliessen des YAC-Sockets"); //$NON-NLS-1$
		}
		isRunning = false;
		this.setDisconnectedStatus();
	}

	public boolean isConnected() {
		return connected;
	}

	public void closeConnection() {
		log.warn("Method not implemented!");
	}

	private void setConnectedStatus()
	{
		if (stateListener != null)
		{
			for (int i=0; i<stateListener.size(); i++)
			{
				stateListener.get(i).setConnectedStatus(boxName);
			}
		}
	}

	private void setDisconnectedStatus()
	{
		if (stateListener != null)
		{
			for (int i=0; i<stateListener.size(); i++)
			{
				stateListener.get(i).setDisconnectedStatus(boxName);
			}
		}
	}
}

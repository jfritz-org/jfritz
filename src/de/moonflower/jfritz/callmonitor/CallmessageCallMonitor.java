package de.moonflower.jfritz.callmonitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Vector;
import java.io.DataOutputStream;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;

/**
 * Thread, listens on a TCP-Port on Callmessage messages format:
 *
 * @name (number) or: message
 *
 * @author Robert Palmer
 *
 */
public class CallmessageCallMonitor extends Thread implements CallMonitorInterface {

	private boolean isRunning = false;

	private int port;

	private ServerSocket serverSocket;

    DisplayCallsMonitor dcm;

    private boolean connected = false;

    private String boxName = "";

    private Vector<CallMonitorStatusListener> stateListener;

	public CallmessageCallMonitor(String boxName, int port, Vector<CallMonitorStatusListener> listener) {
		super();
		this.boxName = boxName;
		this.port = port;
		this.stateListener = listener;
		start();
	}

	public void run() {
		startCallmessageListener();
	}

	private void startCallmessageListener() {
		isRunning = true;
		Debug.info("Starting Callmessage-Monitor on Port " + port); //$NON-NLS-1$
		try {
			serverSocket = new ServerSocket(port);
			connected = true;
			this.setConnectedStatus();
		} catch (Exception e) {
			try {
                Debug.error(e.toString()); //$NON-NLS-1$
                connected = false;
				synchronized (this) {
					wait(5000);
				}
			} catch (InterruptedException e1) {
				connected = false;
                Debug.error(e1.toString());
	        	Thread.currentThread().interrupt();
			}
			this.setDisconnectedStatus();
		}
        Debug.info("Callmessage-Monitor ready"); //$NON-NLS-1$
		while (isRunning) {
			try {
				// Client-Connection accepten, Extra-Socket öffnen
				Socket connection = serverSocket.accept();
				// Eingabe lesen
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								"ISO-8859-1")); //$NON-NLS-1$
				String msg = input.readLine();
				msg = URLDecoder.decode(msg, "ISO-8859-1"); //$NON-NLS-1$
				msg = msg.substring(5, msg.length() - 9);
				Debug.info("Got message from callmessageMonitor: " + msg); //$NON-NLS-1$

				// Neuer CallmessageMonitor
				 if (msg.startsWith("?")) {  //$NON-NLS-1$
				     // Format: ?caller=MSN&called=MSN2

                     msg = msg.substring(1); // Entferne ?
                     String number = "";  //$NON-NLS-1$
                     String msn = "";  //$NON-NLS-1$
                     String splitted[] = msg.split("&");  //$NON-NLS-1$

                     for (int i = 0; i < splitted.length; i++) {
                          if (splitted[i].startsWith("caller=")) {  //$NON-NLS-1$
                               number = splitted[i].substring(7);
                          }
                          if (splitted[i].startsWith("called=")) {  //$NON-NLS-1$
                               msn = splitted[i].substring(7);
                          }
                     }
                     // TODO: add Call to CallMonitorList and display it only, if number is not in ignoreMSN-List
                     Person person = PhoneBook.searchFirstAndLastNameToPhoneNumber(number);
                     dcm.displayCallInMsg(null, null, number, msn, "", person);  //$NON-NLS-1$
                     // Alter Callmessagemonitor
                } else if (msg.startsWith("@")) {  //$NON-NLS-1$
					// Call
					// Format: @NAME (NUMBER) oder @NAME NUMBER
					// NAME: Name, ".." or "unbekannt"
					// NUMBER: Number or "Keine Rufnummer übermittelt"
					// @unbekannt (01798279574)
					// @.. (Keine Rufnummer ?bermittelt)
					msg = msg.substring(1); // Entferne @
					String name = ""; //$NON-NLS-1$
					String number = ""; //$NON-NLS-1$
					String msn = ""; //$NON-NLS-1$
					String splitted[] = msg.split(" ", 3); //$NON-NLS-1$

					if (splitted.length == 1) {
						name = splitted[0];
					} else if (splitted.length == 2) {
						name = splitted[0];
						number = splitted[1];
						number = number.replaceAll("\\(", ""); //$NON-NLS-1$,  //$NON-NLS-2$
						number = number.replaceAll("\\)", ""); //$NON-NLS-1$,  //$NON-NLS-2$
					} else if (splitted.length == 3) {
						name = splitted[0];
						number = splitted[1];
						msn = splitted[2];
						number = number.replaceAll("\\(", ""); //$NON-NLS-1$,  //$NON-NLS-2$
						number = number.replaceAll("\\)", ""); //$NON-NLS-1$,  //$NON-NLS-2$
					}
					if (name.equals("..") || name.equals("unbekannt")) { //$NON-NLS-1$,  //$NON-NLS-2$
						name = ""; //$NON-NLS-1$
					}
					if (number.equals("Keine Rufnummer übermittelt")) { //$NON-NLS-1$
						number = ""; //$NON-NLS-1$
					}
                    // TODO: add Call to CallMonitorList and display it only, if number is not in ignoreMSN-List
                    Person person = PhoneBook.searchFirstAndLastNameToPhoneNumber(number);
                    dcm.displayCallInMsg(null, null, number, msn, "", person);  //$NON-NLS-1$
					//JFritz.getCallMonitorList().displayCallInMsg(number, msn, name);
				} else {
					// Message
					JFritz.infoMsg(Main.getMessage("yac_message") + ":\n" //$NON-NLS-1$,  //$NON-NLS-2$
							+ msg);
				}

				// No Content ausgeben, Client rauswerfen
				DataOutputStream output = new DataOutputStream(connection
						.getOutputStream());
				output.writeBytes("HTTP/1.1 204 No Content"); //$NON-NLS-1$
				connection.close();
			} catch (SocketException e) {
				Debug.error(e.toString()); //$NON-NLS-1$
				if (!e.toString().equals("java.net.SocketException: socket closed")) { //$NON-NLS-1$
					this.setDisconnectedStatus();
				}
			} catch (Exception e) {
				JFritz.infoMsg("Exception " + e); //$NON-NLS-1$
				Debug.error("CallmessageListener: Exception " + e.toString()); //$NON-NLS-1$
				this.setDisconnectedStatus();
				isRunning = false;
				//				break;
			}
		}
	}

	public void stopCallMonitor() {
		Debug.info("Stopping CallmessageListener"); //$NON-NLS-1$
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (Exception e) {
			Debug.error("Error on closing socket: " + e.toString()); //$NON-NLS-1$
		}
		isRunning = false;
	}

	public boolean isConnected() {
		return connected;
	}

	public void closeConnection() {
		Debug.warning("Method not implemented!");
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

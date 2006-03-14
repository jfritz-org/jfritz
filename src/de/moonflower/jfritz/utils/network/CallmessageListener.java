package de.moonflower.jfritz.utils.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.io.DataOutputStream;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

/**
 * Thread, listens on a TCP-Port on Callmessage messages format:
 *
 * @name (number) or: message
 *
 * @author Robert Palmer
 *
 */
public class CallmessageListener extends Thread implements CallMonitor {

	private boolean isRunning = false;

	private int port;

	private JFritz jfritz;

	private ServerSocket serverSocket;

	public CallmessageListener(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		start();
		port = 23232;
	}

	public CallmessageListener(JFritz jfritz, int port) {
		super();
		this.jfritz = jfritz;
		start();
		this.port = port;
	}

	public void run() {
		startCallmessageListener();
	}

	public void startCallmessageListener() {
		isRunning = true;
		Debug.msg("Starting Callmessage-Monitor on Port " + port);
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			try {
                Debug.err("Exception occoured");
				synchronized (this) {
					wait(5000);
				}
			} catch (InterruptedException e1) {
                Debug.err(e1.toString());
			}
			jfritz.stopCallMonitor();
		}
        Debug.msg("Callmessage-Monitor ready");
		while (isRunning) {
			try {
				// Client-Connection accepten, Extra-Socket öffnen
				Socket connection = serverSocket.accept();
				// Eingabe lesen
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								"ISO-8859-1"));
				String msg = input.readLine();
				msg = URLDecoder.decode(msg, "ISO-8859-1");
				msg = msg.substring(5, msg.length() - 9);
				Debug.msg("Got message from callmessageMonitor: " + msg);

				 if (msg.startsWith("?")) { // Neer Callmessagemonitor
				     // Format: ?caller=MSN&called=MSN2

                     msg = msg.substring(1); // Entferne ?
                     String number = "";
                     String msn = "";
                     String splitted[] = msg.split("&");

                     for (int i = 0; i < splitted.length; i++) {
                          if (splitted[i].startsWith("caller=")) {
                               number = splitted[i].substring(7);
                          }
                          if (splitted[i].startsWith("called=")) {
                               msn = splitted[i].substring(7);
                          }
                     }
                     jfritz.callInMsg(number, msn, "");
                } else if (msg.startsWith("@")) { // Alter Callmessagemonitor
					// Call
					// Format: @NAME (NUMBER) oder @NAME NUMBER
					// NAME: Name, ".." or "unbekannt"
					// NUMBER: Number or "Keine Rufnummer übermittelt"
					// @unbekannt (01798279574)
					// @.. (Keine Rufnummer ?bermittelt)
					msg = msg.substring(1); // Entferne @
					String name = "";
					String number = "";
					String msn = "";
					String splitted[] = msg.split(" ", 3);

					if (splitted.length == 1) {
						Debug.msg("Split length 1");
						name = splitted[0];
					} else if (splitted.length == 2) {
						Debug.msg("Split length 2");
						name = splitted[0];
						number = splitted[1];
						number = number.replaceAll("\\(", "");
						number = number.replaceAll("\\)", "");
					} else if (splitted.length == 3) {
						Debug.msg("Split length 3");
						name = splitted[0];
						number = splitted[1];
						msn = splitted[2];
						number = number.replaceAll("\\(", "");
						number = number.replaceAll("\\)", "");
					}
					if (name.equals("..") || name.equals("unbekannt")) {
						name = "";
					}
					if (number.equals("Keine Rufnummer übermittelt")) {
						number = "";
					}
					jfritz.callInMsg(number, msn, name);
				} else {
					// Message
					JFritz.infoMsg(JFritz.getMessage("yac_message") + ":\n"
							+ msg);
				}

				// No Content ausgeben, Client rauswerfen
				DataOutputStream output = new DataOutputStream(connection
						.getOutputStream());
				output.writeBytes("HTTP/1.1 204 No Content");
				connection.close();
			} catch (SocketException e) {
				Debug.err("SocketException: " + e);
				if (!e.toString().equals("java.net.SocketException: socket closed")) {
					jfritz.stopCallMonitor();
				}
			} catch (Exception e) {
				JFritz.infoMsg("Exception " + e);
				Debug.msg("CallmessageListener: Exception " + e);
				jfritz.stopCallMonitor();
				isRunning = false;
				//				break;
			}
		}
	}

	public void stopCallMonitor() {
		Debug.msg("Stopping CallmessageListener");
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (Exception e) {
			Debug.msg("Fehler beim Schliessen des Sockets");
		}
		isRunning = false;
	}

}

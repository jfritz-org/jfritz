package de.moonflower.jfritz.utils.network;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

/**
 * Thread, listens on a TCP-Port on YAC Messages Message format:
 * @CALLname~number or: message
 *
 * @author Robert Palmer
 *
 */
public class YAClistener extends Thread implements CallMonitor{

	private boolean isRunning = false;

	private int port;

	private JFritz jfritz;

	private ServerSocket serverSocket;

	public YAClistener(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		start();
		port = 10629;
	}

	public YAClistener(JFritz jfritz, int port) {
		super();
		this.jfritz = jfritz;
		start();
		this.port = port;
	}

	public void run() {
		startYACListener();
	}

	public void startYACListener() {
		isRunning = true;
		try {
			Debug.msg("Starting YAC-Monitor"); //$NON-NLS-1$
			serverSocket = new ServerSocket(port);
            Debug.msg("YAC-Monitor ready"); //$NON-NLS-1$
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
					Debug.msg("Got YAC-Data: " + msg); //$NON-NLS-1$
					// if last character is $00, delete it
					if (msg.length() > 0 && msg.charAt(msg.length() - 1) == 0) {
						msg = msg.substring(0, msg.length() - 1);
					}
					String outputString = ""; //$NON-NLS-1$
					if (msg.indexOf('~') > -1) { //$NON-NLS-1$
						if (msg.startsWith("@CALL")) { //$NON-NLS-1$
							msg = msg.substring(5);
							splitList = msg.split("~"); //$NON-NLS-1$
							String name = ""; //$NON-NLS-1$
							String number = ""; //$NON-NLS-1$
							if (splitList.length == 0) {
								name = ""; //$NON-NLS-1$
								number = ""; //$NON-NLS-1$
							}
							if (splitList.length == 1) {
								if (!splitList[0].equals("")) { //$NON-NLS-1$
									name = splitList[0];
									number = ""; //$NON-NLS-1$
								}
							}
							if (splitList.length == 2) {
								if (splitList[0].equals("")) { //$NON-NLS-1$
									name = ""; //$NON-NLS-1$
								} else
									name = splitList[0];
								number = splitList[1];
							}

							jfritz.callInMsg(number, "", name); //$NON-NLS-1$

						} else {
							outputString = JFritz.getMessage("yac_message") //$NON-NLS-1$
									+ ":\n" + msg; //$NON-NLS-1$
							JFritz.infoMsg(outputString);
						}
					} else {
						outputString = JFritz.getMessage("yac_message") + ":\n" //$NON-NLS-1$,  //$NON-NLS-2$
								+ msg;
						JFritz.infoMsg(outputString);
					}

				}
				socket.close();
			}
			serverSocket.close();

		} catch (IOException e) {
			Debug.err(e.toString());
		}
	}

	public void stopCallMonitor() {
		Debug.msg("Stopping YACListener"); //$NON-NLS-1$
		try {
			if (serverSocket != null)
			serverSocket.close();
		} catch (IOException e) {
			Debug.msg("Fehler beim Schliessen des YAC-Sockets"); //$NON-NLS-1$
		}
		isRunning = false;
	}

}

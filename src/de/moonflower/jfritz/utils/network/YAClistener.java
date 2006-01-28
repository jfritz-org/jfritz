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
			Debug.msg("Starting YAC-Monitor");
			serverSocket = new ServerSocket(port);
            Debug.msg("YAC-Monitor ready");
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
					Debug.msg("Got YAC-Data: " + msg);
					// if last character is $00, delete it
					if (msg.length() > 0 && msg.charAt(msg.length() - 1) == 0) {
						msg = msg.substring(0, msg.length() - 1);
					}
					String outputString = "";
					if (msg.indexOf('~') > -1) {
						if (msg.startsWith("@CALL")) {
							msg = msg.substring(5);
							splitList = msg.split("~");
							String name = "";
							String number = "";
							if (splitList.length == 0) {
								name = "";
								number = "";
							}
							if (splitList.length == 1) {
								if (!splitList[0].equals("")) {
									name = splitList[0];
									number = "";
								}
							}
							if (splitList.length == 2) {
								if (splitList[0].equals("")) {
									name = "";
								} else
									name = splitList[0];
								number = splitList[1];
							}

							jfritz.callInMsg(number, "", name);

						} else {
							outputString = JFritz.getMessage("yac_message")
									+ ":\n" + msg;
							JFritz.infoMsg(outputString);
						}
					} else {
						outputString = JFritz.getMessage("yac_message") + ":\n"
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
		Debug.msg("Stopping YACListener");
		try {
			if (serverSocket != null)
			serverSocket.close();
		} catch (IOException e) {
			Debug.msg("Fehler beim Schliessen des YAC-Sockets");
		}
		isRunning = false;
	}

}

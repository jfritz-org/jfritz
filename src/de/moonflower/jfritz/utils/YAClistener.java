package de.moonflower.jfritz.utils;

/**
 * @author Robert Palmer Listens on a TCP-Port on YAC Messages Message format:
 * @CALLname~number or: message
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.moonflower.jfritz.JFritz;

public class YAClistener implements Runnable {

	private final int yacPort = 10629;

	private boolean isRunning = false;

	public YAClistener() {
	}

	public void run() {
		if (!isRunning) {
			isRunning = true;
			try {
				Debug.msg("Starting YAC listener");
				// TODO: configurable Port
				ServerSocket serverSocket = new ServerSocket(yacPort);
				while (true) {
					Socket socket = serverSocket.accept();
					BufferedReader input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					while (true) {
						String[] splitList;
						String msg = input.readLine();
						if (msg == null)
							break;
						// parsing incoming DATA
						Debug.msg("Got YAC-Data: " + msg);
						// if last character is $00, delete it
						if (msg.length() > 0
								&& msg.charAt(msg.length() - 1) == 0) {
							msg = msg.substring(0, msg.length() - 1);
						}
						String outputString = "";
						if (msg.indexOf('~')>-1) {
							if (msg.startsWith("@CALL")) {
								msg = msg.substring(5);
								splitList = msg.split("~");
								String name = "";
								String number = "";
								if (splitList.length == 0) {
									name = "Unbekannt";
									number = "Unbekannt";
								}
								if (splitList.length == 1) {
									if (!splitList[0].equals("")) {
										name = splitList[0];
										number = "Unbekannt";
									}
								}
								if (splitList.length == 2) {
									if (splitList[0].equals("")) {
										name = "Unbekannt";
									} else
										name = splitList[0];
									number = splitList[1];
								}

								outputString = JFritz
										.getMessage("incoming_call")
										+ "\n"
										+ JFritz.getMessage("name")
										+ ": "
										+ name
										+ "\n"
										+ JFritz.getMessage("number")
										+ ": "
										+ number;
							} else {
								outputString = JFritz.getMessage("yac_message")
										+ ":\n" + msg;
							}
						} else {
							outputString = JFritz.getMessage("yac_message")
									+ ":\n" + msg;
						}

						JFritz.infoMsg(outputString);
						if (!JFritz.SYSTRAY_SUPPORT) {
							//TODO: PopUp-Message
						}
					}
					socket.close();
				}

			} catch (IOException e) {
				System.out.println(e);
			}
			isRunning = false;
		} else {
			// TODO: Kill it
			Debug.msg("Kill yac listener...");
		}
	}
}

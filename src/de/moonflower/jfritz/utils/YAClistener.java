package de.moonflower.jfritz.utils;

/**
 * @author Robert Palmer Listens on a TCP-Port on YAC Messages Message format:
 * @CALLname~number or: message
 */

import java.io.*;
import java.net.*;

import de.moonflower.jfritz.JFritz;

public class YAClistener implements Runnable {

	private JFritz jfritz;

	private final int yacPort = 10629;

	public YAClistener(JFritz jfritz) {
		this.jfritz = jfritz;
	}

	public void run() {
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
					String nachricht = input.readLine();
					if (nachricht == null)
						break;
					// parsing incoming DATA
					Debug.msg("Got YAC-Data: " + nachricht);
					// if last character is $00, delete it
					if (nachricht.charAt(nachricht.length() - 1) == 0) {
						nachricht = nachricht.substring(0,
								nachricht.length() - 1);
					}
					String outputString = "";
					if (nachricht.contains("~")) {
						if (nachricht.startsWith("@CALL")) {
							nachricht = nachricht.substring(5);
							splitList = nachricht.split("~");
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

							outputString = jfritz.getMessages().getString(
									"incoming_call")
									+ "\n"
									+ jfritz.getMessages().getString("name")
									+ ": "
									+ name
									+ "\n"
									+ jfritz.getMessages().getString("number")
									+ ": " + number;
						} else {
							outputString = jfritz.getMessages().getString(
									"yac_message")
									+ ":\n" + nachricht;
						}
					} else {
						outputString = jfritz.getMessages().getString(
								"yac_message")
								+ ":\n" + nachricht;
					}

					jfritz.infoMsg(outputString);
					if (!JFritz.SYSTRAY_SUPPORT) {
						//TODO: PopUp-Message
					}
				}
				socket.close();
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

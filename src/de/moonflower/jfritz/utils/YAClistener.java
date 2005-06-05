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
					if (JFritz.DEVEL_VERSION) {
						Debug.msg("Got YAC-Data: " + nachricht);
					}
					String outputString = "";
					if (nachricht.contains("~")) {
						if (nachricht.startsWith("@CALL")) {
							nachricht = nachricht.substring(5);
							splitList = nachricht.split("~");
							outputString = jfritz.getMessages().getString(
									"incoming_call");
							if (!splitList[0].equals("")) {
								outputString = outputString
										+ "\n"
										+ jfritz.getMessages()
												.getString("name") + ": "
										+ splitList[0];
							}
							outputString = outputString + "\n"
									+ jfritz.getMessages().getString("number")
									+ ": " + splitList[1];
						} else {
							outputString = jfritz.getMessages().getString(
									"yac_message")
									+ ": " + nachricht;
						}
					} else {
						outputString = jfritz.getMessages().getString(
								"yac_message")
								+ ": " + nachricht;
					}

					jfritz.infoMsg(outputString);
					if (!JFritz.SYSTRAY_SUPPORT) {
						//TODO: PopUP-Message
					}
				}
				socket.close();
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

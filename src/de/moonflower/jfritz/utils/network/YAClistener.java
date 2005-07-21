package de.moonflower.jfritz.utils.network;

/**
 * Thread, listens on a TCP-Port on YAC Messages Message format:
 * @CALLname~number or: message
 *
 * @author Robert Palmer
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.Person;

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
			Debug.msg("Starting YAC listener");
			serverSocket = new ServerSocket(port);
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

							Debug.msg("Number: " + number);
							Debug.msg("Name: " + name);
							if (name.equals("Unbekannt")
									&& !number.equals("Unbekannt")) {
								Debug.msg("Searchin in local database ...");
								Person callerperson = jfritz.getPhonebook()
										.findPerson(new PhoneNumber(number));
								if (callerperson != null) {
									name = callerperson.getFullname();
									Debug.msg("Found in local database: "
											+ name);
								} else {
									Debug
											.msg("Searchin on dasoertliche.de ...");
									Person person = ReverseLookup
											.lookup(new PhoneNumber(number));
									if (!person.getFullname().equals("")) {
										name = person.getFullname();
										Debug.msg("Found on dasoertliche.de: "
												+ name);
									}
								}

							}

							outputString = JFritz.getMessage("incoming_call")
									+ "\n" + JFritz.getMessage("name") + ": "
									+ name + "\n" + JFritz.getMessage("number")
									+ ": " + number;
						} else {
							outputString = JFritz.getMessage("yac_message")
									+ ":\n" + msg;
						}
					} else {
						outputString = JFritz.getMessage("yac_message") + ":\n"
								+ msg;
					}

					JFritz.infoMsg(outputString);
				}
				socket.close();
			}
			serverSocket.close();

		} catch (IOException e) {
			System.out.println(e);
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

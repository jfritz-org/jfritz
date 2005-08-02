/*
 * Created on 16.07.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import javax.swing.JOptionPane;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;

import org.apache.commons.net.telnet.*;

/**
 * Manages Telnetfunctions: Connect, Login, Disconnect, sendCommand
 *
 * @author rob
 *
 */
public class Telnet {

	private JFritz jfritz;

	private boolean connected = false;

	private TelnetClient telnet;

	private InputStream in;

	private PrintStream out;

	private final char prompt = '#';

	public Telnet(JFritz jfritz) {
		this.jfritz = jfritz;
		telnet = new TelnetClient();
	}

	/**
	 * Connects to FritzBox-IP
	 *
	 * TODO: Einbau der Abfrage der IP, User, Passwort, wenn keine Verbindung
	 * aufgebaut werden kann.
	 */
	public void connect() {
		boolean isdone = false;
		int connectionFailures = 0;
		while (!isdone) {
			String server = JFritz.getProperty("box.address");
			String user = JFritz.getProperty("telnet.user", "");
			String password;
			if (JFritz.getProperty("telnet.password", "").equals("")) {
				password = "";
			} else {
				password = Encryption.decrypt(JFritz
						.getProperty("telnet.password"));
			}
			int port = 23;
			try {
				jfritz.getJframe().setStatus("Verbinde mit Telnet ...");
				telnet.connect(server, port); // Connect to the specified server
				in = telnet.getInputStream();
				out = new PrintStream(telnet.getOutputStream());
				login(user, password);
				connected = true;
				isdone = true;
				Debug.msg("Done");
			} catch (ConnectException e) { // Connection Timeout
				Debug.msg("Telnet connection timeout ...");
				// Warten, falls wir von einem Standby aufwachen,
				// oder das Netzwerk temporär nicht erreichbar ist.
				if (connectionFailures < 5) {
					Debug.msg("Waiting for FritzBox, retrying ...");
					connectionFailures++;
				} else {
					Debug.msg("FritzBox not found. Get new IP ...");
					jfritz.getJframe().setStatus(
							JFritz.getMessage("box_not_found"));
					String box_address = jfritz.getJframe().showAddressDialog(
							JFritz.getProperty("box.address", "fritz.box"));
					if (!box_address.equals("")) {
						Debug.msg("New IP for FritzBox: " + box_address);
						JFritz.setProperty("box.address", box_address);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * login on a telnet session with user and password
	 *
	 * @param user
	 * @param password
	 */

	private void login(String user, String password) {
		try {
			String login = "ogin: ";
			String passwd = "assword: ";
			boolean firstLogin = true;
			boolean firstPassword = true;
			char lastCharLogin = login.charAt(login.length() - 1);
			char lastCharPasswd = passwd.charAt(passwd.length() - 1);
			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (true) {
				sb.append(ch); // FIXME This can be done better!!!
				if (ch == lastCharLogin || ch == lastCharPasswd || ch == prompt) {
					if (sb.toString().endsWith(login)) {
						if (firstLogin) { // wenn Fehlgeschlagen, dann
										  // mehrmaliges Login mit falschem
										  // Username verhindern
							Debug.msg("Writing Telnet User: " + user);
							write(user);
							firstLogin = false;
						} else {
							user = JOptionPane.showInputDialog(jfritz
									.getJframe(), "Telnet Username: ",
									"Telnet Username falsch",
									JOptionPane.QUESTION_MESSAGE);
							JFritz.setProperty("telnet.user", user);
							firstLogin = true;
						}
					}
					if (sb.toString().endsWith(passwd)) {
						// schauen, ob WebPasswort abgefragt wird
						if (sb.toString().endsWith("web password: ")) {
							password = Encryption.decrypt(JFritz
									.getProperty("box.password"));
						}
						if (firstPassword) {// wenn Fehlgeschlagen, dann
											// mehrmaliges Login mit falschem
											// Passwort verhindern
							Debug.msg("Writing Telnet Password: " + password);
							write(password);
							firstPassword = false;
						} else {
							password = JOptionPane.showInputDialog(jfritz
									.getJframe(), "Telnet Passwort: ",
									"Telnet Passwort falsch",
									JOptionPane.QUESTION_MESSAGE);
							JFritz.setProperty("telnet.password", Encryption
									.encrypt(password));
							firstPassword = true;
						}
					}
					if (ch == prompt) {
						System.err.println(sb.toString());
						break;
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			Debug.err(e.getMessage());
		}
		Debug.msg("Logged into Telnet connection.");
	}

	/**
	 * gets all Data until a pattern is reached
	 *
	 * @param pattern
	 * @return data read until pattern
	 */
	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (true) {
				sb.append(ch); // FIXME This can be done better!!!
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			Debug.err(e.getMessage());
		}
		return null;
	}

	/**
	 * Write value to telnet-session
	 *
	 * @param value
	 */
	public void write(String value) {
		try {
			out.println(value);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * writes command on telnet session and waits till prompt
	 *
	 * @param command
	 * @return data read until prompt
	 */
	public String sendCommand(String command) {
		try {
			write(command);
			String data = readUntil(prompt + " ");
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * @return telnetIsConnected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Disconnects Telnet from FritzBox
	 *
	 */
	public void disconnect() {
		Debug.msg("Disconnect Telnet connection.");
		try {
			telnet.disconnect();
			connected = false;
		} catch (Exception e) {
		}
	}

}

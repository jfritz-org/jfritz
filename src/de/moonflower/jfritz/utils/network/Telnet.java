/*
 * Created on 16.07.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.io.InputStream;
import java.io.PrintStream;

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

	private boolean connected = false;

	private TelnetClient telnet;
	private InputStream in;
	private PrintStream out;
	private final char prompt = '#';

	public Telnet() {
		telnet = new TelnetClient();
	}

/**
 * Connects to FritzBox-IP
 *
 */
	public void connect() {
		String server = JFritz.getProperty("box.address");
		String user = "";
		String password = Encryption
				.decrypt(JFritz.getProperty("box.password"));
		int port = 23;
		try {
			telnet.connect(server, port); // Connect to the specified server
			in = telnet.getInputStream();
			out = new PrintStream(telnet.getOutputStream());
			login(user, password);
			readUntil(prompt + " ");
			connected = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * login on a telnet session with user and password
	 *
	 * @param user
	 * @param password
	 */

	private void login(String user, String password) {
		if (!user.equals("")) {
			// Log the user on
			readUntil("ogin: ");
			write(user);
		}
		if (!password.equals("")) {
			readUntil("assword: ");
			write(password);
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
			boolean found = false;
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

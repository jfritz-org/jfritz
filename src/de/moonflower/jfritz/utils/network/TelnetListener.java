package de.moonflower.jfritz.utils.network;

import org.apache.commons.net.telnet.*;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author Arno Willig
 *
 */

public class TelnetListener extends Thread {

	//IncomingCall: ID 0, caller: "017623352711" called: "592904"
	//IncomingCall from NT: ID 0, caller: "592904" called: "1815212"
	private final String PATTERN_TELEFON = "IncomingCall[^:]*: ID ([^,]*), caller: \"([^\"]*)\" called: \"([^\"]*)\"";

	private final String PATTERN_VOIP_REQUEST = ">>> Request: INVITE ([^\\n]*)";

	private final String PATTERN_VOIP_CALLTO_ESTABLISHED = "call to ([^ ]*) established";

	private final String PATTERN_VOIP_CALLTO_TERMINATED = "call to ([^ ]*) terminated";

	private final String PATTERN_VOIP_CALLTO_DISCONNECTED = "disconnected\\([^)]*\\):";

	private final char prompt = '#';

	private TelnetClient telnet = new TelnetClient();

	private InputStream in;

	private PrintStream out;

	public void run() {
		Debug.msg("run()");
		if (!telnet.isConnected()) {
			connectTelnet();
			restartTelefonDaemon();
			parseOutput();
		}
	}

	private void connectTelnet() {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restartTelefonDaemon() {
		write("killall telefon && telefon &>&1");
		Debug.msg("Telefon Daemon restarted.");
	}

	private void startSyslogDaemon() {
		String ip = "192.168.178.20";
		int port = 4711;
		write("syslogd -R " + ip + ":" + port);
		write("killall telefon && telefon | logger &");
	}

	/**
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

	public void su(String password) {
		try {
			write("su");
			readUntil("Password: ");
			write(password);
			readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			char ch = (char) in.read();
			while (!isInterrupted()) {
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
			interrupt();
		}
		return null;
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) {
		try {
			write(command);
			return readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		Debug.msg("Disconnect Telnet connection.");
		try {
			telnet.disconnect();
		} catch (Exception e) {
		}
	}

	public void parseOutput() {
		try {
			String currentLine = "";
			while (!isInterrupted()) {
				currentLine = readUntil("\n");
				if (isInterrupted())
					break;
				Pattern p = Pattern.compile(PATTERN_TELEFON);
				Matcher m = p.matcher(currentLine);
				if (m.find()) {
					String id = m.group(1);
					String caller = m.group(2);
					String called = m.group(3);
					Debug.msg("NEW CALL " + id + ": " + caller + " -> "
							+ called);

					// POPUP Messages to JFritz
					JFritz.callMsg(caller, called);
				}
				/*
				 * p = Pattern.compile(PATTERN_VOIP_REQUEST); m =
				 * p.matcher(currentLine); if (m.find()) { String dialString =
				 * m.group(1); System.err.print("Sending call request to: ");
				 * System.err.println(dialString); // POPUP Messages to JFritz }
				 *
				 * p = Pattern.compile(PATTERN_VOIP_CALLTO_ESTABLISHED); m =
				 * p.matcher(currentLine); if (m.find()) {
				 * System.err.println("CALL TO: " + m.group(1) + "
				 * ESTABLISHED"); // POPUP Messages to JFritz }
				 *
				 * p = Pattern.compile(PATTERN_VOIP_CALLTO_TERMINATED); m =
				 * p.matcher(currentLine); if (m.find()) { System.err
				 * .println("CALL TO: " + m.group(1) + " TERMINATED"); // POPUP
				 * Messages to JFritz }
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		disconnect();
	}

	public static InetAddress getIP() {
		Enumeration ifaces;
		try {
			ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) ifaces.nextElement();
				System.out.println(ni.getName() + ":");

				Enumeration addrs = ni.getInetAddresses();

				while (addrs.hasMoreElements()) {
					InetAddress addr = (InetAddress) addrs.nextElement();
					System.out.println(" " + addr.getHostAddress());
					return addr;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
}
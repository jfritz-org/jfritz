/*
 * Created on 24.05.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Arno Willig
 *
 */
public class SyslogListener extends Thread implements CallMonitor{

	private final String PATTERN_TELEFON_INCOMING = "IncomingCall[^:]*: ID ([^,]*), caller: \"([^\"]*)\" called: \"([^\"]*)\"";

	private final String PATTERN_TELEFON_OUTGOING = "IncomingCall[^:]*: ID ([^,]*), caller: \"([^\"]*)\" called: \"([^\"]*)\"";

	private DatagramSocket socket;

	private JFritz jfritz;

	public SyslogListener(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		start();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		startSyslogOnFritzBox();
		startSyslogListener();
	}

	public void startSyslogListener() {
		int port = 4711;
		byte[] log_buffer = new byte[2048];
		DatagramPacket packet = new DatagramPacket(log_buffer,
				log_buffer.length);

		try {
			socket = new DatagramSocket(port);
			Debug.msg("Starting SyslogListener on port " + port);
			while (!isInterrupted()) {
				socket.receive(packet);
				String msg = new String(packet.getData());
				Debug.msg("Syslog: " + msg);
				Pattern p = Pattern.compile(PATTERN_TELEFON_INCOMING);
				Matcher m = p.matcher(msg);
				if (m.find()) {
					String id = m.group(1);
					String caller = m.group(2);
					String called = m.group(3);
					Debug.msg("NEW CALL " + id + ": " + caller + " -> "
							+ called);

					// POPUP Messages to JFritz
					JFritz.callMsg(caller, called);
				}
			}
		} catch (SocketException e) {
		} catch (IOException e) {
		}
		socket.close();
	}

	public void stopCallMonitor() {
		Debug.msg("Stopping SyslogListener");
		interrupt();
		socket.close();
	}

	private boolean isTelefondRestarted() {
		try {
			Socket sock = new Socket(JFritz.getProperty("box.address",
					"fritz.box"), 13);
			sock.close();
			return true;
			//		while (isRunning) {
			//			Socket socket = serverSocket.accept();
		} catch (IOException e) {
			Debug.msg("isTelefondRestarted(): " + e);
			return false;
		}
	}

	private void startSyslogOnFritzBox() {
		if (JFritzUtils
				.showYesNoDialog("Der telefond muss neu gestartet werden.\n"
						+ "Dabei wird ein laufendes Gespräch unterbrochen.\n"
						+ "Ohne Neustart wird der Anrufmonitor nicht funktionieren.\n"
						+ "Soll der telefond neu gestartet werden?") == 0) {
			Telnet telnet = new Telnet();
			telnet.connect();
			int port = 4711;
			String ip = "192.168.178.21";
			telnet.write("killall syslogd");
			telnet.write("syslogd -R " + ip + ":" + port);
			Debug.msg("Restarting telefond");
			// get new Calls
			jfritz.getJframe().getFetchButton().doClick();
			telnet.write("killall telefon && telefon | logger &");
			try {
				sleep(500);
			}
			catch (InterruptedException ie) {
				Debug.msg("Failed to sleep Thread SyslogListener");
			}
			telnet.disconnect();
		}
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

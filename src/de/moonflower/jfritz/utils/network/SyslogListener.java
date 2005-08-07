/*
 * Created on 24.05.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.*;

/**
 * Thread. Logon on FritzBox via Telnet. Restart syslogd and telefond on
 * FritzBox. Captures Syslog messages.
 *
 * @author Arno Willig
 *
 */
public class SyslogListener extends Thread implements CallMonitor {

	private final String PATTERN_TELEFON_INCOMING = "IncomingCall[^:]*: ID ([^,]*), caller: \"([^\"]*)\" called: \"([^\"]*)\"";

	private final String PATTERN_TELEFON_OUTGOING = "incoming[^:]*: (\\d\\d) ([^ <-]*) <- (\\d)";

	private final String PATTERN_SYSLOG_RUNNING = "syslogd -R ([^:4711]*)";

	private final String PATTERN_TELEFON_RUNNING = "telefon a";

	//private final String PATTERN_TELEFON_RUNNING2 = "telefon";

	//private final int SYSLOG_PORT = 514;

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
		startSyslogListener();
	}

	/**
	 * Starts syslog listener
	 *
	 */
	public void startSyslogListener() {
		Pattern p;
		Matcher m;
		String data;
		int port = 4711;
		byte[] log_buffer = new byte[2048];
		DatagramPacket packet = new DatagramPacket(log_buffer,
				log_buffer.length);

		try {
			Telnet telnet = new Telnet(jfritz);
			telnet.connect();
			data = telnet.sendCommand("ps -A | grep syslog");
			p = Pattern.compile(PATTERN_SYSLOG_RUNNING);
			m = p.matcher(data);
			if (m.find()) {
				Debug.msg("Syslog IS RUNNING PROPERLY on FritzBox");
			} else {
				Debug
						.msg("Syslog ISN'T RUNNING PROPERLY on FritzBox, RESTARTING SYSLOG");
				restartSyslogOnFritzBox(telnet, JFritz.getProperty(
						"option.syslogclientip", "192.168.178.21"));
			}

			data = telnet.readUntil("# ");
			data = telnet.sendCommand("ps -A | grep telefon");
			p = Pattern.compile(PATTERN_TELEFON_RUNNING);
			m = p.matcher(data);
			if (m.find()) {
				Debug
						.msg("Telefon ISN'T RUNNING PROPERLY on FritzBox, RESTARTING TELEFON");
				restartTelefonOnFritzBox(telnet);
				JFritz.setProperty("telefond.laststarted", "syslogMonitor");
			} else {

				if (!JFritz.getProperty("telefond.laststarted", "").equals(
						"syslogMonitor")) {
					Debug
							.msg("Telefon ISN'T RUNNING PROPERLY on FritzBox, RESTARTING TELEFON");
					restartTelefonOnFritzBox(telnet);
					JFritz.setProperty("telefond.laststarted", "syslogMonitor");
				} else {
					Debug.msg("Telefon IS RUNNING PROPERLY on FritzBox");
				}
			}

			telnet.disconnect();
			socket = new DatagramSocket(port);
			Debug.msg("Starting SyslogListener on port " + port);
			// DatagramSocket passthroughSocket = new
			// DatagramSocket(SYSLOG_PORT);
			while (!isInterrupted()) {
				socket.receive(packet);
				String msg = new String(log_buffer, 0, packet.getLength(),
						"UTF-8");
				Debug.msg("Get Syslogmessage: " + msg);

				//if (JFritzUtils.parseBoolean(JFritz.getProperty(
				//		"option.syslogpassthrough", "false"))) {
				//  	passthroughSocket.send(packet);
				//	    Debug.msg("SendSyslogmessage: " + msg);
				// }

				p = Pattern.compile(PATTERN_TELEFON_INCOMING);
				m = p.matcher(msg);
				if (m.find()) {
					String id = m.group(1);
					String caller = m.group(2);
					String called = m.group(3);
					Debug.msg("NEW INCOMING CALL " + id + ": " + caller
							+ " -> " + called);

					// POPUP Messages to JFritz
					jfritz.callInMsg(caller, called);
				}
				p = Pattern.compile(PATTERN_TELEFON_OUTGOING);
				m = p.matcher(msg);
				if (m.find()) {
					String called = m.group(2);
					if (!called.equals("")) {
						Debug.msg("NEW OUTGOING CALL: " + called);
						//						JFritz.callOutMsg(called);
					}
				}
			}
		} catch (SocketException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Stops call monitor
	 */
	public void stopCallMonitor() {
		Debug.msg("Stopping SyslogListener");
		interrupt();
	}

	/**
	 * Restarts syslog daemon
	 *
	 * @param telnet
	 *            Telnet connection
	 * @param ip
	 *            IP for syslog remote logging
	 */
	public static void restartSyslogOnFritzBox(Telnet telnet, String ip) {
		int port = 4711;
		Debug.msg("Starte Syslog auf der FritzBox: syslog -R " + ip + ":"
				+ port);
		telnet.sendCommand("killall syslogd");
		try {
			sleep(1000);
		} catch (InterruptedException e) {
		}
		telnet.sendCommand("syslogd -R " + ip + ":" + port);
		try {
			sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Restarts telefon daemon
	 *
	 * @param telnet
	 *            Telnet connection
	 */
	private void restartTelefonOnFritzBox(Telnet telnet) {
		if (JOptionPane.showConfirmDialog(null,
				"Der telefond muss neu gestartet werden.\n"
						+ "Dabei wird ein laufendes Gespräch unterbrochen. "
						+ "Die Anrufliste wird vorher gesichert.\n"
						+ "Soll der telefond neu gestartet werden?",
				JFritz.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			Debug.msg("Get new calls");
			jfritz.getJframe().getFetchButton().doClick();
			try {
				sleep(5000);
			} catch (InterruptedException e) {
			}
			telnet.sendCommand("killall telefon");
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}
			telnet.sendCommand("telefon | logger &");
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}
			Debug.msg("telefond restarted");
		}
	}

	/**
	 * @return Returns vector of local IPs
	 */
	public static Vector getIP() {
		Enumeration ifaces;
		Vector addresses = new Vector();
		try {
			ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) ifaces.nextElement();
				System.out.println(ni.getName() + ":");
				if (!ni.getName().equals("lo")) {
					Enumeration addrs = ni.getInetAddresses();

					while (addrs.hasMoreElements()) {
						InetAddress addr = (InetAddress) addrs.nextElement();
						System.out.println(" " + addr.getHostAddress());

						addresses.add(addr);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return addresses;
	}
}

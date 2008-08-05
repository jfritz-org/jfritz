/*
 * Created on 24.05.2005
 *
 */
package de.moonflower.jfritz.callmonitor;

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
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.*;
import de.moonflower.jfritz.utils.network.Telnet;

/**
 * Thread. Logon on FritzBox via Telnet. Restart syslogd and telefond on
 * FritzBox. Captures Syslog messages.
 *
 * @author Arno Willig
 *
 */
public class SyslogCallMonitor extends Thread implements CallMonitorInterface {

	private final String PATTERN_TELEFON_INCOMING = "IncomingCall[^:]*: ID ([^,]*), caller: \"([^\"]*)\" called: \"([^\"]*)\""; //$NON-NLS-1$

	private final String PATTERN_TELEFON_OUTGOING = "incoming[^:]*: (\\d\\d) ([^ <-]*) <- (\\d)"; //$NON-NLS-1$

	private final String PATTERN_SYSLOG_RUNNING = "syslogd -R ([^:]*)([^ ]*)"; //$NON-NLS-1$

	private final String PATTERN_TELEFON_RUNNING = "telefon a"; //$NON-NLS-1$

	//private final String PATTERN_TELEFON_RUNNING2 = "telefon";

	//private final int SYSLOG_PORT = 514;

	private DatagramSocket socket = null;

	private StatusListener statusListener;

	private Telnet telnet;

	public SyslogCallMonitor(StatusListener statusListener) {
		super();
		this.statusListener = statusListener;
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
			if (JFritzUtils.parseBoolean(Main.getProperty(
					"syslog.checkSyslog", "true")) //$NON-NLS-1$,  //$NON-NLS-2$
					|| JFritzUtils.parseBoolean(Main.getProperty(
							"syslog.checkTelefon", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
				telnet = new Telnet();
				telnet.getStatusBarController().addStatusBarListener(statusListener);
				telnet.connect();
				if (telnet.isConnected()) {
					if (JFritzUtils.parseBoolean(Main.getProperty(
							"syslog.checkSyslog", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
						data = telnet.sendCommand("ps -A | grep syslog"); //$NON-NLS-1$
						p = Pattern.compile(PATTERN_SYSLOG_RUNNING);
						m = p.matcher(data);
						if (m.find()) {
							// m.group(1) = IP
							// m.group(2) = Port
							if (m.group(1).equals(
									Main.getProperty("option.syslogclientip", //$NON-NLS-1$
											"192.168.178.21")) //$NON-NLS-1$
									&& (m.group(2).equals(":4711"))) { //$NON-NLS-1$
								Debug
										.msg("Syslog IS RUNNING PROPERLY on FritzBox"); //$NON-NLS-1$
							} else {
								Debug
										.msg("Syslog ISN'T RUNNING PROPERLY on FritzBox, RESTARTING SYSLOG"); //$NON-NLS-1$
								restartSyslogOnFritzBox(telnet, Main.getProperty("option.syslogclientip", //$NON-NLS-1$
												"192.168.178.21")); //$NON-NLS-1$
							}
						} else {
							Debug
									.msg("Syslog ISN'T RUNNING PROPERLY on FritzBox, RESTARTING SYSLOG"); //$NON-NLS-1$
							restartSyslogOnFritzBox(telnet, Main.getProperty(
									"option.syslogclientip", "192.168.178.21")); //$NON-NLS-1$,  //$NON-NLS-2$
						}

						data = telnet.readUntil("# "); //$NON-NLS-1$
					}
					if (JFritzUtils.parseBoolean(Main.getProperty(
							"syslog.checkTelefon", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
						data = telnet.sendCommand("ps -A | grep telefon"); //$NON-NLS-1$
						p = Pattern.compile(PATTERN_TELEFON_RUNNING);
						m = p.matcher(data);
						if (m.find()) {
							Debug
									.msg("Telefon ISN'T RUNNING PROPERLY on FritzBox, RESTARTING TELEFON"); //$NON-NLS-1$
							restartTelefonOnFritzBox(telnet);
						} else {

							if (!Main.getProperty("telefond.laststarted", "") //$NON-NLS-1$,  //$NON-NLS-2$
									.equals("syslogMonitor")) { //$NON-NLS-1$
								Debug
										.msg("Telefon ISN'T RUNNING PROPERLY on FritzBox, RESTARTING TELEFON"); //$NON-NLS-1$
								restartTelefonOnFritzBox(telnet);
							} else {
								Debug
										.msg("Telefon IS RUNNING PROPERLY on FritzBox"); //$NON-NLS-1$
							}
						}
					}
					telnet.disconnect();
				} else {
					// Telnet not connected
					// Disable call monitor
					JFritz.stopCallMonitor();
				}
			}
			socket = new DatagramSocket(port);
			Debug.msg("Starting SyslogListener on port " + port); //$NON-NLS-1$
			// DatagramSocket passthroughSocket = new
			// DatagramSocket(SYSLOG_PORT);
			while (!isInterrupted()) {
				socket.receive(packet);
				String msg = new String(log_buffer, 0, packet.getLength(),
						"UTF-8"); //$NON-NLS-1$
				Debug.msg("Get Syslogmessage: " + msg); //$NON-NLS-1$

				//if (JFritzUtils.parseBoolean(Main.getProperty(
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
					Debug.msg("NEW INCOMING CALL " + id + ": " + caller //$NON-NLS-1$,  //$NON-NLS-2$
							+ " -> " + called); //$NON-NLS-1$

					// POPUP Messages to JFritz
                    // TODO: Add call to CallMonitorList and show message if number is not in ignore list
                    //JFritz.getCallMonitorList().displayCallInMsg(caller, called);
				}
				p = Pattern.compile(PATTERN_TELEFON_OUTGOING);
				m = p.matcher(msg);
				if (m.find()) {
					String called = m.group(2);
					if (!called.equals("")) { //$NON-NLS-1$
						Debug.msg("NEW OUTGOING CALL: " + called); //$NON-NLS-1$
						//						JFritz.callOutMsg(called);
					}
				}
			}
		} catch (SocketException e) {
			if (!e.toString().equals("java.net.SocketException: socket closed")) { //$NON-NLS-1$
				Debug.err("SocketException: " + e); //$NON-NLS-1$
			}
		} catch (IOException e) {
			Debug.err("IOException: " + e); //$NON-NLS-1$
		} catch (WrongPasswordException e1) {
			JFritz.errorMsg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
			Debug.errDlg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
		} catch (InvalidFirmwareException e1) {
			JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
			Debug.errDlg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
		}
	}

	/**
	 * Stops call monitor
	 */
	public void stopCallMonitor() {
		Debug.msg("Stopping SyslogListener"); //$NON-NLS-1$
		interrupt();
		if (socket != null) {
			socket.close();
		}
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
		Debug.msg("Start syslogd on FritzBox with: syslog -R " + ip + ":" //$NON-NLS-1$,  //$NON-NLS-2$
				+ port);
		telnet.sendCommand("killall syslogd"); //$NON-NLS-1$
		try {
			sleep(1000);
		} catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}
		telnet.sendCommand("syslogd -R " + ip + ":" + port); //$NON-NLS-1$,  //$NON-NLS-2$
		try {
			sleep(1000);
		} catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}
		telnet.readUntil("# "); //$NON-NLS-1$
		// Stimmt so, dass 2 mal bis PROMPT gelesen wird
		telnet.readUntil("# "); //$NON-NLS-1$
		try {
			sleep(1000);
		} catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}
	}

	/**
	 * Restarts telefon daemon
	 *
	 * @param telnet
	 *            Telnet connection
	 */
	public static int restartTelefonOnFritzBox(Telnet telnet) {
		if (JOptionPane.showConfirmDialog(null,
				"Der telefond muss neu gestartet werden.\n" //TODO: I18N
						+ "Dabei wird ein laufendes Gespräch unterbrochen. "//TODO: I18N
						+ "Die Anrufliste wird vorher gesichert.\n" //TODO: I18N
						+ "Soll der telefond neu gestartet werden?", //TODO: I18N
						Main.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			Debug.msg("Get new calls"); //$NON-NLS-1$
			JFritz.getJframe().getFetchButton().doClick();
			try {
				sleep(5000);
			} catch (InterruptedException e) {
	        	Thread.currentThread().interrupt();
			}
			telnet.sendCommand("killall telefon"); //$NON-NLS-1$
			try {
				sleep(1000);
			} catch (InterruptedException e) {
	        	Thread.currentThread().interrupt();
			}
			telnet.sendCommand("telefon | logger &"); //$NON-NLS-1$
			try {
				sleep(1000);
			} catch (InterruptedException e) {
	        	Thread.currentThread().interrupt();
			}
			Debug.msg("telefond restarted"); //$NON-NLS-1$
			Main.setProperty("telefond.laststarted", "syslogMonitor"); //$NON-NLS-1$,  //$NON-NLS-2$
			return JOptionPane.YES_OPTION;
		} else {
			JFritz.stopCallMonitor();
			return JOptionPane.NO_OPTION;
		}
	}

	/**
	 * @return Returns vector of local IPs
	 */
	public static Vector<InetAddress> getIP() {
		Enumeration ifaces;
		Vector<InetAddress> addresses = new Vector<InetAddress>();
		try {
			ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) ifaces.nextElement();
				if (!ni.getName().equals("lo")) { //$NON-NLS-1$
					Enumeration addrs = ni.getInetAddresses();

					while (addrs.hasMoreElements()) {
						InetAddress addr = (InetAddress) addrs.nextElement();

						addresses.add(addr);
					}
				}
			}
		} catch (SocketException e) {
            Debug.err(e.toString());
		}
		return addresses;
	}

	public boolean isConnected() {
		return telnet.isConnected();
	}
}

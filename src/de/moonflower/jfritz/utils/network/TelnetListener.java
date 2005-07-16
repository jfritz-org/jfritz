package de.moonflower.jfritz.utils.network;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.network.Telnet;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import java.io.IOException;

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

	private Telnet telnet;

	public TelnetListener(JFritz jfritz) {
		// Fetch new calls
		jfritz.getJframe().getFetchButton().doClick();
		telnet = new Telnet();
		Debug.msg("Starting TelnetListener");
		telnet.connect();
		start();
	}

	public void run() {
		Debug.msg("run()");
		restartTelefonDaemon();
		parseOutput();
	}

	private void restartTelefonDaemon() {
		telnet.write("killall telefon && telefon &>&1");
		Debug.msg("Telefon Daemon restarted.");
	}

	public void parseOutput() {
		try {
			String currentLine = "";
			while (!isInterrupted()) {
				currentLine = telnet.readUntil("\n");
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
		telnet.disconnect();
	}

	public void stopTelnetListener() {
		Debug.msg("Stopping TelnetListener");
		interrupt();
	}
}
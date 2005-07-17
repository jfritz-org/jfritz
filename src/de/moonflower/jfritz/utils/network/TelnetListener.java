package de.moonflower.jfritz.utils.network;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
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

	private boolean isRunning = false;

	private JFritz jfritz;

	public TelnetListener(JFritz jfritz) {
		// Fetch new calls
		this.jfritz = jfritz;
		telnet = new Telnet();
		Debug.msg("Starting TelnetListener");
		telnet.connect();
		start();
	}

	public void run() {
		Debug.msg("run()");
		if (JFritzUtils
				.showYesNoDialog("Der telefond muss neu gestartet werden.\n"
						+ "Dabei wird ein laufendes Gespräch unterbrochen.\n"
						+ "Ohne Neustart wird der Anrufmonitor nicht funktionieren.\n"
						+ "Soll der telefond neu gestartet werden?") == 0) {

		jfritz.getJframe().getFetchButton().doClick();
		restartTelefonDaemon();
		isRunning = true;
		parseOutput();
		}
	}

	private void restartTelefonDaemon() {
		telnet.write("killall telefon && telefon &>&1");
		Debug.msg("Telefon Daemon restarted.");
	}

	public void parseOutput() {
		try {
			String currentLine = "";
			while (isRunning) {
				currentLine = telnet.readUntil("\n");
				Pattern p = Pattern.compile(PATTERN_TELEFON);
				Matcher m = p.matcher(currentLine);
				if (m.find()) {
					String id = m.group(1);
					String caller = m.group(2);
					String called = m.group(3);
					Debug.msg("NEW CALL " + id + ": " + caller + " -> "
							+ called);

					JFritz.callMsg(caller, called);
					if (!isRunning) break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		telnet.disconnect();
	}

	public void stopTelnetListener() {
		Debug.msg("Stopping TelnetListener");
		isRunning = false;
	}
}
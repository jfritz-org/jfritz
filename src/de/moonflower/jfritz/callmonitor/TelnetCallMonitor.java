package de.moonflower.jfritz.callmonitor;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.StatusListener;
import de.moonflower.jfritz.utils.network.Telnet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.JOptionPane;

/**
 * Thread. Logon on FritzBox via Telnet. Captures Callermessages via Telnet.
 *
 *
 */

public class TelnetCallMonitor extends Thread implements CallMonitorInterface {

    // IncomingCall: ID 0, caller: "017623352711" called: "592904"
    // IncomingCall from NT: ID 0, caller: "592904" called: "1815212"
    private final String PATTERN_TELEFON = "IncomingCall[^:]*: ID ([^,]*), caller: \"([^\"]*)\" called: \"([^\"]*)\""; //$NON-NLS-1$

    private Telnet telnet;

    private boolean isRunning = false;

	private StatusListener statusListener;

    public TelnetCallMonitor(StatusListener statusListener) {
        super();
        this.statusListener = statusListener;
        start();

    }

    public void run() {
        telnet = new Telnet();
        telnet.getStatusBarController().addStatusBarListener(statusListener);
        Debug.msg("Starting TelnetListener"); //$NON-NLS-1$
        telnet.connect();
        if (telnet.isConnected()) {
            Debug.msg("run()"); //$NON-NLS-1$
            if (JOptionPane
                    .showConfirmDialog(
                            null,
                            "Der telefond muss neu gestartet werden.\n" // TODO: I18N
                                    + "Dabei wird ein laufendes Gespräch unterbrochen. Die Anrufliste wird vorher gesichert.\n" // TODO: I18N
                                    + "Soll der telefond neu gestartet werden?", // TODO: I18N
                                    Main.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JFritz.getJframe().getFetchButton().doClick();
                restartTelefonDaemon();
                parseOutput();
            } else {
                JFritz.stopCallMonitor();
            }
        } else
            JFritz.stopCallMonitor();

    }

    private void restartTelefonDaemon() {
        telnet.write("killall telefon"); //$NON-NLS-1$
        telnet.readUntil("# "); //$NON-NLS-1$
        telnet.readUntil("# "); //$NON-NLS-1$
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            Debug.err("Fehler beim Schlafen: " + e); //$NON-NLS-1$
        }
        telnet.write("telefon &>&1 &"); //$NON-NLS-1$
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            Debug.err("Fehler beim Schlafen: " + e); //$NON-NLS-1$
        }
        Debug.msg("Telefon Daemon restarted."); //$NON-NLS-1$
        Main.setProperty("telefond.laststarted", "telnetMonitor"); //$NON-NLS-1$,  //$NON-NLS-2$
    }

    public void parseOutput() {
        isRunning = true;
        try {
            String currentLine = ""; //$NON-NLS-1$
            while (isRunning) {
                currentLine = telnet.readUntil("\n"); //$NON-NLS-1$
                Pattern p = Pattern.compile(PATTERN_TELEFON);
                Matcher m = p.matcher(currentLine);
                if (m.find()) {
                    String id = m.group(1);
                    String caller = m.group(2);
                    String called = m.group(3);
                    Debug.msg("NEW CALL " + id + ": " + caller + " -> " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                            + called);

                    // TODO: Add call to CallMonitorList and show message if number is not in ignore list
                    //JFritz.getCallMonitorList().displayCallInMsg(caller, called);
                    if (!isRunning)
                        break;
                }

            }
        } catch (Exception e) {
            Debug.err(e.toString());
            isRunning = false;
        }
        telnet.disconnect();
    }

    public void stopCallMonitor() {
        Debug.msg("Stopping TelnetListener"); //$NON-NLS-1$
        isRunning = false;
    }
}
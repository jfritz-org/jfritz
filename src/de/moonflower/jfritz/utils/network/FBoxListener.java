package de.moonflower.jfritz.utils.network;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public class FBoxListener extends Thread implements CallMonitor {

    private JFritz jfritz;

    private BufferedReader in;

    private Socket clientSocket;

    private String[] ignoredMSNs;

    public FBoxListener(JFritz jfritz) {
        super();
        this.jfritz = jfritz;
        Debug.msg("Starting FBoxListener");
        start();
    }

    public void run() {
        Debug.msg("run()");
        if (connect()) {
            Debug.msg("Connected");
            readOutput();
        }
    }

    private boolean connect() {
        try {
            Debug.msg("Trying to connect to "
                    + JFritz.getProperty("box.address") + ":1012");
            clientSocket = new Socket(JFritz.getProperty("box.address"), 1012);
            return true;
        } catch (UnknownHostException uhe) {
            Debug.msg("Unknown host exception: " + uhe.toString());
            Debug
                    .errDlg("Konnte keine Verbindung zu "
                            + JFritz.getProperty("box.address")
                            + ":1012"
                            + " aufnehmen. \n"
                            + "\n"
                            + "Aktivieren Sie den Anrufmonitor, indem Sie am Telefon #96*5* wählen.\n"
                            + "Es sollte ein Bestätigungston kommen.");
            jfritz.stopCallMonitor();
        } catch (IOException ioe) {
            Debug.msg("IO exception: " + ioe.toString());
            Debug
                    .errDlg("Konnte keine Verbindung zu "
                            + JFritz.getProperty("box.address")
                            + ":1012"
                            + " aufnehmen. \n"
                            + "\n"
                            + "Aktivieren Sie den Anrufmonitor, indem Sie am Telefon #96*5* wählen.\n"
                            + "Es sollte ein Bestätigungston kommen.");
            jfritz.stopCallMonitor();
        }
        return false;
    }

    private void readOutput() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket
                    .getInputStream()));
            String currentLine;
            while (!isInterrupted()) {
                // lese nächste Nachricht ein
                currentLine = in.readLine();
                parseOutput(currentLine);
            }
        } catch (IOException ioe) {
            Debug.msg("IO exception: " + ioe.toString());
        }
    }

    private void initIgnoreList() {
        String ignoreMSNString = JFritz.getProperty(
                "option.callmonitor.ignoreMSN", "");
        if (ignoreMSNString.length() > 0 && ignoreMSNString.indexOf(";") == -1) {
            ignoreMSNString = ignoreMSNString + ";";
        }
        ignoredMSNs = ignoreMSNString.split(";");
        Debug.msg("Ignored MSNs: ");
        for (int i=0; i<ignoredMSNs.length; i++) {
            Debug.msg(ignoredMSNs[i]);
        }
    }

    private void parseOutput(String line) {
        initIgnoreList();
        Debug.msg("Server: " + line);
        String number = "";
        String provider = "";
        String[] split;
        split = line.split(";", 7);
        for (int i = 0; i < split.length; i++) {
            Debug.msg("Split[" + i + "] = " + split[i]);
        }
        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.monitorIncomingCalls", "true"))
                && split[1].equals("RING")) {
            if (split[3].equals("")) {
                number = "Unbekannt";
            } else
                number = split[3];
            if (split[4].equals("")) {
                provider = "Analog";
            } else
                provider = split[4];
            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (provider.equals(ignoredMSNs[i])) {
                    ignoreIt = true;
                    break;
                }
            if (!ignoreIt)
                jfritz.callInMsg(number, provider);
        } else if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.monitorOutgoingCalls", "true"))
                && split[1].equals("CALL")) {
            if (split[5].equals("")) {
                number = "Unbekannt";
            } else
                number = split[5];
            if (split[4].equals("")) {
                provider = "Analog";
            } else
                provider = split[4];
            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (provider.equals(ignoredMSNs[i])) {
                    ignoreIt = true;
                    break;
                }
            if (!ignoreIt)
                jfritz.callOutMsg(number, provider);
        }
    }

    public void stopCallMonitor() {
        Debug.msg("Stopping FBoxListener");
        try {
            if (clientSocket != null)
                clientSocket.close();
            this.interrupt();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
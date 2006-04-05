package de.moonflower.jfritz.utils.network;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public abstract class FBoxListener extends Thread implements CallMonitor {

    protected JFritz jfritz;

    protected BufferedReader in;

    protected Socket clientSocket;

    protected String[] ignoredMSNs;

    protected Random zufallszahl;

    public FBoxListener(JFritz jfritz) {
        super();
        this.jfritz = jfritz;
        Debug.msg("Starting FBoxListener");
        start();
        zufallszahl = new Random();
    }

    public abstract void run();

    protected boolean connect() {
        try {
            Debug.msg("Trying to connect to "
                    + JFritz.getProperty("box.address") + ":1012");
            clientSocket = new Socket(JFritz.getProperty("box.address"), 1012);
            clientSocket.setKeepAlive(true);
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

    protected void readOutput() {
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

    protected void initIgnoreList() {
        String ignoreMSNString = JFritz.getProperty(
                "option.callmonitor.ignoreMSN", "");
        if (ignoreMSNString.length() > 0 && ignoreMSNString.indexOf(";") == -1) {
            ignoreMSNString = ignoreMSNString + ";";
        }
        ignoredMSNs = ignoreMSNString.split(";");
        Debug.msg("Ignored MSNs: ");
        for (int i = 0; i < ignoredMSNs.length; i++) {
            Debug.msg(ignoredMSNs[i]);
        }
    }

    protected abstract void parseOutput(String line);

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
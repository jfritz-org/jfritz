package de.moonflower.jfritz.utils.network;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public class FBoxListenerV3 extends FBoxListener {

    public FBoxListenerV3(JFritz jfritz) {
        super(jfritz);
        Debug.msg("FBoxListener V3");
    }

    public void run() {
        if (super.connect()) {
            Debug.msg("Connected");
            readOutput();
        }
    }

    protected void parseOutput(String line) {
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
            if (number.endsWith("#"))
                number = number.substring(0, number.length() - 1);
            if (split[5].equals("")) { // Used Provider
                provider = "Analog";
            } else
                provider = split[5];
            provider = jfritz.getSIPProviderTableModel().getSipProvider(
                    provider, provider);
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
            if (number.endsWith("#"))
                number = number.substring(0, number.length() - 1);
            if (split[6].equals("")) { // Used Provider
                provider = "Analog";
            } else
                provider = split[6];
            provider = jfritz.getSIPProviderTableModel().getSipProvider(
                    provider, provider);
            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (provider.equals(ignoredMSNs[i])) {
                    ignoreIt = true;
                    break;
                }
            if (!ignoreIt)
                jfritz.callOutMsg(number, provider);
        } else if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.fetchAfterDisconnect", "true"))
                && split[1].equals("DISCONNECT")) {
            try {
                Thread.sleep(zufallszahl.nextInt(3000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                Debug.err(e.toString());
            }
            jfritz.getJframe().fetchList();
        }
    }
}
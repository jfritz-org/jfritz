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

public class FBoxListenerV1 extends FBoxListener {

    public FBoxListenerV1() {
        Debug.msg("FBoxListener V1"); //$NON-NLS-1$
    }

    public void run() {
        if (super.connect()) {
            Debug.msg("Connected"); //$NON-NLS-1$
            readOutput();
        }
    }

    protected void parseOutput(String line) {
        initIgnoreList();
        Debug.msg("Server: " + line); //$NON-NLS-1$
        String number = ""; //$NON-NLS-1$
        String provider = ""; //$NON-NLS-1$
        String[] split;
        split = line.split(";", 7); //$NON-NLS-1$
        for (int i = 0; i < split.length; i++) {
            Debug.msg("Split[" + i + "] = " + split[i]); //$NON-NLS-1$,  //$NON-NLS-2$
        }
        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.monitorIncomingCalls", "true")) //$NON-NLS-1$, //$NON-NLS-2$
                && split[1].equals("RING")) { //$NON-NLS-1$
            if (split[3].equals("")) { //$NON-NLS-1$
                number = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[3];
			if (number.endsWith("#")) number = number.substring(0, number.length()-1); //$NON-NLS-1$
            if (split[4].equals("")) { //$NON-NLS-1$
                provider = JFritz.getMessage("fixed_network"); //$NON-NLS-1$
            } else
                provider = split[4];
            provider = JFritz.getSIPProviderTableModel().getSipProvider(
                    provider, provider);
            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (provider.equals(ignoredMSNs[i])) {
                    ignoreIt = true;
                    break;
                }
            if (!ignoreIt)
                callMonitoring.displayCallInMsg(number, provider);
        } else if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.monitorOutgoingCalls", "true")) //$NON-NLS-1$,  //$NON-NLS-2$
                && split[1].equals("CALL")) { //$NON-NLS-1$
            if (split[5].equals("")) { //$NON-NLS-1$
                number = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[5];
			if (number.endsWith("#")) number = number.substring(0, number.length()-1); //$NON-NLS-1$
            if (split[4].equals("")) { //$NON-NLS-1$
                provider = JFritz.getMessage("fixed_network"); //$NON-NLS-1$
            } else
                provider = split[4];
            provider = JFritz.getSIPProviderTableModel().getSipProvider(
                    provider, provider);
            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (provider.equals(ignoredMSNs[i])) {
                    ignoreIt = true;
                    break;
                }
            if (!ignoreIt)
                callMonitoring.displayCallOutMsg(number, provider);
        } else if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.fetchAfterDisconnect", "true")) //$NON-NLS-1$,  //$NON-NLS-2$
                && split[1].equals("DISCONNECT")) { //$NON-NLS-1$
            try {
                Thread.sleep(zufallszahl.nextInt(3000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                Debug.err(e.toString());
            }
            JFritz.getJframe().fetchList();
        }
    }
}
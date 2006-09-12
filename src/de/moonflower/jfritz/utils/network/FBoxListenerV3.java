package de.moonflower.jfritz.utils.network;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public class FBoxListenerV3 extends FBoxListener {

    public FBoxListenerV3() {
        Debug.msg("FBoxListener V3"); //$NON-NLS-1$
    }

    public void run() {
        if (super.connect()) {
            Debug.msg("Connected"); //$NON-NLS-1$
            readOutput();
        }
    }

    public void parseOutput(String line) {
        parseOutput(line, true);
    }

    public void parseOutput(String line, boolean interactWithJFritz) {
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
                "option.callmonitor.monitorIncomingCalls", "true")) //$NON-NLS-1$,  //$NON-NLS-2$
                && split[1].equals("RING")) { //$NON-NLS-1$
            if (split[3].equals("")) { //$NON-NLS-1$
                number = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[3];
            if (number.endsWith("#")) //$NON-NLS-1$
                number = number.substring(0, number.length() - 1);

            if (split[5].equals("POTS")) { //$NON-NLS-1$
                if (split[4].equals("")) { //$NON-NLS-1$
                    provider = JFritz.getMessage("fixed_network"); //$NON-NLS-1$
                }
                else {
                    provider = split[4] + " (" + JFritz.getMessage("fixed_network") + ")"; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                }
            } else if (split[5].startsWith("SIP")) { //$NON-NLS-1$
                provider = split[4] + " (SIP)"; //$NON-NLS-1$

            }  else if (split[5].equals("ISDN")) { //$NON-NLS-1$
                provider = split[4] + " (ISDN)"; //$NON-NLS-1$
            } else
                provider = split[4];

            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (!ignoredMSNs[i].equals(""))
                    if (split[4].equals(ignoredMSNs[i])) {
                        ignoreIt = true;
                        break;
                        }

            try {
                Call currentCall = new Call(new CallType(CallType.CALLIN),new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(split[0]), new PhoneNumber(number), "0", provider, 0);
                CallMonitor.callMonitoring.addNewCall(Integer.parseInt(split[2]), currentCall);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (!ignoreIt && interactWithJFritz)
                callMonitoring.displayCallInMsg(number, provider);
        } else if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.callmonitor.monitorOutgoingCalls", "true")) //$NON-NLS-1$,  //$NON-NLS-2$
                && split[1].equals("CALL")) { //$NON-NLS-1$
            if (split[5].equals("")) { //$NON-NLS-1$
                number = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[5];
            if (number.endsWith("#")) //$NON-NLS-1$
                number = number.substring(0, number.length() - 1);

            // Entferne das unnötige ; am Ende von SIPX;
            if ( split[6].endsWith(";")) split[6] = split[6].substring(0, split[6].length()-1);

            if (split[6].equals("POTS")) { //$NON-NLS-1$
                if (split[4].equals("")) { //$NON-NLS-1$
                    provider = JFritz.getMessage("fixed_network"); //$NON-NLS-1$
                }
                else {
                    provider = split[4] + " (" + JFritz.getMessage("fixed_network") + ")"; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                }
            } else if (split[6].startsWith("SIP")) { //$NON-NLS-1$
                provider = split[4] + " (" + JFritz.getSIPProviderTableModel().getSipProvider(split[6],split[6]) + ")"; //$NON-NLS-1$,  //$NON-NLS-2$

            }  else if (split[6].equals("ISDN")) { //$NON-NLS-1$
                provider = split[4] + " (ISDN)"; //$NON-NLS-1$
            } else
                provider = split[4];

            try {
                Call currentCall = new Call(new CallType(CallType.CALLOUT),new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(split[0]), new PhoneNumber(number), split[3], provider, 0);
                CallMonitor.callMonitoring.addNewCall(Integer.parseInt(split[2]), currentCall);
            } catch (ParseException e) {
                System.err.println("FBoxListenerV3: Could not convert call" + e);
            }

            boolean ignoreIt = false;
            for (int i = 0; i < ignoredMSNs.length; i++)
                if (!ignoredMSNs[i].equals(""))
                    if (split[4].equals(ignoredMSNs[i])) {
                        ignoreIt = true;
                        break;
                }
            if (!ignoreIt && interactWithJFritz)
                callMonitoring.displayCallOutMsg(number, provider);
        } else if (split[1].equals("DISCONNECT")) { //$NON-NLS-1$
            try {
                CallMonitor.callMonitoring.removeCall(Integer.parseInt(split[2]));
                Thread.sleep(zufallszahl.nextInt(3000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                Debug.err(e.toString());
            }
/**
 * TODO: ENABLE this
            if (JFritzUtils.parseBoolean(JFritz.getProperty(
                    "option.callmonitor.fetchAfterDisconnect", "false"))) //$NON-NLS-1$,  //$NON-NLS-2$
                    JFritz.getJframe().fetchList();
**/
        } else if (split[1].equals("CONNECT")) {
            int callId = Integer.parseInt(split[2]);
            String port = split[3];
            if (split[4].equals("")) { //$NON-NLS-1$
                number = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[4];
            if (number.endsWith("#")) //$NON-NLS-1$
                number = number.substring(0, number.length() - 1);

            Call call = CallMonitor.callMonitoring.getCall(callId);
            PhoneNumber pn = new PhoneNumber(number);
            if ( pn.getIntNumber().equals(call.getPhoneNumber().getIntNumber())) {
                try {
                    CallMonitor.callMonitoring.getCall(callId).setCalldate(new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(split[0]));
                    CallMonitor.callMonitoring.getCall(callId).setPort(port);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                CallMonitor.callMonitoring.establishCall(callId);
            }
        }
    }
}
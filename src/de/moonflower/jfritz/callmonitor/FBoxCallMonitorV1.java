package de.moonflower.jfritz.callmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public class FBoxCallMonitorV1 extends FBoxCallMonitor {
	private Logger log = Logger.getLogger(FBoxCallMonitorV1.class);

	private boolean connected = false;
	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

    public FBoxCallMonitorV1(FritzBox fritzBox, Vector<CallMonitorStatusListener> stateListener, boolean shouldConnect) {
    	super(fritzBox, stateListener, shouldConnect);
        log.info("(CM1) FBoxListener V1"); //$NON-NLS-1$
    }

    public void run() {
    	while (this.isRunning())
    	{
    		if (!this.isConnected()) {
    			connect();
    		}
    		readOutput();
    	}
    }

    protected void parseOutput(String line) {
        log.debug("(CM1) Server: " + line); //$NON-NLS-1$
        String number = ""; //$NON-NLS-1$
        String provider = ""; //$NON-NLS-1$
        String[] split;
        split = line.split(";", 7); //$NON-NLS-1$
        for (int i = 0; i < split.length; i++) {
            log.debug("(CM1) Split[" + i + "] = " + split[i]); //$NON-NLS-1$,  //$NON-NLS-2$
        }

        boolean monitortableIncomingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.monitorTableIncomingCalls"));
        boolean popupIncomingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.popupIncomingCalls"));
        boolean parseIncomingCalls = monitortableIncomingCalls || popupIncomingCalls;

        boolean monitortableOutgoingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.monitorTableOutgoingCalls"));
        boolean popupOutgoingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.popupOutgoingCalls"));
        boolean parseOutgoingCalls = monitortableOutgoingCalls || popupOutgoingCalls;

        if (parseIncomingCalls && split[1].equals("RING")) { //$NON-NLS-1$
            if (split[3].equals("")) { //$NON-NLS-1$
                number = messages.getMessage("unknown"); //$NON-NLS-1$
            } else
            {
            	number = split[3];
            }

            if (number.endsWith("#")) //$NON-NLS-1$
            {
            	number = number.substring(0, number.length() - 1); //$NON-NLS-1$
            }

            if (split[4].equals("")) { //$NON-NLS-1$
                provider = messages.getMessage("fixed_network"); //$NON-NLS-1$
            } else
            {
                provider = split[4];
            }

            try
            {
            	int id = Integer.parseInt(provider);
            	if ((fritzBox != null)
            		&& (fritzBox.getSipProvider(id) != null))
            	{
            		provider = fritzBox.getSipProvider(id).toString();
            	}
            } catch (NumberFormatException nfe)
            {
            	log.warn("(CM1) Provider '" + provider + "' is not a number");
            }

            try {
                Call currentCall = new Call(CallType.CALLIN,
                        new SimpleDateFormat("dd.MM.yy HH:mm:ss")
                                .parse(split[0]), new PhoneNumberOld(this.properties, number, false),
                                new Port(0, "", "-1", "-1"), provider, 0);
                JFritz.getCallMonitorList().addNewCall(
                        Integer.parseInt(split[2]), currentCall);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (parseOutgoingCalls && split[1].equals("CALL")) { //$NON-NLS-1$
            if (split[5].equals("")) { //$NON-NLS-1$
                number = messages.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[5];
            if (number.endsWith("#"))number = number.substring(0, number.length() - 1); //$NON-NLS-1$
            if (split[4].equals("")) { //$NON-NLS-1$
                provider = messages.getMessage("fixed_network"); //$NON-NLS-1$
            } else {
                provider = split[4];
            }

            try
            {
            	int id = Integer.parseInt(provider);
            	if ((fritzBox != null)
            		&& (fritzBox.getSipProvider(id) != null))
            	{
            		provider = fritzBox.getSipProvider(id).toString();
            	}
            } catch (NumberFormatException nfe)
            {
            	log.warn("(CM1) Provider '" + provider + "' is not a number");
            }

            Port port = null;
            try {
            	int portId = Integer.parseInt(split[3]);
            	port = fritzBox.getConfiguredPort(portId);
				if (port == null) { // Fallback auf statisch konfigurierte Ports
					port = Port.getPort(portId);
				}
            } catch (NumberFormatException nfe)
            {
            	port = new Port(0, "", "-1", "-1");
            }
            try {
                Call currentCall = new Call(CallType.CALLOUT,
                        new SimpleDateFormat("dd.MM.yy HH:mm:ss")
                                .parse(split[0]),
                                new PhoneNumberOld(this.properties, number, JFritzUtils.parseBoolean(properties.getProperty("option.activateDialPrefix"))),
                                port, provider, 0);
                JFritz.getCallMonitorList().addNewCall(
                        Integer.parseInt(split[2]), currentCall);
            } catch (ParseException e) {
                log.error("(CM1) FBoxListenerV1: Could not convert call" + e);
            }
        } else if (split[1].equals("DISCONNECT")) { //$NON-NLS-1$
            int callId = Integer.parseInt(split[2]);
            Call call = JFritz.getCallMonitorList().getCall(callId);
            if (call != null) {
                call.setDuration(Integer.parseInt(split[3]));
                JFritz.getCallMonitorList().removeCall(
                        Integer.parseInt(split[2]), call);
            }
        } else if (split[1].equals("CONNECT")) {
            int callId = Integer.parseInt(split[2]);

            Port port = null;
            try {
            	int portId = Integer.parseInt(split[3]);
            	port = fritzBox.getConfiguredPort(portId);
				if (port == null) { // Fallback auf statisch konfigurierte Ports
					port = Port.getPort(portId);
				}
            } catch (NumberFormatException nfe)
            {
            	port = new Port(0, "", "-1", "-1");
            }
            if (split[4].equals("")) { //$NON-NLS-1$
                number = messages.getMessage("unknown"); //$NON-NLS-1$
            } else
                number = split[4];
            if (number.endsWith("#")) //$NON-NLS-1$
                number = number.substring(0, number.length() - 1);

            Call call = JFritz.getCallMonitorList().getCall(callId);
            PhoneNumberOld pn = new PhoneNumberOld(this.properties, number, false);
            if (pn.getIntNumber().equals(call.getPhoneNumber().getIntNumber())
        		|| pn.getIntNumber().equals(properties.getProperty("dial.prefix")+call.getPhoneNumber().getIntNumber())) {
                try {
                    if (JFritz.getCallMonitorList().getCall(callId) != null) {
                        JFritz.getCallMonitorList().getCall(callId)
                                .setCalldate(
                                        new SimpleDateFormat(
                                                "dd.MM.yy HH:mm:ss")
                                                .parse(split[0]));
                        JFritz.getCallMonitorList().getCall(callId).setPort(
                                port);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                JFritz.getCallMonitorList().establishCall(callId);
            }
        }
    }

	public boolean isConnected() {
		return connected;
	}
}
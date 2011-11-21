package de.moonflower.jfritz.callmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class FBoxCallMonitorV3 extends FBoxCallMonitor {
	private Logger log = Logger.getLogger(FBoxCallMonitorV3.class);
	private final static String DELIMITER = ";";

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();
	protected MonitoredCalls monitoredCalls = JFritz.getCallMonitorList();

	public FBoxCallMonitorV3(FritzBox fritzBox,
							 Vector<CallMonitorStatusListener> listener,
							 boolean shouldConnect) {
		super(fritzBox, listener, shouldConnect);
		log.info("FBoxListener V3"); //$NON-NLS-1$
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

    public void parseOutput(final String line) {
    	if ( line != null )
    	{
	        String[] split = splitLine(line);

	        String callAction = split[1];
	        if (shouldMonitorIncomingCalls()
	                && "RING".equals(callAction)) { //$NON-NLS-1$
	            parseRing(split);
	        } else if (shouldMonitorOutgoingCalls()
	                && "CALL".equals(callAction)) { //$NON-NLS-1$
	            parseCall(split);
	        } else if (shouldMonitorCalls()
	        		&& "DISCONNECT".equals(callAction)) { //$NON-NLS-1$
	            parseDisconnect(split);
	        } else if (shouldMonitorCalls()
	        		&& "CONNECT".equals(callAction)) { //$NON-NLS-1$
	            parseConnect(split);
	        }
    	}
    }

	private String[] splitLine(final String line) {
		String[] split = line.split(DELIMITER, 7); //$NON-NLS-1$
		if (log.isDebugEnabled()) {
		    log.debug("Server: " + line); //$NON-NLS-1$
		    for (int i = 0; i < split.length; i++) {
		        log.debug("Split[" + i + "] = " + split[i]); //$NON-NLS-1$,  //$NON-NLS-2$
		    }
		}
		return split;
	}

    private boolean shouldMonitorCalls() {
    	return shouldMonitorIncomingCalls() || shouldMonitorOutgoingCalls();
    }

	private boolean shouldMonitorOutgoingCalls() {
        boolean monitortableOutgoingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.monitorTableOutgoingCalls"));
        boolean popupOutgoingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.popupOutgoingCalls"));
        return monitortableOutgoingCalls || popupOutgoingCalls;
	}

	private boolean shouldMonitorIncomingCalls() {
        boolean monitortableIncomingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.monitorTableIncomingCalls"));
        boolean popupIncomingCalls = JFritzUtils.parseBoolean(properties.getProperty("option.callmonitor.popupIncomingCalls"));
        return monitortableIncomingCalls || popupIncomingCalls;
	}

	private void parseRing(final String[] split) {
		String dateStr = split[0];
		String callIdStr = split[2];
		String portStr = null; // no port available vor RING event, set to null
		String numberStr = parseNumber(split[3]);
		String msn = split[4];
		String line = split[5];

		createCall(CallType.CALLIN, dateStr, callIdStr, portStr, numberStr, msn, line);
	}

	private void parseCall(final String[] split) {
		String dateStr = split[0];
		String callIdStr = split[2];
		String portStr = split[3];
		String msn = split[4];
		String numberStr = parseNumber(split[5]);
		String line = split[6];

		createCall(CallType.CALLOUT, dateStr, callIdStr, portStr, numberStr, msn, line);
	}

	private void createCall(CallType callType, String dateStr, String callIdStr, String portStr,
			String numberStr, String msn, String line) {
		boolean parseDialOut = false;

		if (callType == CallType.CALLOUT) {
			parseDialOut = properties.getProperty("option.activateDialPrefix").toLowerCase().equals("true");
		}

		try {
			Date date = parseDate(dateStr);
			String provider = parseProvider(line, msn);
			int callId = Integer.parseInt(callIdStr);
			PhoneNumberOld phoneNumber = new PhoneNumberOld(numberStr, parseDialOut);
			Port port = parsePort(portStr);

			Call currentCall = new Call(callType, date, phoneNumber, port, provider, 0);
			monitoredCalls.addNewCall(callId, currentCall);
		} catch (ParseException e) {
		    log.error("Could not convert call", e);
		}
	}

	private Port parsePort(final String portStr) {
		Port port = new Port(0, "", "-1", "-1");
		if (portStr != null) {
			try {
				int portId = Integer.parseInt(portStr);
				port = fritzBox.getConfiguredPort(portId);
			} catch (NumberFormatException nfe) {
				log.warn("Could not parse port id", nfe);
			}
		}
		return port;
	}

	private void parseDisconnect(final String[] split) {
	    int callId = Integer.parseInt(split[2]);
	    int duration = Integer.parseInt(split[3]);
	    Call call = monitoredCalls.getCall(callId);
	    if (call != null) {
	        call.setDuration(duration);
	        monitoredCalls.removeCall(callId, call);
	    }
	}

	private void parseConnect(final String[] split) {
		String dateStr = split[0];
		String callIdStr = split[2];
		String portStr = split[3];
		String numberStr = parseNumber(split[4]);

		int callId = Integer.parseInt(callIdStr);
		Port port = parsePort(portStr);

		Call call = monitoredCalls.getCall(callId);
		PhoneNumberOld number = new PhoneNumberOld(numberStr, false);
		if ( call != null ) {
			if (number.getIntNumber().equals(call.getPhoneNumber().getIntNumber())
				|| number.getIntNumber().equals(properties.getProperty("dial.prefix")+call.getPhoneNumber().getIntNumber())) {
					try {
						call.setPort(port);
						Date date = parseDate(dateStr);
						call.setCalldate(date);
					} catch (ParseException e) {
					    log.error("Could not convert call", e);
					}
					monitoredCalls.establishCall(callId);
			}
		}
	}


	private String parseProvider(final String cLine, final String msn) {
		String line = cLine;
		String provider = "";

		// Entferne das unnötige ; am Ende von SIPX;
		if (line.endsWith(DELIMITER))
		    line = line.substring(0, line.length() - 1);

		if ("POTS".equals(line)) { //$NON-NLS-1$
		    if ("".equals(msn)) { //$NON-NLS-1$
		        provider = messages.getMessage("fixed_network"); //$NON-NLS-1$
		    } else {
		        provider = msn;
		    }
		} else if (line.startsWith("SIP")) { //$NON-NLS-1$
		    try {
		    	int id = Integer.parseInt(line.substring(3));
		    	if ((fritzBox != null)
		    		&& (fritzBox.getSipProvider(id) != null))
		    	{
		    		provider = fritzBox.getSipProvider(id).toString();
		    	}
		    } catch (NumberFormatException nfe)
		    {
		    	provider = line;
		    }
		} else if ("ISDN".equals(line)) { //$NON-NLS-1$
		    provider = msn;
		} else {
		    provider = msn;
		}
		return provider;
	}

	private Date parseDate(String dateStr) throws ParseException {
		return new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(dateStr);
	}

	private String parseNumber(final String input) {
		String number;
		if ("".equals(input)) { //$NON-NLS-1$
		    number = messages.getMessage("unknown"); //$NON-NLS-1$
		} else {
		    number = input;
		}

		if (number.endsWith("#")) { //$NON-NLS-1$
		    number = number.substring(0, number.length() - 1);
		}
		return number;
	}
}
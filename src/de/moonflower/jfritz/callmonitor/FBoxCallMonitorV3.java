package de.moonflower.jfritz.callmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public class FBoxCallMonitorV3 extends FBoxCallMonitor {

	public FBoxCallMonitorV3(FritzBox fritzBox,
							 Vector<CallMonitorStatusListener> listener) {
		super(fritzBox, listener);
		Debug.info("FBoxListener V3"); //$NON-NLS-1$
	}

    public void run() {
    	while (this.isRunning())
    	{
    		if (!this.isConnected()) {
    			connect();
    		}
    		else
    		{
    			readOutput();
    		}
    	}
    }

    public void parseOutput(String line) {
        parseOutput(line, true);
    }

    public void parseOutput(String line, boolean interactWithJFritz) {
    	if ( line != null )
    	{
	        Debug.debug("Server: " + line); //$NON-NLS-1$
	        String number = ""; //$NON-NLS-1$
	        String provider = ""; //$NON-NLS-1$
	        String[] split;
	        split = line.split(";", 7); //$NON-NLS-1$
	        for (int i = 0; i < split.length; i++) {
	            Debug.debug("Split[" + i + "] = " + split[i]); //$NON-NLS-1$,  //$NON-NLS-2$
	        }
	        if (JFritzUtils.parseBoolean(Main.getProperty(
	                "option.callmonitor.monitorIncomingCalls")) //$NON-NLS-1$,  //$NON-NLS-2$
	                && split[1].equals("RING")) { //$NON-NLS-1$
	            if (split[3].equals("")) { //$NON-NLS-1$
	                number = Main.getMessage("unknown"); //$NON-NLS-1$
	            } else
	                number = split[3];
	            if (number.endsWith("#")) //$NON-NLS-1$
	                number = number.substring(0, number.length() - 1);

	            // Neues Ausgabeformat
	            if (split[5].equals("POTS")) { //$NON-NLS-1$
	                if (split[4].equals("")) { //$NON-NLS-1$
	                    provider = Main.getMessage("fixed_network"); //$NON-NLS-1$
	                } else {
	                    provider = split[4];
	                }
	            } else if (split[5].startsWith("SIP")) { //$NON-NLS-1$
	                try {
	                	int id = Integer.parseInt(split[5].substring(3));
	                	if ((fritzBox != null)
                    		&& (fritzBox.getSipProvider(id) != null))
                    	{
                    		provider = fritzBox.getSipProvider(id).toString();
                    	}
	                } catch (NumberFormatException nfe)
	                {
	                	provider = split[5];
	                }
	            } else if (split[5].equals("ISDN")) { //$NON-NLS-1$
	                provider = split[4];
	            } else
	                provider = split[4];

	            try {
	                Call currentCall = new Call(new CallType(CallType.CALLIN),
	                        new SimpleDateFormat("dd.MM.yy HH:mm:ss")
	                                .parse(split[0]), new PhoneNumberOld(number, false),
	                                new Port(0, "", "-1", "-1"),
	                                provider, 0);
	                JFritz.getCallMonitorList().addNewCall(
	                        Integer.parseInt(split[2]), currentCall);
	            } catch (ParseException e) {
	                e.printStackTrace();
	            }
	        } else if (JFritzUtils.parseBoolean(Main.getProperty(
	                "option.callmonitor.monitorOutgoingCalls")) //$NON-NLS-1$,  //$NON-NLS-2$
	                && split[1].equals("CALL")) { //$NON-NLS-1$

	            if (split[5].equals("")) { //$NON-NLS-1$
	                number = Main.getMessage("unknown"); //$NON-NLS-1$
	            } else
	                number = split[5];
	            if (number.endsWith("#")) //$NON-NLS-1$
	                number = number.substring(0, number.length() - 1);

	            // Entferne das unnötige ; am Ende von SIPX;
	            if (split[6].endsWith(";"))
	                split[6] = split[6].substring(0, split[6].length() - 1);

	            // Neues Ausgabeformat
	            if (split[6].equals("POTS")) { //$NON-NLS-1$
	                if (split[4].equals("")) { //$NON-NLS-1$
	                    provider = Main.getMessage("fixed_network"); //$NON-NLS-1$
	                } else {
	                    provider = split[4];
	                }
	            } else if (split[6].startsWith("SIP")) { //$NON-NLS-1$
	                try {
	                	int id = Integer.parseInt(split[5].substring(3));
	                	if ((fritzBox != null)
                    		&& (fritzBox.getSipProvider(id) != null))
                    	{
                    		provider = fritzBox.getSipProvider(id).toString();
                    	}
	                } catch (NumberFormatException nfe)
	                {
	                	provider = split[5];
	                }
	            } else if (split[6].equals("ISDN")) { //$NON-NLS-1$
	                provider = split[4];
	            } else
	                provider = split[4];

	            Port port = null;
	            try {
	            	int portId = Integer.parseInt(split[3]);
	            	port = fritzBox.getConfiguredPort(portId);
	            } catch (NumberFormatException nfe)
	            {
	            	port = new Port(0, "", "-1", "-1");
	            }
	            try {
	                Call currentCall = new Call(new CallType(CallType.CALLOUT),
	                        new SimpleDateFormat("dd.MM.yy HH:mm:ss")
	                                .parse(split[0]),
	                                new PhoneNumberOld(number, Main.getProperty("option.activateDialPrefix")
	                                		.toLowerCase().equals("true")),
	                                port, provider, 0);
	                JFritz.getCallMonitorList().addNewCall(
	                        Integer.parseInt(split[2]), currentCall);
	            } catch (ParseException e) {
	                Debug.error("FBoxListenerV3: Could not convert call" + e);
	            }

	        } else if (split[1].equals("DISCONNECT")) { //$NON-NLS-1$
	            try {
	                int callId = Integer.parseInt(split[2]);
	                Call call = JFritz.getCallMonitorList().getCall(callId);
	                if (call != null) {
	                    call.setDuration(Integer.parseInt(split[3]));
	                    JFritz.getCallMonitorList().removeCall(
	                            Integer.parseInt(split[2]), call);
	                    Thread.sleep(zufallszahl.nextInt(3000));
	                }
	            } catch (InterruptedException e) {
	                Debug.error(e.toString());
		        	Thread.currentThread().interrupt();
	            }

	        } else if (split[1].equals("CONNECT")) {
	            int callId = Integer.parseInt(split[2]);
	            Port port = null;
	            try {
	            	int portId = Integer.parseInt(split[3]);
	            	port = fritzBox.getConfiguredPort(portId);
	            } catch (NumberFormatException nfe)
	            {
	            	port = new Port(0, "", "-1", "-1");
	            }
	            if (split[4].equals("")) { //$NON-NLS-1$
	                number = Main.getMessage("unknown"); //$NON-NLS-1$
	            } else
	                number = split[4];
	            if (number.endsWith("#")) //$NON-NLS-1$
	                number = number.substring(0, number.length() - 1);

	            Call call = JFritz.getCallMonitorList().getCall(callId);
	            PhoneNumberOld pn = new PhoneNumberOld(number, false);
	            if ( call != null ) {
	            	if (pn.getIntNumber().equals(call.getPhoneNumber().getIntNumber())
	            		|| pn.getIntNumber().equals(Main.getProperty("dial.prefix")+call.getPhoneNumber().getIntNumber())) {
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
    	}
    }
}
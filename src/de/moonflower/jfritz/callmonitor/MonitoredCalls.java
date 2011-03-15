/*
 * Created on 10.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

/**
 * Diese Klasse enthält eine Liste aller initialiesierter und etablierter
 * Anrufe. Sie wird von den Anrufmonitoren verwendet, um Anrufe anzuzeigen.
 *
 * @author Robert Palmer
 */

public class MonitoredCalls {
	private final static Logger log = Logger.getLogger(MonitoredCalls.class);

    // MSN, die von dem Anrufmonitor ignoriert werden
    protected String[] ignoredMSNs;

    // Dieser Vektor enthält alle Klassen, die auf Anruf-Events reagieren
    // sollen.
    private Vector<CallMonitorListener> listeners = new Vector<CallMonitorListener>();

    // Ankommende oder abgehende Anrufe, bei denen noch keine Verbindung
    // zustandegekommen ist
    private HashMap<Integer, Call> pendingCalls = new HashMap<Integer, Call>();

    // Anrufe, bei denen schon eine Verbindung besteht
    private HashMap<Integer, Call> establishedCalls = new HashMap<Integer, Call>();

    protected PropertyProvider properties = PropertyProvider.getInstance();

    /**
     * Fügt einen Anruf in die Liste der "schwebenden" Anrufe ein
     *
     * @param id, id des Anrufs
     * @param call, der Anruf ansich
     */
    public void addNewCall(int id, Call call) {
        log.debug("Used Provider: " + call.getRoute());
        initIgnoreList();
        boolean ignoreIt = false;
        for (int i = 0; i < ignoredMSNs.length; i++) {
            if (!ignoredMSNs[i].equals(""))
                if (call.getRoute()
                        .equals(ignoredMSNs[i])) {
                    log.debug("Ignoring call because MSN " + ignoredMSNs[i] + " is on ignore list.");
                    ignoreIt = true;
                    break;
                }
        }
        if (!ignoreIt) {
            log.info("Adding new call[" + id + "]: " + call.getPhoneNumber());
            pendingCalls.put(Integer.valueOf(id), call);
            if (call.getCalltype() == CallType.CALLIN) {
                invokeIncomingCall(call);
            } else if (call.getCalltype() == CallType.CALLOUT) {
                invokeOutgoingCall(call);
            }
        }
    }

    /**
     * Transferiert den Anruf von der Liste der "schwebenden" Anrufe in die
     * Liste der etablierten Anrufe
     *
     * @param id, call id
     */
    public void establishCall(int id) {
        Integer callID = Integer.valueOf(id);
        if (pendingCalls.keySet().contains(callID)) {
            log.info("Establishing call["+id+"]");
            establishedCalls.put(callID, pendingCalls.get(Integer.valueOf(id)));
            pendingCalls.remove(callID);
            Call call = establishedCalls.get(callID);

            //notify the listeners of an established call
            if(call.getCalltype() == CallType.CALLIN)
            	this.invokeIncomingCallEstablished(call);
            else
            	this.invokeOutgoingCallEstablished(call);
        }
    }

    /**
     * Entfernt den Anruf aus einer der beiden Listen (pending und established)
     *
     * @param id, id des Anrufs
     */
    public void removeCall(int id, Call call) {
        log.info("Removing call["+id+"]");
        Integer intID = Integer.valueOf(id);
        if (pendingCalls.keySet().contains(intID)) {
            pendingCalls.remove(intID);
            // Setze Type auf FAILED, da kein Anruf zustandegekommen ist
            call.setCallType(CallType.CALLIN_FAILED);
        } else if (establishedCalls.keySet().contains(intID)) {
            establishedCalls.remove(intID);
        }
        if ( call != null ) {
            invokeDisconnectCall(call);
        }
    }

    /**
     * Liefert den Status des Anrufs zurück (pending, established, none)
     *
     * @param id, id des Anrufs
     */
    public CallState getCallState(int id) {
        if (pendingCalls.keySet().contains(Integer.valueOf(id))) {
            return CallState.PENDING;
        } else if (establishedCalls.keySet().contains(Integer.valueOf(id))) {
            return CallState.ESTABLISHED;
        } else
            return CallState.NONE;
    }

    /**
     * Liefert die Daten des Anrufs zu einer Anrufid
     *
     * @param id, id des Anrufs
     */
    public Call getCall(int id) {
    	CallState callState = getCallState(id);
        if (callState == CallState.PENDING) {
            return pendingCalls.get(Integer.valueOf(id));
        } else if (callState == CallState.ESTABLISHED) {
            return establishedCalls.get(Integer.valueOf(id));
        } else {
            return null;
        }
    }

    /**
     * Anzahl "schwebender" Anrufe
     */
    public int getPendingSize() {
        return pendingCalls.size();
    }

    /**
     * Anzahl der etablierten Anrufe
     */
    public int getEstablishedSize() {
        return establishedCalls.size();
    }

    /**
     * Fügt einen Listener hinzu
     *
     * @param cml, new CallMonitorListener
     */
    public void addCallMonitorListener(CallMonitorListener cml) {
        log.info("Added new event listener " + cml.toString());
        listeners.add(cml);
    }

    /**
     * Entfernt einen Listener aus der Liste
     *
     * @param cml, CallMonitorListener to remove
     */
    public void removeCallMonitorListener(CallMonitorListener cml) {
        log.info("Removing event listener " + cml.toString());
        listeners.remove(cml);
    }

    /**
     * Meldet einen etablierten ankommenden Anruf an alle registrierten Listener
     *
     * @param call
     */
    public void invokeIncomingCallEstablished(Call call) {
        log.info("Invoking incoming call established");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).establishedCallIn(call);
        }
    }

    /**
     * Meldet einen etablierten ausgehenden Anruf an alle registrierten Listener
     *
     * @param call
     */
    public void invokeOutgoingCallEstablished(Call call) {
        log.info("Invoking outgoing call established");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).establishedCallOut(call);
        }
    }


    /**
     * Meldet einen ankommenden Anruf an alle registrierten Listener
     *
     * @param call
     */
    public void invokeIncomingCall(Call call) {
        log.info("Invoking incoming call");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).pendingCallIn(call);
        }
    }

    /**
     * Meldet einen ausgehenden Anruf an alle registrierten Listener
     *
     * @param call
     */
    public void invokeOutgoingCall(Call call) {
    	log.info("Invoking outgoing call");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).pendingCallOut( call);
        }
    }

    /**
     * Meldet den Verbindungsabbau an alle registrierten Listener
     *
     * @param call
     */
    public void invokeDisconnectCall(Call call) {
       log.info("Invoking disconnect call");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).endOfCall(call);
        }
    }

    /**
     * Initialisiert die Liste aller zu ignorierenden MSNs
     */
    protected void initIgnoreList() {
        String ignoreMSNString = properties.getProperty(
                "option.callmonitor.ignoreMSN"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (ignoreMSNString.length() > 0 && ignoreMSNString.indexOf(";") == -1) { //$NON-NLS-1$
            ignoreMSNString = ignoreMSNString + ";"; //$NON-NLS-1$
        }
        ignoredMSNs = ignoreMSNString.split(";"); //$NON-NLS-1$
        if (ignoredMSNs.length > 0 && !"".equals(ignoredMSNs[0])) {
        	log.debug("Ignored MSNs: "); //$NON-NLS-1$
        	for (int i = 0; i < ignoredMSNs.length; i++) {
        		log.debug(ignoredMSNs[i]);
        	}
        }
    }
}

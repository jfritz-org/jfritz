/*
 * Created on 10.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import java.util.HashMap;
import java.util.Vector;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.utils.Debug;

/**
 * Diese Klasse enthält eine Liste aller initialiesierter und etablierter
 * Anrufe. Sie wird von den Anrufmonitoren verwendet, um Anrufe anzuzeigen.
 *
 * @author Robert Palmer
 *
 */

// TODO: ignore MSNs
/**
 **/
public class CallMonitorList {

    public final static int PENDING = 0;

    public final static int ESTABLISHED = 1;

    public final static int NONE = 2;

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

    /**
     * Fügt den Anruf call in die Liste der "schwebenden" Anrufe ein
     *
     * @param id,
     *            id des anrufs
     * @param call,
     *            der Anruf ansich
     */
    public void addNewCall(int id, Call call) {
        Debug.debug("Used Provider: " + call.getRoute());
        Debug.debug("Ignored MSNs: ");
        initIgnoreList();
        boolean ignoreIt = false;
        for (int i = 0; i < ignoredMSNs.length; i++) {
            if (!ignoredMSNs[i].equals(""))
                if (call.getRoute()
                        .equals(ignoredMSNs[i])) {
                    Debug.debug("Ignoring call because MSN " + ignoredMSNs[i] + " is on ignore list.");
                    ignoreIt = true;
                    break;
                }
        }
        if (!ignoreIt) {
            Debug.info("CallMonitorList: Adding new call");
            pendingCalls.put(Integer.valueOf(id), call);
            if (call.getCalltype().toInt() == CallType.CALLIN) {
                invokeIncomingCall(call);
            } else if (call.getCalltype().toInt() == CallType.CALLOUT) {
                invokeOutgoingCall(call);
            }
        }
    }

    /**
     * Transferiert den Anruf von der Liste der "schwebenden" Anrufe in die
     * Liste der etablierten Anrufe
     *
     * @param id,
     *            call id
     */
    public void establishCall(int id) {
        Integer callID = Integer.valueOf(id);
        if (pendingCalls.keySet().contains(callID)) {
            Debug.info("CallMonitorList: Establishing call");
            establishedCalls.put(callID, pendingCalls.get(Integer.valueOf(id)));
            pendingCalls.remove(callID);
            Call call = establishedCalls.get(callID);

            //notify the listeners of an established call
            if(call.getCalltype().equals(CallType.CALLIN_STR))
            	this.invokeIncomingCallEstablished(call);
            else
            	this.invokeOutgoingCallEstablished(call);
        }
    }

    /**
     * Entfernt den Anruf aus einer der beiden Listen (pending und established)
     *
     * @param id,
     *            id des Anrufs
     */
    public void removeCall(int id, Call call) {
        Debug.info("CallMonitorList: Removing call");
        Integer intID = Integer.valueOf(id);
        if (pendingCalls.keySet().contains(intID)) {
            pendingCalls.remove(intID);
            // Setze Type auf FAILED, da kein Anruf zustandegekommen ist
            call.setCallType(new CallType(CallType.CALLIN_FAILED));
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
     * @param id,
     *            id des Anrufs
     */
    public int getCallState(int id) {
        if (pendingCalls.keySet().contains(Integer.valueOf(id))) {
            return PENDING;
        } else if (establishedCalls.keySet().contains(Integer.valueOf(id))) {
            return ESTABLISHED;
        } else
            return NONE;
    }

    /**
     * Liefert die Daten des Anrufs
     *
     * @param id,
     *            id des Anrufs
     */
    public Call getCall(int id) {
        if (getCallState(id) == PENDING) {
            return pendingCalls.get(Integer.valueOf(id));
        } else if (getCallState(id) == ESTABLISHED) {
            return establishedCalls.get(Integer.valueOf(id));
        } else
            return null;
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
     * Adds a new listener to listener vector
     *
     * @param cml,
     *            new CallMonitorListener
     */
    public void addCallMonitorListener(CallMonitorListener cml) {
        Debug.info("CallMonitorList: Added new event listener " + cml.toString());
        listeners.add(cml);
    }

    /**
     * Removes a listener from listener vector
     *
     * @param cml,
     *            CallMonitorListener to remove
     */
    public void removeCallMonitorListener(CallMonitorListener cml) {
        Debug.info("CallMonitorList: Removing event listener " + cml.toString());
        listeners.remove(cml);
    }

    /**
     * Throw incoming call event for listeners
     *
     * @param call
     */
    public void invokeIncomingCallEstablished(Call call) {
        Debug.info("CallMonitorList: Invoking incoming call established");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).establishedCallIn(call);
        }
    }

    /**
     * Throw outgoing call event for listeners
     *
     * @param call
     */
    public void invokeOutgoingCallEstablished(Call call) {
        Debug.info("CallMonitorList: Invoking outgoing call established");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).establishedCallOut(call);
        }
    }


    /**
     * Throw incoming call event for listeners
     *
     * @param call
     */
    public void invokeIncomingCall(Call call) {
        Debug.info("CallMonitorList: Invoking incoming call");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).pendingCallIn(call);
        }
    }

    /**
     * Throw outgoing call event for listeners
     *
     * @param call
     */
    public void invokeOutgoingCall(Call call) {
        Debug.info("CallMonitorList: Invoking outgoing call");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).pendingCallOut( call);
        }
    }

    /**
     * Throw disconnect call event for listeners
     *
     * @param call
     */
    public void invokeDisconnectCall(Call call) {
       Debug.info("CallMonitorList: Invoking disconnect call");
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).endOfCall(call);
        }
    }

    protected void initIgnoreList() {
        String ignoreMSNString = Main.getProperty(
                "option.callmonitor.ignoreMSN"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (ignoreMSNString.length() > 0 && ignoreMSNString.indexOf(";") == -1) { //$NON-NLS-1$
            ignoreMSNString = ignoreMSNString + ";"; //$NON-NLS-1$
        }
        ignoredMSNs = ignoreMSNString.split(";"); //$NON-NLS-1$
        Debug.debug("Ignored MSNs: "); //$NON-NLS-1$
        for (int i = 0; i < ignoredMSNs.length; i++) {
            Debug.debug(ignoredMSNs[i]);
        }
    }
}

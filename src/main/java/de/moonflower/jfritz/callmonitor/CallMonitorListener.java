/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import de.moonflower.jfritz.struct.Call;

/**
 * Dieses Interface dient als Schnittstelle für alle Anrufmonitor-Events
 * @author Robert Palmer
 *
 */
public interface CallMonitorListener {

    public void pendingCallIn(Call call);

    public void establishedCallIn(Call call);

    public void pendingCallOut(Call call);

    public void establishedCallOut(Call call);

    public void endOfCall(Call call);
}

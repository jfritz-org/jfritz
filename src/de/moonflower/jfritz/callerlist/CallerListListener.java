package de.moonflower.jfritz.callerlist;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;
/**
 * This interface is used to inform the network subsystem that data has changed
 * usually because a user has added or removed entries.
 *
 * @author brian
 *
 */
public interface CallerListListener {

	public void callsAdded(Vector<Call> newCalls);

	public void callsUpdated(Call original, Call update);

	public void callsRemoved(Vector<Call> callsRemoved);

}

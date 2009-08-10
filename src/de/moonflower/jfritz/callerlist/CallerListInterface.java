package de.moonflower.jfritz.callerlist;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public interface CallerListInterface {

	public Call getFilteredCall(int index);
	public Call getUnfilteredCall(int index);
	public void addEntries(Vector<Call> newCalls);
	public void updateEntry(Call oldCall, Call newCall);
	public void removeEntries(Vector<Call> removeCalls);
}

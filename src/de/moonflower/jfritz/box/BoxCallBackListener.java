package de.moonflower.jfritz.box;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public interface BoxCallBackListener {

	public abstract boolean finishGetCallerList(Vector<Call> calls);
}

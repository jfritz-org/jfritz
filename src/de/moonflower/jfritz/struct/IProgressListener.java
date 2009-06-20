package de.moonflower.jfritz.struct;

import java.util.Vector;

public interface IProgressListener {
	public void setMin(int min);
	public void setMax(int max);
	public void setProgress(int progress);
	public void finished(Vector<Call> newCalls);
}

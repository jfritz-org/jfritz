package de.moonflower.jfritz.box;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;

public interface BoxCallListInterface {
	public Vector<Call> getCallerList(Vector<IProgressListener> callListProgressListener)
			throws IOException, MalformedURLException;

	public void clearCallerList();
}

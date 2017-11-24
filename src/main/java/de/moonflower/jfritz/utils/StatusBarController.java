package de.moonflower.jfritz.utils;

import java.util.Iterator;
import java.util.Vector;


public class StatusBarController {
	private Vector<StatusListener> statusBarListeners = new Vector<StatusListener>();

	public void fireStatusChanged(Object status) {
		for (Iterator<StatusListener> iter = statusBarListeners.iterator(); iter.hasNext();) {
			StatusListener element = (StatusListener) iter.next();
			element.statusChanged(status);
		}
	}

	public void addStatusBarListener(StatusListener listener){
		statusBarListeners.add(listener);
	}
	public void removeStatusBarListener(StatusListener listener){
		statusBarListeners.remove(listener);
	}

}

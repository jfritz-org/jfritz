package de.moonflower.jfritz.box;

import java.util.Vector;

import de.moonflower.jfritz.dialogs.sip.SipProvider;

public interface BoxSipInterface {
	public void detectSipProvider();

	public Vector<SipProvider> getSipProvider();
}

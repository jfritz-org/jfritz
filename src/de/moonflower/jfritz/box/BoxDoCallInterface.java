package de.moonflower.jfritz.box;

import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.Port;

public interface BoxDoCallInterface {
	public void doCall(PhoneNumber number, Port port);
	public void hangup(Port port);
}

package de.moonflower.jfritz.box;

import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;

public interface BoxDoCallInterface {
	public void doCall(PhoneNumberOld number, Port port);
	public void hangup(Port port);
}

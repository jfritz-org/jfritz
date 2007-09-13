package de.moonflower.jfritz.JFritzEvent.events;

import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventParameter;

public interface JFritzEvent {

	public String getName();

	public int getParameterCount();

	public JFritzEventParameter getParameter(byte i);

	public String toString();

}

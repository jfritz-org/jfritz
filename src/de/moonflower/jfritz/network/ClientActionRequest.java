package de.moonflower.jfritz.network;

import java.io.Serializable;

public class ClientActionRequest implements Serializable {

	public static final long serialVersionUID = 100;

	public boolean doLookup = false;

	public boolean getCallList = false;

}

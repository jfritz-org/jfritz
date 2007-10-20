package de.moonflower.jfritz.network;

import java.io.Serializable;

/**
 * used to make the ask the server to do something
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author brian
 *
 */

public class ClientActionRequest implements Serializable {

	public static final long serialVersionUID = 100;

	public boolean doLookup = false;

	public boolean getCallList = false;

}

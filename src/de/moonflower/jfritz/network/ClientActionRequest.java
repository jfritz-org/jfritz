package de.moonflower.jfritz.network;

import java.io.Serializable;

import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * used to ask the server to do something for a client
 *
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author Brian
 *
 */

public class ClientActionRequest implements Serializable {

	public static final long serialVersionUID = 100;

	public boolean doLookup = false;

	public boolean getCallList = false;

	public String siteName;

	public PhoneNumber number;

}

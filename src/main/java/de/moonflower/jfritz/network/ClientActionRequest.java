package de.moonflower.jfritz.network;

import java.io.Serializable;

import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;

/**
 * This class is used to ask the server to perfrom an external action.
 * Because the client doesn't know his permissions with server,
 * it is not guranteed this action will be carried out.
 *
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author Brian
 *
 */

public class ClientActionRequest implements Serializable {

	public static final long serialVersionUID = 101;

	public enum ActionType {doLookup, getCallList, deleteListFromBox, doCall, hangup};

	public ActionType action;

	public Port port;

	public String siteName;

	public PhoneNumberOld number;

}

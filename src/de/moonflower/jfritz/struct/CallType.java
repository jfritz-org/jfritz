/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz.struct;

import de.moonflower.jfritz.Main;


/**
 * This class defines the call types "Call In", "Call In Failed" and "Call out".
 * Three icons are associated with the call types.
 *
 * @author Arno Willig
 */
public class CallType {
	public static final byte CALLIN = 1;
	public static final byte CALLIN_FAILED = 2;
	public static final byte CALLOUT = 3;
	public static final String CALLIN_STR = "call_in"; //$NON-NLS-1$
	public static final String CALLIN_FAILED_STR = "call_in_failed"; //$NON-NLS-1$
	public static final String CALLOUT_STR = "call_out"; //$NON-NLS-1$

	public byte calltype;

	public CallType(byte type) {
		calltype = type;
	}

	public CallType(String type) {
		if (type.equals(CALLIN_STR))
			calltype = CALLIN;
		else if (type.equals(CALLIN_FAILED_STR))
			calltype = CALLIN_FAILED;
		else if (type.equals(CALLOUT_STR))
			calltype = CALLOUT;
	}

	public int toInt() {
		return calltype;
	}

	public int getCallType(){
		return calltype;
	}

	public String toDescription() {
		if (calltype == CALLIN)
			return Main.getMessage("incoming_call"); //$NON-NLS-1$
		else if (calltype == CALLIN_FAILED)
			return Main.getMessage("missed_call"); //$NON-NLS-1$
		else
			return Main.getMessage("outgoing_call"); //$NON-NLS-1$
	}

	public String toString() {
		if (calltype == CALLIN)
			return CALLIN_STR;
		else if (calltype == CALLIN_FAILED)
			return CALLIN_FAILED_STR;
		else
			return CALLOUT_STR;
	}
}

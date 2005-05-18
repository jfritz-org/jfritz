/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz;


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
	public static final String CALLIN_STR = "call_in";
	public static final String CALLIN_FAILED_STR = "call_in_failed";
	public static final String CALLOUT_STR = "call_out";

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

	public String toDescription() { // TODO: I18N
		if (calltype == CALLIN)
			return "Eingehender Anruf";
		else if (calltype == CALLIN_FAILED)
			return "Eingehender Anruf in Abwesenheit";
		else
			return "Ausgehender Anruf";
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

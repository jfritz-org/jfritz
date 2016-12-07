package de.moonflower.jfritz.struct;

import de.moonflower.jfritz.messages.MessageProvider;

/**
 * @author Robert
 */
public enum CallType {

	CALLIN("call_in"),
	CALLIN_FAILED("call_in_failed"),
	CALLIN_BLOCKED("call_in_blocked"),
	CALLOUT("call_out");

	private static final String CALLIN_STR = "call_in";
	private static final String CALLIN_FAILED_STR = "call_in_failed";
	private static final String CALLIN_BLOCKED_STR = "call_in_blocked";
	private static final String CALLOUT_STR = "call_out";


	protected MessageProvider messages = MessageProvider.getInstance();
	private String asString;

	private CallType(final String s) {
		this.asString = s;
	}

	public String toString() {
		return this.asString;
	}

	public String toDescription() {
		if (this == CALLIN) {
			return messages.getMessage("incoming_call"); //$NON-NLS-1$
		} else if (this == CALLIN_FAILED) {
			return messages.getMessage("missed_call"); //$NON-NLS-1$
		} else if (this == CALLIN_BLOCKED) {
			return messages.getMessage("blocked_call"); //$NON-NLS-1$
		} else {
			return messages.getMessage("outgoing_call"); //$NON-NLS-1$
		}
	}

	public static CallType getByString(final String input) {
		if (CALLIN_STR.equals(input)) {
			return CALLIN;
		} else if (CALLIN_FAILED_STR.equals(input)) {
			return CALLIN_FAILED;
		} else if (CALLIN_BLOCKED_STR.equals(input)) {
			return CALLIN_BLOCKED;
		} else if (CALLOUT_STR.equals(input)) {
			return CALLOUT;
		} else {
			return CALLIN;
		}
	}
}

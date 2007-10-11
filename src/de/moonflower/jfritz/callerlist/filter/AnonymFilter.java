package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class AnonymFilter extends CallFilter {

	private static final String type = FILTER_ANONYM;

	public boolean passInternFilter(Call currentCall) {

		if (currentCall.getPhoneNumber() == null)
			return true;
		return false;
	}
}

package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class AnonymFilter extends CallFilter {

	public boolean passInternFilter(Call currentCall) {

		if (currentCall.getPhoneNumber() == null)
			return true;
		return false;
	}
}

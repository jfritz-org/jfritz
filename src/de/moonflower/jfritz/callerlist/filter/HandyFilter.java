package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class HandyFilter extends CallFilter {

	public HandyFilter() {
	}

	public boolean passInternFilter(Call currentCall) {
		if (currentCall.getPhoneNumber() != null
				&& currentCall.getPhoneNumber().isMobile())
			return true;

		return false;
	}
}

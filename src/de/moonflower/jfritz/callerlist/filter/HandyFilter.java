package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class HandyFilter extends CallFilter {

	public HandyFilter() {
	}

	public boolean passFilter(Call currentCall) {
		if (currentCall.getPhoneNumber() != null
				&& currentCall.getPhoneNumber().isMobile())
			return false;

		return true;
	}
}

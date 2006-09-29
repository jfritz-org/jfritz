package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class FixedFilter extends CallFilter {

	public boolean passFilter(Call currentCall) {
		if (currentCall.getPhoneNumber() != null
				&& !currentCall.getPhoneNumber().isMobile())
			return false;
		return true;
	}
}

package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class NoNumberFilter extends CallFilter {

	public boolean passFilter(Call currentCall) {

		if (currentCall.getPhoneNumber() == null)
			return false;
		return true;
	}
}

package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class FixedFilter extends CallFilter {

	private static final String type = FILTER_FIXED;

	public boolean passInternFilter(Call currentCall) {
		if (currentCall.getPhoneNumber() != null
				&& !currentCall.getPhoneNumber().isMobile())
			return true;
		return false;
	}

	public String getType(){
		return type;
	}

	public FixedFilter clone(){
		FixedFilter ff = new FixedFilter();
		ff.setEnabled(this.isEnabled());
		ff.setInvert(this.isInvert());
		return ff;
	}
}

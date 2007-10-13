package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

public class CallInFilter extends CallFilter {

	private static final String type = FILTER_CALLIN_NOTHING;

	public CallInFilter() {
	}

	public boolean passInternFilter(Call currentCall) {
		return (currentCall.getCalltype().toInt() == CallType.CALLIN);
	}

	public String getType(){
		return type;
	}

	public CallInFilter clone(){
		CallInFilter cif = new CallInFilter();
		cif.setEnabled(this.isEnabled());
		cif.setInvert(this.isInvert());
		return cif;
	}

}

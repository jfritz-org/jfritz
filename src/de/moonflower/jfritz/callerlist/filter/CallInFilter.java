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

}

package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

public class CallOutFilter extends CallFilter {

	private static final String type = FILTER_CALLOUT;

	public CallOutFilter() {
	}

	public boolean passInternFilter(Call currentCall) {
		return (currentCall.getCalltype().toInt() == CallType.CALLOUT);
	}

	public String getType(){
		return type;
	}
}

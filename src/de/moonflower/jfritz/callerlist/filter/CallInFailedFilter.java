package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

public class CallInFailedFilter extends CallFilter {

	private static final String type = FILTER_CALLINFAILED;

	public CallInFailedFilter() {
	}

	public boolean passInternFilter(Call currentCall) {
		return (currentCall.getCalltype().toInt() == CallType.CALLIN_FAILED);
	}

	public String getType(){
		return type;
	}
}

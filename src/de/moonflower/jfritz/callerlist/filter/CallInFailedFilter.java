package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

public class CallInFailedFilter extends CallFilter {

	public CallInFailedFilter() {
	}

	public boolean passFilter(Call currentCall) {
		return (currentCall.getCalltype().toInt() != CallType.CALLIN_FAILED);
	}
}

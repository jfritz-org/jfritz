package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class CommentFilter extends CallFilter {

	public boolean passInternFilter(Call currentCall) {

		if (currentCall.getComment().equals(""))
			return false;
		return true;
	}
}

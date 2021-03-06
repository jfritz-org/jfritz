package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class CommentFilter extends CallFilter {

	private static final String type = FILTER_COMMENT;

	public boolean passInternFilter(Call currentCall) {

		if (currentCall.getComment().equals(""))
			return false;
		return true;
	}

	public String getType(){
		return type;
	}

	public CommentFilter clone(){
		CommentFilter cf = new CommentFilter();
		cf.setEnabled(this.isEnabled());
		cf.setInvert(this.isInvert());
		return cf;
	}

}

package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public class SearchFilter extends CallFilter {
	String parts[];

	public SearchFilter(String s) {
		parts = s.toLowerCase().split(" "); // TODO change to
		// toLowercase(locale)
	}
	//TODO reguläre Ausdrücke zulassen

	public boolean passFilter(Call currentCall) {
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
//			if (part.length() == 0)
	//			return true;

			if (currentCall.getPhoneNumber() != null
					&& currentCall.getPhoneNumber().getIntNumber().contains(
							part))
				return true;
			if (currentCall.getPhoneNumber() != null
					&& currentCall.getPhoneNumber().getCallByCall().contains(
							part))
				return true;

			if (currentCall.getPerson() != null
					&& currentCall.getPerson().getFullname().toLowerCase()
					.contains(part))
				return true;

			if (currentCall.getComment() != null
					&& currentCall.getComment().toLowerCase().contains(part))
				return true;

			if (currentCall.getPhoneNumber() != null
					&& currentCall.getPhoneNumber().getAreaNumber() != null
					&& currentCall.getPhoneNumber().getAreaNumber().contains(
							part))
				return true;

		}
		return false;
	}
}

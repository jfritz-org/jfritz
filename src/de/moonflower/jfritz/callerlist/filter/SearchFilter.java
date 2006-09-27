package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;

public class SearchFilter extends CallFilter {
	SearchFilter(String name) {
		super("filter.search");
		// TODO Auto-generated constructor stub
	}
	public boolean passFilterIntern(Call currentCall) {
		String filterSearch = JFritz.getProperty("filter.search", ""); //$NON-NLS-1$,  //$NON-NLS-2$

		String parts[] = filterSearch.split(" "); //$NON-NLS-1$
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];

			if (part.length() > 0
					&& (currentCall.getPhoneNumber() == null ||
							currentCall.getPhoneNumber().getIntNumber().indexOf(parts[i]) == -1)
					&& (currentCall.getPhoneNumber() == null ||
							currentCall.getPhoneNumber().getCallByCall().indexOf(parts[i]) == -1)
					&& (currentCall.getPerson() == null ||
							currentCall.getPerson().getFullname().toLowerCase().indexOf(part.toLowerCase()) == -1))
			{
				return false;
			}
		}
		return true;
	}
	public void setFilter(int filter) {
		// TODO Auto-generated method stub

	}
}

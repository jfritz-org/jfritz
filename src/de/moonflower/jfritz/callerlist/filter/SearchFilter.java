package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

public class SearchFilter extends CallFilter {
	// ist schneller als lokale variablen
	String parts[];
	PhoneNumber phoneNumber;
	Person person;
	String comment;
	String areaNumber;

	public SearchFilter(String s) {
		parts = s.toLowerCase().split(" "); // TODO change to
		// toLowercase(locale)
	}

	//TODO reguläre Ausdrücke zulassen wird dann aber wohl super langsam


	public boolean passInternFilter(Call currentCall) {
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			phoneNumber = currentCall.getPhoneNumber();
			if (phoneNumber != null
					&& phoneNumber.getIntNumber().indexOf(part)!=-1)
				return true;

			if (phoneNumber != null
					&& phoneNumber.getCallByCall().indexOf(part)!=-1)
				return true;

			person = currentCall.getPerson();
			if (person != null
					&& person.getFullname().toLowerCase().indexOf(part)!=-1)
				return true;

			comment = currentCall.getComment();
			if (comment != null && comment.toLowerCase().indexOf(part)!=-1)
				return true;

			if (phoneNumber != null) {
				areaNumber = phoneNumber.getAreaNumber();

				if (areaNumber != null && areaNumber.indexOf(part)!=-1)
					return true;
			}
		}
		return false;
	}

}

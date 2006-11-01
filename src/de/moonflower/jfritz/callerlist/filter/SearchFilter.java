package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;

public class SearchFilter extends CallFilter {
	// ist schneller als lokale variablen
	private String parts[];

	private PhoneNumber phoneNumber;	//only for speedUp
	private Person person;				//only for speedUp
	private String comment;				//only for speedUp
	private String areaNumber;			//only for speedUp
	private String part;				//only for speedUp

	public SearchFilter(String s) {
		setSearchString(s);
	}

	//TODO reguläre Ausdrücke zulassen wird dann aber wohl super langsam

	public void setSearchString(String s){
		parts = s.toLowerCase().split(" "); // TODO change to
		//Debug.msg("setting searchFilter to "+s);
		// toLowercase(locale)
	}

	public boolean passInternFilter(Call currentCall) {
		for (int i = 0; i < parts.length; i++) {
			part = parts[i];
			phoneNumber = currentCall.getPhoneNumber();
			if (phoneNumber != null
					&& phoneNumber.getIntNumber().contains(part))
				return true;

			if (phoneNumber != null
					&& phoneNumber.getCallByCall().contains(part))
				return true;

			person = currentCall.getPerson();
			if (person != null
					&& person.getFullname().toLowerCase().contains(part))
				return true;

			comment = currentCall.getComment();
			if (comment != null && comment.toLowerCase().contains(part))
				return true;

			if (phoneNumber != null) {
				areaNumber = phoneNumber.getAreaNumber();

				if (areaNumber != null && areaNumber.contains(part))
					return true;
			}
		}
		return false;
	}

}

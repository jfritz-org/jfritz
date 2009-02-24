package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

public class SearchFilter extends CallFilter {
	// faster than creating local variables every call of passInternFilter
	private String parts[];

	private PhoneNumber phoneNumber; //only for speedUp

	private Person person; //only for speedUp

	private String comment; //only for speedUp

	private String areaNumber; //only for speedUp

	private String part; //only for speedUp

	private String text;

	private static final String type = FILTER_SEARCH;

	public SearchFilter(String s) {
		setSearchString(s);
	}

	//FEATURE reguläre Ausdrücke zulassen wird dann aber wohl super langsam

	public void setSearchString(String s) {
		parts = s.toLowerCase().trim().split(" ");
		text = s.trim();
		//Debug.msg("setting searchFilter to "+s);
	}

	public boolean passInternFilter(Call currentCall) {
		for (int i = 0; i < parts.length; i++) {
			part = parts[i];
			phoneNumber = currentCall.getPhoneNumber();
			if ((phoneNumber != null)
					&& phoneNumber.getIntNumber().contains(part)) {
				return true;
			}

			if ((phoneNumber != null)
					&& phoneNumber.getCallByCall().contains(part)) {
				return true;
			}

			person = JFritz.getPhonebook().findPerson(currentCall);
			if ((person != null)
					&& person.getFullname().toLowerCase().contains(part)) {
				return true;
			}

			comment = currentCall.getComment();
			if ((comment != null) && comment.toLowerCase().contains(part)) {
				return true;
			}

			if (phoneNumber != null) {
				areaNumber = phoneNumber.getAreaNumber();

				if ((areaNumber != null) && areaNumber.contains(part)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getSearchString(){
		return text;
	}

	public String getType(){
		return type;
	}

	public SearchFilter clone(){
		SearchFilter sf = new SearchFilter(this.text);
		sf.setEnabled(this.isEnabled());
		sf.setInvert(this.isInvert());
		return sf;
	}

}

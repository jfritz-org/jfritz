package de.moonflower.jfritz.utils.reverselookup;

import de.moonflower.jfritz.struct.PhoneNumberOld;
/**
 *
 * @author marc
 *
 */
class LookupRequest implements Comparable<LookupRequest> {
	final PhoneNumberOld number;
	final String lookupSite;
	final public int priority;

	LookupRequest(PhoneNumberOld number, int priority) {
		this.number = number;
		this.priority = priority;
		lookupSite = "";
	}

	LookupRequest(PhoneNumberOld number, int priority, String site) {
		this.number = number;
		this.priority = priority;
		lookupSite = site;
	}

	public int compareTo(LookupRequest o) {
		return priority > o.priority ? 1 : priority < o.priority ? -1 : 0;
	}

	public boolean equals(Object obj){
		return this.number.equals(((LookupRequest)obj).number) && this.priority == ((LookupRequest)obj).priority
			&& this.lookupSite == ((LookupRequest)obj).lookupSite;
	}

}

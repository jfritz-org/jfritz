package de.moonflower.jfritz.utils.reverselookup;

import de.moonflower.jfritz.struct.PhoneNumber;
/**
 *
 * @author marc
 *
 */
class LookupRequest implements Comparable<LookupRequest> {
	final PhoneNumber number;

	final public int priority;

	LookupRequest(PhoneNumber number, int priority) {
		this.number = number;
		this.priority = priority;
	}

	public int compareTo(LookupRequest o) {
		return priority > o.priority ? 1 : priority < o.priority ? -1 : 0;
	}

	public boolean equals(Object obj){
		return this.number.equals(((LookupRequest)obj).number) && this.priority == ((LookupRequest)obj).priority;
	}

}

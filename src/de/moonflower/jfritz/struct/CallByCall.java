package de.moonflower.jfritz.struct;

/**
 * Class for storing the call by call prefix + lenght combos. Used by
 * PhoneNumber.java for parsing out call by call prefixes from numbers.
 * Usage, if number.startsWith(prefix) then number = number.substring(length);
 *
 *
 * @author brian
 *
 */
public class CallByCall {

	private String prefix = "";

	private int length = 0;

	/**
	 * Use the constructor for creating new call by call objects
	 *
	 * @param p is the prefix
	 * @param l is the length of the call by call
	 */
	public CallByCall(String p, int l){
		prefix = p;
		length = l;
	}

	public String getPrefix(){
		return prefix;
	}

	public int getLength(){
		return length;
	}
}

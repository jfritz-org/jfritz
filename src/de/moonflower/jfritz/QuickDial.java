/*
 *
 * Created on 14.05.2005
 *
 */
package de.moonflower.jfritz;

/**
 * @author Arno Willig
 *
 */
public class QuickDial {

	String quickdial, vanity, number;

	/**
	 * creates new QuickDial object
	 *
	 * @param quickdial
	 * @param vanity
	 * @param number
	 */
	public QuickDial(String quickdial, String vanity, String number) {
		this.quickdial = quickdial;
		this.vanity = vanity;
		this.number = number;
	}

	/**
	 * @return Returns the number.
	 */
	public final String getNumber() {
		return number;
	}

	/**
	 * @param number
	 *            The number to set.
	 */
	public final void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return Returns the quickdial.
	 */
	public final String getQuickdial() {
		return quickdial;
	}

	/**
	 * @param quickdial
	 *            The quickdial to set.
	 */
	public final void setQuickdial(String quickdial) {
		this.quickdial = quickdial;
	}

	/**
	 * @return Returns the vanity.
	 */
	public final String getVanity() {
		return vanity;
	}

	/**
	 * @param vanity
	 *            The vanity to set.
	 */
	public final void setVanity(String vanity) {
		this.vanity = vanity;
	}

}

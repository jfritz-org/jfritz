/*
 * Created on 02.06.2005
 *
 */
package de.moonflower.jfritz.struct;

/**
 * @author Arno Willig
 *
 */
public class Country {

	private final String name;

	private final String locale;

	private final String IDD; // International Direct Dialing

	private final String NDD; // National Direct Dialing

	private final String countryCode;

	/**
	 *
	 */
	public Country(final String name, final String locale, final String countryCode, final String IDD,
			final String NDD) {
		this.name = name;
		this.locale = locale;
		this.IDD = IDD;
		this.NDD = NDD;
		this.countryCode = countryCode;
	}


	/**
	 * @return Returns the countryCode.
	 */
	public final String getCountryCode() {
		return countryCode;
	}
	/**
	 * @return Returns the iDD.
	 */
	public final String getIDD() {
		return IDD;
	}
	/**
	 * @return Returns the locale.
	 */
	public final String getLocale() {
		return locale;
	}
	/**
	 * @return Returns the name.
	 */
	public final String getName() {
		return name;
	}
	/**
	 * @return Returns the nDD.
	 */
	public final String getNDD() {
		return NDD;
	}

}

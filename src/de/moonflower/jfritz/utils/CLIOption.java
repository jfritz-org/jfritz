/*
 * Created on 26.06.2005
 *
 */
package de.moonflower.jfritz.utils;

public class CLIOption {

	private char shortOption;

	private String longOption;

	private String description;

	private String parameter;

	public CLIOption(char shortOption, String longOption, String parameter,
			String description) {
		this.shortOption = shortOption;
		this.longOption = longOption;
		this.parameter = parameter;
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the longOption.
	 */
	public String getLongOption() {
		return longOption;
	}

	/**
	 * @return Returns the parameter.
	 * @throws Exception
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * @return Returns true if option has parameter.
	 */
	public boolean hasParameter() {
		return (parameter != null);
	}

	/**
	 * @return Returns the shortOption.
	 */
	public char getShortOption() {
		return shortOption;
	}

}
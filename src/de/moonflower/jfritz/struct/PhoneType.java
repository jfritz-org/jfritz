/*
 * Created on 10.06.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.Main;

/**
 * @author Arno Willig
 *
 */

public class PhoneType {
	String type;

	public PhoneType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String toString() {
		try {
			Pattern p = Pattern.compile("([a-z]*)(\\d*)"); //$NON-NLS-1$
			Matcher m = p.matcher(type);
			if (m.find()) {
				return Main.getMessage("phone_" + m.group(1)) + " " //$NON-NLS-1$,  //$NON-NLS-2$
						+ m.group(2);
			} else {
				return Main.getMessage("phone_" + type); //$NON-NLS-1$

			}
		} catch (MissingResourceException e) {
			return type;
		}
	}
}

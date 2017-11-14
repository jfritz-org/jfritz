/*
 * Created on 10.06.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.messages.MessageProvider;

/**
 * @author Arno Willig
 *
 */

public class PhoneType {
	String type;

	protected MessageProvider messages = MessageProvider.getInstance();

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
				return messages.getMessage("phone_" + m.group(1)) + " " //$NON-NLS-1$,  //$NON-NLS-2$
						+ m.group(2);
			} else {
				return messages.getMessage("phone_" + type); //$NON-NLS-1$

			}
		} catch (MissingResourceException e) {
			return type;
		}
	}
}

package de.moonflower.jfritz.importexport;

import java.util.Enumeration;
import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;

public class VCardParserEMail {
	public static boolean parse(VCardParser parser, Person person) {
		Enumeration<String> en = parser.getPropertyType().keys();
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			if (key.equals("type")) {
				Vector<String> propertyTypes = parser.getPropertyType().get(key);
				for (String types: propertyTypes) {
					Debug.debug(types);
				}

				if (propertyTypes.contains("internet")) {
					person.setEmailAddress(parser.getValues().get(0));
				}
			} else {
				Debug.error("Unknown key in VCardParserEMail: " + key);
			}
		}
		return false;
	}
}

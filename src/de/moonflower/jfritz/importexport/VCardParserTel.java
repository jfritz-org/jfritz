package de.moonflower.jfritz.importexport;

import java.util.Enumeration;
import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;

public class VCardParserTel {

	public static boolean parse(VCardParser parser, Person person) {
		Enumeration<String> en = parser.getPropertyType().keys();
		Vector<PhoneNumberOld> numbers = null;
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			if (key.equals("type")) {
				Vector<String> propertyTypes = parser.getPropertyType().get(key);
				for (String types: propertyTypes) {
					Debug.debug(types);
				}

				if (person.getNumbers() == null) {
					numbers = new Vector<PhoneNumberOld>();
				} else {
					numbers = person.getNumbers();
				}


				if (!containsNumber(numbers, parser.getValues().get(0))) {
					PhoneNumberOld num = new PhoneNumberOld(parser.getValues().get(0), false);
					if (propertyTypes.contains("home")) {
						num.setType("home");
					} else if (propertyTypes.contains("cell")) {
						num.setType("mobile");
					} else if (propertyTypes.contains("work")) {
						num.setType("business");
					} else if (propertyTypes.contains("fax")) {
						num.setType("fax");
					} else if (propertyTypes.contains("voice")) {
						num.setType("home");
					} else {
						num.setType("other");
					}
					numbers.add(num);

					if (propertyTypes.contains("pref") || numbers.size() == 1) {
						person.setStandard(num.getType());
					}
				}
			} else {
				Debug.error("Unknown key in VCardParserTel: " + key);
			}
		}
		return false;
	}

	private static boolean containsNumber(Vector<PhoneNumberOld> numbers, String number) {
		for (PhoneNumberOld num: numbers) {
			if (num.toString().equals(number.toString())) {
				return true;
			}
		}
		return false;
	}
}

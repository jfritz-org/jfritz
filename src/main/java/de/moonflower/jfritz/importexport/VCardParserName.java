package de.moonflower.jfritz.importexport;

import de.moonflower.jfritz.struct.Person;

public class VCardParserName {

	public static boolean parse(VCardParser parser, Person person) {
		if (parser.getValues().size() < 5)
		{
			if (parser.getValues().size() > 0) {
				person.setLastName(parser.getValues().get(0));
			}
			if (parser.getValues().size() > 1) {
				person.setFirstName(parser.getValues().get(1));
			}
			if (parser.getValues().size() > 2) {
				person.setFirstName(person.getFirstName() + " " + parser.getValues().get(2));
			}
			return false;
		} else {
			return true;
		}
	}
}

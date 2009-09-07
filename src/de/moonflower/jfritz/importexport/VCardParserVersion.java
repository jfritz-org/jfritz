package de.moonflower.jfritz.importexport;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;

public class VCardParserVersion {
	public static boolean parse(VCardParser parser, Person person) {
		if (parser.getValues().size() != 1
				|| !parser.getValues().get(0).equals("2.1")) {
			Debug.error("Wrong vCard version " + parser.getValues().get(0).equals("2.1"));
			return true;
		}
		return false;
	}

}

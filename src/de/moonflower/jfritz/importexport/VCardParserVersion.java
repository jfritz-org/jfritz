package de.moonflower.jfritz.importexport;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;

public class VCardParserVersion {
	private final static Logger log = Logger.getLogger(VCardParserVersion.class);
	public static boolean parse(VCardParser parser, Person person) {
		if (parser.getValues().size() != 1
				|| !parser.getValues().get(0).equals("2.1")) {
			log.error("Wrong vCard version " + parser.getValues().get(0).equals("2.1"));
			return true;
		}
		return false;
	}

}

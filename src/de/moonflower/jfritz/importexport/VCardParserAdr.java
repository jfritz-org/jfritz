package de.moonflower.jfritz.importexport;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;

public class VCardParserAdr {
	private final static Logger log = Logger.getLogger(VCardParserAdr.class);

	public static boolean parse(VCardParser parser, Person person) {
		Enumeration<String> en = parser.getPropertyType().keys();
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			if (key.equals("type")) {
				Vector<String> propertyTypes = parser.getPropertyType().get(key);

				for (String types: propertyTypes) {
					log.debug(types);
				}

				if (parser.getValues().size() >= 3)
				{
					if (person.getStreet() != null
							&& !person.getStreet().equals("")) {
						if (propertyTypes.contains("home")) {
							// overwrite with home address (street)
							person.setStreet(parser.getValues().get(2));
						}
					} else {
						person.setStreet(parser.getValues().get(2));
					}
				}

				if (parser.getValues().size() >= 4)
				{
					if (person.getCity() != null && !person.getCity().equals("")) {
						if (propertyTypes.contains("home")) {
							// overwrite with home address (street)
							person.setCity(parser.getValues().get(3));
						}
					} else {
						person.setCity(parser.getValues().get(3));
					}
				}

				if (parser.getValues().size() >= 6)
				{
					if (person.getPostalCode() != null && !person.getPostalCode().equals("")) {
						if (propertyTypes.contains("home")) {
							person.setPostalCode(parser.getValues().get(5));
						}
					} else {
						person.setPostalCode(parser.getValues().get(5));
					}
				}
			} else {
				log.error("Unknown key in VCardParserAdr: " + key);
			}
		}
		return false;
	}
}

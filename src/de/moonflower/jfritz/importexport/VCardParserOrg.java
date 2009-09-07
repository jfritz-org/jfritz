package de.moonflower.jfritz.importexport;

import de.moonflower.jfritz.struct.Person;

public class VCardParserOrg {

	public static boolean parse(VCardParser parser, Person person) {
		String company = "";
		for (String s: parser.getValues()) {
			company += s + ", ";
		}
		if (company.trim().endsWith(",")) {
			company = company.substring(0, company.length()-2);
		}
		person.setCompany(company.trim());

		return false;
	}
}

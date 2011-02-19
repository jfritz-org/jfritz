package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;

public interface ParseSiteResultListener {

	public void finished(Vector<Person> foundPersons);
}

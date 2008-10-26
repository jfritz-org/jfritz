package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.reverselookup.LookupObserver;

public class CheckEntry implements LookupObserver {

	private boolean succeeded = false;
	private boolean done = false;

	private Person checkPerson;

	public CheckEntry(String firstname, String lastname, String street, String zipcode, String city)
	{
		checkPerson = new Person(firstname, "", lastname,
				street, zipcode, city, "", "");
		succeeded = false;
		done = false;
	}

	public void percentOfLookupDone(float f) {
	}

	public void personsFound(Vector<Person> persons) {
		succeeded = (persons.size() == 1);
		if (succeeded)
		{
			Person person = persons.elementAt(0);
			succeeded = (person.getFirstName().equals(checkPerson.getFirstName()));
		}
	}

	public void saveFoundEntries(Vector<Person> persons) {
		succeeded = (persons.size() == 1);
		Person person = persons.elementAt(0);
		if (succeeded)
			succeeded = (person.getFirstName().equals(checkPerson.getFirstName()));
		if (succeeded)
			succeeded = (person.getLastName().equals(checkPerson.getLastName()));
		if (succeeded)
			succeeded = (person.getStreet().equals(checkPerson.getStreet()));
		if (succeeded)
			succeeded = (person.getCity().equals(checkPerson.getCity()));
		if (succeeded)
			succeeded = (person.getPostalCode().equals(checkPerson.getPostalCode()));

		done = true;
	}

	public boolean hasSucceeded()
	{
		return succeeded;
	}

	public boolean isDone()
	{
		return done;
	}
}

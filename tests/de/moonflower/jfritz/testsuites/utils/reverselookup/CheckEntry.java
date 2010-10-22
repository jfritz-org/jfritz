package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.reverselookup.LookupObserver;

public class CheckEntry implements LookupObserver {

	private boolean done = false;

	private Person checkPerson;
	private Person receivedPerson;
	PhoneNumberOld checkedNumber;

	public CheckEntry(PhoneNumberOld num, String firstname, String lastname, String street, String zipcode, String city)
	{
		this(num, firstname, lastname, "", street, zipcode, city);
	}

	public CheckEntry(PhoneNumberOld num, String firstname, String lastname, String company, String street, String zipcode, String city)
	{
		checkPerson = new Person(firstname, "", lastname,
				street, zipcode, city, "", "");
		checkedNumber = num;
		done = false;
	}

	public void percentOfLookupDone(float f) {
	}

	public void personsFound(Vector<Person> persons) {
	}

	public void saveFoundEntries(Vector<Person> persons) {
		receivedPerson = persons.elementAt(0);
		done = true;
	}

	public boolean isDone()
	{
		return done;
	}

	public Person getCheckPerson()
	{
		return checkPerson;
	}

	public Person getReceivedPerson()
	{
		return receivedPerson;
	}

	public PhoneNumberOld getCheckedNumber()
	{
		return checkedNumber;
	}
}

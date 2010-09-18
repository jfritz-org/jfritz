package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.reverselookup.LookupObserver;

public class CheckEntry implements LookupObserver {

	private boolean succeeded = false;
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
		receivedPerson = persons.elementAt(0);
		if (succeeded)
		{
			succeeded = (receivedPerson.getFirstName().equals(checkPerson.getFirstName()));
		}
		if (succeeded)
		{
			succeeded = (receivedPerson.getLastName().equals(checkPerson.getLastName()));
		}
		if (succeeded)
		{
			succeeded = (receivedPerson.getStreet().equals(checkPerson.getStreet()));
		}
		if (succeeded)
		{
			succeeded = (receivedPerson.getCity().equals(checkPerson.getCity()));
		}
		if (succeeded)
		{
			succeeded = (receivedPerson.getPostalCode().equals(checkPerson.getPostalCode()));
		}

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

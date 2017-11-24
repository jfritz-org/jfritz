package de.moonflower.jfritz.phonebook;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;

public interface PhoneBookListener {

	public void contactsAdded(Vector<Person> newContacts);

	public void contactsRemoved(Vector<Person> removedContacts);

	public void contactUpdated(Person original, Person updated);
}

package de.moonflower.jfritz.phonebookNew;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;

public abstract class PhoneBookBase {
	private String name = "";
	private String description = "";

	public PhoneBookBase(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public synchronized void addEntries(final Vector<Person> persons) {
		for (Person person:persons) {
			addEntry(person);
		}
		persist(persons);
	}

	public synchronized void removeEntries(final Vector<Person> persons) {
		for (Person person:persons) {
			removeEntry(person);
		}
		persist(persons);
	}

	public abstract void updateEntry(final Person original, final Person updated);
	public abstract boolean contains(final Person person);
	public abstract boolean contains(final PhoneNumberOld number);
	public abstract Vector<Person> getAllEntries();
	public abstract Person findFirstPerson(final PhoneNumberOld number);
	public abstract Vector<Person> getPersons(final PhoneNumberOld number);

	protected abstract void addEntry(final Person person);
	protected abstract void removeEntry(final Person person);

	public abstract void persist(final Vector<Person> updatedPersons);
}

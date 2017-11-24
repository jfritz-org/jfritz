package de.moonflower.jfritz.phonebookNew;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;

public class XMLPhoneBook extends PhoneBookBase {

	@SuppressWarnings("unused")
	private Vector<Person> persons;

	public XMLPhoneBook(final String name, final String description) {
		super(name, description);
		persons = new Vector<Person>();
	}

	@Override
	protected void addEntry(final Person person) {
	}

	@Override
	public boolean contains(final Person person) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(final PhoneNumberOld number) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void persist(final Vector<Person> updatedPersons) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeEntry(final Person person) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntry(final Person original, final Person updated) {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector<Person> getAllEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Person findFirstPerson(PhoneNumberOld number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<Person> getPersons(PhoneNumberOld number) {
		// TODO Auto-generated method stub
		return null;
	}

}

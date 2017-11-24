package de.moonflower.jfritz.phonebookNew;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;

public class PhoneBookManager {

	protected Vector<PhoneBookBase> listOfPhoneBooks;

	public PhoneBookManager() {
		listOfPhoneBooks = new Vector<PhoneBookBase>();
	}

	public void registerPhonebook(final PhoneBookBase p) {
		if (!listOfPhoneBooks.contains(p)) {
			listOfPhoneBooks.add(p);
		}
	}

	public void unregisterPhonebook(final PhoneBookBase p) {
		if (listOfPhoneBooks.contains(p)) {
			listOfPhoneBooks.remove(p);
		}
	}

	public Person findFirstPerson(final PhoneNumberOld number) {
		for (PhoneBookBase p: listOfPhoneBooks) {
			if (p.contains(number)) {
				return p.findFirstPerson(number);
			}
		}
		return null;
	}

	public Vector<Person> getPersons(final PhoneNumberOld number) {
		Vector<Person> result = new Vector<Person>();
		for (PhoneBookBase p: listOfPhoneBooks) {
			if (p.contains(number)) {
				result.addAll(p.getPersons(number));
			}
		}
		return result;
	}

	public Vector<Person> getAllPersons(final String phonebookName) {
		Vector<Person> result = new Vector<Person>();
		for (PhoneBookBase p:listOfPhoneBooks) {
			if (p.getName().equals(phonebookName)) {
				result.addAll(p.getAllEntries());
			}
		}
		return result;
	}

	public Vector<Person> getAllPersons() {
		Vector<Person> result = new Vector<Person>();
		for (PhoneBookBase p:listOfPhoneBooks) {
			result.addAll(p.getAllEntries());
		}
		return result;
	}
}

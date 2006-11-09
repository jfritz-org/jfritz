/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupSwitzerland;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupFrance;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupGermany;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupItaly;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupNetherlands;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupUnitedStates;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupAustria;

/**
 * Class for telephone number reverse lookup using various search engines
 *
 *
 *
 */
public class ReverseLookup {

	static class LookupThread extends Thread {
		int total;

		int current;

		private Vector<PhoneNumber> number;

		private Vector<Person> result;

		private LookupObserver observer;

		LookupThread(Vector<PhoneNumber> number, LookupObserver obs) {
			total = number.size();
			this.number = number;
			this.observer = obs;
			result = new Vector<Person>();
		}

		public void run() {
			for (current = 0; current < number.size(); current++) {
				// doppelte?!?
				Person newPerson = lookup(number.elementAt(current));
				result.add(newPerson);
			}
			observer.personsFound(result);
		}

		public double getPercentDone() {
			return current / (total + 0.0);
		}

		public String getMessage() {
			return "Looking up: " + number.elementAt(current).toString();
		}
	}

	private static LookupThread thread;

	/**
	 * This Function does a lookup for a Vector of PhoneNumbers, only if the
	 * LookupThread is not busy the caller must give an observer, his method
	 * personsFound(Vector<Person>) will be called
	 *
	 * @param number
	 *            the number wich will be looked up
	 * @param obs
	 *            the observer wich will be will receive the Persons
	 * @return true if the Thread is free and we can start lookup, false if the
	 *         Thread is already busy
	 */
	public static boolean lookup(PhoneNumber number, LookupObserver obs) {
		if (thread.isAlive()) {
			return false;
		}
		Vector<PhoneNumber> v = new Vector<PhoneNumber>();
		v.add(number);
		return lookup(v, obs);
	}

	/**
	 * This Function does a lookup for a Vector of PhoneNumbers, only if the
	 * LookupThread is not busy the caller must give an observer, his method
	 * personsFound(Vector<Person>) will be called
	 *
	 * @param number
	 *            the numbers wich will be looked up
	 * @param obs
	 *            the observer wich will be will receive the Persons
	 * @return true if the Thread is free and we can start lookup, false if the
	 *         Thread is already busy
	 */
	public static boolean lookup(Vector<PhoneNumber> number, LookupObserver obs) {

		if ((thread != null) && thread.isAlive()) {
			return false;
		}
		thread = new LookupThread(number, obs);
		thread.start();
		return true;

	}

	/**
	 * @deprecated better use boolean lookup(Vector<PhoneNumber> number,
	 *             LookupObserver obs) {
	 * @param number
	 * @return the Person
	 */
	//TODO check if we need to sync
	private static Person lookup(PhoneNumber number) {

		Person newPerson;
		/***********************************************************************
		 * if (number.isMobile()) { newPerson = new Person();
		 * newPerson.addNumber(number); Debug.msg("Adding mobile " +
		 * number.getIntNumber()); //$NON-NLS-1$ } else
		 **********************************************************************/
		if (number.isFreeCall()) {
			newPerson = new Person("", "FreeCall"); //$NON-NLS-1$,  //$NON-NLS-2$
			newPerson.addNumber(number);
		} else if (number.isSIPNumber() || number.isQuickDial()) {
			newPerson = new Person();
			newPerson.addNumber(number);
		} else {
			if (number.convertToIntNumber().startsWith(
					PhoneNumber.SWITZERLAND_CODE)) {
				newPerson = ReverseLookupSwitzerland.lookup(number
						.getAreaNumber());
			} else if (number.convertToIntNumber().startsWith(
					PhoneNumber.ITALY_CODE)) {
				newPerson = ReverseLookupItaly.lookup(number.getAreaNumber());
			} else if (number.convertToIntNumber().startsWith(
					PhoneNumber.GERMANY_CODE)) {
				newPerson = ReverseLookupGermany.lookup(number.getAreaNumber());
			} else if (number.convertToIntNumber().startsWith(
					PhoneNumber.HOLLAND_CODE)) {
				newPerson = ReverseLookupNetherlands.lookup(number
						.getAreaNumber());
			} else if (number.convertToIntNumber().startsWith(
					PhoneNumber.FRANCE_CODE)) {
				newPerson = ReverseLookupFrance.lookup(number.getAreaNumber());
			} else if (number.convertToIntNumber().startsWith(
					PhoneNumber.USA_CODE)) {
				newPerson = ReverseLookupUnitedStates.lookup(number
						.getAreaNumber());
			} else if (number.convertToIntNumber().startsWith(
					PhoneNumber.AUSTRIA_CODE)) {
				newPerson = ReverseLookupAustria.lookup(number.getAreaNumber());
			} else {
				newPerson = new Person();
				newPerson.addNumber(number.getAreaNumber(), "home");
			}
		}
		return newPerson;
	}

	public static void loadAreaCodes() {

		// loads the area code city mappings
		ReverseLookupGermany.loadAreaCodes();
		ReverseLookupAustria.loadAreaCodes();
		// ReverseLookupNetherlands.loadAreaCodes();

	}

	public static Person busyLookup(PhoneNumber callerPhoneNumber) {
		return lookup(callerPhoneNumber);
	}

}

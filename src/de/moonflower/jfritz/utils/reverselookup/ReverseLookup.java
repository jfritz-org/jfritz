/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupSwitzerland;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupFrance;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupGermany;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupItaly;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupNetherlands;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupUnitedStates;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupAustria;

class LookupThread extends Thread {
	LookupRequest currentRequest;

	private PriorityBlockingQueue<LookupRequest> requests;

	boolean empty;

	LookupThread(PriorityBlockingQueue<LookupRequest> requests) {
		this.requests = requests;
	}

	public void run() {
		while (true) {
			try {
				currentRequest = requests.take();
			} catch (InterruptedException e) {
				continue;// we were interrupted
			}
			Vector<Person> result = new Vector<Person>();
			for (int i = 0; i < currentRequest.numbers.size(); i++) {
				// doppelte?!?
				Person newPerson = ReverseLookup.lookup(currentRequest.numbers
						.elementAt(i));
				result.add(newPerson);
				currentRequest.observer.percentOfLookupDone(((float)i) / currentRequest.numbers.size());
			}
			currentRequest.observer.personsFound(result);
		}
	}

	public void notifyRequest() {
		notifyAll();
	}
}

class LookupRequest implements Comparable<LookupRequest> {
	final Vector<PhoneNumber> numbers;

	final LookupObserver observer;

	final public int priority;

	LookupRequest(Vector<PhoneNumber> numbers, LookupObserver obs, int priority) {
		this.numbers = numbers;
		this.observer = obs;
		this.priority = priority;
	}

	public int compareTo(LookupRequest o) {
		return priority > o.priority ? 1 : priority < o.priority ? -1 : 0;
	}
}

/**
 * Class for telephone number reverse lookup using various search engines
 *
 *
 *
 */
public class ReverseLookup {

	static LookupThread thread;

	static volatile PriorityBlockingQueue<LookupRequest> requests = new PriorityBlockingQueue<LookupRequest>();

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
	public static synchronized void lookup(PhoneNumber number,
			LookupObserver obs) {
		Vector<PhoneNumber> v = new Vector<PhoneNumber>();
		v.add(number);
		lookup(v, obs);
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
	public static synchronized void lookup(Vector<PhoneNumber> number,
			LookupObserver obs) {

		LookupRequest req = new LookupRequest(number, obs, 5);
		Debug.msg("adding request");
		requests.put(req);

		if (thread == null) {
			Debug.msg("creating thread");
			thread = new LookupThread(requests);
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * @deprecated better use boolean lookup(Vector<PhoneNumber> number,
	 *             LookupObserver obs) {
	 * @param number
	 * @return the Person
	 */
	static synchronized Person lookup(PhoneNumber number) {

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

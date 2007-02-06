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

/**
 *
 * @author marc
 *
 */
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
				currentRequest.observer.percentOfLookupDone(((float) i)
						/ currentRequest.numbers.size());
			}
			currentRequest.observer.personsFound(result);
		}
	}
}

/**
 *
 * @author marc
 *
 */
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
	public static final String AUSTRIA_CODE = "+43", BELGIUM_CODE = "+32",
			CHINA_CODE = "+86", CZECH_CODE = "+420", DENMARK_CODE = "+45",
			FINLAND_CODE = "+358", FRANCE_CODE = "+33", GERMANY_CODE = "+49",
			GREATBRITAIN_CODE = "+44", HOLLAND_CODE = "+31",
			HUNGARY_CODE = "+36", IRELAND_CODE = "+353", ITALY_CODE = "+39",
			JAPAN_CODE = "+81", LUXEMBOURG_CODE = "+352", NORWAY_CODE = "+47",
			POLAND_CODE = "+48", PORTUGAL_CODE = "+351", RUSSIA_CODE = "+7",
			SLOVAKIA_CODE = "+421", SPAIN_CODE = "+34", SWEDEN_CODE = "+46",
			SWITZERLAND_CODE = "+41", TURKEY_CODE = "+90",
			UKRAINE_CODE = "+380", USA_CODE = "+1";

	static LookupThread thread;

	static volatile PriorityBlockingQueue<LookupRequest> requests = new PriorityBlockingQueue<LookupRequest>();

	/**
	 * This Function does a lookup for a Vector of PhoneNumbers, the caller must
	 * give an observer, his method personsFound(Vector<Person>) will be called
	 *
	 * @param number
	 *            the number wich will be looked up
	 * @param obs
	 *            the observer wich will be will receive the Persons
	 */
	public static synchronized void lookup(PhoneNumber number,
			LookupObserver obs) {
		Vector<PhoneNumber> v = new Vector<PhoneNumber>();
		v.add(number);
		lookup(v, obs);
	}

	/**
	 * This Function does a lookup for a Vector of PhoneNumbers the caller must
	 * give an observer, his method personsFound(Vector<Person>) will be called
	 *
	 * @param number
	 *            the numbers wich will be looked up
	 * @param obs
	 *            the observer wich will be will receive the Persons
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
	 *             LookupObserver obs) or busyLookup for one single lookup
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
			if (number.getCountryCode().equals(SWITZERLAND_CODE)) {
				newPerson = ReverseLookupSwitzerland.lookup(number
						.getAreaNumber());
			} else if (number.getCountryCode().equals(ITALY_CODE)) {
				newPerson = ReverseLookupItaly.lookup(number.getAreaNumber());
			} else if (number.getCountryCode().equals(GERMANY_CODE)) {
				newPerson = ReverseLookupGermany.lookup(number.getAreaNumber());
			} else if (number.getCountryCode().equals(HOLLAND_CODE)) {
				newPerson = ReverseLookupNetherlands.lookup(number
						.getAreaNumber());
			} else if (number.getCountryCode().equals(FRANCE_CODE)) {
				newPerson = ReverseLookupFrance.lookup(number.getAreaNumber());
			} else if (number.getCountryCode().equals(USA_CODE)) {
				newPerson = ReverseLookupUnitedStates.lookup(number
						.getAreaNumber());
			} else if (number.getCountryCode().equals(AUSTRIA_CODE)) {
				newPerson = ReverseLookupAustria.lookup(number.getAreaNumber());
			} else {
				newPerson = new Person();
				newPerson.addNumber(number.getAreaNumber(), "home");
			}
		}

		Debug.msg("Name:" +newPerson.getFullname());

		return newPerson;
	}

	public static void loadAreaCodes() {

		// loads the area code city mappings
		ReverseLookupGermany.loadAreaCodes();
		ReverseLookupAustria.loadAreaCodes();
		// ReverseLookupNetherlands.loadAreaCodes();

	}

	/**
	 * This function does one lookup for a PhoneNumber. Good if you want a
	 * single lookup, id you need more numbers looked up better use
	 * <code>lookup(Vector<PhoneNumber> number, LookupObserver obs)</code>
	 * this will start an extra Thread
	 *
	 * @param callerPhoneNumber
	 *            the number wich will be looked up
	 * @return the Person this method found
	 */
	public static Person busyLookup(PhoneNumber callerPhoneNumber) {
		return lookup(callerPhoneNumber);
	}

}

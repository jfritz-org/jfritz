package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;

public interface LookupObserver {
	/**
	 * is called, if some persons were found
	 * @param persons
	 */
	public void personsFound(Vector<Person> persons);


	/**
	 * Will be called if a specified number of calls have been found
	 */
	public void saveFoundEntries(Vector<Person> persons);

	/**
	 * Will be called after each single lookup so a view can show a progressbar
	 * @param f
	 */
	public void percentOfLookupDone(float f);

}

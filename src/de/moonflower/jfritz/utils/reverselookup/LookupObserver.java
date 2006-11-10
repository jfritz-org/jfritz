package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

public interface LookupObserver {
	/**
	 * is called, if some persons were found
	 * @param persons
	 */
	public void personsFound(Vector persons);

	/**
	 * Will be called after each single lookup so a view can show a progressbar
	 * @param f
	 */
	public void percentOfLookupDone(float f);

}

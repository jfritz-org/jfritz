package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;

public interface IReverseLookupFinishedWithResultListener {
	public void finished(final Vector<Person> result);
}

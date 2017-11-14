package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;

public interface IReverseLookupProgressListener {
	public void progress(final int percent, final Vector<Person> result);
}

package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

public interface LookupObserver {
	public void personsFound(Vector persons);

	public void percentOfLookupDone(float f);

}

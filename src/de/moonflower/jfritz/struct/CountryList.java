/*
 * Created on 02.06.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.util.Vector;


/**
 * @author Arno Willig
 *
 */
public class CountryList {

	private static Vector list;

	/**
	 *
	 */
	public CountryList() {
		list.add(new Country("Deutschland", "DE", "+49", "00", "0")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$,  //$NON-NLS-5$
		list.add(new Country("Österreich", "A", "+43", "00", "0")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$,  //$NON-NLS-5$
		list.add(new Country("Schweiz", "CH", "+41", "00", "0")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$,  //$NON-NLS-5$
		list.add(new Country("Niederlande", "NL", "+31", "00", "0")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$,  //$NON-NLS-5$
		list.add(new Country("USA", "US", "+1", "011", "1")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$,  //$NON-NLS-5$
	}

	public static Vector getList() {
		return list;
	}

}

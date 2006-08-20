/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.ReverseLookupSwitzerland;
import de.moonflower.jfritz.utils.ReverseLookupFrance;
import de.moonflower.jfritz.utils.ReverseLookupGermany;
import de.moonflower.jfritz.utils.ReverseLookupItaly;
import de.moonflower.jfritz.utils.ReverseLookupNetherlands;
import de.moonflower.jfritz.utils.ReverseLookupUnitedStates;
/**
 * Class for telephone number reverse lookup using "dasoertliche.de"
 *
 * @author Arno Willig
 *
 */
public class ReverseLookup {

	public static Person lookup(PhoneNumber number) {
		Person newPerson;
/**		if (number.isMobile()) {
			newPerson = new Person();
			newPerson.addNumber(number);
			Debug.msg("Adding mobile " + number.getIntNumber()); //$NON-NLS-1$
		} else **/
        if (number.isFreeCall()) {
			newPerson = new Person("", "FreeCall"); //$NON-NLS-1$,  //$NON-NLS-2$
			newPerson.addNumber(number);
		} else if (number.isSIPNumber() || number.isQuickDial()) {
		    newPerson = new Person ();
		    newPerson.addNumber(number);
		} else {
			if(number.convertToIntNumber().startsWith(PhoneNumber.SWITZERLAND_CODE))
				newPerson = ReverseLookupSwitzerland.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(PhoneNumber.ITALY_CODE))
				newPerson = ReverseLookupItaly.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(PhoneNumber.GERMANY_CODE))
				newPerson = ReverseLookupGermany.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(PhoneNumber.HOLLAND_CODE))
				newPerson = ReverseLookupNetherlands.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(PhoneNumber.FRANCE_CODE))
				newPerson = ReverseLookupFrance.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(PhoneNumber.USA_CODE))
				newPerson = ReverseLookupUnitedStates.lookup(number.getAreaNumber());
			else{
				newPerson = new Person ();
				newPerson.addNumber(number.getAreaNumber(), "home");}
		}
		return newPerson;
	}

}

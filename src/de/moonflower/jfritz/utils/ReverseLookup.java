/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.ReverseLookupSwitzerland;
/**
 * Class for telephone number reverse lookup using "dasoertliche.de"
 *
 * @author Arno Willig
 *
 */
public class ReverseLookup {

	//Sort the following COUNTRY_CODE list alphabetically

	public static final String AUSTRIA_CODE = "+43";

	public static final String CANADA_CODE = "+1";

	public static final String FANCE_CODE = "+33";

	public static final String ITALY_CODE = "+39";

	public static final String GERMANY_CODE = "+49";

	public static final String GREATBRITAIN_CODE = "+44";

	public static final String NETHERLANDS_CODE = "+31";

	public static final String RUSSIA_CODE = "+7";

	public static final String SPAIN_CODE = "+34";

	public static final String SWITZERLAND_CODE = "+41";

	public static final String USA_CODE = "+1";




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
			if(number.convertToIntNumber().startsWith(SWITZERLAND_CODE))
				newPerson = ReverseLookupSwitzerland.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(ITALY_CODE))
				newPerson = ReverseLookupItaly.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(GERMANY_CODE))
				newPerson = ReverseLookupGermany.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(NETHERLANDS_CODE))
				newPerson = ReverseLookupNetherlands.lookup(number.getAreaNumber());
			else{
				newPerson = new Person ();
				newPerson.addNumber(number.getAreaNumber(), "home");}
		}
		return newPerson;
	}

}

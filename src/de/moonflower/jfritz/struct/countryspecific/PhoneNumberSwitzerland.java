package de.moonflower.jfritz.struct.countryspecific;

import de.moonflower.jfritz.JFritz;
/**
 * Class to work with Swiss phone numbers
 *
 * Created: 08.08.07
 *
 * @author Brian Jensen
 *
 */
public class PhoneNumberSwitzerland {

	public static final String CbCPrefix = "10";

	public static final int CbCLength = 5;

	public static String getCallbyCall(String number){
		if (number.startsWith(CbCPrefix))
			return number.substring(0, CbCLength-1);

		return "";
	}



	/**
	 * @return True if number is a mobile one
	 */
	public static boolean isMobile(String number) {
		return number.startsWith("07");
	}

	/**
	 * @return True if number is an emergency number
	 */
	public static boolean isEmergencyCall(String number) {
		if (number.equals("110")) //$NON-NLS-1$
			return true; // Germany Police
		else if (number.equals("112")) //$NON-NLS-1$
			return true; // Germany Medical
		else if (number.equals("116116")) //$NON-NLS-1$
			return true; // Germany Credit Card
		else if (number.equals("144")) //$NON-NLS-1$
			return true; // Switzerland Medical
		return false;
	}

}

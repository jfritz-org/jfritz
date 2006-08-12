/*
 * Created on 01.06.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.util.Enumeration;
import java.util.HashMap;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.countryspecific.*;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Arno Willig
 *
 */
public class PhoneNumber implements Comparable {

	private String numberMatcher = "([0-9]|\\+|\\(|\\)| |-|/)+|\\**";//$NON-NLS-1$

	private String number = "";//$NON-NLS-1$

	private String callbycall = "";//$NON-NLS-1$

	private String type = "";//$NON-NLS-1$
	// type values : "home", "mobile", "homezone",
	// "business", "other", "fax", "sip" };

	//Please keep these in alphabetical order
	public static String AUSTRIA_CODE="+43",
	 					 BELGIUM_CODE="+32",
	 					 CHINA_CODE="+86",
	 					 CZECH_CODE="+420",
	 					 DENMARK_CODE="+45",
	 					 FINLAND_CODE="+358",
	 					 FRANCE_CODE="+33",
	 					 GERMANY_CODE="+49",
	 					 GREATBRITAIN_CODE="+44",
	 					 HOLLAND_CODE="+31",
	 					 HUNGARY_CODE="+36",
	 					 IRELAND_CODE="+353",
	 					 ITALY_CODE = "+39",
	 					 JAPAN_CODE="+81",
	 					 LUXEMBOURG_CODE="+352",
	 					 NORWAY_CODE="+47",
	 					 POLAND_CODE="+48",
	 					 PORTUGAL_CODE="+351",
	 					 RUSSIA_CODE="+7",
	 					 SLOVAKIA_CODE="+421",
	 					 SPAIN_CODE="+34",
	 					 SWEDEN_CODE="+46",
	 					 SWITZERLAND_CODE = "+41",
	 					 TURKEY_CODE="+80",
	 					 UKRAINE_CODE="+380",
	 					 USA_CODE="+1";

	static HashMap mobileMap;

	/**
	 * Constructs a PhoneNumber with a special type
	 * @param number Phone number
	 * @param type Type of number
	 */
	public PhoneNumber(String number, String type) {
		this.type = type;
		//if (number.matches(numberMatcher)) this.number = number;
		this.number = number;
		createMobileMap();
		refactorNumber();
	}

	/**
	 * Constructs a PhoneNumber withput a type
	 *
	 * @param fullNumber
	 */
	public PhoneNumber(String fullNumber) {
		this(fullNumber, "");//$NON-NLS-1$
	}
	/**
	 * This constructor should be used if the number
	 *  may be a quickdial and needs to be resolved!
	 *
	 * @author Brian Jensen
	 *
	 * @param fullNumber the telephone number in raw format
	 * @param jfritz
	 * @param parseDialOut, a boolean value representing if a Dial out prefix needs to be parsed
	 */
	public PhoneNumber(String fullNumber, JFritz jfritz, boolean parseDialOut) {
		this.type = "";
		//if (number.matches(numberMatcher)) this.number = fullNumber;
		if (fullNumber != null && !fullNumber.equals("Unbekannt")) this.number = fullNumber;

		if (parseDialOut && this.number.startsWith(JFritz.getProperty(
        		"dial.prefix", " ")) ){
        	this.number = number.substring(JFritz.getProperty("dial.prefix").length());
        	Debug.msg("Parsed the dial out prefix, new number: "+this.number);
		}
		createMobileMap();
		refactorNumber(jfritz);
	}

	/**
	 * Sets number to this value
	 * @param number Number to be set
	 */
	public void setNumber(String number) {
		if (number.matches(numberMatcher)) this.number = number;
		refactorNumber();
	}

	/**
	 * Creates a map of german cellphone providers
	 */
	private void createMobileMap() {
		if (mobileMap == null) {
			mobileMap = new HashMap();
			mobileMap.put("+49151", "D1");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49160", "D1");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49170", "D1");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49171", "D1");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49175", "D1");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49152", "D2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49162", "D2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49172", "D2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49173", "D2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49174", "D2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49163", "E+");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49177", "E+");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49178", "E+");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49159", "O2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49176", "O2");//$NON-NLS-1$, //$NON-NLS-2$
			mobileMap.put("+49179", "O2");//$NON-NLS-1$, //$NON-NLS-2$
		}
	}

	/**
	 * Removes whitespaces, ) and ( from number
	 *
	 */
	private void removeUnnecessaryChars() {
        number = number.replaceAll(" ","");   //$NON-NLS-1$, //$NON-NLS-2$
	    number = number.replaceAll("\\(0","");//$NON-NLS-1$, //$NON-NLS-2$
	    number = number.replaceAll("\\(",""); //$NON-NLS-1$, //$NON-NLS-2$
	    number = number.replaceAll("\\)",""); //$NON-NLS-1$, //$NON-NLS-2$
	}

	/**
	 * Cuts call by call provider and converts to international number
	 *
	 */
	public void refactorNumber() {
	    removeUnnecessaryChars();
	    cutCallByCall();
		number = convertToIntNumber();

	}

	/**
	 * Method cuts unnecessary characters from the number, resolves quickdials, and
	 * cuts the call by from the number
	 *
	 * @author Brian Jensen
	 *
	 * @param jf, a referenz to the current jfritz instance
	 */
	public void refactorNumber(JFritz jf) {
	    removeUnnecessaryChars();
		convertQuickDial(jf);

		/* Part of the i18n work, don't delete
		if(JFritz.getProperty("country.code","49").equals("41")){
	    	callbycall = PhoneNumberSwitzerland.getCallbyCall(number);
	    	number = number.substring(callbycall.length());
	    	number  = convertToIntNumber();
	    }else{
	    	cutCallByCall();
	    	number = convertToIntNumber();
	    }*/

	    cutCallByCall();
    	number = convertToIntNumber();

	}

	/**
	 * Cuts call by call part of number
	 * @return Number withour call by call part
	 */
	private void cutCallByCall() {
		if (number.startsWith("0100")) {//$NON-NLS-1$
			// cut 0100yy (y = 0..9)
			callbycall = number.substring(0,6);
			number = number.substring(6);
		} else if (number.startsWith("010")) {//$NON-NLS-1$
			// cut 010xx (x = 1..9, y = 0..9)
			callbycall = number.substring(0,5);
			number = number.substring(5);
		}
	}

	/**
	 * Converts number to international number
	 *  Internation numbers have the following format in jfritz
	 *  (+)(Country Code)(Area Code)(Local number)
	 *
	 *
	 * @TODO: This function may need to be redone, if number parsing is misbehaving
	 *
	 * @return Returns internationalized number
	 */
	public String convertToIntNumber() {
		String countryCode = JFritz.getProperty("country.code");//$NON-NLS-1$
		String countryPrefix = JFritz.getProperty("country.prefix");//$NON-NLS-1$
		String areaCode = JFritz.getProperty("area.code");//$NON-NLS-1$
		String areaPrefix = JFritz.getProperty("area.prefix");//$NON-NLS-1$



		if ((number.length() < 3) // A valid number??
				|| (number.startsWith("+"))//$NON-NLS-1$
				// International number
				|| isSIPNumber() // SIP Number
				|| isEmergencyCall() // Emergency
				|| isQuickDial() // FritzBox QuickDial
		) {
			return number;
		}else	if (number.startsWith(countryPrefix))  // International call
			return "+" + number.substring(countryPrefix.length());//$NON-NLS-1$

		else if (number.startsWith(areaPrefix))
			return countryCode + number.substring(areaPrefix.length());//$NON-NLS-1$


		else if (number.startsWith(countryCode.substring(1)) && number.length() > 7)
			// International numbers without countryPrefix
			return "+" + number;//$NON-NLS-1$

		//if its not any internationl call, or a national call (in germany you can't dial
		// a national number using the internation prefix), then its a local call
		return countryCode + areaCode + number;//$NON-NLS-1$
	}

	/**
	 * Converts number to national number, if it is a national one.
	 * @return Returns nationalized number if country code matches, otherwise returns (unchanged) international number.
	 * @author Benjamin Schmitt
	 */
	public String convertToNationalNumber()
	{
		String countryCode = JFritz.getProperty("country.code","49");//$NON-NLS-1$, //$NON-NLS-2$
		String areaPrefix = JFritz.getProperty("area.prefix","0");   //$NON-NLS-1$, //$NON-NLS-2$

		if (number.startsWith(countryCode)) //$NON-NLS-1$
    		return areaPrefix + number.substring(3);

		Debug.msg("PhoneNumber.convertToNationalNumber: this is no national number, returning unchanged (international) number"); //$NON-NLS-1$
		return number;
	}

	/**
	 * Converts number to string representation
	 */
	public String toString() {
		return getIntNumber();
	}

	/**
	 *
	 * @return the international number
	 */
	public String getIntNumber() {
		if(number.startsWith("*"))
			return JFritzUtils.convertSpecialChars(number);
		else
			return number;
	}

	/**
	 *
	 * @return the number with call by call predial
	 */
	public String getFullNumber() {
		return callbycall + number;
	}

	public String getShortNumber() {
		String countryCode = JFritz.getProperty("country.code", "+49");//$NON-NLS-1$
		String areaCode = JFritz.getProperty("area.code"); 		//$NON-NLS-1$
		String areaPrefix = JFritz.getProperty("area.prefix");  //$NON-NLS-1$
		if (number.startsWith(countryCode + areaCode)) //$NON-NLS-1$
			return number.substring(countryCode.length() + areaCode.length());

		else if (number.startsWith(countryCode)) //$NON-NLS-1$
			return areaPrefix + number.substring(countryCode.length());
		return number;
	}

	public String getAreaNumber() {
		String countryCode = JFritz.getProperty("country.code", "+49"); //$NON-NLS-1$
		String areaPrefix = JFritz.getProperty("area.prefix", "0"); //$NON-NLS-1$
		if (number.startsWith(countryCode)) //$NON-NLS-1$
			return areaPrefix + number.substring(countryCode.length());
		return number;
	}

	/**
	 * @return CallByCall predial number
	 */
	public String getCallByCall() {
		return callbycall;
	}

	/**
	 * @param callbycall The callbycall to set.
	 */
	public void setCallByCall(String callbycall) {
		this.callbycall = callbycall;
	}

	/**
	 * @return True if number has a Vorvorwahl (like 01013)
	 */
	public boolean hasCallByCall() {
		return (callbycall.length() > 0);
	}

	/**
	 * @return True if number is a FreeCall number
	 */
	public boolean isFreeCall() {
		boolean ret = number.startsWith("0800"); //$NON-NLS-1$
		if (ret && getType().equals("")) //$NON-NLS-1$
			type = "business"; //$NON-NLS-1$
		return ret;
	}

	/**
	 * @return True if number is a local number
	 */
	public boolean isLocalCall() {
		String countryCode = JFritz.getProperty("country.code"); //$NON-NLS-1$
		String areaCode = JFritz.getProperty("area.code"); //$NON-NLS-1$
		return number.startsWith(countryCode + areaCode); //$NON-NLS-1$
	}

	/**
	 * @return True if number is a SIP number
	 */
	public boolean isSIPNumber() {
		return ((number.indexOf('@') > 0) //$NON-NLS-1$
				// PurTel
				|| number.startsWith("00038")  //$NON-NLS-1$
				// SIPGate
				|| number.startsWith("555")  //$NON-NLS-1$
				// SIPGate
		|| number.startsWith("777") //$NON-NLS-1$
		);
	}

	/**
	 * @return True if number is a short quickdial number
	 */
	public boolean isQuickDial() {
	    if (number.startsWith("**7") || number.length() < 3) { //$NON-NLS-1$
	        return true;
	    } else {
	        return false;
	    }
	}

	/**
	 * @return True if number is an emergency number
	 */
	public boolean isEmergencyCall() {
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

	/**
	 *
	 * @return Country code (49 for Germany, 41 for Switzerland)
	 */
	public String getCountryCode() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * @return Area code
	 */
	public String getAreaCode() {
		return ""; //$NON-NLS-1$
	}

	/**
	 *
	 * @return Local part of number
	 */
	public String getLocalPart() {
		return ""; //$NON-NLS-1$
	}

	/**
	 *
	 * @return Returns mobile provider
	 */
	public String getMobileProvider() {
		if (number.length() < 5)
			return ""; //$NON-NLS-1$
		Object provider = mobileMap.get(number.substring(0, 6));
		if (provider == null)
			return ""; //$NON-NLS-1$
		return mobileMap.get(number.substring(0, 6)).toString();
	}

	/**
	 * @return True if number is a mobile one
	 */
	public boolean isMobile() {
		//		String provider = ReverseLookup.getMobileProvider(getFullNumber());
		//		return (!provider.equals(""));
		if(number.startsWith("+"+SWITZERLAND_CODE) && JFritz.getProperty("country.code", "49").equals(SWITZERLAND_CODE))
			return PhoneNumberSwitzerland.isMobile(getAreaNumber());
		else{
			boolean ret = number.length() > 6
				&& mobileMap.containsKey(number.substring(0, 6));
			if (ret && getType().equals("")) //$NON-NLS-1$
				type = "mobile"; //$NON-NLS-1$
			return ret;
		}

	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		if (arg0.getClass().equals(this.getClass())) {

		}
		return 0;
	}

	/**
	 * @return Returns the type.
	 */
	public final String getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public final void setType(String type) {
		this.type = type;
	}

	/**
	 * Auto-Set type
	 */
	public final void setType() {
		if (isMobile())
			type = "mobile"; //$NON-NLS-1$
		else if (isFreeCall())
			type = "business"; //$NON-NLS-1$
		else if (isSIPNumber())
			type = "sip"; //$NON-NLS-1$
		else
			type = "home"; //$NON-NLS-1$
	}

	/**
	 * This function resolves incoming Quickdials to their appropriate
	 * full number, if one is not found the Quickdial is left unchanged
	 *
	 * @author Brian Jensen
	 *
	 *
	 * @param jf is a referenz to the current jfritz instance
	 */
	public void convertQuickDial(JFritz jf){

		if (number.startsWith("**7")) //$NON-NLS-1$
        	// QuickDial
        {
            Debug.msg("Quickdial: " + number //$NON-NLS-1$
                    + ", searching for the full number"); //$NON-NLS-1$

          	// replace QuickDial with
            // QuickDial-Entry
            String quickDialNumber = number.substring(3, 5);
            Debug.msg("Quickdail number: "+quickDialNumber);

            if (jf.getJframe().getQuickDialPanel().getDataModel()
                    .getQuickDials().size() == 0) {

            	// get QuickDials from FritzBox
            	Debug.msg("No Quickdials present in JFritz, retrieving the list from the box");
            	jf.getJframe().getQuickDialPanel().getDataModel()
            	.getQuickDialDataFromFritzBox();
            }
            Enumeration en = jf.getJframe().getQuickDialPanel()
            	.getDataModel().getQuickDials().elements();
            while (en.hasMoreElements()) {
            	QuickDial quickDial = (QuickDial) en.nextElement();
            	if (quickDialNumber.equals(quickDial.getQuickdial())) {
            		number = quickDial.getNumber();
            		Debug.msg("Quickdial resolved. Number: " //$NON-NLS-1$
            				+ number.toString());
            	}
            }

            if(number.startsWith("**7"))
            	Debug.msg("No quickdial found. Refresh your quickdial list"); //$NON-NLS-1$

        }
	}

}

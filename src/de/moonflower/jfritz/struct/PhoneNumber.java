/*
 * Created on 01.06.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.CallByCall;
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

	// Please keep these in alphabetical order!!!
	public static final String AUSTRIA_CODE = "+43", BELGIUM_CODE = "+32",
			CHINA_CODE = "+86", CZECH_CODE = "+420", DENMARK_CODE = "+45",
			FINLAND_CODE = "+358", FRANCE_CODE = "+33", GERMANY_CODE = "+49",
			GREATBRITAIN_CODE = "+44", HOLLAND_CODE = "+31",
			HUNGARY_CODE = "+36", IRELAND_CODE = "+353", ITALY_CODE = "+39",
			JAPAN_CODE = "+81", LUXEMBOURG_CODE = "+352", NORWAY_CODE = "+47",
			POLAND_CODE = "+48", PORTUGAL_CODE = "+351", RUSSIA_CODE = "+7",
			SLOVAKIA_CODE = "+421", SPAIN_CODE = "+34", SWEDEN_CODE = "+46",
			SWITZERLAND_CODE = "+41", TURKEY_CODE = "+90",
			UKRAINE_CODE = "+380", USA_CODE = "+1", INT_FREECALL = "+800";

	static HashMap<String, String> mobileMap;

	static HashMap<String, String> worldFlagMap;

	static HashMap<String, CallByCall[]> callbyCallMap;

	static String FLAG_FILE_HEADER = "Country Code;Flag file; Full Text";

	static String CBC_FILE_HEADER  = "Country Code;CallbyCall Prefix:length";

	private String flagFileName = "";

	private String Description = "";



	/**
	 * @deprecated use the other constructor from now on
	 *
	 * Constructs a PhoneNumber with a special type
	 * @param number
	 *            Phone number
	 * @param type
	 *            Type of number
	 */
	public PhoneNumber(String number, String type) {
		this.type = type;
		// if (number.matches(numberMatcher)) this.number = number;
		this.number = number;
		//createMobileMap();
		refactorNumber();
	}

	/**
	 * Constructs a PhoneNumber without a type
	 *
	 * @param fullNumber
	 */
	public PhoneNumber(String fullNumber) {
		this(fullNumber, "");//$NON-NLS-1$
	}

	/**
	 * This constructor should be used if the number may be a quickdial and
	 * needs to be resolved!
	 *
	 * @author Brian Jensen
	 *
	 * @param fullNumber
	 *            the telephone number in raw format
	 * @param parseDialOut,
	 *            a boolean value representing if a Dial out prefix needs to be
	 *            parsed
	 */
	public PhoneNumber(String fullNumber, boolean parseDialOut) {
		this.type = "";
		// if (number.matches(numberMatcher)) this.number = fullNumber;
		if (fullNumber != null
				&& !fullNumber.equals(Main.getMessage("unknown"))) {
			this.number = fullNumber;
		}
		if (parseDialOut
				&& this.number.startsWith(Main.getProperty("dial.prefix", " "))) {
			this.number = number.substring(Main.getProperty("dial.prefix")
					.length());
			Debug.msg("Parsed the dial out prefix, new number: " + this.number);
		}

			//createMobileMap();
			refactorNumber();
	}

	/**
	 * Sets number to this value
	 *
	 * @param number
	 *            Number to be set
	 */
	public void setNumber(String number) {
		if (number.matches(numberMatcher))
			this.number = number;
		refactorNumber();
	}

	/**@deprecated completely unnecessary with the new i18n scheme
	 *
	 * Creates a map of german cellphone providers
	 */
	private void createMobileMap() {
		if (mobileMap == null) {
			mobileMap = new HashMap<String, String>();
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
			mobileMap.put("+49157", "E+");//$NON-NLS-1$, //$NON-NLS-2$
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
		number = number.replaceAll(" ", ""); //$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\(0", "");//$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\(", ""); //$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\)", ""); //$NON-NLS-1$, //$NON-NLS-2$
	}

	/**
	 * Method cuts unnecessary characters from the number, resolves quickdials,
	 * cuts the call by call from the number and then processes the country info
	 *
	 * @author Brian Jensen
	 *
	 */
	public void refactorNumber() {
		removeUnnecessaryChars();
		convertQuickDial();
		cutCallByCall();
		number = convertToIntNumber();
		getCountryInfo();

	}

	/**
	 * This function does two things. First it searches for the number in the worldFlagMap,
	 * so it can determine the correct flag to display. Then it determines if the number is
	 * part of a mobile network for this particular country.
	 *
	 * @author brian jensen
	 *
	 */

	private void getCountryInfo(){
		String[] value;

		if(worldFlagMap != null){
			if(worldFlagMap.containsKey(number.substring(1,6))){
				value = worldFlagMap.get(number.substring(1,6)).split(";");
				flagFileName = value[0];
				Description = value[1];
			}else if(worldFlagMap.containsKey(number.substring(1,5))){
				value = worldFlagMap.get(number.substring(1,5)).split(";");
				flagFileName = value[0];
				Description = value[1];
			}else if(worldFlagMap.containsKey(number.substring(1,4))){
				value = worldFlagMap.get(number.substring(1,4)).split(";");
				flagFileName = value[0];
				Description = value[1];
			}else if(worldFlagMap.containsKey(number.substring(1,3))){
				value = worldFlagMap.get(number.substring(1,3)).split(";");
				flagFileName = value[0];
				Description = value[1];
			}else if(worldFlagMap.containsKey(number.substring(1,2))){
				value = worldFlagMap.get(number.substring(1,2)).split(";");
				flagFileName = value[0];
				Description = value[1];
			}else if(worldFlagMap.containsKey(number.substring(1,1))){
				value = worldFlagMap.get(number.substring(1,1)).split(";");
				flagFileName = value[0];
				Description = value[1];
			}else{
				Debug.msg("No flag file for "+number+" found!!");
			}

			//All known mobile numbers are marked in the csv
			if(Description.contains("Mobile"))
				type = "mobile";
			else
				type = "home";

		}
	}

	/**
	 * Used by NumberCellRenderer
	 *
	 * @return flag file name
	 */
	public String getFlagFileName(){
		return flagFileName;
	}

	/**
	 * Used by NumberCellRenderer
	 *
	 * @return description of country and / or phone network
	 */
	public String getDescription(){
		return Description;
	}

	/**
	 * i18n version
	 *
	 * This funtction cuts the call by call from the number as determined
	 * by the parameters loaded from number/internation/callbycall_world.csv
	 *
	 */
	private void cutCallByCall(){
		String countryCode = Main.getProperty("country.code");//$NON-NLS-1$
		CallByCall[] cbc;

		if(callbyCallMap.containsKey(countryCode)){
			cbc = callbyCallMap.get(countryCode);
			for(int i = 0; i < cbc.length; i++){
				if(number.startsWith(cbc[i].getPrefix())){
					//This is just for testing, will be removed soon
					Debug.msg("Number parsed using prefix: "+cbc[i].getPrefix());
					callbycall = number.substring(0, cbc[i].getLength());
					number = number.substring(cbc[i].getLength());
					break;
				}
			}
		} else {
			Debug.msg("No Call by Call prefix information for "+countryCode+" found.");
		}


	}


	/**Disabled, Brian
	 *
	 *
	 * TODO: This is the last part of PhoneNumber that needs to be
	 * internationalised
	 *
	 * Cuts call by call part of number
	 *
	 * @return Number withour call by call part
	 *
	private void cutCallByCall() {
		if (number.startsWith("0100")) {//$NON-NLS-1$
			// cut 0100yy (y = 0..9)
			callbycall = number.substring(0, 6);
			number = number.substring(6);
		} else if (number.startsWith("010")) {//$NON-NLS-1$
			// cut 010xx (x = 1..9, y = 0..9)
			callbycall = number.substring(0, 5);
			number = number.substring(5);
		}
	}
	*/

	/**
	 * Converts number to international number Internation numbers have the
	 * following format in jfritz (+)(Country Code)(Area Code)(Local number)
	 *
	 *
	 * @TODO: This function may need to be redone, if number parsing is
	 *        misbehaving
	 *
	 * @return Returns internationalized number
	 */
	public String convertToIntNumber() {
		// do nothing if number is an "unknown" number
		if (number.equals(Main.getMessage("unknown"))) {
			return number;
		}

		String countryCode = Main.getProperty("country.code");//$NON-NLS-1$
		String countryPrefix = Main.getProperty("country.prefix");//$NON-NLS-1$
		String areaCode = Main.getProperty("area.code");//$NON-NLS-1$
		String areaPrefix = Main.getProperty("area.prefix");//$NON-NLS-1$

		if ((number.length() < 3) // A valid number??
				|| (number.startsWith("+"))//$NON-NLS-1$
				// International number
				|| isSIPNumber() // SIP Number
				|| isEmergencyCall() // Emergency
				|| isQuickDial() // FritzBox QuickDial
		) {
			return number;
		} else if (number.startsWith(countryPrefix)) // International call
			return "+" + number.substring(countryPrefix.length());//$NON-NLS-1$

		else if (number.startsWith(areaPrefix))
			return countryCode + number.substring(areaPrefix.length());//$NON-NLS-1$

		/*
		 * this case should never happen!!! else if
		 * (number.startsWith(countryCode.substring(1)) && number.length() > 7) //
		 * International numbers without countryPrefix return "+" +
		 * number;//$NON-NLS-1$
		 */

		// if its not any internationl call, or a national call (in germany you
		// can't dial
		// a national number using the international prefix), then its a local
		// call
		return countryCode + areaCode + number;//$NON-NLS-1$
	}

	/**
	 * Converts number to national number, if it is a national one.
	 *
	 * @return Returns nationalized number if country code matches, otherwise
	 *         returns (unchanged) international number.
	 * @author Benjamin Schmitt
	 */
	public String convertToNationalNumber() {
		String countryCode = Main.getProperty("country.code", "+49");//$NON-NLS-1$, //$NON-NLS-2$
		String areaPrefix = Main.getProperty("area.prefix", "0"); //$NON-NLS-1$, //$NON-NLS-2$

		if (number.startsWith(countryCode)) //$NON-NLS-1$
			return areaPrefix + number.substring(3);

		Debug
				.msg("PhoneNumber.convertToNationalNumber: this is no national number, returning unchanged (international) number"); //$NON-NLS-1$
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
		if (number.startsWith("*"))
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
		String countryCode = Main.getProperty("country.code", "+49");//$NON-NLS-1$
		String areaCode = Main.getProperty("area.code"); //$NON-NLS-1$
		String areaPrefix = Main.getProperty("area.prefix"); //$NON-NLS-1$
		if (number.startsWith(countryCode + areaCode)) //$NON-NLS-1$
			return number.substring(countryCode.length() + areaCode.length());

		else if (number.startsWith(countryCode)) //$NON-NLS-1$
			return areaPrefix + number.substring(countryCode.length());
		return number;
	}

	public String getAreaNumber() {
		String countryCode = Main.getProperty("country.code", "+49"); //$NON-NLS-1$
		String areaPrefix = Main.getProperty("area.prefix", "0"); //$NON-NLS-1$
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
	 * @param callbycall
	 *            The callbycall to set.
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
		boolean ret = number.startsWith("0800") || number.startsWith(INT_FREECALL); //$NON-NLS-1$
		if (ret && getType().equals("")) //$NON-NLS-1$
			type = "business"; //$NON-NLS-1$
		return ret;
	}

	/**
	 * @return True if number is a local number
	 */
	public boolean isLocalCall() {
		String countryCode = Main.getProperty("country.code"); //$NON-NLS-1$
		String areaCode = Main.getProperty("area.code"); //$NON-NLS-1$
		return number.startsWith(countryCode + areaCode); //$NON-NLS-1$
	}

	/**
	 * @return True if number is a SIP number
	 */
	public boolean isSIPNumber() {
		return ((number.indexOf('@') > 0) //$NON-NLS-1$
				// PurTel
				|| number.startsWith("00038") //$NON-NLS-1$
				// SIPGate
				|| number.startsWith("555") //$NON-NLS-1$
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
	 * This function determines if the phone number is part of a mobile network
	 * NOTE: This attribute is determined upon creation of the object
	 *
	 * @author brian jensen
	 *
	 * @return if number is a part of a mobile network
	 */

	public boolean isMobile(){
		if(type.equals("mobile"))
			return true;
		else
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

	/**@deprecated don't use it anymore!
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

	/** Disabled, Brian
	 * @return True if number is a mobile one

	public boolean isMobile() {
		// String provider = ReverseLookup.getMobileProvider(getFullNumber());
		// return (!provider.equals(""));
		if (number.startsWith("+" + SWITZERLAND_CODE)
				&& Main.getProperty("country.code", "+49").equals(
						SWITZERLAND_CODE))
			return PhoneNumberSwitzerland.isMobile(getAreaNumber());
		else {
			boolean ret = number.length() > 6
					&& mobileMap.containsKey(number.substring(0, 6));
			if (ret && getType().equals("")) //$NON-NLS-1$
				type = "mobile"; //$NON-NLS-1$
			return ret;
		}

	}
	*/


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
	 * This function resolves incoming Quickdials to their appropriate full
	 * number, if one is not found the Quickdial is left unchanged
	 *
	 * @author Brian Jensen
	 *
	 */
	public void convertQuickDial() {

		if (number.startsWith("**7")) //$NON-NLS-1$
		// QuickDial
		{
			Debug.msg("Quickdial: " + number //$NON-NLS-1$
					+ ", searching for the full number"); //$NON-NLS-1$

			// replace QuickDial with
			// QuickDial-Entry
			String quickDialNumber = number.substring(3, 5);
			Debug.msg("Quickdail number: " + quickDialNumber);

			if (JFritz.getQuickDials()
					.getQuickDials().size() == 0) {

				// get QuickDials from FritzBox
				Debug
						.msg("No Quickdials present in JFritz, retrieving the list from the box");
				JFritz.getQuickDials()
						.getQuickDialDataFromFritzBox();
			}
			Enumeration en = JFritz.getQuickDials().getQuickDials().elements();
			while (en.hasMoreElements()) {
				QuickDial quickDial = (QuickDial) en.nextElement();
				if (quickDialNumber.equals(quickDial.getQuickdial())) {
					number = quickDial.getNumber();
					Debug.msg("Quickdial resolved. Number: " //$NON-NLS-1$
							+ number);
				}
			}

			if (number.startsWith("**7"))
				Debug.msg("No quickdial found. Refresh your quickdial list"); //$NON-NLS-1$

		}
	}

	public boolean equals(Object number) {
		if (!(number instanceof PhoneNumber)) {
			return false;
		}
		if (this.getIntNumber().equals(((PhoneNumber) number).getIntNumber()))
			return true;
		else
			return false;
	}

	/**
	 * This function loads all the info from the file number/country_codes_world.csv
	 * and stores the information in a hashmap indexed by country code
	 *
	 * worldFlagMap contains information about the country and has the name of
	 * a flag to display for that country
	 *
	 * @author brian
	 *
	 */
	public static void loadFlagMap(){
		Debug.msg("Loading the country code -> flag map");
		worldFlagMap = new HashMap<String, String>(2200);
		BufferedReader br = null;
		FileInputStream fi = null;

		try{
			fi = new FileInputStream(JFritzUtils.getFullPath("/number") +"/international/country_codes_world.csv");
			br = new BufferedReader(new InputStreamReader(fi, "ISO-8859-1"));

			String line;
			String[] entries;
			int lines = 0;
			String l = br.readLine();
			if(l==null){
				Debug.errDlg("File "+JFritzUtils.getFullPath("/number") +"/international/country_codes_world"+" empty");
			}
			//Load the keys and values quick and dirty
			if(l.equals(FLAG_FILE_HEADER)){
				while (null != (line = br.readLine())) {
					lines++;
					entries = line.split(";");
					if(entries.length == 3)
						//country code is the key, flag name; Description is the value
						worldFlagMap.put(entries[0], entries[1]+";"+entries[2]);
				}
			}

			Debug.msg(lines + " Lines read from country_codes_world.csv");
			Debug.msg("worldFlagMap size: "+worldFlagMap.size());

		}catch(Exception e){
			Debug.msg(e.toString());
		}finally{
			try{
				if(fi!=null)
					fi.close();
				if(br!=null)
					br.close();
			}catch (IOException ioe){
				Debug.msg("error closing stream"+ioe.toString());
			}
		}

	}

	/**
	 * his function loads all the info from the file number/callbycall_world.csv
	 * and stores the information in a hashmap indexed by country code
	 *
	 * callbyCallMap contains the information about callbycall providers used to
	 * process that information from numbers retrieved by jfritz
	 *
	 * @author brian
	 *
	 */
	public static void loadCallbyCallMap(){
		Debug.msg("Loading the country code -> Call by Call Map");
		//reserve space internally for future updates
		callbyCallMap = new HashMap<String, CallByCall[]>(20);
		BufferedReader br = null;
		FileInputStream fi = null;

		try{
			fi = new FileInputStream(JFritzUtils.getFullPath("/number") +"/international/callbycall_world.csv");
			br = new BufferedReader(new InputStreamReader(fi, "ISO-8859-1"));

			String line;
			String[] entries, elements, details;
			CallByCall[] cbc;
			int lines = 0;
			String l = br.readLine();
			if(l==null){
				Debug.errDlg("File "+JFritzUtils.getFullPath("/number") +"/international/callbycall_world.csv"+" empty");
			}
			//Load the keys and values quick and dirty
			if(l.equals(CBC_FILE_HEADER)){
				while (null != (line = br.readLine())) {
					lines++;
					entries = line.split(";");

					//These are the two field entries
					if(entries.length == 2){
						elements = entries[1].split(" ");
						cbc = new CallByCall[elements.length];

						//These are the different cbc:lenth elements
						for(int i = 0; i < elements.length; i++){
							details = elements[i].split(":");

							//make sure each entry is correctly formed
							if(details.length == 2){
								Debug.msg("Call by Call for +"+entries[0]+" added. Prefix: "+
										details[0]+" Length: "+details[1]);

								cbc[i] = new CallByCall(details[0], new Integer(details[1]));
							}
						}

						//country code is the key, CallByCall[] Array is the object
						callbyCallMap.put("+"+entries[0], cbc);

					}
				}
			}

			Debug.msg(lines + " Lines read from callbycall_world.csv");
			Debug.msg("callbyCallMap size: "+callbyCallMap.size());

		}catch(Exception e){
			Debug.msg(e.toString());
		}finally{
			try{
				if(fi!=null)
					fi.close();
				if(br!=null)
					br.close();
			}catch (IOException ioe){
				Debug.msg("error closing stream"+ioe.toString());
			}
		}

	}

}

/*
 * Created on 01.06.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.CallByCall;
import de.moonflower.jfritz.utils.CbCFileXMLHandler;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Arno Willig
 *
 */
public class PhoneNumber implements Comparable, Serializable {

	private static final long serialVersionUID = 102;

	private String numberMatcher = "([0-9]|\\+|\\(|\\)| |-|/)+|\\**";//$NON-NLS-1$

	private String number = "";//$NON-NLS-1$

	private String callbycall = "";//$NON-NLS-1$

	private String type = "";//$NON-NLS-1$

	// type values : "home", "mobile", "homezone",
	// "business", "other", "fax", "sip" };

	// Please keep these in alphabetical order!!!
	public static final String INT_FREECALL = "+800";

	static HashMap<String, String> mobileMap;

	static HashMap<String, String> worldFlagMap;
	static HashMap<String, String> specificWorldFlagMap;

	static HashMap<String, CallByCall[]> callbyCallMap;

	static String FLAG_FILE_HEADER = "Country Code;Flag file; Full Text";

	static String CBC_FILE_HEADER  = "Country Code;CallbyCall Prefix:length";

	private String flagFileName = "";

	private String country = "";

	private String Description = "";

	private String countryCode = "";

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
		refactorNumber();
	}

	/**
	 * @deprecated don't use anymore
	 *
	 * Constructs a PhoneNumber without a type
	 *
	 * @param fullNumber
	 */
	public PhoneNumber(String fullNumber) {
		this(fullNumber, "");//$NON-NLS-1$
	}

	/**
	 * This constructor should be used if the number may be a quickdial and
	 * needs to be resolved! Or if the number starts with a dial out prefix
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

		if (fullNumber != null) {
			this.number = fullNumber;
		}

		if (parseDialOut
				&& this.number.startsWith(Main.getProperty("dial.prefix"))) {
			this.number = number.substring(Main.getProperty("dial.prefix")
					.length());
			Debug.msg("Parsed the dial out prefix, new number: " + this.number);
		}

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

	/**
	 * Removes whitespaces, ) and ( from number
	 *
	 */
	private void removeUnnecessaryChars() {
		number = number.replaceAll(" ", ""); //$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\(0", "");//$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\(", ""); //$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\)", ""); //$NON-NLS-1$, //$NON-NLS-2$
		number = number.replaceAll("\\/", ""); //$NON-NLS-1$, //$NON-NLS-2$
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

			// Finde Landeskennzahl
			for ( int i=6; i>0; i-- ) {
				if ( number.length()>i && worldFlagMap.containsKey(number.substring(1, i))) {
					value = worldFlagMap.get(number.substring(1,i)).split(";");
					countryCode = "+" + number.substring(1,i);
					flagFileName = value[0];
					country = value[1];
					Description = value[1];
					break;
				}
			}

			// Finde weitere Durchwahlen, wie z.B. Mobilfunkanbieter
			for ( int i=11; i>3; i-- ) {
				if ( number.length()>i && specificWorldFlagMap.containsKey(number.substring(1, i))) {
					value = specificWorldFlagMap.get(number.substring(1,i)).split(";");
					if ( countryCode.equals(Main.getProperty("country.code"))) {
						flagFileName = value[0];
					}
					Description = value[1];
					break;
				}
			}
			if ( countryCode.equals("+")) {
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
					callbycall = number.substring(0, cbc[i].getLength());
					number = number.substring(cbc[i].getLength());
					break;
				}
			}
		} else {

		}


	}

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
		String countryCode = Main.getProperty("country.code");//$NON-NLS-1$, //$NON-NLS-2$
		String areaPrefix = Main.getProperty("area.prefix"); //$NON-NLS-1$, //$NON-NLS-2$

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
		String countryCode = Main.getProperty("country.code");//$NON-NLS-1$
		String areaCode = Main.getProperty("area.code"); //$NON-NLS-1$
		String areaPrefix = Main.getProperty("area.prefix"); //$NON-NLS-1$
		if (number.startsWith(countryCode + areaCode)) //$NON-NLS-1$
			return number.substring(countryCode.length() + areaCode.length());

		else if (number.startsWith(countryCode)) //$NON-NLS-1$
			return areaPrefix + number.substring(countryCode.length());
		return number;
	}

	public String getAreaNumber() {
		String countryCode = Main.getProperty("country.code"); //$NON-NLS-1$
		String areaPrefix = Main.getProperty("area.prefix"); //$NON-NLS-1$
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
		boolean ret = number.startsWith("+49800") || number.startsWith(INT_FREECALL); //$NON-NLS-1$
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

	public String getCountry() {
		return country;
	}

	/**
	 *
	 * @return Country code (49 for Germany, 41 for Switzerland)
	 */
	public String getCountryCode() {
		return countryCode; //$NON-NLS-1$
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
				try {
					JFritz.getQuickDials()
							.getQuickDialDataFromFritzBox();
				} catch (WrongPasswordException e1) {
					// just do nothing. No conversion possible
				} catch (IOException e1) {
					// just do nothing. No conversion possible
				} catch (InvalidFirmwareException e1) {
					// just do nothing. No conversion possible
				}
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
		loadSpecificFlagMap();
	}

	/**
	 * This function loads all the info from the file number/country_specfic_codes_world.csv
	 * and stores the information in a hashmap indexed by country code
	 *
	 * specificWorldFlagMap contains information about the country
	 * used provider and has the name of a flag to display for that country
	 *
	 * @author brian
	 *
	 */
	private static void loadSpecificFlagMap(){
		Debug.msg("Loading the country code -> flag map");
		specificWorldFlagMap = new HashMap<String, String>(2200);
		BufferedReader br = null;
		FileInputStream fi = null;

		try{
			fi = new FileInputStream(JFritzUtils.getFullPath("/number") +"/international/country_specfic_codes_world.csv");
			br = new BufferedReader(new InputStreamReader(fi, "ISO-8859-1"));

			String line;
			String[] entries;
			int lines = 0;
			String l = br.readLine();
			if(l==null){
				Debug.errDlg("File "+JFritzUtils.getFullPath("/number") +"/international/country_specfic_codes_world.csv"+" empty");
			}
			//Load the keys and values quick and dirty
			if(l.equals(FLAG_FILE_HEADER)){
				while (null != (line = br.readLine())) {
					lines++;
					entries = line.split(";");
					if(entries.length == 3)
						//country code is the key, flag name; Description is the value
						specificWorldFlagMap.put(entries[0], entries[1]+";"+entries[2]);
				}
			}

			Debug.msg(lines + " Lines read from country_specfic_codes_world.csv");
			Debug.msg("specificWorldFlagMap size: "+specificWorldFlagMap.size());

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

	/** This function loads the file number/international/callbycall_world.xml
	 * It starts by initializing the callbyCallMap and then proceeds to setup
	 * the sax parser
	 *
	 * @author brian
	 *
	 */
	public static void loadCbCXMLFile(){
		try {
			Debug.msg("Loading the call by call xml file");
			callbyCallMap = new HashMap<String, CallByCall[]>(20);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();

			reader.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}

				public void fatalError(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}

				public void warning(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}
			});

			reader.setContentHandler(new CbCFileXMLHandler());
			reader.parse(new InputSource(new FileInputStream(
					JFritzUtils.getFullPath("/number") +"/international/callbycall_world.xml")));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing number/internation/callbycall_world.xml! No call by calls loaded!"); //$NON-NLS-1$,  //$NON-NLS-2$
			Debug.err(e.toString());
			e.printStackTrace();

			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.err(e.getLocalizedMessage());
			}
		} catch (IOException e) {
			Debug.err("Could not read number/international/callbycall_world.xml! No call by calls loaded!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	/**
	 * This function is used by the xml handler to add entries to the list
	 *
	 * @param countryCode
	 * @param cbc_list
	 */
	public static void addCallbyCall(String countryCode, CallByCall[] cbc_list){
		callbyCallMap.put(countryCode, cbc_list);
	}

	public String toCSV(){
		return type+"="+number;
	}

}

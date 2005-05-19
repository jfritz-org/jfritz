/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static class for data retrieval from the fritz box
 *
 * TODO: This class needs to be abstracted, so that subclasses for each boxtype
 * can be implemented.
 *
 * @author akw
 *
 */
public class JFritzUtils {

	final static String POSTDATA_QUICKDIAL_FRITZBOX_7050 = "getpage=../html/de/menus/menu2.html"
			+ "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=kurzwahlen&login%3Acommand%2Fpassword=";

	final static String POSTDATA_LIST_FRITZBOX_7050 = "getpage=../html/de/menus/menu2.html"
			+ "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=foncalls&login%3Acommand%2Fpassword=";

	final static String POSTDATA_LIST_FRITZBOX_FON_WLAN = "getpage=../html/menus/menu2.html"
			+ "&var%3Apagename=foncalls&var%3Amenu=fon&login%3Acommand%2Fpassword=";

	// FIXME for 7050
	final static String POSTDATA_CLEAR_FRITZBOX_7050 = "getpage=../html/de/menus/menu2.html"
			+ "&var%3Apagename=foncalls&var%3Amenu=fon&telcfg%3Asettings/ClearJournal=1";

	final static String PATTERN_FRITZBOX_FON_WLAN = "<tr class=\"Dialoglist\">"
			+ " <td class=\"c1\"><script type=\"text/javascript\">document.write\\(uiCallSymbol\\(\"(\\d)\"\\)\\);</script></td>"
			+ " <td class=\"c3\">(\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d)</td>"
			+ " <td class=\"c4\"><script type=\"text/javascript\">document.write\\(uiRufnummerDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
			+ " <td class=\"c5\"><script type=\"text/javascript\">document.write\\(uiPortDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
			+ " <td class=\"c6\"><script type=\"text/javascript\">document.write\\(uiDauerDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
			+ " </tr>";

	final static String PATTERN_FRITZBOX_7050 = "<tr class=\"Dialoglist\">"
			+ " <td class=\"c1\"><script type=\"text/javascript\">document.write\\(uiCallSymbol\\(\"(\\d)\"\\)\\);</script></td>"
			+ " <td class=\"c3\">(\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d)</td>"
			+ " <td class=\"c4\"><script type=\"text/javascript\">document.write\\(uiRufnummerDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
			+ " <td class=\"c5\"><script type=\"text/javascript\">document.write\\(uiPortDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
			+ " <td class=\"c7\"><script type=\"text/javascript\">document.write\\(uiRouteDisplay\\(\"(\\w*)\"\\)\\);</script></td>"
			+ " <td class=\"c6\"><script type=\"text/javascript\">document.write\\(uiDauerDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
			+ " </tr>";

	final static String PATTERN_QUICKDIAL_FRITZBOX_7050 = "<tr class=\"Dialoglist\">"
			+ " <td style=\"text-align: center;\">(\\d*)</td>"
			+ " <td>(\\w*)</td>"
			+ " <td>([^<]*)</td>"
			+ " <td style=\"text-align: right;\"><button [^>]*> <img [^>]*></button></td>"
			+ " <td style=\"text-align: right;\"><button [^>]*> <img [^>]*></button></td>"
			+ " </tr>";

	final static String POSTDATA_CLEAR_FRITZBOX_FON_WLAN = "getpage=../html/menus/menu2.html"
			+ "&var%3Apagename=foncalls&var%3Amenu=fon&telcfg%3Asettings/ClearJournal=1";

	final static String POSTDATA_SIPPROVIDER = "getpage=../html/de/menus/menu2.html"
			+ "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=siplist&login%3Acommand%2Fpassword=";

	final static String PATTERN_SIPPROVIDER = "<!-- \"(\\d)\" / \"(\\w*)\" -->"
			+ " <td class=\"c1\"> <input type=checkbox id=\"uiViewActivsip\\d\""
			+ " onclick=\"uiOnChangeActivated\\('uiViewActivsip\\d','uiPostActivsip\\d'\\); return true;\">"
			+ " </td> <td class=\"c2\">(\\w*)</td>"
			+ " <td class=\"c3\"><script type=\"text/javascript\">document.write\\(ProviderDisplay\\(\"([^\"]*)\"\\)\\);</script></td>";

	/**
	 * Detects type of fritz box by detecting the firmware version
	 *
	 * @param box_address
	 * @return boxtype
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static byte detectBoxType(String firmware, String box_address,
			String box_password) throws WrongPasswordException, IOException {
		FritzBoxFirmware fw;
		try {
			fw = new FritzBoxFirmware(firmware);
		} catch (InvalidFirmwareException e) {
			fw = FritzBoxFirmware.detectFirmwareVersion(box_address,
					box_password);
			System.out.println("Found Firmware: " + fw + " (" + fw.getBoxName()
					+ ")");

		}

		// TODO: retrieveSipProvider(box_address,box_password,fw.getBoxType());
		return fw.getBoxType();
	}

	/**
	 * retrieves vector of caller data from the fritz box
	 *
	 * @param box_address
	 * @param password
	 * @param countryPrefix
	 * @param areaPrefix
	 * @param areaCode
	 * @return Vector of caller data
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static Vector retrieveCallersFromFritzBox(String box_address,
			String password, String countryPrefix, String countryCode,
			String areaPrefix, String areaCode, byte boxtype)
			throws WrongPasswordException, IOException {

		String postdata;
		if (boxtype == FritzBoxFirmware.BOXTYPE_FRITZBOX_7050) {
			postdata = POSTDATA_LIST_FRITZBOX_7050 + password;
		} else {
			postdata = POSTDATA_LIST_FRITZBOX_FON_WLAN + password;
		}
		String urlstr = "http://" + box_address + "/cgi-bin/webcm";
		String data = fetchDataFromURL(urlstr, postdata);
		Vector list = parseCallerData(data, boxtype, countryPrefix,
				countryCode, areaPrefix, areaCode);
		return list;
	}

	/**
	 *
	 * @param box_address
	 * @param box_password
	 * @param boxtype
	 * @return Vector of QuickDial objects
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static Vector retrieveQuickDialsFromFritzBox(String box_address,
			String box_password, byte boxtype) throws WrongPasswordException,
			IOException {
		String postdata;
		if (boxtype == FritzBoxFirmware.BOXTYPE_FRITZBOX_7050) {
			postdata = POSTDATA_QUICKDIAL_FRITZBOX_7050 + box_password;
		} else { // FIXME POSTDATA_QUICKDIAL_FRITZBOX_7050
			postdata = POSTDATA_QUICKDIAL_FRITZBOX_7050 + box_password;
		}
		String urlstr = "http://" + box_address + "/cgi-bin/webcm";
		String data = fetchDataFromURL(urlstr, postdata);
		return parseQuickDialData(data, boxtype);
	}

	/**
	 * retrieves vector of SipProviders stored in the FritzBox
	 *
	 * @param box_address
	 * @param password
	 * @param boxtype
	 * @return Vector of SipProvider
	 * @throws WrongPasswordException
	 * @throws IOException
	 * author robotniko
	 */
	public static Vector retrieveSipProvider(String box_address,
			String password, byte boxtype) throws WrongPasswordException,
			IOException {

		String postdata = POSTDATA_SIPPROVIDER + password;
		String urlstr = "http://" + box_address + "/cgi-bin/webcm";
		String data = fetchDataFromURL(urlstr, postdata);
		Vector list = parseSipProvider(data);
		return list;
	}

	/**
	 * parses html data from the fritz!box's web interface and retrieves SIP
	 * information.
	 *
	 * @param data
	 *            html data
	 * @return list of SipProvider objects
	 * author robotniko, akw
	 */
	public static Vector parseSipProvider(String data) {
		Vector list = new Vector();
		data = removeDuplicateWhitespace(data);
		Pattern p;
		p = Pattern.compile(PATTERN_SIPPROVIDER);
		Matcher m = p.matcher(data);
		while (m.find()) {
			list.add(new SipProvider(Integer.parseInt(m.group(1)), m.group(3),
					m.group(4)));
			// System.out.println("SIP-Provider: "+list.lastElement());
		}
		return list;
	}

	/**
	 * fetches html data from url using POST requests
	 *
	 * @param urlstr
	 * @param postdata
	 * @return html data
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static String fetchDataFromURL(String urlstr, String postdata)
			throws WrongPasswordException, IOException {
		URL url = null;
		URLConnection urlConn;
		DataOutputStream printout;
		String data = "";
		boolean wrong_pass = false;

		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			System.err.println("URL invalid: " + urlstr);
			throw new WrongPasswordException("URL invalid: " + urlstr);
		}

		if (url != null) {
			urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			// Sending postdata
			if (postdata != null) {
				urlConn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				printout = new DataOutputStream(urlConn.getOutputStream());
				printout.writeBytes(postdata);
				printout.flush();
				printout.close();
			}

			BufferedReader d;

			try {
				// Get response data
				d = new BufferedReader(new InputStreamReader(urlConn
						.getInputStream()));
				int i = 0;
				String str;
				while (null != ((str = d.readLine()))) {
					// Password seems to be wrong
					if (str.contains("FEHLER:&nbsp;Das angegebene Kennwort "))
						wrong_pass = true;
					// Skip a few lines
					//if (i > 778)
					data += str;
					i++;
				}
				d.close();
			} catch (IOException e1) {
				throw new IOException("Network unavailable");
			}

			if (wrong_pass)
				throw new WrongPasswordException("Password invalid");
		}
		return data;
	}

	/**
	 * clears the caller list on the fritz box
	 *
	 * @param box_address
	 * @param password
	 * @throws IOException
	 * @throws WrongPasswordException
	 */
	public static void clearListOnFritzBox(String box_address, String password)
			throws WrongPasswordException, IOException {
		System.out.println("Clearing List");
		String urlstr = "http://" + box_address + "/cgi-bin/webcm";
		String postdata = POSTDATA_CLEAR_FRITZBOX_7050;
		fetchDataFromURL(urlstr, postdata);

	}

	/**
	 * removes all duplicate whitespaces from inputStr
	 *
	 * @param inputStr
	 * @return outputStr
	 */
	public static String removeDuplicateWhitespace(String inputStr) {
		Pattern p = Pattern.compile("\\s+");
		Matcher matcher = p.matcher(inputStr);
		return matcher.replaceAll(" ");
	}

	/**
	 * creates a list of QuickDial objects
	 *
	 * @param data
	 * @param boxtype
	 * @return list of QuickDial objects
	 */
	public static Vector parseQuickDialData(String data, int boxtype) {
		Vector list = new Vector();
		data = removeDuplicateWhitespace(data);
		Pattern p;
		p = Pattern.compile(PATTERN_QUICKDIAL_FRITZBOX_7050);
		Matcher m = p.matcher(data);

		while (m.find())
			list.add(new QuickDial(m.group(1), m.group(2), m.group(3)));

		return list;
	}

	/**
	 * Parses html data from the fritz!box's web interface.
	 *
	 * @param data
	 */
	public static Vector parseCallerData(String data, int boxtype,
			String countryPrefix, String countryCode, String areaPrefix,
			String areaCode) {
		Vector list = new Vector();
		data = removeDuplicateWhitespace(data);

		Pattern p;
		if (boxtype == FritzBoxFirmware.BOXTYPE_FRITZBOX_7050) {
			p = Pattern.compile(PATTERN_FRITZBOX_7050);
			Matcher m = p.matcher(data);

			while (m.find()) {
				try {
					CallType symbol = new CallType(Byte.parseByte(m.group(1)));
					String port = m.group(4);
					String route = m.group(5);
					int duration = Integer.parseInt(m.group(6));
					String number = create_area_number(m.group(3),
							countryPrefix, countryCode, areaPrefix, areaCode);
					Date datum = new Date();
					datum = new SimpleDateFormat("dd.MM.yy HH:mm").parse(m
							.group(2));

					Call call = new Call(symbol, datum, number, port, route,
							duration);
					list.add(call);

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} else {
			p = Pattern.compile(PATTERN_FRITZBOX_FON_WLAN);
			Matcher m = p.matcher(data);

			int i = 0;
			while (m.find()) {
				i++;
				//System.err.println("Found:"+m.group(1)+"|"+m.group(2)+"|"+m.group(3)+"|"+m.group(4)+"|"+m.group(5));
				try {
					CallType symbol = new CallType(Byte.parseByte(m.group(1)));
					String port = m.group(4);
					String route = "";
					int duration = Integer.parseInt(m.group(5));
					String number = create_area_number(m.group(3),
							countryPrefix, countryCode, areaPrefix, areaCode);
					Date datum = new Date();
					datum = new SimpleDateFormat("dd.MM.yy HH:mm").parse(m
							.group(2));

					Call call = new Call(symbol, datum, number, port, route,
							duration);
					list.add(call);

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		return list;
	}

	/**
	 * creates number with area code prefix
	 *
	 * @param number
	 * @param countryPrefix
	 * @param areaPrefix
	 * @param areaCode
	 * @return number with area code prefix
	 */
	public static String create_area_number(String number,
			String countryPrefix, String countryCode, String areaPrefix,
			String areaCode) {
		if (!number.equals("")) {
			if (number.startsWith(countryPrefix)) { // International call

				if (number.startsWith(countryPrefix + countryCode)) {
					// if own country, remove countrycode
					number = areaPrefix
							+ number.substring(countryPrefix.length()
									+ countryCode.length());
				}
			} else if (number.startsWith(areaPrefix)) {
				if (number.startsWith("010")) { // cut 01013 and others
					number = number.substring(5);
				}
			} else {
				number = areaPrefix + areaCode + number;
			}
		}
		return number;
	}

	/**
	 * creates a String with version and date of CVS Id-Tag
	 *
	 * @param tag
	 * @return String with version and date of CVS Id-Tag
	 */
	public static String getVersionFromCVSTag(String tag) {
		String[] parts = tag.split(" ");
		return "CVS v" + parts[2] + " (" + parts[3] + ")";
	}

}
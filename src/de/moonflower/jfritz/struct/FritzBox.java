package de.moonflower.jfritz.struct;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.quickdial.QuickDials;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.HTMLUtil;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzBox {

	private static String POSTDATA_CLEAR = "&var%3Alang=$LANG&var%3Apagename=foncalls&var%3Amenu=fon&telcfg%3Asettings/ClearJournal=1"; //$NON-NLS-1$

	private static String POSTDATA_SIPPROVIDER = "&var%3Alang=$LANG&var%3Amenu=fon&var%3Apagename=siplist&login%3Acommand%2Fpassword="; //$NON-NLS-1$

	private static String POSTDATA_LIST = "&var%3Alang=$LANG&var%3Amenu=fon&var%3Apagename=foncalls&login%3Acommand%2Fpassword="; //$NON-NLS-1$

	private static String POSTDATA_FETCH_CALLERLIST = "getpage=../html/$LANG/$CSV_FILE&errorpage=..%2Fhtml%2F$LANG%2Fmenus%2Fmenu2.html&var%3Alang=$LANG&var%3Apagename=foncalls&var%3Aerrorpagename=foncalls&var%3Amenu=fon&var%3Apagemaster=&time%3Asettings%2Ftime=1136559837%2C-60";

	private static String POSTDATA_QUICKDIAL = "&var%3Alang=$LANG&var%3Amenu=fon&var%3Apagename=kurzwahlen&login%3Acommand%2Fpassword="; //$NON-NLS-1$

	private static String POSTDATA_QUICKDIAL_NEW = "&var%3Alang=$LANG&var%3Amenu=fon&var%3Apagename=fonbuch&login%3Acommand%2Fpassword="; //$NON-NLS-1$

	private static String POSTDATA_LOGIN = "&login:command/password=$PASSWORT"; //$NON-NLS-1$

	private static String POSTDATA_CALL = "&login:command/password=$PASSWORT&telcfg:settings/UseClickToDial=1&telcfg:settings/DialPort=$NEBENSTELLE&telcfg:command/Dial=$NUMMER"; //$NON-NLS-1$

	private final static String CSV_FILE_EN = "FRITZ!Box_Calllist.csv";

	private final static String CSV_FILE_DE = "FRITZ!Box_Anrufliste.csv";

	private final static String PATTERN_SIPPROVIDER_OLD = "<!-- \"(\\d)\" / \"(\\w*)\" -->" //$NON-NLS-1$
		// FW <= 37
		+ "\\s*<td class=\"c1\">\\s*<input type=checkbox id=\"uiViewActivsip\\d\"" //$NON-NLS-1$
		+ "\\s*onclick=\"uiOnChangeActivated\\('uiViewActivsip\\d','uiPostActivsip\\d'\\); return true;\">" //$NON-NLS-1$
		+ "\\s*</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c2\">([^<]*)</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c3\"><script type=\"text/javascript\">document.write\\(ProviderDisplay\\(\"([^\"]*)\"\\)\\);</script></td>"; //$NON-NLS-1$

	private final static String PATTERN_SIPPROVIDER_37_91 = "<!-- \"(\\d)\" / \"(\\w*)\" -->" //$NON-NLS-1$
		// 37 < FW <= 91
		+ "\\s*<td class=\"c1\">\\s*<input type=checkbox id=\"uiViewActivsip\\d\"" //$NON-NLS-1$
		+ "\\s*onclick=\"uiOnChangeActivated\\('uiViewActivsip\\d','uiPostActivsip\\d'\\); return true;\">" //$NON-NLS-1$
		+ "\\s*</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c2\">([^<]*)</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c3\"><script type=\"text/javascript\">document.write\\(ProviderDisplay\\(\"([^\"]*)\"\\)\\);</script></td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c6\"><script type=\"text/javascript\">document.write\\(AuswahlDisplay\\(\"([^\"]*)\"\\)\\);</script></td>"; //$NON-NLS-1$

	private final static String PATTERN_SIPPROVIDER_96 = "<!--\\s*\"(\\d)\"\\s*/\\s*\"(\\w*)\"\\s*/\\s*\"\\w*\"\\s*-->" //$NON-NLS-1$
		// FW >= 96
		+ "\\s*<td class=\"c1\">\\s*<input type=checkbox id=\"uiViewActivsip\\d\"" //$NON-NLS-1$
		+ "\\s*onclick=\"uiOnChangeActivated\\('uiViewActivsip\\d','uiPostActivsip\\d'\\); return true;\">" //$NON-NLS-1$
		+ "\\s*</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c2\">([^<]*)</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c3\"><script type=\"text/javascript\">document.write\\(ProviderDisplay\\(\"([^\"]*)\"\\)\\);</script></td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c6\"><script type=\"text/javascript\">document.write\\(AuswahlDisplay\\(\"([^\"]*)\"\\)\\);</script></td>"; //$NON-NLS-1$

	private final static String PATTERN_SIPPROVIDER_ACTIVE = "<input type=\"hidden\" name=\"sip:settings/sip(\\d)/activated\" value=\"(\\d)\" id=\"uiPostActivsip"; //$NON-NLS-1$

	private final static String PATTERN_QUICKDIAL = "<tr class=\"Dialoglist\">" //$NON-NLS-1$
		+ "\\s*<td style=\"text-align: center;\">(\\d*)</td>" //$NON-NLS-1$
		+ "\\s*<td>(\\w*)</td>" //$NON-NLS-1$
		+ "\\s*<td>([^<]*)</td>" //$NON-NLS-1$
		+ "\\s*<td style=\"text-align: right;\"><button [^>]*>\\s*<img [^>]*></button></td>" //$NON-NLS-1$
		+ "\\s*<td style=\"text-align: right;\"><button [^>]*>\\s*<img [^>]*></button></td>" //$NON-NLS-1$
		+ "\\s*</tr>"; //$NON-NLS-1$

	private final static String PATTERN_QUICKDIAL_NEW = "<td class=\"c5\"><span title=\"[^\"]*\">([^<]*)</span></td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c3\"><span title=\"[^\"]*\">([^<]*)</span></td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c1\">(\\d*)</td>" //$NON-NLS-1$
		+ "\\s*<td class=\"c2\"><span title=\"[^\"]*\">([^<]*)"; //$NON-NLS-1$

	private final static String PATTERN_QUICKDIAL_BETA = "<script type=\"text/javascript\">document.write\\(TrFon\\(\"[^\"]*\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\"\\)\\);</script>";


	private FritzBoxFirmware firmware;

	private String box_address;

	private String box_password;

	private String box_port;

	private static int[] quickdial_indizes = { -1, -1, -1, -1 };

	private final static int VANITY = 0;

	private final static int NAME = 1;

	private final static int QUICKDIAL = 2;

	private final static int NUMBER = 3;

	public FritzBox ( String address, String password, String port) {
		box_address = address;
		box_password = password;
		box_port = port;
		detectFirmware();
	}

	/**
	 * Detects firmware version
	 * @return
	 */
	public void detectFirmware() {
		try {
			box_address = JFritz.getProperty("box.address");
			box_password = Encryption.decrypt(JFritz.getProperty("box.password"));
			box_port = JFritz.getProperty("box.port");
			firmware = FritzBoxFirmware.detectFirmwareVersion(
					box_address,
					box_password,
					box_port);
		} catch (WrongPasswordException e) {
			Debug.msg(JFritz.getMessage("wrong_password"));
		} catch (InvalidFirmwareException e) {
			Debug.msg(JFritz.getMessage("box_address_wrong"));
		} catch (IOException e) {
			Debug.msg(JFritz.getMessage("box_address_wrong"));
		}
	}

	/**
	 * Returns reference to firmware
	 * @return
	 */
	public FritzBoxFirmware getFirmware() {
		return firmware;
	}

	/**
	 * clears the caller list on the fritz box
	 *
	 * @throws IOException
	 * @throws WrongPasswordException
	 */
	public void clearListOnFritzBox() throws WrongPasswordException,
			IOException {
		Debug.msg("Clearing List"); //$NON-NLS-1$
		String urlstr = "http://" + box_address + ":" + box_port + "/cgi-bin/webcm"; //$NON-NLS-1$,  //$NON-NLS-2$
		String postdata = firmware.getAccessMethod()
				+ POSTDATA_CLEAR.replaceAll("\\$LANG", firmware.getLanguage());
		fetchDataFromURL(urlstr, postdata, true);
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
	public String fetchDataFromURL(String urlstr, String postdata,
			boolean retrieveData) throws WrongPasswordException, IOException {
		URL url = null;
		URLConnection urlConn;
		DataOutputStream printout;
		String data = ""; //$NON-NLS-1$
		boolean wrong_pass = false;
		Debug.msg("Urlstr: " + urlstr);
		Debug.msg("Postdata: " + postdata);

		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
			throw new WrongPasswordException("URL invalid: " + urlstr); //$NON-NLS-1$
		}

		if (url != null) {
			urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			// Sending postdata
			if (postdata != null) {
				urlConn.setRequestProperty("Content-Type", //$NON-NLS-1$
						"application/x-www-form-urlencoded"); //$NON-NLS-1$
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
				String str;
				while (null != ((str = HTMLUtil.stripEntities(d.readLine())))) {
					// Password seems to be wrong
					if (str.indexOf("FRITZ!Box Anmeldung") > 0) //$NON-NLS-1$
						wrong_pass = true;
					if (retrieveData)
						data += str;
				}
				d.close();


			} catch (IOException e1) {
				throw new IOException("Network unavailable"); //$NON-NLS-1$
			}

			if (wrong_pass)
				throw new WrongPasswordException("Password invalid"); //$NON-NLS-1$
		}

		return data;
	}

	/**
	 * retrieves vector of SipProviders stored in the FritzBox
	 *
	 * @return Vector of SipProvider
	 * @throws WrongPasswordException
	 * @throws IOException
	 *             author robotniko
	 * @throws InvalidFirmwareException
	 */
	public Vector retrieveSipProvider()
			throws WrongPasswordException, IOException,
			InvalidFirmwareException {
		if (firmware == null)
			throw new InvalidFirmwareException("No valid firmware"); //$NON-NLS-1$
		String postdata = firmware.getAccessMethod()
				+ POSTDATA_SIPPROVIDER.replaceAll("\\$LANG", firmware
						.getLanguage())
				+ URLEncoder.encode(box_password, "ISO-8859-1"); //$NON-NLS-1$
		String urlstr = "http://" + box_address + ":" + box_port + "/cgi-bin/webcm"; //$NON-NLS-1$,  //$NON-NLS-2$
		Debug.msg("Postdata: " + postdata); //$NON-NLS-1$
		Debug.msg("Urlstr: " + urlstr); //$NON-NLS-1$
		String data = fetchDataFromURL(urlstr, postdata, true);

		// DEBUG: Test other versions
		if (false) {
			String filename = "./Firmware 96/sipProvider.html"; //$NON-NLS-1$
			Debug.msg("Debug mode: Loading " + filename); //$NON-NLS-1$
			try {
				data = ""; //$NON-NLS-1$
				String thisLine;
				BufferedReader in = new BufferedReader(new FileReader(filename));
				while ((thisLine = in.readLine()) != null) {
					data += thisLine;
				}
				in.close();
			} catch (IOException e) {
				Debug.err("File not found: " + filename); //$NON-NLS-1$
			}
		}

		Vector list = parseSipProvider(data, firmware);
		return list;
	}

	/**
	 * parses html data from the fritz!box's web interface and retrieves SIP
	 * information.
	 *
	 * @param data
	 *            html data
	 * @return list of SipProvider objects author robotniko, akw
	 */
	public Vector parseSipProvider(String data, FritzBoxFirmware firmware) {
		Vector list = new Vector();
		data = JFritzUtils.removeDuplicateWhitespace(data);
		Pattern p;
		if (firmware.getMajorFirmwareVersion() == 3
				&& firmware.getMinorFirmwareVersion() < 42)
			p = Pattern.compile(PATTERN_SIPPROVIDER_OLD);
		else if (firmware.getMajorFirmwareVersion() == 3
				&& firmware.getMinorFirmwareVersion() < 96)
			p = Pattern.compile(PATTERN_SIPPROVIDER_37_91);
		else
			p = Pattern.compile(PATTERN_SIPPROVIDER_96);
		Matcher m = p.matcher(data);
		while (m.find()) {
			Debug.msg("FOUND SIP-PROVIDER"); //$NON-NLS-1$
			if (!(m.group(4).equals(""))) { //$NON-NLS-1$
				if (firmware.getMajorFirmwareVersion() == 3
						&& firmware.getMinorFirmwareVersion() < 42)
					list.add(new SipProvider(Integer.parseInt(m.group(1)), m
							.group(3), m.group(4)));
				else
					list.add(new SipProvider(Integer.parseInt(m.group(5)), m
							.group(3), m.group(4)));

				Debug.msg("SIP-Provider: " + list.lastElement()); //$NON-NLS-1$
			}
		}
		p = Pattern.compile(PATTERN_SIPPROVIDER_ACTIVE);
		m = p.matcher(data);
		while (m.find()) {
			Enumeration en = list.elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getProviderID() == Integer.parseInt(m.group(1))) {
					if (Integer.parseInt(m.group(2)) == 1) {
						sipProvider.setActive(true);
					} else {
						sipProvider.setActive(false);
					}
				}
			}
		}

		return list;
	}

	/**
	 * This function fetches the call list from the box it first gets a
	 * connection to the fritz!Box opens the call list html page, then it sends
	 * a request to the box for the csv file then passes a bufferedReader to
	 * CallerList.importFromCSV() it passes back to the caller if there were new
	 * callers or not NOTE: Function no longer checks for invalid passwords!
	 *
	 *
	 * LAST MODIFIED: 14.04.06 Brian Jensen
	 *
	 * @author Brian Jensen
	 *
	 * @return if there were new calls or not
	 * @throws WrongPasswordException
	 * @throws IOException
	 */

	public boolean retrieveCSVList()
			throws WrongPasswordException, IOException {

		URL url;
		URLConnection urlConn;
		DataOutputStream printout;
		boolean wrong_pass = false;
		boolean newEntries = false;
		Debug.msg("Opening HTML Callerlist page");
		// retrieveHTMLCallerList(box_address, password, countryPrefix,
		// countryCode,
		// areaPrefix, areaCode, firmware, jfritz);

		// Attempting to fetch the html version of the call list
		String postdata = firmware.getAccessMethod()
				+ POSTDATA_LIST.replaceAll("\\$LANG", firmware.getLanguage())
				+ URLEncoder.encode(box_password, "ISO-8859-1");
		String urlstr = "http://" + box_address +":" + box_port + "/cgi-bin/webcm";

		Debug.msg("Urlstr: " + urlstr);
		Debug.msg("Postdata: " + postdata);

		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr);
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

			try {
				// Get response data from the box
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(urlConn.getInputStream()));

				// read out the response data!
				while (reader.skip(100000) > 0) {
					// kind of stupid, but it has to be
					// If you don't read the list, you may not get an
					// Updated list from the box
				}

				// close the streams
				reader.close();
				urlConn.getInputStream().close();

			} catch (IOException e1) {
				throw new IOException("Network unavailable");
			}

		}

		// The list should be updated now
		// Get the csv file for processing
		Debug.msg("Retrieving the CSV list from the box");
		urlstr = "http://" + box_address + ":" + box_port + "/cgi-bin/webcm";

		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr);
			throw new WrongPasswordException("URL invalid: " + urlstr);
		}

		// If the url is valid load the data
		if (url != null) {

			urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			// Sending postdata to the fritz box
			urlConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			printout = new DataOutputStream(urlConn.getOutputStream());
			if (firmware.getLanguage().equals("de")) {
				printout.writeBytes(POSTDATA_FETCH_CALLERLIST.replaceAll(
						"\\$LANG", firmware.getLanguage()).replaceAll(
						"\\$CSV_FILE", CSV_FILE_DE));
			} else if (firmware.getLanguage().equals("en")) {
				printout.writeBytes(POSTDATA_FETCH_CALLERLIST.replaceAll(
						"\\$LANG", firmware.getLanguage()).replaceAll(
						"\\$CSV_FILE", CSV_FILE_EN));
			}
			printout.flush();
			printout.close();

			BufferedReader reader;

			try {
				// Get response data from the box
				reader = new BufferedReader(new InputStreamReader(urlConn
						.getInputStream()));

				// pass it on to the import function

				Debug.msg("Received response, begin processing call list");
				newEntries = JFritz.getCallerList().importFromCSVFile(reader);
				Debug.msg("Finished processing response");

				// close the reader and the cocket connection
				reader.close();
				urlConn.getInputStream().close();

			} catch (IOException e1) {
				throw new IOException("Network unavailable");
			}

			if (wrong_pass)
				throw new WrongPasswordException("Password invalid");
		}

		// return if there were new entries or not
		return newEntries;
	}

	/**
	 *
	 * @return Vector of QuickDial objects
	 * @throws WrongPasswordException
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 */
	public Vector retrieveQuickDialsFromFritzBox(QuickDials model) throws WrongPasswordException,
			IOException, InvalidFirmwareException {

		String postdata;
		if (firmware.getMajorFirmwareVersion() == 4
				&& firmware.getMinorFirmwareVersion() >= 3) {
			postdata = firmware.getAccessMethod()
					+ POSTDATA_QUICKDIAL_NEW.replaceAll("\\$LANG", firmware
							.getLanguage())
					+ URLEncoder.encode(box_password, "ISO-8859-1"); //$NON-NLS-1$,  //$NON-NLS-2$
		} else {
			postdata = firmware.getAccessMethod()
					+ POSTDATA_QUICKDIAL.replaceAll("\\$LANG", firmware
							.getLanguage())
					+ URLEncoder.encode(box_password, "ISO-8859-1"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		String urlstr = "http://" //$NON-NLS-1$
				+ box_address + ":"
				+ box_port
				+ "/cgi-bin/webcm"; //$NON-NLS-1$
		String data = fetchDataFromURL(urlstr, postdata, true);
		return parseQuickDialData(model, data, firmware);
	}

	/**
	 * creates a list of QuickDial objects
	 *
	 * @param data
	 * @param firmware
	 * @return list of QuickDial objects
	 */
	public Vector parseQuickDialData(QuickDials model, String data,
			FritzBoxFirmware firmware) {
		Vector list = new Vector();
		data = JFritzUtils.removeDuplicateWhitespace(data);
		Pattern p;
		if (firmware.getMajorFirmwareVersion() == 4
				&& firmware.getMinorFirmwareVersion() >= 3
				&& firmware.getMinorFirmwareVersion() < 5) {
			p = Pattern.compile(PATTERN_QUICKDIAL_NEW);
			quickdial_indizes[NAME] = 1;
			quickdial_indizes[QUICKDIAL] = 3;
			quickdial_indizes[VANITY] = 4;
			quickdial_indizes[NUMBER] = 2;
		} else if (firmware.getMajorFirmwareVersion() == 4
				&& firmware.getMinorFirmwareVersion() >= 5) {
			p = Pattern.compile(PATTERN_QUICKDIAL_BETA);
			quickdial_indizes[NAME] = 1;
			quickdial_indizes[QUICKDIAL] = 3;
			quickdial_indizes[VANITY] = 4;
			quickdial_indizes[NUMBER] = 2;
		} else {
			p = Pattern.compile(PATTERN_QUICKDIAL);
			quickdial_indizes[QUICKDIAL] = 1;
			quickdial_indizes[VANITY] = 2;
			quickdial_indizes[NUMBER] = 3;

		}
		Matcher m = p.matcher(data);

		// TODO: Name einfügen
		while (m.find()) {
			String description = model.getDescriptionFromNumber(m
					.group(quickdial_indizes[NUMBER]));
			list.add(new QuickDial(m.group(quickdial_indizes[QUICKDIAL]), m
					.group(quickdial_indizes[VANITY]), m
					.group(quickdial_indizes[NUMBER]), description));
		}
		return list;
	}

	public boolean checkValidFirmware() {
		while (firmware == null) {
			try {
				firmware = FritzBoxFirmware.detectFirmwareVersion(
								box_address,
								box_password,
								box_port);
				return true;
			} catch (WrongPasswordException e) {
				String new_box_password = JFritz.getJframe().showPasswordDialog("");
				if (new_box_password == null) // Canceled dialog
					return false;
				else {
					box_password = new_box_password;
				}
			} catch (InvalidFirmwareException e) {
				String new_box_address = JFritz.getJframe().showAddressDialog(box_address);
				if (new_box_address == null) { // Dialog canceled
					return false;
				} else {
					box_address = new_box_address;
				}
			} catch (IOException e) {
				String new_box_address = JFritz.getJframe().showAddressDialog(box_address);
				if (new_box_address == null) { // Dialog canceled
					return false;
				} else {
					box_address = new_box_address;
				}
			}
		}
		return true;
	}

	/**
	 * Sends login data to FritzBox
	 */
	public void login() {
		try {
			String postdata = POSTDATA_LOGIN.replaceAll("\\$PASSWORT", //$NON-NLS-1$
					URLEncoder.encode(box_password, "ISO-8859-1"));

			postdata = firmware.getAccessMethod() + postdata;

			String urlstr = "http://" //$NON-NLS-1$
					+ box_address + ":" + box_port
					+ "/cgi-bin/webcm"; //$NON-NLS-1$
			fetchDataFromURL(urlstr, postdata, true);
		} catch (UnsupportedEncodingException uee) {
			//TODO handle Exceptions
		} catch (WrongPasswordException wpe) {
			//TODO handle Exceptions
		} catch (IOException ioe) {
			//TODO handle Exceptions
		}
	}

	public void doCall(String number, String port) {
		try {
			login();
			number = number.replaceAll("\\+", "00"); //$NON-NLS-1$,  //$NON-NLS-2$

			String portStr = ""; //$NON-NLS-1$
			if (port.equals("Fon 1")) { //$NON-NLS-1$
				portStr = "1"; //$NON-NLS-1$
			} else if (port.equals("Fon 2")) { //$NON-NLS-1$
				portStr = "2"; //$NON-NLS-1$
			} else if (port.equals("Fon 3")) { //$NON-NLS-1$
				portStr = "3"; //$NON-NLS-1$
			} else if (port.equals(JFritz.getMessage("analog_telephones_all"))) { //$NON-NLS-1$
				portStr = "9"; //$NON-NLS-1$
			} else if (port.equals("ISDN Alle")) { //$NON-NLS-1$
				portStr = "50"; //$NON-NLS-1$
			} else if (port.equals("ISDN 1")) { //$NON-NLS-1$
				portStr = "51"; //$NON-NLS-1$
			} else if (port.equals("ISDN 2")) { //$NON-NLS-1$
				portStr = "52"; //$NON-NLS-1$
			} else if (port.equals("ISDN 3")) { //$NON-NLS-1$
				portStr = "53"; //$NON-NLS-1$
			} else if (port.equals("ISDN 4")) { //$NON-NLS-1$
				portStr = "54"; //$NON-NLS-1$
			} else if (port.equals("ISDN 5")) { //$NON-NLS-1$
				portStr = "55"; //$NON-NLS-1$
			} else if (port.equals("ISDN 6")) { //$NON-NLS-1$
				portStr = "56"; //$NON-NLS-1$
			} else if (port.equals("ISDN 7")) { //$NON-NLS-1$
				portStr = "57"; //$NON-NLS-1$
			} else if (port.equals("ISDN 8")) { //$NON-NLS-1$
				portStr = "58"; //$NON-NLS-1$
			} else if (port.equals("ISDN 9")) { //$NON-NLS-1$
				portStr = "59"; //$NON-NLS-1$
			}
            String postdata = POSTDATA_CALL.replaceAll("\\$PASSWORT", //$NON-NLS-1$
                    URLEncoder.encode(box_password, "ISO-8859-1"));
			postdata = postdata.replaceAll("\\$NUMMER", number); //$NON-NLS-1$
			postdata = postdata.replaceAll("\\$NEBENSTELLE", portStr); //$NON-NLS-1$

			postdata = firmware.getAccessMethod() + postdata;

			String urlstr = "http://" //$NON-NLS-1$
					+ box_address + ":" + box_port
					+ "/cgi-bin/webcm"; //$NON-NLS-1$
			fetchDataFromURL(urlstr, postdata, true);
		} catch (UnsupportedEncodingException uee) {
		} catch (WrongPasswordException wpe) {
		} catch (IOException ioe) {
		}

	}

	/**
	 * Returns current box address
	 * @return
	 */
	public String getAddress() {
		return box_address;
	}

	/**
	 * Returns current box password
	 * @return
	 */
	public String getPassword() {
		return box_password;
	}

	/**
	 * Returns current box port
	 * @return
	 */
	public String getPort() {
		return box_port;
	}
}

/*
 *
 * Created on 17.05.2005
 *
 */
package de.moonflower.jfritz.box.fritzbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.conf.SupportedFritzBoxProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Class for detected and managing different firmware versions
 *
 * @author Arno Willig
 *
 */
public class FritzBoxFirmware {

	public final static byte ACCESS_METHOD_POST_0342 = 0;

	public final static byte ACCESS_METHOD_ENGLISH = 1;

	public final static byte ACCESS_METHOD_PRIOR_0342 = 2;

	private byte boxtype;

	private byte majorFirmwareVersion;

	private byte minorFirmwareVersion;

	private String modFirmwareVersion;

	private String language;

	private boolean sessionIdNecessary;

	private String sessionId;

	private final static String[] POSTDATA_ACCESS_METHOD = {
			"getpage=../html/de/menus/menu2.html", //$NON-NLS-1$
			"getpage=../html/en/menus/menu2.html", //$NON-NLS-1$
			"getpage=../html/menus/menu2.html" }; //$NON-NLS-1$

	private final static String[] POSTDATA_DETECT_FIRMWARE = {
			"&var%3Alang=de&var%3Amenu=home&var%3Apagename=home", //$NON-NLS-1$
			"&var%3Alang=en&var%3Amenu=home&var%3Apagename=home" }; //$NON-NLS-1$

	private final static String PATTERN_DETECT_FIRMWARE = "version=(\\d\\d\\d*).(\\d\\d).(\\d\\d\\d*)"; //$NON-NLS-1$
	private final static String PATTERN_DETECT_FIRMWARE_OLD = "[Firmware|Labor][-| ][V|v]ersion[^\\d]*(\\d\\d\\d*).(\\d\\d).(\\d\\d\\d*)([^<]*)"; //$NON-NLS-1$

	private final static String PATTERN_DETECT_LANGUAGE_DE = "Telefonie";

	private final static String PATTERN_DETECT_LANGUAGE_EN = "Telephony";

	protected static PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	/**
	 * Firmware Constructor using Strings
	 *
	 * @param boxtype
	 * @param majorFirmwareVersion
	 * @param minorFirmwareVersion
	 * @param modFirmwareVersion
	 * @param language
	 * @param sidNecessary
	 * @param sid
	 */
	public FritzBoxFirmware(String boxtype, String majorFirmwareVersion,
			String minorFirmwareVersion, String modFirmwareVersion,
			String language, boolean sidNecessary, String sid) {
		this.boxtype = Byte.parseByte(boxtype);
		this.majorFirmwareVersion = Byte.parseByte(majorFirmwareVersion);
		this.minorFirmwareVersion = Byte.parseByte(minorFirmwareVersion);
		this.modFirmwareVersion = modFirmwareVersion;
		this.language = language;
		this.sessionIdNecessary = sidNecessary;
		this.sessionId = sid;
	}

	/**
	 * Static method for firmware detection
	 *
	 * @param box_name
	 * @param box_address
	 * @param box_password
	 * @return New instance of FritzBoxFirmware
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static FritzBoxFirmware detectFirmwareVersion(String box_name,
			String box_protocol, String box_address, String box_password, String port)
			throws WrongPasswordException, IOException,
			InvalidFirmwareException {
		final String urlstr = box_protocol + "://" + box_address + ":" + port + "/"; //$NON-NLS-1$, //$NON-NLS-2$

		if ("2".equals(properties.getProperty("network.type"))
				&& Boolean.parseBoolean(properties
						.getProperty("option.clientCallList"))
				&& NetworkStateMonitor.isConnectedToServer()) {

			Debug
					.netMsg("JFritz is configured as a client and using call list from server, canceling firmware detection");
			return null;
		}

		Vector<String> data = new Vector<String>();
		String language = "de";

		boolean detected = false;

		SIDLogin sidLogin = new SIDLogin();
		for (int i = 0; i < (POSTDATA_ACCESS_METHOD).length && !detected; i++) {
			for (int j = 0; j < (POSTDATA_DETECT_FIRMWARE).length && !detected; j++) {
				boolean password_wrong = true;
				int retry_count = 0;
				int max_retry_count = 2;

				while ((password_wrong) && (retry_count < max_retry_count)) {
					String postdata = POSTDATA_ACCESS_METHOD[i]
							+ POSTDATA_DETECT_FIRMWARE[j];
					Debug.debug("Retry count: " + retry_count);
					retry_count++;

					try {
						sidLogin.check(box_name, urlstr, box_password);
					} catch (WrongPasswordException wpe) {
						Debug.debug("No SID-Login necessary.");
					}

					try {
						if (sidLogin.isSidLogin()) {
							postdata = postdata + "&login%3Acommand%2Fresponse=";
							postdata = postdata + URLEncoder.encode(sidLogin.getResponse(), "ISO-8859-1");
							sidLogin.login(box_name, urlstr, postdata);
							postdata = POSTDATA_ACCESS_METHOD[i] + POSTDATA_DETECT_FIRMWARE[j];
							data = JFritzUtils.fetchDataFromURLToVector(box_name, urlstr + "cgi-bin/webcm", postdata + "&sid="+sidLogin.getSessionId(), true);
						} else {
							postdata = postdata + "&login%3Acommand%2Fpassword=";
							postdata = postdata + URLEncoder.encode(box_password, "ISO-8859-1");
							data = JFritzUtils.fetchDataFromURLToVector(box_name, urlstr + "cgi-bin/webcm", postdata, true);
						}
						password_wrong = false;
					} catch (WrongPasswordException wpe) {
						password_wrong = true;
						if (retry_count == max_retry_count) {
							throw wpe;
						}
						try {
							Thread.sleep(wpe.getRetryTime() * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				// read firmware informations from file
				if (false) {
					String filename = "/home/robotniko/firmware"; //$NON-NLS-1$
					Debug.debug("Debug mode: Loading " + filename); //$NON-NLS-1$
					try {
						data.clear(); //$NON-NLS-1$
						String thisLine;
						BufferedReader in = new BufferedReader(new FileReader(
								filename));
						while ((thisLine = in.readLine()) != null) {
							data.add(thisLine);
						}
						in.close();
					} catch (IOException e) {
						Debug.error("File not found: " + filename); //$NON-NLS-1$
					}
				}

				// Debug.msg(data);

				Pattern detectDE = Pattern.compile(PATTERN_DETECT_LANGUAGE_DE);
				Pattern detectEN = Pattern.compile(PATTERN_DETECT_LANGUAGE_EN);

				long startParse = JFritzUtils.getTimestamp();
				for (int k = 0; k < data.size(); k++) {
					Matcher m = detectDE.matcher(data.get(k));
					if (m.find()) {
						language = "de";
						detected = true;
						break;
					}

					if (!detected) {
						m = detectEN.matcher(data.get(k));
						if (m.find()) {
							language = "en";
							detected = true;
							break;
						}
					}
				}
				long endParse = JFritzUtils.getTimestamp();
				Debug.debug("Used time to parse response: "
						+ (endParse - startParse) + "ms");
			}
			if (detected)
				break;
		}

		if (!detected)
			throw new InvalidFirmwareException();

		sidLogin.getSidFromResponse(data);

		long startTimestamp = JFritzUtils.getTimestamp();
		Debug.debug("Parsing data (" + data.size()
				+ " lines) to detect firmware!");
		Pattern firmwarePattern = Pattern.compile(PATTERN_DETECT_FIRMWARE);
		Pattern firmwarePatternOld = Pattern.compile(PATTERN_DETECT_FIRMWARE_OLD);

		for (int k = data.size() - 1; k > 0; k--) {
			Matcher m = firmwarePattern.matcher(data.get(k));
			Matcher m2 = firmwarePatternOld.matcher(data.get(k));
			Matcher found = null;
			boolean foundOldFirmware = false;
			boolean foundFirmware = m.find();
			if (foundFirmware) {
				found = m;
			} else {
				foundOldFirmware = m2.find();
				found = m2;
			}

			if (foundFirmware || foundOldFirmware) {
				String boxtypeString = found.group(1);
				String majorFirmwareVersion = found.group(2);
				String minorFirmwareVersion = found.group(3);
				String modFirmwareVersion = "";
				if (found.groupCount() > 3) {
					modFirmwareVersion = found.group(4).trim();
				}

				Debug.info("Detected Firmware: " + boxtypeString + "."
						+ majorFirmwareVersion + "." + minorFirmwareVersion
						+ modFirmwareVersion + " " + language);
				if ((((Integer.parseInt(majorFirmwareVersion) == 4) && (Integer
						.parseInt(minorFirmwareVersion) >= 67)) || (Integer
						.parseInt(majorFirmwareVersion) > 4))
						&& (language.equals("en"))) {
					// ab version xx.04.67 gibt es bei englischen Firmwares
					// keine Unterscheidung mehr zwischen
					// internationaler und deutscher Firmware bei den URLs und
					// Anrufliste.csv
					Debug
							.info("Detected international firmware greater than xx.04.67. Forcing to use german patterns.");
					language = "de";
				}

				long endTimestamp = JFritzUtils.getTimestamp();
				Debug.debug("Used time to detect Firmware: "
						+ (endTimestamp - startTimestamp) + "ms");
				return new FritzBoxFirmware(boxtypeString,
						majorFirmwareVersion, minorFirmwareVersion,
						modFirmwareVersion, language, sidLogin.isSidLogin(),
						sidLogin.getSessionId());
			}
		}

		// no firmware found
		throw new InvalidFirmwareException();
	}

	/**
	 * @return Returns the boxtype.
	 */
	public final byte getBoxType() {
		return boxtype;
	}

	/**
	 * @return Returns the access method string.
	 *
	 */
	public final String getAccessMethod() {
		int accessMethod;
		if (language.equals("en"))
			accessMethod = ACCESS_METHOD_ENGLISH;
		else if (majorFirmwareVersion == 3 && minorFirmwareVersion < 42)
			accessMethod = ACCESS_METHOD_PRIOR_0342;
		else
			accessMethod = ACCESS_METHOD_POST_0342;

		return POSTDATA_ACCESS_METHOD[accessMethod];
	}

	/**
	 * @return Returns the majorFirmwareVersion.
	 */
	public byte getMajorFirmwareVersion() {
		return majorFirmwareVersion;
	}

	/**
	 * @return Returns the minorFirmwareVersion.
	 */
	public byte getMinorFirmwareVersion() {
		return minorFirmwareVersion;
	}

	/**
	 * @return Returns the majorFirmwareVersion.
	 */
	public final String getFirmwareVersion() {
		String boxtypeStr = Byte.toString(boxtype);
		String majorStr = Byte.toString(majorFirmwareVersion);
		String minorStr = Byte.toString(minorFirmwareVersion);
		if (boxtypeStr.length() == 1) {
			boxtypeStr = "0" + boxtypeStr;} //$NON-NLS-1$
		if (majorStr.length() == 1) {
			majorStr = "0" + majorStr;} //$NON-NLS-1$
		if (minorStr.length() == 1) {
			minorStr = "0" + minorStr;} //$NON-NLS-1$
		return boxtypeStr
				+ "." + majorStr + "." + minorStr + modFirmwareVersion; //$NON-NLS-1$,  //$NON-NLS-2$
	}

	public String getBoxName() {
		return SupportedFritzBoxProvider.getInstance().getBoxById(boxtype);
	}

	public final String toString() {
		return getFirmwareVersion();
	}

	public final String getLanguage() {
		return language;
	}

	public final boolean isSidLogin() {
		return sessionIdNecessary;
	}

	public final String getSessionId() {
		return sessionId;
	}

	public final boolean isLowerThan(final int major, final int minor) {
		return (getMajorFirmwareVersion() < major || (getMajorFirmwareVersion() == major && getMinorFirmwareVersion() < minor));
	}
}

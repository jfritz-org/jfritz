/*
 *
 * Created on 17.05.2005
 *
 */
package de.moonflower.jfritz.firmware;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.struct.SIDLogin;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Class for detected and managing different firmware versions
 *
 * @author Arno Willig
 *
 */
public class FritzBoxFirmware {

	public final static byte BOXTYPE_FRITZBOX_FON = 6;

	public final static byte BOXTYPE_FRITZBOX_FON_WLAN = 8;

	public final static byte BOXTYPE_FRITZBOX_ATA = 11;

    public final static byte BOXTYPE_FRITZBOX_5010 = 23;

    public final static byte BOXTYPE_FRITZBOX_5012 = 25;

	public final static byte BOXTYPE_FRITZBOX_5050 = 12;

    public final static byte BOXTYPE_FRITZBOX_7050 = 14;

    public final static byte BOXTYPE_EUMEX_300 = 15;

    public final static byte BOXTYPE_FRITZBOX_SPEEDPORT_W501V = 28;

    public final static byte BOXTYPE_FRITZBOX_7170 = 29;

    public final static byte BOXTYPE_FRITZBOX_7140 = 30;

    public final static byte BOXTYPE_FRITZBOX_SPEEDPORT_W900V = 34;

    public final static byte BOXTYPE_FRITZBOX_7141 = 40;

    public final static byte BOXTYPE_FRITZBOX_5140 = 43;

    public final static byte BOXTYPE_FRITZBOX_7270 = 54;

    public final static byte BOXTYPE_FRITZBOX_7113 = 60;

    public final static byte BOXTYPE_FRITZBOX_7240 = 73;

    public final static byte BOXTYPE_FRITZBOX_7570 = 75;

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
			"&var%3Alang=de&var%3Amenu=home&var%3Apagename=home&login%3Acommand%2F%LOGINMODE%=", //$NON-NLS-1$
			"&var%3Alang=en&var%3Amenu=home&var%3Apagename=home&login%3Acommand%2F%LOGINMODE%="}; //$NON-NLS-1$

	private final static String PATTERN_DETECT_FIRMWARE = "[Firmware|Labor][-| ][V|v]ersion[^\\d]*(\\d\\d).(\\d\\d).(\\d\\d\\d*)([^<]*)"; //$NON-NLS-1$

	private final static String PATTERN_WAIT_FOR_X_SECONDS = "var loginBlocked = parseInt\\(\"([^\"]*)\",10\\)";

	private final static String PATTERN_DETECT_LANGUAGE_DE = "Telefonie";

	private final static String PATTERN_DETECT_LANGUAGE_EN = "Telephony";

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
			String minorFirmwareVersion, String modFirmwareVersion, String language,
			boolean sidNecessary, String sid) {
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
	public static FritzBoxFirmware detectFirmwareVersion(
			String box_name, String box_address,
			String box_password, String port) throws WrongPasswordException, IOException, InvalidFirmwareException {
		long start = 0;
		long end = 0;

		start = JFritzUtils.getTimestamp();
		final String urlstr = "http://" + box_address +":" + port + "/cgi-bin/webcm"; //$NON-NLS-1$, //$NON-NLS-2$

		if(Main.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(Main.getProperty("option.clientCallList"))
				&& NetworkStateMonitor.isConnectedToServer()){

			Debug.netMsg("JFritz is configured as a client and using call list from server, canceling firmware detection");
			return null;
		}

		Vector<String> data = new Vector<String>();
		String language = "de";

		boolean detected = false;

		SIDLogin sidLogin = new SIDLogin();
		sidLogin.check(box_name, urlstr, box_password);

		for (int i=0; i<(POSTDATA_ACCESS_METHOD).length && !detected; i++)
		{
			for (int j=0; j<(POSTDATA_DETECT_FIRMWARE).length && !detected; j++)
			{
				String postdata = POSTDATA_ACCESS_METHOD[i] + POSTDATA_DETECT_FIRMWARE[j];
				if (sidLogin.isSidLogin())
				{
					postdata = postdata.replace("%LOGINMODE%", "response");
					postdata = postdata + URLEncoder.encode(sidLogin.getResponse(), "ISO-8859-1");
				}
				else
				{
					postdata = postdata.replace("%LOGINMODE%", "password");
					postdata = postdata + URLEncoder.encode(box_password, "ISO-8859-1");
				}
				data = JFritzUtils.fetchDataFromURLToVector(
						box_name, urlstr, postdata, true);

                if (false) {
                    String filename = "c://SpeedFirm.txt"; //$NON-NLS-1$
                    Debug.debug("Debug mode: Loading " + filename); //$NON-NLS-1$
                    try {
                        data.clear(); //$NON-NLS-1$
                        String thisLine;
                        BufferedReader in = new BufferedReader(new FileReader(filename));
                        while ((thisLine = in.readLine()) != null) {
                            data.add(thisLine);
                        }
                        in.close();
                    } catch (IOException e) {
                        Debug.error("File not found: " + filename); //$NON-NLS-1$
                    }
                }

//        		Debug.msg(data);

                Debug.debug("Parsing for Pattern 'Wait for x seconds'!");
    			int wait = 20;
    			Pattern waitSeconds = Pattern.compile(PATTERN_WAIT_FOR_X_SECONDS);
				Pattern detectDE = Pattern.compile(PATTERN_DETECT_LANGUAGE_DE);
				Pattern detectEN = Pattern.compile(PATTERN_DETECT_LANGUAGE_EN);

				long startParse = JFritzUtils.getTimestamp();
    			for (int k=0; k<data.size(); k++)
    			{
	    			Matcher m = waitSeconds.matcher(data.get(k));
	    			if (m.find())
	    			{
	    				try {
	  					wait = Integer.parseInt(m.group(1));
	    				}
	    				catch (NumberFormatException nfe)
	    				{
	    					wait = 20;
	    				}

	    				if (wait != 0)
	    				{
		    				Debug.warning("Wrong password! Waiting for " + wait + " seconds!"); //$NON-NLS-1$
		    				throw new WrongPasswordException(box_name,
		    						"Wrong password for box \"" + box_name + "\"! Could not detect FRITZ!Box firmware version.",
		    						wait); //$NON-NLS-1$
	    				}
	    			}


					m = detectDE.matcher(data.get(k));
					if (m.find()) {
						language = "de";
						detected = true;
						break;
					}

					if (!detected)
					{
						m = detectEN.matcher(data.get(k));
						if (m.find()) {
							language = "en";
							detected = true;
							break;
						}
					}
    			}
				long endParse = JFritzUtils.getTimestamp();
				Debug.debug("Used time to parse response: " + (endParse-startParse) + "ms");
			}
			if ( detected ) break;
		}

		if (!detected ) throw new InvalidFirmwareException();

		sidLogin.getSidFromResponse(data);

		long startTimestamp = JFritzUtils.getTimestamp();
        Debug.debug("Parsing data (" + data.size() + " lines) to detect firmware!");
		Pattern normalFirmware = Pattern.compile(PATTERN_DETECT_FIRMWARE);

		for (int k=data.size()-1; k>0; k--)
		{
			Matcher m = normalFirmware.matcher(data.get(k));
			if (m.find()) {
				String boxtypeString = m.group(1);
				String majorFirmwareVersion = m.group(2);
				String minorFirmwareVersion = m.group(3);
				String modFirmwareVersion = m.group(4).trim();

				Debug.info("Detected Firmware: " +
						boxtypeString + "." +
						majorFirmwareVersion + "." +
						minorFirmwareVersion +
						modFirmwareVersion + " " +
						language);
				if ((((Integer.parseInt(majorFirmwareVersion) == 4) && (Integer.parseInt(minorFirmwareVersion) >= 67))
					  || (Integer.parseInt(majorFirmwareVersion) > 4))
					&& (language.equals("en")))
					{
						// ab version xx.04.67 gibt es bei englischen Firmwares keine Unterscheidung mehr zwischen
						// internationaler und deutscher Firmware bei den URLs und Anrufliste.csv
						Debug.info("Detected international firmware greater than xx.04.67. Forcing to use german patterns.");
						language = "de";
					}

				long endTimestamp = JFritzUtils.getTimestamp();
				Debug.debug("Used time to detect Firmware: " + (endTimestamp - startTimestamp) + "ms");
				return new FritzBoxFirmware(boxtypeString, majorFirmwareVersion,
						minorFirmwareVersion, modFirmwareVersion, language,
						sidLogin.isSidLogin(), sidLogin.getSessionId());
			}
		}

		// no firmware found
		throw new InvalidFirmwareException();
	}

	private static FritzBoxFirmware detectFirmwareVersion2(
			String box_name, String box_address,
			String box_password, String port) throws WrongPasswordException, IOException, InvalidFirmwareException {
		final String urlstr = "http://" + box_address +":" + port + "/cgi-bin/webcm"; //$NON-NLS-1$, //$NON-NLS-2$

		if(Main.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(Main.getProperty("option.clientCallList"))
				&& NetworkStateMonitor.isConnectedToServer()){

			Debug.netMsg("JFritz is configured as a client and using call list from server, canceling firmware detection");
			return null;
		}

		Vector<String> data = new Vector<String>();
		String language = "de";

		boolean detected = false;

		SIDLogin sidLogin = new SIDLogin();
		sidLogin.check(box_name, urlstr, box_password);

		for (int i=0; i<(POSTDATA_ACCESS_METHOD).length && !detected; i++)
		{
			for (int j=0; j<(POSTDATA_DETECT_FIRMWARE).length && !detected; j++)
			{
				String postdata = POSTDATA_ACCESS_METHOD[i] + POSTDATA_DETECT_FIRMWARE[j];
				if (sidLogin.isSidLogin())
				{
					postdata = postdata.replace("%LOGINMODE%", "response");
					postdata = postdata + URLEncoder.encode(sidLogin.getResponse(), "ISO-8859-1");
				}
				else
				{
					postdata = postdata.replace("%LOGINMODE%", "password");
					postdata = postdata + URLEncoder.encode(box_password, "ISO-8859-1");
				}
				data = JFritzUtils.fetchDataFromURLToVector(
						box_name, urlstr, postdata, true);

                if (false) {
                    String filename = "c://SpeedFirm.txt"; //$NON-NLS-1$
                    Debug.debug("Debug mode: Loading " + filename); //$NON-NLS-1$
                    try {
                        data.clear(); //$NON-NLS-1$
                        String thisLine;
                        BufferedReader in = new BufferedReader(new FileReader(filename));
                        while ((thisLine = in.readLine()) != null) {
                            data.add(thisLine);
                        }
                        in.close();
                    } catch (IOException e) {
                        Debug.error("File not found: " + filename); //$NON-NLS-1$
                    }
                }

//        		Debug.msg(data);

                Debug.debug("Parsing for Pattern 'Wait for x seconds'!");
    			int wait = 20;
    			Pattern waitSeconds = Pattern.compile(PATTERN_WAIT_FOR_X_SECONDS);
				Pattern detectDE = Pattern.compile(PATTERN_DETECT_LANGUAGE_DE);
				Pattern detectEN = Pattern.compile(PATTERN_DETECT_LANGUAGE_EN);

    			for (int k=0; k<data.size(); k++)
    			{
	    			Matcher m = waitSeconds.matcher(data.get(k));
	    			if (m.find())
	    			{
	    				try {
	  					wait = Integer.parseInt(m.group(1));
	    				}
	    				catch (NumberFormatException nfe)
	    				{
	    					wait = 20;
	    				}

	    				if (wait != 0)
	    				{
		    				Debug.warning("Wrong password! Waiting for " + wait + " seconds!"); //$NON-NLS-1$
		    				throw new WrongPasswordException(box_name,
		    						"Wrong password for box \"" + box_name + "\"! Could not detect FRITZ!Box firmware version.",
		    						wait); //$NON-NLS-1$
	    				}
	    			}

					m = detectDE.matcher(data.get(k));
					if (m.find()) {
						language = "de";
						detected = true;
						break;
					}

					if (!detected)
					{
						m = detectEN.matcher(data.get(k));
						if (m.find()) {
							language = "en";
							detected = true;
							break;
						}
					}
    			}
			}
			if ( detected ) break;
		}

		if (!detected ) throw new InvalidFirmwareException();

		// Modded firmware: data = "> FRITZ!Box Fon WLAN, <span
		// class=\"Dialoglabel\">Modified-Firmware </span>08.03.37mod-0.55
		// \n</div>";
        Debug.debug("Parsing data (" + data.size() + " lines) to detect firmware!");
		Pattern normalFirmware = Pattern.compile(PATTERN_DETECT_FIRMWARE);

		for (int k=data.size()-1; k>0; k--)
		{
			Matcher m = normalFirmware.matcher(data.get(k));
			if (m.find()) {
				String boxtypeString = m.group(1);
				String majorFirmwareVersion = m.group(2);
				String minorFirmwareVersion = m.group(3);
				String modFirmwareVersion = m.group(4).trim();

				Debug.info("Detected Firmware: " +
						boxtypeString + "." +
						majorFirmwareVersion + "." +
						minorFirmwareVersion +
						modFirmwareVersion + " " +
						language);
				if ((((Integer.parseInt(majorFirmwareVersion) == 4) && (Integer.parseInt(minorFirmwareVersion) >= 67))
					  || (Integer.parseInt(majorFirmwareVersion) > 4))
					&& (language.equals("en")))
					{
						// ab version xx.04.67 gibt es bei englischen Firmwares keine Unterscheidung mehr zwischen
						// internationaler und deutscher Firmware bei den URLs und Anrufliste.csv
						Debug.info("Detected international firmware greater than xx.04.67. Forcing to use german patterns.");
						language = "de";
					}

				sidLogin.getSidFromResponse(data);

				return new FritzBoxFirmware(boxtypeString, majorFirmwareVersion,
						minorFirmwareVersion, modFirmwareVersion, language,
						sidLogin.isSidLogin(), sidLogin.getSessionId());
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
		if ( language.equals("en"))
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
	public final byte getMajorFirmwareVersion() {
		return majorFirmwareVersion;
	}

	/**
	 * @return Returns the minorFirmwareVersion.
	 */
	public final byte getMinorFirmwareVersion() {
		return minorFirmwareVersion;
	}

	/**
	 * @return Returns the majorFirmwareVersion.
	 */
	public final String getFirmwareVersion() {
        String boxtypeStr = Byte.toString(boxtype);
        String majorStr = Byte.toString(majorFirmwareVersion);
        String minorStr = Byte.toString(minorFirmwareVersion);
        if (boxtypeStr.length() == 1) { boxtypeStr = "0" + boxtypeStr; } //$NON-NLS-1$
        if (majorStr.length() == 1) { majorStr = "0" + majorStr; } //$NON-NLS-1$
        if (minorStr.length() == 1) { minorStr = "0" + minorStr; } //$NON-NLS-1$
       	return boxtypeStr + "." + majorStr + "." + minorStr + modFirmwareVersion; //$NON-NLS-1$,  //$NON-NLS-2$
	}

	public String getBoxName() {
		switch (boxtype) {
		case 6:
			return "FRITZ!Box Fon"; //$NON-NLS-1$
		case 8:
			return "FRITZ!Box Fon WLAN"; //$NON-NLS-1$
		case 14:
			return "FRITZ!Box 7050"; //$NON-NLS-1$
		case 12:
			return "FRITZ!Box 5050"; //$NON-NLS-1$
		case 11:
			return "FRITZ!Box ata"; //$NON-NLS-1$
		case 15:
			return "Eumex 300ip"; //$NON-NLS-1$
        case 23:
            return "FRITZ!Box 5010"; //$NON-NLS-1$
        case 25:
            return "FRITZ!Box 5012"; //$NON-NLS-1$
        case 28:
        	return "FRITZ!Box Fon WLAN Speedport W501V";
        case 29:
            return "FRITZ!Box 7170"; //$NON-NLS-1$
        case 30:
            return "FRITZ!Box 7140"; //$NON-NLS-1$
        case 34:
            return "FRITZ!Box Fon WLAN Speedport W900V";
        case 40:
            return "FRITZ!Box 7141"; //$NON-NLS-1$
        case 43:
            return "FRITZ!Box 5140"; //$NON-NLS-1$
        case 54:
            return "FRITZ!Box 7270"; //$NON-NLS-1$
        case 60:
            return "FRITZ!Box 7113"; //$NON-NLS-1$
        case 73:
        	return "FRITZ!Box 7240"; //$NON-NLS-1$
        case 75:
        	return "FRITZ!Box 7570"; //$NON-NLS-1$
		default:
			return Main.getMessage("unknown"); //$NON-NLS-1$
		}
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
}

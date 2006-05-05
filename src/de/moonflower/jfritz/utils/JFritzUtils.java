/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.QuickDial;
import de.moonflower.jfritz.utils.HTMLUtil;
import java.net.URLEncoder;

/**
 * Static class for data retrieval from the fritz box
 *
 * TODO: This class needs to be abstracted, so that subclasses for each boxtype
 * can be implemented.
 *
 * Notiz: Der Webserver auf der Fritzbox unterstützt leider kein UTF-8 als
 * URL-Codierung, deshalb habe ich ISO-8859-1 genommen
 *
 * @author akw
 *
 */
public class JFritzUtils {

    private static String POSTDATA_FETCH_CALLERLIST = "getpage=../html/de/FRITZ!Box_Anrufliste.csv&errorpage=..%2Fhtml%2Fde%2Fmenus%2Fmenu2.html&var%3Alang=de&var%3Apagename=foncalls&var%3Aerrorpagename=foncalls&var%3Amenu=fon&var%3Apagemaster=&time%3Asettings%2Ftime=1136559837%2C-60";

    private final static String POSTDATA_LIST = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=foncalls&login%3Acommand%2Fpassword=";

    private final static String POSTDATA_QUICKDIAL = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=kurzwahlen&login%3Acommand%2Fpassword="; //$NON-NLS-1$

    private final static String POSTDATA_QUICKDIAL_NEW = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=fonbuch&login%3Acommand%2Fpassword="; //$NON-NLS-1$

    private final static String POSTDATA_SIPPROVIDER = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=siplist&login%3Acommand%2Fpassword="; //$NON-NLS-1$

    private final static String POSTDATA_CLEAR = "&var%3Alang=de&var%3Apagename=foncalls&var%3Amenu=fon&telcfg%3Asettings/ClearJournal=1"; //$NON-NLS-1$

    private final static String POSTDATA_CALL = "&login:command/password=$PASSWORT&telcfg:settings/UseClickToDial=1&telcfg:command/Dial=$NUMMER&telcfg:settings/DialPort=$NEBENSTELLE"; //$NON-NLS-1$

    private final static String PATTERN_LIST_CSV_OLD = "(\\d);(\\d\\d.\\d\\d.\\d\\d \\d\\d:\\d\\d);([^;]*);([^;]*);([^;]*);(\\d:\\d\\d)"; //$NON-NLS-1$

    private final static String PATTERN_LIST_CSV_NEW = "(\\d);(\\d\\d.\\d\\d.\\d\\d \\d\\d:\\d\\d);([^;]*);([^;]*);([^;]*);([^;]*);(\\d:\\d\\d)"; //$NON-NLS-1$

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

    private final static int CALLTYPE = 0;

    private final static int DATE = 2;

    private final static int PORT = 4;

    private final static int DURATION = 5;

    private final static int ROUTE = 6;

    private final static int NAME = 1;

    private final static int NUMBER = 3;

    private final static int VANITY = 0;

    private final static int QUICKDIAL = 2;

    private static int[] calllist_indizes = { -1, -1, -1, -1, -1, -1, -1, -1 };

    private static int[] quickdial_indizes = { -1, -1, -1, -1 };

    /**
     * Detects type of fritz box by detecting the firmware version
     *
     * @param box_address
     * @return boxtype
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static FritzBoxFirmware detectBoxType(String firmware,
            String box_address, String box_password)
            throws WrongPasswordException, IOException {
        FritzBoxFirmware fw;
        try {
            fw = new FritzBoxFirmware(firmware);
            // FIXME: Debug
            // fw = new FritzBoxFirmware("14.03.88");
            Debug.msg("Using Firmware: " + fw + " (" + fw.getBoxName() + ")"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        } catch (InvalidFirmwareException e) {
            fw = FritzBoxFirmware.detectFirmwareVersion(box_address,
                    box_password);
            Debug.msg("Found Firmware: " + fw + " (" + fw.getBoxName() + ")"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        }
        return fw;
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
    public static void retrieveHTMLCallerList(String box_address,
            String password, String countryPrefix, String countryCode,
            String areaPrefix, String areaCode, FritzBoxFirmware firmware,
            JFritz jfritz) throws WrongPasswordException, IOException {

        String postdata = firmware.getAccessMethod() + POSTDATA_LIST
                + URLEncoder.encode(password, "ISO-8859-1"); //$NON-NLS-1$
        String urlstr = "http://" + box_address + "/cgi-bin/webcm"; //$NON-NLS-1$,  //$NON-NLS-2$
        Debug.msg("Postdata: " + postdata); //$NON-NLS-1$
        Debug.msg("Urlstr: " + urlstr); //$NON-NLS-1$
        fetchDataFromURL(urlstr, postdata, false);
    }

    /**
     *
     * @param firmware
     * @return Vector of QuickDial objects
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static Vector retrieveQuickDialsFromFritzBox(QuickDials model,
            FritzBoxFirmware firmware) throws WrongPasswordException,
            IOException {
        String postdata;
        if (firmware.getMajorFirmwareVersion() == 4
                && firmware.getMinorFirmwareVersion() >= 3) {
            postdata = firmware.getAccessMethod()
                    + POSTDATA_QUICKDIAL_NEW
                    + URLEncoder.encode(Encryption.decrypt(JFritz
                            .getProperty("box.password")), "ISO-8859-1"); //$NON-NLS-1$,  //$NON-NLS-2$
        } else {
            postdata = firmware.getAccessMethod()
                    + POSTDATA_QUICKDIAL
                    + URLEncoder.encode(Encryption.decrypt(JFritz
                            .getProperty("box.password")), "ISO-8859-1"); //$NON-NLS-1$,  //$NON-NLS-2$
        }
        String urlstr = "http://" //$NON-NLS-1$
                + JFritz.getProperty("box.address", "fritz.box") //$NON-NLS-1$,  //$NON-NLS-2$
                + "/cgi-bin/webcm"; //$NON-NLS-1$
        String data = fetchDataFromURL(urlstr, postdata, true);
        return parseQuickDialData(model, data, firmware);
    }

    /**
     * retrieves vector of SipProviders stored in the FritzBox
     *
     * @param box_address
     * @param box_password
     * @param firmware
     * @return Vector of SipProvider
     * @throws WrongPasswordException
     * @throws IOException
     *             author robotniko
     * @throws InvalidFirmwareException
     */
    public static Vector retrieveSipProvider(String box_address,
            String box_password, FritzBoxFirmware firmware)
            throws WrongPasswordException, IOException,
            InvalidFirmwareException {
        if (firmware == null)
            throw new InvalidFirmwareException("No valid firmware"); //$NON-NLS-1$
        String postdata = firmware.getAccessMethod() + POSTDATA_SIPPROVIDER
                + URLEncoder.encode(box_password, "ISO-8859-1"); //$NON-NLS-1$
        String urlstr = "http://" + box_address + "/cgi-bin/webcm"; //$NON-NLS-1$,  //$NON-NLS-2$
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
    public static Vector parseSipProvider(String data, FritzBoxFirmware firmware) {
        Vector list = new Vector();
        data = removeDuplicateWhitespace(data);
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
     * fetches html data from url using POST requests
     *
     * @param urlstr
     * @param postdata
     * @return html data
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static String fetchDataFromURL(String urlstr, String postdata, boolean retrieveData)
            throws WrongPasswordException, IOException {
        URL url = null;
        URLConnection urlConn;
        DataOutputStream printout;
        String data = ""; //$NON-NLS-1$
        boolean wrong_pass = false;

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
     * clears the caller list on the fritz box
     *
     * @param box_address
     * @param password
     * @throws IOException
     * @throws WrongPasswordException
     */
    public static void clearListOnFritzBox(String box_address, String password,
            FritzBoxFirmware firmware) throws WrongPasswordException,
            IOException {
        Debug.msg("Clearing List"); //$NON-NLS-1$
        String urlstr = "http://" + box_address + "/cgi-bin/webcm"; //$NON-NLS-1$,  //$NON-NLS-2$
        String postdata = firmware.getAccessMethod() + POSTDATA_CLEAR;
        fetchDataFromURL(urlstr, postdata, true);
    }

    /**
     * This function fetches the call list from the box
     * it first gets a connection to the fritz!Box
     * opens the call list html page, then it sends a
     * request to the box for the  csv file
     * then passes a bufferedReader to CallerList.importFromCSV()
     * it passes back to the caller if there were new callers or not
     * NOTE: Function no longer checks for invalid passwords!
     *
     *
     * LAST MODIFIED: 14.04.06 Brian Jensen
     *
     * @author Brian Jensen
     *
     * @param box_address
     * @param password
     * @param countryPrefix
     * @param countryCode
     * @param areaPrefix
     * @param areaCode
     * @param firmware
     * @param jfritz
     * @return if there were new calls or not
     * @throws WrongPasswordException
     * @throws IOException
     */

    public static boolean retrieveCSVList(String box_address, String password,
            String countryPrefix, String countryCode, String areaPrefix,
            String areaCode, FritzBoxFirmware firmware, JFritz jfritz)
            throws WrongPasswordException, IOException {

    	URL url;
    	URLConnection urlConn;
    	DataOutputStream printout;
        boolean wrong_pass = false;
        boolean newEntries = false;
        Debug.msg("Opening HTML Callerlist page");
		//retrieveHTMLCallerList(box_address, password, countryPrefix, countryCode,
	    //        			   areaPrefix, areaCode, firmware, jfritz);

        //Attempting to fetch the html version of the call list
        String postdata = firmware.getAccessMethod() + POSTDATA_LIST
        + URLEncoder.encode(password, "ISO-8859-1");
        String urlstr = "http://" + box_address + "/cgi-bin/webcm";

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
            	BufferedReader reader = new BufferedReader(new InputStreamReader(
            			urlConn.getInputStream()));

           		//read out the response data!
           	   	while(reader.skip(100000) > 0){
           	   		//kind of stupid, but it has to be
           	   		//If you don't read the list, you may not get an
           	   		//Updated list from the box
           	   	}

           	   	//close the streams
           	   	reader.close();
           		urlConn.getInputStream().close();

            } catch (IOException e1) {
            	throw new IOException("Network unavailable");
            }

        }

        //The list should be updated now
        //Get the csv file for processing
        Debug.msg("Retrieving the CSV list from the box");
        urlstr = "http://" + box_address + "/cgi-bin/webcm";

        try {
            url = new URL(urlstr);
        } catch (MalformedURLException e) {
            Debug.err("URL invalid: " + urlstr);
            throw new WrongPasswordException("URL invalid: " + urlstr);
        }

        //If the url is valid load the data
        if (url != null) {

        	urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Sending postdata to the fritz box
            urlConn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
            printout = new DataOutputStream(urlConn.getOutputStream());
            printout.writeBytes(POSTDATA_FETCH_CALLERLIST);
            printout.flush();
            printout.close();

            BufferedReader reader;

            try {
            	// Get response data from the box
            	reader = new BufferedReader(new InputStreamReader(urlConn
            			.getInputStream()));

           		//pass it on to the import function

            	Debug.msg("Recieved response, begin processin call list");
            	newEntries = jfritz.getCallerlist().importFromCSVFile(reader);
            	Debug.msg("Finished processing response");

            	//close the reader and the cocket connection
           		reader.close();
           		urlConn.getInputStream().close();

            } catch (IOException e1) {
            	throw new IOException("Network unavailable");
            }



            if (wrong_pass)
            	throw new WrongPasswordException("Password invalid");
        }

        //return if there were new entries or not
        return newEntries;
    }

    /**
     * removes all duplicate whitespaces from inputStr
     *
     * @param inputStr
     * @return outputStr
     */
    public static String removeDuplicateWhitespace(String inputStr) {
        Pattern p = Pattern.compile("\\s+"); //$NON-NLS-1$
        Matcher matcher = p.matcher(inputStr);
        String outputStr = matcher.replaceAll(" "); //$NON-NLS-1$
        outputStr.replaceAll(">\\s+<", "><"); //$NON-NLS-1$,  //$NON-NLS-2$
        return outputStr;
    }

    /**
     * creates a list of QuickDial objects
     *
     * @param data
     * @param firmware
     * @return list of QuickDial objects
     */
    public static Vector parseQuickDialData(QuickDials model, String data,
            FritzBoxFirmware firmware) {
        Vector list = new Vector();
        data = removeDuplicateWhitespace(data);
        Pattern p;
        if (firmware.getMajorFirmwareVersion() == 4
                && firmware.getMinorFirmwareVersion() >= 3 && firmware.getMinorFirmwareVersion() < 5) {
            p = Pattern.compile(PATTERN_QUICKDIAL_NEW);
            quickdial_indizes[NAME] = 1;
            quickdial_indizes[QUICKDIAL] = 3;
            quickdial_indizes[VANITY] = 4;
            quickdial_indizes[NUMBER] = 2;
        }
        else if (firmware.getMajorFirmwareVersion() == 4
                    && firmware.getMinorFirmwareVersion() >= 5) {
        	p = Pattern.compile(PATTERN_QUICKDIAL_BETA);
            quickdial_indizes[NAME] = 1;
            quickdial_indizes[QUICKDIAL] = 3;
            quickdial_indizes[VANITY] = 4;
            quickdial_indizes[NUMBER] = 2;
        }
        else {
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

    /**
     * Parses CSV data from the fritz!box.
     *
     * @param data
     */
    public static Vector parseCallerData(String data,
            FritzBoxFirmware firmware, String countryPrefix,
            String countryCode, String areaPrefix, String areaCode,
            JFritz jfritz) {
        Vector list = new Vector();
        Pattern p;
        Debug.msg(2, "FirmwareMajorVersion: " //$NON-NLS-1$
                + firmware.getMajorFirmwareVersion());
        Debug.msg(2, "FirmwareMinorVersion: " //$NON-NLS-1$
                + firmware.getMinorFirmwareVersion());
        if (firmware.getMajorFirmwareVersion() == 4
                && firmware.getMinorFirmwareVersion() >= 03) {
            p = Pattern.compile(PATTERN_LIST_CSV_NEW);
            calllist_indizes[CALLTYPE] = 1;
            calllist_indizes[DATE] = 2;
            calllist_indizes[NAME] = 3;
            calllist_indizes[NUMBER] = 4;
            calllist_indizes[PORT] = 5;
            calllist_indizes[ROUTE] = 6;
            calllist_indizes[DURATION] = 7;
        } else {
            p = Pattern.compile(PATTERN_LIST_CSV_OLD);
            calllist_indizes[CALLTYPE] = 1;
            calllist_indizes[DATE] = 2;
            calllist_indizes[NUMBER] = 3;
            calllist_indizes[PORT] = 4;
            calllist_indizes[ROUTE] = 5;
            calllist_indizes[DURATION] = 6;
        }

        Matcher m = p.matcher(data);
        while (m.find()) {
            try {
                Debug.msg("Found new call: " + m.group(0)); //$NON-NLS-1$

                CallType symbol = new CallType(Byte.parseByte(m
                        .group(calllist_indizes[CALLTYPE])));
                String port = m.group(calllist_indizes[PORT]);
                if (port.equals("FON 1")) { //$NON-NLS-1$
                    port = "0"; //$NON-NLS-1$
                } else if (port.equals("FON 2")) { //$NON-NLS-1$
                    port = "1"; //$NON-NLS-1$
                } else if (port.equals("FON 3")) { //$NON-NLS-1$
                    port = "2"; //$NON-NLS-1$
                } else if (port.equals("FON S0")) { //$NON-NLS-1$
                    port = "4"; //$NON-NLS-1$
                } else if (port.equals("DATA S0")) { //$NON-NLS-1$
                    port = "36"; //$NON-NLS-1$
                }

                PhoneNumber number = createAreaNumber(m
                        .group(calllist_indizes[NUMBER]), countryPrefix,
                        countryCode, areaPrefix, areaCode, jfritz);
                Date date = new SimpleDateFormat("dd.MM.yy HH:mm").parse(m //$NON-NLS-1$
                        .group(calllist_indizes[DATE]));
                String route = ""; //$NON-NLS-1$
                if (calllist_indizes[ROUTE] != -1) {
                    route = m.group(calllist_indizes[ROUTE]);
                    if (route.startsWith("Internet: ")) { //$NON-NLS-1$
                        Enumeration en = jfritz.getSIPProviderTableModel()
                                .getProviderList().elements();
                        while (en.hasMoreElements()) {
                            SipProvider sipProvider = (SipProvider) en
                                    .nextElement();
                            if (sipProvider.getNumber().equals(route.substring(10))) {
                                route = "SIP" + sipProvider.getProviderID(); //$NON-NLS-1$
                                break;
                            }
                        }
                    }
                }
                String[] durationStrings = m.group(calllist_indizes[DURATION])
                        .split(":"); //$NON-NLS-1$
                int duration = 0;
                if (durationStrings.length == 1) {
                    duration = Integer.parseInt(durationStrings[0]);
                } else
                    duration = Integer.parseInt(durationStrings[0]) * 3600
                            + Integer.parseInt(durationStrings[1]) * 60;

                list.add(new Call(jfritz, symbol, date, number, port, route,
                        duration));
            } catch (ParseException e) {
                Debug.err(e.toString());
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
    public static PhoneNumber createAreaNumber(String number,
            String countryPrefix, String countryCode, String areaPrefix,
            String areaCode, JFritz jfritz) {
        PhoneNumber phoneNumber;
        if (!number.equals("")) { //$NON-NLS-1$
            if (number.length() < 3) { // Short Number (maybe internal)
                phoneNumber = new PhoneNumber(number);
                return phoneNumber;
            }
            if (number.startsWith("**7")) //$NON-NLS-1$
            	// QuickDial
            {
                int count = 5;
                Debug.msg("Quickdial: " + number //$NON-NLS-1$
                        + "! Replace quickdial with number."); //$NON-NLS-1$
                while (count > 0) {
                    count--;
                    // replace QuickDial with
                    // QuickDial-Entry
                    String quickDialNumber = number.substring(3);
                    if (jfritz.getJframe().getQuickDialPanel().getDataModel()
                            .getQuickDials().size() == 0) {
                        // get QuickDials from FritzBox
                        jfritz.getJframe().getQuickDialPanel().getDataModel()
                                .getQuickDialDataFromFritzBox();
                    }
                    Enumeration en = jfritz.getJframe().getQuickDialPanel()
                            .getDataModel().getQuickDials().elements();
                    while (en.hasMoreElements()) {
                        QuickDial quickDial = (QuickDial) en.nextElement();
                        if (quickDialNumber.equals(quickDial.getQuickdial())) {
                            number = null;
                            phoneNumber = new PhoneNumber(quickDial.getNumber());
                            Debug.msg("Quickdial. Number: " //$NON-NLS-1$
                                    + phoneNumber.getFullNumber());
                            return phoneNumber;
                        }
                    }
                    Debug
                            .msg("No quickdial found. Actualize quickdial-list"); //$NON-NLS-1$
                    ((QuickDials) jfritz.getJframe().getQuickDialPanel()
                            .getDataModel()).getQuickDialDataFromFritzBox();
                }
            }

            if (number.startsWith(countryCode) && number.length() > 9) {
                // International numbers without countryPrefix
                // (some VOIP numbers)
                number = countryPrefix + number;
            }
            if (number.startsWith(countryPrefix)) { // International call

                if (number.startsWith(countryPrefix + countryCode)) {
                    // if own country, remove countrycode
                    number = areaPrefix
                            + number.substring(countryPrefix.length()
                                    + countryCode.length());
                }
            } else if (!number.startsWith(areaPrefix)) {
                number = areaPrefix + areaCode + number;
            }
            phoneNumber = new PhoneNumber(number);
            return phoneNumber;
        }
        return null;
    }

    /**
     * creates a String with version and date of CVS Id-Tag
     *
     * @param tag
     * @return String with version and date of CVS Id-Tag
     */
    public static String getVersionFromCVSTag(String tag) {
        String[] parts = tag.split(" "); //$NON-NLS-1$
        return "CVS v" + parts[2] + " (" + parts[3] + ")"; //$NON-NLS-1$, //$NON-NLS-2$,  //$NON-NLS-3$
    }

    public static boolean parseBoolean(String input) {
        if (input != null && input.equalsIgnoreCase("true")) //$NON-NLS-1$
            return true;
        else
            return false;
    }

    public static String lookupAreaCode(String number) {
        Debug.msg("Looking up " + number + "..."); //$NON-NLS-1$,  //$NON-NLS-2$
        // FIXME: Does not work (Cookies)
        String urlstr = "http://www.vorwahl.de/national.php"; //$NON-NLS-1$
        String postdata = "search=1&vorwahl=" + number; //$NON-NLS-1$
        String data = ""; //$NON-NLS-1$
        try {
            data = fetchDataFromURL(urlstr, postdata, true);
        } catch (Exception e) {
        }
        Debug.msg("DATA: " + data.trim()); //$NON-NLS-1$
        return ""; //$NON-NLS-1$
    }

    public static String convertSpecialChars(String input) {
        // XML Sonderzeichen durch ASCII Codierung ersetzen
        String out = input;
        out = out.replaceAll("&", "&#38;"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("'", "&#39;"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("<", "&#60;"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll(">", "&#62;"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("\"", "&#34;"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("=", "&#61;"); //$NON-NLS-1$,  //$NON-NLS-2$
        return out;
    }

    public static String deconvertSpecialChars(String input) {
        // XML Sonderzeichen durch ASCII Codierung ersetzen
        String out = input;
        out = out.replaceAll("&#38;", "&"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("&#39;", "'"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("&#60;", "<"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("&#62;", ">"); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("&#61;", "="); //$NON-NLS-1$,  //$NON-NLS-2$
        out = out.replaceAll("&#34;", "\""); //$NON-NLS-1$,  //$NON-NLS-2$
        return out;
    }

    public static void doCall(String number, String port,
            FritzBoxFirmware firmware) {
        try {
            String passwort = Encryption.decrypt(JFritz.getProperty(
                    "box.password", Encryption.encrypt(""))); //$NON-NLS-1$,  //$NON-NLS-2$
            number = number.replaceAll("\\+", "00"); //$NON-NLS-1$,  //$NON-NLS-2$

            String portStr = ""; //$NON-NLS-1$
            if (port.equals("Fon 1")) { //$NON-NLS-1$
                portStr = "1"; //$NON-NLS-1$
            } else if (port.equals("Fon 2")) { //$NON-NLS-1$
                portStr = "2"; //$NON-NLS-1$
            } else if (port.equals("Fon 3")) { //$NON-NLS-1$
                portStr = "3"; //$NON-NLS-1$
            } else if (port.equals(JFritz.getMessage("analoge_telephones_all"))) { //$NON-NLS-1$
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
                    URLEncoder.encode(passwort, "ISO-8859-1")); //$NON-NLS-1$
            postdata = postdata.replaceAll("\\$NUMMER", number); //$NON-NLS-1$
            postdata = postdata.replaceAll("\\$NEBENSTELLE", portStr); //$NON-NLS-1$

            postdata = firmware.getAccessMethod() + postdata;

            String urlstr = "http://" //$NON-NLS-1$
                    + JFritz.getProperty("box.address", "fritz.box") //$NON-NLS-1$, //$NON-NLS-2$
                    + "/cgi-bin/webcm"; //$NON-NLS-1$
            fetchDataFromURL(urlstr, postdata, true);
        } catch (UnsupportedEncodingException uee) {
        } catch (WrongPasswordException wpe) {
        } catch (IOException ioe) {
        }

    }

}
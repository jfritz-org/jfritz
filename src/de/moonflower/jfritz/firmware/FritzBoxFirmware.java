/*
 *
 * Created on 17.05.2005
 *
 */
package de.moonflower.jfritz.firmware;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
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

	public final static byte BOXTYPE_FRITZBOX_5050 = 12;

	public final static byte BOXTYPE_FRITZBOX_7050 = 14;

	public final static byte ACCESS_METHOD_POST_0342 = 0;

	public final static byte ACCESS_METHOD_PRIOR_0342 = 1;

	private byte boxtype;

	private byte majorFirmwareVersion;

	private byte minorFirmwareVersion;

	private String modFirmwareVersion;

	private final static String[] POSTDATA_ACCESS_METHOD = {
			"getpage=../html/de/menus/menu2.html",
			"getpage=../html/menus/menu2.html" };

	private final static String POSTDATA_DETECT_FIRMWARE = "&var%3Alang=de&var%3Amenu=home&var%3Apagename=home&login%3Acommand%2Fpassword=";

	private final static String PATTERN_DETECT_FIRMWARE = "<span class=\"Dialoglabel\">[^<]*</span>(\\d\\d).(\\d\\d).(\\d\\d)([^<]*)";

	/**
	 * Firmware Constructor using Bytes
	 *
	 * @param boxtype
	 * @param majorFirmwareVersion
	 * @param minorFirmwareVersion
	 */
	public FritzBoxFirmware(byte boxtype, byte majorFirmwareVersion,
			byte minorFirmwareVersion) {
		this.boxtype = boxtype;
		this.majorFirmwareVersion = majorFirmwareVersion;
		this.minorFirmwareVersion = minorFirmwareVersion;
		this.modFirmwareVersion = "";
	}

	/**
	 * Firmware Constructor using Strings
	 *
	 * @param boxtype
	 * @param majorFirmwareVersion
	 * @param minorFirmwareVersion
	 */
	public FritzBoxFirmware(String boxtype, String majorFirmwareVersion,
			String minorFirmwareVersion) {
		this.boxtype = Byte.parseByte(boxtype);
		this.majorFirmwareVersion = Byte.parseByte(majorFirmwareVersion);
		this.minorFirmwareVersion = Byte.parseByte(minorFirmwareVersion);
		this.modFirmwareVersion = "";
	}

	/**
	 * Firmware Constructor using Strings
	 *
	 * @param boxtype
	 * @param majorFirmwareVersion
	 * @param minorFirmwareVersion
	 * @param modFirmwareVersion
	 */
	public FritzBoxFirmware(String boxtype, String majorFirmwareVersion,
			String minorFirmwareVersion, String modFirmwareVersion) {
		this.boxtype = Byte.parseByte(boxtype);
		this.majorFirmwareVersion = Byte.parseByte(majorFirmwareVersion);
		this.minorFirmwareVersion = Byte.parseByte(minorFirmwareVersion);
		this.modFirmwareVersion = modFirmwareVersion;
	}

	/**
	 * Firmware Constructor using a single String
	 *
	 * @param firmware
	 *            Firmware string like '14.06.37'
	 */
	public FritzBoxFirmware(String firmware) throws InvalidFirmwareException {
		String mod = "";
		if (firmware == null)
			throw new InvalidFirmwareException("No firmware found");
		if (firmware.indexOf("mod")>0) {
			mod = firmware.substring(firmware.indexOf("mod"));
			firmware = firmware.substring(0, firmware.indexOf("mod"));
		} else if (firmware.indexOf("ds-")>0) { // danisahne MOD
            mod = firmware.substring(firmware.indexOf("ds-"));
            firmware = firmware.substring(0, firmware.indexOf("ds-"));
        } else if (firmware.indexOf("m")>0) {
            mod = firmware.substring(firmware.indexOf("m"));
            firmware = firmware.substring(0, firmware.indexOf("m"));
        } else if (firmware.indexOf("-")>0) { // BETA Firmware von AVM
			mod = firmware.substring(firmware.indexOf("-"));
			firmware = firmware.substring(0, firmware.indexOf("-"));
		}
		String[] parts = firmware.split("\\.");
		if (parts.length != 3)
			throw new InvalidFirmwareException("Firmware number crippled");

		this.boxtype = Byte.parseByte(parts[0]);
		this.majorFirmwareVersion = Byte.parseByte(parts[1]);
		this.minorFirmwareVersion = Byte.parseByte(parts[2]);
		this.modFirmwareVersion = mod;
	}


	/**
	 * Static method for firmware detection
	 *
	 * @param box_address
	 * @param box_password
	 * @return New instance of FritzBoxFirmware
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static FritzBoxFirmware detectFirmwareVersion(String box_address,
			String box_password) throws WrongPasswordException, IOException {
		final String urlstr = "http://" + box_address + "/cgi-bin/webcm";

		String data = "";
		int i = 0;

		// Try postdata's until code is found
		while ((data.length() == 0) && (i < POSTDATA_ACCESS_METHOD.length)) {
			data = JFritzUtils.fetchDataFromURL(
					urlstr,
					POSTDATA_ACCESS_METHOD[i] + POSTDATA_DETECT_FIRMWARE
							+ URLEncoder.encode(box_password, "ISO-8859-1")).trim();
			i++;
		}
		// Modded firmware: data = "> FRITZ!Box Fon WLAN, <span
		// class=\"Dialoglabel\">Modified-Firmware </span>08.03.37mod-0.55
		// \n</div>";
		Pattern p = Pattern.compile(PATTERN_DETECT_FIRMWARE);
		Matcher m = p.matcher(data);
		if (m.find()) {
			String boxtypeString = m.group(1);
			String majorFirmwareVersion = m.group(2);
			String minorFirmwareVersion = m.group(3);
			String modFirmwareVersion = m.group(4).trim();
			return new FritzBoxFirmware(boxtypeString, majorFirmwareVersion,
					minorFirmwareVersion, modFirmwareVersion);
		} else {
			System.err.println("detectFirmwareVersion: Password wrong?");
			throw new WrongPasswordException(
					"Could not detect FRITZ!Box firmware version.");
		}
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
	 * TODO: Später noch die Major-Version mit einbeziehen, falls es mit neueren
	 * Versionen nicht klappen sollte
	 *
	 */
	public final String getAccessMethod() {
		int accessMethod;
		if (minorFirmwareVersion < 42)
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
		DecimalFormat df = new DecimalFormat("##,##,##");
		return df.format(boxtype * 10000 + majorFirmwareVersion * 100
				+ minorFirmwareVersion)
				+ modFirmwareVersion;
	}

	public String getBoxName() {
		switch (boxtype) {
		case 6:
			return "FRITZ!Box Fon";
		case 8:
			return "FRITZ!Box Fon WLAN";
		case 14:
			return "FRITZ!Box 7050";
		case 12:
			return "FRITZ!Box 5050";
		case 11:
			return "FRITZ!Box ata";
		default:
			return "unknown";
		}
	}

	public final String toString() {
		return getFirmwareVersion();
	}
}

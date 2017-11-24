package de.moonflower.jfritz.importexport;

import java.util.Hashtable;
import java.util.Vector;

public class VCardParser {

	private String property;

	private Hashtable<String, Vector<String>> propertyType;

	private Vector<String> values;

	public void parseLine(final String line) {
		if (line.length()>0) {
			property = "";
			propertyType = new Hashtable<String, Vector<String>>();
			values = new Vector<String>();

			// do the parsing
			int propertyDelimiter = line.indexOf(":");
			String propertyFields = line.substring(0, propertyDelimiter);
			String valueFields = line.substring(propertyDelimiter + 1);

			parsePropertyFields(propertyFields);
			parseValueFields(valueFields);
		}
	}

	private void parsePropertyFields(final String propertyFields) {
		Vector<String> items = splitFields(propertyFields);
		if (items.size() > 0) {
			this.property = items.get(0).trim().toLowerCase();

			if (items.size() > 1) {
				Vector<String> propertyTypeValues = null;
				for (int i=1; i<items.size(); i++) {
					if (items.get(i).indexOf("=") != -1) {
						// contains a =
						// possible values: ENCODING = BASE64, QUOTED-PRINTABLE, 8BIT
						//					CHARSET = ISO-8859-8, UTF-8, ...
						//					LANGUAGE = de-DE, en-US, ...
						//					VALUE = URL, CONTENT-ID
						// PHOTO;TYPE = GIF, CGM, WMF, BMP, MET, PMB, DIB, PICT, TIFF, PS, PDF, JPEG, MPEG, MPEG2, AVI, QTIME
						// LOGO;TYPE = GIF, CGM, WMF, BMP, MET, PMB, DIB, PICT, TIFF, PS, PDF, JPEG, MPEG, MPEG2, AVI, QTIME
						// ADR;TYPE = DOM, INTL, POSTAL, PARCEL, HOME, WORK
						// TEL;TYPE = PREF, WORK, HOME, VOICE, FAX, MSG, CELL, PAGER, BBS, MODEM, CAR, ISDN, VIDEO
						// EMAIL;TYPE = AOL, AppleLink, ATTMail, CIS, eWorld, INTERNET, IBMMail, MCIMail, POWERSHARE, PRODIGY, TLX, X400

						// TEL;TYPE=HOME;VOICE;ENCODING=ISO-8859-8:

						String[] split = items.get(i).split("=");
						propertyTypeValues = new Vector<String>();
						propertyTypeValues.add(split[1].trim().toLowerCase());
						propertyType.put(split[0].trim().toLowerCase(), propertyTypeValues);

					} else {
						if (propertyTypeValues == null) {
							propertyTypeValues = new Vector<String>();
							propertyType.put("type", propertyTypeValues);
						}
						propertyTypeValues.add(items.get(i).trim().toLowerCase());
					}
				}
			}
		}
	}

	private void parseValueFields(final String valueFields) {
		this.values = splitFields(valueFields);
	}

	/**
	 * Split a line by ; with correct handling of escape characters
	 * @param line
	 * @return
	 */
	private Vector<String> splitFields(String line) {
		String[] split = line.split(";");

		Vector<String> items = new Vector<String>(split.length);
		// detect escaped ; character
		String corrected = "";
		for (int i=0; i<split.length; i++) {
			if (split[i].endsWith("\"")) {
				corrected += ";" + split[i];
			} else {
				if (corrected.equals("")) {
					items.add(split[i].trim());
				} else {
					corrected += split[i];
					items.add(corrected.trim());
					corrected = "";
				}
			}
		}

		return items;
	}

	public String getProperty() {
		return property;
	}

	public Hashtable<String, Vector<String>> getPropertyType() {
		return propertyType;
	}

	public Vector<String> getValues() {
		return values;
	}
}

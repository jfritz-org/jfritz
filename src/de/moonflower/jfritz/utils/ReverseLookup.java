/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.ReverseLookupSwitzerland;
/**
 * Class for telephone number reverse lookup using "dasoertliche.de"
 *
 * @author Arno Willig
 *
 */
public class ReverseLookup {

	//Sort the following COUNTRY_CODE list alphabetically

	public static final String AUSTRIA_CODE = "+43";

	public static final String CANADA_CODE = "+1";

	public static final String FANCE_CODE = "+33";

	public static final String ITALY_CODE = "+39";

	public static final String GERMANY_CODE = "+49";

	public static final String GREATBRITAIN_CODE = "+44";

	public static final String RUSSIA_CODE = "+7";

	public static final String SPAIN_CODE = "+34";

	public static final String SWITZERLAND_CODE = "+41";

	public static final String USA_CODE = "+1";




	public static Person lookup(PhoneNumber number) {
		Person newPerson;
/**		if (number.isMobile()) {
			newPerson = new Person();
			newPerson.addNumber(number);
			Debug.msg("Adding mobile " + number.getIntNumber()); //$NON-NLS-1$
		} else **/
        if (number.isFreeCall()) {
			newPerson = new Person("", "FreeCall"); //$NON-NLS-1$,  //$NON-NLS-2$
			newPerson.addNumber(number);
		} else if (number.isSIPNumber() || number.isQuickDial()) {
		    newPerson = new Person ();
		    newPerson.addNumber(number);
		} else {
			if(number.convertToIntNumber().startsWith(SWITZERLAND_CODE))
				newPerson = ReverseLookupSwitzerland.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(ITALY_CODE))
				newPerson = ReverseLookupItaly.lookup(number.getAreaNumber());
			else if(number.convertToIntNumber().startsWith(GERMANY_CODE))
				newPerson = lookupDasOertliche(number.getAreaNumber());
			else
				newPerson = new Person ();
				newPerson.addNumber(number.getAreaNumber(), "home");
		}
		return newPerson;
	}

	/**
	 * Static method for looking up entries from "dasoertliche"
	 *
	 * @param number
	 * @return name
	 */
	public static Person lookupDasOertliche(String number) {
		if (number.equals("")) { //$NON-NLS-1$
			return null;
		}
		Debug.msg("Looking up " + number + "..."); //$NON-NLS-1$,  //$NON-NLS-2$
		URL url = null;
		String data = ""; //$NON-NLS-1$
		Person newPerson;

		String urlstr = "http://www.dasoertliche.de/DB4Web/es/oetb2suche/home.htm?main=Antwort&s=2&kw_invers=" //$NON-NLS-1$
				+ number;
		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					con = url.openConnection();

					String header = ""; //$NON-NLS-1$
					String charSet = ""; //$NON-NLS-1$
					for (int i = 0;; i++) {
						String headerName = con.getHeaderFieldKey(i);
						String headerValue = con.getHeaderField(i);

						if (headerName == null && headerValue == null) {
							// No more headers
							break;
						}
						if ("content-type".equalsIgnoreCase(headerName)) { //$NON-NLS-1$
							String[] split = headerValue.split(" ", 2); //$NON-NLS-1$
							for (int j = 0; j < split.length; j++) {
								split[j] = split[j].replaceAll(";", ""); //$NON-NLS-1$,  //$NON-NLS-2$
								if (split[j].toLowerCase().startsWith(
										"charset=")) { //$NON-NLS-1$
									String[] charsetSplit = split[j].split("="); //$NON-NLS-1$
									charSet = charsetSplit[1];
								}
							}
						}
						header += headerName + ": " + headerValue + " | "; //$NON-NLS-1$,  //$NON-NLS-2$
					}
					Debug.msg("Header of dasoertliche.de: " + header); //$NON-NLS-1$
					Debug.msg("CHARSET : " + charSet); //$NON-NLS-1$

					// Get used Charset
					BufferedReader d;
					if (charSet.equals("")) { //$NON-NLS-1$
						d = new BufferedReader(new InputStreamReader(con
								.getInputStream(), "ISO-8859-1")); //$NON-NLS-1$
					} else {
						d = new BufferedReader(new InputStreamReader(con
								.getInputStream(), charSet));
					}
					int i = 0;
					String str = ""; //$NON-NLS-1$

					// Get response data
					while ((i < 700) && (null != ((str = d.readLine())))) {
						data += str;
						i++;
					}
					d.close();
					Debug.msg("Begin processing responce from dasoertliche.de");
					//This just makes the debug output unreadable!
					//Debug.msg("DasOertliche Webpage: " + data); //$NON-NLS-1$
					Pattern p = Pattern
							.compile("<a\\s*class=\"blb\" href=\"[^\"]*\">([^<]*)</a>(?:<br>([^<]*))?</td>"); //$NON-NLS-1$

					Matcher m = p.matcher(data);
					// Get name and address
					if (m.find()) {
						String line1 = m.group(1).trim();
						Debug.msg(3, "Pattern1: " + line1); //$NON-NLS-1$

						String[] split = line1.split(" ", 2); //$NON-NLS-1$
						String firstname = "", //$NON-NLS-1$
								lastname = "", //$NON-NLS-1$
								company = "", //$NON-NLS-1$
								address = "", //$NON-NLS-1$
								zipcode = "", //$NON-NLS-1$
								city = ""; 	  //$NON-NLS-1$
						lastname = split[0];
						if (split.length > 1) {
							firstname = " " + split[1]; //$NON-NLS-1$
							Debug.msg("*" + firstname + "*" //$NON-NLS-1$,  //$NON-NLS-2$
									+ firstname.indexOf("  ")); //$NON-NLS-1$
							if ((firstname.indexOf("  ") > -1) //$NON-NLS-1$
									&& (firstname.indexOf("  u.") == -1)) { //$NON-NLS-1$
								company = firstname.substring(
										firstname.indexOf("  ")).trim(); //$NON-NLS-1$
								firstname = firstname.substring(0,
										firstname.indexOf("  ")).trim(); //$NON-NLS-1$
							} else {
								firstname = firstname.replaceAll("  u. ", //$NON-NLS-1$
										" und "); //$NON-NLS-1$
							}
						}
						firstname = firstname.trim();
						if (m.group(2) != null) { // there is an address
							String line2 = m.group(2).trim();
							Debug.msg(3, "Pattern2: " + line2); //$NON-NLS-1$
							split = line2.split(", ", 2); //$NON-NLS-1$
							String zipcity = ""; //$NON-NLS-1$
							if (split.length > 1) {
								address = split[0].trim();
								zipcity = split[1].trim();
							} else {
								zipcity = split[0].trim();
								address = ""; //$NON-NLS-1$
							}
							split = zipcity.split(" ", 2); //$NON-NLS-1$
							if (split.length > 1) {
								zipcode = split[0].trim();
								city = split[1].trim();
							} else {
								city = split[0].trim();
							}
						}

						Debug.msg("Firstname: " + firstname); //$NON-NLS-1$
						Debug.msg("Lastname: " + lastname); //$NON-NLS-1$
						Debug.msg("Company: " + company); //$NON-NLS-1$
						Debug.msg("Address: " + address); //$NON-NLS-1$
						Debug.msg("ZipCode: " + zipcode); //$NON-NLS-1$
						Debug.msg("City: " + city); //$NON-NLS-1$

						newPerson = new Person(firstname, company, lastname,
								address, zipcode, city, ""); //$NON-NLS-1$
						if (company.length() > 0) {
							newPerson.addNumber(number, "business"); //$NON-NLS-1$
						} else {
							newPerson.addNumber(number, "home"); //$NON-NLS-1$
						}
						return newPerson;
					}
				} catch (IOException e1) {
					Debug.err("Error while retrieving " + urlstr); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
		}
		newPerson = new Person();
		newPerson.addNumber(number, "home"); //$NON-NLS-1$
		return newPerson;
	}
}

package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.HTMLUtil;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * This class performs a reverse lookup
 *
 * @author Brian Jensen
 * @param number in area format to be looked up
 *
 * @return a person object created using the data from the site
 */
public class ReverseLookupGermanyOertliche {

	public final static String SEARCH_URL="http://www.dasoertliche.de/?form_name=search_inv&page=RUECKSUCHE&context=RUECKSUCHE&action=STANDARDSUCHE&la=de&rci=no&ph=";

	/**
	 * Static method for looking up entries from "dastelefonbuch.de"
	 *
	 * @param number
	 * @return name
	 */
	public static Person lookup(String number) {
		if (number.equals("")) { //$NON-NLS-1$
			return null;
		}
		Debug.msg("Looking up " + number + " using dasoertliche..."); //$NON-NLS-1$,  //$NON-NLS-2$
		URL url = null;
		String data = ""; //$NON-NLS-1$
		Person newPerson;

		String urlstr = SEARCH_URL + number.replaceAll("\\+","%2B");
		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					con = url.openConnection();
					// 5 Sekunden-Timeout für Verbindungsaufbau
					//15 seconds for the response
					con.setConnectTimeout(5000);
					con.setReadTimeout(15000);

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

					String str = ""; //$NON-NLS-1$

					while (null != ((str = d.readLine())))
							data += str;

					d.close();
					Debug.msg("Begin processing responce from dasoertliche.de");
					Debug.msg(data);

					Pattern pName = Pattern
							.compile("class=\"entry\">([^<]*)</a>");
					Pattern pAddress = Pattern
							.compile("([^,>]*),([^<]*)<br/>");

					String firstname = "", //$NON-NLS-1$
					lastname = "", //$NON-NLS-1$
					company = "", //$NON-NLS-1$
					address = "", //$NON-NLS-1$
					zipcode = "", //$NON-NLS-1$
					city = ""; 	  //$NON-NLS-1$

					Matcher mName = pName.matcher(data);
					// Get name part
					if (mName.find()) {
						Debug.msg("Found a name match");
						String line1 = mName.group(1).trim();
						String[] split = line1.split(" ", 2); //$NON-NLS-1$

						lastname = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(split[0]));
						if (split.length > 1) {
							firstname = " " + HTMLUtil.stripEntities(split[1]); //$NON-NLS-1$
							if ((firstname.indexOf("  ") > -1) //$NON-NLS-1$
									&& (firstname.indexOf("  u.") == -1)) { //$NON-NLS-1$
								company = JFritzUtils.removeLeadingSpaces(firstname.substring(
										firstname.indexOf("  ")).trim()); //$NON-NLS-1$
								firstname = JFritzUtils.removeLeadingSpaces(firstname.substring(0,
										firstname.indexOf("  ")).trim()); //$NON-NLS-1$
							} else {
								firstname = JFritzUtils.removeLeadingSpaces(firstname.replaceAll("  u. ", //$NON-NLS-1$
										" und ")); //$NON-NLS-1$
							}
						}

						firstname = JFritzUtils.removeLeadingSpaces(firstname.trim());
					}

					//get address part, if its available
					Matcher mAddress = pAddress.matcher(data);
					if(mAddress.find()){
						Debug.msg("found an address match");
						address = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(mAddress.group(1).trim()));
						String line2 = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(mAddress.group(2)).trim());
						String split[] = line2.split(" ", 2);
						zipcode = JFritzUtils.removeLeadingSpaces(split[0]);
						if(split.length == 2){
							city = JFritzUtils.removeLeadingSpaces(split[1]);
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

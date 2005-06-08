/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.struct.Person;

/**
 * Class for telephone number reverse lookup using "dasoertliche.de"
 *
 * @author Arno Willig
 *
 */
public class ReverseLookup {

	static HashMap mobileMap;

	/**
	 *
	 */
	public ReverseLookup() {
		createMobileMap();
	}

	/**
	 * creates a map of german cellphone providers
	 */
	private void createMobileMap() {
		mobileMap = new HashMap();
		mobileMap.put("0151", "D1");
		mobileMap.put("0160", "D1");
		mobileMap.put("0170", "D1");
		mobileMap.put("0171", "D1");
		mobileMap.put("0175", "D1");
		mobileMap.put("0152", "D2");
		mobileMap.put("0162", "D2");
		mobileMap.put("0172", "D2");
		mobileMap.put("0173", "D2");
		mobileMap.put("0174", "D2");
		mobileMap.put("0163", "E+");
		mobileMap.put("0177", "E+");
		mobileMap.put("0178", "E+");
		mobileMap.put("0159", "O2");
		mobileMap.put("0176", "O2");
		mobileMap.put("0179", "O2");
	}

	public static Person lookup(String number) {
		Person newPerson;
		if (numberIsMobile(number)) {
			newPerson = new Person();
			newPerson.addNumber(number, "mobile");
			Debug.msg("Adding mobile " + number);
		} else if (numberIsFreecall(number)) {
			newPerson = new Person("", "FreeCall");
			newPerson.addNumber(number, "business");
		} else {
			newPerson = lookupDasOertliche(number);
		}
		return newPerson;
	}

	public static boolean numberIsMobile(String number) {
		return number.length() > 3
				&& mobileMap.containsKey(number.substring(0, 4));
	}

	public static boolean numberIsFreecall(String number) {
		return number.startsWith("0800");
	}

	public static String getMobileProvider(String number) {
		if (number.length() < 5)
			return "";
		Object provider = mobileMap.get(number.substring(0, 4));
		if (provider == null)
			return "";
		return mobileMap.get(number.substring(0, 4)).toString();
	}

	/**
	 * Static method for looking up entries from "dasoertliche"
	 *
	 * @param number
	 * @return name
	 */

	public static Person lookupDasOertliche(String number) {
		Debug.msg("Looking up " + number + "...");
		URL url = null;
		URLConnection urlConn;
		DataOutputStream printout;
		String data = "";
		boolean wrong_pass = false;
		boolean wrong_url = false;
		Person newPerson;

		String urlstr = "http://www.dasoertliche.de/DB4Web/es/oetb2suche/home.htm?main=Antwort&s=2&kw_invers="
				+ number;
		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr);
		}
		if (url != null) {

			URLConnection con;
			try {
				con = url.openConnection();

				// Get response data
				BufferedReader d = new BufferedReader(new InputStreamReader(con
						.getInputStream()));
				int i = 0;
				String str = "";

				while ((i < 700) && (null != ((str = d.readLine())))) {
					data += str;
					i++;
				}
				d.close();
				Pattern p = Pattern
						.compile("<a class=\"blb\" href=\"[^\"]*\">([^<]*)</a><br>([^<]*)</td>");
				Matcher m = p.matcher(data);
				if (m.find()) {
					Debug.msg(3, "Pattern: " + m.group(1).trim());
					Debug.msg(3, "Pattern: " + m.group(2).trim());
					// TODO: wie splittet man am ersten SPACE und nicht an
					// jedem?
					String[] splitNames, splitAddress, splitPostCodeCity;
					splitNames = m.group(1).trim().split(" ", 2);
					splitAddress = m.group(2).trim().split(", ");
					splitPostCodeCity = splitAddress[1].split(" ", 2);
					String firstname = "", lastname = "";
					if (splitNames.length > 1) {
						firstname = splitNames[1];
					}
					lastname = splitNames[0];

					newPerson = new Person(firstname, "", lastname,
							splitAddress[0], splitPostCodeCity[0],
							splitPostCodeCity[1].trim(), "");
					newPerson.addNumber(number, "home");
					return newPerson;
				}
			} catch (IOException e1) {
				Debug.err("Error while retrieving " + urlstr);
			}
		}
		newPerson = new Person();
		newPerson.addNumber(number, "home");
		return newPerson;
	}

	public static String beautifyMatch(String match) {
		if (match.indexOf(" ") > 0) {
			// Add a comma after surname
			match = match.substring(0, match.indexOf(" ")) + ","
					+ match.substring(match.indexOf(" "));
			// Replace 'u.' with 'und'
			match = match.replaceAll("( u\\.)", "und");
			// Put parentheses around last word
			match = match.replaceFirst("  ([\\S]*)", " ($1)");
		}
		return match;
	}

}

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

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * Class for telephone number reverse lookup using "dasoertliche.de"
 *
 * @author Arno Willig
 *
 */
public class ReverseLookup {

	public static Person lookup(PhoneNumber number) {
		Person newPerson;
		if (number.isMobile()) {
			newPerson = new Person();
			newPerson.addNumber(number);
			Debug.msg("Adding mobile " + number.getIntNumber());
		} else if (number.isFreeCall()) {
			newPerson = new Person("", "FreeCall");
			newPerson.addNumber(number);
		} else if (number.isSIPNumber() || number.isQuickDial()) {
		    newPerson = new Person ();
		    newPerson.addNumber(number);
		} else {
			newPerson = lookupDasOertliche(number.getAreaNumber());
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
		if (number.equals("")) {
			return null;
		}
		Debug.msg("Looking up " + number + "...");
		URL url = null;
		String data = "";
		Person newPerson;

		String urlstr = "http://www.dasoertliche.de/DB4Web/es/oetb2suche/home.htm?main=Antwort&s=2&kw_invers="
				+ number;
		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					con = url.openConnection();

					String header = "";
					String charSet = "";
					for (int i = 0;; i++) {
						String headerName = con.getHeaderFieldKey(i);
						String headerValue = con.getHeaderField(i);

						if (headerName == null && headerValue == null) {
							// No more headers
							break;
						}
						if ("content-type".equalsIgnoreCase(headerName)) {
							String[] split = headerValue.split(" ", 2);
							for (int j = 0; j < split.length; j++) {
								split[j] = split[j].replaceAll(";", "");
								if (split[j].toLowerCase().startsWith(
										"charset=")) {
									String[] charsetSplit = split[j].split("=");
									charSet = charsetSplit[1];
								}
							}
						}
						header += headerName + ": " + headerValue + " | ";
					}
					Debug.msg("Header of dasoertliche.de: " + header);
					Debug.msg("CHARSET : " + charSet);

					// Get used Charset
					BufferedReader d;
					if (charSet.equals("")) {
						d = new BufferedReader(new InputStreamReader(con
								.getInputStream(), "ISO-8859-1"));
					} else {
						d = new BufferedReader(new InputStreamReader(con
								.getInputStream(), charSet));
					}
					int i = 0;
					String str = "";

					// Get response data
					while ((i < 700) && (null != ((str = d.readLine())))) {
						data += str;
						i++;
					}
					d.close();
					Debug.msg("DasOertliche Webpage: " + data);
					Pattern p = Pattern
							.compile("<a class=\"blb\" href=\"[^\"]*\">([^<]*)</a>(?:<br>([^<]*))?</td>");
					Matcher m = p.matcher(data);
					// Get name and address
					if (m.find()) {
						String line1 = m.group(1).trim();
						Debug.msg(3, "Pattern1: " + line1);

						String[] split = line1.split(" ", 2);
						String firstname = "", lastname = "", company = "", address = "", zipcode = "", city = "";
						lastname = split[0];
						if (split.length > 1) {
							firstname = " " + split[1];
							Debug.msg("*" + firstname + "*"
									+ firstname.indexOf("  "));
							if ((firstname.indexOf("  ") > -1)
									&& (firstname.indexOf("  u.") == -1)) {
								company = firstname.substring(
										firstname.indexOf("  ")).trim();
								firstname = firstname.substring(0,
										firstname.indexOf("  ")).trim();
							} else {
								firstname = firstname.replaceAll("  u. ",
										" und ");
							}
						}
						firstname = firstname.trim();
						if (m.group(2) != null) { // there is an address
							String line2 = m.group(2).trim();
							Debug.msg(3, "Pattern2: " + line2);
							split = line2.split(", ", 2);
							String zipcity = "";
							if (split.length > 1) {
								address = split[0].trim();
								zipcity = split[1].trim();
							} else {
								zipcity = split[0].trim();
								address = "";
							}
							split = zipcity.split(" ", 2);
							if (split.length > 1) {
								zipcode = split[0].trim();
								city = split[1].trim();
							} else {
								city = split[0].trim();
							}
						}

						Debug.msg("Firstname: " + firstname);
						Debug.msg("Lastname: " + lastname);
						Debug.msg("Company: " + company);
						Debug.msg("Address: " + address);
						Debug.msg("ZipCode: " + zipcode);
						Debug.msg("City: " + city);

						newPerson = new Person(firstname, company, lastname,
								address, zipcode, city, "");
						if (company.length() > 0) {
							newPerson.addNumber(number, "business");
						} else {
							newPerson.addNumber(number, "home");
						}
						return newPerson;
					}
				} catch (IOException e1) {
					Debug.err("Error while retrieving " + urlstr);
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr);
		}
		newPerson = new Person();
		newPerson.addNumber(number, "home");
		return newPerson;
	}
}

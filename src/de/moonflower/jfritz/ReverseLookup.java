/*
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz;

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
		mobileMap.put("0151","D1");
		mobileMap.put("0160","D1");
		mobileMap.put("0170","D1");
		mobileMap.put("0171","D1");
		mobileMap.put("0175","D1");
		mobileMap.put("0152","D2");
		mobileMap.put("0162","D2");
		mobileMap.put("0172","D2");
		mobileMap.put("0173","D2");
		mobileMap.put("0174","D2");
		mobileMap.put("0163","E+");
		mobileMap.put("0177","E+");
		mobileMap.put("0178","E+");
		mobileMap.put("0159","O2");
		mobileMap.put("0176","O2");
		mobileMap.put("0179","O2");
	}

	public static String lookup(String number) {
		String participant="";
		if (numberIsMobile(number)) {
			participant="? (Mobil)";
		} else {
			participant = lookupDasOertliche(number);
			if (participant.equals("")) {
				participant="?";
			}
		}
		return participant;
	}


	public static boolean numberIsMobile(String number) {
		return mobileMap.containsKey(number.substring(0,4));
	}

	public static String getMobileProvider(String number) {
		return mobileMap.get(number.substring(0,4)).toString();
	}


	/**
	 * Static method for looking up entries from "dasoertliche"
	 *
	 * @param number
	 * @return name
	 */

	public static String lookupDasOertliche(String number) {
		//		System.out.println("Looking up " + number + "...");
		URL url = null;
		URLConnection urlConn;
		DataOutputStream printout;
		String data = "";
		boolean wrong_pass = false;
		boolean wrong_url = false;

		String urlstr = "http://www.dasoertliche.de/DB4Web/es/oetb2suche/home.htm?main=Antwort&s=2&kw_invers="
				+ number;
		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			System.err.println("URL invalid: " + urlstr);
		}

		if (url != null) {

			URLConnection con;
			try {
				con = url.openConnection();

				// Get response data
				BufferedReader d = new BufferedReader(new InputStreamReader(con
						.getInputStream()));
				int i = 0;
				String str;

				while (null != ((str = d.readLine()))) {
					//					if (i > 600) // Skip a few lines..
					data += str;
					i++;
				}
				d.close();
				Pattern p = Pattern
						.compile("<a class=\"blb\" href=\"[^\"]*\">([^<]*)</a>");
				Matcher m = p.matcher(data);
				if (m.find()) {
					return beautifyMatch(m.group(1).trim());
				}
			} catch (IOException e1) {
				System.err.println("Error while retrieving " + urlstr);
			}
		}
		return "";
	}


	public static String beautifyMatch(String match) {
		// Add a comma after surname
		match = match.substring(0, match.indexOf(" ")) + ","
				+ match.substring(match.indexOf(" "));
		// Replace 'u.' with '&'
		match = match.replaceAll("( u\\.)","&");
		// Put parentheses around last word
		match = match.replaceFirst("  ([\\S]*)"," ($1)");
		return match;
	}

}

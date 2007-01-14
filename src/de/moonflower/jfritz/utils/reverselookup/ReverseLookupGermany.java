package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.HTMLUtil;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * This class is responsible for doing reverse lookups for german numbers
 *
 * The search engine used is: http://dastelefonbuch.de
 * A big thanks to them for creating an easy to parse web page
 *
 *
 *
 */
public final class ReverseLookupGermany {

	public final static String SEARCH_URL="http://www.tao.dastelefonbuch.de/?sourceid=Mozilla-search&cmd=search&kw=";

	public final static String FILE_HEADER = "Vorwahl;Ortsnetz";

	public static String s1 = "<!-- ****** Treffer Einträge ****** -->",
	s2 = "<!-- ****** Ende Treffer Einträge ****** -->";



	private static HashMap<String, String> numberMap;

	/**
	 * This function performs the reverse lookup
	 *
	 * @author Brian Jensen
	 * @param number in area format to be looked up
	 *
	 * @return a person object created using the data from the site
	 */
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
		Debug.msg("Looking up " + number + "..."); //$NON-NLS-1$,  //$NON-NLS-2$
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
					con.setConnectTimeout(5000);

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
					Debug.msg("Header of dastelefonbuch.de: " + header); //$NON-NLS-1$
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

					// Get response data
					boolean flg_found_start = false;
					boolean flg_found_end   = false;
					String str = ""; //$NON-NLS-1$

					//Strings must be cast to ISO-8859-1 or else umlauts wont match up
					String searchString = new String(s1.getBytes(), "ISO-8859-1");
					String endString = new String(s2.getBytes(), "ISO-8859-1");


					while ((flg_found_end == false) && (null != ((str = d.readLine())))) {

						// Search for starttag
						if (flg_found_start == false
								&& (str.indexOf(searchString)!=-1))
							flg_found_start = true;

						if (flg_found_start == true) {
							data += str;
							// Seach for endtag
							if (str.indexOf(endString)!=-1)
								flg_found_end = true;
						}
					}

					//too much output
					//Debug.msg(data);
					d.close();
					Debug.msg("Begin processing responce from dastelefonbuch.de");

					//removed the tail since it caused certain people without an address not to be matched
					Pattern p = Pattern
							.compile("title=\\\"([^<]*)\\\">[^\"]*[^>]*>([^<]*)?[^\"]*?.*title=\\\"([^<]*)\\\">"); //([^-*]*)"); //$NON-NLS-1$

					Matcher m = p.matcher(data);
					// Get name and address
					if (m.find()) {
						Debug.msg("Found a match");
						String line1 = m.group(1).trim();
						Debug.msg(3, "Pattern1: " + line1); //$NON-NLS-1$

						String[] split = line1.split(" ", 2); //$NON-NLS-1$
						String firstname = "", //$NON-NLS-1$
								lastname = "", //$NON-NLS-1$
								company = "", //$NON-NLS-1$
								address = "", //$NON-NLS-1$
								zipcode = "", //$NON-NLS-1$
								city = ""; 	  //$NON-NLS-1$
						lastname = HTMLUtil.stripEntities(split[0]);
						if (split.length > 1) {
							firstname = " " + HTMLUtil.stripEntities(split[1]); //$NON-NLS-1$
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
							Debug.msg("raw match 2: "+m.group(2));
							String line2 = HTMLUtil.stripEntities(m.group(2).trim());
							Debug.msg(3, "Pattern2: " + line2); //$NON-NLS-1$
							address = line2.trim();
						}
						if (m.group(3) != null) { // there is a zipcity
							String line3 = HTMLUtil.stripEntities(m.group(3).trim());
							Debug.msg(3, "Pattern3: " + line3); //$NON-NLS-1$
							String zipcity = line3.replaceAll("\t", ""); //$NON-NLS-1$
							split = zipcity.split(" ", 2); //$NON-NLS-1$
							if (split.length > 1) {
								zipcode = split[0].trim();
								city = HTMLUtil.stripEntities(split[1].trim());
							} else {
								city = HTMLUtil.stripEntities(split[0].trim());
							}
						}

						//use the area code table if city isnt found
						if(city.equals(""))
							city = getCity(number);

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

		//try to lookup the city in the area code table
		String city = getCity(number);
		newPerson = new Person("", "", "", "", "", city, "");

		newPerson.addNumber(number, "home"); //$NON-NLS-1$
		return newPerson;
	}

	/**
	 * This function attemps to fill the hashmap numberMap up with the data found
	 * in number/germany/areacodes_germany.csv
	 * The funtion uses the area codes listed in the file as keys and the cities as values
	 *
	 *
	 * @author Brian Jensen
	 *
	 */
	public static void loadAreaCodes(){
		Debug.msg("Loading the german number to city list");
		numberMap = new HashMap<String, String>(5300);
		BufferedReader br = null;
		FileInputStream fi = null;

		try{
			fi = new FileInputStream(JFritzUtils.getFullPath("/number") +"/germany/areacodes_germany.csv");
			br = new BufferedReader(new InputStreamReader(fi, "ISO-8859-1"));

			String line;
			String[] entries;
			int lines = 0;
			String l = br.readLine();
			if(l==null){
				Debug.errDlg("File "+JFritzUtils.getFullPath("/number") +"/germany/areacodes_germany.csv"+" empty");
			}
			//Load the keys and values quick and dirty
			if(l.equals(FILE_HEADER)){
				while (null != (line = br.readLine())) {
					lines++;
					entries = line.split(";");
					if(entries.length == 2)
						//number is the key, city is the value
						numberMap.put(entries[0], entries[1]);

				}
			}

			Debug.msg(lines + " Lines read from areacodes_germany.csv");
			Debug.msg("numberMap size: "+numberMap.size());

		}catch(Exception e){
			Debug.msg(e.toString());
		}finally{
			try{
				if(fi!=null)
					fi.close();
				if(br!=null)
					br.close();
			}catch (IOException ioe){
				Debug.msg("error closing stream"+ioe.toString());
			}
		}


	}

	/**
	 * This function determines the city to a particular number
	 * The hashmap does not have to initialised in order to call this function
	 *
	 *
	 * @param number in area format e.g. starting with "0"
	 * @return the city found or "" if nothing was found
	 */

	public static String getCity(String number){

		Debug.msg("Looking up city in numberMap: "+number);
		String city = "";
		int l = number.length();
		if(number.startsWith("0") && numberMap != null){
			if(l>=3 && numberMap.containsKey(number.substring(0, 3)))
				city = numberMap.get(number.substring(0,3));
			else if(l>=4 && numberMap.containsKey(number.substring(0,4)))
				city = numberMap.get(number.substring(0,4));
			else if(l>=5 && numberMap.containsKey(number.substring(0,5)))
				city = numberMap.get(number.substring(0,5));
		}

		return city;
	}

}

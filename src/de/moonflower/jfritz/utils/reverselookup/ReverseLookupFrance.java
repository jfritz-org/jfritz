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
 * This class is responsible for doing reverse lookups for swiss numbers
 *
 * The search engine used is: http://www.annuaireinverse.com
 * A big thanks to them for creating an easy to parse web page
 *
 * @author Bastian Shcaefer
 *
 */
public final class ReverseLookupFrance {

	public final static String SEARCH_URL_PRENUMBER="http://www.annuaireinverse.com/G118012/home.asp?RN=";
	public final static String SEARCH_URL_POSTNUMBER="&CO=RN&FR=118012&sm=rep";

	/**
	 * This function performs the reverse lookup
	 *
	 * @author Bastian Schaefer
	 * @param number in area format to be looked up
	 *
	 * @return a person object created using the data from the site
	 */
	public static Person lookup(String number){
		boolean intNumber = false;

		if(number.startsWith("+")){
			number = number.substring(1);
			intNumber = true;
		}
		Debug.msg("France reverselookup number: "+number);

		String urlstr = SEARCH_URL_PRENUMBER + number + SEARCH_URL_POSTNUMBER;
		Person newPerson;

		String firstname = "",
				lastname = "",
				company = "",
				street = "", //$NON-NLS-1$
				zipCode = "", //$NON-NLS-1$
				city = ""; 	  //$NON-NLS-1$;


		try {
			URL url = new URL(urlstr);
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
					Debug.msg("Header of http://www.annuaireinverse.com: " + header); //$NON-NLS-1$
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
					String data = "";

					// Get response data
					while ((i < 700) && (null != ((str = d.readLine())))) {
						data += str;
						i++;
					}
					d.close();
					Debug.msg("Begin processing responce from http://www.annuaireinverse.com");

					Pattern pName = Pattern
							.compile("<TD HEIGHT=\"15\" BGCOLOR=\"#ABEEFB\"  ><FONT CLASS=\"ctexte\">&nbsp;([^<]*)"); //$NON-NLS-1$
					Pattern pCompany = Pattern
							.compile("<TD HEIGHT=\"15\"  bgcolor=#FFD92A  ><FONT CLASS=\"ctexte\">&nbsp;([^<]*)");
					Pattern pAddress = Pattern
						.compile("<TD HEIGHT=\"35\" VALIGN=\"TOP\"><FONT CLASS=\"copytexte\">([^<]*)<br />([^<]*)"); //$NON-NLS-1$

					//parse Name
					Matcher mName = pName.matcher(data);
					if (mName.find()) {
						if(mName.group(1).trim().indexOf(" ")>-1){
						String[] results = mName.group(1).trim().replaceAll("\\s+"," ").split("\\s",2);

						lastname = HTMLUtil.stripEntities(results[0]);
						Debug.msg("Last name: " + lastname);
						if(results.length > 1)
							firstname = HTMLUtil.stripEntities(results[1]);


						Debug.msg("First name: " + firstname);
						}
						else
							lastname = HTMLUtil.stripEntities(mName.group(1).trim());
					}

					Matcher mCompany = pCompany.matcher(data);
					if ( mCompany.find()) {
						company = HTMLUtil.stripEntities(mCompany.group(1));
						while ( company.endsWith(" ")) {
							company = company.substring(0, company.length()-1);
						}
						Debug.msg("Company: " + company);
					}

					//parse Street, zip code and city
					Matcher mAddress = pAddress.matcher(data);
					if(mAddress.find()){
						street = JFritzUtils.capitalize(HTMLUtil.stripEntities(mAddress.group(1).trim().toLowerCase()));
						Debug.msg("Street: "+street);
						zipCode  = HTMLUtil.stripEntities(mAddress.group(2).trim().split("\\s",2)[0]);
						Debug.msg("Zip Code: "+ zipCode);
						city = JFritzUtils.capitalize(HTMLUtil.stripEntities(mAddress.group(2).trim().split("\\s",2)[1].toLowerCase()));
						Debug.msg("City: "+city);
					}

				} catch (IOException e1) {
					Debug.err("Error while retrieving " + urlstr); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
		}

		newPerson = new Person(firstname, company, lastname, street, zipCode, city, "");
		if(intNumber)
			number = "+" + number;

		newPerson.addNumber(number, "home"); //$NON-NLS-1$


		return newPerson;

	}

}


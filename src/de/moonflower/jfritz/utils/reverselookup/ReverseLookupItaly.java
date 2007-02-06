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
/**
 * Class for looking up Italian phone numbers
 * Search engine used is www.paginebianche.it
 *
 * Created: 03.08.06
 *
 * @author Brian Jensen
 *
 */
public class ReverseLookupItaly {

public final static String SEARCH_URL="http://www.paginebianche.it/execute.cgi?btt=1&tl=2&tr=106&tc=&cb=&x=0&y=0&qs=";

	/**
	 * This function performs the reverse lookup
	 *
	 * @author Brian Jensen
	 * @param number in area format to be looked
	 *
	 * @return a Person object created using the data collected from the site
	 */
	public static Person lookup(String number){
		String searchNumber = number;

		if(number.startsWith("+")){
			searchNumber = number.substring(3);
			if(!searchNumber.startsWith("0"))
				searchNumber = "0" + searchNumber;
		}
		Debug.msg("Italy reverselookup number: "+searchNumber);

		String urlstr = SEARCH_URL + searchNumber;
		Debug.msg("URL: "+urlstr);
		Person newPerson;

		String firstname = "",
				lastname = "",
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
					// 5 Sekunden-Timeout für Verbindungsaufbau
					//Set the read time for 15 seconds
					con.setConnectTimeout(5000);
					con.setReadTimeout(15000);

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
					Debug.msg("Header of www.paginebianche.it: " + header); //$NON-NLS-1$
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
					Debug.msg("Begin processing responce from www.paginebianche.it");
					Pattern pMain = Pattern
							.compile("<td class=\"dati\"><span [^>]*>([^<]*)</span><br>([0-9]*)&nbsp;([^&]*)[^-]*-([^<]*)<"); //$NON-NLS-1$
					Pattern pSecondary = Pattern
							.compile("<td class=\"dati\"><!--LOGO--><a [^>]*><img [^>]*></a><a class=\"AInserzionista\"[^>]*>([^<]*) </a><br><font [^>]*>([0-9]*)&nbsp;([^&]*)[^-]*-([^<]*)<");

					//parse Data
					Matcher mData = pMain.matcher(data);
					if (mData.find()) {
						lastname = HTMLUtil.stripEntities(mData.group(1).trim());
						Debug.msg("Last name: " + lastname);
						zipCode = HTMLUtil.stripEntities(mData.group(2).trim());
						Debug.msg("Zip code: "+ zipCode);
						city = HTMLUtil.stripEntities(mData.group(3).trim());
						Debug.msg("City: "+ city);
						street = HTMLUtil.stripEntities(mData.group(4).trim());
						Debug.msg("Street: "+ street);

						//don't run the reg ex unless necessary
					}else if((mData = pSecondary.matcher(data)).find()){
						Debug.msg("Found secondary match");
						lastname = HTMLUtil.stripEntities(mData.group(1).trim());
						Debug.msg("Last name: " + lastname);
						zipCode = HTMLUtil.stripEntities(mData.group(2).trim());
						Debug.msg("Zip code: "+ zipCode);
						city = HTMLUtil.stripEntities(mData.group(3).trim());
						Debug.msg("City: "+ city);
						street = HTMLUtil.stripEntities(mData.group(4).trim());
						Debug.msg("Street: "+ street);
					}

				} catch (IOException e1) {
					Debug.err("Error while retrieving " + urlstr); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
		}

		newPerson = new Person(firstname, "", lastname, street, zipCode, city, "");

		newPerson.addNumber(number, "home"); //$NON-NLS-1$


		return newPerson;

	}
}

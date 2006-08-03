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

public class ReverseLookupItaly {

public final static String SEARCH_URL="http://www.paginebianche.it/execute.cgi?btt=1&tl=2&tr=106&tc=&cb=&x=0&y=0&qs=";

	public static Person lookup(String number){
		boolean intNumber = false;

		if(number.startsWith("+")){
			number = number.substring(3);
			number = "0" + number;
			intNumber = true;
		}
		Debug.msg("Italy reverselookup number: "+number);

		String urlstr = SEARCH_URL + number;
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
					Pattern pName = Pattern
							.compile("<td class=\"dati\"><span [^>]*>([^<]*)</span><br>([0-9]*)&nbsp;([^&]*)[^-]*-([^<]*)<"); //$NON-NLS-1$

					//parse Data
					Matcher mData = pName.matcher(data);
					if (mData.find()) {
						lastname = mData.group(1).trim();
						Debug.msg("Last name: " + lastname);
						zipCode = mData.group(2).trim();
						Debug.msg("Zip code: "+ zipCode);
						city = mData.group(3).trim();
						Debug.msg("City: "+ city);
						street = mData.group(4).trim();
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
		if(intNumber)
			number = "+39" + number.substring(1);

		newPerson.addNumber(number, "home"); //$NON-NLS-1$


		return newPerson;

	}
}

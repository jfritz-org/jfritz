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
import de.moonflower.jfritz.utils.Debug;

/**
 * This class is responsible for doing reverse lookups for swiss numbers
 *
 * The search engine used is: http://www.whitepages.com
 * A big thanks to them for creating an easy to parse web page
 *
 * @author Brian Jensen
 *
 */
public final class ReverseLookupUnitedStates {

	public final static String SEARCH_URL="http://www.whitepages.com/search/ReversePhone?phone=";

	/**
	 * This function performs the reverse lookup
	 *
	 * @author Brian Jensen
	 * @param number in area format to be looked up
	 *
	 * @return a person object created using the data from the site
	 */
	public static Person lookup(String number){
		boolean intNumber = false;

		if(number.startsWith("+")){
			number = number.substring(2);
			intNumber = true;
		}
		Debug.msg("USA reverselookup number: "+number);

		String urlstr = SEARCH_URL + number;
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
			//	con.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)");


				try {
					con = url.openConnection();
					con.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)");
					String header = ""; //$NON-NLS-1$
					String charSet = ""; //$NON-NLS-1$
					for (int i = 0;; i++) {
						String headerName = con.getHeaderFieldKey(i);
						String headerValue = con.getHeaderField(i);

						if (headerName == null && headerValue == null) {
							// No more headers
							break;
						}
						if ("Content-Type".equalsIgnoreCase(headerName)) { //$NON-NLS-1$
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
					Debug.msg("Header of whitepages.com: " + header); //$NON-NLS-1$
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
					Debug.msg("Begin processing responce from whitepages.com");
			        //Debug.msg("DATA:"+data);
					Pattern pName = Pattern
									.compile("lname=([^<]*)&amp;fname=([^<]*)&amp;h_street="); //$NON-NLS-1$
					Pattern pAddress = Pattern
								.compile("h_street=([^<]*)&amp;h_city=([^<]*)&amp;h_state=([^<]*)&amp;h_zip=([^<]*)&amp;h_country"); //$NON-NLS-1$


					//parse Name
					Matcher mName = pName.matcher(data);
					if (mName.find()) {
						lastname = replaceChar(mName.group(1).trim());
						Debug.msg("Last name: " + lastname);
						firstname = replaceChar(mName.group(2).trim());
						Debug.msg("First name: " + firstname);
					}

					//parse Street, zip code and city
					Matcher mAddress = pAddress.matcher(data);
					if(mAddress.find()){
					    street = replaceChar(mAddress.group(1).trim());
						Debug.msg("Street: "+street);
						zipCode  =replaceChar(mAddress.group(3).trim()+" "+mAddress.group(4).trim());
						Debug.msg("Zip Code: "+ zipCode);
						city = replaceChar(mAddress.group(2).trim());
						Debug.msg("City: "+city);
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
			number = "+1" + number;

		newPerson.addNumber(number, "home"); //$NON-NLS-1$


		return newPerson;

	}
	public static String replaceChar ( String text ){
	// Hier gibt es noch Optimierungsbedarf

		 if ( text == null )
               return null;

         if ( text.indexOf( '%' ) < 0 )
            // are no entities, nothing to do
            return text;

         int originalTextLength = text.length();
         StringBuffer sb = new StringBuffer( originalTextLength );
         for ( int i = 0; i < originalTextLength; i++ )
           {
            int whereProz = text.indexOf( '%', i );
            if ( whereProz < 0 ){
            	// no more %, we are done
            	// append all remaining text
            	sb.append( text.substring( i ) );
            	break;
            }else{
            	// append all text to left of next &
            	sb.append( text.substring( i, whereProz ) );
            	// avoid reprocessing those chars
            	i = whereProz;
            	// text.charAt(i) is an %
            	// possEntity has lead % stripped.

            	String possEntity="";
            	if ( i +3 <= originalTextLength ){
            		possEntity = text.substring( i + 1,i + 3  );
            		possEntity=possEntity.toUpperCase(); }
                  	i= i + 2;
                  	char t=0;

                  	if (possEntity.length()==2)
                  		t=(char)Integer.parseInt( possEntity,16 );

                  	if ( t != 0 )
                  		sb.append( t );

              } // end else
           } // end for
          // if result is not shorter, we did not do anything. Saves RAM.
         return ( sb.length() == originalTextLength ) ? text : sb.toString();
    } // end replaceChar

}

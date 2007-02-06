package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
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
 * This class is responsible for doing reverse lookups for dutch numbers
 *
 * The search engine used is: http://www.gebeld.nl/content.asp?zoek=numm
 * A big thanks to them for creating an easy to parse web page
 *
 * @author Bastian Schaefer
 *
 */
public final class ReverseLookupNetherlands {

	public final static String SEARCH_URL="http://www.gebeld.nl/content.asp?zapp=zapp&land=Nederland&zoek=numm&searchfield1=fullnumber&searchfield2=&queryfield1=";

	public final static String FILE_HEADER = "Number;City";

	private static HashMap<String, String> numberMap;

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
			number = "0"+number.substring(3);
			intNumber = true;
		}
		Debug.msg("Netherland reverselookup number: "+number);

		String urlstr = SEARCH_URL + number.replaceAll("\\+","%2B");
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
					Debug.msg("Header of http://www.gebeld.nl/content.asp?zoek=numm: " + header); //$NON-NLS-1$
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
					Debug.msg("Begin processing responce from http://www.gebeld.nl/content.asp?zoek=numm");

					Pattern pName = Pattern
							.compile("<td><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">([^<]*)</font></td></tr>"); //$NON-NLS-1$
					Pattern pAddress = Pattern
						.compile("<tr><td></td><td><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">([^<]*)</font></td></tr><tr><td></td><td>([^<]*)</td></tr>"); //$NON-NLS-1$
					//parse Name
					Matcher mName = pName.matcher(data);
					if (mName.find()) {
						String[] results = mName.group(1).trim().split(",");

						lastname = HTMLUtil.stripEntities(results[0]);
						Debug.msg("Last name: " + lastname);
						if(results.length > 1)
							firstname = HTMLUtil.stripEntities(results[1].substring(results[1].indexOf(";")+1));
						Debug.msg("First name: " + firstname);
					}

					//parse Street, zip code and city
					Matcher mAddress = pAddress.matcher(data);
					if(mAddress.find()){
						street = HTMLUtil.stripEntities(mAddress.group(1).trim());
						Debug.msg("Street: "+street);
						zipCode  = HTMLUtil.stripEntities(mAddress.group(2).trim().substring(mAddress.group(2).indexOf(";")+1));
						Debug.msg("Zip Code: "+ zipCode);
						city = HTMLUtil.stripEntities(mAddress.group(2).trim().substring(0,mAddress.group(2).indexOf(";")));
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
			number = "+31" + number.substring(1);

		newPerson.addNumber(number, "home"); //$NON-NLS-1$


		return newPerson;

	}

	/**
	 * This function attemps to fill the hashmap numberMap up with the data found
	 * in number/holland/areacodes_holland.csv
	 * The funtion uses the area codes listed in the file as keys and the cities as values
	 *
	 * @deprecated This function is not currently used
	 *
	 * @author Brian Jensen
	 *
	 */
	public static void loadAreaCodes(){
		Debug.msg("Loading the holland number to city list");
		numberMap = new HashMap<String,String>(256);
		try{
			FileReader fr = new FileReader(JFritzUtils.getFullPath("/number") +"/holland/areacodes_holland.csv");
			BufferedReader br = new BufferedReader(fr);
			String line;
			String[] entries;
			int lines = 0;

			//Load the keys and values quick and dirty
			if(br.readLine().equals(FILE_HEADER)){
				while (null != (line = br.readLine())) {
					lines++;
					entries = line.split(";");
					if(entries.length == 2)
						//number is the key, city is the value
						numberMap.put(entries[0], entries[1]);

				}
			}

			Debug.msg(lines + " Lines read from areacodes_holland.csv");
			Debug.msg("numberMap size: "+numberMap.size());

		}catch(Exception e){
			Debug.msg(e.toString());
		}

	}

	/**
	 * This function determines the city to a particular number
	 * The hashmap does not have to initialised in order to call this function
	 *
	 * @deprecated This function is not used right now
	 *
	 * @param number in area format e.g. starting with "0"
	 * @return the city found or "" if nothing was found
	 */

	public static String getCity(String number){

		Debug.msg("Looking up city in numberMap: "+number);
		String city = "";
		if(number.startsWith("0") && numberMap != null){
			if(numberMap.containsKey(number.substring(0, 3)))
				city = numberMap.get(number.substring(0,3));
			else if(numberMap.containsKey(number.substring(0,4)))
				city = numberMap.get(number.substring(0,4));
			else if(numberMap.containsKey(number.substring(0,5)))
				city = numberMap.get(number.substring(0,5));
		}

		return city;
	}


}

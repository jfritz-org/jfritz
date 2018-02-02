package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/** This class is responsible for maintaining the turkish area code -> city
 * hash map. It is also used to perfom the lookups using this map
 *
 *
 * @author brian
 *
 */
public class ReverseLookupTurkey {
	private final static Logger log = Logger.getLogger(ReverseLookupTurkey.class);

	public final static String FILE_HEADER = "Area Code;City";

	private static HashMap<String, String> numberMap;

	/**
	 * This function attemps to fill the hashmap numberMap up with the data found
	 * in number/turkey/areacodes_turkey.csv
	 * The funtion uses the area codes listed in the file as keys and the cities as values
	 *
	 *
	 * @author Brian
	 *
	 */
	public static void loadAreaCodes(){
	log.debug("Loading the turkish number to city list");
		numberMap = new HashMap<String, String>(5300);
		BufferedReader br = null;
		FileInputStream fi = null;

		try{
			fi = new FileInputStream(JFritzUtils.getFullPath(JFritzUtils.FILESEP + "number") + JFritzUtils.FILESEP + "turkey" + JFritzUtils.FILESEP + "areacodes_turkey.csv");
			br = new BufferedReader(new InputStreamReader(fi, "UTF-8"));

			String line;
			String[] entries;
			int lines = 0;
			String l = br.readLine();
			if(l==null){
				String message = "File "+JFritzUtils.getFullPath(JFritzUtils.FILESEP + "number") + JFritzUtils.FILESEP + "turkey" + JFritzUtils.FILESEP + "areacodes_turkey.csv"+" empty";
				log.error(message);
				Debug.errDlg(message);
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

			log.debug(lines + " Lines read from areacodes_turkey.csv");
			log.debug("numberMap size: "+numberMap.size());

		}catch(Exception e){
			log.error(e.toString());
		}finally{
			try{
				if(fi!=null)
						fi.close();
					if(br!=null)
						br.close();
				}catch (IOException ioe){
					log.error("error closing stream "+ioe.toString());
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

		log.info("Looking up city in numberMap: "+number);
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

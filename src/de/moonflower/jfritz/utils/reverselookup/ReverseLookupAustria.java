package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * This class is responsible for finding a city based upon the city prefix of the number
 *
 * @author Brian Jensen
 *
 */
public final class ReverseLookupAustria {
	private final static Logger log = Logger.getLogger(ReverseLookupAustria.class);

	public final static String VORWAHLEN_HEADER = "Number;City";

	private static HashMap<String, String> numberMap;

	/**
	 * This function attemps to fill the hashmap numberMap up with the data found
	 * in number/austria/areacodes_austria.csv
	 * The funtion uses the area codes listed in the file as keys and the cities as values
	 *
	 *
	 * @author Brian Jensen
	 *
	 */
	public static void loadAreaCodes(){
		log.info("Loading the austrian number to city list");
		numberMap = new HashMap<String, String>(1200);
		BufferedReader br = null;
		FileInputStream fi = null;
		try{
			fi = new FileInputStream(JFritzUtils.getFullPath(JFritzUtils.FILESEP + "number") +JFritzUtils.FILESEP + "austria" + JFritzUtils.FILESEP + "areacodes_austria.csv");
			br = new BufferedReader(new InputStreamReader(fi, "UTF-8"));
			String line;
			String[] entries;
			int lines = 0;

			//Load the keys and values quick and dirty
			if(br.readLine().equals(VORWAHLEN_HEADER)){
				while (null != (line = br.readLine())) {
					lines++;
					entries = line.split(";");
					if(entries.length == 2)
						//number is the key, city is the value
						numberMap.put(entries[0], entries[1]);

				}
			}

			log.info(lines + " Lines read from areacodes_austria.csv");
			log.info("numberMap size: "+numberMap.size());

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

	public static String getCity(final String number){
		String currentNum = number;
		log.info("Looking up city in austrian numberMap: "+currentNum);
		String city = "";
		if ( currentNum.startsWith("+43")) {
			currentNum = "0"+currentNum.substring(3);
		}
		if(currentNum.startsWith("0") && numberMap != null){
			if(numberMap.containsKey(currentNum.substring(0, 3)))
				city = numberMap.get(currentNum.substring(0,3));
			else if(numberMap.containsKey(currentNum.substring(0,4)))
				city = numberMap.get(currentNum.substring(0,4));
			else if(numberMap.containsKey(currentNum.substring(0,5)))
				city = numberMap.get(currentNum.substring(0,5));
		}

		return city;
	}
}

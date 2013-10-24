package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/** This class is responsible for mainting the germany area code -> city
 * hash map. It is also used to perfom the lookups using this map

 *
 * @author brian
 *
 */
public class ReverseLookupGermany {

	public final static String FILE_HEADER = "AreaCode;City";

	private static HashMap<String, String> numberMap;

	/**
	 * This function attemps to fill the hashmap numberMap up with the data found
	 * in number/germany/areacodes_germany.csv
	 * The funtion uses the area codes listed in the file as keys and the cities as values
	 *
	 *
	 * @author Brian
	 *
	 */
	public static void loadAreaCodes(){
		Debug.info("Loading the german number to city list");
		numberMap = new HashMap<String, String>(5300);
		BufferedReader br = null;
		FileInputStream fi = null;

		try{
			fi = new FileInputStream(JFritzUtils.getFullPath(JFritzUtils.FILESEP + "number") + JFritzUtils.FILESEP + "germany" + JFritzUtils.FILESEP + "areacodes_germany.csv");
			br = new BufferedReader(new InputStreamReader(fi, "UTF-8"));

			String line;
			String[] entries;
			int lines = 0;
			String l = br.readLine();
			if(l==null){
				Debug.errDlg("File "+JFritzUtils.getFullPath(JFritzUtils.FILESEP + "number") + JFritzUtils.FILESEP + "germany" + JFritzUtils.FILESEP + "areacodes_germany.csv"+" empty");
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

			Debug.info(lines + " Lines read from areacodes_germany.csv");
			Debug.info("numberMap size: "+numberMap.size());

		}catch(Exception e){
			Debug.error(e.toString());
		}finally{
			try{
				if(fi!=null)
					fi.close();
				if(br!=null)
					br.close();
			}catch (IOException ioe){
				Debug.error("error closing stream "+ioe.toString());
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

		Debug.info("Looking up city in numberMap: "+number);
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

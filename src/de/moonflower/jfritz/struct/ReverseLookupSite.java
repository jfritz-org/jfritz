package de.moonflower.jfritz.struct;

import java.util.Vector;

/** This is a structure class for holding reverse lookup sites
 * Currently you can add a URL, a user defined name, a prefix needed,
 * an area code length (for sites that like to split up the numbers)
 * for the site and a list of sets of patterns to match for the site
 *
 * Pattern categories available are: name, street, city, zip code
 *
 * Placeholders available for the url: $PFXAREACODE gets replaced
 * with the prefix and area code (determined by AREACODE_LENGTH),
 * $AREACODE gets replaced with area code minus prefix, and finally
 * $NUMBER which gets replaced the whole number - AREACODE_LENGTH
 *
 * Note: There may be more than one grouping for each pattern,
 * each grouping found will be concated on the previous grouping
 * found, ordering is not yet supported
 *
 *
 *
 * @author brian jensen
 *
 * created: 06.02.07
 *
 */
public class ReverseLookupSite {

	//Site url, as will be used to process the reverse lookup
	private String URL;

	//A user defined site name
	private String NAME;

	//needed to be certify if the number format is correct for the site
	private String PREFIX;

	private int AREACODE_LENGTH;

	//currently a linked list of String arrays
	private Vector<String[]> entries;

	public ReverseLookupSite(String url, String name, String prefix, int ac_length){
		URL = url;
		NAME = name;
		PREFIX = prefix;
		AREACODE_LENGTH = ac_length;
		entries = new Vector<String[]>(2);
	}

	public void addEntry(String name, String street, String city, String zipcode){
		String[] patterns = new String[4];
		patterns[0] = name;
		patterns[1] = street;
		patterns[2] = city;
		patterns[3] = zipcode;

		entries.add(patterns);
	}

	/** function returns a set of patterns
	 *
	 * @param number index of the set of patterns
	 * @return a String[4] object containing the patterns
	 */
	public String[] getEntry(int index){
		return entries.get(index);
	}

	public String getURL(){
		return URL;
	}

	public String getName(){
		return NAME;
	}

	public int size(){
		return entries.size();
	}

	public String getPrefix(){
		return PREFIX;
	}

	public int getAreaCodeLength(){
		return AREACODE_LENGTH;
	}

}

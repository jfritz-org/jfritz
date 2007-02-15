package de.moonflower.jfritz.struct;

import java.util.LinkedList;

/** This is a structure class for holding reverse lookup sites
 * Currently you can add a URL, a user defined name, a prefix needed
 * for the site and a list of sets of patterns to match for the site
 *
 * Pattern categories available are: name, street, city, zip code
 *
 * Note: It is important that in each pattern exactly one grouping is
 * present, or else the lookup engine won't find anything or the wrong data!!!
 * These objects may be omitted if necessary
 *
 * @author brian jensen
 *
 * created: 06.02.07
 *
 */
public class ReverseLookupSite {

	//Site url, as will be used to process the reverse lookup
	private String URL="";

	//A user defined site name
	private String NAME="";

	//needed to be certify if the number format is correct for the site
	private String PREFIX="";

	int count = 0;

	//currently a linked list of String arrays
	private LinkedList<String[]> entries;

	public ReverseLookupSite(String url, String name, String prefix){
		URL = url;
		NAME = name;
		PREFIX = prefix;
		entries = new LinkedList<String[]>();
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
}

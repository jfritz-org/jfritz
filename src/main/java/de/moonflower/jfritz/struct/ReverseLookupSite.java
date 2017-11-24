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
	public static final int NAME = 0;
	public static final int STREET = 1;
	public static final int CITY = 2;
	public static final int ZIPCODE = 3;
	public static final int FIRSTNAME = 4;
	public static final int LASTNAME = 5;
	public static final int FIRSTOCCURANCE = 6;
	public static final int SWAPFIRSTANDLASTNAME = 7;
	public static final int STARTLINE = 8;
	public static final int ENDLINE = 9;
	public static final int NUMLINES = 10;

	//Site url, as will be used to process the reverse lookup
	private String url;

	//A user defined site name
	private String name;

	//needed to be certify if the number format is correct for the site
	private String prefix;

	// how many lines should be connected together to perform patternmatching
	private int numLines;

	private int areacodeLength;

	//currently a linked list of String arrays
	private Vector<String[]> entries;

	public ReverseLookupSite(String url, String name, String prefix, int numLines, int ac_length){
		this.url = url;
		this.name = name;
		this.prefix = prefix;
		this.numLines = numLines;
		this.areacodeLength = ac_length;
		entries = new Vector<String[]>(2);
	}

	public void addEntry(final String firstOccurance,
			final String name, final String street, final String city, final String zipcode,
			final String firstName, final String lastName, final String swapFirstAndLastName,
			final String startline, final String endline){
		String[] patterns = new String[10];
		patterns[NAME] = name;
		patterns[STREET] = street;
		patterns[CITY] = city;
		patterns[ZIPCODE] = zipcode;
		patterns[FIRSTNAME] = firstName;
		patterns[LASTNAME] = lastName;
		patterns[FIRSTOCCURANCE] = firstOccurance;
		patterns[SWAPFIRSTANDLASTNAME] = swapFirstAndLastName;
		patterns[STARTLINE] = startline;
		patterns[ENDLINE] = endline;

		entries.add(patterns);
	}

	/** function returns a set of patterns
	 *
	 * @param number index of the set of patterns
	 * @return a String[4] object containing the patterns
	 */
	public String[] getEntry(final int index){
		return entries.get(index);
	}

	public String getURL(){
		return url;
	}

	public String getName(){
		return name;
	}

	public int size(){
		return entries.size();
	}

	public String getPrefix(){
		return prefix;
	}

	public int getNumLines(){
		return numLines;
	}
	public int getAreaCodeLength(){
		return areacodeLength;
	}
}

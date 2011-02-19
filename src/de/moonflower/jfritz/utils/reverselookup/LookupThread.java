package de.moonflower.jfritz.utils.reverselookup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.ReverseLookupSite;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.HTMLUtil;
import de.moonflower.jfritz.utils.JFritzUtils;

public class LookupThread extends Thread {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.1) Gecko/20090624 Firefox/3.5 (.NET CLR 3.5.30729)";

	private static final int MAX_DATA_LENGTH = 30000;

	private static final int SPACE_ALTERNATIVE = 160; // hex: a0 = space like ascii character

	private LookupRequest currentRequest;

	private boolean threadSuspended, quit, terminate, terminated;

	private Person result;

	private static String charSet;

	private static Vector<ReverseLookupSite> rls_list;

	private static int readLines = 0;

	private static Pattern namePattern = null;
	private static Pattern streetPattern = null;
	private static Pattern cityPattern = null;
	private static Pattern zipcodePattern = null;
	private static Pattern firstnamePattern = null;
	private static Pattern lastnamePattern = null;
	private static Pattern startlinePattern = null;
	private static Pattern endlinePattern = null;

	/**
	 * sets the default thread state to active
	 *
	 */
	public LookupThread(boolean quitOnDone) {
		threadSuspended = false;
		terminate = false;
		terminated = false;
		quit = quitOnDone;
	}

	/**
	 * This method iterates over all lookup requests and stops once
	 * no more are present, in which case it notifies ReverseLookup
	 * that it is done. If threadSuspended is true the thread while wait
	 * it is reactivated to continue doing lookups.
	 *
	 */
	public void run() {

		while (!terminate) {
			try {

			if(ReverseLookup.getRequestsCount() == 0 && quit)
			{
				Debug.debug("No more numbers to lookup!");
				break;
			}

			while((!terminate) && (threadSuspended || ReverseLookup.getRequestsCount() == 0)){
					Debug.info("Lookup done!");
					ReverseLookup.lookupDone();
					synchronized(this){
						wait();
					}
				}
			} catch (InterruptedException e) {
				Debug.debug("Lookup-Thread interrupted!");
	        	Thread.currentThread().interrupt();
			}

			if (!terminate)
			{
				currentRequest = ReverseLookup.getNextRequest();

				result = lookup(currentRequest.number, currentRequest.lookupSite);
				ReverseLookup.personFound(result);

				try{
					sleep(2000);
				}catch(Exception e){
					Debug.debug("Exception occourred: " + e.toString());
					break;
				}
			}
		}

		Debug.info("Lookup thread  has quit");
		terminated = true;
	}

	public synchronized void terminate(){
		terminate = true;
		resumeLookup();
	}

	public synchronized boolean isTerminated(){
		return terminated;
	}

	public synchronized void suspendLookup(){
		Debug.info("suspending lookup thread");
		threadSuspended = true;
	}

	public synchronized void resumeLookup(){
		threadSuspended = false;
		notify();
		Debug.info("resuming lookup thread again");
	}

	public synchronized boolean isSuspended(){
		return threadSuspended;
	}

	/**
	 * This function handles the actual "lookup" part. It uses the data loaded
	 * from number/international/revserlookup.xml to iterate over a series of
	 * websites and over series of patterns for processing each website
	 *
	 * @see de.moonflower.jfritz.struct.ReverseLookupSite
	 *
	 *
	 * @param number number to be looked up
	 * @return a new person object containing the results of the search
	 */
	static synchronized Person lookup(PhoneNumberOld number, String siteName) {

		Vector<Person> foundPersons = new Vector<Person>(10);
		String nummer = number.getAreaNumber();
		if (number.isFreeCall()) {
			Person p = new Person("", "FreeCall"); //$NON-NLS-1$,  //$NON-NLS-2$
			p.addNumber(number);
			foundPersons.add(p);
			Debug.debug("FreeCall detected");
		} else if (number.isSIPNumber() || number.isQuickDial()) {
			Person p = new Person();
			p.addNumber(number);
			foundPersons.add(p);
			Debug.debug("SIP-Number or QuickDial detected");
		} else if (ReverseLookup.rlsMap.containsKey(number.getCountryCode())) {

			rls_list = ReverseLookup.rlsMap.get(number.getCountryCode());

			Debug.info("Begin reverselookup for: "+nummer);

			//cut off the country code if were doing a non local lookup
			if(nummer.startsWith(number.getCountryCode())) {
				if (nummer.length() > number.getCountryCode().length()) {
					nummer = nummer.substring(number.getCountryCode().length());
					Debug.debug("Number has been refactored: " + nummer);
				}
				else
				{
					Debug.debug("Number has no country code, creating new Person as response!");
					Person p = new Person();
					p.addNumber(number);
					return p;
				}
			}

			//Iterate over all the web sites loaded for the given country
			for(int i=0; i < rls_list.size(); i++){
				Vector<Person> foundPersonsOnThisSite = new Vector<Person>(5);
				yield();

				ReverseLookupSite rls = rls_list.get(i);

				if(!siteName.equals("") && !siteName.equals(rls.getName())){
					Debug.warning("This lookup should be done using a specific site, skipping");
					continue;
				}

				String prefix = rls.getPrefix();
				int ac_length = rls.getAreaCodeLength();

				//needed to make sure international calls are formatted correctly
				if(!nummer.startsWith(prefix))
				{
					nummer = prefix + nummer;
					Debug.debug("Added prefix: " + nummer);
				}

				String urlstr = replacePlaceHoldersInURL(rls.getURL(), prefix, ac_length, nummer);

				Debug.info("Reverse lookup using: "+urlstr);
				URL url = null;
				String[] data = new String[MAX_DATA_LENGTH];

				//open a connection to the site
				try {
					url = new URL(urlstr);
					if (url != null) {

						try {
							URLConnection con = establishConnection(url, rls.getName());
							data = readSite(con);

							Debug.info("Begin processing response from "+rls.getName());
							// read reverse lookup response from file, only for DEBUG
							// data = overrideSiteResponse("c://dastelefon.htm"); //$NON-NLS-1$
							// debugOutputSiteResponse();

							//iterate over all patterns for this web site
							for(int j=0; j < rls.size(); j++){
								final String[] patterns = rls.getEntry(j);
								foundPersonsOnThisSite.addAll(parsePageWithPattern(data, number, readLines, patterns, rls.getNumLines(), rls.getName()));
							} //Done iterating for the given pattern

							yield();
						} catch (IOException e1) {
							Debug.error("Error while retrieving " + urlstr); //$NON-NLS-1$
						} catch (Exception e) {
							Debug.error("Exception in reverselookup"); //$NON-NLS-1$
						}
					}
				} catch (MalformedURLException e) {
					Debug.error("URL invalid: " + urlstr); //$NON-NLS-1$
				} catch (Exception e) {
				    Debug.error("Exception in reverselookup 2"); //$NON-NLS-1$
			    }

				foundPersons.addAll(foundPersonsOnThisSite);

			} // done iterating over all the loaded web sites
			yield();

		//no reverse lookup sites available for country
		} else {
			Debug.warning("No reverse lookup sites for: "+number.getCountryCode());
		}

		if (foundPersons.size() == 0) {
			// we have not found a entry, create a dummy entry
			Person p = new Person();
			foundPersons.add(p);
			p.addNumber(number.getAreaNumber(), "home"); //$NON-NLS-1$
		}

		// fix city for all found persons
		for (Person person: foundPersons) {
			fixCityIfNecessary(number, person, nummer);
		}

		// sorting person list by number of filled fields
		foundPersons = sortPersonList(foundPersons);

		return foundPersons.get(0);
	}

	private static String replacePlaceHoldersInURL(final String urlstr, final String prefix, final int ac_length, final String nummer) {
		String result = urlstr;
		if(result.contains("$AREACODE")
				&& (nummer.length() > (prefix.length()+ac_length))) {
			result = result.replaceAll("\\$AREACODE", nummer.substring(prefix.length(), ac_length+prefix.length()));
			result = result.replaceAll("\\$NUMBER", nummer.substring(prefix.length()+ac_length));
		}else if(result.contains("$PFXAREACODE")
				&& (nummer.length() > (prefix.length()+ac_length))){
			result = result.replaceAll("\\$PFXAREACODE", nummer.substring(0, prefix.length()+ac_length));
			result = result.replaceAll("\\$NUMBER", nummer.substring(prefix.length()+ ac_length));
		}else {
			result = result.replaceAll("\\$NUMBER", nummer);
		}
		return result;
	}

	private static URLConnection establishConnection(final URL url, final String lookupSiteName) throws IOException {
		URLConnection con = url.openConnection();
		// 5 Sekunden-Timeout für Verbindungsaufbau
		// 20 Sekunden-Timeout für die Antwort
		con.setConnectTimeout(5000);
		con.setReadTimeout(20000);
		con.addRequestProperty("User-Agent", USER_AGENT);
		con.connect();
		//process header
		//avoid problems with null headers
		String header = "";
		charSet = "";

		for (int j = 0;; j++) {
			String headerName = con.getHeaderFieldKey(j);
			String headerValue = con.getHeaderField(j);

			if (headerName == null && headerValue == null) {
				// No more headers
				break;
			}
			if ("content-type".equalsIgnoreCase(headerName)) { //$NON-NLS-1$
				String[] split = headerValue.split(";", 2); //$NON-NLS-1$
				for (int k = 0; k < split.length; k++) {
					if (split[k].trim().toLowerCase().startsWith(
							"charset=")) { //$NON-NLS-1$
						String[] charsetSplit = split[k].split("="); //$NON-NLS-1$
						charSet = charsetSplit[1].trim();
					}
				}
			}
			header += headerName + ": " + headerValue + " | "; //$NON-NLS-1$,  //$NON-NLS-2$
		}
		Debug.debug("Header of "+lookupSiteName+":" + header); //$NON-NLS-1$
		Debug.debug("CHARSET : " + charSet); //$NON-NLS-1$

		return con;
	}

	private static String[] readSite(final URLConnection con) throws UnsupportedEncodingException, IOException {
		String[] result = new String[MAX_DATA_LENGTH];
		// Get used Charset
		BufferedReader d;
		if (charSet.equals("")) { //$NON-NLS-1$
			d = new BufferedReader(new InputStreamReader(con
					.getInputStream(), "ISO-8859-1")); //$NON-NLS-1$
		} else {
			d = new BufferedReader(new InputStreamReader(con
					.getInputStream(), charSet));
		}
		//read in data, if the website has stalled the timer will kill the connection
		String tmpLine = "";
		readLines = 0;
		while (null != ((tmpLine = d.readLine()))) {
				result[readLines] = tmpLine;
				yield();
				readLines++;
				if ( readLines >= MAX_DATA_LENGTH ) {
					System.err.println("Result > " + MAX_DATA_LENGTH + " Lines");
					break;
				}
		}
		d.close();
		return result;
	}

	@SuppressWarnings("unused")
	private static void debugOutputSiteResponse(final String[] input) {
		for (int i=0; i<MAX_DATA_LENGTH; i++ )
		{
			if (input[i] != null)
			{
				Debug.debug("Lookup-Response: " + input[i]);
			}
		}
	}

	@SuppressWarnings("unused")
	private static String[] overrideSiteResponse(final String filePath) {
		Debug.debug("Debug mode: Loading " + filePath); //$NON-NLS-1$
		String[] result = new String[MAX_DATA_LENGTH];
		try {
			String thisLine;
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			readLines = 0;
			while ((thisLine = in.readLine()) != null) {
				result[readLines] = thisLine;
				yield();
				readLines++;
				if ( readLines >= MAX_DATA_LENGTH ) {
					System.err.println("Result > " + MAX_DATA_LENGTH + " Lines");
					readLines = MAX_DATA_LENGTH;
					break;
				}
			}
			in.close();
		} catch (IOException e) {
			Debug.error("File not found: " + filePath); //$NON-NLS-1$
		}
		return result;
	}

	private static Vector<Person> parsePageWithPattern(String[] input, PhoneNumberOld number,
			int lines, String[] patterns, int numLinesAtOnce, String lookupSiteName) {
		Vector<Person> foundPersonsOnThisSiteWithThisPattern = new Vector<Person>(5);
		List<ParseItem> parseItems = new ArrayList<ParseItem>();

		yield();

		boolean useStartLine = false;
		Matcher startlineMatcher = null;
		Matcher endlineMatcher = null;

		useStartLine = initPatterns(patterns);

		Debug.debug("Parsing " + lines + " lines:");
		boolean inParseArea = false;
		String currentLineToMatch;
		for (int line=0; line<lines; line++)
		{
			if (numLinesAtOnce == 1) {
				currentLineToMatch = input[line];
			} else {
				currentLineToMatch="";
				for (int lineIt=0; lineIt<numLinesAtOnce; lineIt++) {
					if (line+lineIt<lines) {
						if (!input[line+lineIt].trim().equals(""))
							currentLineToMatch += input[line+lineIt];
					}
				}
			}
			if (currentLineToMatch != null)
			{
				currentLineToMatch = currentLineToMatch.replaceAll(new Character((char)SPACE_ALTERNATIVE).toString(), " "); //$NON-NLS-1$

				if (useStartLine)
				{
					if (startlinePattern != null)
					{
						startlineMatcher = startlinePattern.matcher(currentLineToMatch);
						if (startlineMatcher.find()) {
							inParseArea = true;
						}
					}
					if (endlinePattern != null)
					{
						endlineMatcher = endlinePattern.matcher(currentLineToMatch);
						if (endlineMatcher.find()) {
							inParseArea = false;
						}
					}
				}

				if (!useStartLine || (useStartLine && inParseArea))
				{
					matchLine(patterns, parseItems, namePattern,
							streetPattern, cityPattern,
							zipcodePattern,
							firstnamePattern,
							lastnamePattern,
							currentLineToMatch, line);
				}
			}
		}
		Collections.sort(parseItems);
		Person p = new Person();

		for (ParseItem parseItem: parseItems) {
			p = createPersonsFromParseResults(foundPersonsOnThisSiteWithThisPattern,
					p, parseItem);
		}
		foundPersonsOnThisSiteWithThisPattern.add(p);
		// set lookup site and number for all found entries
		for (Person person: foundPersonsOnThisSiteWithThisPattern) {
			person.setLookupSite(lookupSiteName);
			person.addNumber(number.getAreaNumber(), "home"); //$NON-NLS-1$
		}

		return foundPersonsOnThisSiteWithThisPattern;
	}

	private static void fixCityIfNecessary(PhoneNumberOld number, Person person, final String nummer) {
		if ((person.getCity() == null) || ("".equals(person.getCity())))
		{
			if(number.getCountryCode().equals(ReverseLookup.GERMANY_CODE))
				person.setCity(ReverseLookupGermany.getCity(nummer));
			else if(number.getCountryCode().equals(ReverseLookup.AUSTRIA_CODE))
				person.setCity(ReverseLookupAustria.getCity(nummer));
			else if(number.getCountryCode().startsWith(ReverseLookup.USA_CODE))
				person.setCity(ReverseLookupUnitedStates.getCity(nummer));
			else if(number.getCountryCode().startsWith(ReverseLookup.TURKEY_CODE))
				person.setCity(ReverseLookupTurkey.getCity(nummer));
		}
	}

	private static boolean initPatterns(final String[] patterns) {
		boolean useStartLine = false;
		namePattern = null;
		streetPattern = null;
		cityPattern = null;
		zipcodePattern = null;
		firstnamePattern = null;
		lastnamePattern = null;
		startlinePattern = null;
		endlinePattern = null;


		if (!patterns[ReverseLookupSite.STARTLINE].equals("")) {
			Debug.debug("Startline detected");
			startlinePattern = Pattern.compile(patterns[ReverseLookupSite.STARTLINE]);
			useStartLine = true;
		}
		if (!patterns[ReverseLookupSite.ENDLINE].equals("")) {
			Debug.debug("Endline detected");
			endlinePattern = Pattern.compile(patterns[ReverseLookupSite.ENDLINE]);
			useStartLine = true;
		}

		if (!patterns[ReverseLookupSite.NAME].equals("")
				&& (patterns[ReverseLookupSite.FIRSTNAME].equals("")
				             && patterns[ReverseLookupSite.LASTNAME].equals("")))
		{
			namePattern = Pattern.compile(patterns[ReverseLookupSite.NAME]);
			Debug.debug("Name-Pattern: " + namePattern.toString());
		}
		if (!patterns[ReverseLookupSite.STREET].equals(""))
		{
			streetPattern = Pattern.compile(patterns[ReverseLookupSite.STREET]);
			Debug.debug("Street-Pattern: " + streetPattern.toString());
		}
		if (!patterns[ReverseLookupSite.CITY].equals(""))
		{
			cityPattern = Pattern.compile(patterns[ReverseLookupSite.CITY]);
			Debug.debug("City-Pattern: " + cityPattern.toString());
		}
		if (!patterns[ReverseLookupSite.ZIPCODE].equals(""))
		{
			zipcodePattern = Pattern.compile(patterns[ReverseLookupSite.ZIPCODE]);
			Debug.debug("ZipCode-Pattern: " + zipcodePattern.toString());
		}
		if (!patterns[ReverseLookupSite.FIRSTNAME].equals(""))
		{
			firstnamePattern = Pattern.compile(patterns[ReverseLookupSite.FIRSTNAME]);
			Debug.debug("FirstName-Pattern: " + firstnamePattern.toString());
		}
		if (!patterns[ReverseLookupSite.LASTNAME].equals(""))
		{
			lastnamePattern = Pattern.compile(patterns[ReverseLookupSite.LASTNAME]);
			Debug.debug("LastName-Pattern: " + lastnamePattern.toString());
		}
		return useStartLine;
	}

	private static void matchLine(String[] patterns, List<ParseItem> parseItems,
			Pattern namePattern, Pattern streetPattern, Pattern cityPattern,
			Pattern zipcodePattern, Pattern firstnamePattern,
			Pattern lastnamePattern, String currentLineToMatch, int line) {
		Matcher nameMatcher;
		Matcher streetMatcher;
		Matcher cityMatcher;
		Matcher zipcodeMatcher;
		Matcher firstnameMatcher;
		Matcher lastnameMatcher;
		//match first name
		if(firstnamePattern != null){
			firstnameMatcher = firstnamePattern.matcher(currentLineToMatch);
			if(firstnameMatcher.find()){
				parseItems.add(parseLine(ParseItemType.FIRSTNAME, firstnameMatcher, line));
			}
		}

		yield();
		//match last name
		if(lastnamePattern != null){
			lastnameMatcher = lastnamePattern.matcher(currentLineToMatch);
			if(lastnameMatcher.find()){
				parseItems.add(parseLine(ParseItemType.LASTNAME, lastnameMatcher, line));
			}
		}

		yield();
		//match name
		if(namePattern != null){
			nameMatcher = namePattern.matcher(currentLineToMatch);
			if(nameMatcher.find()){
				parseItems.addAll(parseNameFields(patterns, nameMatcher, line));
			}
		}

		yield();
		//match street
		if(streetPattern != null){
			streetMatcher = streetPattern.matcher(currentLineToMatch);
			if(streetMatcher.find()){
				parseItems.add(parseLine(ParseItemType.STREET, streetMatcher, line));
			}
		}
		yield();
		//match city
		if(cityPattern != null){
			cityMatcher = cityPattern.matcher(currentLineToMatch);
			if(cityMatcher.find()){
				parseItems.add(parseLine(ParseItemType.CITY, cityMatcher, line));
			}
		}

		yield();
		//match zip code
		if(zipcodePattern != null){
			zipcodeMatcher = zipcodePattern.matcher(currentLineToMatch);
			if(zipcodeMatcher.find()){
				parseItems.add(parseLine(ParseItemType.ZIPCODE, zipcodeMatcher, line));
			}
		}
	}

	private static List<ParseItem> parseNameFields(String[] patterns, Matcher nameMatcher, int line) {
		//read in and concatenate all groupings
		String str = "";
		for(int k=1; k <= nameMatcher.groupCount(); k++){
			if(nameMatcher.group(k) != null)
				str = str + nameMatcher.group(k).trim() + " ";
		}

		String[] split;
		split = str.split(" ", 2); //$NON-NLS-1$

		String foundFirst = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(split[0]));
		foundFirst = foundFirst.trim();
		foundFirst = foundFirst.replaceAll(",", "");
		foundFirst = foundFirst.replaceAll("%20", " ");
		foundFirst = JFritzUtils.replaceSpecialCharsUTF(foundFirst);
		foundFirst = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(foundFirst));
		foundFirst = JFritzUtils.removeDuplicateWhitespace(foundFirst);

		String foundSecond = "";
		String company = "";
		if (split[1].length() > 0) {
			foundSecond = HTMLUtil.stripEntities(split[1]); //$NON-NLS-1$
			if ((foundSecond.indexOf("  ") > -1) //$NON-NLS-1$
					&& (foundSecond.indexOf("  u.") == -1)) { //$NON-NLS-1$
				company = JFritzUtils.removeLeadingSpaces(foundSecond.substring(
						foundSecond.indexOf("  ")).trim()); //$NON-NLS-1$
				foundSecond = JFritzUtils.removeLeadingSpaces(foundSecond.substring(0,
						foundSecond.indexOf("  ")).trim()); //$NON-NLS-1$
			} else {
				foundSecond = JFritzUtils.removeLeadingSpaces(foundSecond.replaceAll("  u. ", //$NON-NLS-1$
						" und ")); //$NON-NLS-1$
			}
		}

		foundSecond = foundSecond.replaceAll("%20", " ");
		foundSecond = JFritzUtils.replaceSpecialCharsUTF(foundSecond);
		foundSecond = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(foundSecond));
		foundSecond = JFritzUtils.removeDuplicateWhitespace(foundSecond);
		foundSecond = foundSecond.trim();

		company = company.replaceAll("%20", " ");
		company = JFritzUtils.replaceSpecialCharsUTF(company);
		company = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(company));
		company = JFritzUtils.removeDuplicateWhitespace(company);
		company = company.trim();

		ParseItem firstnameItem = new ParseItem(ParseItemType.FIRSTNAME);
		ParseItem lastnameItem = new ParseItem(ParseItemType.LASTNAME);
		ParseItem companyItem = new ParseItem(ParseItemType.COMPANY);
		companyItem.setLine(line);
		companyItem.setStartIndex(nameMatcher.start(1));
		companyItem.setValue(company);

		if (JFritzUtils.parseBoolean(patterns[ReverseLookupSite.SWAPFIRSTANDLASTNAME]))
		{
			firstnameItem.setLine(line);
			firstnameItem.setStartIndex(nameMatcher.start(1));
			firstnameItem.setValue(foundFirst);
			lastnameItem.setLine(line);
			lastnameItem.setStartIndex(nameMatcher.start(1));
			lastnameItem.setValue(foundSecond);
		} else {
			firstnameItem.setLine(line);
			firstnameItem.setStartIndex(nameMatcher.start(1));
			firstnameItem.setValue(foundSecond);
			lastnameItem.setLine(line);
			lastnameItem.setStartIndex(nameMatcher.start(1));
			lastnameItem.setValue(foundFirst);
		}

		List<ParseItem> result = new ArrayList<ParseItem>();
		result.add(firstnameItem);
		result.add(lastnameItem);
		result.add(companyItem);

		return result;
	}

	private static Vector<Person> sortPersonList(final Vector<Person> input) {
		Vector<Person> personList = new Vector<Person>();

		Debug.info("" + input.size());
		boolean notFound = false;
		while (!notFound) {
			notFound = true;
			int numMostFilledFields = -1;
			Person mostFilledFieldsPerson = null;
			int tmp = -1;
			for (Person p: input) {
				if (!personList.contains(p)) {
					tmp = p.getNumFilledFields();
					if (tmp>numMostFilledFields) {
						numMostFilledFields = tmp;
						mostFilledFieldsPerson = p;
						notFound = false;
					}
				}
			}
			if (mostFilledFieldsPerson != null) {
				personList.add(mostFilledFieldsPerson);
				Debug.debug("Next person: " + tmp + " / "
						+ " site: " + mostFilledFieldsPerson.getLookupSite()
						+ " fullName: " + mostFilledFieldsPerson.getFullname()
						+ " street:" + mostFilledFieldsPerson.getStreet()
						+ " plz:" + mostFilledFieldsPerson.getPostalCode()
						+ " city:" + mostFilledFieldsPerson.getCity());
			}
		}
		return personList;
	}

	private static ParseItem parseLine(final ParseItemType type, final Matcher matcher, final int lineNumber) {
		//read in and concatenate all groupings
		String str = "";
		for(int k=1; k <= matcher.groupCount(); k++){
			if(matcher.group(k) != null)
				str = str + matcher.group(k).trim() + " ";
		}

		String value = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(str));
		value = value.trim();
		value = value.replaceAll(",", "");
		value = value.replaceAll("%20", " ");
		value = JFritzUtils.replaceSpecialCharsUTF(value);
		value = JFritzUtils.removeLeadingSpaces(HTMLUtil.stripEntities(value));
		value = JFritzUtils.removeDuplicateWhitespace(value);
		value = value.trim();

		ParseItem parseItem = new ParseItem(type);
		parseItem.setLine(lineNumber);
		parseItem.setStartIndex(matcher.start(1));
		parseItem.setValue(value);

		return parseItem;
	}

	private static Person createPersonsFromParseResults(
			Vector<Person> foundPersons, Person p, ParseItem parseItem) {
		switch (parseItem.getType()) {
			case FIRSTNAME:
				if (!"".equals(p.getFirstName())) {
					p = addNewPerson(foundPersons, p);
				}
				p.setFirstName(parseItem.getValue());
				break;
			case LASTNAME:
				if (!"".equals(p.getLastName())) {
					p = addNewPerson(foundPersons, p);
				}
				p.setLastName(parseItem.getValue());
				break;
			case CITY:
				if (!"".equals(p.getCity())) {
					p = addNewPerson(foundPersons, p);
				}
				p.setCity(parseItem.getValue());
				break;
			case COMPANY:
				if (!"".equals(p.getCompany())) {
					p = addNewPerson(foundPersons, p);
				}
				p.setCompany(parseItem.getValue());
				break;
			case STREET:
				if (!"".equals(p.getStreet())) {
					p = addNewPerson(foundPersons, p);
				}
				p.setStreet(parseItem.getValue());
				break;
			case ZIPCODE:
				if (!"".equals(p.getPostalCode())) {
					p = addNewPerson(foundPersons, p);
				}
				p.setPostalCode(parseItem.getValue());
				break;
		default: break;
		}
		return p;
	}

	private static Person addNewPerson(Vector<Person> foundPersons, Person p) {
		foundPersons.add(p);
		p = new Person();
		return p;
	}

}



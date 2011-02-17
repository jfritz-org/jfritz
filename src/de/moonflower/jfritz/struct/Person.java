package de.moonflower.jfritz.struct;

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.HTMLUtil;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupAustria;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupGermany;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupTurkey;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupUnitedStates;

/**
 * @author rob
 *
 */
public class Person implements Cloneable, Serializable{

	private static final long serialVersionUID = 104;

	private static final int SCALE_WIDTH = 40;

	private static final int SCALE_HEIGHT = 50;

	private boolean privateEntry = false;

	private String firstName = ""; //$NON-NLS-1$

	private String lastName = ""; //$NON-NLS-1$

	private String company = ""; //$NON-NLS-1$

	private String street = ""; //$NON-NLS-1$

	private String postalCode = ""; //$NON-NLS-1$

	private String city = ""; //$NON-NLS-1$

	private String standard = ""; //$NON-NLS-1$

	private String emailAddress = ""; //$NON-NLS-1$

	private Vector<PhoneNumberOld> numbers;

	private String pictureUrl = ""; //$NON-NLS-1$

	private ImageIcon scaledPicture = null;

	private String lookupSite = ""; //$NON-NLS-1$

	private String[] basicTypes = {"home", "mobile", "homezone", "business", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$
			"other", "fax", "sip", "main"}; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$


	public Person() {
		numbers = new Vector<PhoneNumberOld>();
	}

	/**
	 * this function is needed to override the basic function equals
	 * inherited by object or else the network code won't work at all
	 *
	 * @author Robert
	 *
	 */
	public boolean equals(Object p){
		Person person;
		if(!(p instanceof Person))
			return false;

		person = (Person) p;

		if ((person.lastName.equals(this.lastName))
			&& (person.firstName.equals(this.firstName))
			&& (person.numbers.size() == this.numbers.size())
			&& (person.standard.equals(this.standard))
			&& (person.city.equals(this.city))
			&& (person.street.equals(this.street))
			&& (person.postalCode.equals(this.postalCode))
			&& ((person.privateEntry == this.privateEntry))
			&& (person.pictureUrl.equals(this.pictureUrl))
			&& (person.company.equals(this.company))
			&& (person.emailAddress.equals(this.emailAddress)))
		{
			PhoneNumberOld myNumber;
			PhoneNumberOld hisNumber;
			for (int i=0; i<this.numbers.size(); i++)
			{
				myNumber = this.numbers.get(i);
				hisNumber = person.numbers.get(i);
				if (!myNumber.equals(hisNumber))
				{
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public Person(final String firstName, final String company, final String lastName,
			final String street, final String postalCode, final String city, final String eMail,
			final String pictureUrl) {
		this();

		this.firstName = firstName;
		this.company = company;
		this.lastName = lastName;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
		this.emailAddress = eMail;
		this.pictureUrl = pictureUrl;
		this.privateEntry = false;

		updateScaledPicture();
	}

	public Person(final String firstName, final String lastName) {
		this();

		this.firstName = firstName;
		this.lastName = lastName;
		this.privateEntry = false;
	}

	private void copyFrom(final Person person) {
		firstName = person.getFirstName();
		company = person.getCompany();
		lastName = person.getLastName();
		street = person.getStreet();
		postalCode = person.getPostalCode();
		city = person.getCity();
		emailAddress = person.getEmailAddress();
		standard = person.getStandard();
		pictureUrl = person.getPictureUrl();
		scaledPicture = person.getScaledPicture();
		numbers.clear();
		final Enumeration<PhoneNumberOld> en = person.getNumbers().elements(); // NOPMD
		while (en.hasMoreElements()) {
			numbers.add(en.nextElement().clone());
		}
		privateEntry = person.isPrivateEntry();
	}

	public void addNumber(final PhoneNumberOld number) {
		numbers.add(number);
		if (numbers.size() == 1)
		{
			setStandard(number.getType());
		}
	}

	public void addNumber(final String number, final String type) {
		final PhoneNumberOld pNumber = new PhoneNumberOld(number, false); // NOPMD
		pNumber.setType(type);
		addNumber(pNumber);
	}

	public Vector<PhoneNumberOld> getNumbers() {
		return numbers;
	}

	public void setNumbers(final Vector<PhoneNumberOld> numbers, final String std) {
		this.numbers = numbers;
		setStandard(std);

		if (this.standard.equals("") && this.numbers.size() >= 1) {
			// if there is no standard defined, promote the first number
			this.standard = this.numbers.get(0).getType();
		}
	}

	public boolean isEmpty() {
		return getFullname().equals("") && numbers.size() < 2; //$NON-NLS-1$
	}

	public String getFullname() {
		String ret;
		if ((lastName == null) && (firstName == null)) {
			ret = "";  //$NON-NLS-1$
		}
		else if (lastName == null)
		{
			ret = firstName;
		}
		else if (firstName == null)
		{
			ret = lastName;
		}
		else if (lastName.length() == 0 && firstName.length() > 0)
		{
			ret = firstName;
		}
		else if (firstName.length() == 0)
		{
			ret = lastName;
		}
		else
		{
			ret = (lastName + ", " + firstName).trim(); //$NON-NLS-1$
		}
		if ((company != null) && (company.length() > 0)) {
			if (ret.length() > 0)
			{
				ret += " (" + company + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
			}
			else
			{
				ret = company;
			}
		}
		return ret;
	}

	// TODO Privat & Geschäftlich durch Konstanten ersetzen
	// private String[] basicTypes = { "home", "mobile", "homezone",
	// "business", "other", "fax", "sip" };
	// TODO Sonstiges und Nichtgefundenes
	// TODO sip != Pager, korrigieren
	public String toVCard() {
		StringBuffer vcard = new StringBuffer(111);  //$NON-NLS-1$
		vcard.append("BEGIN:vCard\r\n" //$NON-NLS-1$
				+ "VERSION:2.1\r\n" //$NON-NLS-1$
				);
				// name: lastName;firstName;moreNames;Prefix;Suffix
					vcard.append("N: " + this.getLastName() + ";" + this.getFirstName() + ";;;\r\n");
				if (!this.getCompany().equals("")) {
					vcard.append("ORG: " + this.getCompany() + ";\r\n");
				}
				// formated name
				vcard.append("FN: " + getFullname() + "\r\n"); //$NON-NLS-1$
				// address: PostOfficeAddress;ExtendedAddress;Street;Locality;Region;PostalCode;Counry
				vcard.append("ADR;Type=HOME;POSTAL:;;" + getStreet() + ";" //$NON-NLS-1$,  //$NON-NLS-2$
				+ getCity() + ";;" + getPostalCode() + ";\r\n"); //$NON-NLS-1$,  //$NON-NLS-2$
		Enumeration<PhoneNumberOld> en = numbers.elements();
		PhoneNumberOld number;
		while (en.hasMoreElements()) {
			number = en.nextElement();
			if (number.getType().startsWith("home")) //$NON-NLS-1$
			{
				vcard.append("TEL;TYPE=VOICE;HOME:"); //$NON-NLS-1$
			}
			else if (number.getType().startsWith("business")) //$NON-NLS-1$
			{
				vcard.append("TEL;TYPE=VOICE;WORK:"); //$NON-NLS-1$
			}
			else if (number.getType().startsWith("mobile")) //$NON-NLS-1$
			{
				vcard.append("TEL;CELL:"); //$NON-NLS-1$
			}
			else if (number.getType().startsWith("sip")) //$NON-NLS-1$
			{
				vcard.append("TEL;PAGER:"); //$NON-NLS-1$
			}
			else if (number.getType().startsWith("fax")) //$NON-NLS-1$
			{
				vcard.append("TEL;FAX:"); //$NON-NLS-1$
			}
			else
			{
				vcard.append("TEL;VOICE:"); //$NON-NLS-1$
			}
			vcard.append(number.convertToIntNumber());
			vcard.append("\r\n");
		}
		vcard.append("EMAIL;TYPE=INTERNET:");
		vcard.append(getEmailAddress());
		vcard.append("\r\n");
		vcard.append("END:vCard\r\n"); //$NON-NLS-1$
		vcard.append("\r\n");
		return vcard.toString();
	}

	/**
	 * Saves vcard to file
	 *
	 * @param file
	 */
	public void saveToVCard(final File file) {
		FileOutputStream fos;
		PrintWriter pWriter;
		try {
			fos = new FileOutputStream(file);
			pWriter = new PrintWriter(fos);
			pWriter.println(toVCard());
			pWriter.close();
		} catch (FileNotFoundException e) {
			Debug.error("Could not write " + file.getName() + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	public String getFirstName() {
		if (firstName == null)
		{
			return ""; //$NON-NLS-1$
		}
		return firstName;
	}

	public String getCompany() {
		if (company == null)
		{
			return ""; //$NON-NLS-1$
		}
		return company;
	}

	public String getLastName() {
		if (lastName == null)
		{
			return ""; //$NON-NLS-1$
		}
		return lastName;
	}

	public String getStreet() {
		if (street == null)
		{
			return ""; //$NON-NLS-1$
		}
		return street;
	}

	public String getPostalCode() {
		if (postalCode == null)
		{
			return ""; //$NON-NLS-1$
		}
		return postalCode;
	}

	public String getCity() {
		if (city == null)
		{
			return ""; //$NON-NLS-1$
		}
		return city;
	}

	public String getPictureUrl() {
		if (pictureUrl == null)
		{
			return ""; //$NON-NLS-1$
		}
		return pictureUrl;
	}

	public ImageIcon getScaledPicture() {
		return scaledPicture;
	}

	public PhoneNumberOld getPhoneNumber(final String type) {
		final Enumeration<PhoneNumberOld> enumeration = numbers.elements(); // NOPMD
		PhoneNumberOld number;
		while (enumeration.hasMoreElements()) {
			number = enumeration.nextElement();
			if (number.getType().equals(type))
				return number;
		}
		return null;
	}

	/**
	 * @return Returns the standard PhoneNumber
	 */
	public PhoneNumberOld getStandardTelephoneNumber() {
		return getPhoneNumber(standard);
	}

	/**
	 * Checks if person has telephone number
	 *
	 * @param number
	 *            the phone number to check as String
	 * @param considerMain
	 *            true, if main number sould be considered
	 * @return True if person has a phone number
	 */
	public boolean hasNumber(final String number, final boolean considerMain) {
		Enumeration<PhoneNumberOld> en = numbers.elements();
		PhoneNumberOld numb;
		while (en.hasMoreElements()) {
			numb = en.nextElement();
			if ((numb.getType().startsWith("main")) && (considerMain)) { //$NON-NLS-1$
				// starts with ...
				if (number.startsWith(numb.getIntNumber()))
					return true;
			} else { // equal number
				if (number.equals(numb.getIntNumber()))
					return true;
			}
		}
		return false;
	}

	public String getEmailAddress() {
		if (emailAddress == null)
		{
			return ""; //$NON-NLS-1$
		}
		return emailAddress;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public void setCompany(final String middleName) {
		this.company = middleName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public void setStreet(final String street) {
		this.street = street;
	}

	public void setPostalCode(final String postCode) {
		this.postalCode = postCode;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public void setEmailAddress(final String email) {
		this.emailAddress = email;
	}

	public void setPictureUrl(final String pictureUrl) {
		this.pictureUrl = pictureUrl;
		updateScaledPicture();
	}

	/**
	 * @return Returns the standard Number.
	 */
	public final String getStandard() {
		if (standard == null)
		{
			return ""; //$NON-NLS-1$
		}
		return standard;
	}

	/**
	 * @param standard
	 *            Sets standard number
	 */
	public final void setStandard(final String standard) {
		if  (getPhoneNumber(standard) == null) {
			// do not set a type as standard, if the number does not exist
			return;
		}
		this.standard = standard;
	}

	public void setPrivateEntry(final boolean b) {
		privateEntry = b;
	}

	public boolean isPrivateEntry() {
		return privateEntry;
	}

	/**
	 * Creates the address of that person separated by given separators
	 *
	 * @return the address as String
	 * @param
	 * @author Benjamin Schmitt
	 */
	public String getAddress() {
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		String wordSeparator = " "; //$NON-NLS-1$
		// used to separate words in one line, e.g.
		// between firstname an surname
		return this.getAddress(lineSeparator, wordSeparator);
	}

	/**
	 * Creates the address of that person separated by given separators
	 *
	 * @return the address as String
	 * @param
	 * @author Benjamin Schmitt
	 */
	public String getAddress(final String lineSeparator, final String wordSeparator) {

		// TODO: create patterns as params for that function

		if (this == null) {return "";}//$NON-NLS-1$
		String address = ""; //$NON-NLS-1$

		String company = this.getCompany();
		String firstName = this.getFirstName();
		String lastName = this.getLastName();
		String street = this.getStreet();
		String postalCode = this.getPostalCode();
		String city = this.getCity();

		address = (!company.equals("") ? company + lineSeparator : "") //$NON-NLS-1$,  //$NON-NLS-2$
				+ (!firstName.equals("") ? firstName + wordSeparator : "") //$NON-NLS-1$,  //$NON-NLS-2$
				+ (!lastName.equals("") ? lastName + lineSeparator : "") //$NON-NLS-1$,  //$NON-NLS-2$
				+ (!street.equals("") ? street + lineSeparator : "") //$NON-NLS-1$,  //$NON-NLS-2$
				+ (!postalCode.equals("") ? postalCode + wordSeparator : "") //$NON-NLS-1$,  //$NON-NLS-2$
				+ (!city.equals("") ? city + lineSeparator : ""); //$NON-NLS-1$,  //$NON-NLS-2$
		return address;
	}

	/**
	 * @author Bastian Schaefer
	 * @param separator Separator for using in CSV-files
	 * @return Returns CSV String
	 */
	public String toCSV(char separator) {
		String outString = ""; //$NON-NLS-1$

		// private contact?
		if (privateEntry) {
			outString = "\"YES\""; //$NON-NLS-1$
		} else {
			outString = "\"NO\""; //$NON-NLS-1$
		}

		// last name
		outString = outString.concat(separator+"\"" + getLastName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// first name
		outString = outString.concat(separator+"\"" + getFirstName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// company
		outString = outString.concat(separator+"\"" + getCompany() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// Street
		outString = outString.concat(separator+"\"" + getStreet() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// Postal Code
		outString = outString.concat(separator+"\"" + getPostalCode() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// city
		outString = outString.concat(separator+"\"" + getCity() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// email
		outString = outString.concat(separator+"\"" + getEmailAddress() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// picture
		outString = outString.concat(separator+"\"" + getPictureUrl() + "\""); //$NON-NLS-1$

		// numbers
		if (getNumbers() == null)
		{
			outString = outString.concat(separator+"\"\""); //$NON-NLS-1$
		}
		else
		{
			for (int i = 0; i < 8; i++) {
				try {
					outString = outString.concat(separator+"\""//$NON-NLS-1$
							+ getPhoneNumber(basicTypes[i].replaceAll(" ", ""))//$NON-NLS-1$, //$NON-NLS-2$
									.toString().replaceAll("\\[|\\]", "")//$NON-NLS-1$, //$NON-NLS-2$
							+ "\"");//$NON-NLS-1$
				} catch (NullPointerException ex) {
					outString = outString.concat(separator+"\"\"");//$NON-NLS-1$
				}

			}
		}

		return outString;
	}

	/**
	 * used by client server model
	 * @return person in csv
	 */
	public String toCSV() {
		String outString = ""; //$NON-NLS-1$

		// private contact?
		if (privateEntry) {
			outString = "\"YES\""; //$NON-NLS-1$
		} else {
			outString = "\"NO\""; //$NON-NLS-1$
		}

		// last name
		outString = outString.concat(";\"" + getLastName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// first name
		outString = outString.concat(";\"" + getFirstName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// company
		outString = outString.concat(";\"" + getCompany() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// Street
		outString = outString.concat(";\"" + getStreet() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// Postal Code
		outString = outString.concat(";\"" + getPostalCode() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// city
		outString = outString.concat(";\"" + getCity() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// email
		outString = outString.concat(";\"" + getEmailAddress() + "\";\""); //$NON-NLS-1$,  //$NON-NLS-2$

		//@todo meybe also transmit picture

		// numbers
		if (getNumbers() == null)
		{
			outString = outString.concat("\""); //$NON-NLS-1$
		}
		else
		{
			for (PhoneNumberOld number: numbers)
			{
				outString = outString.concat(number.toCSV()+":");
			}
		}

		outString = outString.substring(0, outString.length()-1);
		outString = outString.concat("\"");

		return outString;
	}

	/**
	 * Use this only to generate a string for debug-output
	 * @return person in csv
	 */
	public String toDebugStr() {
		String outString = ""; //$NON-NLS-1$

		if (!"".equals(lookupSite)) {
			outString = "\"" + lookupSite + "\";";
		}

		// private contact?
		if (privateEntry) {
			outString = outString.concat("\"YES\""); //$NON-NLS-1$
		} else {
			outString = outString.concat("\"NO\""); //$NON-NLS-1$
		}

		// last name
		outString = outString.concat(";\"" + getLastName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// first name
		outString = outString.concat(";\"" + getFirstName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// company
		outString = outString.concat(";\"" + getCompany() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// Street
		outString = outString.concat(";\"" + getStreet() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// Postal Code
		outString = outString.concat(";\"" + getPostalCode() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// city
		outString = outString.concat(";\"" + getCity() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// email
		outString = outString.concat(";\"" + getEmailAddress() + "\";\""); //$NON-NLS-1$,  //$NON-NLS-2$

		//@todo meybe also transmit picture

		// numbers
		if (getNumbers() == null)
		{
			outString = outString.concat("\""); //$NON-NLS-1$
		}
		else
		{
			for (PhoneNumberOld number: numbers)
			{
				outString = outString.concat(number.toCSV()+":");
			}
		}

		outString = outString.substring(0, outString.length()-1);
		outString = outString.concat("\"");

		return outString;
	}

	/**
	 * @author: haeusler DATE: 02.04.06, added by Brian This is part of a fix
	 *          for the null pointer exceptions that are caused by adding a
	 *          contact when a filter is set
	 */
	public boolean matchesKeyword(final String key) {
		if (key == null || key.equals("")) {//$NON-NLS-1$
			return true;
		}
		if (getFullname().toLowerCase().indexOf(key.toLowerCase()) != -1) {
			return true;
		}
		Enumeration<PhoneNumberOld> en = numbers.elements();
		PhoneNumberOld number;
		while (en.hasMoreElements()) {
			number = en.nextElement();

            if ( key.charAt(0) == '+') {
                if (number.getIntNumber().indexOf(key) != -1) {
                    return true;
                }
            } else if (number.getAreaNumber().indexOf(key) != -1) {
				return true;
			}
		}
		if (getAddress().toLowerCase().indexOf(key.toLowerCase()) != -1) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the current object supersedes a given one.
	 *
	 * @param p data object which is checked for redundancy
	 * @return true if p contains only redundant information
	 */
	public boolean supersedes(final Person p) {
		// Person data is checked
		if ((firstName.indexOf(p.firstName) == -1)
			|| (lastName.indexOf(p.lastName) == -1)
			|| (company.indexOf(p.company) == -1)
			|| (street.indexOf(p.street) == -1)
			|| (postalCode.indexOf(p.postalCode) == -1)
			|| (city.indexOf(p.city) == -1)
			|| (emailAddress.indexOf(p.emailAddress) == -1))
		{
			return false;
		}

		// Ensuring that this person has more numbers
		if (numbers.size() < p.numbers.size())
		{
			return false;
		}

		// Creating a set of this person's numbers
		Enumeration<PhoneNumberOld> ownNumberEnum = numbers.elements();
		Set<String> ownNumberSet = new HashSet<String>();
		PhoneNumberOld number;
		while (ownNumberEnum.hasMoreElements()) {
			number = ownNumberEnum.nextElement();
			ownNumberSet.add(number.getIntNumber());
		}

		// Checking whether this person's numbers are a real superset
		Enumeration<PhoneNumberOld> otherNumberEnum = p.numbers.elements();
		while (otherNumberEnum.hasMoreElements()) {
			number = (PhoneNumberOld) otherNumberEnum.nextElement();
			if (! ownNumberSet.contains(number.getIntNumber()))
			{
				return false;
			}
		}

		return true;
	}

	public Person clone() {
		Person p = new Person();
		p.copyFrom(this);
		return p;
	}

	public boolean isDummy() {
		return getFullname().equals("") && (getNumbers().size() == 1 || getNumbers().size() == 0)
				&& getCompany().equals("")
				&& getEmailAddress().equals("")
				&& getPostalCode().equals("")
				&& getStreet().equals("")
				&& getPictureUrl().equals("");
	}

	private void updateScaledPicture()
	{
		if (! "".equals(pictureUrl))
		{
			ImageIcon pictureIcon = new ImageIcon(pictureUrl);

			// if we don't find the image, display the default one
			if (pictureIcon.getIconWidth() == -1 || pictureIcon.getIconHeight() == -1)
			{
				pictureIcon = new ImageIcon("");
			}
			float pictureWFactor = (float)pictureIcon.getIconWidth() / (float)SCALE_WIDTH;
			float pictureHFactor = (float)pictureIcon.getIconHeight() / (float)SCALE_HEIGHT;

			int scaleToWidth = 0;
			int scaleToHeight = 0;
			if ( pictureWFactor > pictureHFactor )
			{
				scaleToWidth = (int)((float)pictureIcon.getIconWidth() / pictureWFactor);
				scaleToHeight = (int)((float)pictureIcon.getIconHeight() / pictureWFactor);
			}
			else
			{
				scaleToWidth = (int)((float)pictureIcon.getIconWidth() / pictureHFactor);
				scaleToHeight = (int)((float)pictureIcon.getIconHeight() / pictureHFactor);
			}

			this.scaledPicture = new ImageIcon(pictureIcon.getImage().getScaledInstance(scaleToWidth, scaleToHeight, Image.SCALE_SMOOTH));
		}
	}

	public void setLookupSite(String site) {
		this.lookupSite = site;
	}

	public String getLookupSite() {
		return this.lookupSite;
	}

	public String getGoogleLink() {
		String loc = Main.getProperty("locale");
		String googlePrefix = "http://maps.google.com/maps?f=q&hl="+ loc.substring(0, 2) +"&q=";
		String googleLink = "";
		PhoneNumberOld localNumber = null;
		googleLink += HTMLUtil.stripEntities(street)+", ";

		for (PhoneNumberOld number: numbers)
		{
			if (!number.isEmergencyCall()
				&& !number.isFreeCall()
				&& !number.isQuickDial()
				&& !number.isSIPNumber()
				)
			{
				if (localNumber != null)
				{
					if ((localNumber.isMobile())
					&& (!number.isMobile())) {
						localNumber = number;
					}
				}
				else {
					localNumber = number;
				}
			}
		}
		if ( city.replaceAll(" ", "").equals(""))
		{
			if (localNumber != null) {
				if(localNumber.getCountryCode().equals(ReverseLookup.GERMANY_CODE))
				{
					googleLink += HTMLUtil.stripEntities(ReverseLookupGermany.getCity(localNumber.getAreaNumber()))+", ";
				}
				if(localNumber.getCountryCode().equals(ReverseLookup.AUSTRIA_CODE))
				{
					googleLink += HTMLUtil.stripEntities(ReverseLookupAustria.getCity(localNumber.getAreaNumber()))+", ";
				}
				if(localNumber.getCountryCode().equals(ReverseLookup.USA_CODE))
				{
					googleLink += HTMLUtil.stripEntities(ReverseLookupUnitedStates.getCity(localNumber.getAreaNumber()))+", ";
				}
				if(localNumber.getCountryCode().equals(ReverseLookup.TURKEY_CODE))
				{
					googleLink += HTMLUtil.stripEntities(ReverseLookupTurkey.getCity(localNumber.getAreaNumber()))+", ";
				}
			}
		}

		googleLink += HTMLUtil.stripEntities(city);
		if (localNumber != null) {
			googleLink += HTMLUtil.stripEntities(", " + localNumber.getCountry());
		}
		try {
			googleLink = URLEncoder.encode(googleLink, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			Debug.error("Unsupported encoding in getGoogleLink: " + e.toString());
		}
		return googlePrefix+googleLink;
	}

	public int getNumFilledFields() {
		int numFilled = 0;
		if (!"".equals(this.city.trim())) {
			numFilled++;
		}
		if (!"".equals(this.company.trim())) {
			numFilled++;
		}
		if (!"".equals(this.emailAddress.trim())) {
			numFilled++;
		}
		if (!"".equals(this.firstName.trim())) {
			numFilled++;
		}
		if (!"".equals(this.lastName.trim())) {
			numFilled++;
		}
		if (!"".equals(this.lookupSite.trim())) {
			numFilled++;
		}
		if (!"".equals(this.pictureUrl.trim())) {
			numFilled++;
		}
		if (!"".equals(this.postalCode.trim())) {
			numFilled++;
		}
		if (!"".equals(this.street.trim())) {
			numFilled++;
		}
		return numFilled;
	}
}
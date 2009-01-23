package de.moonflower.jfritz.struct;

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 *
 */
public class Person implements Cloneable, Serializable{

	private static final long serialVersionUID = 104;

	private static final int scaleWidth = 40;

	private static final int scaleHeight = 50;

	private boolean privateEntry = false;

	private String firstName = ""; //$NON-NLS-1$

	private String lastName = ""; //$NON-NLS-1$

	private String company = ""; //$NON-NLS-1$

	private String street = ""; //$NON-NLS-1$

	private String postalCode = ""; //$NON-NLS-1$

	private String city = ""; //$NON-NLS-1$

	private String standard = ""; //$NON-NLS-1$

	private String emailAddress = ""; //$NON-NLS-1$

	private Vector<PhoneNumber> numbers;

	private Call lastCall; //TODO lastCall aktualisiern beim löschen von calls

	private String pictureUrl = ""; //$NON-NLS-1$

	private Image scaledPicture = null;

	private String[] basicTypes = {"home", "mobile", "homezone", "business", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$
			"other", "fax", "sip", "main"}; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$


	public Person() {
		numbers = new Vector<PhoneNumber>();
	}

	/**
	 * this function is needed to override the basic function equals
	 * inherited by object or else the network code won't work at all
	 *
	 * @author brian
	 *
	 */
	public boolean equalsOld(Object p){
		boolean equals = false;
		Person person;
		if(!(p instanceof Person))
			return false;

		person = (Person) p;

		//Just use csv to compare for now, if it doesnt work then write one by hand
		if(person.toCSV().equals(this.toCSV()))
		{
			equals = true;
		}
		else
		{
			equals = false;
		}

		if (equals && person.getStandardTelephoneNumber()!=null && this.getStandardTelephoneNumber()!=null)
		{
			equals = person.getStandardTelephoneNumber().equals(this.getStandardTelephoneNumber());
		}

		if (equals)
		{
			equals = person.getStandard().equals(this.getStandard());
		}

		if (equals)
		{
			if ((person.getPictureUrl() != null)
			   && (this.getPictureUrl() != null))
			   {
					equals = (person.getPictureUrl().equals(this.getPictureUrl()));
			   }
		}
		return equals;
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

		if (person.lastName.equals(this.lastName))
			if (person.firstName.equals(this.firstName))
				if (person.numbers.size() == this.numbers.size())
					if (person.standard.equals(this.standard))
				if (person.city.equals(this.city))
				if (person.street.equals(this.street))
				if (person.postalCode.equals(this.postalCode))
				if ((person.privateEntry == this.privateEntry))
				if (person.pictureUrl.equals(this.pictureUrl))
				if (person.company.equals(this.company))
				if (person.emailAddress.equals(this.emailAddress))
		{
			for (int i=0; i<this.numbers.size(); i++)
			{
				PhoneNumber myNumber = this.numbers.get(i);
				PhoneNumber hisNumber = person.numbers.get(i);
				if (!myNumber.equals(hisNumber))
				{
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public Person(String firstName, String company, String lastName,
			String street, String postalCode, String city, String eMail,
			String pictureUrl) {
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

	public Person(String firstName, String lastName) {
		this();

		this.firstName = firstName;
		this.lastName = lastName;
		this.privateEntry = false;
	}

	private void copyFrom(Person person) {
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
		Enumeration<PhoneNumber> en = person.getNumbers().elements();
		while (en.hasMoreElements()) {
			numbers.add(en.nextElement().clone());
		}
		privateEntry = person.isPrivateEntry();
	}

	public void addNumber(PhoneNumber number) {
		numbers.add(number);
		if (numbers.size() == 1)
			setStandard(number.getType());
	}

	public void addNumber(String number, String type) {
		PhoneNumber pn = new PhoneNumber(number, false);
		pn.setType(type);
		addNumber(pn);
	}

	public Vector<PhoneNumber> getNumbers() {
		return numbers;
	}

	public void setNumbers(Vector<PhoneNumber> numbers, String std) {
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
		} else if (lastName == null)
			ret = firstName;
		else if (firstName == null)
			ret = lastName;
		else if (lastName.length() == 0 && firstName.length() > 0) {
			ret = firstName;
		} else if (firstName.length() == 0) {
			ret = lastName;
		} else
			ret = (lastName + ", " + firstName).trim(); //$NON-NLS-1$
		if ((company != null) && (company.length() > 0)) {
			if (ret.length() > 0)
				ret += " (" + company + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
			else
				ret = company;
		}
		return ret;
	}

	// TODO Privat & Geschäftlich durch Konstanten ersetzen
	// private String[] basicTypes = { "home", "mobile", "homezone",
	// "business", "other", "fax", "sip" };
	// TODO Sonstiges und Nichtgefundenes
	// TODO sip != Pager, korrigieren
	public String toVCard() {
		String vcard = "";  //$NON-NLS-1$
		vcard = "BEGIN:vCard\n" //$NON-NLS-1$
				+ "VERSION:2.1\n" //$NON-NLS-1$
				+ "FN: " + getFullname() //$NON-NLS-1$
				+ "\n" + "ADR;Type=HOME,POSTAL:;;" + getStreet() + ";" //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				+ getCity() + ";;" + getPostalCode() + "\n"; //$NON-NLS-1$,  //$NON-NLS-2$
		Enumeration<PhoneNumber> en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = en.nextElement();
			if (n.getType().startsWith("home")) //$NON-NLS-1$
				vcard = vcard + "TEL;TYPE=VOICE,HOME:"; //$NON-NLS-1$
			else if (n.getType().startsWith("business")) //$NON-NLS-1$
				vcard = vcard + "TEL;TYPE=VOICE,WORK:"; //$NON-NLS-1$
			else if (n.getType().startsWith("mobile")) //$NON-NLS-1$
				vcard = vcard + "TEL;CELL:"; //$NON-NLS-1$
			else if (n.getType().startsWith("sip")) //$NON-NLS-1$
				vcard = vcard + "TEL;PAGER:"; //$NON-NLS-1$
			else if (n.getType().startsWith("fax")) //$NON-NLS-1$
				vcard = vcard + "TEL;FAX:"; //$NON-NLS-1$
			else
				vcard = vcard + "TEL;DIVERS:"; //$NON-NLS-1$
			vcard = vcard + n.convertToIntNumber() + "\n"; //$NON-NLS-1$
		}
		vcard = vcard + "EMAIL;TYPE=INTERNET,PREF:" + getEmailAddress() + "\n" //$NON-NLS-1$, //$NON-NLS-2$
				+ "END:vCard\n"; //$NON-NLS-1$
		return vcard;
	}

	/**
	 * Saves vcard to file
	 *
	 * @param file
	 */
	public void saveToVCard(File file) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(fos);
			pw.println(toVCard());
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + file.getName() + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	public String getFirstName() {
		if (firstName == null)
			return ""; //$NON-NLS-1$
		return firstName;
	}

	public String getCompany() {
		if (company == null)
			return ""; //$NON-NLS-1$
		return company;
	}

	public String getLastName() {
		if (lastName == null)
			return ""; //$NON-NLS-1$
		return lastName;
	}

	public String getStreet() {
		if (street == null)
			return ""; //$NON-NLS-1$
		return street;
	}

	public String getPostalCode() {
		if (postalCode == null)
			return ""; //$NON-NLS-1$
		return postalCode;
	}

	public String getCity() {
		if (city == null)
			return ""; //$NON-NLS-1$
		return city;
	}

	public String getPictureUrl() {
		if (pictureUrl == null)
			return ""; //$NON-NLS-1$
		return pictureUrl;
	}

	public Image getScaledPicture() {
		return scaledPicture;
	}

	public PhoneNumber getPhoneNumber(String type) {
		Enumeration<PhoneNumber> en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = en.nextElement();
			if (n.getType().equals(type))
				return n;
		}
		return null;
	}

	/**
	 * @return Returns the standard PhoneNumber
	 */
	public PhoneNumber getStandardTelephoneNumber() {
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
	public boolean hasNumber(String number, boolean considerMain) {
		Enumeration<PhoneNumber> en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = en.nextElement();
			if ((n.getType().startsWith("main")) && (considerMain)) { //$NON-NLS-1$
				// starts with ...
				if (number.startsWith(n.getIntNumber()))
					return true;
			} else { // equal number
				if (number.equals(n.getIntNumber()))
					return true;
			}
		}
		return false;
	}

	public String getEmailAddress() {
		if (emailAddress == null)
			return ""; //$NON-NLS-1$
		return emailAddress;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setCompany(String middleName) {
		this.company = middleName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setPostalCode(String postCode) {
		this.postalCode = postCode;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setEmailAddress(String email) {
		this.emailAddress = email;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
		updateScaledPicture();
	}

	/**
	 * @return Returns the standard Number.
	 */
	public final String getStandard() {
		if (standard == null)
			return ""; //$NON-NLS-1$
		return standard;
	}

	/**
	 * @param standard
	 *            Sets standard number
	 */
	public final void setStandard(String standard) {
		if  (getPhoneNumber(standard) == null) {
			// do not set a type as standard, if the number does not exist
			return;
		}
		this.standard = standard;
	}

	public void setPrivateEntry(boolean b) {
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
	public String getAddress(String lineSeparator, String wordSeparator) {

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
			outString = outString.concat(separator+"\"\""); //$NON-NLS-1$

		else
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
			outString = outString.concat("\""); //$NON-NLS-1$

		else
			for (PhoneNumber number: numbers)
				outString = outString + number.toCSV()+":";

			outString = outString.substring(0, outString.length()-1);
			outString = outString+"\"";

		return outString;
	}


	/**
	 * @author: haeusler DATE: 02.04.06, added by Brian This is part of a fix
	 *          for the null pointer exceptions that are caused by adding a
	 *          contact when a filter is set
	 */
	public boolean matchesKeyword(String s) {
		if (s == null || s.equals("")) {//$NON-NLS-1$
			return true;
		}
		if (getFullname().toLowerCase().indexOf(s.toLowerCase()) != -1) {
			return true;
		}
		Enumeration<PhoneNumber> en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = en.nextElement();

            if ( s.startsWith("+")) {
                if (n.getIntNumber().indexOf(s) != -1) {
                    return true;
                }
            } else if (n.getAreaNumber().indexOf(s) != -1) {
				return true;
			}
		}
		if (getAddress().toLowerCase().indexOf(s.toLowerCase()) != -1) {
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
	public boolean supersedes(Person p) {
		// Person data is checked
		if (firstName.indexOf(p.firstName) == -1)
			return false;
		if (lastName.indexOf(p.lastName) == -1)
			return false;
		if (company.indexOf(p.company) == -1)
			return false;
		if (street.indexOf(p.street) == -1)
			return false;
		if (postalCode.indexOf(p.postalCode) == -1)
			return false;
		if (city.indexOf(p.city) == -1)
			return false;
		if (emailAddress.indexOf(p.emailAddress) == -1)
			return false;

		// Ensuring that this person has more numbers
		if (numbers.size() < p.numbers.size())
			return false;

		// Creating a set of this person's numbers
		Enumeration<PhoneNumber> ownNumberEnum = numbers.elements();
		Set<String> ownNumberSet = new HashSet<String>();
		while (ownNumberEnum.hasMoreElements()) {
			PhoneNumber n = ownNumberEnum.nextElement();
			ownNumberSet.add(n.getIntNumber());
		}

		// Checking whether this person's numbers are a real superset
		Enumeration<PhoneNumber> otherNumberEnum = p.numbers.elements();
		while (otherNumberEnum.hasMoreElements()) {
			PhoneNumber n = (PhoneNumber) otherNumberEnum.nextElement();
			if (! ownNumberSet.contains(n.getIntNumber()))
				return false;
		}

		return true;
	}

	public Call getLastCall() {
		return lastCall;
	}

	public void setLastCall(Call lastCall) {
		this.lastCall = lastCall;
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
		ImageIcon pictureIcon = new ImageIcon(pictureUrl);

		// if we don't find the image, display the default one
		if (pictureIcon.getIconWidth() == -1 || pictureIcon.getIconHeight() == -1)
		{
			pictureIcon = new ImageIcon("");
		}
		float pictureWidthFactor = (float)pictureIcon.getIconWidth() / (float)scaleWidth;
		float pictureHeightFactor = (float)pictureIcon.getIconHeight() / (float)scaleHeight;

		int scaleToWidth = 0;
		int scaleToHeight = 0;
		if ( pictureWidthFactor > pictureHeightFactor )
		{
			scaleToWidth = (int)((float)pictureIcon.getIconWidth() / pictureWidthFactor);
			scaleToHeight = (int)((float)pictureIcon.getIconHeight() / pictureWidthFactor);
		}
		else
		{
			scaleToWidth = (int)((float)pictureIcon.getIconWidth() / pictureHeightFactor);
			scaleToHeight = (int)((float)pictureIcon.getIconHeight() / pictureHeightFactor);
		}

		this.scaledPicture = pictureIcon.getImage().getScaledInstance(scaleToWidth, scaleToHeight, Image.SCALE_SMOOTH);
	}
}
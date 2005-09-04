package de.moonflower.jfritz.struct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 *
 */
public class Person {

	private boolean privateEntry = false;

	private String firstName = "";

	private String lastName = "";

	private String company = "";

	private String street = "";

	private String postalCode = "";

	private String city = "";

	private String standard = "";

	private String emailAddress = "";

	private Vector numbers;

	public Person() {
		numbers = new Vector();
	}

	public Person(String firstName, String company, String lastName,
			String street, String postalCode, String city, String eMail) {
		this();

		this.firstName = firstName;
		this.company = company;
		this.lastName = lastName;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
		this.emailAddress = eMail;
		this.privateEntry = false;

	}

	public Person(String firstName, String lastName) {
		this();

		this.firstName = firstName;
		this.lastName = lastName;
		this.privateEntry = false;
	}

	public Person(Person person) {
		this();
		copyFrom(person);
	}

	public void copyFrom(Person person) {
		firstName = person.getFirstName();
		company = person.getCompany();
		lastName = person.getLastName();
		street = person.getStreet();
		postalCode = person.getPostalCode();
		city = person.getCity();
		emailAddress = person.getEmailAddress();
		standard = person.getStandard();
		numbers.clear();
		Enumeration en = person.getNumbers().elements();
		while (en.hasMoreElements()) {
			numbers.add(en.nextElement());
		}
		privateEntry = person.isPrivateEntry();
	}

	public void addNumber(PhoneNumber number) {
		numbers.add(number);
		if (numbers.size() == 1)
			setStandard(number.getType());
	}

	public void addNumber(String number, String type) {
		addNumber(new PhoneNumber(number, type));
	}

	public Vector getNumbers() {
		return numbers;
	}

	public void setNumbers(Vector numbers, String std) {
		this.numbers = numbers;
		this.standard = std;
	}

	public boolean isEmpty() {
		return getFullname().equals("") && numbers.size() < 2;
	}

	public String getFullname() {
		String ret;
		if ((lastName == null) && (firstName == null)) {
			ret = "";
		} else if (lastName == null)
			ret = firstName;
		else if (firstName == null)
			ret = lastName;
		else if (lastName.length() == 0 && firstName.length() > 0) {
			ret = firstName;
		} else if (firstName.length() == 0) {
			ret = lastName;
		} else
			ret = (lastName + ", " + firstName).trim();
		if ((company != null) && (company.length() > 0)) {
			if (ret.length() > 0)
				ret += " (" + company + ")";
			else
				ret = company;
		}
		return ret;
	}

	//TODO Privat & Geschäftlich durch Konstanten ersetzen
	//private String[] basicTypes = { "home", "mobile", "homezone",
		//	"business", "other", "fax", "sip" };
	//TODO Sonstiges und Nichtgefundenes
	//TODO sip != Pager, korrigieren
	public String toVCard() {
		String vcard = "";
		vcard = "BEGIN:vCard\n" +
				"VERSION:2.1\n" +
				"FN: " + getFullname()+ "\n" +
				"ADR;Type=HOME,POSTAL:;;" + getStreet() + ";" + getCity() + ";;" +getPostalCode()+"\n";
		Enumeration en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = (PhoneNumber) en.nextElement();
			if (n.getType().startsWith("home"))
				vcard = vcard + "TEL;TYPE=VOICE,HOME:";
			else if (n.getType().startsWith("business"))
				vcard = vcard + "TEL;TYPE=VOICE,WORK:";
			else if (n.getType().startsWith("mobile"))
				vcard = vcard + "TEL;CELL:";
			else if (n.getType().startsWith("sip"))
				vcard = vcard + "TEL;PAGER:";
			else if (n.getType().startsWith("fax"))
				vcard = vcard + "TEL;FAX:";
			else vcard = vcard + "TEL;DIVERS:";
			vcard = vcard + n.convertToIntNumber() + "\n";
		}
		vcard = vcard +
		"EMAIL;TYPE=INTERNET,PREF:" + getEmailAddress() + "\n" +
		"END:vCard\n";
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
			Debug.err("Could not write " + file.getName() + "!");
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public String getCompany() {
		return company;
	}

	public String getLastName() {
		return lastName;
	}

	public String getStreet() {
		return street;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCity() {
		return city;
	}

	public PhoneNumber getPhoneNumber(String type) {
		Enumeration en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = (PhoneNumber) en.nextElement();
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
	 * @return True if person has a phone number
	 */
	public boolean hasNumber(String number) {
		Enumeration en = numbers.elements();
		while (en.hasMoreElements()) {
			PhoneNumber n = (PhoneNumber) en.nextElement();
			if (number.equals(n.getIntNumber()))
				return true;
		}
		return false;
	}

	public String getEmailAddress() {
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

	/**
	 * @return Returns the standard.
	 */
	public final String getStandard() {
		return standard;
	}

	/**
	 * @param standard
	 *            Sets standard number
	 */
	public final void setStandard(String standard) {
		this.standard = standard;
	}

	public void setPrivateEntry(boolean b) {
		privateEntry = b;
	}

	public boolean isPrivateEntry() {
		return privateEntry;
	}
}

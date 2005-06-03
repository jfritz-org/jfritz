package de.moonflower.jfritz.struct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 *
 */
public class Person {

	private String firstName;

	private String lastName;

	private String middleName;

	private String street;

	private String postalCode;

	private String city;

	private PhoneNumber homeTelephoneNumber;

	private PhoneNumber mobileTelephoneNumber;

	private PhoneNumber businessTelephoneNumber;

	private PhoneNumber otherTelephoneNumber;

	private PhoneNumber standardTelephoneNumber;

	private String emailAddress;

	private String category;

	public Person(String firstName, String middleName, String lastName,
			String street, String postalCode, String city,
			String homeTelephoneNumber, String mobileTelephoneNumber,
			String businessTelephoneNumber, String otherTelephoneNumber,
			String standardTelephoneNumber, String emailAddress, String category) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
		this.homeTelephoneNumber = new PhoneNumber(homeTelephoneNumber);
		this.mobileTelephoneNumber = new PhoneNumber(mobileTelephoneNumber);
		this.businessTelephoneNumber = new PhoneNumber(businessTelephoneNumber);
		this.otherTelephoneNumber = new PhoneNumber(otherTelephoneNumber);
		this.standardTelephoneNumber = new PhoneNumber(standardTelephoneNumber);
		this.emailAddress = emailAddress;
		this.category = category;
	}

	public Person(String number) {
		this("?", "", "?", "", "", "", number, "", "", "", number, "", "");
	}

	public Person(String firstName, String middleName, String lastName) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
	}

	public String getFullname() {
		return (lastName + ", " + firstName + " " + middleName).trim();
	}

	public String toVCard() {
		String vcard = "";
		vcard = "BEGIN:vCard\n" + "VERSION:3.0\n" + "FN: " + getFullname() + "\n"
				+ "TEL;TYPE=VOICE,MSG,WORK:" + getStandardTelephoneNumber() + "\n" + "END:vCard\n";
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

	public PhoneNumber[] getNumbers() {
		return new PhoneNumber[] { homeTelephoneNumber, mobileTelephoneNumber,
				businessTelephoneNumber, otherTelephoneNumber };
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleName() {
		return middleName;
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

	public PhoneNumber getHomeTelNumber() {
		return homeTelephoneNumber;
	}

	public PhoneNumber getMobileTelNumber() {
		return mobileTelephoneNumber;
	}

	public PhoneNumber getBusinessTelNumber() {
		return businessTelephoneNumber;
	}

	public PhoneNumber getOtherTelNumber() {
		return otherTelephoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getCategory() {
		return category;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
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

	public void setHomeTelNumber(String telNumber) {
		this.homeTelephoneNumber = new PhoneNumber(telNumber);
	}

	public void setMobileTelNumber(String telNumber) {
		this.mobileTelephoneNumber = new PhoneNumber(telNumber);
	}

	public void setBusinessTelNumber(String telNumber) {
		this.businessTelephoneNumber = new PhoneNumber(telNumber);
	}

	public void setOtherTelNumber(String telNumber) {
		this.otherTelephoneNumber = new PhoneNumber(telNumber);
	}

	public void setEmailAddress(String email) {
		this.emailAddress = email;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return Returns the standardTelephoneNumber.
	 */
	public PhoneNumber getStandardTelephoneNumber() {
		return standardTelephoneNumber;
	}

	/**
	 * @param standardTelephoneNumber
	 *            The standardTelephoneNumber to set.
	 */
	public void setStandardTelephoneNumber(String standardTelephoneNumber) {
		this.standardTelephoneNumber = new PhoneNumber(standardTelephoneNumber);
	}

	/**
	 * Checks if person has telephone number number
	 *
	 * @param number
	 * @return True if person has a phone number
	 */
	public boolean hasNumber(String number) {
		if (number.equals(homeTelephoneNumber))
			return true;
		else if (number.equals(mobileTelephoneNumber))
			return true;
		else if (number.equals(businessTelephoneNumber))
			return true;
		else if (number.equals(otherTelephoneNumber))
			return true;
		else
			return false;
	}
}

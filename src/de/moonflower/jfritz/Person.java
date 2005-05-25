package de.moonflower.jfritz;

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

	private String homeTelephoneNumber;

	private String mobileTelephoneNumber;

	private String businessTelephoneNumber;

	private String otherTelephoneNumber;

	private String emailAddress;

	private String category;

	public Person(String firstName, String middleName, String lastName,
			String street, String postalCode, String city,
			String homeTelephoneNumber, String mobileTelephoneNumber,
			String businessTelephoneNumber, String otherTelephoneNumber,
			String emailAddress, String category) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
		this.homeTelephoneNumber = homeTelephoneNumber;
		this.mobileTelephoneNumber = mobileTelephoneNumber;
		this.businessTelephoneNumber = businessTelephoneNumber;
		this.otherTelephoneNumber = otherTelephoneNumber;
		this.emailAddress = emailAddress;
		this.category = category;
	}

	public Person(String firstName, String middleName, String lastName) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
	}

	public String getFullname() {
		return firstName + middleName + lastName;
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

	public String getHomeTelNumber() {
		return homeTelephoneNumber;
	}

	public String getMobileTelNumber() {
		return mobileTelephoneNumber;
	}

	public String getBusinessTelNumber() {
		return businessTelephoneNumber;
	}

	public String getOtherTelNumber() {
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
		this.homeTelephoneNumber = telNumber;
	}

	public void setMobileTelNumber(String telNumber) {
		this.mobileTelephoneNumber = telNumber;
	}

	public void setBusinessTelNumber(String telNumber) {
		this.businessTelephoneNumber = telNumber;
	}

	public void setOtherTelNumber(String telNumber) {
		this.otherTelephoneNumber = telNumber;
	}

	public void setEmailAddress(String email) {
		this.emailAddress = email;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}

package de.moonflower.jfritz.utils.reverselookup;

public class ParseResult {

	private ParseItem firstName;
	private ParseItem lastName;
	private ParseItem company;
	private ParseItem street;
	private ParseItem streetNumber;
	private ParseItem zipCode;
	private ParseItem city;
	public ParseItem getFirstName() {
		return firstName;
	}
	public void setFirstName(ParseItem firstName) {
		this.firstName = firstName;
	}
	public ParseItem getLastName() {
		return lastName;
	}
	public void setLastName(ParseItem lastName) {
		this.lastName = lastName;
	}
	public ParseItem getCompany() {
		return company;
	}
	public void setCompany(ParseItem company) {
		this.company = company;
	}
	public ParseItem getStreet() {
		return street;
	}
	public void setStreet(ParseItem street) {
		this.street = street;
	}
	public ParseItem getStreetNumber() {
		return streetNumber;
	}
	public void setStreetNumber(ParseItem streetNumber) {
		this.streetNumber = streetNumber;
	}
	public ParseItem getZipCode() {
		return zipCode;
	}
	public void setZipCode(ParseItem zipCode) {
		this.zipCode = zipCode;
	}
	public ParseItem getCity() {
		return city;
	}
	public void setCity(ParseItem city) {
		this.city = city;
	}

}

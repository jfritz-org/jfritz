package de.moonflower.jfritz.struct;

public class PhoneNumber {
	private String countryCode;
	private String areaCode;
	private String number;

	private String callByCall;

	private String country;
	private String description;
	private String flagPath;

	private String city;

	private boolean localCall;
	private boolean internationalCall;

	public PhoneNumber()
	{
		countryCode = "";
		areaCode = "";
		number = "";

		callByCall = "";

		country = "";
		description = "";
		flagPath = "";

		city = "";

		localCall = false;
		internationalCall = false;
	}

	public String getInternationalNumber() {
		return "+"+countryCode+areaCode+number;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCallByCall() {
		return callByCall;
	}

	public void setCallByCall(String callByCall) {
		this.callByCall = callByCall;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFlagPath() {
		return flagPath;
	}

	public void setFlagPath(String flagPath) {
		this.flagPath = flagPath;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(final String city)
	{
		this.city = city;
	}

	public boolean isLocalCall()
	{
		return localCall;
	}

	public void setLocalCall(final boolean call)
	{
		this.localCall = call;
	}

	public boolean isInternationalCall()
	{
		return internationalCall;
	}

	public void setInternationalCall(final boolean call)
	{
		this.internationalCall = call;
	}
}

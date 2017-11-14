package de.moonflower.jfritz.numbers;

public class NumberRefactoring {

	private static final String INTERNATIONAL_PREFIX = "+";
	private static final int MIN_NUMBER_LENGTH = 3;

	private static final String SIP_PROVIDER_PREFIX_PURETEL = "00038";
	private static final String SIP_PROVIDER_PREFIX_SIPGATE = "555";
	private static final String SIP_PROVIDER_PREFIX_SIPGATE_2 = "777";

	private static final String GERMANY_POLICE = "110";
	private static final String GERMANY_MEDICAL = "112";
	private static final String GERMANY_CREDIT_CARD = "116116";
	private static final String SWITZERLAND_MEDICAL = "144";

	private String mCountryPrefix = null;
	private String mCountryCode = null;
	private String mAreaPrefix = null;
	private String mAreaCode = null;

	private String mInputNumber = null;

	public NumberRefactoring() {

	}

	public void setCountryPrefix(final String prefix) {
		mCountryPrefix = prefix;
	}

	public void setCountryCode(final String countryCode) {
		mCountryCode = countryCode;
	}

	public void setAreaPrefix(final String areaPrefix) {
		mAreaPrefix = areaPrefix;
	}

	public void setAreaCode(final String areaCode) {
		mAreaCode = areaCode;
	}

	public String convertToIntNumber(final String inputNumber) {
		mInputNumber = inputNumber;
		if (isNumberTooShort()
				|| isInternationalNumber()
				|| isSIPNumber()
				|| isEmergencyCall()
				|| isQuickDial()) {
			return mInputNumber;
		}

		if (numberStartsWithCountryPrefix()) {
			return replaceCountryPrefixWithInternationalPrefix();
		}

		if (numberStartsWithAreaPrefix()) {
			return replaceAreaPrefixWithCountryCode();
		}

		return prependCountryCodeAndArea();
	}

	private boolean isNumberTooShort() {
		return (mInputNumber.length() < MIN_NUMBER_LENGTH);
	}

	private boolean isInternationalNumber() {
		return mInputNumber.startsWith(INTERNATIONAL_PREFIX);
	}

	public boolean isSIPNumber() {
		return ((mInputNumber.indexOf('@') > 0) //$NON-NLS-1$
				|| mInputNumber.startsWith(SIP_PROVIDER_PREFIX_PURETEL)
				|| mInputNumber.startsWith(SIP_PROVIDER_PREFIX_SIPGATE)
				|| mInputNumber.startsWith(SIP_PROVIDER_PREFIX_SIPGATE_2)
		);
	}

	// FIXME: special numbers verwenden, je nach Provider!!!
	private boolean isEmergencyCall() {
		if (mInputNumber.equals(GERMANY_POLICE))
			return true;
		else if (mInputNumber.equals(GERMANY_MEDICAL))
			return true;
		else if (mInputNumber.equals(GERMANY_CREDIT_CARD))
			return true;
		else if (mInputNumber.equals(SWITZERLAND_MEDICAL))
			return true;
		return false;
	}

	private boolean isQuickDial() {
		if (mInputNumber.startsWith("**7") || mInputNumber.length() < 3) { //$NON-NLS-1$
			return true;
		} else {
			return false;
		}
	}

	private boolean numberStartsWithCountryPrefix() {
		return mCountryPrefix != null && mInputNumber.startsWith(mCountryPrefix);
	}

	private String replaceCountryPrefixWithInternationalPrefix() {
		return INTERNATIONAL_PREFIX + mInputNumber.substring(mCountryPrefix.length());
	}

	private boolean numberStartsWithAreaPrefix() {
		return mAreaPrefix != null && mInputNumber.startsWith(mAreaPrefix);
	}

	private String replaceAreaPrefixWithCountryCode() {
		return mCountryCode + mInputNumber.substring(mAreaPrefix.length());
	}

	private String prependCountryCodeAndArea() {
		StringBuilder result = new StringBuilder();

		if (mCountryCode != null) {
			result.append(mCountryCode);
		}

		if (mAreaCode != null) {
			result.append(mAreaCode);
		}

		result.append(mInputNumber);

		return result.toString();
	}
}

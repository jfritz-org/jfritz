/*
 *
 * Created on 16.05.2005
 *
 */
package de.moonflower.jfritz;

/**
 * @author rob
 *
 */
public class SipProvider {
	private int providerID;

	private String providerName, phoneNumber;

	public SipProvider(int providerID, String phoneNumber, String providerName) {
		this.providerID = providerID;
		this.providerName = providerName;
		this.phoneNumber = phoneNumber;
	}

	public final String getNumber() {
		return phoneNumber;
	}

	public final String getName() {
		return providerName;
	}

	public String toString() {
		return "SIP" + providerID + ": " + phoneNumber + "@" + providerName;
	}

}

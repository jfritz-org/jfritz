/*
 *
 * Created on 16.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

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

	/**
	 * @return Returns phone number
	 */
	public final String getNumber() {
		return phoneNumber;
	}

	/**
	 * @return Returns name of sip-provider or IP
	 */
	public final String getProvider() {
		return providerName;
	}

	public String toString() {
		return phoneNumber + "@" + providerName;
	}

	/**
	 * @return Returns the providerID.
	 */
	public final int getProviderID() {
		return providerID;
	}
}

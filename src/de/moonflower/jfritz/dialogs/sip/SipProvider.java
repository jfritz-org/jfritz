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

	private boolean active;

	private String providerName, phoneNumber;

	public SipProvider(int providerID, String phoneNumber, String providerName) {
		this.providerID = providerID;
		this.providerName = providerName;
		this.phoneNumber = phoneNumber;
	}

	public SipProvider(int providerID, String phoneNumber, String providerName, boolean active) {
		this.providerID = providerID;
		this.providerName = providerName;
		this.phoneNumber = phoneNumber;
		this.active = active;
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

	public final void setActive(boolean state) {
	    active = state;
	}

	public final boolean isActive() {
	    return active;
	}

	/**
	 * @return Returns XML String
	 */
	public String toXML() {
		String sep = System.getProperty("line.separator", "\n");
		String output = "";
		output = ("<entry id=\"" + providerID + "\">" + sep);
		output = output + ("\t<name>" + providerName + "</name>" + sep);
		output = output + ("\t<number>" + phoneNumber + "</number>" + sep);
		output = output + ("\t<active>" + active + "</active>" + sep);
		output = output + ("</entry>");
		return output;
	}
}

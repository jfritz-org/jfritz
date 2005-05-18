/*
 *
 * Created on 16.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.moonflower.jfritz;

/**
 * @author rob
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SipProvider {
	String providerName,phoneNumber;

	public SipProvider(String providerName, String phoneNumber) {
		this.providerName = providerName;
		this.phoneNumber = phoneNumber;
	}

	public final String getNumber() {
		return phoneNumber;
	}

	public final String getName() {
		return providerName;
	}


}

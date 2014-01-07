package de.moonflower.jfritz.box.fritzbox;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.moonflower.jfritz.dialogs.sip.SipProvider;

public class FritzBoxTests {

	private FritzBox fritzBox;

	@Before
	public void setUp() {
		fritzBox = new FritzBox("testbox", "FritzBox for tests", "http", "192.168.178.1", "80", false, "", "");
	}

	@Test
	public void getSipProviderBySipUri() {

		SipProvider sipProvider = fritzBox.getSipProviderByRoute("12345@abc.com");

		Assert.assertEquals("12345", sipProvider.getNumber());
		Assert.assertEquals("abc.com", sipProvider.getProvider());
		Assert.assertEquals(SipProvider.UNKNOWN_SIP_PROVIDER_ID, sipProvider.getProviderID());
		Assert.assertEquals("12345@abc.com", sipProvider.toString());
	}

}

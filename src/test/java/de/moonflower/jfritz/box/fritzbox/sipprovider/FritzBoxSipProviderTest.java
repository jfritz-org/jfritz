package de.moonflower.jfritz.box.fritzbox.sipprovider;

import java.util.Vector;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.dialogs.sip.SipProvider;

public class FritzBoxSipProviderTest {
	
	private FritzBoxSipProvider fbSipProvider;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		fbSipProvider = new FritzBoxSipProvider();
	}
	
	@After
	public void tearDown() {
		fbSipProvider = null;
	}

	@Test
	public void testParseResponse_NULL() {
		fbSipProvider.parseResponse(null);
		
		Assert.assertEquals(0, fbSipProvider.getSipProvider().size());
	}

	@Test
	public void testParseResponse_Empty() {
		Vector<String> response = new Vector<String>();
		response.add("");
		fbSipProvider.parseResponse(response);
		
		Assert.assertEquals(0, fbSipProvider.getSipProvider().size());
	}

	@Test
	public void testParseResponse_Error() {
		Vector<String> response = new Vector<String>();
		response.add("er");
		fbSipProvider.parseResponse(response);
		
		Assert.assertEquals(0, fbSipProvider.getSipProvider().size());
	}
	
	@Test
	public void testParseResponse_OneProvider_Active() {
		// mock query to MSN
		fbSipProvider.msnMap.put("sip0", "12345");
		Vector<String> response = generateTestData(new SipProvider(true, 0, "12345", "registrar"));

		fbSipProvider.parseResponse(response);

		Assert.assertEquals(1, fbSipProvider.getSipProvider().size());

		SipProvider sipProvider = fbSipProvider.getSipProvider(0);
		Assert.assertTrue(sipProvider.isActive());
		Assert.assertEquals(0, sipProvider.getProviderID());
		Assert.assertEquals("12345", sipProvider.getNumber());
		Assert.assertEquals("registrar", sipProvider.getProvider());
	}
	
	@Test
	public void testParseResponse_OneProvider_NotActive() {
		// mock query to MSN
		fbSipProvider.msnMap.put("sip0", "12345");
		Vector<String> response = generateTestData(new SipProvider(false, 0, "12345", "registrar"));

		fbSipProvider.parseResponse(response);

		Assert.assertEquals(1, fbSipProvider.getSipProvider().size());

		SipProvider sipProvider = fbSipProvider.getSipProvider(0);
		Assert.assertFalse(sipProvider.isActive());
		Assert.assertEquals(0, sipProvider.getProviderID());
		Assert.assertEquals("12345", sipProvider.getNumber());
		Assert.assertEquals("registrar", sipProvider.getProvider());
	}


	@Test
	public void testParseResponse_TwoProvider() {
		// mock query to MSN
		fbSipProvider.msnMap.put("sip0", "12345");
		fbSipProvider.msnMap.put("sip1", "98765");

		Vector<String> response = generateTestData(
				new SipProvider(true, 0, "12345", "registrar"),
				new SipProvider(true, 1, "98765", "otherregistrar"));
		fbSipProvider.parseResponse(response);

		Assert.assertEquals(2, fbSipProvider.getSipProvider().size());

		SipProvider sipProvider1 = fbSipProvider.getSipProvider(0);
		Assert.assertTrue(sipProvider1.isActive());
		Assert.assertEquals(0, sipProvider1.getProviderID());
		Assert.assertEquals("12345", sipProvider1.getNumber());
		Assert.assertEquals("registrar", sipProvider1.getProvider());

		SipProvider sipProvider2 = fbSipProvider.getSipProvider(1);
		Assert.assertTrue(sipProvider2.isActive());
		Assert.assertEquals(1, sipProvider2.getProviderID());
		Assert.assertEquals("98765", sipProvider2.getNumber());
		Assert.assertEquals("otherregistrar", sipProvider2.getProvider());
	}

	@Test
	public void testParseResponse_ThreeProvider() {
		// mock query to MSN
		fbSipProvider.msnMap.put("sip0", "12345");
		fbSipProvider.msnMap.put("sip1", "98765");
		fbSipProvider.msnMap.put("sip2", "45678");

		Vector<String> response = generateTestData(
				new SipProvider(true, 0, "12345", "registrar"),
				new SipProvider(true, 1, "98765", "otherregistrar"),
				new SipProvider(true, 2, "45678", "sip.1und1.de")
				);
		fbSipProvider.parseResponse(response);

		Assert.assertEquals(3, fbSipProvider.getSipProvider().size());

		SipProvider sipProvider1 = fbSipProvider.getSipProvider(0);
		Assert.assertTrue(sipProvider1.isActive());
		Assert.assertEquals(0, sipProvider1.getProviderID());
		Assert.assertEquals("12345", sipProvider1.getNumber());
		Assert.assertEquals("registrar", sipProvider1.getProvider());

		SipProvider sipProvider2 = fbSipProvider.getSipProvider(1);
		Assert.assertTrue(sipProvider2.isActive());
		Assert.assertEquals(1, sipProvider2.getProviderID());
		Assert.assertEquals("98765", sipProvider2.getNumber());
		Assert.assertEquals("otherregistrar", sipProvider2.getProvider());

		SipProvider sipProvider3 = fbSipProvider.getSipProvider(2);
		Assert.assertTrue(sipProvider3.isActive());
		Assert.assertEquals(2, sipProvider3.getProviderID());
		Assert.assertEquals("45678", sipProvider3.getNumber());
		Assert.assertEquals("sip.1und1.de", sipProvider3.getProvider());
	}

	@Test
	public void testParseResponse_TwoProvider_Missing_ID() {
		// mock query to MSN
		fbSipProvider.msnMap.put("sip0", "12345");
		fbSipProvider.msnMap.put("sip2", "45678");

		Vector<String> response = generateTestData(
				new SipProvider(true, 0, "12345", "registrar"),
				new SipProvider(true, 2, "45678", "sip.1und1.de")
				);
		fbSipProvider.parseResponse(response);

		Assert.assertEquals(2, fbSipProvider.getSipProvider().size());

		SipProvider sipProvider1 = fbSipProvider.getSipProvider(0);
		Assert.assertTrue(sipProvider1.isActive());
		Assert.assertEquals(0, sipProvider1.getProviderID());
		Assert.assertEquals("12345", sipProvider1.getNumber());
		Assert.assertEquals("registrar", sipProvider1.getProvider());

		SipProvider sipProvider2 = fbSipProvider.getSipProvider(2);
		Assert.assertTrue(sipProvider2.isActive());
		Assert.assertEquals(2, sipProvider2.getProviderID());
		Assert.assertEquals("45678", sipProvider2.getNumber());
		Assert.assertEquals("sip.1und1.de", sipProvider2.getProvider());
	}

	private Vector<String> generateTestData(SipProvider... sipProviders) {
		Vector<String> response = new Vector<String>();
		
		for (int i=0; i<sipProviders.length; i++) {
			SipProvider sipProvider = sipProviders[i];
			response.add(sipProvider.isActive() ? "1" : "0");
			response.add(Integer.toString(sipProvider.getProviderID()));
			response.add(sipProvider.getProvider());
			response.add(sipProvider.getNumber());
		}
		
		return response;
	}
}

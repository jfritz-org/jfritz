package de.moonflower.jfritz.box.fritzbox.callerlist;

import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.Port;

public class CallListCsvLineParserTests {

	@Mock private FritzBox mockedFritzBox;
	@Mock private MessageProvider mockedMessages;

	private CallListCsvLineParser parser;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		parser = new CallListCsvLineParser(";");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testNull() throws FeatureNotSupportedByFirmware {
		parser.parseLine(mockedFritzBox, null);
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testEmpty() throws FeatureNotSupportedByFirmware {
		parser.parseLine(mockedFritzBox, "");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testNoSeparators() throws FeatureNotSupportedByFirmware {
		parser.parseLine(mockedFritzBox, "0");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testTooFewColumns() throws FeatureNotSupportedByFirmware {
		parser.parseLine(mockedFritzBox, "1;2;3;4;5;6");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testTooMuchColumns() throws FeatureNotSupportedByFirmware {
		parser.parseLine(mockedFritzBox, "1;2;3;4;5;6;7;8");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void fritzBoxNull() throws FeatureNotSupportedByFirmware {
		parser.parseLine(null, "0;00.12.12 19:27;;07211234567;FRITZ!App Fon Nexus 10;Internet: 12345678;0:01");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void wrongCallType() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "0;06.12.12 19:27;;07211234567;FRITZ!App Fon Nexus 10;Internet: 12345678;0:01");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void wrongCallDate() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		parser.parseLine(mockedFritzBox, "0;00.12.12 19:27;;07211234567;FRITZ!App Fon Nexus 10;Internet: 12345678;0:01");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void missingCallTime() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		parser.parseLine(mockedFritzBox, "0;01.12.12;;07211234567;FRITZ!App Fon Nexus 10;Internet: 12345678;0:01");
	}

	@Test
	public void missedCall() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "2;25.12.12 17:45;;0123456789;;Internet: 12345678;0:00");

		// verify
		Assert.assertNotNull(call);
		assertDate(25, Calendar.DECEMBER, 2012, 17, 45, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN_FAILED, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals(0, call.getDuration());
		Assert.assertEquals("0123456789", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("", call.getPort().getName());
		Assert.assertEquals("12345678@mockedProvider", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	@Test
	public void missedCallEmpty() throws FeatureNotSupportedByFirmware {
		// preconditions
		when(mockedMessages.getMessage("fixed_network")).thenReturn("Festnetz");
		parser.messages = mockedMessages;

		// test
		Call call = parser.parseLine(mockedFritzBox, "2;25.12.12 17:45;;0123456789;;;0:00");

		// verify
		Assert.assertNotNull(call);
		assertDate(25, Calendar.DECEMBER, 2012, 17, 45, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN_FAILED, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals(0, call.getDuration());
		Assert.assertEquals("0123456789", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("", call.getPort().getName());
		Assert.assertEquals("Festnetz", call.getRoute());
		Assert.assertEquals(Call.ROUTE_FIXED_NETWORK, call.getRouteType());
	}


	@Test
	public void blockedCall() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "3;25.12.12 17:45;;0123456789;;Internet: 12345678;0:00");

		// verify
		Assert.assertNotNull(call);
		assertDate(25, Calendar.DECEMBER, 2012, 17, 45, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN_BLOCKED, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals(0, call.getDuration());
		Assert.assertEquals("0123456789", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("", call.getPort().getName());
		Assert.assertEquals("12345678@mockedProvider", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	private void assertDate(int day, int month, int year, int hour, int minute, Date actualDate) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal.getTime(), actualDate);
	}

	@Test
	public void outgoingCall() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "4;06.12.12 19:27;;07211234567;FRITZ!App Fon Nexus 10;Internet: 12345678;0:01");

		// verify
		Assert.assertNotNull(call);
		assertDate(6, Calendar.DECEMBER, 2012, 19, 27, call.getCalldate());
		Assert.assertEquals(CallType.CALLOUT, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals(60, call.getDuration());
		Assert.assertEquals("07211234567", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("FRITZ!App Fon Nexus 10", call.getPort().getName());
		Assert.assertEquals("12345678@mockedProvider", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	@Test
	public void incomingCall() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;Internet: 12345678;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("12345678@mockedProvider", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	@Test
	public void incomingCallEmpty() throws FeatureNotSupportedByFirmware {
		// preconditions
		when(mockedMessages.getMessage("fixed_network")).thenReturn("Festnetz");
		parser.messages = mockedMessages;

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("Festnetz", call.getRoute());
		Assert.assertEquals(Call.ROUTE_FIXED_NETWORK, call.getRouteType());
	}

	@Test
	public void callWithName() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;Name;072198765432;SIEMENS;Internet: 12345678;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("12345678@mockedProvider", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	@Test
	public void fixedPhoneNumber() throws FeatureNotSupportedByFirmware {
		// preconditions

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;12345678;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("12345678", call.getRoute());
		Assert.assertEquals(Call.ROUTE_FIXED_NETWORK, call.getRouteType());
	}

	@Test
	public void fixedEmptyPhoneNumber() throws FeatureNotSupportedByFirmware {
		// preconditions
		when(mockedMessages.getMessage("fixed_network")).thenReturn("Festnetz");
		parser.messages = mockedMessages;

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("Festnetz", call.getRoute());
		Assert.assertEquals(Call.ROUTE_FIXED_NETWORK, call.getRouteType());
	}

	@Test
	public void sipProviderNotFound() throws FeatureNotSupportedByFirmware {
		// preconditions
		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(null);
		when(mockedMessages.getMessage("unknown_sip_provider")).thenReturn("unknown");
		parser.messages = mockedMessages;

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;Internet: 12345678;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("12345678@unknown", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	@Test
	public void sipUri() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "abc.com");

		when(mockedFritzBox.getSipProviderByRoute("12345678@abc.com")).thenReturn(mockedSipProvider);

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;12345678@abc.com;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("SIEMENS", call.getPort().getName());
		Assert.assertEquals("12345678@abc.com", call.getRoute());
		Assert.assertEquals(Call.ROUTE_SIP, call.getRouteType());
	}

	@Test
	public void portKnown() throws FeatureNotSupportedByFirmware {
		// preconditions
		Port mockedPort = new Port(1, "Portname", "dialPort", "internal Number");
		when(mockedFritzBox.getConfiguredPort(1)).thenReturn(mockedPort);

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;1;12345678;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("Portname", call.getPort().getName());
		Assert.assertEquals("12345678", call.getRoute());
		Assert.assertEquals(Call.ROUTE_FIXED_NETWORK, call.getRouteType());
	}

	@Test
	public void portUnknown() throws FeatureNotSupportedByFirmware {
		// preconditions
		when(mockedFritzBox.getConfiguredPort(0)).thenReturn(null);

		// test
		Call call = parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;0;12345678;1:25");

		// verify
		Assert.assertNotNull(call);
		assertDate(2, Calendar.DECEMBER, 2012, 21, 55, call.getCalldate());
		Assert.assertEquals(CallType.CALLIN, call.getCalltype());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals((60+25)*60, call.getDuration());
		Assert.assertEquals("072198765432", call.getPhoneNumber().getIntNumber());
		Assert.assertEquals("Fon 1", call.getPort().getName());
		Assert.assertEquals("12345678", call.getRoute());
		Assert.assertEquals(Call.ROUTE_FIXED_NETWORK, call.getRouteType());
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void timeNoColon() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;Internet: 12345678;123");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void timeWrongHours() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;Internet: 12345678;a:05");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void timeWrongMinutes() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(0, "12345678", "mockedProvider");

		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		parser.parseLine(mockedFritzBox, "1;02.12.12 21:55;;072198765432;SIEMENS;Internet: 12345678;1:ab");
	}

}

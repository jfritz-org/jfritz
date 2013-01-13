package de.moonflower.jfritz.box.fritzbox.callerlist;

import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

public class CallListCsvParserTests {

	private static final String CSV_SEPARATOR = "sep=;\n";
	private static final String CSV_HEADER = "Typ;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer\n";
	private CallListCsvParser parser;
	@Mock private FritzBox mockedFritzBox;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		parser = new CallListCsvParser();
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testNull() throws FeatureNotSupportedByFirmware {
		// preconditions

		// test
		Vector<Call> calls = parser.parseCsvString(mockedFritzBox, null);
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testEmpty() throws FeatureNotSupportedByFirmware {
		// preconditions

		// test
		Vector<Call> calls = parser.parseCsvString(mockedFritzBox, "");
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testMissingSeparator() throws FeatureNotSupportedByFirmware {
		// preconditions
		StringBuilder sb = new StringBuilder();
		sb.append(CSV_HEADER);

		// test
		Vector<Call> calls = parser.parseCsvString(mockedFritzBox, sb.toString());
	}

	@Test(expected=FeatureNotSupportedByFirmware.class)
	public void testMissingHeader() throws FeatureNotSupportedByFirmware {
		// preconditions
		StringBuilder sb = new StringBuilder();
		sb.append(CSV_SEPARATOR);

		// test
		Vector<Call> calls = parser.parseCsvString(mockedFritzBox, sb.toString());
	}

	@Test
	public void testSeparatorAndHeader() throws FeatureNotSupportedByFirmware {
		// preconditions
		StringBuilder sb = new StringBuilder();
		sb.append(CSV_SEPARATOR);
		sb.append(CSV_HEADER);

		// test
		Vector<Call> calls = parser.parseCsvString(mockedFritzBox, sb.toString());

		// verify
		Assert.assertEquals(0, calls.size());
	}

	@Test
	public void testOneCall() throws FeatureNotSupportedByFirmware {
		// preconditions
		SipProvider mockedSipProvider = new SipProvider(99, "12345678", "mockedProvider");
		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		// test
		StringBuilder sb = new StringBuilder();
		sb.append(CSV_SEPARATOR);
		sb.append(CSV_HEADER);
		sb.append("2;25.12.12 17:45;;0123456789;;Internet: 12345678;0:07");
		Vector<Call> calls = parser.parseCsvString(mockedFritzBox, sb.toString());

		// verify
		Assert.assertEquals(1, calls.size());

		Call call = calls.get(0);
		Assert.assertEquals(CallType.CALLIN_FAILED, call.getCalltype());
		assertDate(25, Calendar.DECEMBER, 2012, 17, 45, call.getCalldate());
		Assert.assertEquals("", call.getComment());
		Assert.assertEquals(-1.0, call.getCost(), 0.5);
		Assert.assertEquals(7*60, call.getDuration());
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
}

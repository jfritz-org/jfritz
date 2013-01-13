package de.moonflower.jfritz.box.fritzbox.callerlist;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.box.fritzboxnew.HttpHelper;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.IProgressListener;

public class FritzBoxCallList_Actual_Tests {

	@Mock HttpHelper mockedHttp;
	@Mock FritzBox mockedFritzBox;
	@Mock IProgressListener mockedProgressListener;

	FritzBoxCallList_Actual callList;
	Vector<IProgressListener> allProgressListener = new Vector<IProgressListener>();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		allProgressListener.clear();
		allProgressListener.add(mockedProgressListener);

		callList = new FritzBoxCallList_Actual(mockedFritzBox, null);
		callList.httpHelper = mockedHttp;
	}

	@Test
	public void empty() throws MalformedURLException, IOException, FeatureNotSupportedByFirmware {
		// precondition
		initFritzBoxMock();
		String mockedResponse = initCsvResponseHeader();
		when(mockedHttp.getHttpContentAsString("http://fritz.box:80/fon_num/foncalls_list.lua?csv=&sid=82c73f70081dd373")).thenReturn(mockedResponse);

		// test
		Vector<Call> result = callList.getCallerList(allProgressListener);

		// verify
		Assert.assertEquals(0, result.size());
		verify(mockedProgressListener, times(1)).setMax(0);
		verify(mockedProgressListener, times(1)).setMin(0);
		verify(mockedProgressListener, times(1)).setProgress(0);
		verify(mockedProgressListener, times(1)).setMax(2);
		verify(mockedProgressListener, times(1)).setProgress(1);
		verify(mockedProgressListener, times(1)).setProgress(2);
	}

	private void initFritzBoxMock() throws UnsupportedEncodingException {
		when(mockedFritzBox.getUrlPrefix()).thenReturn("http://fritz.box:80");
		when(mockedFritzBox.appendSidOrPassword("http://fritz.box:80/fon_num/foncalls_list.lua?csv=")).thenReturn("http://fritz.box:80/fon_num/foncalls_list.lua?csv=&sid=82c73f70081dd373");
	}

	private String initCsvResponseHeader() {
		return "sep=;\nTyp;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer\n";
	}

	@Test
	public void one() throws MalformedURLException, IOException, FeatureNotSupportedByFirmware {
		// precondition
		initFritzBoxMock();
		SipProvider mockedSipProvider = new SipProvider(99, "12345678", "mockedProvider");
		when(mockedFritzBox.getSipProviderByRoute("12345678")).thenReturn(mockedSipProvider);

		String mockedResponse = initCsvResponseHeader();
		mockedResponse = addLineToResponse(mockedResponse, "2;25.12.12 17:45;;0123456789;;Internet: 12345678;0:07");
		when(mockedHttp.getHttpContentAsString("http://fritz.box:80/fon_num/foncalls_list.lua?csv=&sid=82c73f70081dd373")).thenReturn(mockedResponse);

		// test
		Vector<Call> result = callList.getCallerList(allProgressListener);

		// verify
		verify(mockedProgressListener, times(1)).setMax(0);
		verify(mockedProgressListener, times(1)).setMin(0);
		verify(mockedProgressListener, times(1)).setProgress(0);
		verify(mockedProgressListener, times(1)).setMax(3);
		verify(mockedProgressListener, times(1)).setProgress(1);
		verify(mockedProgressListener, times(1)).setProgress(2);

		Assert.assertEquals(1, result.size());
		verify(mockedProgressListener, times(1)).setProgress(3);
		Call call = result.get(0);
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

	private String addLineToResponse(String response, String newLine) {
		return response + newLine + "\n";
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
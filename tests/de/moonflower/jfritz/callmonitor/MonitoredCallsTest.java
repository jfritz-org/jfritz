package de.moonflower.jfritz.callmonitor;

import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import de.moonflower.jfritz.TestHelper;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

public class MonitoredCallsTest extends TestCase {

	@Mock private PropertyProvider mockedProperties;
	@Mock private CallMonitorListener mockedListener;

	MonitoredCalls monitoredCalls;

	@BeforeClass
	public static void setup() {
		TestHelper.initLogging();
		MessageProvider.getInstance().loadMessages(new Locale("de_DE"));
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		monitoredCalls = new MonitoredCalls();
		monitoredCalls.properties = mockedProperties;
		monitoredCalls.addCallMonitorListener(mockedListener);

		Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
		Assert.assertEquals(0, monitoredCalls.getPendingSize());
	}

	@Test
	public void testAddNewCall() {
		CallType callType = CallType.CALLIN;
		Call testCall = TestHelper.createTestCall(callType);
		int callId = 0;

		doReturn("").when(mockedProperties).getProperty("option.callmonitor.ignoreMSN");

		// add a new call
		monitoredCalls.addNewCall(callId, testCall);
		Assert.assertEquals(CallState.PENDING, monitoredCalls.getCallState(0));
		Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
		Assert.assertEquals(1, monitoredCalls.getPendingSize());
		Call checkCall = monitoredCalls.getCall(callId);
		TestHelper.assertTestCall(checkCall, callType);
		verify(this.mockedListener, times(1)).pendingCallIn(testCall);
		verify(this.mockedProperties, times(1)).getProperty("option.callmonitor.ignoreMSN");

		// establish call
		monitoredCalls.establishCall(callId);
		Assert.assertEquals(CallState.ESTABLISHED, monitoredCalls.getCallState(0));
		Assert.assertEquals(1, monitoredCalls.getEstablishedSize());
		Assert.assertEquals(0, monitoredCalls.getPendingSize());
		checkCall = monitoredCalls.getCall(callId);
		TestHelper.assertTestCall(checkCall, callType);
		verify(this.mockedListener, times(1)).establishedCallIn(testCall);

		// remove call
		monitoredCalls.removeCall(callId, testCall);
		Assert.assertEquals(CallState.NONE, monitoredCalls.getCallState(0));
		Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
		Assert.assertEquals(0, monitoredCalls.getPendingSize());
		checkCall = monitoredCalls.getCall(callId);
		Assert.assertNull(checkCall);
		verify(this.mockedListener, times(1)).endOfCall(testCall);

		verifyNoMoreInteractions(this.mockedListener);
		verifyNoMoreInteractions(this.mockedProperties);
	}
}

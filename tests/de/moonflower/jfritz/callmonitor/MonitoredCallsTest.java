package de.moonflower.jfritz.callmonitor;

import java.util.Locale;

import junit.framework.Assert;

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

public class MonitoredCallsTest {

	@Mock
	PropertyProvider mockedProperties;

	@Mock
	CallMonitorListener mockedListener;

	MonitoredCalls mc;

	@BeforeClass
	public static void setup() {
		TestHelper.initLogging();
		MessageProvider.getInstance().loadMessages(new Locale("de_DE"));
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		mc = new MonitoredCalls();
		mc.addCallMonitorListener(mockedListener);
		mc.properties = mockedProperties;

		Assert.assertEquals(0, mc.getEstablishedSize());
		Assert.assertEquals(0, mc.getPendingSize());
	}

	@Test
	public void testAddNewCall() {
		CallType callType = CallType.CALLIN;
		Call testCall = TestHelper.createTestCall(callType);
		int callId = 0;

		doReturn("").when(mockedProperties).getProperty("option.callmonitor.ignoreMSN");

		// add a new call
		mc.addNewCall(callId, testCall);
		Assert.assertEquals(CallState.PENDING, mc.getCallState(0));
		Assert.assertEquals(0, mc.getEstablishedSize());
		Assert.assertEquals(1, mc.getPendingSize());
		Call checkCall = mc.getCall(callId);
		TestHelper.assertTestCall(checkCall, callType);
		verify(this.mockedListener, times(1)).pendingCallIn(testCall);
		verify(this.mockedProperties, times(1)).getProperty("option.callmonitor.ignoreMSN");

		// establish call
		mc.establishCall(callId);
		Assert.assertEquals(CallState.ESTABLISHED, mc.getCallState(0));
		Assert.assertEquals(1, mc.getEstablishedSize());
		Assert.assertEquals(0, mc.getPendingSize());
		checkCall = mc.getCall(callId);
		TestHelper.assertTestCall(checkCall, callType);
		verify(this.mockedListener, times(1)).establishedCallIn(testCall);

		// remove call
		mc.removeCall(callId, testCall);
		Assert.assertEquals(CallState.NONE, mc.getCallState(0));
		Assert.assertEquals(0, mc.getEstablishedSize());
		Assert.assertEquals(0, mc.getPendingSize());
		checkCall = mc.getCall(callId);
		Assert.assertNull(checkCall);
		verify(this.mockedListener, times(1)).endOfCall(testCall);

		verifyNoMoreInteractions(this.mockedListener);
		verifyNoMoreInteractions(this.mockedProperties);
	}
}

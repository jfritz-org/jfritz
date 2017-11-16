/*
 * Created on 10.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import static org.mockito.Mockito.doReturn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.TestHelper;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.Port;

public class FBoxListenerV3Test extends TestCase {

	@Mock private FritzBox mockedFritzBox;
    @Mock private PropertyProvider mockedProperties;
    @Mock private MessageProvider mockedMessages;
    @Mock private SipProvider mockedSipProvider;
    @Mock private Port mockedPort;
    private MonitoredCalls monitoredCalls;

    private FBoxCallMonitorV3 fBoxCallMonitor;

    @BeforeClass
    public static void setup() {
    	TestHelper.initLogging();
    }

    @Before
    public void setUp() throws Exception {
    	MockitoAnnotations.initMocks(this);

        monitoredCalls = new MonitoredCalls();
        monitoredCalls.properties = mockedProperties;

        fBoxCallMonitor = new FBoxCallMonitorV3(mockedFritzBox, null, false);
        fBoxCallMonitor.properties = mockedProperties;
        fBoxCallMonitor.messages = mockedMessages;
        fBoxCallMonitor.monitoredCalls = monitoredCalls;
    }

    @After
    public void after() throws Exception {
        fBoxCallMonitor = null;
    }

    @Test
    public void testOutgoingCall() {
    	int sipId = 0;
    	int portId = 0;
    	int callId = 0;

    	initWhenRules(sipId, portId, mockedPort);

        fBoxCallMonitor.parseOutput("09.09.06 15:59:41;CALL;0;0;1234567;01237654321;SIP0");
		assertCall(callId, CallState.PENDING, CallType.CALLOUT, "09.09.06 15:59:41", "1234567@sipgate.de", mockedPort, "01237654321", 0, 1, 0);

        fBoxCallMonitor.parseOutput("09.09.06 16:00:01;CONNECT;0;0;01237654321;");
        assertCall(callId, CallState.ESTABLISHED, CallType.CALLOUT, "09.09.06 16:00:01", "1234567@sipgate.de", mockedPort, "01237654321", 0, 0, 1);

        fBoxCallMonitor.parseOutput("09.09.06 16:10:31;DISCONNECT;0;660;");
        Assert.assertEquals(0, monitoredCalls.getPendingSize());
        Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
        Assert.assertNull(monitoredCalls.getCall(callId));
    }

    @Test
    public void testOutgoingCallWithSemicolon() {
    	int sipId = 0;
    	int portId = 0;
    	int callId = 0;

    	initWhenRules(sipId, portId, mockedPort);

        fBoxCallMonitor.parseOutput("09.09.06 15:59:41;CALL;0;0;1234567;01237654321;SIP0;");
		assertCall(callId, CallState.PENDING, CallType.CALLOUT, "09.09.06 15:59:41", "1234567@sipgate.de", mockedPort, "01237654321", 0, 1, 0);

        fBoxCallMonitor.parseOutput("09.09.06 16:00:01;CONNECT;0;0;01237654321;");
        assertCall(callId, CallState.ESTABLISHED, CallType.CALLOUT, "09.09.06 16:00:01", "1234567@sipgate.de", mockedPort, "01237654321", 0, 0, 1);

        fBoxCallMonitor.parseOutput("09.09.06 16:10:31;DISCONNECT;0;660;");
        Assert.assertEquals(0, monitoredCalls.getPendingSize());
        Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
        Assert.assertNull(monitoredCalls.getCall(callId));
    }

    @Test
    public void testIncomingCall() {
    	int sipId = 0;
    	int portId = 0;
    	int callId = 2;
		Port port = new Port(0, "", "-1", "-1");

    	initWhenRules(sipId, portId, port);

        fBoxCallMonitor.parseOutput("09.09.06 16:03:55;RING;2;01781231234;4271960;POTS");
		assertCall(callId, CallState.PENDING, CallType.CALLIN, "09.09.06 16:03:55", "4271960", port, "01781231234", 0, 1, 0);

        fBoxCallMonitor.parseOutput("09.09.06 16:04:12;CONNECT;2;0;01781231234;");
        assertCall(callId, CallState.ESTABLISHED, CallType.CALLIN, "09.09.06 16:04:12", "4271960", port, "01781231234", 0, 0, 1);

        fBoxCallMonitor.parseOutput("09.09.06 16:06:31;DISCONNECT;2;660;");
        Assert.assertEquals(0, monitoredCalls.getPendingSize());
        Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
        Assert.assertNull(monitoredCalls.getCall(callId));
    }

    @Test
    public void testIncomingCallSemicolon() {
    	int sipId = 0;
    	int portId = 0;
    	int callId = 2;
		Port port = new Port(0, "", "-1", "-1");

    	initWhenRules(sipId, portId, port);

        fBoxCallMonitor.parseOutput("09.09.06 16:03:55;RING;2;01781231234;4271960;POTS;");
		assertCall(callId, CallState.PENDING, CallType.CALLIN, "09.09.06 16:03:55", "4271960", port, "01781231234", 0, 1, 0);

        fBoxCallMonitor.parseOutput("09.09.06 16:04:12;CONNECT;2;0;01781231234;");
        assertCall(callId, CallState.ESTABLISHED, CallType.CALLIN, "09.09.06 16:04:12", "4271960", port, "01781231234", 0, 0, 1);

        fBoxCallMonitor.parseOutput("09.09.06 16:06:31;DISCONNECT;2;660;");
        Assert.assertEquals(0, monitoredCalls.getPendingSize());
        Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
        Assert.assertNull(monitoredCalls.getCall(callId));
    }

    @Test
    public void testParseMultipleCalls() {
    	int sipId = 0;
    	int portId4 = 4;
    	int portId1 = 1;
    	int callId1 = 1;
    	int callId2 = 2;
		Port port4 = new Port(0, "", "-1", "-1");
		Port port1 = new Port(0, "", "-1", "-1");

    	initWhenRules(sipId, portId4, port4, portId1, port1);

    	fBoxCallMonitor.parseOutput("13.04.03 09:10:13;CALL;1;4;7654321;01231234567;ISDN");
		assertCall(callId1, CallState.PENDING, CallType.CALLOUT, "13.04.03 09:10:13", "7654321", port4, "01231234567", 0, 1, 0);

        fBoxCallMonitor.parseOutput("13.04.03 09:10:17;CONNECT;1;4;01231234567;");
        assertCall(callId1, CallState.ESTABLISHED, CallType.CALLOUT, "13.04.03 09:10:17", "7654321", port4, "01231234567", 0, 0, 1);

    	fBoxCallMonitor.parseOutput("09.09.06 16:03:55;RING;2;01781231234;4271960;POTS;");
		assertCall(callId2, CallState.PENDING, CallType.CALLIN, "09.09.06 16:03:55", "4271960", port1, "01781231234", 0, 1, 1);

        fBoxCallMonitor.parseOutput("09.09.06 16:04:12;CONNECT;2;1;01781231234;");
        assertCall(callId2, CallState.ESTABLISHED, CallType.CALLIN, "09.09.06 16:04:12", "4271960", port1, "01781231234", 0, 0, 2);

        fBoxCallMonitor.parseOutput("09.09.06 16:06:31;DISCONNECT;2;660;");
        Assert.assertEquals(0, monitoredCalls.getPendingSize());
        Assert.assertEquals(1, monitoredCalls.getEstablishedSize());
        Assert.assertNull(monitoredCalls.getCall(callId2));

        fBoxCallMonitor.parseOutput("09.09.06 16:06:31;DISCONNECT;1;800;");
        Assert.assertEquals(0, monitoredCalls.getPendingSize());
        Assert.assertEquals(0, monitoredCalls.getEstablishedSize());
        Assert.assertNull(monitoredCalls.getCall(callId1));
    }

	private void assertCall(int callId, CallState callState, CallType callType, String dateStr,
			String routeStr, Port port, String numberStr,
			int duration, int pendingSize, int establishedSize) {
		Assert.assertEquals(callState, monitoredCalls.getCallState(callId));
        Assert.assertEquals(pendingSize, monitoredCalls.getPendingSize());
        Assert.assertEquals(establishedSize, monitoredCalls.getEstablishedSize());
        Call call = monitoredCalls.getCall(callId);
        Assert.assertEquals(getDate(dateStr), call.getCalldate());
        Assert.assertEquals(callType.toString(), call.getCalltype().toString());
        Assert.assertEquals(port, call.getPort());
        Assert.assertEquals(numberStr, call.getPhoneNumber().getAreaNumber());
        Assert.assertEquals(routeStr, call.getRoute().split(" ")[0]);
        Assert.assertEquals(duration, call.getDuration());
	}

	private Date getDate(final String input) {
		Date result = null;
		try {
			result = new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(input);
		} catch (ParseException pe) {
			// nothing to do here
		}
		return result;
	}

	private void initWhenRules(int sipId, int portId, Port port) {
		doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.monitorTableIncomingCalls");
		doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.popupIncomingCalls");
    	doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.monitorTableOutgoingCalls");
    	doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.popupOutgoingCalls");
    	doReturn("false").when(this.mockedProperties).getProperty("option.activateDialPrefix");
    	doReturn("49").when(this.mockedProperties).getProperty("country.code");
		doReturn("0").when(this.mockedProperties).getProperty("area.prefix");
    	doReturn("").when(this.mockedProperties).getProperty("option.callmonitor.ignoreMSN");
    	doReturn(mockedSipProvider).when(this.mockedFritzBox).getSipProvider(sipId);
    	doReturn("1234567@sipgate.de").when(this.mockedSipProvider).toString();
		doReturn(port).when(this.mockedFritzBox).getConfiguredPort(portId);
	}

	private void initWhenRules(int sipId, int portId1, Port port1, int portId2, Port port2) {
		doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.monitorTableIncomingCalls");
		doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.popupIncomingCalls");
    	doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.monitorTableOutgoingCalls");
    	doReturn("true").when(this.mockedProperties).getProperty("option.callmonitor.popupOutgoingCalls");
    	doReturn("false").when(this.mockedProperties).getProperty("option.activateDialPrefix");
    	doReturn("49").when(this.mockedProperties).getProperty("country.code");
		doReturn("0").when(this.mockedProperties).getProperty("area.prefix");
    	doReturn("").when(this.mockedProperties).getProperty("option.callmonitor.ignoreMSN");
    	doReturn(mockedSipProvider).when(this.mockedFritzBox).getSipProvider(sipId);
    	doReturn("1234567@sipgate.de").when(this.mockedSipProvider).toString();
		doReturn(port1).when(this.mockedFritzBox).getConfiguredPort(portId1);
		doReturn(port2).when(this.mockedFritzBox).getConfiguredPort(portId2);
	}
}

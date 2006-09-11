/*
 * Created on 10.09.2006
 *
 */
package de.moonflower.jfritz.testsuites.utils.network;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.utils.network.CallMonitor;
import de.moonflower.jfritz.utils.network.FBoxListenerV3;
import junit.framework.TestCase;

public class TestCase_FBoxListenerV3 extends TestCase {

    private FBoxListenerV3 fBox;

    private JFritz jfritz;

    public TestCase_FBoxListenerV3() {
        jfritz = new JFritz(false, false, "", false, false, false);
    }

    public void setUp() throws Exception {
        super.setUp();
        fBox = new FBoxListenerV3(jfritz);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        fBox = null;
    }

    public void testParseMultipleCalls() {
        fBox.parseOutput("09.09.06 15:59:41;CALL;0;0;1234567;01237654321;SIP0", false);
        fBox.parseOutput("13.04.03 09:10:13;CALL;1;4;7654321;01231234567;ISDN", false);
        fBox.parseOutput("09.09.06 16:03:55;RING;2;0178xxxxxxx;4271960;POTS;", false);

        Call call = CallMonitor.callMonitoring.getCall(0);

        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 15:59:41"));
            assertEquals(call.getCalltype().toString(), CallType.CALLIN_STR);
            assertEquals(call.getPort(), "0");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01237654321");
            assertEquals(call.getRoute().split(" ")[0], "1234567");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        call = CallMonitor.callMonitoring.getCall(1);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("13.04.03 09:10:13"));
            assertEquals(call.getCalltype().toString(), CallType.CALLIN_STR);
            assertEquals(call.getPort(), "4");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01231234567");
            assertEquals(call.getRoute().split(" ")[0], "7654321");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        fBox.parseOutput("09.09.06 16:03:08;DISCONNECT;0;156;", false);
        assertEquals(CallMonitor.callMonitoring.getPendingSize(), 2);
        fBox.parseOutput("13.04.03 09:40:13;DISCONNECT;1;960;", false);
        assertEquals(CallMonitor.callMonitoring.getPendingSize(), 1);
    }

}

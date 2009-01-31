/*
 * Created on 10.09.2006
 *
 */
package de.moonflower.jfritz.testsuites.utils.network;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callmonitor.CallMonitorList;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV3;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.utils.Debug;
import junit.framework.TestCase;

public class FBoxListenerV3Test extends TestCase {

    private FBoxCallMonitorV3 fBox;

    public JFritz jfritz;

    public FBoxListenerV3Test() {
    	Debug.on();
		Main.loadProperties();
		Main.loadMessages(new Locale("de_DE"));
		String[] args = new String[1];
		args[0] = "-q";
		Main main = new Main(args);
        jfritz = main.getJfritz();
    }

    public void setUp() throws Exception {
        super.setUp();
        fBox = new FBoxCallMonitorV3();
    }

    public void tearDown() throws Exception {
        fBox = null;
        super.tearDown();
    }

    /**
     * This testset tests an simple incoming call
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call port
     *      - call number
     *      - call route
     */
    public void testIncomingCall() {
        fBox.parseOutput("09.09.06 15:59:41;CALL;0;0;1234567;01237654321;SIP0", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(0));

        Call call = JFritz.getCallMonitorList().getCall(0);

        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 15:59:41"));
            assertEquals(call.getCalltype().toString(), CallType.CALLOUT_STR);
            assertEquals(call.getPort(), "0");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01237654321");
            assertEquals(call.getRoute().split(" ")[0], "1234567");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * This testset tests an simple incoming call with an ending semicolon
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call port
     *      - call number
     *      - call route
     */
    public void testIncomingCallWithSemicolon() {
        fBox.parseOutput("09.09.06 15:59:41;CALL;0;0;1234567;01237654321;SIP0;", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(0));

        Call call = JFritz.getCallMonitorList().getCall(0);

        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 15:59:41"));
            assertEquals(call.getCalltype().toString(), CallType.CALLOUT_STR);
            assertEquals(call.getPort(), "0");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01237654321");
            assertEquals(call.getRoute().split(" ")[0], "1234567");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * This testset tests an simple outgoing call
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call number
     *      - call route
     */
    public void testOutgoingCall() {
        fBox.parseOutput("09.09.06 16:03:55;RING;2;01781231234;4271960;POTS", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(2));
        Call call = JFritz.getCallMonitorList().getCall(2);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 16:03:55"));
            assertEquals(call.getCalltype().toString(), CallType.CALLIN_STR);
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01781231234");
            assertEquals(call.getRoute().split(" ")[0], "4271960");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * This testset tests an simple incoming call with an ending semicolon
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call number
     *      - call route
     */
    public void testOutgoingCallWithSemicolon() {
        fBox.parseOutput("09.09.06 16:03:55;RING;2;01781231234;4271960;POTS;", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(2));
        Call call = JFritz.getCallMonitorList().getCall(2);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 16:03:55"));
            assertEquals(call.getCalltype().toString(), CallType.CALLIN_STR);
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01781231234");
            assertEquals(call.getRoute().split(" ")[0], "4271960");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * This testset tests a complete call from initialisation, connect and disconnect of an incoming call
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call port
     *      - call number
     *      - call route
     */
    public void testEstablishIncomingCall() {
        fBox.parseOutput("09.09.06 16:03:00;RING;2;01781231234;4271960;POTS;", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(2));

        fBox.parseOutput("09.09.06 16:03:10;CONNECT;2;4;01781231234;");
        assertEquals(CallMonitorList.ESTABLISHED, JFritz.getCallMonitorList().getCallState(2));

        Call call = JFritz.getCallMonitorList().getCall(2);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 16:03:10"));
            assertEquals(call.getCalltype().toString(), CallType.CALLIN_STR);
            assertEquals(call.getPort(), "4");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01781231234");
            assertEquals(call.getRoute().split(" ")[0], "4271960");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fBox.parseOutput("09.09.06 16:05:00;DISCONNECT;2;110;");
        assertEquals(CallMonitorList.NONE, JFritz.getCallMonitorList().getCallState(2));
    }

    /**
     * This testset tests a complete call from initialisation, connect and disconnect of an outgoing call
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call port
     *      - call number
     *      - call route
     */
    public void testEstablishOutgoingCall() {
        fBox.parseOutput("09.09.06 16:03:00;CALL;2;4;4271960;01781231234;POTS;", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(2));

        fBox.parseOutput("09.09.06 16:03:10;CONNECT;2;4;01781231234;");
        assertEquals(CallMonitorList.ESTABLISHED, JFritz.getCallMonitorList().getCallState(2));

        Call call = JFritz.getCallMonitorList().getCall(2);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 16:03:10"));
            assertEquals(call.getCalltype().toString(), CallType.CALLOUT_STR);
            assertEquals(call.getPort(), "4");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01781231234");
            assertEquals(call.getRoute().split(" ")[0], "4271960");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fBox.parseOutput("09.09.06 16:05:00;DISCONNECT;2;110;");
        assertEquals(CallMonitorList.NONE, JFritz.getCallMonitorList().getCallState(2));
    }

    /**
     * This testset tests multiple calls
     * Verifies:
     *      - call state
     *      - call date
     *      - call type
     *      - call port
     *      - call number
     *      - call route
     */
    public void testParseMultipleCalls() {
        fBox.parseOutput("09.09.06 15:59:41;CALL;0;0;1234567;01237654321;SIP0", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(0));

        fBox.parseOutput("13.04.03 09:10:13;CALL;1;4;7654321;01231234567;ISDN", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(1));

        fBox.parseOutput("13.04.03 09:10:13;CONNECT;1;4;01231234567;", false);
        assertEquals(CallMonitorList.ESTABLISHED, JFritz.getCallMonitorList().getCallState(1));

        fBox.parseOutput("09.09.06 16:03:55;RING;2;01781231234;4271960;POTS", false);
        assertEquals(CallMonitorList.PENDING, JFritz.getCallMonitorList().getCallState(2));


        Call call = JFritz.getCallMonitorList().getCall(0);

        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 15:59:41"));
            assertEquals(call.getCalltype().toString(), CallType.CALLOUT_STR);
            assertEquals(call.getPort(), "0");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01237654321");
            assertEquals(call.getRoute().split(" ")[0], "1234567");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        call = JFritz.getCallMonitorList().getCall(1);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("13.04.03 09:10:13"));
            assertEquals(call.getCalltype().toString(), CallType.CALLOUT_STR);
            assertEquals(call.getPort(), "4");
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01231234567");
            assertEquals(call.getRoute().split(" ")[0], "7654321");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        call = JFritz.getCallMonitorList().getCall(2);
        try {
            assertEquals(call.getCalldate(), new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse("09.09.06 16:03:55"));
            assertEquals(call.getCalltype().toString(), CallType.CALLIN_STR);
            assertEquals(call.getPhoneNumber().getAreaNumber(), "01781231234");
            assertEquals(call.getRoute().split(" ")[0], "4271960");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        fBox.parseOutput("09.09.06 16:03:08;DISCONNECT;0;156;", false);
        assertEquals(JFritz.getCallMonitorList().getPendingSize(), 1);
        fBox.parseOutput("13.04.03 09:40:13;DISCONNECT;2;310;", false);
        assertEquals(JFritz.getCallMonitorList().getPendingSize(), 0);
        fBox.parseOutput("13.04.03 09:40:13;DISCONNECT;1;960;", false);
        assertEquals(JFritz.getCallMonitorList().getPendingSize(), 0);
    }

}

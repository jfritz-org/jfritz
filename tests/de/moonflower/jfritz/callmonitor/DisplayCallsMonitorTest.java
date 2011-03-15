package de.moonflower.jfritz.callmonitor;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.TestHelper;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.sounds.PlaySound;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;

public class DisplayCallsMonitorTest {

	@Mock
	PlaySound sound;
	private DisplayCallsMonitor cm = new DisplayCallsMonitor(sound);

	@BeforeClass
	public static void setup() {
		TestHelper.initLogging();
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		Debug.on();
    	Debug.setVerbose(true);
    	Debug.setDebugLevel(Debug.LS_DEBUG);
		PropertyProvider.getInstance().loadProperties(false);
		MessageProvider.getInstance().loadMessages(new Locale("de_DE"));
		JFritz.loadNumberSettings();
	}

	@Test
	public void test() {

//		properties.getProperty("option.popuptype")
//		properties.getProperty("option.playSounds")
//		properties.getProperty("option.startExternProgram")
//		properties.getProperty("option.activateDialPrefix")
//		properties.getProperty("option.externProgram"

//		messages.getMessage("unknown")
//		messages.getMessage("incoming_call")
//		messages.getMessage("from")
//		messages.getMessage("to")
//		messages.getMessage("outgoing_call")
//		messages.getMessage("through_provider")
//		messages.getMessage("no_external_program")
//		messages.getMessage("not_external_program_start")

		CallType callType = CallType.CALLIN;
		Date now = Calendar.getInstance().getTime();
		PhoneNumberOld phoneNumber = new PhoneNumberOld("07212542015", false);
		Port dialPort = new Port(0, "PortName", "21", "621");
		String route = "865072";
		int duration = 0;
		Call call = new Call(callType, now, phoneNumber, dialPort, route, duration);

		String caller = "caller";
		String called = "called";
		String port = "port";
		Person person = null;
		cm.displayCallInMsg(call, caller, called, port, person);

		verify(sound, times(1)).playRingSound(1.0f);
	}
}

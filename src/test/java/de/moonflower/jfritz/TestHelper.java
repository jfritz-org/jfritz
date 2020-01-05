package de.moonflower.jfritz;

import java.util.Calendar;
import java.util.Date;

import de.moonflower.jfritz.struct.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

import de.moonflower.jfritz.properties.PropertyProvider;

public class TestHelper {

	public static void initLogging() {
		ConsoleAppender ca = new ConsoleAppender();
		ca.setName("Console");
		ca.setTarget("System.out");
		ca.setLayout(new PatternLayout("%d{dd.MM.yyyy HH:mm:ss}|%p|%C{1}|%m%n"));
		ca.activateOptions();
		ca.setThreshold(Level.DEBUG);
		LogManager.getRootLogger().addAppender(ca);
	}

	public static Call createTestCall(CallType callType) {
		Date callDate = createTestDate();
		PhoneNumberOld number = createTestPhoneNumber();
		Port port = createTestPort();
		String route = createTestRoute();
		Call call = new Call(callType, callDate, number, port, route, 0);
		return call;
	}

	public static boolean assertTestCall(final Call call, final CallType callType) {
		return (call.getCalldate() == createTestDate()
				&& call.getCalltype() == callType
				&& call.getComment().equals("")
				&& call.getCost() == 0.0
				&& call.getDuration() == 0
				&& call.getPhoneNumber() == createTestPhoneNumber()
				&& call.getPort() == createTestPort()
				&& call.getRoute() == createTestRoute()
				&& call.getRouteType() == 0);
	}

	private static Date createTestDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(2011, 02, 26);
		return cal.getTime();
	}

	private static PhoneNumberOld createTestPhoneNumber() {
		return new PhoneNumberOld(PropertyProvider.getInstance(), "+4972112345678", false);
	}

	private static Port createTestPort() {
		return new Port(0, PortType.GENERIC, "PortName", "10", "610");
	}

	private static String createTestRoute() {
		return "987654";
	}
}

package de.moonflower.jfritz;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

public class TestHelper {

	public static void initLogging() {
		ConsoleAppender ca = new ConsoleAppender();
		ca.setName("Console");
		ca.setTarget("System.out");
		ca.setLayout(new PatternLayout());
		ca.activateOptions();
		LogManager.getRootLogger().addAppender(ca);
	}
}

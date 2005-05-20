/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz;

/**
 * @author Arno Willig
 *
 */
public class Debug {

	private static int debugLevel;

	public static void on() {
		Debug.debugLevel = 3;
		msg("debugging mode has been enabled");
	}

	public static void msg(String message) {
		msg(1, message);
	}

	public static void msg(int level, String message) {
		if (debugLevel >= level) {
			System.out.println("DEBUG: " + message);
		}
	}

	public static void err(String message) {
		System.err.println("ERROR: " + message);
	}

}

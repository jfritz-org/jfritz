/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * @author Arno Willig
 *
 */
public class Debug {

	private static int debugLevel;
	private static boolean logFile = false;
	private static String logFileName = "";

	public static void on() {
		Debug.debugLevel = 3;
		msg("debugging mode has been enabled");
	}

	public static void logToFile(String fname) {
		Debug.debugLevel = 3;
		logFile = true;
		logFileName = fname;
		logMessage("------------------------------------------");
		msg("logging to file \""+ fname + "\" has been enabled");
	}

	public static void msg(String message) {
		msg(1, message);
	}

	public static void msg(int level, String message) {
		if (debugLevel >= level) {
			System.out.println("DEBUG: " + message);
			if (logFile) {
				logMessage("DEBUG: " + message);
			}
		}
	}

	public static void err(String message) {
		System.err.println("ERROR: " + message);
		if (logFile) {
			logMessage("ERROR: " + message);
		}
	}

	public static void logMessage(String message) {
		BufferedWriter appendedFile = null;
		try {
		appendedFile = new BufferedWriter(new FileWriter(logFileName, true));
    	 appendedFile.write(message);
    	 appendedFile.newLine();
    	 appendedFile.flush();
		appendedFile.close();
		}
		catch (Exception e) {
			System.out.println("EXCEPTION when writing to LOGFILE");
		} finally {                       // always close the file
			if (appendedFile != null) try {
				appendedFile.close();
			} catch (Exception ioe2) {
			    // just ignore it
			}
		}
	}

	public static void errDlg(String message) {
		err(message);
		JOptionPane.showMessageDialog(null, message);
	}
}

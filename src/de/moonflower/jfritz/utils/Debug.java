/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write debug messages to STDOUT or FILE.
 * Show Error-Dialog with a special message
 *
 * @author Arno Willig
 *
 */
public class Debug {

	private static int debugLevel;
	private static boolean logFile = false;
	private static String logFileName = "";

	/**
	 * Turns debug-mode on
	 *
	 */
	public static void on() {
		Debug.debugLevel = 3;
		msg("debugging mode has been enabled");
	}

	/**
	 * Turn on logging mode to file
	 * @param fname Filename to log into
	 */
	public static void logToFile(String fname) {
		Debug.debugLevel = 3;
		logFile = true;
		logFileName = fname;
		logMessage("------------------------------------------");
		msg("logging to file \""+ fname + "\" has been enabled");
	}

	/**
	 * Print message with priority 1
	 * @param message
	 */
	public static void msg(String message) {
		msg(1, message);
	}

	/**
	 *
	 * @return current Time HH:mm:ss
	 */
	private static String getCurrentTime() {
		Date now = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		return df.format(now);
	}

	/**
	 * Print message with prioriry level
	 * @param level
	 * @param message
	 */
	public static void msg(int level, String message) {
		if (debugLevel >= level) {
			message = "(" + getCurrentTime() + ") DEBUG: " + message;
			System.out.println(message);
			if (logFile) {
				logMessage(message);
			}
		}
	}

	/**
	 * Print error-message
	 * @param message
	 */
	public static void err(String message) {
		message = "(" + getCurrentTime() + ") ERROR: " + message;
		System.err.println(message);
		if (logFile) {
			logMessage(message);
		}
	}

	/**
	 * Write message to logfile
	 * @param message
	 */
	private static void logMessage(String message) {
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

    /**
     * Show Dialog with message
     * @param message
     */
    public static void msgDlg(String message) {
        msg(message);
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * Show error Dialog with message
	 * @param message
	 */
	public static void errDlg(String message) {
		err(message);
        JOptionPane.showMessageDialog(null, message, "Fehler", JOptionPane.ERROR_MESSAGE);
	}
}

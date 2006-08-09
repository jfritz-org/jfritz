/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.JFritz;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write debug messages to STDOUT or FILE.
 * Show Error-Dialog with a special message
 *
 * 14.05.06 Added support for redirecting System.out and System.err
 * Now all exceptions are also included in the debug file
 * Brian Jensen
 *
 * @author Arno Willig
 *
 */
public class Debug {

	private static int debugLevel;
	private static boolean verboseMode = false;
	private static boolean logFile = false;
	private static PrintStream fileRedirecter, originalOut;


	/**
	 * Turns debug-mode on
	 *
	 */
	public static void on() {
		verboseMode = true;
		Debug.debugLevel = 3;
		msg("debugging mode has been enabled"); //$NON-NLS-1$

	}

	/**
	 * This function works by redirecting System.out and System.err to fname
	 * The original console stream is saved as originalout
	 * 15.05.06 Brian Jensen
	 *
	 * Turn on logging mode to file
	 * @param fname Filename to log into
	 */
	public static void logToFile(String fname) {
		Debug.debugLevel = 3;
		logFile = true;
		//Save the original outputstream so we can write to the console too!
		originalOut = System.out;

		try {
			//setup the redirection of Sysem.out and System.err
			FileOutputStream tmpOutputStream = new FileOutputStream(JFritz.SAVE_DIR+fname);
			fileRedirecter = new PrintStream(tmpOutputStream);
			System.setOut(fileRedirecter);
			System.setErr(fileRedirecter);
 		}

		catch (Exception e) {
			System.out.println("EXCEPTION when writing to LOGFILE"); //$NON-NLS-1$
		}

		fileRedirecter.println("------------------------------------------"); //$NON-NLS-1$
		msg("logging to file \""+ JFritz.SAVE_DIR + fname + "\" has been enabled"); //$NON-NLS-1$,  //$NON-NLS-2$
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
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		return df.format(now);
	}

	/**
	 * Print message with prioriry level
	 * @param level
	 * @param message
	 */
	public static void msg(int level, String message) {
		if (debugLevel >= level) {
			message = "(" + getCurrentTime() + ") DEBUG: " + message; //$NON-NLS-1$,  //$NON-NLS-2$
			System.out.println(message);

			//if both verbose mode and logging enabled, make sure output
			//still lands on the console as well!
			if (logFile && verboseMode) {
				originalOut.println(message);
			}
		}
	}

	/**
	 * Print error-message
	 * @param message
	 */
	public static void err(String message) {
		message = "(" + getCurrentTime() + ") ERROR: " + message; //$NON-NLS-1$,  //$NON-NLS-2$
		System.err.println(message);

		//if both verbose mode and logging enabled, make sure output
		//still lands on the console as well!
		if (logFile && verboseMode) {
			originalOut.println(message);
		}
	}


	/**
	 * Write message to logfile
	 * @param message

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
			System.out.println("EXCEPTION when writing to LOGFILE"); //$NON-NLS-1$
		} finally {                       // always close the file
			if (appendedFile != null) try {
				appendedFile.close();
			} catch (Exception ioe2) {
			    // just ignore it
			}
		}

	}
	*/

    /**
     * Show Dialog with message
     * @param message
     */
    public static void msgDlg(String message) {
        msg(message);
        JOptionPane.showMessageDialog(null, message, JFritz.getMessage("information"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
    }

    /**
	 * Show error Dialog with message
	 * @param message
	 */
	public static void errDlg(String message) {
		err(message);
        JOptionPane.showMessageDialog(null, message, JFritz.getMessage("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}
}

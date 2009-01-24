/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.autoupdate;

import de.moonflower.jfritz.Main;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.JOptionPane;

public final class Logger{
	private Logger() {}

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss", Locale.GERMANY); //$NON-NLS-1$

	private static boolean enabled = false;

	/**
	 * Turns debug-mode on
	 *
	 */
	public static void on() {
		enabled = true;
		msg("debugging mode has been enabled"); //$NON-NLS-1$
	}

	/**
	 * Print message
	 *
	 * @param message
	 */
	public static void msg(final String msg) {
		if (enabled)
		{
			System.out.println("(" + getCurrentTime() + ") DEBUG: " + msg);
		}
	}

	/**
	 *
	 * @return current Time HH:mm:ss
	 */
	 private synchronized static String getCurrentTime() {
		return dateFormat.format(new java.util.Date());
	}

	/**
	 * This is a modified message function, used by the network subsystem
	 * so the debug output is more readable
	 *
	 * @param message
	 */
	public static void netMsg(final String msg){
		if (enabled)
		{
			System.out.println("(" + getCurrentTime() + ") NETWORK: " + msg);
		}
	}

	/**
	 * Print error-message
	 *
	 * @param message
	 */
	public static void err(final String msg) {
		System.err.println("(" + getCurrentTime() + ") ERROR: " + msg);
	}

	/**
	 * Show Dialog with message
	 *
	 * @param message
	 */
	public static void msgDlg(final String message) {
		if (enabled)
		{
			msg(message);
			JOptionPane.showMessageDialog(null, message, Main
					.getMessage("information"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
		}
	}

	/**
	 * Show error Dialog with message
	 *
	 * @param message
	 */
	public static void errDlg(final String message) {
		err(message);
		JOptionPane.showMessageDialog(null, message,
				Main.getMessage("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}
}

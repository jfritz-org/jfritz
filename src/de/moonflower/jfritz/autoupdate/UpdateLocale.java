package de.moonflower.jfritz.autoupdate;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.moonflower.jfritz.utils.Debug;

public class UpdateLocale {
	private static ResourceBundle messages;

	/**
	 * Lädt die Ressource-Nachrichten
	 *
	 * @param locale
	 */
	public static void loadMessages(Locale locale) {
		try {
			messages = ResourceBundle.getBundle("update", locale);//$NON-NLS-1$
		} catch (MissingResourceException e) {
			System.err.println("Cannot find the language file \"update_"
					+ locale + ".properties\"!");//$NON-NLS-1$
		}
	}

	/**
	 * @return Returns an internationalized message. Last modified: 26.04.06 by
	 *         Bastian
	 */
	public static String getMessage(String msg) {
		String i18n = ""; //$NON-NLS-1$
		try {
			if (!messages.getString(msg).equals("")) {
				i18n = messages.getString(msg);
			} else {
				i18n = msg;
			}
		} catch (MissingResourceException e) {
			Debug.err("Can't find resource string for " + msg); //$NON-NLS-1$
			i18n = msg;
		}
		return i18n;
	}

}

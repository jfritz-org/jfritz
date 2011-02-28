package de.moonflower.jfritz.messages;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import de.moonflower.jfritz.ProgramConstants;

public class MessageProvider {

	private final static Logger log = Logger.getLogger(MessageProvider.class);

	private static ResourceBundle messages;
	private static ResourceBundle en_messages;

	private static MessageProvider INSTANCE = new MessageProvider();

	public static MessageProvider getInstance() {
		return INSTANCE;
	}

	/**
	 * Loads resource messages
	 *
	 * @param locale
	 */
	public void loadMessages(Locale locale) {
		try {
			log.info("Loading locale: " + locale);
			en_messages = ResourceBundle.getBundle("jfritz", new Locale("en","US"));//$NON-NLS-1$
			messages = ResourceBundle.getBundle("jfritz", locale);//$NON-NLS-1$

			UIManager.put("OptionPane.cancelButtonText", getMessage("cancel"));
			UIManager.put("OptionPane.noButtonText", getMessage("no"));
			UIManager.put("OptionPane.okButtonText", getMessage("okay"));
			UIManager.put("OptionPane.yesButtonText", getMessage("yes"));
		} catch (MissingResourceException e) {
			log.error("Can't find i18n resource! (\"jfritz_" + locale + ".properties\")");//$NON-NLS-1$
			JOptionPane.showMessageDialog(null, ProgramConstants.PROGRAM_NAME + " v"//$NON-NLS-1$
					+ ProgramConstants.PROGRAM_VERSION
					+ "\n\nCannot find the language file \"jfritz_" + locale
					+ ".properties\"!" + "\nProgram will exit!");//$NON-NLS-1$
		}
	}

	/**
	 * @return Returns an internationalized message
	 */
	public String getMessage(String msg) {
		String i18n = ""; //$NON-NLS-1$
		try {
			if (messages != null && !messages.getString(msg).equals("")) {
				i18n = messages.getString(msg);
			} else {
				i18n = msg;
			}
		} catch (MissingResourceException e) {
			log.error("Can't find resource string for " + msg); //$NON-NLS-1$
			if (en_messages != null) {
				i18n = en_messages.getString(msg);
			} else {
				String errMsg = "Messages have not been initialized"; //$NON-NLS-1$
				log.error(errMsg);
				i18n = errMsg;
			}
		}
		return i18n;
	}

}

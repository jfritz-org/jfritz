package de.moonflower.jfritz.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.utils.JFritzUtils;

public class SupportedFritzBoxProvider {

	private final static Logger log = Logger.getLogger(SupportedFritzBoxProvider.class);

	private static Properties boxes = new Properties();

	private static SupportedFritzBoxProvider INSTANCE = new SupportedFritzBoxProvider();
	private static String unknownMessage = MessageProvider.getInstance().getMessage("unknown");

	public static SupportedFritzBoxProvider getInstance() {
		return INSTANCE;
	}

	public String getBoxById(byte id) {
		try {
			if (boxes.size() == 0) {
				// TODO load boxes
				log.info("Loading box names");
				String path = JFritzUtils.getFullPath("/conf");
				String fileName = "boxnames.conf";
				InputStream fis = new FileInputStream(path + "/" + fileName);
				boxes.load(fis); //$NON-NLS-1$
			}

			String result = boxes.getProperty(String.valueOf(id), unknownMessage);
			if ("".equals(result)) {
				result = unknownMessage;
			}
			return result;
		} catch (MissingResourceException e) {
			log.error("Can't find box name for " + id); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			log.error("Could not find file!" + e.toString()); //$NON-NLS-1$
		} catch (IOException e) {
			log.error("IOException " + e.toString()); //$NON-NLS-1$
		}

		return unknownMessage;
	}

}

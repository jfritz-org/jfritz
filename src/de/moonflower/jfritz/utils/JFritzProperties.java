/*
 * Created on 21.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import de.moonflower.jfritz.exceptions.InvalidPropertiesFormatException;

/**
 * Backport of JRE 1.5 Properties class to JRE 1.4.2
 *
 * @author Arno Willig
 *
 */
public class JFritzProperties extends Properties {

	/**
	 *
	 */
	public JFritzProperties() {
		super();
	}

	public JFritzProperties(JFritzProperties defaultProperties) {
		super(defaultProperties);
	}

	public synchronized void loadFromXML(InputStream in)
			throws InvalidPropertiesFormatException, IOException {
		if (in == null)
			throw new NullPointerException();
		XMLProperties.load(this, in);
	}

	public synchronized void storeToXML(OutputStream os, String comment)
			throws IOException {
		if (os == null)
			throw new NullPointerException();
		storeToXML(os, comment, "UTF-8");
	}

	public synchronized void storeToXML(OutputStream os, String comment,
			String encoding) throws IOException {
		if (os == null)
			throw new NullPointerException();
		XMLProperties.save(this, os, comment, encoding);
	}

	public Object setProperty(String key, String value) {
		return super.setProperty(key, value);
	}

	public String getProperty(String key) {
		return super.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return super.getProperty(key, defaultValue);
	}

	public Object remove(Object key) {
		return super.remove(key);
	}

}

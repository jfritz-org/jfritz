/*
 * Created on 21.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Backport of JRE 1.5 Properties class to JRE 1.4.2
 *
 * @author Arno Willig
 *
 */
public class JFritzProperties extends Properties {
	private static final String PROPS_DTD_URI = "http://java.sun.com/dtd/properties.dtd";

	private static final String PROPS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!-- DTD for properties -->"
			+ "<!ELEMENT properties ( comment?, entry* ) >"
			+ "<!ATTLIST properties"
			+ " version CDATA #FIXED \"1.0\">"
			+ "<!ELEMENT comment (#PCDATA) >"
			+ "<!ELEMENT entry (#PCDATA) >"
			+ "<!ATTLIST entry " + " key CDATA #REQUIRED>";

	/**
	 * Version number for the format of exported properties files.
	 */
	private static final String EXTERNAL_XML_VERSION = "1.0";

	/**
	 *
	 */
	public JFritzProperties() {
		super();
	}

	public JFritzProperties(JFritzProperties defaultProperties) {
		super(defaultProperties);
	}

	public synchronized void loadFromXML(InputStream in) throws IOException {
		if (in == null)
			throw new NullPointerException();
		try {
			load(this, in);
		} catch (Exception e) {
			throw new IOException("Could not load properties!");
		}
	}

	public synchronized void storeToXML(OutputStream os, String comment)
			throws IOException {
		if (os == null)
			throw new NullPointerException();
		save(this, os, comment, "UTF-8");
	}

	public synchronized Object setProperty(String key, String value) {
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

	// ********************* XML Utilities *********************
	// load, getLoadingDoc, importProperties, save, emitDocument

	public static void load(Properties props, InputStream in)
			throws IOException, SAXException {
		Document doc = null;
		doc = getLoadingDoc(in);
		Element propertiesElement = (Element) doc.getChildNodes().item(1);
		String xmlVersion = propertiesElement.getAttribute("version");
		if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0)
			throw new SAXException("Exported Properties file format version "
					+ xmlVersion
					+ " is not supported. This java installation can read"
					+ " versions " + EXTERNAL_XML_VERSION + " or older. You"
					+ " may need to install a newer version of JDK.");
		importProperties(props, propertiesElement);
	}

	public static Document getLoadingDoc(InputStream in) throws SAXException,
			IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setValidating(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringComments(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String pid, String sid)
				throws SAXException {
			if (sid.equals(PROPS_DTD_URI)) {
				InputSource is;
				is = new InputSource(new StringReader(PROPS_DTD));
				is.setSystemId(PROPS_DTD_URI);
				return is;
			}
			throw new SAXException("Invalid system identifier: " + sid);
		}
			});
			db.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException x) throws SAXException {
					throw x;
				}

				public void fatalError(SAXParseException x) throws SAXException {
					throw x;
				}

				public void warning(SAXParseException x) throws SAXException {
					throw x;
				}
			});
			InputSource is = new InputSource(in);
			return db.parse(is);
		} catch (ParserConfigurationException x) {
			throw new Error(x);
		}
	}

	public static void importProperties(Properties props,
			Element propertiesElement) {
		NodeList entries = propertiesElement.getChildNodes();
		int numEntries = entries.getLength();
		int start = numEntries > 0
				&& entries.item(0).getNodeName().equals("comment") ? 1 : 0;
		for (int i = start; i < numEntries; i++) {
			Element entry = (Element) entries.item(i);
			if (entry.hasAttribute("key")) {
				Node n = entry.getFirstChild();
				String val = (n == null) ? "" : n.getNodeValue();
				props.setProperty(entry.getAttribute("key"), val);
			}
		}
	}

	public static void save(Properties props, OutputStream os, String comment,
			String encoding) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			// FIXME assert(false);
		}
		Document doc = db.newDocument();
		Element properties = (Element) doc.appendChild(doc
				.createElement("properties"));

		if (comment != null) {
			Element comments = (Element) properties.appendChild(doc
					.createElement("comment"));
			comments.appendChild(doc.createTextNode(comment));
		}

		Set keys = props.keySet();
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			Element entry = (Element) properties.appendChild(doc
					.createElement("entry"));
			entry.setAttribute("key", key);
			entry.appendChild(doc.createTextNode(props.getProperty(key)));
		}
		emitDocument(doc, os, encoding);
	}

	static void emitDocument(Document doc, OutputStream os, String encoding)
			throws IOException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, PROPS_DTD_URI);
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.ENCODING, encoding);
		} catch (TransformerConfigurationException tce) {
			// FIXME assert(false);
		}
		DOMSource doms = new DOMSource(doc);
		StreamResult sr = new StreamResult(os);
		try {
			t.transform(doms, sr);
		} catch (TransformerException te) {
			IOException ioe = new IOException();
			ioe.initCause(te);
			throw ioe;
		}
	}

}

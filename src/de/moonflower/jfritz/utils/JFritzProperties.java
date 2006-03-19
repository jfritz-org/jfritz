/*
 * Created on 21.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;

/**
 * Backport of JRE 1.5 Properties class to JRE 1.4.2
 *
 * @author Arno Willig
 *
 */
public class JFritzProperties extends Properties {
	private static final long serialVersionUID = 1;
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

	public synchronized void loadFromXML(String filename) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            reader.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException x) throws SAXException {
                    // Debug.err(x.toString());
                    throw x;
                }

                public void fatalError(SAXParseException x) throws SAXException {
                    // Debug.err(x.toString());
                    throw x;
                }

                public void warning(SAXParseException x) throws SAXException {
                    // Debug.err(x.toString());
                    throw x;
                }
            });
            reader.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId,
                        String systemId) throws SAXException, IOException {
                    if (systemId.equals(PROPS_DTD_URI)) {
                        InputSource is;
                        is = new InputSource(new StringReader(PROPS_DTD));
                        is.setSystemId(PROPS_DTD_URI);
                        return is;
                    }
                    throw new SAXException("Invalid system identifier: "
                            + systemId);
                }

            });
            reader.setContentHandler(new PropertiesXMLHandler());
            reader.parse(new InputSource(new FileInputStream(filename)));

        } catch (ParserConfigurationException e) {
            Debug.err("Error with ParserConfiguration!");
        } catch (SAXException e) {
            Debug.err("Error on parsing " + filename + "!");
            Debug.err(e.toString());
            if (e.getLocalizedMessage().startsWith("Relative URI")
                    || e.getLocalizedMessage().startsWith(
                            "Invalid system identifier")) {
                Debug.err(e.getLocalizedMessage());
                System.exit(0);
            }
        } catch (IOException e) {
            Debug.err("Could not read " + filename + "!");
        }
	}

	public synchronized void storeToXML(String filename)
			throws IOException {
		if (filename == null)
			throw new NullPointerException();
		save(filename);
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

	public void load(Properties props, InputStream in)
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
		dbf.setValidating(false);
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
//				Debug.msg("Load properties: " + entry.getAttribute("key") + " = " + val);
				props.setProperty(entry.getAttribute("key"), val);
			}
		}
	}

    public void save(String filename) throws IOException {
        Debug.msg("Saving to file " + filename);
        try {
                BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "UTF8"));
            pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.newLine();
//          pw.write("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
//                  + "\">");
//          pw.newLine();
            pw.write("<properties>");
            pw.newLine();
            pw.write("<comment>Properties for " + JFritz.PROGRAM_NAME + " v"
                    + JFritz.PROGRAM_VERSION + "</comment>");
            pw.newLine();

            Enumeration en = keys();
            while (en.hasMoreElements()) {
                String element = en.nextElement().toString();
                pw.write("<entry key=\"" + element + "\">"+JFritzUtils.convertSpecialChars(JFritzUtils.deconvertSpecialChars(getProperty(element)))+"</entry>");
                pw.newLine();
            }
            pw.write("</properties>");
            pw.newLine();
            pw.close();
          } catch (UnsupportedEncodingException e) {
              Debug.err("UTF-8 not supported.");
            } catch (FileNotFoundException e) {
                Debug.err("Could not write " + filename + "!");
          } catch (IOException e) {
            Debug.err("IOException " + filename);
        }
    }
}

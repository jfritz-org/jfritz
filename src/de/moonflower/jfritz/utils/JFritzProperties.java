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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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

import de.moonflower.jfritz.Main;

/**
 * Backport of JRE 1.5 Properties class to JRE 1.4.2
 *
 * @author Arno Willig
 *
 */
public class JFritzProperties extends Properties {
	private static final long serialVersionUID = 1;
	private static final String PROPS_DTD_URI = "http://java.sun.com/dtd/properties.dtd"; //$NON-NLS-1$

	private static final String PROPS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //$NON-NLS-1$
			+ "<!-- DTD for properties -->" //$NON-NLS-1$
			+ "<!ELEMENT properties ( comment?, entry* ) >" //$NON-NLS-1$
			+ "<!ATTLIST properties" //$NON-NLS-1$
			+ " version CDATA #FIXED \"1.0\">" //$NON-NLS-1$
			+ "<!ELEMENT comment (#PCDATA) >" //$NON-NLS-1$
			+ "<!ELEMENT entry (#PCDATA) >" //$NON-NLS-1$
			+ "<!ATTLIST entry " + " key CDATA #REQUIRED>"; //$NON-NLS-1$,  //$NON-NLS-2$

	/**
	 * Version number for the format of exported properties files.
	 */
	private static final String EXTERNAL_XML_VERSION = "1.0"; //$NON-NLS-1$

	/**
	 *
	 */
	public JFritzProperties() {
		super();
	}

	public JFritzProperties(JFritzProperties defaultProperties) {
		super(defaultProperties);
	}

	public synchronized void loadFromXML(String filename) throws IOException,
			FileNotFoundException {
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
                    throw new SAXException("Invalid system identifier: " //$NON-NLS-1$
                            + systemId);
                }

            });
            reader.setContentHandler(new PropertiesXMLHandler(this));
            reader.parse(new InputSource(new FileInputStream(filename)));

        } catch (ParserConfigurationException e) {
            Debug.error("Error with ParserConfiguration!"); //$NON-NLS-1$
        } catch (SAXException e) {
            Debug.error("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
            Debug.error(e.toString());
            if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
                    || e.getLocalizedMessage().startsWith(
                            "Invalid system identifier")) { //$NON-NLS-1$
                Debug.error(e.toString());
            Debug.errDlg("Error on parsing " + filename);
            }
        } catch (IOException e) {
            Debug.error("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$

            //make sure that we jfritz.java knows to show the config wizard
            throw new FileNotFoundException();
        }
	}

	public synchronized void storeToXML(String filename)
			throws IOException {
		if (filename == null)
			throw new NullPointerException();
		save(filename);
	}

	public synchronized Object setProperty(String key, String value) {
		return super.setProperty(key, JFritzUtils.convertSpecialChars(JFritzUtils.deconvertSpecialChars(value)));
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
		String xmlVersion = propertiesElement.getAttribute("version"); //$NON-NLS-1$
		if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0)
			throw new SAXException("Exported Properties file format version " //$NON-NLS-1$
					+ xmlVersion
					+ " is not supported. This java installation can read" //$NON-NLS-1$
					+ " versions " + EXTERNAL_XML_VERSION + " or older. You" //$NON-NLS-1$,  //$NON-NLS-2$
					+ " may need to install a newer version of JDK."); //$NON-NLS-1$
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
			throw new SAXException("Invalid system identifier: " + sid); //$NON-NLS-1$
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
				&& entries.item(0).getNodeName().equals("comment") ? 1 : 0; //$NON-NLS-1$
		for (int i = start; i < numEntries; i++) {
			Element entry = (Element) entries.item(i);
			if (entry.hasAttribute("key")) { //$NON-NLS-1$
				Node n = entry.getFirstChild();
				String val = (n == null) ? "" : n.getNodeValue(); //$NON-NLS-1$
				props.setProperty(entry.getAttribute("key"), val); //$NON-NLS-1$
			}
		}
	}

    public void save(String filename) throws IOException {
        Debug.info("Saving to file " + filename); //$NON-NLS-1$
        try {
                BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
            pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
            pw.newLine();
            pw.write("<properties>"); //$NON-NLS-1$
            pw.newLine();
            pw.write("<comment>Properties for " + Main.PROGRAM_NAME + " v" //$NON-NLS-1$,  //$NON-NLS-2$
                    + Main.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$
            pw.newLine();

            Enumeration <Object> keys = keys();
            List <String> elementList = new ArrayList<String>();
            while (keys.hasMoreElements()) {
            elementList.add((String)keys.nextElement());
            }

            Collections.sort(elementList);

            for (int i=0; i<elementList.size(); i++)
            {
            	String element = elementList.get(i);
            	pw.write("<entry key=\"" + element + //$NON-NLS-1$
            			"\">" //$NON-NLS-1$
            			+JFritzUtils.convertSpecialChars(
            					JFritzUtils.deconvertSpecialChars(
            							getProperty(element)))+"</entry>"); //$NON-NLS-1$
            	pw.newLine();
            }
            pw.write("</properties>"); //$NON-NLS-1$
            pw.newLine();
            pw.close();
          } catch (UnsupportedEncodingException e) {
              Debug.error("UTF-8 not supported."); //$NON-NLS-1$
            } catch (FileNotFoundException e) {
                Debug.error("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
          } catch (IOException e) {
            Debug.error("IOException " + filename); //$NON-NLS-1$
        }
    }
}

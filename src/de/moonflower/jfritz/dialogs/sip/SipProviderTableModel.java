/*
 *
 * Created on 18.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Arno Willig
 *
 * TODO: I18N
 */
public class SipProviderTableModel extends AbstractTableModel {

    private static final String SIP_DTD_URI = "http://jfritz.moonflower.de/dtd/sip.dtd";

	private static final String SIP_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		    + "<!-- DTD for JFritz sip provider -->"
			+ "<!ELEMENT provider (commment?,entry*)>"
			+ "<!ELEMENT comment (#PCDATA)>"
			+ "<!ELEMENT name (#PCDATA)>"
			+ "<!ELEMENT number (#PCDATA)>"
			+ "<!ELEMENT active (#PCDATA)>"
			+ "<!ELEMENT entry (name,number,active?)>"
			+ "<!ATTLIST entry id CDATA #REQUIRED>";

    private static final long serialVersionUID = 1;

    private final String columnNames[] = { "ID", "Aktiv", "SIP-Nummer",
            "Provider" };

    private Vector providerList;

    public SipProviderTableModel() {
        super();
        providerList = new Vector();
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return providerList.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        SipProvider sip = (SipProvider) providerList.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return Integer.toString(sip.getProviderID());
        case 1:
            if (sip.isActive())
                return "Ja";
            else
                return "Nein";
        case 2:
            return sip.getNumber();
        case 3:
            return sip.getProvider();
        default:
            return "?";
        }
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * @return Returns the providerList.
     */
    public final Vector getProviderList() {
        return providerList;
    }

    /**
     * @param providerList
     *            The providerList to set.
     */
    public final void setProviderList(Vector providerList) {
        this.providerList = providerList;
    }

    public final void addProvider(SipProvider sip) {
        providerList.add(sip);
    }

	/**
	 * Saves sip provider list to xml file.
	 *
	 * @param filename
	 *            Filename to save to
	 */
	public void saveToXMLFile(String filename) {
		Debug.msg("Saving to file " + filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<!DOCTYPE sipprovider SYSTEM \"" + SIP_DTD_URI + "\">");
			pw.println("<provider>");
			pw.println("<comment>SIP-Provider for " + JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION + "</comment>");

			Enumeration en = providerList.elements();
				while (en.hasMoreElements()) {
					SipProvider provider = (SipProvider) en.nextElement();
					pw.println(provider.toXML());
				}
			pw.println("</provider>");
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!");
		}
	}

	/**
	 * Loads calls from xml file
	 *
	 * @param filename
	 */
	public void loadFromXMLFile(String filename) {
		try {

			// Workaround for SAX parser
			// File dtd = new File("calls.dtd");
			// dtd.deleteOnExit();
			// if (!dtd.exists()) dtd.createNewFile();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false); // FIXME Something wrong with the DTD
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
					if (systemId.equals(SIP_DTD_URI)
							|| systemId.equals("sip.dtd")) {
						InputSource is;
						is = new InputSource(new StringReader(SIP_DTD));
						is.setSystemId(SIP_DTD_URI);
						return is;
					}
					throw new SAXException("Invalid system identifier: "
							+ systemId);
				}

			});

			reader.setContentHandler(new SIPFileXMLHandler(this));
			reader.parse(new InputSource(new FileInputStream(filename)));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!");
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!" + e);
			if (e.getLocalizedMessage().startsWith("Relative URI")
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) {
				Debug.err(e.getLocalizedMessage());
				Debug
						.errDlg("STRUKTURÄNDERUNG!\n\nBitte in der Datei jfritz.sipprovider.xml\n "
								+ "die Zeichenkette \"sip.dtd\" durch\n \""
								+ SIP_DTD_URI + "\"\n ersetzen!");
				System.exit(0);
			}
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!");
		}
	}
}

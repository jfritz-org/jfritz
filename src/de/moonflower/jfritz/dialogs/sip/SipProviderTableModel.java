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
import java.util.Collections;
import java.util.Comparator;
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
     * Updates SIP-Provider list
     * @param newProviderList
     *            The new providerList to update.
     */
    public final void updateProviderList(Vector newProviderList) {
        Vector newProviderVector = new Vector();
        if (providerList.size() == 0) { // Empty providerList
            providerList = newProviderList;
        } else {
            Enumeration en1 = newProviderList.elements(); // neue Provider
            while (en1.hasMoreElements()) {
                SipProvider sip1 = (SipProvider) en1.nextElement();
                boolean found =  false;
                for (int i=0; i < providerList.size(); i++) {
                    SipProvider sip2 = (SipProvider) providerList.get(i);
                    if (sip1.toString().equals(sip2.toString())) {
                        // Provider existiert schon
                        // Active-Status und ProviderID anpassen und zur neuen Liste hinzufügen
                        found = true;
                        sip2.setActive(sip1.isActive());
                        sip2.setProviderID(sip1.getProviderID());
                        newProviderVector.add(sip2);
                    }
                }
                if (!found) {
                    newProviderVector.add(sip1);
                }
            }
            providerList = newProviderVector;
            sortAllRowsBy(0);
        }
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
			sortAllRowsBy(0);

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

	public void sortAllRowsBy(int col) {
	    Collections.sort(providerList, new ColumnSorter(col, true));
		fireTableDataChanged();
	}

	/**
	 * Get phoneNumber and providerName to corresponding providerID
	 * @param sipID p.E. SIP0
	 * @param defaultReturn p.E. SIP0 or 123456
	 * @return Number of SipProvider (123@sipgate.de)
	 */
	public String getSipProvider(String sipID, String defaultReturn) {
	    if (sipID.startsWith("SIP")) {
            Enumeration en = providerList.elements();
            	while (en.hasMoreElements()) {
            	    SipProvider sipProvider = (SipProvider) en.nextElement();
            	    if (sipProvider.getProviderID() == Integer.parseInt(sipID.substring(3)))
            	        return sipProvider.toString();
            	}
            	return defaultReturn; // If SipProvider not found
	    } else return defaultReturn;
	}

	/**
	 * This comparator is used to sort vectors of data
	 */
	public class ColumnSorter implements Comparator {
		int colIndex;

		boolean ascending;

		ColumnSorter(int colIndex, boolean ascending) {
			this.colIndex = colIndex;
			this.ascending = ascending;
		}

		public int compare(Object a, Object b) {
			Object o1, o2;
			SipProvider v1 = (SipProvider) a;
			SipProvider v2 = (SipProvider) b;
			switch (colIndex) {
			case 0:
			    if (v1.getProviderID() > v2.getProviderID()) {
			        return 1;
			    } else return 0;
			default:
			    o1 = null;
				o2 = null;
			}

			// Treat empty strings like nulls
			if (o1 instanceof String && ((String) o1).trim().length() == 0) {
				o1 = null;
			}
			if (o2 instanceof String && ((String) o2).trim().length() == 0) {
				o2 = null;
			}

			// Sort nulls so they appear last, regardless
			// of sort order
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return 1;
			} else if (o2 == null) {
				return -1;
			} else if (o1 instanceof Comparable) {
				if (ascending) {
					return ((Comparable) o1).compareTo(o2);
				} else {
					return ((Comparable) o2).compareTo(o1);
				}
			} else {
				if (ascending) {
					return o1.toString().compareTo(o2.toString());
				} else {
					return o2.toString().compareTo(o1.toString());
				}
			}
		}
	}
}

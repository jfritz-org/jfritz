/*
 * Created on 25.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

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
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.QuickDial;
import de.moonflower.jfritz.utils.Debug;

/**
 * Table model for QuickDials
 *
 * @author Arno Willig
 */
public class QuickDials extends AbstractTableModel {
	private static final long serialVersionUID = 1;
	private static final String QUICKDIALS_DTD_URI = "http://jfritz.moonflower.de/dtd/quickdials.dtd";  //$NON-NLS-1$

	private static final String QUICKDIALS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  //$NON-NLS-1$
			+ "<!-- DTD for JFritz quickdials -->"  //$NON-NLS-1$
			+ "<!ELEMENT quickdials (commment?,entry*)>"  //$NON-NLS-1$
			+ "<!ELEMENT comment (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ELEMENT entry (number?,vanity?,description?)>"  //$NON-NLS-1$
			+ "<!ELEMENT number (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ELEMENT vanity (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ELEMENT description (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ATTLIST entry id CDATA #REQUIRED>";  //$NON-NLS-1$

	Vector<QuickDial> quickDials;

	/**
	 *
	 */
	public QuickDials() {
		super();
		quickDials = new Vector<QuickDial>();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return quickDials.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 4;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		QuickDial quick = quickDials.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return quick.getQuickdial();
		case 1:
			return quick.getVanity();
		case 2:
			return quick.getNumber();
		case 3:
			return quick.getDescription();
		default:
			return null;
		}
	}

	/**
	 * Sets a value to a specific position
	 */
	public void setValueAt(Object object, int rowIndex, int columnIndex) {
		if (rowIndex < getRowCount()) {
			QuickDial dial = quickDials.get(rowIndex);

			switch (columnIndex) {
			case 0:
				dial.setQuickdial(object.toString());
				break;
			case 1:
				dial.setVanity(object.toString());
				break;
			case 2:
				dial.setNumber(object.toString());
				break;
			case 3:
				dial.setDescription(object.toString());
				break;
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return Main.getMessage("quickdial");  //$NON-NLS-1$
		case 1:
			return Main.getMessage("vanity");  //$NON-NLS-1$
		case 2:
			return Main.getMessage("number");  //$NON-NLS-1$
		case 3:
			return Main.getMessage("description");  //$NON-NLS-1$
		default:
			return null;
		}
	}

	public void getQuickDialDataFromFritzBox() {

		if (JFritz.getFritzBox().getFirmware() == null)
			if (JFritz.getFritzBox().checkValidFirmware() == false) {
				return;
			}
		try {
			quickDials = JFritz.getFritzBox().retrieveQuickDialsFromFritzBox(this); //$NON-NLS-1$
			fireTableDataChanged();
		} catch (WrongPasswordException e) {
			Debug.err("getQuickDialData: Wrong password"); //$NON-NLS-1$
			Debug.errDlg(Main.getMessage("wrong_password")); //$NON-NLS-1$
		} catch (IOException e) {
			Debug.err("getQuickDialData: Box not found"); //$NON-NLS-1$
			Debug.errDlg(Main.getMessage("box_address_wrong")); //$NON-NLS-1$
		} catch (InvalidFirmwareException e) {
			Debug.err("getQuickDialData: Invalid firmware"); //$NON-NLS-1$
			Debug.errDlg(Main.getMessage("box_address_wrong")); //$NON-NLS-1$
		}
	}

	public void addEntry(QuickDial quickDial) {
		quickDials.add(quickDial);
	}

	public void remove(int row) {
		quickDials.remove(row);
	}

	/**
	 * Loads quickdial list from xml file
	 *
	 * @param filename
	 */
	public void loadFromXMLFile(String filename) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			XMLReader reader = factory.newSAXParser().getXMLReader();
			reader.setErrorHandler(new ErrorHandler() {
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
			reader.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					if (systemId.equals(QUICKDIALS_DTD_URI)) {
						InputSource is = new InputSource(new StringReader(
								QUICKDIALS_DTD));
						is.setSystemId(QUICKDIALS_DTD_URI);
						return is;
					}
					throw new SAXException("Invalid system identifier: " //$NON-NLS-1$
							+ systemId);
				}

			});
			reader.setContentHandler(new QuickDialXMLHandler(this));
			reader.parse(new InputSource(new FileInputStream(filename)));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
            Debug.err(e.toString());
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	/**
	 * Saves quickdial list to xml file.
	 *
	 * @param filename
	 */
	public void saveToXMLFile(String filename) {
		Debug.msg("Saving to file " + filename); //$NON-NLS-1$
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
//			pw.println("<!DOCTYPE quickdials SYSTEM \"" + QUICKDIALS_DTD_URI
//					+ "\">");
			pw.println("<quickdials>"); //$NON-NLS-1$
			pw.println("\t<comment>QuickDial list for " + Main.PROGRAM_NAME //$NON-NLS-1$
					+ " v" + Main.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$,  //$NON-NLS-2$
			Enumeration<QuickDial> en = quickDials.elements();
			while (en.hasMoreElements()) {
				QuickDial current = en.nextElement();
				pw.println("\t<entry id=\"" + current.getQuickdial() + "\">"); //$NON-NLS-1$,  //$NON-NLS-2$
				if (!current.getNumber().equals("")) //$NON-NLS-1$
					pw.println("\t\t<number>" + current.getNumber() //$NON-NLS-1$
							+ "</number>"); //$NON-NLS-1$
				if (!current.getVanity().equals("")) //$NON-NLS-1$
					pw.println("\t\t<vanity>" + current.getVanity() //$NON-NLS-1$
							+ "</vanity>"); //$NON-NLS-1$
				if (!current.getDescription().equals("")) //$NON-NLS-1$
					pw.println("\t\t<description>" + current.getDescription() //$NON-NLS-1$
							+ "</description>"); //$NON-NLS-1$
				pw.println("\t</entry>"); //$NON-NLS-1$
			}
			pw.println("</quickdials>"); //$NON-NLS-1$
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	public String getDescriptionFromNumber(String number) {
		Enumeration<QuickDial> en = quickDials.elements();
		while (en.hasMoreElements()) {
			QuickDial q = en.nextElement();
			if (q.getNumber().equals(number))
				return q.getDescription();
		}
		return null;
	}

	/**
	 * @return Returns the quickDials.
	 */
	public final Vector<QuickDial> getQuickDials() {
		return quickDials;
	}
}

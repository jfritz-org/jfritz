/*
 * Created on 25.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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

import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.struct.QuickDial;
import de.moonflower.jfritz.utils.Debug;
import de.robotniko.fboxlib.fritzbox.FritzBoxCommunication;

/**
 * Table model for QuickDials
 *
 * @author Arno Willig
 */
public class QuickDials extends AbstractTableModel {
	private static final long serialVersionUID = 1;
	private static final String QUICKDIALS_DTD_URI = "http://jfritz.moonflower.de/dtd/quickdials.dtd";  //$NON-NLS-1$
	protected MessageProvider messages = MessageProvider.getInstance();

	private static final String QUICKDIALS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  //$NON-NLS-1$
			+ "<!-- DTD for JFritz quickdials -->"  //$NON-NLS-1$
			+ "<!ELEMENT quickdials (commment?,entry*)>"  //$NON-NLS-1$
			+ "<!ELEMENT comment (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ELEMENT entry (number?,vanity?,description?)>"  //$NON-NLS-1$
			+ "<!ELEMENT number (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ELEMENT vanity (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ELEMENT description (#PCDATA)>"  //$NON-NLS-1$
			+ "<!ATTLIST entry id CDATA #REQUIRED>";  //$NON-NLS-1$

	//private static FirmwareVersion firmware = new FirmwareVersion(); // 23.08.2015
	private static FritzBoxCommunication firmware = new FritzBoxCommunication(); // 23.08.2015
//	private static QuickDialUpnp quickDialUpnp = new QuickDialUpnp(); // 23.08.2015

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
			return messages.getMessage("quickdial");  //$NON-NLS-1$
		case 1:
			return messages.getMessage("vanity");  //$NON-NLS-1$
		case 2:
			return messages.getMessage("number");  //$NON-NLS-1$
		case 3:
			return messages.getMessage("description");  //$NON-NLS-1$
		default:
			return null;
		}
	}

	public void getQuickDialDataFromFritzBox() {// throws WrongPasswordException, IOException, InvalidFirmwareException {

		// 23.08.2015
//		if ((firmware != null) && firmware.isUpperThan(5, 49)) {
//			Debug.info("getQuickDialDataFromFritzBox() in QuickDials! " + firmware.isUpperThan(5, 49) + "   " + firmware.getMajor() + "   " + firmware.getMinor());
//			quickDials = quickDialUpnp.retrieveQuickDialsFromFritzBox(this);
//			fireTableDataChanged();
//		} else {
//			Debug.info("getQuickDialDataFromFritzBox() in QuickDials! " + firmware.isLowerThan(5, 50) + "   " + firmware.getMajor() + "   " + firmware.getMinor());
//			Debug.error("Fix getQuickDialDataFromFritzBox() in QuickDials!");
//		if (JFritz.getFritzBox().getFirmware() == null)
//			if (JFritz.getFritzBox().checkValidFirmware()) {
//				quickDials = JFritz.getFritzBox().retrieveQuickDialsFromFritzBox(this); //$NON-NLS-1$
//				fireTableDataChanged();
//			}
//		}
//		}
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
			Debug.error("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.error("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
            Debug.error(e.toString());
		} catch (IOException e) {
			Debug.error("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	/**
	 * Saves quickdial list to xml file.
	 *
	 * @param filename
	 */
	public void saveToXMLFile(String filename) {
		Debug.info("Saving to file " + filename); //$NON-NLS-1$
		//FileOutputStream fos; // nur ASCII
		//fos = new FileOutputStream(filename);
		//Writer fos = null; // 23.08.2015 UTF-8
		//BufferedWriter fos = null; // 23.08.2015 UTF-8
		//fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")); // 23.08.2015

		BufferedWriter fos = null; // 23.08.2015
		try {
			fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")); // 23.08.2015
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
//			pw.println("<!DOCTYPE quickdials SYSTEM \"" + QUICKDIALS_DTD_URI
//					+ "\">");
			pw.println("<quickdials>"); //$NON-NLS-1$
			pw.println("\t<comment>QuickDial list for " + ProgramConstants.PROGRAM_NAME //$NON-NLS-1$
					+ " v" + ProgramConstants.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$,  //$NON-NLS-2$
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
				if (!current.getDescription().equals(""))
					pw.println("\t\t<description>" + current.getDescription() //$NON-NLS-1$
								+ "</description>");
				pw.println("\t</entry>"); //$NON-NLS-1$
			}
			pw.println("</quickdials>"); //$NON-NLS-1$
			pw.close();
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

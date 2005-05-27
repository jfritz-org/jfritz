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
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Arno Willig
 *
 */
public class QuickDialTableModel extends AbstractTableModel {

	private static final String QUICKDIALS_DTD_URI = "http://jfritz.moonflower.de/dtd/quickdials.dtd";

	private static final String QUICKDIALS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!-- DTD for JFritz quickdials -->"
			+ "<!ELEMENT quickdials (commment?,entry*)>"
			+ "<!ELEMENT comment (#PCDATA)>"
			+ "<!ELEMENT entry (number?,vanity?,description?)>"
			+ "<!ELEMENT number (#PCDATA)>"
			+ "<!ELEMENT vanity (#PCDATA)>"
			+ "<!ELEMENT description (#PCDATA)>"
			+ "<!ATTLIST entry id CDATA #REQUIRED>";

	JFritz jfritz;

	Vector modelData;

	/**
	 *
	 */
	public QuickDialTableModel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		modelData = new Vector();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return modelData.size();
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
		QuickDial quick = (QuickDial) modelData.get(rowIndex);
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
			QuickDial dial = (QuickDial) modelData.get(rowIndex);

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
			return jfritz.getMessages().getString("quickdial");
		case 1:
			return jfritz.getMessages().getString("vanity");
		case 2:
			return jfritz.getMessages().getString("number");
		case 3:
			return jfritz.getMessages().getString("description");
		default:
			return null;
		}
	}

	public void getQuickDialDataFromFritzBox() {
		try {//FIXME
			modelData = JFritzUtils.retrieveQuickDialsFromFritzBox(this, jfritz
					.getProperties().getProperty("box.address"), jfritz
					.getProperties().getProperty("box.password"), JFritzUtils
					.detectBoxType(jfritz.getProperties().getProperty(
							"box.firmware"), jfritz.getProperties()
							.getProperty("box.address"), jfritz.getProperties()
							.getProperty("box.password")));
		} catch (WrongPasswordException e) {
			Debug.err("getQuickDialData: Wrong password");
		} catch (IOException e) {
			Debug.err("getQuickDialData: Box not found");
		}
	}

	public void addEntry(QuickDial quickDial) {
		modelData.add(quickDial);
	}

	public void remove(int row) {
		modelData.remove(row);
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
					throw new SAXException("Invalid system identifier: "
							+ systemId);
				}

			});
			reader.setContentHandler(new QuickDialXMLHandler(this));
			reader.parse(new InputSource(new FileInputStream(filename)));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!");
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!");
			e.printStackTrace();
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!");
		}
	}

	/**
	 * Saves quickdial list to xml file.
	 *
	 * @param filename
	 */
	public void saveToXMLFile(String filename) {
		Debug.msg("Saving to file " + filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<!DOCTYPE quickdials SYSTEM \"" + QUICKDIALS_DTD_URI
					+ "\">");
			pw.println("<quickdials>");
			pw.println("\t<comment>QuickDial list for " + JFritz.PROGRAM_NAME
					+ " v" + JFritz.PROGRAM_VERSION + "</comment>");
			Enumeration en = modelData.elements();
			while (en.hasMoreElements()) {
				QuickDial current = (QuickDial) en.nextElement();
				pw.println("\t<entry id=\"" + current.getQuickdial() + "\">");
				if (current.getNumber() != "")
					pw.println("\t\t<number>" + current.getNumber()
							+ "</number>");
				if (current.getVanity() != "")
					pw.println("\t\t<vanity>" + current.getVanity()
							+ "</vanity>");
				if (current.getDescription() != "")
					pw.println("\t\t<description>" + current.getDescription()
							+ "</description>");
				pw.println("\t</entry>");
			}
			pw.println("</quickdials>");
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!");
		}
	}

	public String getDescriptionFromNumber(String number) {
		Enumeration en = modelData.elements();
		while (en.hasMoreElements()) {
			QuickDial q = (QuickDial) en.nextElement();
			if (q.getNumber().equals(number))
				return q.getDescription();
		}
		return null;
	}
}

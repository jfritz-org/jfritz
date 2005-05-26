/*
 * Created on 25.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

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
			+ "<!ELEMENT number (#PCDATA)>"
			+ "<!ELEMENT vanity (#PCDATA)>"
			+ "<!ELEMENT description (#PCDATA)>"
			+ "<!ELEMENT entry (number?,vanity?,description?)>"
			+ "<!ATTLIST entry id CDATA #REQUIRED>";

	JFritz jfritz;

	Vector quickDialData;

	/**
	 *
	 */
	public QuickDialTableModel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return quickDialData.size();
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
		QuickDial quick = (QuickDial) quickDialData.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return quick.getDescription();
		case 1:
			return quick.getQuickdial();
		case 2:
			return quick.getVanity();
		case 3:
			return quick.getNumber();
		default:
			return null;
		}
	}

	/**
	 * Sets a value to a specific position
	 */
	public void setValueAt(Object object, int rowIndex, int columnIndex) {
		if (rowIndex < getRowCount()) {
			QuickDial dial = (QuickDial) quickDialData.get(rowIndex);

			switch (columnIndex) {
			case 0:
				dial.setDescription(object.toString());
				break;
			case 1:
				dial.setQuickdial(object.toString());
				break;
			case 2:
				dial.setVanity(object.toString());
				break;
			case 3:
				dial.setNumber(object.toString());
				break;
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return jfritz.getMessages().getString("description");
		case 1:
			return jfritz.getMessages().getString("quickdial");
		case 2:
			return jfritz.getMessages().getString("vanity");
		case 3:
			return jfritz.getMessages().getString("number");
		default:
			return null;
		}
	}

	public void getQuickDialDataFromFritzBox() {
		try {
			quickDialData = JFritzUtils.retrieveQuickDialsFromFritzBox(jfritz
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

	/**
	 * Saves phonebook to xml file.
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
			Enumeration en = quickDialData.elements();
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

}

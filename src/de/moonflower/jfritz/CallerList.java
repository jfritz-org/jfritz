/*
 *
 * Created on 08.04.2005
 *
 */
package de.moonflower.jfritz;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
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

/**
 * This class manages the caller list.
 *
 * @author Arno Willig
 */
public class CallerList extends AbstractTableModel {

	private static final String CALLS_DTD_URI = "http://jfritz.moonflower.de/dtd/calls.dtd";

	private static final String CALLS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!-- DTD for JFritz calls -->"
			+ "<!ELEMENT calls (commment?,entry*)>"
			+ "<!ELEMENT comment (#PCDATA)>"
			+ "<!ELEMENT date (#PCDATA)>"
			+ "<!ELEMENT caller (#PCDATA)>"
			+ "<!ELEMENT port (#PCDATA)>"
			+ "<!ELEMENT route (#PCDATA)>"
			+ "<!ELEMENT duration (#PCDATA)>"
			+ "<!ELEMENT entry (date,caller?,port?,route?,duration?)>"
			+ "<!ATTLIST entry calltype (call_in|call_in_failed|call_out) #REQUIRED>";

	private JFritzProperties properties, participants;

	private JFritzWindow jframe;

	private Vector callerdata;

	private Vector unfilteredcallerdata;

	public Vector getCallVector() {
		return unfilteredcallerdata;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (!((Call) unfilteredcallerdata.get(rowIndex)).getNumber()
				.equals("") && (columnIndex == 3));
	}

	public Class getColumnClass(int columnIndex) {
		Object o = getValueAt(0, columnIndex);
		if (o == null) {
			return Object.class;
		} else {
			return o.getClass();
		}

	}

	/**
	 * CallerList Constructor
	 *
	 */
	public CallerList() {
		callerdata = new Vector();
		unfilteredcallerdata = new Vector();
	}

	/**
	 * CallerList Constructor
	 *
	 * @param properties
	 * @param participants
	 */
	public CallerList(JFritzProperties properties, JFritzProperties participants) {
		this();
		setProperties(properties, participants);
	}

	public void setProperties(JFritzProperties properties,
			JFritzProperties participants) {
		this.properties = properties;
		this.participants = participants;
	}

	/**
	 * Save caller list to xml file.
	 *
	 */
	public void saveToXMLFile() {
		Debug.msg("Saving to file " + JFritz.CALLS_FILE);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(JFritz.CALLS_FILE);
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<!DOCTYPE calls SYSTEM \"" + CALLS_DTD_URI + "\">");
			pw.println("<calls>");
			pw.println("<comment>Calls for " + JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION + "</comment>");
			Enumeration en = unfilteredcallerdata.elements();
			while (en.hasMoreElements()) {
				Call call = (Call) en.nextElement();
				CallType type = call.getCalltype();
				Date datum = call.getCalldate();
				String caller = call.getNumber();
				String port = call.getPort();
				String route = call.getRoute();
				int duration = call.getDuration();
				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				pw.println("<entry calltype=\"" + type.toString() + "\">");
				pw.println("\t<date>" + df.format(datum) + "</date>");
				if (!caller.equals(""))
					pw.println("\t<caller>" + caller + "</caller>");
				if (!port.equals(""))
					pw.println("\t<port>" + port + "</port>");
				if (!route.equals(""))
					pw.println("\t<route>" + route + "</route>");
				if (duration > 0)
					pw.println("\t<duration>" + duration + "</duration>");
				pw.println("</entry>");
			}
			pw.println("</calls>");
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + JFritz.CALLS_FILE + "!");
		}
	}

	/**
	 * Saves callerlist to csv file
	 *
	 * @param filename
	 */
	public void saveToCSVFile(String filename) {
		Debug.msg("Saving to csv file " + filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);

			Enumeration en = getCallVector().elements();
			while (en.hasMoreElements()) {
				Call call = (Call) en.nextElement();
				pw.println(call.toCSV());
			}
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!");
		}

	}

	/**
	 * Loads calls from xml file
	 *
	 */
	public void loadFromXMLFile() {
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
					if (systemId.equals(CALLS_DTD_URI)
							|| systemId.equals("calls.dtd")) {
						InputSource is;
						is = new InputSource(new StringReader(CALLS_DTD));
						is.setSystemId(CALLS_DTD_URI);
						return is;
					}
					throw new SAXException("Invalid system identifier: "
							+ systemId);
				}

			});
			reader.setContentHandler(new CallFileXMLHandler(this));
			reader
					.parse(new InputSource(new FileInputStream(
							JFritz.CALLS_FILE)));
			// parser.parse(new File(JFritz.CALLS_FILE),new
			// CallFileXMLHandler(this));
			updateFilter();

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!");
		} catch (SAXException e) {
			Debug.err("Error on parsing " + JFritz.CALLS_FILE + "!");
			if (e.getLocalizedMessage().startsWith("Relative URI")
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) {
				Debug.err(e.getLocalizedMessage());
				Debug
						.errDlg("STRUKTURÄNDERUNG!\n\nBitte in der Datei jfritz.calls.xml\n "
								+ "die Zeichenkette \"calls.dtd\" durch\n \""
								+ CALLS_DTD_URI + "\"\n ersetzen!");
				System.exit(0);
			}
		} catch (IOException e) {
			Debug.err("Could not read " + JFritz.CALLS_FILE + "!");
		}
	}

	/**
	 * @param number
	 *            of participant
	 * @return Returns name of participant
	 */
	public String getParticipantFromNumber(String number) {
		String areanumber = JFritzUtils.create_area_number(number, properties
				.getProperty("country.prefix"), properties
				.getProperty("country.code"), properties
				.getProperty("area.prefix"), properties
				.getProperty("area.code"));
		return participants.getProperty(areanumber, "");
	}

	/**
	 * Removes all duplicate whitespaces from inputStr
	 *
	 * @param inputStr
	 * @return outputStr
	 */
	public static String removeDuplicateWhitespace(String inputStr) {
		Pattern p = Pattern.compile("\\s+");
		Matcher matcher = p.matcher(inputStr);
		return matcher.replaceAll(" ");
	}

	/**
	 * Adds new Call to CallerList
	 *
	 * @param symbol
	 * @param datum
	 * @param number
	 * @param port
	 * @param route
	 * @param duration
	 */
	public boolean addEntry(CallType symbol, Date datum, String number,
			String port, String route, int duration) {
		Call call = new Call(symbol, datum, number, port, route, duration);
		return addEntry(call);
	}

	/**
	 * adds new Call to CallerList
	 *
	 * @param call
	 */
	public boolean addEntry(Call call) {
		boolean newEntry = true;

		for (Enumeration el = getCallVector().elements(); el.hasMoreElements();) {

			Date d = ((Call) el.nextElement()).getCalldate();
			if (d.equals(call.getCalldate())) { // We already have this call
				newEntry = false;
				break;
			}
		}

		if (newEntry) { // Add new entry to table model
			unfilteredcallerdata.add(call);
		}
		return newEntry;
	}

	/**
	 * Retrieves data from FRITZ!Box
	 *
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public void getNewData() throws WrongPasswordException, IOException {
		Vector data = JFritzUtils.retrieveCallersFromFritzBox(properties
				.getProperty("box.address"), properties
				.getProperty("box.password"), properties
				.getProperty("country.prefix"), properties
				.getProperty("country.code"), properties
				.getProperty("area.prefix"), properties
				.getProperty("area.code"), JFritzUtils.detectBoxType(properties
				.getProperty("box.firmware"), properties
				.getProperty("box.address"), properties
				.getProperty("box.password")));

		int newEntries = 0;
		for (Enumeration el = data.elements(); el.hasMoreElements();) {
			boolean newEntry = addEntry((Call) el.nextElement());
			if (newEntry)
				newEntries++;
		}

		saveToXMLFile();

		// Notify user?
		if (properties.getProperty("option.notifyOnCalls", "false").equals(
				"true")
				&& (newEntries > 0)) {
			Debug.msg(newEntries + " new calls retrieved!");
			// TODO: I18N
			if (newEntries == 1) {
				JOptionPane.showMessageDialog(null,
						"Ein neuer Anruf empfangen!");
			} else {
				JOptionPane.showMessageDialog(null, newEntries
						+ " neue Anrufe empfangen!");
			}

		}
		// Clear data on fritz box ?
		if (properties.getProperty("option.deleteAfterFetch", "false").equals(
				"true")) {
			JFritzUtils.clearListOnFritzBox(properties
					.getProperty("box.address"), properties
					.getProperty("box.password"), JFritzUtils.detectBoxType(
					properties.getProperty("box.firmware"), properties
							.getProperty("box.address"), properties
							.getProperty("box.password")));
		}

	}

	/**
	 * returns number of rows in CallerList
	 *
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return callerdata.size();
	}

	/**
	 * returns number of columns of a call
	 *
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		// 6 Columns on the Table
		return 7;
	}

	/**
	 * returns the value at a specific position
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Call call = (Call) callerdata.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return call.getCalltype();
		case 1:
			return call.getCalldate();
		case 2:
			return JFritzUtils.create_area_number(call.getNumber(), properties
					.getProperty("country.prefix"), properties
					.getProperty("country.code"), properties
					.getProperty("area.prefix"), properties
					.getProperty("area.code"));
		case 3:
			return getParticipantFromNumber(call.getNumber());
		case 4:
			return call.getPort();
		case 5:
			if (call.getRoute().startsWith("SIP")) {
				String sipstr = properties.getProperty(call.getRoute());
				if (sipstr != null) {
					return sipstr;
				} else
					return call.getRoute();
			}
			return call.getRoute();
		case 6:
			return Integer.toString(call.getDuration());
		default:
			throw new IllegalArgumentException("Invalid column: " + columnIndex);
		}
	}

	/**
	 * Sets a value to a specific position
	 */
	public void setValueAt(Object object, int rowIndex, int columnIndex) {
		Call call = (Call) callerdata.get(rowIndex);
		if (columnIndex == 3) {
			setParticipant(object.toString(), rowIndex);
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/**
	 * Sets the participant of a specifix row
	 *
	 * @param participant
	 * @param rowIndex
	 */
	public void setParticipant(String participant, int rowIndex) {
		Call call = (Call) callerdata.get(rowIndex);
		if (!call.getNumber().equals("")) { // no empty numbers
			if (participant.equals("")) {
				participants.remove(call.getNumber());
			} else {
				participants.setProperty(call.getNumber(), participant);
			}
			fireTableCellUpdated(rowIndex, 3);
			fireTableStructureChanged();
		}
	}

	/**
	 * Sort table model rows by a specific column
	 *
	 * @param colIndex
	 *            Index of column to be sorted by
	 * @param ascending
	 *            Order of sorting
	 */
	public void sortAllRowsBy(int colIndex, boolean ascending) {
		Collections.sort(callerdata, new ColumnSorter(colIndex, ascending));
		fireTableStructureChanged();
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
			Call v1 = (Call) a;
			Call v2 = (Call) b;
			Object o1 = v1.getCalldate();
			Object o2 = v2.getCalldate();

			// Treat empty strains like nulls
			if (o1 instanceof String && ((String) o1).length() == 0) {
				o1 = null;
			}
			if (o2 instanceof String && ((String) o2).length() == 0) {
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

	// ************************************************************************************************************
	/**
	 * Update the call filter.
	 */
	public void updateFilter() {
		boolean filterCallIn = Boolean.parseBoolean(properties
				.getProperty("filter.callin"));
		boolean filterCallInFailed = Boolean.parseBoolean(properties
				.getProperty("filter.callinfailed"));
		boolean filterCallOut = Boolean.parseBoolean(properties
				.getProperty("filter.callout"));

		Debug.msg(3, "CallTypeFilter: " + filterCallIn + "|"
				+ filterCallInFailed + "|" + filterCallOut);

		if ((!filterCallIn) && (!filterCallInFailed) && (!filterCallOut))
			callerdata = unfilteredcallerdata;
		else {
			Enumeration en = unfilteredcallerdata.elements();
			Vector filteredcallerdata;
			filteredcallerdata = new Vector();
			while (en.hasMoreElements()) {
				Call call = (Call) en.nextElement();
				if ((!filterCallIn)
						&& (call.getCalltype().toInt() == CallType.CALLIN))
					filteredcallerdata.add(call);
				if ((!filterCallInFailed)
						&& (call.getCalltype().toInt() == CallType.CALLIN_FAILED))
					filteredcallerdata.add(call);
				if ((!filterCallOut)
						&& (call.getCalltype().toInt() == CallType.CALLOUT))
					filteredcallerdata.add(call);
			}
			callerdata = filteredcallerdata;
		}
		if (jframe != null) jframe.setStatus("");
	}
	/**
	 * @param jfritzwindow The jframe to set.
	 */
	public final void setJFritzWindow(JFritzWindow jfritzwindow) {
		this.jframe = jfritzwindow;
	}
}

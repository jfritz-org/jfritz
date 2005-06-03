/*
 *
 * Created on 08.04.2005
 *
 */
package de.moonflower.jfritz.window;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

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

	private JFritz jfritz;

	private Vector filteredCallerData;

	private Vector unfilteredCallerData;

	private int sortColumn = 1;

	private boolean sortDirection = false;

	/**
	 * CallerList Constructor
	 *
	 * @param jfritz
	 */
	public CallerList(JFritz jfritz) {
		filteredCallerData = new Vector();
		unfilteredCallerData = new Vector();
		this.jfritz = jfritz;
	}

	/**
	 *
	 * @return Unfiltered Vector of Calls
	 */
	public Vector getUnfilteredCallVector() {
		return unfilteredCallerData;
	}

	/**
	 *
	 * @return Filtered Vector of Calls
	 */
	public Vector getFilteredCallVector() {
		return filteredCallerData;
	}

	/**
	 *
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (!((Call) filteredCallerData.get(rowIndex)).getPhoneNumber()
				.equals("") && (columnIndex == 3));
	}

	/**
	 * @param columnIndex
	 * @return class of column
	 */
	public Class getColumnClass(int columnIndex) {
		Object o = getValueAt(0, columnIndex);
		if (o == null) {
			return Object.class;
		} else {
			return o.getClass();
		}

	}

	/**
	 * Saves caller list to xml file.
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
			pw.println("<!DOCTYPE calls SYSTEM \"" + CALLS_DTD_URI + "\">");
			pw.println("<calls>");
			pw.println("<comment>Calls for " + JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION + "</comment>");
			Enumeration en = unfilteredCallerData.elements();
			while (en.hasMoreElements()) {
				Call call = (Call) en.nextElement();
				CallType type = call.getCalltype();
				Date datum = call.getCalldate();
				PhoneNumber caller = call.getPhoneNumber();
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
			Debug.err("Could not write " + filename + "!");
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

			Enumeration en = getUnfilteredCallVector().elements();
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
			reader.parse(new InputSource(new FileInputStream(filename)));
			sortAllUnfilteredRows();

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!");
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!");
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
			Debug.err("Could not read " + filename + "!");
		}
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
	public boolean addEntry(CallType symbol, Date datum, PhoneNumber number,
			String port, String route, int duration) {
		Call call = new Call(jfritz, symbol, datum, number, port, route,
				duration);
		return addEntry(call);
	}

	/**
	 * adds new Call to CallerList
	 *
	 * @param call
	 */
	public boolean addEntry(Call call) {
		boolean newEntry = true;
		Enumeration en = getUnfilteredCallVector().elements();
		while (en.hasMoreElements()) {
			Call c = (Call) en.nextElement();
			if (c.getCalldate().equals(call.getCalldate())
					&& (c.getPhoneNumber().getNumber().equals(call
							.getPhoneNumber().getNumber()))) {
				// We already have this call
				newEntry = false;
				break;
			}
		}

		if (newEntry) { // Add new entry to table model
			unfilteredCallerData.add(call);
		}
		return newEntry;
	}

	/**
	 * Retrieves data from FRITZ!Box
	 *
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public void getNewCalls() throws WrongPasswordException, IOException {
		Vector data = JFritzUtils.retrieveCallersFromFritzBox(jfritz
				.getProperties().getProperty("box.address"), jfritz
				.getProperties().getProperty("box.password"), jfritz
				.getProperties().getProperty("country.prefix"), jfritz
				.getProperties().getProperty("country.code"), jfritz
				.getProperties().getProperty("area.prefix"), jfritz
				.getProperties().getProperty("area.code"), JFritzUtils
				.detectBoxType(jfritz.getProperties().getProperty(
						"box.firmware"), jfritz.getProperties().getProperty(
						"box.address"), jfritz.getProperties().getProperty(
						"box.password")), jfritz);

		int newEntries = 0;
		for (Enumeration el = data.elements(); el.hasMoreElements();) {
			boolean newEntry = addEntry((Call) el.nextElement());
			if (newEntry)
				newEntries++;
		}

		sortAllUnfilteredRows();
		saveToXMLFile(JFritz.CALLS_FILE);

		// Notify user?
		if ((jfritz.getProperties().getProperty("option.notifyOnCalls", "true")
				.equals("true"))
				&& (newEntries > 0)) {
			jfritz.getJframe().setVisible(true);
			jfritz.getJframe().toFront();
		}
		if (newEntries > 0) {
			Debug.msg(newEntries + " new calls retrieved!");
			String msg;
			// TODO: I18N
			if (newEntries == 1) {
				msg = "Ein neuer Anruf empfangen!";
			} else {
				msg = newEntries + " neue Anrufe empfangen!";
			}
			jfritz.infoMsg(msg);

		}
		// Clear data on fritz box ?
		if (jfritz.getProperties().getProperty("option.deleteAfterFetch",
				"false").equals("true")) {
			JFritzUtils.clearListOnFritzBox(jfritz.getProperties().getProperty(
					"box.address"), jfritz.getProperties().getProperty(
					"box.password"), JFritzUtils.detectBoxType(jfritz
					.getProperties().getProperty("box.firmware"), jfritz
					.getProperties().getProperty("box.address"), jfritz
					.getProperties().getProperty("box.password")));
		}

	}

	/**
	 * returns number of rows in CallerList
	 *
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return filteredCallerData.size();
	}

	/**
	 * returns number of columns of a call
	 *
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		// 7 Columns on the Table
		return 7;
	}

	/**
	 * @param rowIndex
	 * @param columnIndex
	 * @return the value at a specific position
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Call call = (Call) filteredCallerData.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return call.getCalltype();
		case 1:
			return call.getCalldate();
		case 2:
			return call.getPhoneNumber();
		case 3:
			return call.getPerson();
		case 4:
			return call.getPort();
		case 5:
			if (call.getRoute().startsWith("SIP")) {
				String sipstr = jfritz.getProperties().getProperty(
						call.getRoute());
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
		Call call = (Call) filteredCallerData.get(rowIndex);
		if (columnIndex == 3) {
			Person p = new Person("", "", object.toString());
			setPerson(p, rowIndex);
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/**
	 * Sets the participant of a specifix row
	 *
	 * @param participant
	 * @param rowIndex
	 */
	public void setParticipant2(String participant, int rowIndex) {
		Call call = (Call) filteredCallerData.get(rowIndex);

		if (!call.getPhoneNumber().equals("")) { // no empty numbers
			if (participant.equals("")) {
				Debug.err("REMOVING PARTICIPANT");

				// FIXME jfritz.getParticipants().remove(call.getPhoneNumber());
			} else {
				Debug.err("SETTING PARTICIPANT: " + participant);
				// FIXME
				// jfritz.getParticipants().setProperty(call.getPhoneNumber().getNumber(),
				// participant);
			}
			fireTableCellUpdated(rowIndex, 3);
			fireTableStructureChanged();
		}
	}

	public void setPerson(Person person, int rowIndex) {
		Call call = (Call) filteredCallerData.get(rowIndex);

		if (call.getPhoneNumber() != null) { // no empty numbers
			if (person == null) {
				Debug.err("REMOVING PERSON");
				// FIXME
				// jfritz.getPhonebook().removeEntry(call.getPhoneNumber());
			} else {
				Debug.err("SETTING PERSON");
				jfritz.getPhonebook().addEntry(person);
			}
			fireTableCellUpdated(rowIndex, 3);
			fireTableStructureChanged();
		}

	}

	/**
	 * Sort table model rows by a specific column and direction
	 *
	 * @param col
	 *            Index of column to be sorted by
	 * @param asc
	 *            Order of sorting
	 */
	public void sortAllFilteredRowsBy(int col, boolean asc) {
		Debug.msg("Sorting column " + col + " " + asc);
		Collections.sort(filteredCallerData, new ColumnSorter(col, asc));
		fireTableStructureChanged();
	}

	/**
	 * Sort table model rows by a specific column. The direction is determined
	 * automatically.
	 *
	 * @param col
	 *            Index of column to be sorted by
	 */
	public void sortAllFilteredRowsBy(int col) {
		if ((sortColumn == col) && (sortDirection == false)) {
			sortDirection = true;
		} else {
			sortColumn = col;
			sortDirection = false;
		}
		sortAllFilteredRowsBy(sortColumn, sortDirection);
	}

	public void sortAllUnfilteredRows() {
		Debug.msg("Sorting unfiltered data");
		Collections.sort(unfilteredCallerData, new ColumnSorter(1, false));
		// Resort filtered data
		Collections.sort(filteredCallerData, new ColumnSorter(sortColumn,
				sortDirection));
		updateFilter();
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
			Object o1, o2;
			Call v1 = (Call) a;
			Call v2 = (Call) b;
			switch (colIndex) {
			case 0:
				o1 = v1.getCalltype().toString();
				o2 = v2.getCalltype().toString();
				break;
			case 2:
				o1 = v1.getPhoneNumber().getNumber();
				o2 = v2.getPhoneNumber().getNumber();
				break;
			case 3:
				o1 = v1.getPerson().getFullname();
				o2 = v2.getPerson().getFullname();
				break;
			case 4:
				o1 = v1.getPort();
				o2 = v2.getPort();
				break;
			case 5:
				o1 = v1.getRoute();
				o2 = v2.getRoute();
				break;
			case 6:
				o1 = format(Integer.toString(v1.getDuration()), 10);
				o2 = format(Integer.toString(v2.getDuration()), 10);
				break;
			default:
				o1 = v1.getCalldate();
				o2 = v2.getCalldate();
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

		public String format(String s, int places) {
			int j = places - s.length();
			if (j > 0) {
				StringBuffer sb = null;
				sb = new StringBuffer(j);
				for (int k = 0; k < j; k++)
					sb.append(' ');
				return sb.toString() + s;
			} else
				return s;
		}
	}

	// ************************************************************************************************************
	/**
	 * Updates the call filter.
	 */
	public void updateFilter() {
		boolean filterCallIn = JFritzUtils.parseBoolean(jfritz.getProperties()
				.getProperty("filter.callin"));
		boolean filterCallInFailed = JFritzUtils.parseBoolean(jfritz
				.getProperties().getProperty("filter.callinfailed"));
		boolean filterCallOut = JFritzUtils.parseBoolean(jfritz.getProperties()
				.getProperty("filter.callout"));
		boolean filterNumber = JFritzUtils.parseBoolean(jfritz.getProperties()
				.getProperty("filter.number"));
		boolean filterHandy = JFritzUtils.parseBoolean(jfritz.getProperties()
				.getProperty("filter.handy"));
		boolean filterDate = JFritzUtils.parseBoolean(jfritz.getProperties()
				.getProperty("filter.date"));
		String filterSearch = jfritz.getProperties().getProperty(
				"filter.search", "");
		String filterDateFrom = jfritz.getProperties().getProperty(
				"filter.date_from", "");
		String filterDateTo = jfritz.getProperties().getProperty(
				"filter.date_to", "");

		Debug
				.msg(3, "CallTypeFilter: " + filterCallIn + "|"
						+ filterCallInFailed + "|" + filterCallOut + "|"
						+ filterSearch);

		try {
			jfritz.getJframe().getCallertable().getCellEditor()
					.cancelCellEditing();
		} catch (NullPointerException e) {
		}

		if ((!filterCallIn) && (!filterCallInFailed) && (!filterCallOut)
				&& (!filterNumber) && (!filterDate) && (!filterHandy)
				&& (filterSearch.length() == 0)) {
			// Use unfiltered data
			filteredCallerData = unfilteredCallerData;
			sortAllFilteredRowsBy(sortColumn, sortDirection);
		} else { // Data got to be filtered
			Enumeration en = unfilteredCallerData.elements();
			Vector filteredcallerdata;
			filteredcallerdata = new Vector();
			while (en.hasMoreElements()) {
				Call call = (Call) en.nextElement();
				boolean dateFilterPassed = true;
				boolean searchFilterPassed = true;
				boolean handyFilterPassed = true;

				// SearchFilter: Number, Participant, Date
				String parts[] = filterSearch.split(" ");
				for (int i = 0; i < parts.length; i++) {
					String part = parts[i];
					if (part.length() > 0
							&& call.getPhoneNumber().getNumber().indexOf(
									parts[i]) == -1
							&& call.getPerson().getFullname().toLowerCase()
									.indexOf(part.toLowerCase()) == -1) {
						searchFilterPassed = false;
						break;
					}
				}

				try {
					if (filterDate
							&& !(call.getCalldate().after(
									new SimpleDateFormat("dd.MM.yy")
											.parse(filterDateFrom)) && call
									.getCalldate().before(
											new SimpleDateFormat(
													"dd.MM.yy HH:mm")
													.parse(filterDateTo
															+ " 23:59")))) {
						dateFilterPassed = false;
					}
				} catch (ParseException e1) {
				}

				if (filterHandy && (call.isMobileCall()))
					handyFilterPassed = false;

				if (searchFilterPassed && dateFilterPassed && handyFilterPassed)
					if (!(filterNumber && call.getPhoneNumber().getNumber()
							.equals(""))) {
						if ((!filterCallIn)
								&& (call.getCalltype().toInt() == CallType.CALLIN))
							filteredcallerdata.add(call);
						else if ((!filterCallInFailed)
								&& (call.getCalltype().toInt() == CallType.CALLIN_FAILED))
							filteredcallerdata.add(call);
						else if ((!filterCallOut)
								&& (call.getCalltype().toInt() == CallType.CALLOUT))
							filteredcallerdata.add(call);
					}
			}
			filteredCallerData = filteredcallerdata;
			sortAllFilteredRowsBy(sortColumn, sortDirection);
		}
		if (jfritz.getJframe() != null)
			jfritz.getJframe().setStatus();
	}

	/**
	 * @return Total duration of all (filtered) calls
	 */
	public int getTotalDuration() {
		Enumeration en = getFilteredCallVector().elements();
		int total = 0;
		while (en.hasMoreElements()) {
			Call call = (Call) en.nextElement();
			total += call.getDuration();
		}
		return total;
	}

	/**
	 * @return Returns the jfritz.
	 */
	public final JFritz getJfritz() {
		return jfritz;
	}
}

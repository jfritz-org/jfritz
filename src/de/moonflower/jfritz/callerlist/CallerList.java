/*
 *
 * Created on 08.04.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
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
import javax.swing.table.TableColumn;
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
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * This class manages the caller list.
 *
 * @author Arno Willig
 */
public class CallerList extends AbstractTableModel {
	private static final long serialVersionUID = 1;

	private static final String CALLS_DTD_URI = "http://jfritz.moonflower.de/dtd/calls.dtd"; //$NON-NLS-1$

	private static final String CALLS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //$NON-NLS-1$
			+ "<!-- DTD for JFritz calls -->" //$NON-NLS-1$
			+ "<!ELEMENT calls (comment?,entry*)>" //$NON-NLS-1$
			+ "<!ELEMENT comment (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT date (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT caller (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT port (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT route (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT duration (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT comment (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT entry (date,caller?,port?,route?,duration?,comment?)>" //$NON-NLS-1$
			+ "<!ATTLIST entry calltype (call_in|call_in_failed|call_out) #REQUIRED>"; //$NON-NLS-1$

	private final static String PATTERN_CSV = "(\\||;)"; //$NON-NLS-1$

	private final static String EXPORT_CSV_FORMAT_JFRITZ = "\"CallType\";\"Date\";\"Time\";\"Number\";\"Route\";\"" + //$NON-NLS-1$
			"Port\";\"Duration\";\"Name\";\"Address\";\"City\";\"CallByCall\";\"Comment\""; //$NON-NLS-1$

	private final static String EXPORT_CSV_FORMAT_FRITZBOX = "Typ;Datum;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer"; //$NON-NLS-1$

	// Is the type eyported from a 7170
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE = "Typ; Datum; Rufnummer; Nebenstelle; Eigene Rufnummer; Dauer"; //$NON-NLS-1$

	// is the type exported from the new firmware
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE = "Typ;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer";

	// english firmware, unknown version
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH = "Typ;Date;Number;Extension;Outgoing Caller ID;Duration";

	private JFritz jfritz;

	// call list used to display entries in the table, can be sorted by other
	// criteria
	private Vector filteredCallerData;

	// internal call list, sorted descending by date
	private Vector unfilteredCallerData;

	// temp vector for adding in new calls
	private Vector newCalls;

	private int sortColumn;

	private boolean sortDirection = false;

	/**
	 * CallerList Constructor new contrustor, using binary sizes
	 * NOTE:filteredCallerData = unfilteredCallerData is forbidden!! use
	 * filteredCallerData = unfilteredCallerData.clone() instead
	 *
	 * @author Brian Jensen
	 *
	 * @param jfritz
	 */
	public CallerList(JFritz jfritz) {
		// Powers of 2 always have better performance
		unfilteredCallerData = new Vector(256);
		filteredCallerData = new Vector();

		// lets see if my new method works better
		newCalls = new Vector(32);

		this.jfritz = jfritz;

		sortColumn = 1;
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
	 * Is used for the clickability!
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		String columnName = getRealColumnName(columnIndex);
		if (columnName.equals("participant")) { //$NON-NLS-1$
			return ((Call) filteredCallerData.get(rowIndex)).getPhoneNumber() != null;
		} else if (columnName.equals("comment")) { //$NON-NLS-1$
			return true;
		} else if (columnName.equals("number")) { //$NON-NLS-1$
			return true;
		}
		return false;
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
	 *            Filename to save to
	 * @param wholeCallerList
	 *            Save whole caller list or only selected entries
	 */
	public void saveToXMLFile(String filename, boolean wholeCallerList) {
		Debug.msg("Saving to file " + filename); //$NON-NLS-1$
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
			pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
			pw.newLine();
			// pw.write("<!DOCTYPE calls SYSTEM \"" + CALLS_DTD_URI + "\">");
			// pw.newLine();
			pw.write("<calls>"); //$NON-NLS-1$
			pw.newLine();
			pw.write("<comment>Calls for " + JFritz.PROGRAM_NAME + " v" //$NON-NLS-1$,  //$NON-NLS-2$
					+ JFritz.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$
			pw.newLine();

			int rows[] = null;
			if (jfritz != null && jfritz.getJframe() != null) {
				rows = jfritz.getJframe().getCallerTable().getSelectedRows();
			}
			if (!wholeCallerList && rows != null && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					Call currentCall = (Call) filteredCallerData
							.elementAt(rows[i]);
					pw.write(currentCall.toXML());
					pw.newLine();
				}
			} else if (wholeCallerList) { // Export ALL UNFILTERED Calls
				Enumeration en = unfilteredCallerData.elements();
				while (en.hasMoreElements()) {
					Call call = (Call) en.nextElement();
					pw.write(call.toXML());
					pw.newLine();
				}
			} else {// Export ALL FILTERED Calls
				Enumeration en = filteredCallerData.elements();
				while (en.hasMoreElements()) {
					Call call = (Call) en.nextElement();
					pw.write(call.toXML());
					pw.newLine();
				}
			}
			pw.write("</calls>"); //$NON-NLS-1$

			pw.close();
		} catch (UnsupportedEncodingException e) {
			Debug.err("UTF-8 not supported"); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		} catch (IOException e) {
			Debug.err("IOException " + filename); //$NON-NLS-1$
		}
	}

	/**
	 * Saves callerlist to csv file
	 *
	 * @param filename
	 *            Filename to save to
	 * @param wholeCallerList
	 *            Save whole caller list or only selected entries
	 */
	public void saveToCSVFile(String filename, boolean wholeCallerList) {
		Debug.msg("Saving to csv file " + filename); //$NON-NLS-1$
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw
					.println("\"CallType\";\"Date\";\"Time\";\"Number\";\"Route\";\"Port\";\"Duration\";\"Name\";\"Address\";\"City\";\"CallByCall\";\"Comment\""); //$NON-NLS-1$
			int rows[] = null;
			if (jfritz != null && jfritz.getJframe() != null) {
				rows = jfritz.getJframe().getCallerTable().getSelectedRows();
			}
			if (!wholeCallerList && rows != null && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					Call currentCall = (Call) filteredCallerData
							.elementAt(rows[i]);
					pw.println(currentCall.toCSV());
				}
			} else if (wholeCallerList) { // Export ALL UNFILTERED Calls
				Enumeration en = getUnfilteredCallVector().elements();
				while (en.hasMoreElements()) {
					Call call = (Call) en.nextElement();
					pw.println(call.toCSV());
				}
			} else { // Export ALL FILTERED Calls
				Enumeration en = getFilteredCallVector().elements();
				while (en.hasMoreElements()) {
					Call call = (Call) en.nextElement();
					pw.println(call.toCSV());
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
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
			factory.setValidating(false); // FIXME Something wrong with the
			// DTD
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
							|| systemId.equals("calls.dtd")) { //$NON-NLS-1$
						InputSource is;
						is = new InputSource(new StringReader(CALLS_DTD));
						is.setSystemId(CALLS_DTD_URI);
						return is;
					}
					throw new SAXException("Invalid system identifier: " //$NON-NLS-1$
							+ systemId);
				}

			});
			reader.setContentHandler(new CallFileXMLHandler(this));
			reader.parse(new InputSource(new FileInputStream(filename)));

			// Synchronise the call vectors
			fireUpdateCallVector();

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.err(e.getLocalizedMessage());
				Debug
						.errDlg("STRUKTURÄNDERUNG!\n\nBitte in der Datei jfritz.calls.xml\n " //$NON-NLS-1$
								+ "die Zeichenkette \"calls.dtd\" durch\n \"" //$NON-NLS-1$
								+ CALLS_DTD_URI + "\"\n ersetzen!"); //$NON-NLS-1$
				System.exit(0);
			}
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	/**
	 * Removes all duplicate whitespaces from inputStr
	 *
	 * @param inputStr
	 * @return outputStr
	 */
	public static String removeDuplicateWhitespace(String inputStr) {
		Pattern p = Pattern.compile("\\s+"); //$NON-NLS-1$
		Matcher matcher = p.matcher(inputStr);
		return matcher.replaceAll(" "); //$NON-NLS-1$
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
			String port, String route, int duration, String comment) {
		Call call = new Call(jfritz, symbol, datum, number, port, route,
				duration);
		call.setComment(comment);
		return addEntry(call);
	}

	/**
	 * Adds an entry to the call list this function calls contains(Call newCall)
	 * to test if the given call is contained in the list the function then adds
	 * the entry to newCalls if appropriate
	 *
	 * Note: After all import processes make sure to call fireUpdateCallVector()
	 *
	 *
	 * @author Brian Jensen
	 */
	public boolean addEntry(Call call) {

		if (contains(call)) {
			return false;
		} else { // add a new enty to the call list
			newCalls.add(call);
			return true;
		}
	}

	/**
	 * This function tests if the given call (not the call object!!!) is
	 * contained in the call list
	 *
	 * This new method is using a binary search algorithm, that means
	 * unfilteredCallerData has to be sorted ascending by date or it won't work
	 *
	 *
	 * @author Brian Jensen
	 *
	 */
	public boolean contains(Call newCall) {
		int left, right, middle;
		left = 0;
		right = unfilteredCallerData.size() - 1;

		while (left <= right) {
			middle = ((right - left) / 2) + left;

			if (unfilteredCallerData.isEmpty())
				return false;

			Call c = (Call) unfilteredCallerData.elementAt(middle);
			int Compare = newCall.getCalldate().compareTo(c.getCalldate());

			// check if the date is before or after the current element in the
			// vector
			// Note: change the values here to fit the current sorting method
			if (Compare > 0)
				right = middle - 1;
			else if (Compare < 0)
				left = middle + 1;
			else {
				// if we are here, then the dates match
				// lets check if everything else matches
				if (c.equals(newCall))
					return true;
				else {
					// two calls, same date, different values...
					// this is really a performance killer...
					int tmpMiddle = middle - 1;

					// Yikes! Don't forget to stay in the array bounds
					if (tmpMiddle >= 0) {
						c = (Call) unfilteredCallerData.elementAt(tmpMiddle);

						// search left as long as the dates still match
						while (c.getCalldate().equals(newCall.getCalldate())) {

							// check if equal
							if (c.equals(newCall))
								return true;

							// make sure we stay in the array bounds
							if (tmpMiddle > 0)
								c = (Call) unfilteredCallerData
										.elementAt(--tmpMiddle);
							else
								break;
						}
					}

	        			tmpMiddle = middle +1;
					if (tmpMiddle < unfilteredCallerData.size()) {
						c = (Call) unfilteredCallerData.elementAt(middle + 1);

						// search right as long as the dates still match
						while (c.getCalldate().equals(newCall.getCalldate())) {

							// check if equal
							if (c.equals(newCall))
								return true;

							// make sure to stay in the array bounds
							if (tmpMiddle < (unfilteredCallerData.size() - 1))
								c = (Call) unfilteredCallerData
										.elementAt(++tmpMiddle);
							else
								break;
						}

					}

					// No matching calls found with the same date
					return false;

				}
			}
		}

		// we exited the loop => no matching date found
		return false;

	}

	/**
	 * This method synchronises the main call vector with with the recently
	 * added calls per addEntry(Call call)
	 *
	 * NOTE: This method must be called after any calls have been added but
	 * should not be called until done importing all calls
	 *
	 * @author Brian Jensen
	 *
	 */
	public void fireUpdateCallVector() {
		// update the call list and then sort it
		unfilteredCallerData.addAll(newCalls);
		newCalls.clear();
		sortAllUnfilteredRows();

	}

	/**
	 * Retrieves data from FRITZ!Box
	 *
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public void getNewCalls() throws WrongPasswordException, IOException {
		getNewCalls(false);
	}

	/**
	 * Retrieves data from FRITZ!Box Function calls
	 * JFritzUtils.retrieveCSVList(...) which reads the HTML page from the box
	 * then reads the csv-file in and passes it on to
	 * CallerList.importFromCSVFile(BufferedReader br) which then parses all the
	 * entries makes backups and deletes entries from the box as appropriate *
	 *
	 * @author Brian Jensen
	 *
	 * @param deleteFritzBoxCallerList
	 *            true indicates that fritzbox callerlist should be deleted
	 *            without considering number of entries or config
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public void getNewCalls(boolean deleteFritzBoxCallerList)
			throws WrongPasswordException, IOException {

		if (JFritzUtils.checkValidFirmware(jfritz)) {

			Debug.msg("box.address: " + JFritz.getProperty("box.address"));
			Debug.msg("box.password: " + JFritz.getProperty("box.password"));
			Debug.msg("box.firmware: "
					+ JFritz.getFirmware().getFirmwareVersion() + " "
					+ JFritz.getFirmware().getLanguage());

			boolean newEntries = JFritzUtils.retrieveCSVList(JFritz
					.getProperty("box.address","fritz.box"), Encryption.decrypt(JFritz
					.getProperty("box.password")), JFritz
					.getProperty("box.port", "80"), JFritz
					.getProperty("country.prefix"), JFritz
					.getProperty("country.code"), JFritz
					.getProperty("area.prefix"), JFritz
					.getProperty("area.code"), JFritz.getFirmware(), jfritz);

			// Notify user?
			if ((JFritz.getProperty("option.notifyOnCalls", "true")
					.equals("true"))
					&& newEntries) {
				jfritz.getJframe().setVisible(true);
				jfritz.getJframe().toFront();
			}

			if ((newEntries && JFritz.getProperty("option.deleteAfterFetch",
					"false").equals("true"))
					|| deleteFritzBoxCallerList) {
				JFritzUtils
						.clearListOnFritzBox(
								JFritz.getProperty("box.address"), //$NON-NLS-1$
								JFritz.getProperty("box.password"),//$NON-NLS-1$
								JFritz.getProperty("box.port"),//$NON-NLS-1$
								JFritz.getFirmware());
			}

			// Make back-up after fetching the caller list?
			if (newEntries
					&& JFritzUtils.parseBoolean(JFritz.getProperty(
							"option.createBackupAfterFetch", "false"))) {
				doBackup();
			}
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
		// 9 Columns on the Table
		return 9;
	}

	/**
	 * @param rowIndex
	 * @param columnIndex
	 * @return the value at a specific position
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Call call = (Call) filteredCallerData.get(rowIndex);
		String columnName = getRealColumnName(columnIndex);
		if (columnName.equals("type")) { //$NON-NLS-1$
			return call.getCalltype();
		} else if (columnName.equals("date")) { //$NON-NLS-1$
			return call.getCalldate();
		} else if (columnName.equals("callbycall")) { //$NON-NLS-1$
			if (call.getPhoneNumber() != null)
				return call.getPhoneNumber().getCallByCall();
			else
				return null;
		} else if (columnName.equals("number")) { //$NON-NLS-1$
			return call.getPhoneNumber();
		} else if (columnName.equals("participant")) { //$NON-NLS-1$
			return call.getPerson();
		} else if (columnName.equals("port")) { //$NON-NLS-1$
			return call.getPort();
		} else if (columnName.equals("route")) { //$NON-NLS-1$
			if (call.getRoute().startsWith("SIP")) //$NON-NLS-1$
				return jfritz.getSIPProviderTableModel().getSipProvider(
						call.getRoute(), call.getRoute());
			return call.getRoute();
		} else if (columnName.equals("duration")) { //$NON-NLS-1$
			return Integer.toString(call.getDuration());
		} else if (columnName.equals("comment")) { //$NON-NLS-1$
			return call.getComment();
			/**
			 * } else if (columnName.equals("Kosten")) { return
			 * Double.toString(call.getCost());
			 */
		}

		// default: return null
		return null;
	}

	/**
	 * Sets a value to a specific position
	 */
	public void setValueAt(Object object, int rowIndex, int columnIndex) {
		String columnName = getRealColumnName(columnIndex);
		if (columnName.equals("participant")) { //$NON-NLS-1$
			setPerson((Person) object, rowIndex);
		} else if (columnName.equals("comment")) { //$NON-NLS-1$
			setComment((String) object, rowIndex);
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public void setComment(String comment, int rowIndex) {
		Call call = (Call) filteredCallerData.get(rowIndex);
		call.setComment(comment);
	}

	public void setPerson(Person person, int rowIndex) {
		Call call = (Call) filteredCallerData.get(rowIndex);

		if (call.getPhoneNumber() != null) { // no empty numbers
			if (person == null) {
				Debug
						.err("Callerlist.setPerson():  IMPLEMENT ME (remove person)"); //$NON-NLS-1$
			} else {
				if (call.getPerson() == null) {
					if (!person.isEmpty())
						jfritz.getPhonebook().addEntry(person);
				} else if (!call.getPerson().equals(person)) {
					call.getPerson().copyFrom(person);
				}
			}
			fireTableDataChanged();
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
		// Debug.msg("Sorting column " + col + " " + asc);

		Debug.msg("Sorting all filtered Rows");
		Collections.sort(filteredCallerData, new ColumnSorter(col, asc));
		fireTableDataChanged();
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
		Debug.msg("Sorting unfiltered data"); //$NON-NLS-1$
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
		int columnIndex;

		boolean ascending;

		ColumnSorter(int columnIndex, boolean ascending) {
			this.columnIndex = columnIndex;
			this.ascending = ascending;
		}

		public int compare(Object a, Object b) {
			Object o1 = null, o2 = null;
			fireTableDataChanged();
			Call v1 = (Call) a;
			Call v2 = (Call) b;
			String columnName = getRealColumnName(columnIndex);

			if (columnName.equals("type")) { //$NON-NLS-1$
				o1 = v1.getCalltype().toString();
				o2 = v2.getCalltype().toString();
			} else if (columnName.equals("date")) { //$NON-NLS-1$
				o1 = v1.getCalldate();
				o2 = v2.getCalldate();
			} else if (columnName.equals("callbycall")) { //$NON-NLS-1$
				if (v1.getPhoneNumber() != null)
					o1 = v1.getPhoneNumber().getCallByCall();
				else
					o1 = null;
				if (v2.getPhoneNumber() != null)
					o2 = v2.getPhoneNumber().getCallByCall();
				else
					o2 = null;
			} else if (columnName.equals("number")) { //$NON-NLS-1$
				if (v1.getPhoneNumber() != null)
					o1 = v1.getPhoneNumber().getIntNumber();
				else
					o1 = null;
				if (v2.getPhoneNumber() != null)
					o2 = v2.getPhoneNumber().getIntNumber();
				else
					o2 = null;
			} else if (columnName.equals("participant")) { //$NON-NLS-1$
				if (v1.getPerson() != null)
					o1 = v1.getPerson().getFullname().toUpperCase();
				else
					o1 = null;
				if (v2.getPerson() != null)
					o2 = v2.getPerson().getFullname().toUpperCase();
				else
					o2 = null;
			} else if (columnName.equals("port")) { //$NON-NLS-1$
				o1 = v1.getPort();
				o2 = v2.getPort();
			} else if (columnName.equals("route")) { //$NON-NLS-1$
				o1 = v1.getRoute();
				o2 = v2.getRoute();
			} else if (columnName.equals("duration")) { //$NON-NLS-1$
				if (v1.getDuration() != 0)
					o1 = format(Integer.toString(v1.getDuration()), 10);
				else
					o1 = null;
				if (v2.getDuration() != 0)
					o2 = format(Integer.toString(v2.getDuration()), 10);
				else
					o2 = null;
			} else if (columnName.equals("comment")) { //$NON-NLS-1$
				o1 = v1.getComment().toUpperCase();
				o2 = v2.getComment().toUpperCase();
			} else {
				// Sort by Date
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
		Debug.msg("updating the filter");

		boolean filterCallIn = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callin")); //$NON-NLS-1$
		boolean filterCallInFailed = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callinfailed")); //$NON-NLS-1$
		boolean filterCallOut = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callout")); //$NON-NLS-1$
		boolean filterNumber = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.number")); //$NON-NLS-1$
		boolean filterFixed = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.fixed")); //$NON-NLS-1$
		boolean filterHandy = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.handy")); //$NON-NLS-1$
		boolean filterDate = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.date")); //$NON-NLS-1$
		boolean filterSip = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.sip")); //$NON-NLS-1$
		boolean filterCallByCall = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callbycall")); //$NON-NLS-1$
		boolean filterComment = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.comment")); // fireTableDataChanged();$NON-NLS-1$
		String filterSearch = JFritz.getProperty("filter.search", ""); //$NON-NLS-1$,  //$NON-NLS-2$
		String filterDateFrom = JFritz.getProperty("filter.date_from", ""); //$NON-NLS-1$,  //$NON-NLS-2$
		String filterDateTo = JFritz.getProperty("filter.date_to", ""); //$NON-NLS-1$,  //$NON-NLS-2$

		try {
			jfritz.getJframe().getCallerTable().getCellEditor()
					.cancelCellEditing();
		} catch (NullPointerException e) {
		}

		if ((!filterCallIn) && (!filterCallInFailed) && (!filterCallOut)
				&& (!filterNumber) && (!filterDate) && (!filterHandy)
				&& (!filterFixed) && (!filterSip) && (!filterCallByCall)
				&& (!filterComment) && (filterSearch.length() == 0)) {

			// Use unfiltered data
			filteredCallerData = (Vector) unfilteredCallerData.clone();
			sortAllFilteredRowsBy(sortColumn, sortDirection);
		} else { // Data got to be filtered
			Vector filteredSipProviders = new Vector();
			if (filterSip) {
				String providers = JFritz.getProperty("filter.sipProvider", //$NON-NLS-1$
						"[]"); //$NON-NLS-1$
				if (providers.equals("[]")) { // No entries selected
					// //$NON-NLS-1$
					filterSip = false;
				}
				providers = providers.replaceAll("\\[", ""); //$NON-NLS-1$,  //$NON-NLS-2$
				providers = providers.replaceAll("\\]", ""); //$NON-NLS-1$,  //$NON-NLS-2$
				String[] providerEntries = providers.split(","); //$NON-NLS-1$
				for (int i = 0; i < providerEntries.length; i++) {
					if (providerEntries[i].length() > 0) {
						if (providerEntries[i].charAt(0) == 32) { // delete
							// first SPACE
							providerEntries[i] = providerEntries[i]
									.substring(1);
						}
					}
					filteredSipProviders.add(providerEntries[i]);
				}
			}

			Vector filteredCallByCallProviders = new Vector();
			if (filterCallByCall) {
				String providers = JFritz.getProperty(
						"filter.callbycallProvider", "[]"); //$NON-NLS-1$,  //$NON-NLS-2$

				// No entries selected
				if (providers.equals("[]")) { //$NON-NLS-1$
					filterCallByCall = false;
				}
				providers = providers.replaceAll("\\[", ""); //$NON-NLS-1$,  //$NON-NLS-2$
				providers = providers.replaceAll("\\]", ""); //$NON-NLS-1$,  //$NON-NLS-2$
				String[] providerEntries = providers.split(","); //$NON-NLS-1$
				for (int i = 0; i < providerEntries.length; i++) {
					if (providerEntries[i].length() > 0) {
						if (providerEntries[i].charAt(0) == 32) { // delete
							// first SPACE
							providerEntries[i] = providerEntries[i]
									.substring(1);
						}
					}
					filteredCallByCallProviders.add(providerEntries[i]);
				}
			}

			Vector filteredcallerdata;
			filteredcallerdata = new Vector();
			Enumeration en = unfilteredCallerData.elements();
			while (en.hasMoreElements()) {
				Call call = (Call) en.nextElement();
				boolean dateFilterPassed = true;
				boolean searchFilterPassed = true;
				boolean handyFilterPassed = true;
				boolean fixedFilterPassed = true;
				boolean sipFilterPassed = true;
				boolean commentFilterPassed = true;

				// SearchFilter: Number, Participant, Date
				String parts[] = filterSearch.split(" "); //$NON-NLS-1$
				for (int i = 0; i < parts.length; i++) {
					String part = parts[i];
					if (part.length() > 0
							&& (call.getPhoneNumber() == null || call
									.getPhoneNumber().getAreaNumber().indexOf(
											parts[i]) == -1)
							&& (call.getPerson() == null || call.getPerson()
									.getFullname().toLowerCase().indexOf(
											part.toLowerCase()) == -1)) {
						searchFilterPassed = false;
						break;
					}
				}

				if (filterSip) {
					String route = call.getRoute();
					if (route.equals("")) { //$NON-NLS-1$
						route = "FIXEDLINE"; //$NON-NLS-1$
					}
					if (!filteredSipProviders.contains(route)) {
						searchFilterPassed = false;
					}
				}

				if (filterCallByCall) {
					if (call.getPhoneNumber() != null) {
						String callbycallprovider = call.getPhoneNumber()
								.getCallByCall();
						if (callbycallprovider.equals("")) { //$NON-NLS-1$
							callbycallprovider = "NONE"; //$NON-NLS-1$
						}
						if (!filteredCallByCallProviders
								.contains(callbycallprovider)) {
							searchFilterPassed = false;
						}
					} else { // Hide calls without number
						if (!filteredCallByCallProviders.contains("NONE")) { //$NON-NLS-1$
							searchFilterPassed = false;
						}
					}
				}

				try {
					if (filterDate
							&& !(call.getCalldate().after(
									new SimpleDateFormat("dd.MM.yy") //$NON-NLS-1$
											.parse(filterDateFrom)) && call
									.getCalldate().before(
											new SimpleDateFormat(
													"dd.MM.yy HH:mm") //$NON-NLS-1$
													.parse(filterDateTo
															+ " 23:59")))) { //$NON-NLS-1$
						dateFilterPassed = false;
					}
				} catch (ParseException e1) {
				}

				if (filterFixed && call.getPhoneNumber() != null
						&& !call.getPhoneNumber().isMobile())
					fixedFilterPassed = false;

				if (filterHandy && call.getPhoneNumber() != null
						&& call.getPhoneNumber().isMobile())
					handyFilterPassed = false;

				if (filterComment
						&& !call.getComment().equals(
								JFritz.getProperty("filter.comment.text", ""))) { //$NON-NLS-1$,  //$NON-NLS-2$
					commentFilterPassed = false;
				}

				if (searchFilterPassed && dateFilterPassed && handyFilterPassed
						&& fixedFilterPassed && sipFilterPassed
						&& commentFilterPassed)
					if (!(filterNumber && call.getPhoneNumber() == null)) {
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
	 * @return Total costs of all filtered calls
	 */
	public int getTotalCosts() {
		Enumeration en = getFilteredCallVector().elements();
		int total = 0;
		while (en.hasMoreElements()) {
			Call call = (Call) en.nextElement();
			if (call.getCost() > 0) // Negative Kosten => unbekannte kosten
				total += call.getCost();
		}
		return total;
	}

	/**
	 * @return Returns the jfritz.
	 */
	public final JFritz getJfritz() {
		return jfritz;
	}

	/**
	 * @param person
	 * @return Returns last call of person
	 */
	public Call findLastCall(Person person) {
		// FIXME: Nicht nur Standardnummer suchen
		Enumeration en = unfilteredCallerData.elements();
		while (en.hasMoreElements()) {
			Call call = (Call) en.nextElement();
			if (call.getPhoneNumber() != null
					&& person.getStandardTelephoneNumber() != null
					&& call.getPhoneNumber().getIntNumber().equals(
							person.getStandardTelephoneNumber().getIntNumber())) {
				return call;
			}
		}
		return null;
	}

	public void clearList() {
		Debug.msg("Clearing caller Table"); //$NON-NLS-1$
		unfilteredCallerData.clear();
		if ((jfritz.getJframe() != null)
				&& jfritz.getJframe().getCallerTable() != null)
			jfritz.getJframe().getCallerTable().clearSelection();
		saveToXMLFile(JFritz.CALLS_FILE, true);
		fireTableDataChanged();
	}

	public void removeEntries() {
		if (JOptionPane.showConfirmDialog(jfritz.getJframe(), JFritz
				.getMessage("really_delete_entries"), //$NON-NLS-1$
				JFritz.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			Debug.msg("Removing entries"); //$NON-NLS-1$
			int row[] = jfritz.getJframe().getCallerTable().getSelectedRows();
			if (row.length > 0) {
				Vector personsToDelete = new Vector();
				for (int i = 0; i < row.length; i++) {
					personsToDelete.add(filteredCallerData.get(row[i]));
				}
				Enumeration en = personsToDelete.elements();
				while (en.hasMoreElements()) {
					unfilteredCallerData.remove(en.nextElement());
				}
				saveToXMLFile(JFritz.CALLS_FILE, true);
				updateFilter();
				fireTableDataChanged();
			}
		}
	}

	public void fireTableDataChanged() {
		super.fireTableDataChanged();
	}

	public String getRealColumnName(int columnIndex) {
		String columnName = ""; //$NON-NLS-1$
		if (jfritz != null && jfritz.getJframe() != null) {
			Enumeration en = jfritz.getJframe().getCallerTable()
					.getTableHeader().getColumnModel().getColumns();
			while (en.hasMoreElements()) {
				TableColumn col = (TableColumn) en.nextElement();
				if (col.getModelIndex() == columnIndex)
					columnName = col.getIdentifier().toString();
			}
		}
		return columnName;
	}

	private static void doBackup() {
		CopyFile backup = new CopyFile();
		backup.copy(".", "xml"); //$NON-NLS-1$,  //$NON-NLS-2$
	}

	public Call getSelectedCall() {
		int rows[] = null;
		if ((jfritz != null) && (jfritz.getJframe() != null))
			rows = jfritz.getJframe().getCallerTable().getSelectedRows();

		if (rows != null && rows.length == 1)
			return (Call) this.filteredCallerData.elementAt(rows[0]);
		else
			Debug.errDlg(JFritz.getMessage("error_choose_one_call")); //$NON-NLS-1$

		return null;
	}

	/**
	 * @author Brian Jensen
	 *
	 * function reads the stream line by line using a buffered reader and using
	 * the appropriate parse function based on the structure
	 *
	 * currently supported file types: JFritz's own export format:
	 * EXPORT_CSV_FOMAT_JFRITZ Exported files from the fritzbox's web interface:
	 * EXPORT_CSV_FORMAT_FRITZBOX Exported files from fritzbox's Push service
	 * EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE Exported files from the new
	 * fritzbox's new Firmware EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE Exported
	 * files from the fritzbox's web interface:
	 * EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH (english firmware)
	 *
	 *
	 * function also has the ability to 'nicely' handle broken CSV lines
	 *
	 * NOTE: As is standard the caller must close the input stream on exit!
	 *
	 * @param filename
	 *            of the csv file to import from
	 */
	public boolean importFromCSVFile(BufferedReader br) {

		String line = "";
		boolean isJFritzExport = false; // flags to check which type to parse
		boolean isPushFile = false;
		boolean isNewFirmware = false;
		boolean isEnglishFirmware = false;
		int newEntries = 0;

		try {
			line = br.readLine();
			Debug.msg("CSV-Header: " + line);

			// check if we have a correct header
			if (line.equals(EXPORT_CSV_FORMAT_JFRITZ)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH)) {

				// check which kind of a file it is
				if (line.equals(EXPORT_CSV_FORMAT_JFRITZ))
					isJFritzExport = true;
				else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE))
					isPushFile = true;
				else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE))
					isNewFirmware = true;
				else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH))
					isEnglishFirmware = true;

				int linesRead = 0;
				Call c;
				while (null != (line = br.readLine())) {
					linesRead++;

					// call the apropriate parse function
					if (isJFritzExport)
						c = parseCallJFritzCSV(line);
					else if (isNewFirmware)
						c = parseCallFritzboxNewCSV(line);
					else if (isEnglishFirmware)
						c = parseCallFritzboxEnglishCSV(line);
					else
						c = parseCallFritzboxCSV(line, isPushFile);

					if (c == null) {
						if (!line.equals(""))
							Debug.err("Broken entry: " + line);

					} else if (addEntry(c)) {
						newEntries++;
					}
				}

				Debug.msg(linesRead + " Lines read from csv file ");
				Debug.msg(newEntries + " New entries processed");

				fireUpdateCallVector();

				if (newEntries > 0) {

					// uncomment these in case the import function is broken
					// for(int i=0; i < unfilteredCallerData.size(); i++)
					// System.out.println(unfilteredCallerData.elementAt(i).toString());

					saveToXMLFile(JFritz.CALLS_FILE, true);

					String msg;

					if (newEntries == 1) {
						msg = JFritz.getMessage("imported_call"); //$NON-NLS-1$
					} else {
						msg = JFritz
								.getMessage("imported_calls").replaceAll("%N", Integer.toString(newEntries)); //$NON-NLS-1$, //$NON-NLS-2$
					}

					JFritz.infoMsg(msg);

				} else {
					// JFritz.infoMsg(JFritz.getMessage("no_imported_calls"));
					// //$NON-NLS-1$
				}

			} else {
				// Invalid file header
				Debug.err("Wrong file type or corrupted file"); //$NON-NLS-1$
			}

			// NOTE: the caller must close the stream!

		} catch (FileNotFoundException e) {
			Debug.err("Could not read from File!");
		} catch (IOException e) {
			Debug.err("IO Exception reading csv file"); //$NON-NLS-1$
		}

		if (newEntries > 0)
			return true;
		else
			return false;

	}

	/**
	 * @author Brian Jensen
	 *
	 * function first splits the line into substrings, then strips the
	 * quotationmarks(do those have to be?) functions parses according to the
	 * format EXPORT_CSV_FORMAT_JFRITZ
	 *
	 *
	 * @param line
	 *            contains the line to be processed from a csv file
	 * @return returns a call object, or null if the csv line is invalid
	 */
	public Call parseCallJFritzCSV(String line) {
		String[] field = line.split(PATTERN_CSV);
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length < 12) {
			if (field.length != 1)
				Debug.err("Invalid CSV format, incorrect number of fields!"); //$NON-NLS-1$
			return null;
		}

		// Strip those damn quotes
		for (int i = 0; i < 12; i++)
			field[i] = field[i].substring(1, field[i].length() - 1);

		// Call type
		// Perhaps it would be nice to standardize the calltype and export
		// strings
		if (field[0].equals("Incoming")) { //$NON-NLS-1$
			calltype = new CallType("call_in"); //$NON-NLS-1$
		} else if (field[0].equals("Missed")) { //$NON-NLS-1$
			calltype = new CallType("call_in_failed"); //$NON-NLS-1$
		} else if (field[0].equals("Outgoing")) { //$NON-NLS-1$
			calltype = new CallType("call_out"); //$NON-NLS-1$
		} else {
			Debug.err("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null && field[2] != null) {

			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1] + " " + field[2]); //$NON-NLS-1$,  //$NON-NLS-2$
			} catch (ParseException e) {
				Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
			return null;
		}

		// change the port to fit the jfritz naming convention
		if (field[5].equals("FON1")) {
			field[5] = "0";
		} else if (field[5].equals("FON2")) {
			field[5] = "1";
		} else if (field[5].equals("FON3")) {
			field[5] = "2";
		} else if (field[5].equals("Durchwahl")) {
			field[5] = "3";
		} else if (field[5].equals("ISDN")) {
			field[5] = "4";
		} else if (field[5].equals("DATA")) {
			field[5] = "36";
		}

		// Parse the SIP Provider and save it correctly
		if (field[4].startsWith("Internet: ")) {
			Enumeration en = jfritz.getSIPProviderTableModel()
					.getProviderList().elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getNumber().equals(field[4].substring(10))) {
					field[4] = "SIP" + sipProvider.getProviderID();
					break;
				}
			}
		}

		// Phone number
		if (!field[3].equals("")) {
			number = new PhoneNumber(field[3]);
			number.setCallByCall(field[10]);
		} else
			number = null;

		// now make the call object
		// TODO: change the order of the Call constructor to fit
		// the oder of the csv export function or vice versa!!!
		call = new Call(jfritz, calltype, calldate, number, field[5], field[4],
				Integer.parseInt(field[6]));

		// TODO: perhaps split export function into two functions
		// exportCallListCSV() and exportPhoneBookCSV()
		// the few entries in the current export format are not complete
		// enough to reconstruct the phonebook correctly
		call.setComment(field[11]);

		return call;
	}

	/**
	 * @author Brian Jensen function parses a line of a csv file, that was
	 *         directly exported from the Fritzbox web interface, either
	 *         directly or through jfritz
	 *
	 * function parses according to format: EXPORT_CSV_FORMAT_FRITZBOX and
	 * EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE
	 *
	 * @param line
	 *            contains the line to be processed
	 * @return is call object, or null if the csv was invalid
	 */
	public Call parseCallFritzboxCSV(String line, boolean isPushFile) {
		String[] field = line.split(PATTERN_CSV);
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 6) {
			if (field.length != 1)
		    		Debug.err("Invalid CSV format, incorrect number of fields!");		//if you find an error here, its not because
			return null; // jfritz is broken, the fritz box exports things
		} // with an extra empty line for whatever reason

		// Call type
		// Why would they change the cvs format in the Push???
		if ((field[0].equals("1") && !isPushFile) //$NON-NLS-1$
				|| (field[0].equals("2") && isPushFile)) { //$NON-NLS-1$
			calltype = new CallType("call_in"); //$NON-NLS-1$
		} else if ((field[0].equals("2") && !isPushFile) //$NON-NLS-1$
				|| (field[0].equals("3") && isPushFile)) { //$NON-NLS-1$
			calltype = new CallType("call_in_failed"); //$NON-NLS-1$
		} else if ((field[0].equals("3") && !isPushFile) //$NON-NLS-1$
				|| (field[0].equals("1") && isPushFile)) { //$NON-NLS-1$
			calltype = new CallType("call_out"); //$NON-NLS-1$
		} else {
			Debug.err("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
			return null;
		}

		// Phone number
		if (!field[2].equals(""))
			number = new PhoneNumber(field[2]);
		else
			number = null;

		// split the duration into two stings, hours:minutes
		String[] time = field[5].split(":");

		// change the port to fit the jfritz naming convention
		if (field[3].equals("FON 1")) {
			field[3] = "0";
		} else if (field[3].equals("FON 2")) {
			field[3] = "1";
		} else if (field[3].equals("FON 3")) {
			field[3] = "2";
		} else if (field[3].equals("Durchwahl")) {
			field[3] = "3";
		} else if (field[3].equals("FON S0")) {
			field[3] = "4";
		} else if (field[3].equals("DATA S0")) {
			field[3] = "36";
		}

		// Parse the SIP Provider and save it correctly
		if (field[4].startsWith("Internet: ")) {
			Enumeration en = jfritz.getSIPProviderTableModel()
					.getProviderList().elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getNumber().equals(field[4].substring(10))) {
					field[4] = "SIP" + sipProvider.getProviderID();
					break;
				}
			}
		}

		// make the call object and exit
		call = new Call(jfritz, calltype, calldate, number, field[3], field[4],
				Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
						* 60);

		return call;

	}

	/**
	 * @author KCh function parses a line of a csv file, that was directly
	 *         exported from the Fritzbox web interface with BETA FW or with a
	 *         fritzbox with the new firmware >= XX.04.05
	 *
	 *
	 * @param line
	 *            contains the line to be processed
	 * @return is call object, or null if the csv was invalid
	 */
	public Call parseCallFritzboxNewCSV(String line) {
		String[] field = line.split(PATTERN_CSV);
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 7) {
			if (field.length != 1)
				Debug.err("Invalid CSV format, incorrect number fields!");
			return null;
		}

		// Call type
		if ((field[0].equals("1"))) {
			calltype = new CallType("call_in");
		} else if ((field[0].equals("2"))) {
			calltype = new CallType("call_in_failed");
		} else if ((field[0].equals("3"))) {
			calltype = new CallType("call_out");
		} else {
			Debug.err("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
			return null;
		}

		// Phone number
		if (!field[3].equals(""))
			number = new PhoneNumber(field[3]);
		else
			number = null;

		// split the duration into two stings, hours:minutes
		String[] time = field[6].split(":");
		// make the call object

		// change the port to fit the jfritz naming convention
		if (field[4].equals("FON 1")) {
			field[4] = "0";
		} else if (field[4].equals("FON 2")) {
			field[4] = "1";
		} else if (field[4].equals("FON 3")) {
			field[4] = "2";
		} else if (field[4].equals("Durchwahl")) {
			field[4] = "3";
		} else if (field[4].equals("FON S0")) {
			field[4] = "4";
		} else if (field[4].equals("DATA S0")) {
			field[4] = "36";
		}

		// Parse the SIP Provider and save it correctly
		if (field[5].startsWith("Internet: ")) {
			Enumeration en = jfritz.getSIPProviderTableModel()
					.getProviderList().elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getNumber().equals(field[5].substring(10))) {
					field[5] = "SIP" + sipProvider.getProviderID();
					break;
				}
			}
		}

		// make the call object and exit
		call = new Call(jfritz, calltype, calldate, number, field[4], field[5],
				Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
						* 60);

		return call;

	}

	/**
	 * @author Brian Jensen function parses a line of a csv file, that was
	 *         directly exported from the Fritzbox web interface, either
	 *         directly or through jfritz
	 *
	 * function parses according to format: EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH
	 * this is the format exported by boxes with english firmwar (unkown
	 * version)
	 *
	 * Note: This function has yet to be tested!
	 *
	 * @param line
	 *            contains the line to be processed
	 * @return is call object, or null if the csv was invalid
	 *
	 */
	public Call parseCallFritzboxEnglishCSV(String line) {
		String[] field = line.split(PATTERN_CSV);
		// leave this in here in case the push file is different, like with the
		// german firmware
		boolean isPushFile = false;
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 6) {
			if (field.length != 1)
				Debug.err("Invalid CSV format, incorrect number of fields"); // if
			return null; // jfritz is broken, the fritz box exports things
		} // with an extra empty line for whatever reason

		// Call type
		// Why would they change the cvs format in the Push file???
		if ((field[0].equals("1") && !isPushFile) //$NON-NLS-1$
				|| (field[0].equals("2") && isPushFile)) { //$NON-NLS-1$
			calltype = new CallType("call_in"); //$NON-NLS-1$
		} else if ((field[0].equals("2") && !isPushFile) //$NON-NLS-1$
				|| (field[0].equals("3") && isPushFile)) { //$NON-NLS-1$
			calltype = new CallType("call_in_failed"); //$NON-NLS-1$
		} else if ((field[0].equals("3") && !isPushFile) //$NON-NLS-1$
				|| (field[0].equals("1") && isPushFile)) { //$NON-NLS-1$
			calltype = new CallType("call_out"); //$NON-NLS-1$
		} else {
			Debug.err("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.err("Invalid date format in csv entry!"); //$NON-NLS-1$
			return null;
		}

		// Phone number
		if (!field[2].equals(""))
			number = new PhoneNumber(field[2]);
		else
			number = null;

		// split the duration into two stings, hours:minutes
		String[] time = field[5].split(":");

		// change the port to fit the jfritz naming convention
		if (field[3].equals("FON 1")) {
			field[3] = "0";
		} else if (field[3].equals("FON 2")) {
			field[3] = "1";
		} else if (field[3].equals("FON 3")) {
			field[3] = "2";
		} else if (field[3].equals("Durchwahl")) {
			field[3] = "3";
		} else if (field[3].equals("FON S0")) {
			field[3] = "4";
		} else if (field[3].equals("DATA S0")) {
			field[3] = "36";
		}

		// Parse the SIP Provider and save it correctly
		if (field[4].startsWith("Internet: ")) {
			Enumeration en = jfritz.getSIPProviderTableModel()
					.getProviderList().elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getNumber().equals(field[4].substring(10))) {
					field[4] = "SIP" + sipProvider.getProviderID();
					break;
				}
			}
		}

		// make the call object and exit
		call = new Call(jfritz, calltype, calldate, number, field[3], field[4],
				Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
						* 60);

		return call;

	}
}

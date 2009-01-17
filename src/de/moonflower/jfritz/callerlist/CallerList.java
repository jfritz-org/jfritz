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

import javax.swing.ImageIcon;
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
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.callerlist.filter.DateFilter;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.LookupObserver;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

/**
 * This class manages the caller list.
 *
 */
public class CallerList extends AbstractTableModel implements LookupObserver {
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

	// is the type exported from a 7170 with a >= XX.04.12
	private final static String EXPORT_CSV_FORMAT_PUSHSERVICE_NEW = "Typ; Datum; Name; Rufnummer; Nebenstelle; Eigene Rufnummer; Dauer";

	// is the type exported from the new firmware
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE = "Typ;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer";

	// english firmware, unknown version
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH = "Typ;Date;Number;Extension;Outgoing Caller ID;Duration";

	private final static String EXPORT_CSV_FORMAT_PUSHSERVICE_ENGLISH = "Type; Date; Number; Extension; Local Number; Duration";

	private final static String EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_NEW = "Typ;Date;Name;Number;Extension;Outgoing Caller ID;Duration";

	private final static String EXPORT_CSV_FORMAT_FRITZBOX_EN_140426 = "Type;Date;Name;Number;Extension;Outgoing Caller ID;Duration";

	// call list used to display entries in the table, can be sorted by other
	// criteria
	private Vector<Call> filteredCallerData;

	// internal call list, sorted descending by date
	private Vector<Call> unfilteredCallerData;

	// temp vector for adding in new calls
	private Vector<Call> newCalls;

	private int sortColumn;

	private Vector<CallFilter> filters;

	private Vector<CallerListListener> callListListeners;

	private boolean sortDirection = false;

	private PhoneBook phonebook;

	private Vector<IProgressListener> progressListeners;

	/**
	 * CallerList Constructor new contrustor, using binary sizes
	 * NOTE:filteredCallerData = unfilteredCallerData is forbidden!! use
	 * filteredCallerData = unfilteredCallerData.clone() instead
	 *
	 * @author Brian Jensen
	 *
	 */
	public CallerList() {
		// Powers of 2 always have better performance
		unfilteredCallerData = new Vector<Call>(256);
		filteredCallerData = new Vector<Call>();
		filters = new Vector<CallFilter>();

		newCalls = new Vector<Call>(32);
		callListListeners = new Vector<CallerListListener>();

		progressListeners = new Vector<IProgressListener>();

		sortColumn = 1;
	}

	/**CallerListListeners are used to passively catch changes to the
	 * data in the call list
	 *
	 * @param l the listener to be added
	 */
	public synchronized void addListener(CallerListListener l){
		callListListeners.add(l);
	}

	/**
	 * CallerListListeners are used to passively catch changes to the
	 * data in the call list
	 *
	 * @param l the listener to be removed
	 */
	public synchronized void removeListener(CallerListListener l){
		callListListeners.remove(l);
	}

	/**
	 *
	 * @return Unfiltered Vector of Calls
	 */
	public Vector<Call> getUnfilteredCallVector() {
		return unfilteredCallerData;
	}

	/**
	 *
	 * @return Filtered Vector of Calls
	 */
	public Vector<Call> getFilteredCallVector() {
		return filteredCallerData;
	}

	/**
	 * Is used for the clickability!
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		String columnName = getRealColumnName(columnIndex);
//		if (columnName.equals("participant")) { //$NON-NLS-1$
//			return (filteredCallerData.get(rowIndex)).getPhoneNumber() != null;
//		} else
		if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
			return true;
		} else if (columnName.equals(CallerTable.COLUMN_NUMBER)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * @param columnIndex
	 * @return class of column
	 */
	@SuppressWarnings("unchecked")
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
	public synchronized void saveToXMLFile(String filename, boolean wholeCallerList) {
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
			pw.write("<comment>Calls for " + Main.PROGRAM_NAME + " v" //$NON-NLS-1$,  //$NON-NLS-2$
					+ Main.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$
			pw.newLine();

			int rows[] = null;
			if (JFritz.getJframe() != null) {
				rows = JFritz.getJframe().getCallerTable().getSelectedRows();
			}
			if (!wholeCallerList && (rows != null) && (rows.length > 0)) {
				for (int i = 0; i < rows.length; i++) {
					Call currentCall = filteredCallerData.elementAt(rows[i]);
					pw.write(currentCall.toXML());
					pw.newLine();
				}
			} else if (wholeCallerList) { // Export ALL UNFILTERED Calls
				Enumeration<Call> en = unfilteredCallerData.elements();
				while (en.hasMoreElements()) {
					Call call = en.nextElement();
					pw.write(call.toXML());
					pw.newLine();
				}
			} else {// Export ALL FILTERED Calls
				Enumeration<Call> en = filteredCallerData.elements();
				while (en.hasMoreElements()) {
					Call call = en.nextElement();
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
	public synchronized void saveToCSVFile(String filename, boolean wholeCallerList) {
		Debug.msg("Saving to csv file " + filename); //$NON-NLS-1$
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw
					.println("\"CallType\";\"Date\";\"Time\";\"Number\";\"Route\";\"Port\";\"Duration\";\"Name\";\"Address\";\"City\";\"CallByCall\";\"Comment\""); //$NON-NLS-1$
			int rows[] = null;
			if (JFritz.getJframe() != null) {
				rows = JFritz.getJframe().getCallerTable().getSelectedRows();
			}
			if (!wholeCallerList && (rows != null) && (rows.length > 0)) {
				for (int i = 0; i < rows.length; i++) {
					Call currentCall = filteredCallerData.elementAt(rows[i]);
					pw.println(currentCall.toCSV());
				}
			} else if (wholeCallerList) { // Export ALL UNFILTERED Calls
				Enumeration<Call> en = unfilteredCallerData.elements();
				while (en.hasMoreElements()) {
					Call call = en.nextElement();
					pw.println(call.toCSV());
				}
			} else { // Export ALL FILTERED Calls
				Enumeration<Call> en = filteredCallerData.elements();
				while (en.hasMoreElements()) {
					Call call = en.nextElement();
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
	public synchronized void loadFromXMLFile(String filename) {
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
	public synchronized static String removeDuplicateWhitespace(String inputStr) {
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
		Call call = new Call(symbol, datum, number, port, route, duration,
				comment);
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
		// if( unfilteredCallerData.contains(call))return false;
		if (contains(call)) {
			return false;
		} // add a new enty to the call list

		Person p = phonebook.findPerson(call.getPhoneNumber());
		call.setPerson(p);
		if (p != null) {
			if (p.getLastCall() == null) {
				phonebook.setLastCall(p, call);
			} else if (p.getLastCall().getCalldate().before(call.getCalldate())) {
				phonebook.setLastCall(p, call);
			}
		}
		newCalls.add(call);

		return true;

	}

	/**
	 * Adds a vector of new calls to the list, used by network code to
	 * import calls en masse
	 *
	 * @author brian
	 *
	 * @param newCalls to be added to the call list
	 */
	public synchronized void addEntries(Vector<Call> newCalls){
		int newEntries = 0;

		for(Call call: newCalls)
			if(addEntry(call))
				newEntries++;

		if (newEntries > 0) {

			fireUpdateCallVector();
			update();

			saveToXMLFile(Main.SAVE_DIR + JFritz.CALLS_FILE, true);

			String msg;

			if (newEntries == 1) {
				msg = Main.getMessage("imported_call"); //$NON-NLS-1$
			} else {
				msg = Main
						.getMessage("imported_calls").replaceAll("%N", Integer.toString(newEntries)); //$NON-NLS-1$, //$NON-NLS-2$
			}

			// Notify user?
			if (Main.getProperty("option.notifyOnCalls")
					.equals("true")) {
				JFritz.infoMsg(msg);
			}

			// Make back-up after fetching the caller list?
			if (JFritzUtils.parseBoolean(Main.getProperty(
							"option.createBackupAfterFetch")))
				doBackup();


		}
	}

	/**
	 * Updates call data based upon an external data source
	 *
	 * @param oldCall original call
	 * @param newCall new call containing changed data
	 */
	public synchronized void updateEntry(Call oldCall, Call newCall){

		int index = unfilteredCallerData.indexOf(oldCall);

		//make sure original call was in our list
		if(index >= 0){

			unfilteredCallerData.setElementAt(newCall, index);

			for(CallerListListener listener: callListListeners)
				listener.callsUpdated(oldCall, newCall);

			update();
			saveToXMLFile(Main.SAVE_DIR+JFritz.CALLS_FILE, true);
		}
	}

	/**
	 * Removes a vector of calls, as dictated by an external data source
	 *
	 * @author brian
	 *
	 * @param removeCalls calls to be removed
	 */
	public synchronized void removeEntries(Vector<Call> removeCalls){

			unfilteredCallerData.removeAll(removeCalls);
			update();
			saveToXMLFile(Main.SAVE_DIR + JFritz.CALLS_FILE, true);

	}

	/**
	 * This function tests if the given call  is
	 * contained in the call list
	 *
	 * This new method is using a binary search algorithm, that means
	 * unfilteredCallerData has to be sorted ascending by date or it won't work
	 *
	 *
	 * @author Brian Jensen
	 *
	 */
	public synchronized boolean contains(Call newCall) {
		int left, right, middle;
		left = 0;
		right = unfilteredCallerData.size() - 1;

		while (left <= right) {
			middle = ((right - left) / 2) + left;

			if (unfilteredCallerData.isEmpty()) {
				return false;
			}

			Call c = unfilteredCallerData.elementAt(middle);
			int Compare = newCall.getCalldate().compareTo(c.getCalldate());

			// check if the date is before or after the current element in the
			// vector
			// Note: change the values here to fit the current sorting method
			if (Compare > 0) {
				right = middle - 1;
			} else if (Compare < 0) {
				left = middle + 1;
			} else {
				// if we are here, then the dates match
				// lets check if everything else matches
				if (c.equals(newCall)) {
					return true;
				} else {
					// two calls, same date, different values...
					// this is really a performance killer...
					int tmpMiddle = middle - 1;

					// Yikes! Don't forget to stay in the array bounds
					if (tmpMiddle >= 0) {
						c = unfilteredCallerData.elementAt(tmpMiddle);

						// search left as long as the dates still match
						while (c.getCalldate().equals(newCall.getCalldate())) {

							// check if equal
							if (c.equals(newCall)) {
								return true;
							}

							// make sure we stay in the array bounds
							if (tmpMiddle > 0) {
								c = unfilteredCallerData.elementAt(--tmpMiddle);
							} else {
								break;
							}
						}
					}

					tmpMiddle = middle + 1;
					if (tmpMiddle < unfilteredCallerData.size()) {
						c = unfilteredCallerData.elementAt(middle + 1);

						// search right as long as the dates still match
						while (c.getCalldate().equals(newCall.getCalldate())) {

							// check if equal
							if (c.equals(newCall)) {
								return true;
							}

							// make sure to stay in the array bounds
							if (tmpMiddle < (unfilteredCallerData.size() - 1)) {
								c = unfilteredCallerData.elementAt(++tmpMiddle);
							} else {
								break;
							}
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
	 * This method synchronizes the main call vector with with the recently
	 * added calls per addEntry(Call call)
	 *
	 * NOTE: This method must be called after any calls have been added but
	 * should not be called until done importing all calls
	 *
	 * @author Brian Jensen
	 *
	 */
	public synchronized void fireUpdateCallVector() {
		// update the call list and then sort it
		unfilteredCallerData.addAll(newCalls);

		for(CallerListListener l: callListListeners)
			l.callsAdded((Vector<Call>) newCalls.clone());

		newCalls.clear();
		sortAllUnfilteredRows();

	}

	/**
	 * Retrieves data from FRITZ!Box
	 *
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public void getNewCalls() throws WrongPasswordException, InvalidFirmwareException, IOException {
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
			throws WrongPasswordException, InvalidFirmwareException, IOException {

		if (JFritz.getFritzBox().checkValidFirmware()) {

			Debug.msg("box.address: " + JFritz.getFritzBox().getAddress());
			Debug.msg("box.port: " + JFritz.getFritzBox().getPort());
			Debug.msg("box.password: " + JFritz.getFritzBox().getPassword());
			Debug.msg("box.firmware: "
					+ JFritz.getFritzBox().getFirmware().getFirmwareVersion()
					+ " " + JFritz.getFritzBox().getFirmware().getLanguage());

			boolean newEntries = JFritz.getFritzBox().retrieveCSVList();

			if (!JFritz.isShutdownInvoked())
			{
				if ((newEntries && Main.getProperty("option.deleteAfterFetch")
						.equals("true"))
						|| deleteFritzBoxCallerList) {
					JFritz.getFritzBox().clearListOnFritzBox();
				}
			}

			// Make back-up after fetching the caller list?
			if (newEntries
					&& JFritzUtils.parseBoolean(Main.getProperty(
							"option.createBackupAfterFetch"))) {
				doBackup();
			}
		}
		update();
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
		// 10 Columns on the Table
		return 10;
	}

	/**
	 * @param rowIndex
	 * @param columnIndex
	 * @return the value at a specific position
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Call call = filteredCallerData.get(rowIndex);
		String columnName = getRealColumnName(columnIndex);
		if (columnName.equals(CallerTable.COLUMN_TYPE)) { //$NON-NLS-1$
			return call.getCalltype();
		} else if (columnName.equals(CallerTable.COLUMN_DATE)) { //$NON-NLS-1$
			return call.getCalldate();
		} else if (columnName.equals(CallerTable.COLUMN_CALL_BY_CALL)) { //$NON-NLS-1$
			if (call.getPhoneNumber() != null) {
				return call.getPhoneNumber().getCallByCall();
			} else {
				return null;
			}
		} else if (columnName.equals(CallerTable.COLUMN_NUMBER)) { //$NON-NLS-1$
			return call.getPhoneNumber();
		} else if (columnName.equals(CallerTable.COLUMN_PARTICIPANT)) { //$NON-NLS-1$
			return call.getPerson();
		} else if (columnName.equals(CallerTable.COLUMN_PORT)) { //$NON-NLS-1$
			return call.getPort();
		} else if (columnName.equals(CallerTable.COLUMN_ROUTE)) { //$NON-NLS-1$
			if (call.getRoute().startsWith("SIP")) {
				return JFritz.getSIPProviderTableModel().getSipProvider(
						call.getRoute(), call.getRoute());
			}
			return call.getRoute();
		} else if (columnName.equals(CallerTable.COLUMN_DURATION)) { //$NON-NLS-1$
			return Integer.toString(call.getDuration());
		} else if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
			return call.getComment();
		} else if (columnName.equals(CallerTable.COLUMN_PICTURE)) { //$NON-NLS-1$
			if (call.getPerson() != null)
				return call.getPerson().getScaledPicture();
			else
				return new ImageIcon("");
		}
			/**
			 * } else if (columnName.equals("Kosten")) { return
			 * Double.toString(call.getCost());
			 * }
			 */

		// default: return null
		return null;
	}

	/**
	 * Sets a value to a specific position
	 */
	public void setValueAt(Object object, int rowIndex, int columnIndex) {

		String columnName = getRealColumnName(columnIndex);
		if (columnName.equals(CallerTable.COLUMN_PARTICIPANT)) { //$NON-NLS-1$
			setPerson((Person) object, rowIndex);
		} else if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
			setComment((String) object, rowIndex);
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public synchronized void setComment(String comment, int rowIndex) {
		Call updated = filteredCallerData.get(rowIndex);
		Call original = updated.clone();
		updated.setComment(comment);

		//Remove the old copy at each client
		for(CallerListListener listener: callListListeners)
			listener.callsUpdated(original, updated);

	}

	public void setPerson(Person person, int rowIndex) {
		Call call = filteredCallerData.get(rowIndex);
		setPerson(person, call);
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

		// Debug.msg("Sorting all filtered Rows by:" + col);
		Collections.sort(filteredCallerData, new ColumnSorter<Call>(col, asc));
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

		int indexOfDate = -1;
		for (int i = 0; i < getColumnCount(); i++) {
			String columnName = getRealColumnName(i);
			if (columnName.equals(CallerTable.COLUMN_DATE)) {
				indexOfDate = i;
			}
		}

		Collections.sort(unfilteredCallerData, new ColumnSorter<Call>(
				indexOfDate, false));
		// Resort filtered data
		Collections.sort(filteredCallerData, new ColumnSorter<Call>(sortColumn,
				sortDirection));
		// updateFilter(); //TODO überlegen ob man das noch braucht
		fireTableStructureChanged();
	}

	/**
	 * This comparator is used to sort vectors of data
	 */
	public class ColumnSorter<T extends Call> implements Comparator<Call> {
		int columnIndex;

		boolean ascending;

		ColumnSorter(int columnIndex, boolean ascending) {
			this.columnIndex = columnIndex;
			this.ascending = ascending;
		}

		public int compare2(Object a, Object b) {

			if (!(a instanceof Call) || !(b instanceof Call)) {
				return 0;
			}

			Call call1 = (Call) a;
			Call call2 = (Call) b;
			return compare(call1, call2);
		}

		// FIXME
		public int compare(Call call1, Call call2) {
			Object o1 = null, o2 = null;
			String columnName = getRealColumnName(columnIndex);

			if (columnName.equals(CallerTable.COLUMN_TYPE)) { //$NON-NLS-1$
				o1 = call1.getCalltype().toString();
				o2 = call2.getCalltype().toString();
			} else if (columnName.equals(CallerTable.COLUMN_DATE)) { //$NON-NLS-1$
				o1 = call1.getCalldate();
				o2 = call2.getCalldate();
			} else if (columnName.equals(CallerTable.COLUMN_CALL_BY_CALL)) { //$NON-NLS-1$
				if (call1.getPhoneNumber() != null) {
					o1 = call1.getPhoneNumber().getCallByCall();
				} else {
					o1 = null;
				}
				if (call2.getPhoneNumber() != null) {
					o2 = call2.getPhoneNumber().getCallByCall();
				} else {
					o2 = null;
				}
			} else if (columnName.equals(CallerTable.COLUMN_NUMBER)) { //$NON-NLS-1$
				if (call1.getPhoneNumber() != null) {
					o1 = call1.getPhoneNumber().getIntNumber();
				} else {
					o1 = null;
				}
				if (call2.getPhoneNumber() != null) {
					o2 = call2.getPhoneNumber().getIntNumber();
				} else {
					o2 = null;
				}
			} else if (columnName.equals(CallerTable.COLUMN_PARTICIPANT)) { //$NON-NLS-1$
				if (call1.getPerson() != null) {
					o1 = call1.getPerson().getFullname().toUpperCase();
				} else {
					o1 = null;
				}
				if (call2.getPerson() != null) {
					o2 = call2.getPerson().getFullname().toUpperCase();
				} else {
					o2 = null;
				}
			} else if (columnName.equals(CallerTable.COLUMN_PORT)) { //$NON-NLS-1$
				o1 = call1.getPort();
				o2 = call2.getPort();
			} else if (columnName.equals(CallerTable.COLUMN_ROUTE)) { //$NON-NLS-1$
				o1 = call1.getRoute();
				o2 = call2.getRoute();
			} else if (columnName.equals(CallerTable.COLUMN_DURATION)) { //$NON-NLS-1$
				if (call1.getDuration() != 0) {
					o1 = format(Integer.toString(call1.getDuration()), 10);
				} else {
					o1 = null;
				}
				if (call2.getDuration() != 0) {
					o2 = format(Integer.toString(call2.getDuration()), 10);
				} else {
					o2 = null;
				}
			} else if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
				o1 = call1.getComment().toUpperCase();
				o2 = call2.getComment().toUpperCase();
			} else {
				// Sort by Date
				o1 = call1.getCalldate();
				o2 = call2.getCalldate();
			}

			// Treat empty strings like nulls
			if ((o1 instanceof String) && (((String) o1).trim().length() == 0)) {
				o1 = null;
			}
			if ((o2 instanceof String) && (((String) o2).trim().length() == 0)) {
				o2 = null;
			}

			// Sort nulls so they appear last, regardless
			// of sort order
			if ((o1 == null) && (o2 == null)) {
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
				for (int k = 0; k < j; k++) {
					sb.append(' ');
				}
				return sb.toString() + s;
			} else {
				return s;
			}
		}

	}

	/**
	 * @return Total duration of all (filtered) calls
	 */
	public int getTotalDuration() {
		Enumeration<Call> en = filteredCallerData.elements();
		int total = 0;
		while (en.hasMoreElements()) {
			Call call = en.nextElement();
			total += call.getDuration();
		}
		return total;
	}

	/**
	 * @return Total costs of all filtered calls
	 */
	public int getTotalCosts() {
		Enumeration<Call> en = filteredCallerData.elements();
		int total = 0;
		while (en.hasMoreElements()) {
			Call call = en.nextElement();
			if (call.getCost() > 0) {
				total += call.getCost();
			}
		}
		return total;
	}

	/**
	 * @param person
	 * @return Returns last call of person
	 */
	public Call findLastCall(Person person) {
		Vector<PhoneNumber> numbers = person.getNumbers();
		if (numbers.size() > 0) {
			Enumeration<Call> en = unfilteredCallerData.elements();
			while (en.hasMoreElements()) {
				Call call = en.nextElement();
				if (call.getPhoneNumber() != null) {
					for (int i = 0; i < numbers.size(); i++) {
						if (call.getPhoneNumber().getIntNumber().equals(
								((PhoneNumber) numbers.get(i)).getIntNumber())) {
							return call;
						}
					}
				}
			}
		}
		return null;
	}

	public void clearList() {
		Debug.msg("Clearing caller Table"); //$NON-NLS-1$
		unfilteredCallerData.clear();
		if ((JFritz.getJframe() != null)
				&& (JFritz.getJframe().getCallerTable() != null)) {
			JFritz.getJframe().getCallerTable().clearSelection();
		}
		saveToXMLFile(Main.SAVE_DIR + JFritz.CALLS_FILE, true);
		fireTableDataChanged();
	}

	/**
	 * rows contain the rows of the <b>un</b>filteredCallerData wich will be
	 * removed from the filteredCallerData. Then fireTableChanged is called,
	 * wich will update the filteredCallerData
	 *
	 * @param rows
	 *            of the filteredCallerData to be removed
	 */
	public synchronized void  removeEntries(int[] rows) {

		Vector<Call> removedCalls = new Vector<Call>(rows.length);
		if (rows.length > 0) {
			Call call;
			for (int i = 0; i < rows.length; i++) {
				call = filteredCallerData.get(rows[i]);
				removedCalls.add(call);
				unfilteredCallerData.remove(call);
				//Debug.msg("removing " + call);
				Person p = call.getPerson();
				if (p != null) {
					if (call.equals(p.getLastCall())) {
						// this was the LastCall of the Person
						phonebook.setLastCall(p, findLastCall(p));
					}
				}

			}

			//notify all listeners that calls have been removed
			for(CallerListListener l: callListListeners)
				l.callsRemoved((Vector) removedCalls.clone());

			saveToXMLFile(Main.SAVE_DIR + JFritz.CALLS_FILE, true);
			update();
			fireTableDataChanged();
		}
	}

	public void fireTableDataChanged() {
		super.fireTableDataChanged();
	}

	public String getRealColumnName(int columnIndex) {
		String columnName = ""; //$NON-NLS-1$
		if (JFritz.getJframe() != null) {
			Enumeration<TableColumn> en = JFritz.getJframe().getCallerTable()
					.getTableHeader().getColumnModel().getColumns();
			while (en.hasMoreElements()) {
				TableColumn col = (TableColumn) en.nextElement();
				if (col.getModelIndex() == columnIndex) {
					columnName = col.getIdentifier().toString();
				}
			}
		}
		return columnName;
	}

	private static void doBackup() {
		CopyFile backup = new CopyFile();
		backup.copy(Main.SAVE_DIR, "xml"); //$NON-NLS-1$,  //$NON-NLS-2$
	}

	public Call getSelectedCall() {
		int rows[] = null;
		if (JFritz.getJframe() != null) {
			rows = JFritz.getJframe().getCallerTable().getSelectedRows();
		}

		if ((rows != null) && (rows.length == 1)) {
			return this.filteredCallerData.elementAt(rows[0]);
		} else {
			Debug.errDlg(Main.getMessage("error_choose_one_call")); //$NON-NLS-1$
		}

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
public synchronized boolean importFromCSVFile(BufferedReader br) {
		long t1, t2;
		t1 = System.currentTimeMillis();
		String line = "";
		boolean isJFritzExport = false; // flags to check which type to parse
		boolean isPushFile = false;
		boolean isNewFirmware = false;
		boolean isEnglishFirmware = false;
		boolean isNewEnglishFirmware = false;
		int newEntries = 0;

		try {
			String separator = PATTERN_CSV;
			line = br.readLine();
			Debug.msg("CSV-Header: " + line);
			if (line == null) {
				Debug.err("File empty"); //$NON-NLS-1$
				return false;
			} else
			{
				Pattern p = Pattern.compile("sep=(\\W{1})"); //$NON-NLS-1$
				Matcher matcher = p.matcher(line);
				if (matcher.find())
				{
					if (matcher.groupCount() == 1)
					{
						separator = matcher.group(1);
						Debug.msg("Separator: " + separator);
						line = br.readLine();
						Debug.msg("CSV-Header: " + line);
					}
				}
			}

			for(IProgressListener listener: progressListeners)
			{
				listener.setMin(0);
				listener.setMax(400);
			}

			// check if we have a correct header
			if (line.equals(EXPORT_CSV_FORMAT_JFRITZ)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH)
					|| line.equals(EXPORT_CSV_FORMAT_PUSHSERVICE_ENGLISH)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_NEW)
					|| line.equals(EXPORT_CSV_FORMAT_PUSHSERVICE_NEW)
					|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_EN_140426)) {

				// check which kind of a file it is
				if (line.equals(EXPORT_CSV_FORMAT_JFRITZ)) {
					isJFritzExport = true;
				} else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_PUSHSERVICE)) {
					isPushFile = true;
				} else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE)
						|| line.equals(EXPORT_CSV_FORMAT_PUSHSERVICE_NEW)) {
					isNewFirmware = true;
				} else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH)
						|| line.equals(EXPORT_CSV_FORMAT_PUSHSERVICE_ENGLISH)) {
					isEnglishFirmware = true;
				} else if (line.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_NEW)
						|| line.equals(EXPORT_CSV_FORMAT_FRITZBOX_EN_140426)) {
					isNewEnglishFirmware = true;
				}

				int linesRead = 0;
				Call c;
				while (!JFritz.isShutdownInvoked() && null != (line = br.readLine())) {
					linesRead++;

					// call the appropriate parse function
					if (isJFritzExport) {
						c = parseCallJFritzCSV(line, separator);
					} else if (isNewFirmware) {
						c = parseCallFritzboxNewCSV(line, separator);
					} else if (isEnglishFirmware) {
						c = parseCallFritzboxEnglishCSV(line, separator);
					} else if (isNewEnglishFirmware) {
						c = parseCallFritzboxNewEnglishCSV(line, separator);
					} else {
						c = parseCallFritzboxCSV(line, isPushFile, separator);
					}

					if (c == null) {
						if (!line.equals("")) {
							Debug.err("Broken entry: " + line);
						}
					} else if (addEntry(c)) {
						newEntries++;
					}
					for(IProgressListener listener: progressListeners)
					{
						listener.setProgress(linesRead);
					}
				}

				for(IProgressListener listener: progressListeners)
				{
					listener.finished();
				}
				Debug.msg(linesRead + " Lines read from csv file ");
				Debug.msg(newEntries + " New entries processed");

				fireUpdateCallVector();

				if (newEntries > 0) {

					// uncomment these in case the import function is broken
					// for(int i=0; i < unfilteredCallerData.size(); i++)
					// System.out.println(unfilteredCallerData.elementAt(i).toString());

					saveToXMLFile(Main.SAVE_DIR + JFritz.CALLS_FILE, true);

					String msg;

					if (newEntries == 1) {
						msg = Main.getMessage("imported_call"); //$NON-NLS-1$
					} else {
						msg = Main
								.getMessage("imported_calls").replaceAll("%N", Integer.toString(newEntries)); //$NON-NLS-1$, //$NON-NLS-2$
					}

					if (Main.getProperty("option.notifyOnCalls")
							.equals("true")) {
						JFritz.infoMsg(msg);
					}

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
		t2 = System.currentTimeMillis();
		Debug.msg("Time used to import CSV-File: " + (t2 - t1) + "ms");

		if (newEntries > 0) {
			return true;
		} else {
			return false;
		}

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
	public Call parseCallJFritzCSV(String line, String separator) {
		String[] field = line.split(separator);
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length < 12) {
			if (field.length != 1) {
				Debug.err("Invalid CSV format, incorrect number of fields!"); //$NON-NLS-1$
			}
			return null;
		}

		// Strip those damn quotes
		for (int i = 0; i < 12; i++) {
			field[i] = field[i].substring(1, field[i].length() - 1);
		}

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
		if ((field[1] != null) && (field[2] != null)) {

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

		// Phone number
		if (!field[3].equals("")) {
			number = new PhoneNumber(field[3], false);
			number.setCallByCall(field[10]);
		} else {
			number = null;
		}

		// now make the call object
		// TODO: change the order of the Call constructor to fit
		// the oder of the csv export function or vice versa!!!
		call = new Call(calltype, calldate, number, field[5], field[4], Integer
				.parseInt(field[6]));

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
	public Call parseCallFritzboxCSV(String line, boolean isPushFile,
			String separator) {
		String[] field = line.split(separator);
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 6) {
			if (field.length != 1) {
				Debug.err("Invalid CSV format, incorrect number of fields!"); // if
			}
			// you
			// find
			// an
			// error
			// here,
			// its
			// not
			// because
			return null; // jfritz is broken, the fritz box exports things
		} // with an extra empty line for whatever reason

		// Call type
		// Why would they change the cvs format in the Push service???
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
		if (!field[2].equals("")) {
			number = new PhoneNumber(field[2], Main.getProperty(
					"option.activateDialPrefix").toLowerCase().equals("true")
					&& (calltype.toInt() == CallType.CALLOUT)
					&& !field[4].startsWith("Internet"));
		} else {
			number = null;
		}

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

		// make the call object and exit
		call = new Call(calltype, calldate, number, field[3], field[4], Integer
				.parseInt(time[0])
				* 3600 + Integer.parseInt(time[1]) * 60);

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
	public Call parseCallFritzboxNewCSV(String line, String separator) {
		String[] field = line.split(separator);
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 7) {
			if (field.length != 1) {
				Debug.err("Invalid CSV format, incorrect number fields!");
			}
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
		if (!field[3].equals("")) {
			number = new PhoneNumber(field[3], Main.getProperty(
					"option.activateDialPrefix").toLowerCase().equals("true")
					&& (calltype.toInt() == CallType.CALLOUT)
					&& !field[5].startsWith("Internet"));
		} else {
			number = null;
		}

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

		// make the call object and exit
		call = new Call(calltype, calldate, number, field[4], field[5], Integer
				.parseInt(time[0])
				* 3600 + Integer.parseInt(time[1]) * 60);

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
	public Call parseCallFritzboxEnglishCSV(String line, String separator) {
		String[] field = line.split(separator);
		// leave this in here in case the push file is different, like with the
		// german firmware
		boolean isPushFile = false;
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 6) {
			if (field.length != 1) {
				Debug.err("Invalid CSV format, incorrect number of fields"); // if
			}
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
		if (!field[2].equals("")) {
			number = new PhoneNumber(field[2], Main.getProperty(
					"option.activateDialPrefix").toLowerCase().equals("true")
					&& (calltype.toInt() == CallType.CALLOUT)
					&& !field[4].startsWith("Internet"));
		} else {
			number = null;
		}

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

		// make the call object and exit
		call = new Call(calltype, calldate, number, field[3], field[4], Integer
				.parseInt(time[0])
				* 3600 + Integer.parseInt(time[1]) * 60);

		return call;

	}

	/**
	 * @author Brian Jensen
	 *
	 * function parses a line of a csv file, that was
	 *         directly exported from the Fritzbox web interface, either
	 *         directly or through jfritz
	 *
	 * function parses according to format:
	 * EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_NEW this is the format exported by
	 * boxes with english firmware (xx.04.20)
	 *
	 * Note: This function has yet to be tested!
	 *
	 * @param line
	 *            contains the line to be processed
	 * @return is call object, or null if the csv was invalid
	 *
	 */
	public Call parseCallFritzboxNewEnglishCSV(String line, String separator) {
		String[] field = line.split(separator);
		// leave this in here in case the push file is different, like with the
		// german firmware
		boolean isPushFile = false;
		Call call;
		CallType calltype;
		Date calldate;
		PhoneNumber number;

		// check if line has correct amount of entries
		if (field.length != 7) {
			if (field.length != 1) {
				Debug.err("Invalid CSV format, incorrect number of fields"); // if
			}
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

		// Name
		// field[2]

		// Phone number
		if (!field[3].equals("")) {
			number = new PhoneNumber(field[3], Main.getProperty(
					"option.activateDialPrefix").toLowerCase().equals("true")
					&& (calltype.toInt() == CallType.CALLOUT)
					&& !field[5].startsWith("Internet"));
		} else {
			number = null;
		}

		// split the duration into two stings, hours:minutes
		String[] time = field[6].split(":");
		Debug.msg("time length: "+time.length);
		//Apparently with the new 14.04.26 the duration is stored as hours.minutes
		if(time.length != 2){
			time = field[6].split("\\.");
			Debug.msg("adjusted length: "+time.length);
		}

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

		// make the call object and exit
		call = new Call(calltype, calldate, number, field[4], field[5], Integer
				.parseInt(time[0])
				* 3600 + Integer.parseInt(time[1]) * 60);

		return call;

	}

	/*
	 * too slow public void calculateAllLastCalls(Vector unfilteredPersons){ for
	 * (Iterator iter = unfilteredCallerData.iterator(); iter.hasNext();) { Call
	 * element = (Call) iter.next(); Person p = element.getPerson(); Date
	 * lastOfList; if(unfilteredPersons.contains(p)){ lastOfList =
	 * element.getCalldate();
	 * if(p.getLastCall().getCalldate().before(lastOfList)){
	 * p.getLastCall().setCalldate(lastOfList); } } } }
	 */
	/**
	 * adds a Filter to sort out some calls
	 *
	 * @param cf
	 *            the CallFilter which should be applied
	 * @param name
	 *            the name of the Filter
	 */
	public void addFilter(CallFilter cf) {
		filters.add(cf);
		// maybe we can save some time and not recalculate the old filters
	}

	/**
	 * removes a Filter
	 *
	 * @param name
	 *            the name of the Filter
	 * @return true if the filter was removed
	 */
	public boolean removeFilter(CallFilter cf) {
		boolean o = filters.remove(cf);
		return o;
	}

	/**
	 * removes a Filter
	 *
	 */
	public void removeAllFilter() {
		filters.removeAllElements();
	}

	/**
	 * Filters the unfilteredData and writes it to filtered Data all added
	 * Filters are used
	 */
	public Vector<Call> filterData(Vector<Call> src) {
		Vector<Call> result = new Vector<Call>();
		// Debug.msg("updating filtered Data");
		Enumeration<Call> en = src.elements();
		Call call;
		CallFilter f;
		int i;
		while (en.hasMoreElements()) {
			call = en.nextElement();
			for (i = 0; i < filters.size(); i++) {
				f = filters.elementAt(i);
				if (!f.passFilter(call)) {
					break;
				}
			}// only add if we passed all filters
			if (i == filters.size()) {
				result.add(call);
			}
		}
		return result;
	}

	/**
	 * get all Call by Call Providers from the selected rows if no row is
	 * selected return all cbc Providers
	 *
	 * @return the providers
	 */
	public Vector<String> getCbCProviders(int[] rows) {
		Vector<String> callByCallProviders = new Vector<String>();
		for (int i = 0; i < rows.length; i++) {
			Call call = filteredCallerData.get(rows[i]);
			addIfCbCProvider(callByCallProviders, call);
		}
		return callByCallProviders;
	}

	/**
	 * Adds a route to the CbCProviders Vector, if the route is a CbCProvider
	 *
	 * @param callByCallProviders
	 *            the Vector of CbCProviders
	 * @param call
	 *            the call with the route, if it is a route to a CbCProvider it
	 *            will be added
	 */
	// FIXME
	private void addIfCbCProvider(Vector<String> callByCallProviders, Call call) {
		String provider = "";
		if (call.getPhoneNumber() != null) {
			provider = call.getPhoneNumber().getCallByCall();
			// Debug.msg("call.getPhoneNumber().getCallByCall(): "+
			// call.getPhoneNumber().getCallByCall());
			if (!provider.equals("")) { //$NON-NLS-1$
				if (!callByCallProviders.contains(provider)) {
					callByCallProviders.add(provider);
				}
			}
		}
	}

	/**
	 *
	 * @return all CallByCallProviders
	 */
	public Vector<String> getCbCProviders() {
		Vector<String> callByCallProviders = new Vector<String>();
		for (int i = 0; i < unfilteredCallerData.size(); i++) {
			Call call = unfilteredCallerData.get(i);
			addIfCbCProvider(callByCallProviders, call);
		}
		return callByCallProviders;
	}

	/**
	 * returns all selected Providers
	 *
	 * @return the providers
	 */
	public Vector<String> getSelectedProviders(int[] rows) {
		Vector<String> selectedProviders = new Vector<String>();
		for (int i = 0; i < rows.length; i++) {
			Call call = filteredCallerData.get(rows[i]);
			if (!call.getRoute().equals("")) {
				if (!selectedProviders.contains(call.getRoute())) {
					selectedProviders.add(call.getRoute());
				}
			}
		}
		return selectedProviders;
	}

	/**
	 *
	 * @return all SipProviders of the callertable
	 */
	public Vector<String> getAllSipProviders() {
		Vector<String> sipProviders = new Vector<String>();
		for (int i = 0; i < filteredCallerData.size(); i++) {
			Call call = filteredCallerData.get(i);
			// Debug.msg("route:"+route);
			// Debug.msg("callrouteType:"+call.getRouteType());
			if (!call.getRoute().equals("")) {
				if (call.getRouteType() == Call.ROUTE_SIP) {
					if (!sipProviders.contains(call.getRoute())) {
						sipProviders.add(call.getRoute());
					}
				}
			}
		}
		return sipProviders;
	}

	public void findAllPersons() {
		// TODO updaten wenn neue call oder personen oder rufnummern hinzukommen
		// oder alte gelöscht werden
		Debug.msg("searching all Persons for the CallerList...");
		if (phonebook == null) {
			Debug.err("set phonebook first!");
		}
		if (!phonebook.getAllLastCallsSearched()) {
			Debug.err("searchAllLastCalls in the phonebook first");
		}
		Call call;
		Person person;
		for (int i = 0; i < unfilteredCallerData.size(); i++) {
			call = unfilteredCallerData.get(i);
			person = phonebook.findPerson(call);
			call.setPerson(person);
		}
		Debug.msg("...done");
	}

	public void update() {
		filteredCallerData = filterData(unfilteredCallerData);
		sortAllFilteredRowsBy(sortColumn, sortDirection);
		fireTableDataChanged();
	}

	/**
	 * looks up either all filtered/displayed calls or all calls
	 *
	 * @param filteredOnly
	 *            if false it will lookup all calls
	 * @param searchAlsoDummyEntries
	 *            if true, it will also lookup dummy entries
	 * @return
	 */
	public void reverseLookup(boolean filteredOnly, boolean searchAlsoForDummyEntries) {
		JFritz.getJframe().selectLookupButton(true);
		JFritz.getJframe().setLookupBusy(true);
		Vector<PhoneNumber> numbers = new Vector<PhoneNumber>();
		if (filteredOnly) {
			for (int i = 0; i < filteredCallerData.size(); i++) {
				Call call = filteredCallerData.get(i);
				if (call.getPhoneNumber() != null) {
					Person foundPerson = phonebook.findPerson(call);
					if ((foundPerson == null ) || (searchAlsoForDummyEntries && foundPerson.isDummy())
							&& !numbers.contains(call.getPhoneNumber())) {
						numbers.add(call.getPhoneNumber());
					}
				}
			}
		} else {
			numbers = getAllUnknownEntries(searchAlsoForDummyEntries);
		}
		reverseLookup(numbers);
	}
	/**
	 * Returns all unknown entries
	 * @param searchAlsoForDummyEntries
	 *            if true, it will also lookup dummy entries
	 * @return all unknown entries
	 */
	public Vector<PhoneNumber> getAllUnknownEntries(boolean searchAlsoForDummyEntries){
		Vector<PhoneNumber> numbers = new Vector<PhoneNumber>();
		for (int i = 0; i < unfilteredCallerData.size(); i++) {
			Call call = unfilteredCallerData.get(i);
			if (call.getPhoneNumber() != null) {
				Person foundPerson = phonebook.findPerson(call);
				if ((foundPerson == null || (searchAlsoForDummyEntries && foundPerson.isDummy()))
						&& !numbers.contains(call.getPhoneNumber())) {
					numbers.add(call.getPhoneNumber());
				}
			}
		}

		return numbers;
	}

	/**
	 * does reverse lookup (find the name and address for a given phone number
	 *
	 * @param rows
	 *            the rows, wich are selected for reverse lookup
	 */

	public void doReverseLookup(int[] rows) {
		Vector<PhoneNumber> numbers = new Vector<PhoneNumber>();
		// nur für markierte Einträge ReverseLookup
		// durchführen
		for (int i = 0; i < rows.length; i++) {
			Call call = filteredCallerData.get(rows[i]);
			if (call.getPhoneNumber() != null) {
				numbers.add(call.getPhoneNumber());
			}
		}
		reverseLookup(numbers);
	}

	/**
	 * Does a reverse lookup for all numbers in vector "numbers"
	 *
	 * @param numbers,
	 *            a vector of numbers to do reverse lookup on
	 */
	public void reverseLookup(Vector<PhoneNumber> numbers) {
		JFritz.getJframe().selectLookupButton(true);
		JFritz.getJframe().setLookupBusy(true);
		ReverseLookup.lookup(numbers, this, false);
	}

	/**
	 * for the LookupObserver
	 */
	public void personsFound(Vector<Person> persons) {
		if (persons != null) {
			phonebook.addEntries(persons);
			this.fireTableDataChanged();
		}
		JFritz.getJframe().selectLookupButton(false);
		JFritz.getJframe().setLookupBusy(false);

	}

	/**
	 * for the LookupObserver
	 */
	public void percentOfLookupDone(float f) {
		// TODO Auto-generated method stub

	}

	/**
	 * for the LookupObserver
	 */
	public void saveFoundEntries(Vector<Person> persons) {
		if (persons != null) {
			phonebook.addEntries(persons);
			this.fireTableDataChanged();
		}
	}

	public void setPhoneBook(PhoneBook phonebook) {
		this.phonebook = phonebook;

	}

	public void setPerson(Person person, Call call) {
		if (call.getPhoneNumber() != null) { // no empty numbers
			if (person == null) {
				call.setPerson(null);
			} else {
				if (call.getPerson() == null) {
					if (!person.isEmpty()) {
						call.setPerson(person.clone());
					}
				} else if (!call.getPerson().equals(person)) {
					call.setPerson(person.clone());
				}
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Aktualisiert diejenigen Anrufe mit den Nummern aus dem Vector
	 * phoneNumbers
	 *
	 * @param person,
	 *            die neuen Personendaten
	 * @param phoneNumbers,
	 *            die zu aktualisierenden Rufnummern
	 */
	public void updatePersonInCalls(Person person,
			Vector<PhoneNumber> phoneNumbers) {
		Enumeration<Call> en = unfilteredCallerData.elements();
		while (en.hasMoreElements()) {
			Call call = en.nextElement();
			if (phoneNumbers.contains(call.getPhoneNumber())) {
				call.setPerson(person);
			}
		}
		update();
	}

	public PhoneBook getPhoneBook() {
		return phonebook;
	}

	public void stopLookup(){
		ReverseLookup.stopLookup();
	}

	/**
	 *  This function is used to get the date of the last
	 *  call in the list. Used by the network code to get updates
	 *
	 *  @author brian
	 *
	 */
	public synchronized Date getLastCallDate(){

		if(unfilteredCallerData.size() > 0)
			return unfilteredCallerData.firstElement().getCalldate();
		return null;
	}

	/** This function is used by the network code to retrieve all calls
	 * newer than that of the timestamp
	 *
	 * @author brian
	 *
	 * @param timestamp of the last call received
	 * @return a vector of calls newer than the timestamp
	 */
	public synchronized Vector<Call> getNewerCalls(Date timestamp){
		Vector<Call> newerCalls = new Vector<Call>();
		DateFilter dateFilter = new DateFilter(timestamp, new Date(System.currentTimeMillis()));
		for(Call call: unfilteredCallerData){
			if(dateFilter.passFilter(call))
				newerCalls.add(call);
		}

		return newerCalls;
	}

	public Vector<CallFilter> getCallFilters(){
		return filters;
	}

	/**
	 * this function is used for determing the call called in a pop up message
	 *
	 * @param row number of call in table
	 * @return call object
	 */
	public Call getCallAt(int row){
		return filteredCallerData.elementAt(row);
	}

	public void registerProgressListener(IProgressListener listener)
	{
		progressListeners.add(listener);
	}

	public void unregisterProgressListener(IProgressListener listener)
	{
		progressListeners.remove(listener);
	}
}
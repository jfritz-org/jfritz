/*
 *
 * Created on 08.04.2005
 *
 */
package de.moonflower.jfritz.callerlist;

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
import java.util.List;
import java.util.Vector;

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
import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.callerlist.filter.DateFilter;
import de.moonflower.jfritz.importexport.CSVCallerListImport;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.phonebook.PhoneBookListener;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.LookupObserver;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

/**
 * This class manages the caller list.
 *
 */
public class CallerList extends AbstractTableModel
		implements LookupObserver, PhoneBookListener, IProgressListener,
		BoxCallBackListener, CallerListInterface {
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

	private final static String EXPORT_CSV_FORMAT_JFRITZ = "\"CallType\";\"Date\";\"Time\";\"Number\";\"Route\";\"" + //$NON-NLS-1$
			"Port\";\"Duration\";\"Name\";\"Address\";\"City\";\"CallByCall\";\"Comment\""; //$NON-NLS-1$

	private final static String EXPORT_CSV_FORMAT_FRITZBOX = "Typ;Datum;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer"; //$NON-NLS-1$

	// is the type exported from the new firmware
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE = "Typ;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer";

	// Is the type eyported from a 7170
	private final static String EXPORT_CSV_FORMAT_PUSHSERVICE = "Typ; Datum; Rufnummer; Nebenstelle; Eigene Rufnummer; Dauer"; //$NON-NLS-1$

	// is the type exported from a 7170 with a >= XX.04.12
	private final static String EXPORT_CSV_FORMAT_PUSHSERVICE_NEW = "Typ; Datum; Name; Rufnummer; Nebenstelle; Eigene Rufnummer; Dauer";

	private final static String EXPORT_CSV_FORMAT_PUSHSERVICE_ENGLISH = "Type; Date; Number; Extension; Local Number; Duration";

	// english firmware, unknown version
	private final static String EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH = "Typ;Date;Number;Extension;Outgoing Caller ID;Duration";


	private final static String EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_NEW = "Typ;Date;Name;Number;Extension;Outgoing Caller ID;Duration";

	private final static String EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_140426 = "Type;Date;Name;Number;Extension;Outgoing Caller ID;Duration";

	private final static String EXPORT_CSV_FORMAT_JANRUFMONITOR = "Status;Datum;Rufnummer;Name;eigene Rufnummer (MSN);Dauer;";

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

	private NumberCallMultiHashMap hashMap;

	private boolean initStage = true;

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

		hashMap = new NumberCallMultiHashMap();

		sortColumn = 1;
	}

	/**
	 * CallerListListeners are used to passively catch changes to the
	 * data in the call list
	 *
	 * @param l the listener to be added
	 */
	public synchronized void addListener(final CallerListListener listener){
		callListListeners.add(listener);
	}

	/**
	 * CallerListListeners are used to passively catch changes to the
	 * data in the call list
	 *
	 * @param l the listener to be removed
	 */
	public synchronized void removeListener(final CallerListListener listener){
		callListListeners.remove(listener);
	}

	/**
	 * Is used for the clickability!
	 */
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		boolean isEditable;
		final String columnName = getRealColumnName(columnIndex); // NOPMD
		if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
			isEditable = true;
		} else if (columnName.equals(CallerTable.COLUMN_NUMBER)) { //$NON-NLS-1$
			isEditable = true;
		}
		else
		{
			isEditable = false;
		}
		return isEditable;
	}

	/**
	 * @param columnIndex
	 * @return class of column
	 */
	@SuppressWarnings("unchecked")
	public Class getColumnClass(final int columnIndex) {
		Class result;
		final Object o = getValueAt(0, columnIndex); // NOPMD
		if (o == null) {
			result = Object.class;
		} else {
			result = o.getClass();
		}
		return result;
	}

	/**
	 * Saves caller list to xml file.
	 *
	 * @param filename
	 *            Filename to save to
	 * @param wholeCallerList
	 *            Save whole caller list or only selected entries
	 */
	public synchronized void saveToXMLFile(final String filename, final boolean wholeCallerList) {
		Debug.always("Saving to file " + filename); //$NON-NLS-1$
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
				Call currentCall;
				for (int i = 0; i < rows.length; i++) {
					currentCall = filteredCallerData.elementAt(rows[i]);
					pw.write(currentCall.toXML());
					pw.newLine();
				}
			} else if (wholeCallerList) { // Export ALL UNFILTERED Calls
				Enumeration<Call> en = unfilteredCallerData.elements();
				Call call;
				while (en.hasMoreElements()) {
					call = en.nextElement();
					pw.write(call.toXML());
					pw.newLine();
				}
			} else {// Export ALL FILTERED Calls
				Enumeration<Call> en = filteredCallerData.elements();
				Call call;
				while (en.hasMoreElements()) {
					call = en.nextElement();
					pw.write(call.toXML());
					pw.newLine();
				}
			}
			pw.write("</calls>"); //$NON-NLS-1$

			pw.close();
		} catch (UnsupportedEncodingException e) {
			Debug.error("UTF-8 not supported"); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			Debug.error("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		} catch (IOException e) {
			Debug.error("IOException " + filename); //$NON-NLS-1$
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
		Debug.always("Saving to csv file " + filename); //$NON-NLS-1$
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
				Call currentCall;
				for (int i = 0; i < rows.length; i++) {
					currentCall = filteredCallerData.elementAt(rows[i]);
					pw.println(currentCall.toCSV());
				}
			} else if (wholeCallerList) { // Export ALL UNFILTERED Calls
				Enumeration<Call> en = unfilteredCallerData.elements();
				Call call;
				while (en.hasMoreElements()) {
					call = en.nextElement();
					pw.println(call.toCSV());
				}
			} else { // Export ALL FILTERED Calls
				Enumeration<Call> en = filteredCallerData.elements();
				Call call;
				while (en.hasMoreElements()) {
					call = en.nextElement();
					pw.println(call.toCSV());
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.error("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
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
			Debug.error("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.error("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.error(e.toString());
				Debug
						.errDlg("STRUKTURÄNDERUNG!\n\nBitte in der Datei jfritz.calls.xml\n " //$NON-NLS-1$
								+ "die Zeichenkette \"calls.dtd\" durch\n \"" //$NON-NLS-1$
								+ CALLS_DTD_URI + "\"\n ersetzen!"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			Debug.error("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		} finally {
			initStage = false;
		}
		JFritz.getPhonebook().addListener(this);
	}

	/**
	 * Adds an entry to the call list
	 * Note: After all import processes make sure to call fireUpdateCallVector()
	 *
	 *
	 * @author Brian Jensen
	 */
	private synchronized void addEntry(Call call) {
		newCalls.add(call);
		if (call.getPhoneNumber() != null)
		{
			hashMap.addCall(call.getPhoneNumber(), call);
		}
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

		filterNewCalls(newCalls);
		Debug.debug("Adding " + newCalls.size() + " new calls.");

		for(Call call: newCalls)
		{
			addEntry(call);
			newEntries++;
		}

		if ((!initStage) && (newEntries > 0)) {

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
			if (JFritzUtils.parseBoolean(Main.getProperty("option.notifyOnCalls"))) {
				JFritz.infoMsg(msg);
			}

			// Make back-up after fetching the caller list?
			if (JFritzUtils.parseBoolean(Main.getProperty(
							"option.createBackupAfterFetch")))
			{
				doBackup();
			}
		}
	}

	/**
	 * Adapts incoming list of calls.
	 * Removes all elements which are already in our unfilteredCallerData from newCalls.
	 * @param newCalls
	 */
	private void filterNewCalls(Vector<Call> newCalls)
	{
		Vector<Call> copyCalls = (Vector<Call>) unfilteredCallerData.clone();
		for (int i=0; i<newCalls.size(); i++)
		{
			Call call = newCalls.get(i);
			Enumeration<Call> enList = copyCalls.elements();
			boolean found = false;
			while (enList.hasMoreElements() && !found)
			{
				Call callInList = enList.nextElement();
				if (callInList.equals(call)) {
					// if this call is in both lists remove it from both lists
					copyCalls.remove(callInList);
					newCalls.remove(call);
					found = true;
					i--; // go to previous element to get next element
				}
			}
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
			for (Call c:removeCalls)
			{
				hashMap.deleteCall(c.getPhoneNumber(), c);
			}

			for(CallerListListener listener: callListListeners)
			{
				listener.callsRemoved(removeCalls);
			}

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
	private synchronized boolean contains(Call newCall) {
		int left, right, middle;
		left = 0;
		right = unfilteredCallerData.size() - 1;

		if (unfilteredCallerData.isEmpty()) {
			return false;
		}

		Call c;
		while (left <= right) {
			middle = ((right - left) / 2) + left;

			c = unfilteredCallerData.elementAt(middle);
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
	 * This method synchronizes the main call vector with the recently
	 * added calls per addEntry(Call call)
	 *
	 * NOTE: This method must be called after any calls have been added but
	 * should not be called until done importing all calls
	 *
	 * @author Brian Jensen
	 *
	 */
	private synchronized void fireUpdateCallVector() {
		// update the call list and then sort it
		unfilteredCallerData.addAll(newCalls);

		for(CallerListListener l: callListListeners)
			l.callsAdded((Vector<Call>) newCalls.clone());

		newCalls.clear();
		sortAllUnfilteredRows();

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
			return JFritz.getPhonebook().findPerson(call);
		} else if (columnName.equals(CallerTable.COLUMN_PORT)) { //$NON-NLS-1$
			return call.getPort();
		} else if (columnName.equals(CallerTable.COLUMN_ROUTE)) { //$NON-NLS-1$
			return call.getRoute();
		} else if (columnName.equals(CallerTable.COLUMN_DURATION)) { //$NON-NLS-1$
			return Integer.toString(call.getDuration());
		} else if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
			return call.getComment();
		} else if (columnName.equals(CallerTable.COLUMN_PICTURE)) { //$NON-NLS-1$
			Person p = JFritz.getPhonebook().findPerson(call);
			if (p != null)
				return p.getScaledPicture();
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
		if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
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
		Debug.debug("Sorting unfiltered data"); //$NON-NLS-1$

		int indexOfDate = -1;
		String columnName = "";
		for (int i = 0; i < getColumnCount(); i++) {
			columnName = getRealColumnName(i);
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
				Person p1 = JFritz.getPhonebook().findPerson(call1);
				Person p2 = JFritz.getPhonebook().findPerson(call2);
				if (p1 != null) {
					o1 = p1.getFullname().toUpperCase();
				} else {
					o1 = null;
				}
				if (p2 != null) {
					o2 = p2.getFullname().toUpperCase();
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
		Call call;
		while (en.hasMoreElements()) {
			call = en.nextElement();
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
		Call call;
		while (en.hasMoreElements()) {
			call = en.nextElement();
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
		boolean found = false;
		Vector<PhoneNumber> numbers = person.getNumbers();
		if (numbers.size() > 0) {
			Call result = new Call(new CallType(CallType.CALLIN_STR),
					new Date(0), new PhoneNumber("", false),
					new Port(0, "", "-1", "-1"), "route", 0);
			for (PhoneNumber num:numbers)
			{
				if ("main".equals(num.getType())){
					for (int i=0; i<unfilteredCallerData.size(); i++)
					{
						Call c = unfilteredCallerData.get(i);
						if (c.getCalldate().after(result.getCalldate()))
						{
							result = c;
							found = true;
						}
					}
				}
				else
				{
					List<Call> l = hashMap.getCall(num);
					if (l != null)
					{
						for (Call c:l)
						{
							if (c.getCalldate().after(result.getCalldate()))
							{
								result = c;
								found = true;
							}
						}
					}
				}
			}
			if (!found) {
				result = null;
			}
			return result;
		}

		return null;
//		Vector<PhoneNumber> numbers = person.getNumbers();
//		if (numbers.size() > 0) {
//			Enumeration<Call> en = unfilteredCallerData.elements();
//			Call call;
//			while (en.hasMoreElements()) {
//				call = en.nextElement();
//				if (call.getPhoneNumber() != null) {
//					for (int i = 0; i < numbers.size(); i++) {
//						if (call.getPhoneNumber().getIntNumber().equals(
//								((PhoneNumber) numbers.get(i)).getIntNumber())) {
//							return call;
//						}
//					}
//				}
//			}
//		}
//		return null;
	}

	public void clearList() {
		Debug.info("Clearing caller Table"); //$NON-NLS-1$
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
	public synchronized void removeEntries(int[] rows) {

		Vector<Call> removedCalls = new Vector<Call>(rows.length);
		if (rows.length > 0) {
			Call call;
			Person p;
			for (int i = 0; i < rows.length; i++) {
				call = filteredCallerData.get(rows[i]);
				removedCalls.add(call);
				unfilteredCallerData.remove(call);
				hashMap.deleteCall(call.getPhoneNumber(), call);
			}

			//notify all listeners that calls have been removed
			for(CallerListListener l: callListListeners)
			{
				l.callsRemoved((Vector) removedCalls.clone());
			}

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
			TableColumn col;
			while (en.hasMoreElements()) {
				col = (TableColumn) en.nextElement();
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

	public synchronized void importFromCSVFile(final String file) {
		CSVCallerListImport csvImport = new CSVCallerListImport(file);
		csvImport.openFile();
		String firstLine = csvImport.readHeader(0);
		Debug.debug("Header: " + firstLine);
		if (firstLine.equals(EXPORT_CSV_FORMAT_JFRITZ)) {
			csvImport.setSeparaor(";");
			csvImport.mapColumn(0, CallerTable.COLUMN_TYPE);
			csvImport.mapColumn(1, CallerTable.COLUMN_DATE);
			csvImport.mapColumn(2, CallerTable.COLUMN_DATE);
			csvImport.mapColumn(3, CallerTable.COLUMN_NUMBER);
			csvImport.mapColumn(4, CallerTable.COLUMN_ROUTE);
			csvImport.mapColumn(5, CallerTable.COLUMN_PORT);
			csvImport.mapColumn(6, CallerTable.COLUMN_DURATION);
			csvImport.mapColumn(10, CallerTable.COLUMN_CALL_BY_CALL);
			csvImport.mapColumn(11, CallerTable.COLUMN_COMMENT);
		} else if (firstLine.startsWith("sep=")) {
			String[] split = firstLine.split("=");
			Debug.debug("Found separator: " + split[1]);
			csvImport.setSeparaor(split[1]);
			String secondLine = csvImport.readHeader(1);
			if (   secondLine.equals(EXPORT_CSV_FORMAT_FRITZBOX_NEWFIRMWARE)
				|| secondLine.equals(EXPORT_CSV_FORMAT_FRITZBOX)
				|| secondLine.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH)
				|| secondLine.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_140426)
				|| secondLine.equals(EXPORT_CSV_FORMAT_FRITZBOX_ENGLISH_NEW)
				|| secondLine.equals(EXPORT_CSV_FORMAT_PUSHSERVICE)
				|| secondLine.equals(EXPORT_CSV_FORMAT_PUSHSERVICE_ENGLISH)
				|| secondLine.equals(EXPORT_CSV_FORMAT_PUSHSERVICE_NEW)
					) {
				csvImport.mapColumn(0, CallerTable.COLUMN_TYPE);
				csvImport.mapColumn(1, CallerTable.COLUMN_DATE);
				csvImport.mapColumn(3, CallerTable.COLUMN_NUMBER);
				csvImport.mapColumn(4, CallerTable.COLUMN_PORT);
				csvImport.mapColumn(5, CallerTable.COLUMN_ROUTE);
				csvImport.mapColumn(6, CallerTable.COLUMN_DURATION);
			}
		} else if (firstLine.equals(EXPORT_CSV_FORMAT_JANRUFMONITOR)) {
			// away;20.08.2009 12:28;+49 (152) number;Name;MSN;27 min ;
			csvImport.mapColumn(0, CallerTable.COLUMN_TYPE);
			csvImport.mapColumn(1, CallerTable.COLUMN_DATE);
			csvImport.mapColumn(2, CallerTable.COLUMN_NUMBER);
			csvImport.mapColumn(4, CallerTable.COLUMN_ROUTE);
			csvImport.mapColumn(5, CallerTable.COLUMN_DURATION);
		}
		csvImport.csvImport();
		csvImport.closeFile();
		int newEntries = csvImport.getImportedCalls().size();
		if (newEntries > 0)
		{
			JFritz.getCallerList().addEntries(csvImport.getImportedCalls());

			Debug.debug(newEntries + " New entries processed");

			fireUpdateCallVector();

			saveToXMLFile(Main.SAVE_DIR + JFritz.CALLS_FILE, true);
		}
	}

	/**
	 * @author Brian Jensen
	 *
	 * function first splits the line into substrings, then strips the
	 * quotationmarks(do those have to be?) functions parses according to the
	 * format EXPORT_CSV_FORMAT_JFRITZ
	 *
	 * Header:
	 * "CallType";"Date";"Time";"Number";"Route";"Port";"Duration";"Name";"Address";"City";"CallByCall";"Comment"
	 * Data:
	 * "Outgoing";"02.09.2009";"20:09";"+49YYYXXXXXXX";"MSN-NR";"ISDN";"240";"Mustermann";"";"Stadt";"01072";"Kommentar"
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
				Debug.error("Invalid CSV format, incorrect number of fields!"); //$NON-NLS-1$
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
			Debug.error("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if ((field[1] != null) && (field[2] != null)) {

			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1] + " " + field[2]); //$NON-NLS-1$,  //$NON-NLS-2$
			} catch (ParseException e) {
				Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
			return null;
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
		call = new Call(calltype, calldate, number,
				new Port(0, field[5], "-1", "-1"),
				field[4], Integer.parseInt(field[6]));

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
	 * Header:
	 * Typ;Datum;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer
	 * 3;04.09.09 13:13;;0162XXXXXXX;ISDN Geraet;XXXXXX;0:02
	 * Data:
	 *
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
				Debug.error("Invalid CSV format, incorrect number of fields!"); // if
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
			Debug.error("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
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
//		if (field[3].equals("FON 1")) {
//			field[3] = "0";
//		} else if (field[3].equals("FON 2")) {
//			field[3] = "1";
//		} else if (field[3].equals("FON 3")) {
//			field[3] = "2";
//		} else if (field[3].equals("Durchwahl")) {
//			field[3] = "3";
//		} else if (field[3].equals("FON S0")) {
//			field[3] = "4";
//		} else if (field[3].equals("DECT 1")) {
//			field[3] = "10";
//		} else if (field[3].equals("DECT 2")) {
//			field[3] = "11";
//		} else if (field[3].equals("DECT 3")) {
//			field[3] = "12";
//		} else if (field[3].equals("DECT 4")) {
//			field[3] = "13";
//		} else if (field[3].equals("DECT 5")) {
//			field[3] = "14";
//		} else if (field[3].equals("DECT 6")) {
//			field[3] = "15";
//		} else if (field[3].equals("DATA S0")) {
//			field[3] = "36";
//		}

		// make the call object and exit
		call = new Call(calltype, calldate, number,
				new Port(0, field[3], "-1", "-1"),
				field[4], Integer.parseInt(time[0])
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
				Debug.error("Invalid CSV format, incorrect number fields!");
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
			Debug.error("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
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
//		if (field[4].equals("FON 1")) {
//			field[4] = "0";
//		} else if (field[4].equals("FON 2")) {
//			field[4] = "1";
//		} else if (field[4].equals("FON 3")) {
//			field[4] = "2";
//		} else if (field[4].equals("Durchwahl")) {
//			field[4] = "3";
//		} else if (field[4].equals("DECT 1")) {
//			field[4] = "10";
//		} else if (field[4].equals("DECT 2")) {
//			field[4] = "11";
//		} else if (field[4].equals("DECT 3")) {
//			field[4] = "12";
//		} else if (field[4].equals("DECT 4")) {
//			field[4] = "13";
//		} else if (field[4].equals("DECT 5")) {
//			field[4] = "14";
//		} else if (field[4].equals("DECT 6")) {
//			field[4] = "15";
//		} else if (field[4].equals("FON S0")) {
//			field[4] = "4";
//		} else if (field[4].equals("DATA S0")) {
//			field[4] = "36";
//		}

		// make the call object and exit
		call = new Call(calltype, calldate, number,
				new Port(0, field[4], "-1", "-1"),
				field[5], Integer.parseInt(time[0])
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
				Debug.error("Invalid CSV format, incorrect number of fields"); // if
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
			Debug.error("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
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
//		if (field[3].equals("FON 1")) {
//			field[3] = "0";
//		} else if (field[3].equals("FON 2")) {
//			field[3] = "1";
//		} else if (field[3].equals("FON 3")) {
//			field[3] = "2";
//		} else if (field[3].equals("Durchwahl")) {
//			field[3] = "3";
//		} else if (field[3].equals("FON S0")) {
//			field[3] = "4";
//		} else if (field[3].equals("DECT 1")) {
//			field[3] = "10";
//		} else if (field[3].equals("DECT 2")) {
//			field[3] = "11";
//		} else if (field[3].equals("DECT 3")) {
//			field[3] = "12";
//		} else if (field[3].equals("DECT 4")) {
//			field[3] = "13";
//		} else if (field[3].equals("DECT 5")) {
//			field[3] = "14";
//		} else if (field[3].equals("DECT 6")) {
//			field[3] = "15";
//		} else if (field[3].equals("DATA S0")) {
//			field[3] = "36";
//		}

		// make the call object and exit
		call = new Call(calltype, calldate, number,
				new Port(0, field[3], "-1", "-1"),
				field[4], Integer.parseInt(time[0])
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
				Debug.error("Invalid CSV format, incorrect number of fields"); // if
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
			Debug.error("Invalid Call type in CSV entry!"); //$NON-NLS-1$
			return null;
		}

		// Call date and time
		if (field[1] != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(field[1]); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
				return null;
			}
		} else {
			Debug.error("Invalid date format in csv entry!"); //$NON-NLS-1$
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
		//Apparently with the new 14.04.26 the duration is stored as hours.minutes
		if(time.length != 2){
			time = field[6].split("\\.");
		}

		// change the port to fit the jfritz naming convention
//		if (field[4].equals("FON 1")) {
//			field[4] = "0";
//		} else if (field[4].equals("FON 2")) {
//			field[4] = "1";
//		} else if (field[4].equals("FON 3")) {
//			field[4] = "2";
//		} else if (field[4].equals("Durchwahl")) {
//			field[4] = "3";
//		} else if (field[4].equals("FON S0")) {
//			field[4] = "4";
//		} else if (field[4].equals("DECT 1")) {
//			field[4] = "10";
//		} else if (field[4].equals("DECT 2")) {
//			field[4] = "11";
//		} else if (field[4].equals("DECT 3")) {
//			field[4] = "12";
//		} else if (field[4].equals("DECT 4")) {
//			field[4] = "13";
//		} else if (field[4].equals("DECT 5")) {
//			field[4] = "14";
//		} else if (field[4].equals("DECT 6")) {
//			field[4] = "15";
//		} else if (field[4].equals("DATA S0")) {
//			field[4] = "36";
//		}

		// make the call object and exit
		call = new Call(calltype, calldate, number,
				new Port(0, field[4], "-1", "-1"),
				field[5], Integer.parseInt(time[0])
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
		Call call;
		for (int i = 0; i < rows.length; i++) {
			call = filteredCallerData.get(rows[i]);
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
		Call call;
		for (int i = 0; i < unfilteredCallerData.size(); i++) {
			call = unfilteredCallerData.get(i);
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
		Call call;
		for (int i = 0; i < rows.length; i++) {
			call = filteredCallerData.get(rows[i]);
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
		Call call;
		for (int i = 0; i < filteredCallerData.size(); i++) {
			call = filteredCallerData.get(i);
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
			Call call;
			Person foundPerson;
			for (int i = 0; i < filteredCallerData.size(); i++) {
				call = filteredCallerData.get(i);
				if (call.getPhoneNumber() != null) {
					foundPerson = phonebook.findPerson(call);
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
		Call call;
		Person foundPerson;
		for (int i = 0; i < unfilteredCallerData.size(); i++) {
			call = unfilteredCallerData.get(i);
			if (call.getPhoneNumber() != null) {
				foundPerson = phonebook.findPerson(call);
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
		Call call;
		for (int i = 0; i < rows.length; i++) {
			call = filteredCallerData.get(rows[i]);
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
		if (persons != null && persons.size() > 0) {
			phonebook.addEntries(persons);
			update();
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
			update();
		}
	}

	public void setPhoneBook(PhoneBook phonebook) {
		this.phonebook = phonebook;

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

	public void contactUpdated(Person original, Person updated) {
		update();
	}

	public void contactsAdded(Vector<Person> newContacts) {
		update();
	}

	public void contactsRemoved(Vector<Person> removedContacts) {
		update();
	}

	public void finished(Vector<Call> newCalls) {
		if (newCalls != null)
		{
			addEntries(newCalls);
		}
	}

	public void setMax(int max) {
		// do nothing
	}

	public void setMin(int min) {
		// do nothing
	}

	public void setProgress(int progress) {
		// do nothing
	}

	public boolean finishGetCallerList(Vector<Call> calls) {
		Vector<Call> clone = (Vector<Call>)calls.clone();
		filterNewCalls(clone);
		if (clone.size() == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public Call getFilteredCall(int index) {
		return filteredCallerData.get(index);
	}

	public Call getUnfilteredCall(int index) {
		return filteredCallerData.get(index);
	}

	public Vector<String> getUsedProviderList() {
		Vector<String> providers = new Vector<String>(10);
		for (Call call: unfilteredCallerData)
		{
			if (!providers.contains(call.getRoute()))
			{
				providers.add(call.getRoute());
			}
		}
		return providers;
	}

	public Vector<String> getUsedPortsList() {
		Vector<String> ports = new Vector<String>(10);
		for (Call call: unfilteredCallerData)
		{
			if (!ports.contains(call.getPort().getName())
					&& (!call.getPort().getName().equals("")))
			{
				ports.add(call.getPort().getName());
			}
		}
		return ports;
	}

	public Vector<Call> getUnfilteredCallVector() {
		return unfilteredCallerData;
	}
}
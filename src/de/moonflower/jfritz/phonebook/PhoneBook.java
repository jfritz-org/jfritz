package de.moonflower.jfritz.phonebook;

/**
 * This is the phonebook
 *
 * @author Robert Palmer
 *
 * TODO: Cellrenderer for PrivateCell
 *
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.CallerList;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.LookupObserver;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class PhoneBook extends AbstractTableModel implements LookupObserver {
	private static final long serialVersionUID = 1;

	private static final String PHONEBOOK_DTD_URI = "http://jfritz.moonflower.de/dtd/phonebook.dtd"; //$NON-NLS-1$

	// TODO Write correct dtd
	private static final String PHONEBOOK_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //$NON-NLS-1$
			+ "<!-- DTD for JFritz phonebook -->" //$NON-NLS-1$
			+ "<!ELEMENT firstname (commment?,entry*)>" //$NON-NLS-1$
			+ "<!ELEMENT middlename (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT lastname (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT entry (firstname?,middlename?,lastname?)>"; //$NON-NLS-1$

	private final String columnNames[] = {
			"private_entry", "fullName", "telephoneNumber", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			"address", "city", "last_call" }; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

	private static final String PATTERN_THUNDERBRID_CSV = ","; //$NON-NLS-1$

	private Vector<Person> filteredPersons;

	private Vector<Person> unfilteredPersons;

	private Vector<PhoneBookListener> listeners;

	private String fileLocation;

	private CallerList callerList;

	private boolean allLastCallsSearched = false;

	/**
	 * A vector of Persons that will match any search filter. In other words: a
	 * list of sticky Persons, that will always show up. Used to ensure that a
	 * newly created Person can be seen by the user, even if there is a filter
	 * active
	 */
	private Vector<Person> filterExceptions;

	private int sortColumn = 1;

	private boolean sortDirection = true;

	public PhoneBook(String fileLocation) {
		this.fileLocation = fileLocation;
		filteredPersons = new Vector<Person>();
		unfilteredPersons = new Vector<Person>();
		filterExceptions = new Vector<Person>();
		listeners = new Vector<PhoneBookListener>();

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
		Collections.sort(filteredPersons, new ColumnSorter(col, asc));
		// Debug.msg("last calls: "+(t2-t1) + "ms sorting: "+(t3-t2)+"ms");
		fireTableDataChanged();
	}

	public synchronized void addListener(PhoneBookListener l){
		listeners.add(l);
	}

	public synchronized void removeListener(PhoneBookListener l){
		listeners.remove(l);
	}

	/**
	 * Sort table model rows by a specific column. The direction is determined
	 * automatically.
	 *
	 * @param col
	 *            Index of column to be sorted by
	 */
	public void sortAllFilteredRowsBy(int col) {
		if ((sortColumn == col) && (sortDirection == true)) {
			sortDirection = false;
		} else {
			sortColumn = col;
			sortDirection = true;
		}
		sortAllFilteredRowsBy(sortColumn, sortDirection);
	}

	/**
	 *
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		String columnName = getColumnName(columnIndex);
		// If the wahlhilfe doesnt work, check here again!
		if (columnName.equals(Main.getMessage("telephoneNumber"))) {
			return true;
		}
		return false;
	}

	/**
	 * Sort table model rows automatically.
	 *
	 */
	public void sortAllFilteredRows() {
		sortAllFilteredRowsBy(sortColumn, sortDirection);
	}

	public void sortAllUnfilteredRows() {
		Debug.msg("Sorting unfiltered data"); //$NON-NLS-1$
		Collections.sort(unfilteredPersons, new ColumnSorter(1, true));
		// Resort filtered data
		Collections.sort(filteredPersons, new ColumnSorter(sortColumn,
				sortDirection));
		updateFilter();
		fireTableStructureChanged();
	}

	/**
	 * This comparator is used to sort vectors of data
	 */
	public class ColumnSorter implements Comparator<Person> {
		int colIndex;

		boolean ascending;

		ColumnSorter(int colIndex, boolean ascending) {
			this.colIndex = colIndex;
			this.ascending = ascending;
		}

		/*
		 * public int compare(Object a, Object b) { Person p1 = (Person) a;
		 * Person p2 = (Person) b; return compare (p1,p2); }
		 */
		// FIXME
		public int compare(Person p1, Person p2) {
			Object o1, o2;
			switch (colIndex) {
			case 0:
				o1 = Boolean.toString(p1.isPrivateEntry());
				o2 = Boolean.toString(p2.isPrivateEntry());
				break;
			case 1:
				o1 = p1.getFullname().toUpperCase();
				o2 = p2.getFullname().toUpperCase();
				break;
			case 2:
				o1 = ""; //$NON-NLS-1$
				o2 = ""; //$NON-NLS-1$
				if (p1.getStandardTelephoneNumber() != null) {
					o1 = p1.getStandardTelephoneNumber().toString();
				}
				if (p2.getStandardTelephoneNumber() != null) {
					o2 = p2.getStandardTelephoneNumber().toString();
				}
				break;
			case 3:
				o1 = p1.getStreet().toUpperCase();
				o2 = p2.getStreet().toUpperCase();
				break;
			case 4:
				o1 = p1.getPostalCode() + p1.getCity().toUpperCase();
				o2 = p2.getPostalCode() + p2.getCity().toUpperCase();
				break;
			case 5:
				o1 = ""; //$NON-NLS-1$
				o2 = ""; //$NON-NLS-1$
				Call call1 = p1.getLastCall();
				if (call1 != null) {
					o1 = call1.getCalldate();
				}
				Call call2 = p2.getLastCall();
				if (call2 != null) {
					o2 = call2.getCalldate();
				}
				break;
			default:
				o1 = p1.getFullname();
				o2 = p2.getFullname();
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
	}

	public Vector<Person> getFilteredPersons() {
		return filteredPersons;
	}

	/**
	 * @author haeusler DATE: 02.04.06 Adds a Person to the list of
	 *         filterExceptions.
	 * @param nonFilteredPerson
	 * @see #filterExceptions
	 */
	public void addFilterException(Person nonFilteredPerson) {
		filterExceptions.add(nonFilteredPerson);
	}

	/**
	 * TODO: wird noch nicht benutzt does reverse lookup (find the name and
	 * address for a given phone number
	 *
	 * @param rows
	 *            the rows, wich are selected for reverse lookup
	 */
	public void doReverseLookup(int[] rows) {
		if (rows.length > 0) { // nur für markierte Einträge ReverseLookup
			// durchführen
			Vector<PhoneNumber> numbers = new Vector<PhoneNumber>();
			for (int i = 0; i < rows.length; i++) {
				Person person = getFilteredPersons().get(rows[i]);
				numbers.addAll(person.getNumbers());
			}
			ReverseLookup.lookup(numbers, this, false);
		} else { // Für alle Einträge ReverseLookup durchführen
			reverseLookupPersons(filteredPersons);
		}
	}

	public void personsFound(Vector persons) {
		if (persons != null) {
			addEntries(persons);
			fireTableDataChanged();
		}
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
	public void saveFoundEntries(Vector persons) {
		if (persons != null) {
			addEntries(persons);
			fireTableDataChanged();
		}
	}


	/**
	 * Does a reverse lookup on all calls
	 *
	 * @param calls,
	 *            calls to do reverse lookup on
	 */
	public void reverseLookupPersons(Vector<Person> persons) {
		Debug.msg("Doing reverse Lookup");
		for (int i = 0; i < persons.size(); i++) {
			Person person = persons.get(i);
			callerList.reverseLookup(person.getNumbers());
		}
	}

	/**
	 * Clears the list of filterExceptions.
	 *
	 * @see #filterExceptions
	 */
	public void clearFilterExceptions() {
		filterExceptions.clear();
	}

	/**
	 * This method is called by the network code and reverselookup code
	 * to add many contacts at one
	 *
	 * This method calls addEntry(Person person), to set properties
	 * like last call and set the correct person in the call list.
	 * It then notifies all PhoneBookListener objects of the new data.
	 *
	 * @author brian
	 *
	 * @param persons to be added to the phonebook
	 */
	public synchronized void addEntries(Vector<Person> persons) {

		for(Person person: persons)
			addEntry(person);

		for(PhoneBookListener listener: listeners)
			listener.contactsAdded((Vector) persons.clone());

		updateFilter();
		fireTableDataChanged();
		this.saveToXMLFile(Main.SAVE_DIR + JFritz.PHONEBOOK_FILE);
	}

	/**
	 * This method is called by the network code to remove many contacts at once
	 *
	 * It calls deleteEntry(Person person) for person to ensure all traces are removed in
	 * the callerlist.
	 *
	 * It then notifies all listeners of the removed data.
	 *
	 *
	 * @param persons
	 */
	public synchronized void removeEntries(Vector<Person> persons){

		for(Person person: persons)
			deleteEntry(person);

		for(PhoneBookListener listener: listeners)
			listener.contactsRemoved((Vector) persons.clone());

		updateFilter();
		fireTableDataChanged();
		saveToXMLFile(Main.SAVE_DIR + JFritz.PHONEBOOK_FILE);

	}

	/**
	 * This function is responsible for updating a contact
	 *
	 * It is called by the network code for updating the contact
	 * in the call list and the telephonebook
	 *
	 * @param original the original contact
	 * @param updated the updated data for this contact
	 */
	public synchronized void updateEntry(Person original, Person updated){
		int index = unfilteredPersons.indexOf(original);

		//make sure original contact was in our list first
		if(index >= 0){
			unfilteredPersons.set(index, updated);

			updated.setLastCall(JFritz.getCallerList().findLastCall(updated));
			JFritz.getCallerList().updatePersonInCalls(updated, updated.getNumbers());

			for(PhoneBookListener listener: listeners)
				listener.contactUpdated(original, updated);

			updateFilter();
			fireTableDataChanged();
			saveToXMLFile(Main.SAVE_DIR + JFritz.PHONEBOOK_FILE);

		}
	}

	/**
	 * This function is called once a user has clicked ok on the
	 * edit user pane
	 *
	 * @param original the original data for this contact
	 * @param updated the updated data for this contact
	 */
	public synchronized void notifyListenersOfUpdate(Person original, Person updated){
		for(PhoneBookListener listener: listeners)
			listener.contactUpdated(original, updated);

	}

	/*
	 * inherited from AbstractTableModel
	 */
	public synchronized boolean addEntry(Person newPerson) {
		// TODO: Mergen von Einträgen.
		PhoneNumber pn1 = newPerson.getStandardTelephoneNumber();

		Enumeration<Person> en = unfilteredPersons.elements();
		while (en.hasMoreElements()){
			Person p = en.nextElement();
			if (p.isDummy()
					&& p.getStandardTelephoneNumber() != null
					&& newPerson.getStandardTelephoneNumber() != null
					&& newPerson.getStandardTelephoneNumber().getIntNumber()
							.equals(
									p.getStandardTelephoneNumber()
											.getIntNumber())) {
				deleteEntry(p);
			} else {
				// TODO: merge entries
				// Bisher nur vergleich mit standardrufnummer
				// und hinzufügen, wenn kein Eintrag existiert
				PhoneNumber pn2 = p.getStandardTelephoneNumber();
				if ((pn1 != null) && (pn2 != null)
						&& pn1.getIntNumber().equals(pn2.getIntNumber())) {
					return false;
				}
			}
		}

		newPerson.setLastCall(callerList.findLastCall(newPerson));
		unfilteredPersons.add(newPerson);
		callerList.updatePersonInCalls(newPerson, newPerson.getNumbers());
		return true;
	}

	public void setLastCall(Person p, Call c) {
		int index = unfilteredPersons.indexOf(p);
		unfilteredPersons.get(index).setLastCall(c);
		fireTableDataChanged();
	}

	/**
	 * This method is used for removing a person from the telephonebook and
	 * for removing the reference from call list, if one was present.
	 *
	 * If this method is not called, the person reference in the telephonebook will remain!
	 *
	 * @param person
	 */
	public void deleteEntry(Person person) {
		unfilteredPersons.remove(person);
		Call call = callerList.findLastCall(person);
		Person newPerson = null;
		if (call != null) {
			newPerson = findPerson(call);
			callerList.setPerson(newPerson, call);
		}
		callerList.updatePersonInCalls(newPerson, person.getNumbers());
		//updateFilter();
	}

	/**
	 * Saves phonebook to BIT FBF Dialer file.
	 *
	 * @param filename
	 */
	public synchronized void saveToBITFBFDialerFormat(String filename) {
		Debug.msg("Saving to BIT FBF Dialer file " + filename); //$NON-NLS-1$
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
			Enumeration<Person> en1 = unfilteredPersons.elements();

			Enumeration en2;
			Person current;
			String name;

			String nr;// , type;
			PhoneNumber pn;

			while (en1.hasMoreElements()) {
				current = en1.nextElement();
				name = ""; //$NON-NLS-1$
				if (current.getFullname().length() > 0) {
					if (current.getLastName().length() > 0) {
						name += current.getLastName();
					}
					if ((current.getLastName().length() > 0)
							&& (current.getFirstName().length() > 0)) {
						name += ", "; //$NON-NLS-1$
					}
					if (current.getFirstName().length() > 0) {
						name += current.getFirstName();
					}
					if (current.getCompany().length() > 0) {
						name += " (" + current.getCompany() + ")"; //$NON-NLS-1$,   //$NON-NLS-2$
					}
				} else if (current.getCompany().length() > 0) {
					name += current.getCompany();
				}

				if (name.length() > 0) {
					en2 = current.getNumbers().elements();
					while (en2.hasMoreElements()) {
						pn = (PhoneNumber) en2.nextElement();
						nr = pn.getIntNumber();
						if (nr.startsWith("+49")) {
							nr = "0" + nr.substring(3, nr.length()); //$NON-NLS-1$,  //$NON-NLS-2$
						}
						// type = pn.getType();

						pw.write(nr + "=" + name); //$NON-NLS-1$
						pw.newLine();
					}
				}
			}
			pw.close();
		} catch (Exception e) {
			Debug.err("Could not write file!"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves phonebook to BIT FBF Dialer file.
	 *
	 * @param filename
	 */
	public synchronized void saveToCallMonitorFormat(String filename) {
		Debug.msg("Saving to Call Monitor file " + filename); //$NON-NLS-1$
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
			Enumeration<Person> en1 = unfilteredPersons.elements();

			Enumeration en2;
			Person current;
			String name;

			String nr;// , type;
			PhoneNumber pn;

			while (en1.hasMoreElements()) {
				current = en1.nextElement();
				name = ""; //$NON-NLS-1$
				if (current.getFullname().length() > 0) {
					if (current.getLastName().length() > 0) {
						name += current.getLastName();
					}
					if ((current.getLastName().length() > 0)
							&& (current.getFirstName().length() > 0)) {
						name += ", "; //$NON-NLS-1$
					}
					if (current.getFirstName().length() > 0) {
						name += current.getFirstName();
					}
					if (current.getCompany().length() > 0) {
						name += " (" + current.getCompany() + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
					}
				} else if (current.getCompany().length() > 0) {
					name += current.getCompany();
				}

				if (name.length() > 0) {
					en2 = current.getNumbers().elements();
					while (en2.hasMoreElements()) {
						pn = (PhoneNumber) en2.nextElement();
						nr = pn.getIntNumber();
						if (nr.startsWith("+49")) {
							nr = "0" + nr.substring(3, nr.length()); //$NON-NLS-1$,  //$NON-NLS-2$
						}
						// type = pn.getType();

						pw.write("\"" + name + "\",\"" + nr + "\""); //$NON-NLS-1$, //$NON-NLS-2$,  //$NON-NLS-3$
						pw.newLine();
					}
				}
			}
			pw.close();
		} catch (Exception e) {
			Debug.err("Could not write file!"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves phonebook to xml file.
	 *
	 * @param filename
	 */
	public synchronized void saveToXMLFile(String filename) {
		Debug.msg("Saving to file " + filename); //$NON-NLS-1$
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
			pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
			pw.newLine();
			// pw.write("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
			// + "\">");
			// pw.newLine();
			pw.write("<phonebook>"); //$NON-NLS-1$
			pw.newLine();
			pw.write("<comment>Phonebook for " + Main.PROGRAM_NAME + " v" //$NON-NLS-1$,  //$NON-NLS-2$
					+ Main.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$
			pw.newLine();
			Enumeration<Person> en = unfilteredPersons.elements();
			while (en.hasMoreElements()) {
				Person current = en.nextElement();
				pw
						.write("<entry private=\"" + current.isPrivateEntry() + "\">"); //$NON-NLS-1$,  //$NON-NLS-2$
				pw.newLine();
				if (current.getFullname().length() > 0) {
					pw.write("\t<name>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getFirstName().length() > 0) {
						pw
								.write("\t\t<firstname>" + JFritzUtils.convertSpecialChars(current.getFirstName()) //$NON-NLS-1$
										+ "</firstname>"); //$NON-NLS-1$
					}
					pw.newLine();
					if (current.getLastName().length() > 0) {
						pw
								.write("\t\t<lastname>" + JFritzUtils.convertSpecialChars(current.getLastName()) //$NON-NLS-1$
										+ "</lastname>"); //$NON-NLS-1$
					}
					pw.newLine();
					pw.write("\t</name>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getCompany().length() > 0) {
						pw
								.write("\t<company>" + JFritzUtils.convertSpecialChars(current.getCompany()) //$NON-NLS-1$
										+ "</company>"); //$NON-NLS-1$
					}
					pw.newLine();
				}

				if ((current.getStreet().length() > 0)
						|| (current.getPostalCode().length() > 0)
						|| (current.getCity().length() > 0)) {
					pw.write("\t<address>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getStreet().length() > 0) {
						pw
								.write("\t\t<street>" + JFritzUtils.convertSpecialChars(current.getStreet()) //$NON-NLS-1$
										+ "</street>"); //$NON-NLS-1$
					}
					pw.newLine();
					if (current.getPostalCode().length() > 0) {
						pw
								.write("\t\t<postcode>" + JFritzUtils.convertSpecialChars(current.getPostalCode()) //$NON-NLS-1$
										+ "</postcode>"); //$NON-NLS-1$
					}
					pw.newLine();
					if (current.getCity().length() > 0) {
						pw
								.write("\t\t<city>" + JFritzUtils.convertSpecialChars(current.getCity()) //$NON-NLS-1$
										+ "</city>"); //$NON-NLS-1$
					}
					pw.newLine();
					pw.write("\t</address>"); //$NON-NLS-1$
					pw.newLine();
				}

				pw.write("\t<phonenumbers standard=\"" //$NON-NLS-1$
						+ current.getStandard() + "\">"); //$NON-NLS-1$
				pw.newLine();
				Enumeration en2 = current.getNumbers().elements();
				while (en2.hasMoreElements()) {
					PhoneNumber nr = (PhoneNumber) en2.nextElement();
					pw.write("\t\t<number type=\"" + nr.getType() + "\">" //$NON-NLS-1$,  //$NON-NLS-2$
							+ JFritzUtils
									.convertSpecialChars(nr.getIntNumber())
							+ "</number>"); //$NON-NLS-1$
					pw.newLine();

				}
				pw.write("\t</phonenumbers>"); //$NON-NLS-1$
				pw.newLine();

				if (current.getEmailAddress().length() > 0) {
					pw.write("\t<internet>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getEmailAddress().length() > 0) {
						pw
								.write("\t\t<email>" + JFritzUtils.convertSpecialChars(current.getEmailAddress()) //$NON-NLS-1$
										+ "</email>"); //$NON-NLS-1$
					}
					pw.newLine();
					pw.write("\t</internet>"); //$NON-NLS-1$
					pw.newLine();
				}
				pw.write("</entry>"); //$NON-NLS-1$
				pw.newLine();
			}
			pw.write("</phonebook>"); //$NON-NLS-1$
			pw.newLine();
			pw.close();
		} catch (UnsupportedEncodingException e) {
			Debug.err("UTF-8 not supported."); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		} catch (IOException e) {
			Debug.err("IOException " + filename); //$NON-NLS-1$
		}

		this.callerList.fireTableDataChanged();

	}

	public synchronized void loadFromXMLFile(String filename) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
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
					if (systemId.equals(PHONEBOOK_DTD_URI)) {
						InputSource is;
						is = new InputSource(new StringReader(PHONEBOOK_DTD));
						is.setSystemId(PHONEBOOK_DTD_URI);
						return is;
					}
					throw new SAXException("Invalid system identifier: " //$NON-NLS-1$
							+ systemId);
				}

			});
			reader.setContentHandler(new PhonebookFileXMLHandler(this));
			reader.parse(new InputSource(new FileInputStream(filename)));
			updateFilter();
		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
			Debug.err(e.toString());
			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.err(e.getLocalizedMessage());

				Debug.errDlg("Error on paring " + filename);
			}
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		allLastCallsSearched = true;
		updateFilter();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Person person = filteredPersons.get(rowIndex);
		switch (columnIndex) {
		case 0:
			if (person.isPrivateEntry()) {
				return "YES"; //$NON-NLS-1$
			} else {
				return "NO"; //$NON-NLS-1$
			}
		case 1:
			return person.getFullname();
		case 2:
			return person.getStandardTelephoneNumber();
		case 3:
			return person.getStreet();
		case 4:
			return (person.getPostalCode() + " " + person.getCity()).trim(); //$NON-NLS-1$
		case 5: {
			return person.getLastCall();
		}

		default:
			return "X"; //$NON-NLS-1$
		// throw new IllegalArgumentException("Invalid column: " +
		// columnIndex);
		}
	}

	/**
	 * Returns the index of a Person in the filtered PhoneBook
	 *
	 * @param p
	 * @return
	 */
	public int indexOf(Person p) {
		return filteredPersons.indexOf(p);
	}

	private String getCSVHeader(char separator) {
		return "\"Private\"" + separator + "\"Last Name\"" + separator + "\"First Name\"" + separator + "\"Company\"" + separator + "\"Street\"" + separator + "\"ZIP Code\"" + separator + "\"City\"" + separator + "\"E-Mail\"" + separator + "\"Home\"" + separator + "\"Mobile\"" + separator + "\"Homezone\"" + separator + "\"Business\"" + separator + "\"Other\"" + separator + "\"Fax\"" + separator + "\"Sip\"" + separator + "\"Main\""; //$NON-NLS-1$
	}

	/**
	 * Saves PhoneBook to csv file
	 *
	 * @author Bastian Schaefer
	 *
	 * @param filename
	 *            Filename to save to
	 * @param wholePhoneBook
	 *            Save whole phone book or only selected entries
	 * @deprecated
	 */
	/*
	 * public void saveToCSVFile(String filename, boolean wholePhoneBook, char
	 * separator) { Debug.msg("Saving phone book to csv file " + filename);
	 * //$NON-NLS-1$ FileOutputStream fos; try { fos = new
	 * FileOutputStream(filename); PrintWriter pw = new PrintWriter(fos); //
	 * pw.println("\"Private\";\"Last Name\";\"First //
	 * Name\";\"Number\";\"Address\";\"City\"");
	 * pw.println(getCSVHeader(separator)); int rows[] = null;
	 *
	 * if (JFritz.getJframe() != null) { rows =
	 * JFritz.getJframe().getPhoneBookPanel()
	 * .getPhoneBookTable().getSelectedRows(); }
	 *
	 * if (!wholePhoneBook && (rows != null) && (rows.length > 0)) { for (int i =
	 * 0; i < rows.length; i++) { Person currentPerson = (Person)
	 * filteredPersons .elementAt(rows[i]);
	 * pw.println(currentPerson.toCSV(separator)); } } else if (wholePhoneBook) { //
	 * Export ALL UNFILTERED Calls Enumeration en =
	 * getUnfilteredPersons().elements(); while (en.hasMoreElements()) { Person
	 * person = (Person) en.nextElement(); pw.println(person.toCSV(separator)); } }
	 * else { // Export ALL FILTERED Calls Enumeration en =
	 * getFilteredPersons().elements(); while (en.hasMoreElements()) { Person
	 * person = (Person) en.nextElement(); pw.println(person.toCSV(separator)); } }
	 * pw.close(); } catch (FileNotFoundException e) { Debug.err("Could not
	 * write " + filename + "!"); //$NON-NLS-1$, //$NON-NLS-2$ } }
	 */
	/**
	 * Saves PhoneBook to csv file
	 *
	 * @author Bastian Schaefer
	 *
	 * @param filename
	 *            Filename to save to Save whole phone book
	 */
	public void saveToCSVFile(String filename, char separator) {
		Debug
				.msg("Saving phone book(" + unfilteredPersons.size() + " lines) to csv file " + filename); //$NON-NLS-1$
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			// pw.println("\"Private\";\"Last Name\";\"First
			// Name\";\"Number\";\"Address\";\"City\"");
			pw.println(getCSVHeader(separator));
			// wenn man das komplette buch speichern will
			// unfilteredPersons durchsuchen
			for (int i = 0; i < unfilteredPersons.size(); i++) {
				Person currentPerson = unfilteredPersons.elementAt(i);
				pw.println(currentPerson.toCSV(separator));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}

	}

	/**
	 * Saves PhoneBook to csv file
	 *
	 * @author Bastian Schaefer
	 *
	 * @param filename
	 *            Filename to save to Save phone book only selected entries
	 */
	public void saveToCSVFile(String filename, int[] rows, char separator) {
		if (rows.length == 0) {
			saveToCSVFile(filename, separator);
			return;
		}
		Debug
				.msg("Saving phone book(" + rows.length + " lines) to csv file " + filename); //$NON-NLS-1$
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			// pw.println("\"Private\";\"Last Name\";\"First
			// Name\";\"Number\";\"Address\";\"City\"");
			pw.println(getCSVHeader(separator));
			// wenn man nicht das komplette buch speichern will
			// muss man filteredPersons durchsuchen
			for (int i = 0; i < rows.length; i++) {
				Person currentPerson = filteredPersons.elementAt(rows[i]);
				pw.println(currentPerson.toCSV(separator));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	/**
	 * Returns info about stored Person
	 *
	 * @param rowIndex
	 */
	public Person getPersonAt(int rowIndex) {
		if (rowIndex >= 0) {
			return filteredPersons.get(rowIndex);
		} else {
			return null;
		}
	}

	public int getRowCount() {
		return filteredPersons.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		try {
			return Main.getMessage(columnNames[column]);
		} catch (Exception e) {
			return columnNames[column];
		}
	}

	public Person findPerson(PhoneNumber number) {
		return findPerson(number, true);
	}

	public Person findPerson(Call call) {
		return findPerson(call.getPhoneNumber(), true);
	}

	/**
	 * Finds a person with the given number.
	 *
	 * @param number
	 *            a String containing the number to search for
	 * @param considerMain
	 *            true, if search for main number (telephone switchboard) shoul
	 *            be enabled.
	 * @return the Person having that number or the main number of telephone
	 *         switchboard in companies, null if no person was found
	 * @author Benjamin Schmitt (overwriting)
	 */
	public Person findPerson(PhoneNumber number, boolean considerMain) {
		if (number == null) {
			return null;
		}
		Vector<Person> foundPersons = new Vector<Person>();
		Enumeration<Person> en = unfilteredPersons.elements();
		while (en.hasMoreElements()) {
			Person p = en.nextElement();
			if (p.hasNumber(number.getIntNumber(), considerMain)) {
				foundPersons.add(p);
			}
		}
		if (foundPersons.size() == 0) {
			return null;
		} else if (foundPersons.size() == 1) {
			return foundPersons.get(0);
		} else {
			// delete all dummy entries for this number and return first element
			// of foundPersons
			/**
			 * for (int i = 0; i < foundPersons.size(); i++) { Person p =
			 * foundPersons.get(i); if ( p.isDummy() ) { // dummy entry, delete
			 * it from database foundPersons.removeElement(p);
			 * unfilteredPersons.removeElement(p);
			 * this.saveToXMLFile(fileLocation); } }
			 */
			return foundPersons.get(0);
		}
	}

	/**
	 * searches for the last call for every Person in the Addressbook, and write
	 * it to person.lastCall to speed up sorting
	 */
	public void findAllLastCalls() {
		// TODO updaten wenn neue call oder personen oder rufnummern hinzukommen
		// oder alte gelöscht werden
		Debug.msg("searching lastCall for allPersons in the phonebook....");
		if (callerList == null) {
			Debug.err("setCallerList first!");
		}
		/*
		 * JFritz.getCallerList().calculateAllLastCalls(unfilteredPersons); too
		 * slow
		 */
		Person person;
		Call call;
		for (int i = 0; i < unfilteredPersons.size(); i++) {
			person = unfilteredPersons.get(i);
			call = callerList.findLastCall(person);
			if (call != null) {
				// wichtig, sonst stht da noch null drinn und den Fehler findet
				// man dann niemals
				callerList.setPerson(person, call);
			}
			person.setLastCall(call);
		}
		Debug.msg("...done");
		fireTableDataChanged();
	}

	/**
	 * @param columnIndex
	 * @return class of column
	 */
	public Class<? extends Object> getColumnClass(int columnIndex) {
		Object o = getValueAt(0, columnIndex);
		if (o == null) {
			return Object.class;
		} else {
			return o.getClass();
		}
	}

	public void updateFilter() {
		/*
		 * try { JFritz.getJframe().getCallerTable().getCellEditor()
		 * .cancelCellEditing(); } catch (NullPointerException e) { }
		 */
		boolean filter_private = JFritzUtils.parseBoolean(Main
				.getStateProperty("filter_private")); //$NON-NLS-1$

		String filterSearch = Main.getStateProperty("filter.Phonebook.search", ""); //$NON-NLS-1$,  //$NON-NLS-2$
		String keywords[] = filterSearch.split(" "); //$NON-NLS-1$

		if ((!filter_private) && (keywords.length == 0)) {
			// Use unfiltered data
			filteredPersons = unfilteredPersons;
			Debug.msg("Im updating the filter");
			Debug.msg("Size of filtered contacts: "+filteredPersons.size());
		} else {
			// Data got to be filtered
			Vector<Person> newFilteredPersons = new Vector<Person>();
			Enumeration<Person> en = unfilteredPersons.elements();
			while (en.hasMoreElements()) {
				Person current = en.nextElement();

				// check whether this Person should be shown anyway
				if (filterExceptions.contains(current)) {
					newFilteredPersons.add(current);
					continue; // skip to next person in the while-loop
				}

				boolean match = true;

				// check wether the private filter rules this Person out
				if (filter_private && (!current.isPrivateEntry())) {
					match = false;
				}

				// check the keywords, if there are any
				for (int i = 0; match && (i < keywords.length); i++) {
					if (!current.matchesKeyword(keywords[i])) {
						match = false;
					}
				}

				// if all filter criteria are met, we add the person
				if (match) {
					newFilteredPersons.add(current);
				}
			}
			filteredPersons = newFilteredPersons;
		}

		sortAllFilteredRows();

		/*
		 * //FIXME checken, ob das jetzt echt nicht mehr gebraucht wird if
		 * (JFritz.getJframe() != null) { if
		 * (JFritz.getJframe().getPhoneBookPanel() != null) {
		 * JFritz.getJframe().getPhoneBookPanel().setStatus(); } }
		 */
	}

	/**
	 * @author Brian Jensen function reads the thunderbird csv file line for
	 *         line adding new contacts after each line
	 *
	 * @param filename
	 *            is the path to a valid thunderbird csv file
	 */
	public String importFromThunderbirdCSVfile(String filename) {
		Debug.msg("Importing Thunderbird Contacts from csv file " + filename); //$NON-NLS-1$
		String line = ""; //$NON-NLS-1$
		String message;
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);

			int linesRead = 0;
			int newEntries = 0;
			// read until EOF
			while (null != (line = br.readLine())) {
				line += "\n";
				linesRead++;
				Person person = parseContactsThunderbirdCSV(line);

				// check if person had person had phone number
				if (person != null) {
					// check if it was a new person
					if (addEntry(person)) {
						newEntries++;
					}
				}

			}

			Debug.msg(linesRead
					+ " Lines read from Thunderbird csv file " + filename); //$NON-NLS-1$
			Debug.msg(newEntries + " New contacts processed"); //$NON-NLS-1$

			if (newEntries > 0) {
				sortAllUnfilteredRows();
				saveToXMLFile(Main.SAVE_DIR + fileLocation);
				String msg;

				if (newEntries == 1) {
					msg = Main.getMessage("imported_contact"); //$NON-NLS-1$
				} else {
					msg = newEntries
							+ " " + Main.getMessage("imported_contacts"); //$NON-NLS-1$,  //$NON-NLS-2$
				}
				message = msg;

			} else {
				message = Main.getMessage("no_imported_contacts"); //$NON-NLS-1$
			}

			br.close();

		} catch (FileNotFoundException e) {
			message = "Could not read from " + filename + "!";
			Debug.err("Could not read from " + filename + "!"); //$NON-NLS-1$, //$NON-NLS-2$
		} catch (IOException e) {
			message = "IO Exception reading csv file";
			Debug.err("IO Exception reading csv file"); //$NON-NLS-1$
		}
		return message;
	}

	/**
	 * @author Brian Jensen
	 *
	 * function parses out relevant contact information from a csv file, if no
	 * telephone number is found or the format is invalid null is returned
	 * tested with thunderbird version 1.50 tested with Mozilla suite 1.7.x
	 *
	 * Note: This class does NOT check for valid telephone numbers! That means
	 * contacts could be created without telephone numbers
	 *
	 * @param string
	 *            line is the current line of the csv file
	 * @return returns a person object if a telephone number can be processed
	 *         from the datei
	 */
	public Person parseContactsThunderbirdCSV(String line) {
		String[] field = splitCSVLine(line);
		Person person;

		// check if line has correct amount of entries
		if (field.length < 37) {
			Debug.err("Invalid Thunderbird CSV format!"); //$NON-NLS-1$
			return null;
		}

		// check first if the entry even has a phone number
		// Debug.msg(field[6]+" "+field[7]+" "+field[8]+" "+field[9]+"
		// "+field[10]);
		if (field[6].equals("") && field[7].equals("") && field[8].equals("") && //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				field[9].equals("") && field[10].equals("")) { //$NON-NLS-1$,  //$NON-NLS-2$
			Debug.msg("No phone number present for contact"); //$NON-NLS-1$
			return null;
		}

		// at least a phone number and an email exists because thunderbird
		// is an email client and stores at least an email addy
		// so create a new person object
		person = new Person(field[0], field[25], field[1], field[11]
				+ field[12], field[15], field[13], field[4]);

		// TODO: Check for valid numbers, as you can never gurantee
		// that users do things properly, could be possible to create
		// contacts in the phonebook with no phone number = useless

		// Work number
		if (!field[6].equals("")) {
			person.addNumber(field[6], "business"); //$NON-NLS-1$
		}

		// home number
		if (!field[7].equals("")) {
			person.addNumber(field[7], "home"); //$NON-NLS-1$
		}

		// fax number
		if (!field[8].equals("")) {
			person.addNumber(field[8], "fax"); //$NON-NLS-1$
		}

		// pager number
		if (!field[9].equals("")) {
			person.addNumber(field[9], "other"); //$NON-NLS-1$
		}

		// Cell phone number
		if (!field[10].equals("")) {
			person.addNumber(field[10], "mobile"); //$NON-NLS-1$
		}

		// lets quit while we're still sane and return the person object
		return person;

	}

	/**
	 * Removes redundant entries from the phonebook. It checks for every pair of
	 * entries, if one entry supersedes another entry.
	 *
	 * @see de.moonflower.jfritz.struct.Person#supersedes(Person)
	 * @return the number of removed entries
	 */
	public synchronized int deleteDuplicateEntries() {
		Set<Person> redundantEntries = new HashSet<Person>();

		synchronized (unfilteredPersons) {
			int size = unfilteredPersons.size();
			for (int i = 0; i < size; i++) {
				Person currentOuter = unfilteredPersons.elementAt(i);
				for (int j = i + 1; j < size; j++) {
					Person currentInner = unfilteredPersons.elementAt(j);
					if (currentOuter.supersedes(currentInner)) {
						redundantEntries.add(currentInner);
					} else if (currentInner.supersedes(currentOuter)) {
						redundantEntries.add(currentOuter);
					}
				}
			}

			Iterator<Person> iterator = redundantEntries.iterator();
			while (iterator.hasNext()) {
				Person p = iterator.next();
				deleteEntry(p);
			}
		}

		if (redundantEntries.size() > 0) {
			saveToXMLFile(Main.SAVE_DIR + fileLocation);
			updateFilter();
		}

		return redundantEntries.size();
	}

	public void setCallerList(CallerList list) {
		this.callerList = list;

	}

	public boolean getAllLastCallsSearched() {
		return allLastCallsSearched;
	}

	public String[] splitCSVLine(String line) {
		String[] items;
		// Alle überflüssigen " zusammenfassen
		// line = line.trim();
		if (line.contains("\"")) {
			while (!line.equals(line = line.replace("\"\"", "\"")))
				;
			// jetzt zuerst am " splitten und danach nur falls der String ein
			// "," am Anfang oder Ende enthält
			String[] pre = line.split("\"");
			// aaa,bbb,"ccc,ddd,eee",fff,ggg,hhh,"iii,jjj,kkk",lll,mmm
			String[] tmpString;
			Vector<String> v = new Vector<String>();
			for (int i = 0; i < pre.length; i++) {
				// falls der String ein "," am Anfang oder Ende enthält
				if ((pre[i].indexOf(",") == 0)
						|| pre[i].lastIndexOf(",") == pre[i].length() - 1)
					tmpString = pre[i].split(",");
				else {
					tmpString = new String[1];
					tmpString[0] = pre[i];
				}
				for (int j = 0; j < tmpString.length; j++)
					v.add(tmpString[j]);
			}

			items = new String[v.capacity()];
			v.copyInto(items);
		} else
			items = line.split(",");
		return items;
	}

    public static Person searchFirstAndLastNameToPhoneNumber(String caller) {
    	Vector<Person> persons = new Vector<Person>();
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searching in local database for number "+caller+" ..."); //$NON-NLS-1$
        Person person = JFritz.getPhonebook().findPerson(callerPhoneNumber);
        if (person != null) {
            Debug.msg("Found in local database: " + person.getLastName() + ", " + person.getFirstName()); //$NON-NLS-1$,  //$NON-NLS-2$
        } else {
            Debug.msg("Searching on dastelefonbuch.de ..."); //$NON-NLS-1$
            person = ReverseLookup.busyLookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) { //$NON-NLS-1$
                Debug
                        .msg("Found on dastelefonbuch.de: " + person.getLastName() + ", " + person.getFirstName()); //$NON-NLS-1$,  //$NON-NLS-2$
                Debug.msg("Add person to database"); //$NON-NLS-1$
                persons.add(person);
            } else {
                person = new Person();
                person.addNumber(new PhoneNumber(caller));
                Debug.msg("Found no person"); //$NON-NLS-1$
                Debug.msg("Add dummy person to database"); //$NON-NLS-1$
                persons.add(person);
            }
            JFritz.getPhonebook().addEntries(persons);
            JFritz.getPhonebook().fireTableDataChanged();
        }
        return person;
    }

    public Vector<Person> getUnfilteredPersons(){
    	return unfilteredPersons;
    }

    /**
     * function removes the user selected contacts from the phone book
     *
     *  @author brian
     *
     * @param rows
     */
    public synchronized void removePersons(int[] rows){
    	if (rows.length > 0) {
			// Markierte Einträge löschen
			Vector<Person> personsToDelete = new Vector<Person>();
			Person person;
			for (int i = 0; i < rows.length; i++) {
				person = filteredPersons.get(rows[i]);
				personsToDelete.add(person);
				deleteEntry(person);
			}

			for(PhoneBookListener listener: listeners)
				listener.contactsRemoved((Vector) personsToDelete.clone());

			updateFilter();
			fireTableDataChanged();
			saveToXMLFile(Main.SAVE_DIR + JFritz.PHONEBOOK_FILE);
		}
    }

    /**
     * This function is used to notify the network subsystem that a
     * contact has been modified
     *
     * @param old
     * @param updated
     */
    public void personUpdated(Person old, Person updated){
    	Vector<Person> o = new Vector<Person>();
    	Vector<Person> u = new Vector<Person>();
    	o.add(old);
    	u.add(updated);

    	for(PhoneBookListener listener: listeners){
    		listener.contactsRemoved(o);
    		listener.contactsAdded(u);
    	}
    }

}
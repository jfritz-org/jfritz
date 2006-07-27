package de.moonflower.jfritz.dialogs.phonebook;

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
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class PhoneBook extends AbstractTableModel {
	private static final long serialVersionUID = 1;
	private static final String PHONEBOOK_DTD_URI = "http://jfritz.moonflower.de/dtd/phonebook.dtd"; //$NON-NLS-1$

	// TODO Write correct dtd
	private static final String PHONEBOOK_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //$NON-NLS-1$
			+ "<!-- DTD for JFritz phonebook -->" //$NON-NLS-1$
			+ "<!ELEMENT firstname (commment?,entry*)>" //$NON-NLS-1$
			+ "<!ELEMENT middlename (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT lastname (#PCDATA)>" //$NON-NLS-1$
			+ "<!ELEMENT entry (firstname?,middlename?,lastname?)>"; //$NON-NLS-1$

	private final String columnNames[] = { "private_entry", "fullName", "telephoneNumber", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			"address", "city", "last_call" }; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

	private static final String PATTERN_THUNDERBRID_CSV = ","; //$NON-NLS-1$

	private Vector filteredPersons;

	private Vector unfilteredPersons;

	/**
	 * A vector of Persons that will match any search filter.
	 * In other words: a list of sticky Persons, that will always show up.
	 * Used to ensure that a newly created Person can be seen by the user,
	 * even if there is a filter active
	 */
	private Vector filterExceptions;

	private JFritz jfritz;

	private int sortColumn = 1;

	private boolean sortDirection = true;

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
		fireTableDataChanged();
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
		//If the wahlhilfe doesnt work, check here again!
		if (columnName.equals(JFritz.getMessage("telephoneNumber"))) //$NON-NLS-1$
			return true;
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
	public class ColumnSorter implements Comparator {
		int colIndex;

		boolean ascending;

		ColumnSorter(int colIndex, boolean ascending) {
			this.colIndex = colIndex;
			this.ascending = ascending;
		}

		public int compare(Object a, Object b) {
			Object o1, o2;
			Person v1 = (Person) a;
			Person v2 = (Person) b;
			switch (colIndex) {
			case 0:
				o1 = Boolean.toString(v1.isPrivateEntry());
				o2 = Boolean.toString(v2.isPrivateEntry());
				break;
			case 1:
				o1 = v1.getFullname().toString().toUpperCase();
				o2 = v2.getFullname().toString().toUpperCase();
				break;
			case 2:
                o1 = ""; //$NON-NLS-1$
                o2 = ""; //$NON-NLS-1$
                if (v1.getStandardTelephoneNumber()!= null)
                    o1 = v1.getStandardTelephoneNumber().toString();
                if (v2.getStandardTelephoneNumber()!= null)
                    o2 = v2.getStandardTelephoneNumber().toString();
			    break;
			case 3:
			    o1 = v1.getStreet().toUpperCase();
			    o2 = v2.getStreet().toUpperCase();
			    break;
			case 4:
			    o1 = v1.getPostalCode() + v1.getCity().toUpperCase();
			    o2 = v2.getPostalCode() + v1.getCity().toUpperCase();
			    break;
			case 5:
                o1 = ""; //$NON-NLS-1$
                o2 = ""; //$NON-NLS-1$
                if (jfritz.getCallerlist().findLastCall(v1) != null)
                    o1 = jfritz.getCallerlist().findLastCall(v1).getCalldate();
                if (jfritz.getCallerlist().findLastCall(v2) != null)
                    o2 = jfritz.getCallerlist().findLastCall(v2).getCalldate();
			    break;
			default:
				o1 = v1.getFullname().toString();
				o2 = v2.getFullname().toString();
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
	}

	public PhoneBook(JFritz jfritz) {
		this.jfritz = jfritz;
		filteredPersons = new Vector();
		unfilteredPersons = new Vector();
		filterExceptions = new Vector();
	}

	public Vector getFilteredPersons() {
		return filteredPersons;
	}

	public Vector getUnfilteredPersons() {
		return unfilteredPersons;
	}

	/**
	 * @author haeusler
     * DATE: 02.04.06
	 * Adds a Person to the list of filterExceptions.
	 * @param nonFilteredPerson
	 * @see #filterExceptions
	 */
	public void addFilterException(Person nonFilteredPerson) {
		filterExceptions.add(nonFilteredPerson);
	}

	/** Clears the list of filterExceptions.
	 * @see #filterExceptions
	 */
	public void clearFilterExceptions() {
		filterExceptions.clear();
	}

	public boolean addEntry(Person newPerson) {
		Enumeration en = unfilteredPersons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			PhoneNumber pn1 = p.getStandardTelephoneNumber();
			PhoneNumber pn2 = newPerson.getStandardTelephoneNumber();
			if (pn1 != null && pn2 != null
					&& pn1.getIntNumber().equals(pn2.getIntNumber())) {
				return false;
			}
		}
		unfilteredPersons.add(newPerson);
		updateFilter();
		return true;
	}

	public void deleteEntry(Person person) {
		unfilteredPersons.remove(person);
		updateFilter();
	}

	/**
	 * Saves phonebook to BIT FBF Dialer file.
	 *
	 * @param filename
	 */
	public synchronized void saveToBITFBFDialerFormat(String filename) {
		Debug.msg("Saving to BIT FBF Dialer file " + filename); //$NON-NLS-1$
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
			Enumeration en1 = unfilteredPersons.elements();

			Enumeration en2;
			Person current;
			String name;

			String nr, type;
			PhoneNumber pn;

			while (en1.hasMoreElements()) {
				current = (Person) en1.nextElement();
				name = ""; //$NON-NLS-1$
				if (current.getFullname().length() > 0) {
					if (current.getLastName().length() > 0) name += current.getLastName();
					if (current.getLastName().length() > 0 && current.getFirstName().length() > 0) name += ", "; //$NON-NLS-1$
					if (current.getFirstName().length() > 0) name += current.getFirstName();
					if (current.getCompany().length() > 0) name += " (" + current.getCompany() + ")";   //$NON-NLS-1$,   //$NON-NLS-2$
				}
				else if (current.getCompany().length() > 0) name += current.getCompany();

				if (name.length() > 0) {
					en2 = current.getNumbers().elements();
					while (en2.hasMoreElements()) {
						pn = (PhoneNumber) en2.nextElement();
						nr = pn.getIntNumber();
						if (nr.startsWith("+49")) nr = "0" + nr.substring(3, nr.length()); //$NON-NLS-1$,  //$NON-NLS-2$
						type = pn.getType();

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
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
			Enumeration en1 = unfilteredPersons.elements();

			Enumeration en2;
			Person current;
			String name;

			String nr, type;
			PhoneNumber pn;

			while (en1.hasMoreElements()) {
				current = (Person) en1.nextElement();
				name = ""; //$NON-NLS-1$
				if (current.getFullname().length() > 0) {
					if (current.getLastName().length() > 0) name += current.getLastName();
					if (current.getLastName().length() > 0 && current.getFirstName().length() > 0) name += ", "; //$NON-NLS-1$
					if (current.getFirstName().length() > 0) name += current.getFirstName();
					if (current.getCompany().length() > 0) name += " (" + current.getCompany() + ")";  //$NON-NLS-1$,  //$NON-NLS-2$
				}
				else if (current.getCompany().length() > 0) name += current.getCompany();

				if (name.length() > 0 ) {
					en2 = current.getNumbers().elements();
					while (en2.hasMoreElements()) {
						pn = (PhoneNumber) en2.nextElement();
						nr = pn.getIntNumber();
						if (nr.startsWith("+49")) nr = "0" + nr.substring(3, nr.length()); //$NON-NLS-1$,  //$NON-NLS-2$
						type = pn.getType();

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
//			pw.write("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
//					+ "\">");
//			pw.newLine();
			pw.write("<phonebook>"); //$NON-NLS-1$
			pw.newLine();
			pw.write("<comment>Phonebook for " + JFritz.PROGRAM_NAME + " v" //$NON-NLS-1$,  //$NON-NLS-2$
					+ JFritz.PROGRAM_VERSION + "</comment>"); //$NON-NLS-1$
			pw.newLine();
			Enumeration en = unfilteredPersons.elements();
			while (en.hasMoreElements()) {
				Person current = (Person) en.nextElement();
				pw.write("<entry private=\"" + current.isPrivateEntry() + "\">"); //$NON-NLS-1$,  //$NON-NLS-2$
				pw.newLine();
				if (current.getFullname().length() > 0) {
					pw.write("\t<name>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getFirstName().length() > 0)
						pw.write("\t\t<firstname>" + JFritzUtils.convertSpecialChars(current.getFirstName()) //$NON-NLS-1$
								+ "</firstname>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getLastName().length() > 0)
						pw.write("\t\t<lastname>" + JFritzUtils.convertSpecialChars(current.getLastName()) //$NON-NLS-1$
								+ "</lastname>"); //$NON-NLS-1$
					pw.newLine();
					pw.write("\t</name>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getCompany().length() > 0)
						pw.write("\t<company>" + JFritzUtils.convertSpecialChars(current.getCompany()) //$NON-NLS-1$
								+ "</company>"); //$NON-NLS-1$
					pw.newLine();
				}

				if ((current.getStreet().length() > 0)
						|| (current.getPostalCode().length() > 0)
						|| (current.getCity().length() > 0)) {
					pw.write("\t<address>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getStreet().length() > 0)
						pw.write("\t\t<street>" + JFritzUtils.convertSpecialChars(current.getStreet()) //$NON-NLS-1$
								+ "</street>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getPostalCode().length() > 0)
						pw.write("\t\t<postcode>" + JFritzUtils.convertSpecialChars(current.getPostalCode()) //$NON-NLS-1$
								+ "</postcode>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getCity().length() > 0)
						pw
						.write("\t\t<city>" + JFritzUtils.convertSpecialChars(current.getCity()) //$NON-NLS-1$
										+ "</city>"); //$NON-NLS-1$
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
							+ JFritzUtils.convertSpecialChars(nr.getIntNumber()) + "</number>"); //$NON-NLS-1$
					pw.newLine();

				}
				pw.write("\t</phonenumbers>"); //$NON-NLS-1$
				pw.newLine();

				if (current.getEmailAddress().length() > 0) {
					pw.write("\t<internet>"); //$NON-NLS-1$
					pw.newLine();
					if (current.getEmailAddress().length() > 0)
						pw.write("\t\t<email>" + JFritzUtils.convertSpecialChars(current.getEmailAddress()) //$NON-NLS-1$
								+ "</email>"); //$NON-NLS-1$
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

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
			Debug.err(e.toString());
			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.err(e.getLocalizedMessage());
				System.exit(0);
			}
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		updateFilter();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Person person = (Person) filteredPersons.get(rowIndex);
		switch (columnIndex) {
		case 0: if (person.isPrivateEntry()) return "YES"; //$NON-NLS-1$
			 else return "NO"; //$NON-NLS-1$
		case 1:
			return person.getFullname();
		case 2:
			return person.getStandardTelephoneNumber();
		case 3:
			return person.getStreet();
		case 4:
			return (person.getPostalCode() + " " + person.getCity()).trim();  //$NON-NLS-1$
		case 5:
			return jfritz.getCallerlist().findLastCall(person);
		default:
			return "X"; //$NON-NLS-1$
		//throw new IllegalArgumentException("Invalid column: " + columnIndex);
		}
	}

	/**
	 * Returns the index of a Person in the filtered PhoneBook
	 * @param p
	 * @return
	 */
	public int indexOf(Person p) {
		return filteredPersons.indexOf(p);
	}

    private String getCSVHeader(char separator) {
    	return "\"Private\""+separator+"\"Last Name\""+separator+"\"First Name\""+separator+"\"Company\""+separator+"\"Street\""+separator+"\"ZIP Code\""+separator+"\"City\""+separator+"\"E-Mail\""+separator+"\"Home\""+separator+"\"Mobile\""+separator+"\"Homezone\""+separator+"\"Business\""+separator+"\"Other\""+separator+"\"Fax\""+separator+"\"Sip\""+separator+"\"Main\""; //$NON-NLS-1$
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
     */
    public void saveToCSVFile(String filename, boolean wholePhoneBook, char separator) {
        Debug.msg("Saving phone book to csv file " + filename); //$NON-NLS-1$
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filename);
            PrintWriter pw = new PrintWriter(fos);
           // pw.println("\"Private\";\"Last Name\";\"First Name\";\"Number\";\"Address\";\"City\"");
           pw.println(getCSVHeader(separator));
            int rows[] = null;
            if (jfritz != null && jfritz.getJframe() != null) {
            	 rows = jfritz.getJframe().getPhoneBookPanel().getPhoneBookTable().getSelectedRows();
            }
            if (!wholePhoneBook && rows != null && rows.length > 0) {
                for (int i = 0; i < rows.length; i++) {
                    Person currentPerson = (Person) filteredPersons
                            .elementAt(rows[i]);
                    pw.println(currentPerson.toCSV(separator));
                }
            } else if (wholePhoneBook) { // Export ALL UNFILTERED Calls
                Enumeration en = getUnfilteredPersons().elements();
                while (en.hasMoreElements()) {
                    Person person = (Person) en.nextElement();
                    pw.println(person.toCSV(separator));
                }
            } else { // Export ALL FILTERED Calls
                Enumeration en = getFilteredPersons().elements();
                while (en.hasMoreElements()) {
                    Person person = (Person) en.nextElement();
                    pw.println(person.toCSV(separator));
                }
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
		if (rowIndex >= 0)
			return (Person) filteredPersons.get(rowIndex);
		else
			return null;
	}

	public int getRowCount() {
		return filteredPersons.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		try {
			return JFritz.getMessage(columnNames[column]);
		} catch (Exception e) {
			return columnNames[column];
		}
	}

	public Person findPerson(PhoneNumber number) {
		return findPerson(number,true);
	}

	/**
	 * Finds a person with the given number.
	 *
	 * @param number
	 * 			a String containing the number to search for
	 * @param considerMain
	 * 			true, if search for main number (telephone switchboard) shoul be enabled.
	 * @return the Person having that number or the main number of telephone switchboard in companies, null if no person was found
	 * @author Benjamin Schmitt (overwriting)
	 */
	public Person findPerson(PhoneNumber number, boolean considerMain) {
		if (number == null)
			return null;
        Vector foundPersons = new Vector();
		Enumeration en = unfilteredPersons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			if (p.hasNumber(number.getIntNumber(),considerMain))
                foundPersons.add(p);
		}
        if ( foundPersons.size() == 0)
            return null;
        else if ( foundPersons.size() == 1 ) {
            return (Person) foundPersons.get(0);
        }
        else {
            // delete all dummy entries for this number and return first element of foundPersons
            for ( int i=0; i<foundPersons.size(); i++) {
                Person p = (Person) foundPersons.get(i);
                if (p.getFullname().equals("") && p.getNumbers().size() == 1
                        && p.getAddress().equals("") && p.getCity().equals("")
                        && p.getCompany().equals("") && p.getEmailAddress().equals("")
                        && p.getPostalCode().equals("") && p.getStreet().equals("")) {
                    // dummy entry, delete it from database
                    foundPersons.removeElement(p);
                    unfilteredPersons.removeElement(p);
                    this.saveToXMLFile(JFritz.PHONEBOOK_FILE);
                }
            }
            return (Person) foundPersons.get(0);
        }
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

	public void updateFilter() {
/*        try {
            jfritz.getJframe().getCallerTable().getCellEditor()
                    .cancelCellEditing();
        } catch (NullPointerException e) {
        }
*/
		boolean filter_private = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter_private")); //$NON-NLS-1$

		String filterSearch = JFritz.getProperty("filter.Phonebook.search", ""); //$NON-NLS-1$,  //$NON-NLS-2$
		String keywords[] = filterSearch.split(" "); //$NON-NLS-1$

		if ((!filter_private) && (keywords.length == 0)) {
            // Use unfiltered data
			filteredPersons = unfilteredPersons;
		} else {
			// Data got to be filtered
			Vector newFilteredPersons = new Vector();
			Enumeration en = unfilteredPersons.elements();
			while (en.hasMoreElements()) {
				Person current = (Person) en.nextElement();

				// check whether this Person should be shown anyway
				if (filterExceptions.contains(current)) {
					newFilteredPersons.add(current);
					continue; // skip to next person in the while-loop
				}

				boolean match = true;

				// check wether the private filter rules this Person out
				if (filter_private && (! current.isPrivateEntry())) {
					match = false;
				}

				// check the keywords, if there are any
                for (int i = 0; match && i < keywords.length; i++) {
                    if (! current.matchesKeyword(keywords[i])) {
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

		if (jfritz!= null)
			if (jfritz.getJframe() != null)
				if(jfritz.getJframe().getPhoneBookPanel()!=null)
					jfritz.getJframe().getPhoneBookPanel().setStatus();
	}

	/**
	 * @author Brian Jensen
	 * function reads the thunderbird csv file line for line
	 * adding new contacts after each line
	 *
	 * @param filename is the path to a valid thunderbird csv file
	 */
	public void importFromThunderbirdCSVfile(String filename){
	    Debug.msg("Importing Thunderbird Contacts from csv file " + filename); //$NON-NLS-1$
	    String line = "";  //$NON-NLS-1$
	    try {
	      FileReader fr = new FileReader(filename);
	          BufferedReader br = new BufferedReader(fr);

	          int linesRead = 0;
	          int newEntries = 0;
	          //read until EOF
	          while(null != (line = br.readLine())){
	        	  linesRead++;
	              Person person = parseContactsThunderbirdCSV(line);

	              //check if person had person had phone number
	              if(person != null)
	            	  //check if it was a new person
	            	  if(addEntry(person))
	            		  newEntries++;


	          }

	          Debug.msg(linesRead+" Lines read from Thunderbird csv file "+filename); //$NON-NLS-1$
	          Debug.msg(newEntries+" New contacts processed"); //$NON-NLS-1$

	          if (newEntries > 0) {
	        	  sortAllUnfilteredRows();
	              saveToXMLFile(JFritz.SAVE_DIR + JFritz.PHONEBOOK_FILE);
	              String msg;

	              if (newEntries == 1) {
	                msg = JFritz.getMessage("imported_contact"); //$NON-NLS-1$
	              } else {
	                msg = newEntries + " "+JFritz.getMessage("imported_contacts"); //$NON-NLS-1$,  //$NON-NLS-2$
	              }
	              JFritz.infoMsg(msg);

	          }else{
	        	  JFritz.infoMsg(JFritz.getMessage("no_imported_contacts")); //$NON-NLS-1$
	          }

	          br.close();

	    } catch (FileNotFoundException e) {
	    	Debug.err("Could not read from " + filename + "!"); //$NON-NLS-1$, //$NON-NLS-2$
	    } catch(IOException e){
	    	Debug.err("IO Exception reading csv file"); //$NON-NLS-1$
	    }
	}

	/**
	 * @author Brian Jensen
	 *
	 * function parses out relevant contact information from a csv file,
	 * if no telephone number is found or the format is invalid
	 * null is returned
	 * tested with thunderbird version 1.50
	 * tested with Mozilla suite 1.7.x
	 *
	 * Note: This class does NOT check for valid telephone numbers!
	 * That means contacts could be created without telephone numbers
	 *
	 * @param string line is the current line of the csv file
	 * @return returns a person object if a telephone number can be processed from the datei
	 */
	public Person parseContactsThunderbirdCSV(String line){
	    String[] field = line.split(PATTERN_THUNDERBRID_CSV);
	    Person person;

	    //check if line has correct amount of entries
	    if(field.length < 36){
	      Debug.err("Invalid Thunderbird CSV format!"); //$NON-NLS-1$
	      return null;
	    }

	    //check first if the entry even has a phone number
	    //Debug.msg(field[6]+"   "+field[7]+"   "+field[8]+"   "+field[9]+"   "+field[10]);
	    if (field[6].equals("") && field[7].equals("") && field[8].equals("") && //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
	    		field[9].equals("") && field[10].equals("")){ //$NON-NLS-1$,  //$NON-NLS-2$
	    	Debug.msg("No phone number present for contact"); //$NON-NLS-1$
	    	return null;
	    }

	    //at least a phone number and an email exists because thunderbird
	    //is an email client and stores at least an email addy
	    //so create a new person object
	    person = new Person(field[0], field[25], field[1],
	    		field[11]+field[12], field[15], field[13], field[4]);

	    //TODO: Check for valid numbers, as you can never gurantee
	    //that users do things properly, could be possible to create
	    //contacts in the phonebook with no phone number = useless

	    //Work number
	    if(!field[6].equals("")) //$NON-NLS-1$
	    	person.addNumber(field[6], "business"); //$NON-NLS-1$

	    //home number
	    if(!field[7].equals("")) //$NON-NLS-1$
	    	person.addNumber(field[7], "home"); //$NON-NLS-1$

	    //fax number
	    if(!field[8].equals("")) //$NON-NLS-1$
	    	person.addNumber(field[8], "fax"); //$NON-NLS-1$

	    //pager number
	    if(!field[9].equals("")) //$NON-NLS-1$
	    	person.addNumber(field[9], "other"); //$NON-NLS-1$

	    //Cell phone number
	    if(!field[10].equals("")) //$NON-NLS-1$
	    	person.addNumber(field[10], "mobile"); //$NON-NLS-1$

	    //lets quit while we're still sane and return the person object
	    return person;

	}

	/**
	 * Removes redundant entries from the phonebook.
	 * It checks for every pair of entries,
	 * if one entry supersedes another entry.
	 *
	 * @see de.moonflower.jfritz.struct.Person#supersedes(Person)
	 * @return the number of removed entries
	 */
	public synchronized int deleteDuplicateEntries() {
		Set redundantEntries = new HashSet();

		synchronized (unfilteredPersons) {
			int size = unfilteredPersons.size();
			for (int i = 0; i < size; i++) {
				Person currentOuter = (Person) unfilteredPersons.elementAt(i);
				for (int j = i+1; j < size; j++) {
					Person currentInner = (Person) unfilteredPersons.elementAt(j);
					if (currentOuter.supersedes(currentInner)) {
						redundantEntries.add(currentInner);
					} else if (currentInner.supersedes(currentOuter)) {
						redundantEntries.add(currentOuter);
					}
				}
			}

			Iterator iterator = redundantEntries.iterator();
			while (iterator.hasNext()) {
				Person p = (Person) iterator.next();
				deleteEntry(p);
			}
		}

		if (redundantEntries.size() > 0) {
			saveToXMLFile(JFritz.SAVE_DIR + JFritz.PHONEBOOK_FILE);
			updateFilter();
		}

		return redundantEntries.size();
	}

}

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
	private static final String PHONEBOOK_DTD_URI = "http://jfritz.moonflower.de/dtd/phonebook.dtd";

	// TODO Write correct dtd
	private static final String PHONEBOOK_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!-- DTD for JFritz phonebook -->"
			+ "<!ELEMENT firstname (commment?,entry*)>"
			+ "<!ELEMENT middlename (#PCDATA)>"
			+ "<!ELEMENT lastname (#PCDATA)>"
			+ "<!ELEMENT entry (firstname?,middlename?,lastname?)>";

	private final String columnNames[] = { "private_entry", "fullName", "telephoneNumber",
			"address", "city", "last_call" };

	private static final String PATTERN_THUNDERBRID_CSV = ",";

	private Vector filteredPersons;

	private Vector unfilteredPersons;

	private JFritz jfritz;

	private int sortColumn = 1;

	private boolean sortDirection = true;

    private final static String EXPORT_CSV_FORMAT = "\"Private\";\"Last Name\";\"First Name\";\"Company\";\"Street\";\"Postal Code\";\"City\";\"E-Mail\";\"Phone Numbers\"";
	/**
	 * Sort table model rows by a specific column and direction
	 *
	 * @param col
	 *            Index of column to be sorted by
	 * @param asc
	 *            Order of sorting
	 */
	public void sortAllFilteredRowsBy(int col, boolean asc) {
		//		Debug.msg("Sorting column " + col + " " + asc);
		Collections.sort(filteredPersons, new ColumnSorter(col, asc));
		fireTableDataChanged();
//		fireTableStructureChanged();
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
		if (columnName.equals(JFritz.getMessage("number")))
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
		Debug.msg("Sorting unfiltered data");
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
                o1 = "";
                o2 = "";
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
                o1 = "";
                o2 = "";
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
	}

	public Vector getFilteredPersons() {
		return filteredPersons;
	}

	public Vector getUnfilteredPersons() {
		return unfilteredPersons;
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
	 * Saves phonebook to xml file.
	 *
	 * @param filename
	 */
	public synchronized void saveToXMLFile(String filename) {
		Debug.msg("Saving to file " + filename);
		try {
		        BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
		            new FileOutputStream(filename), "UTF8"));
			pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.newLine();
//			pw.write("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
//					+ "\">");
//			pw.newLine();
			pw.write("<phonebook>");
			pw.newLine();
			pw.write("<comment>Phonebook for " + JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION + "</comment>");
			pw.newLine();
			Enumeration en = unfilteredPersons.elements();
			while (en.hasMoreElements()) {
				Person current = (Person) en.nextElement();
				pw.write("<entry private=\"" + current.isPrivateEntry() + "\">");
				pw.newLine();
				if (current.getFullname().length() > 0) {
					pw.write("\t<name>");
					pw.newLine();
					if (current.getFirstName().length() > 0)
						pw.write("\t\t<firstname>" + JFritzUtils.convertSpecialChars(current.getFirstName())
								+ "</firstname>");
					pw.newLine();
					if (current.getLastName().length() > 0)
						pw.write("\t\t<lastname>" + JFritzUtils.convertSpecialChars(current.getLastName())
								+ "</lastname>");
					pw.newLine();
					pw.write("\t</name>");
					pw.newLine();
					if (current.getCompany().length() > 0)
						pw.write("\t<company>" + JFritzUtils.convertSpecialChars(current.getCompany())
								+ "</company>");
					pw.newLine();
				}

				if ((current.getStreet().length() > 0)
						|| (current.getPostalCode().length() > 0)
						|| (current.getCity().length() > 0)) {
					pw.write("\t<address>");
					pw.newLine();
					if (current.getStreet().length() > 0)
						pw.write("\t\t<street>" + JFritzUtils.convertSpecialChars(current.getStreet())
								+ "</street>");
					pw.newLine();
					if (current.getPostalCode().length() > 0)
						pw.write("\t\t<postcode>" + JFritzUtils.convertSpecialChars(current.getPostalCode())
								+ "</postcode>");
					pw.newLine();
					if (current.getCity().length() > 0)
						pw
						.write("\t\t<city>" + JFritzUtils.convertSpecialChars(current.getCity())
										+ "</city>");
					pw.newLine();
					pw.write("\t</address>");
					pw.newLine();
				}

				pw.write("\t<phonenumbers standard=\""
						+ current.getStandard() + "\">");
				pw.newLine();
				Enumeration en2 = current.getNumbers().elements();
				while (en2.hasMoreElements()) {
					PhoneNumber nr = (PhoneNumber) en2.nextElement();
					pw.write("\t\t<number type=\"" + nr.getType() + "\">"
							+ JFritzUtils.convertSpecialChars(nr.getIntNumber()) + "</number>");
					pw.newLine();

				}
				pw.write("\t</phonenumbers>");
				pw.newLine();

				if (current.getEmailAddress().length() > 0) {
					pw.write("\t<internet>");
					pw.newLine();
					if (current.getEmailAddress().length() > 0)
						pw.write("\t\t<email>" + JFritzUtils.convertSpecialChars(current.getEmailAddress())
								+ "</email>");
					pw.newLine();
					pw.write("\t</internet>");
					pw.newLine();
				}
				/*
				 * if (current.getCategory().length() > 0) { pw.println("\t
				 * <categories>"); if (current.getCategory().length() > 0)
				 * pw.println("\t\t <category>" + current.getCategory() + "
				 * </category>"); pw.println("\t </categories>"); }
				 */
				pw.write("</entry>");
				pw.newLine();
			}
			pw.write("</phonebook>");
			pw.newLine();
			pw.close();
		  } catch (UnsupportedEncodingException e) {
		      Debug.err("UTF-8 not supported.");
			} catch (FileNotFoundException e) {
				Debug.err("Could not write " + filename + "!");
		  } catch (IOException e) {
		  	Debug.err("IOException " + filename);
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
					throw new SAXException("Invalid system identifier: "
							+ systemId);
				}

			});
			reader.setContentHandler(new PhonebookFileXMLHandler(this));
			reader.parse(new InputSource(new FileInputStream(filename)));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!");
		} catch (SAXException e) {
			Debug.err("Error on parsing " + filename + "!");
			Debug.err(e.toString());
			if (e.getLocalizedMessage().startsWith("Relative URI")
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) {
				Debug.err(e.getLocalizedMessage());
				System.exit(0);
			}
		} catch (IOException e) {
			Debug.err("Could not read " + filename + "!");
		}
		updateFilter();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Person person = (Person) filteredPersons.get(rowIndex);
		switch (columnIndex) {
		case 0: if (person.isPrivateEntry()) return "YES";
			 else return "NO";
		case 1:
			return person.getFullname();
		case 2:
			return person.getStandardTelephoneNumber();
		case 3:
			return person.getStreet();
		case 4:
			return (person.getPostalCode() + " " + person.getCity()).trim();
		case 5:
			return jfritz.getCallerlist().findLastCall(person);
		default:
			return "X";
		//throw new IllegalArgumentException("Invalid column: " + columnIndex);
		}
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
    public void saveToCSVFile(String filename, boolean wholePhoneBook) {
        Debug.msg("Saving phone book to csv file " + filename);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filename);
            PrintWriter pw = new PrintWriter(fos);
           // pw.println("\"Private\";\"Last Name\";\"First Name\";\"Number\";\"Address\";\"City\"");
           pw.println(EXPORT_CSV_FORMAT);
            int rows[] = null;
            if (jfritz != null && jfritz.getJframe() != null) {
            	 rows = jfritz.getJframe().getPhoneBookPanel().getPhoneBookTable().getSelectedRows();
            }
            if (!wholePhoneBook && rows != null && rows.length > 0) {
                for (int i = 0; i < rows.length; i++) {
                    Person currentPerson = (Person) filteredPersons
                            .elementAt(rows[i]);
                    pw.println(currentPerson.toCSV());
                }
            } else if (wholePhoneBook) { // Export ALL UNFILTERED Calls
                Enumeration en = getUnfilteredPersons().elements();
                while (en.hasMoreElements()) {
                    Person person = (Person) en.nextElement();
                    pw.println(person.toCSV());
                }
            } else { // Export ALL FILTERED Calls
                Enumeration en = getFilteredPersons().elements();
                while (en.hasMoreElements()) {
                    Person person = (Person) en.nextElement();
                    pw.println(person.toCSV());
                }
            }
            pw.close();
        } catch (FileNotFoundException e) {
            Debug.err("Could not write " + filename + "!");
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
		Enumeration en = unfilteredPersons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			if (p.hasNumber(number.getIntNumber(),considerMain))
				return p;
		}
		return null;
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
		boolean filter_private = JFritzUtils.parseBoolean(JFritz
				.getProperty("filter_private"));

		String filterSearch = JFritz.getProperty("filter.Phonebook.search", "");
/*        try {
            jfritz.getJframe().getCallerTable().getCellEditor()
                    .cancelCellEditing();
        } catch (NullPointerException e) {
        }
*/

        if ((filter_private) || (filterSearch.length() > 0)) {
    		if (filter_private) {
    			Enumeration en = unfilteredPersons.elements();
    			Vector newFilteredPersons;
    			newFilteredPersons = new Vector();
    			while (en.hasMoreElements()) {
    				Person current = (Person) en.nextElement();
    				if (current.isPrivateEntry()) {
    					newFilteredPersons.add(current);
    				}
    			}
    			filteredPersons = newFilteredPersons;
    			sortAllFilteredRows();
    		}
    		if(filterSearch.length() > 0)
    		{
    			Enumeration en = unfilteredPersons.elements();
    			Vector newFilteredPersons = new Vector();
    			while (en.hasMoreElements()) {
    				Person current = (Person) en.nextElement();
    				int matchFilter=0;
                    // SearchFilter: Number, Participant, Date
                    String parts[] = filterSearch.split(" ");
                    for (int i = 0; i < parts.length; i++) {
                        String part = parts[i];
                        if (part.length() > 0
                                && ((current == null || current
                                        .getStandardTelephoneNumber().getAreaNumber().indexOf(
                                                parts[i]) != -1)
                                || (current == null || current
                                        .getFullname().toLowerCase().indexOf(
                                                part.toLowerCase()) != -1))) {
                        	matchFilter++;
                        }

                    }
                    if (matchFilter==parts.length)
                    	newFilteredPersons.add(current);
    			}
    			filteredPersons = newFilteredPersons;
    			sortAllFilteredRows();
    		}
        }
        else{
            // Use unfiltered data
			filteredPersons = unfilteredPersons;
			sortAllFilteredRows();
        }
	}

	/**
	 * @author Brian Jensen
	 * function reads the thunderbird csv file line for line
	 * adding new contacts after each line
	 *
	 * @param filename is the path to a valid thunderbird csv file
	 */
	public void importFromThunderbirdCSVfile(String filename){
	    Debug.msg("Importing Thunderbird Contacts from csv file " + filename);
	    String line = "";
	    try {
	      FileReader fr = new FileReader(filename);
	          BufferedReader br = new BufferedReader(fr);

	          int linesRead = 0;
	          int newEntries = 0;
	          while(null != (line = br.readLine())){
	        	  linesRead++;
	              Person person = parseContactsThunderbirdCSV(line);
	              if(person != null)
	            	  if(addEntry(person))
	            		  newEntries++;


	          }

	          Debug.msg(linesRead+" Lines read from Thunderbird csv file "+filename);
	          Debug.msg(newEntries+" New contacts processed");

	          if (newEntries > 0) {
	        	  sortAllUnfilteredRows();
	              saveToXMLFile(JFritz.PHONEBOOK_FILE);
	              String msg;

	              if (newEntries == 1) {
	                msg = JFritz.getMessage("imported_contact");
	              } else {
	                msg = newEntries + " "+JFritz.getMessage("imported_contacts");
	              }
	              JFritz.infoMsg(msg);

	          }else{
	        	  JFritz.infoMsg(JFritz.getMessage("no_imported_contacts"));
	          }

	          br.close();

	    } catch (FileNotFoundException e) {
	    	Debug.err("Could not read from " + filename + "!");
	    } catch(IOException e){
	    	Debug.err("IO Exception reading csv file");
	    }
	}

	/**
	 * @author Brian Jensen
	 *
	 * function parses out relevant contact information from a csv file,
	 * if no telephone number is found or the format is invalid
	 * null is returned
	 * tested with thunderbird version 1.50
	 *
	 * Note: This class does NOT check for valid telephone numbers!
	 *
	 * @param string line is the current line of the csv file
	 * @return returns a person object if a telephone number can be processed from the datei
	 */
	public Person parseContactsThunderbirdCSV(String line){
	    String[] field = line.split(PATTERN_THUNDERBRID_CSV);
	    Person person;

	    //check if line has correct amount of entries
	    if(field.length < 36){
	      Debug.err("Invalid Thunderbird CSV format!");
	      return null;
	    }

	    //check first if the entry even has a phone number
	    //Debug.msg(field[6]+"   "+field[7]+"   "+field[8]+"   "+field[9]+"   "+field[10]);
	    if (field[6].equals("") && field[7].equals("") && field[8].equals("") &&
	    		field[9].equals("") && field[10].equals("")){
	    	Debug.msg("No phone number present for contact");
	    	return null;
	    }

	    //at least a phone number and an email exists because thunderbird
	    //is an email client and stores at least an email addy
	    //so create a new person object
	    person = new Person(field[0], field[25], field[1],
	    		field[11]+field[12], field[15], field[13], field[5]);

	    //TODO: Check for valid numbers, as you can never gurantee
	    //that users do things properly, could be possible to create
	    //contacts in the phonebook with no phone number = useless

	    //Work number
	    if(!field[6].equals(""))
	    	person.addNumber(field[6], "business");

	    //home number
	    if(!field[7].equals(""))
	    	person.addNumber(field[7], "home");

	    //fax number
	    if(!field[8].equals(""))
	    	person.addNumber(field[8], "fax");

	    //pager number
	    if(!field[9].equals(""))
	    	person.addNumber(field[9], "other");

	    //Cell phone number
	    if(!field[10].equals(""))
	    	person.addNumber(field[10], "mobile");

	    //lets quit while we're still sane and return the person object
	    return person;

	}



}

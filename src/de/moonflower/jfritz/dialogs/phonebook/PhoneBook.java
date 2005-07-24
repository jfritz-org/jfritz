package de.moonflower.jfritz.dialogs.phonebook;

/**
 * This is the phonebook
 *
 * @author Robert Palmer
 *
 * TODO: Cellrenderer for PrivateCell
 *
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import java.io.UnsupportedEncodingException;

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

	private Vector filteredPersons;
	private Vector unfilteredPersons;

	private JFritz jfritz;

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
				o1 = v1.getFullname().toString();
				o2 = v2.getFullname().toString();
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

	public void addEntry(Person newPerson) {
		Enumeration en = unfilteredPersons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			PhoneNumber pn1 = p.getStandardTelephoneNumber();
			PhoneNumber pn2 = newPerson.getStandardTelephoneNumber();
			if (pn1 != null && pn2 != null
					&& pn1.getFullNumber().equals(pn2.getFullNumber())) {
				return;
			}
		}
		unfilteredPersons.add(newPerson);
		updateFilter();
	}

	public void deleteEntry(Person person) {
		unfilteredPersons.remove(person);
		updateFilter();
	}

	/**
	 * Sorts phonebook alphabetically
	 */
	public synchronized void sort() {
		Collections.sort(unfilteredPersons, new ColumnSorter(0, true));
		this.fireTableDataChanged();
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
			pw.write("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
					+ "\">");
			pw.newLine();
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
						pw.write("\t\t<firstname>" + JFritzUtils.replaceSpecialChars(current.getFirstName())
								+ "</firstname>");
					pw.newLine();
					if (current.getLastName().length() > 0)
						pw.write("\t\t<lastname>" + JFritzUtils.replaceSpecialChars(current.getLastName())
								+ "</lastname>");
					pw.newLine();
					pw.write("\t</name>");
					pw.newLine();
					if (current.getCompany().length() > 0)
						pw.write("\t<company>" + JFritzUtils.replaceSpecialChars(current.getCompany())
								+ "</company>");
					pw.newLine();
				}

				if ((current.getStreet().length() > 0)
						|| (current.getPostalCode().length() > 0)
						|| (current.getCity().length() > 0)) {
					pw.write("\t<address>");
					pw.newLine();
					if (current.getStreet().length() > 0)
						pw.write("\t\t<street>" + JFritzUtils.replaceSpecialChars(current.getStreet())
								+ "</street>");
					pw.newLine();
					if (current.getPostalCode().length() > 0)
						pw.write("\t\t<postcode>" + JFritzUtils.replaceSpecialChars(current.getPostalCode())
								+ "</postcode>");
					pw.newLine();
					if (current.getCity().length() > 0)
						pw
						.write("\t\t<city>" + JFritzUtils.replaceSpecialChars(current.getCity())
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
							+ JFritzUtils.replaceSpecialChars(nr.getFullNumber()) + "</number>");
					pw.newLine();

				}
				pw.write("\t</phonenumbers>");
				pw.newLine();

				if (current.getEmailAddress().length() > 0) {
					pw.write("\t<internet>");
					pw.newLine();
					if (current.getEmailAddress().length() > 0)
						pw.write("\t\t<email>" + JFritzUtils.replaceSpecialChars(current.getEmailAddress())
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
			e.printStackTrace();
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
		if (number == null)
			return null;
		Enumeration en = filteredPersons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			Vector numbers = p.getNumbers();
			Enumeration en2 = numbers.elements();
			while (en2.hasMoreElements()) {
				PhoneNumber n = (PhoneNumber) en2.nextElement();
				if (n.getFullNumber().equals(number.getFullNumber())) {
					return p;
				}
			}
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
			sort();
		}
		else {
			filteredPersons = unfilteredPersons;
			sort();
		}
	}
}

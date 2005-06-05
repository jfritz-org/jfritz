package de.moonflower.jfritz.dialogs.phonebook;

/**
 * This is the phonebook
 *
 * @author Robert Palmer
 *
 * TODO: Sonderzeichen beim Speichern und lesen ersetzen, sonst ist das
 * Phonebook nicht lesbar
 *
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
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

public class PhoneBook extends AbstractTableModel {
	private static final String PHONEBOOK_DTD_URI = "http://jfritz.moonflower.de/dtd/phonebook.dtd";

	// TODO Write correct dtd
	private static final String PHONEBOOK_DTD = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
			+ "<!-- DTD for JFritz phonebook -->"
			+ "<!ELEMENT firstname (commment?,entry*)>"
			+ "<!ELEMENT middlename (#PCDATA)>"
			+ "<!ELEMENT lastname (#PCDATA)>"
			+ "<!ELEMENT entry (firstname?,middlename?,lastname?)>";

	private final String columnNames[] = { "fullName", "telephoneNumber",
			"address", "city", "last_call" };

	private Vector persons;

	private JFritz jfritz;

	public PhoneBook(JFritz jfritz) {
		this.jfritz = jfritz;
		persons = new Vector();

	}

	public Vector getPersons() {
		return persons;
	}

	public void addEntry(Person newPerson) {
		Enumeration en = persons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			PhoneNumber pn1 = p.getStandardTelephoneNumber();
			PhoneNumber pn2 = newPerson.getStandardTelephoneNumber();
			if (pn1 != null && pn2 != null
					&& pn1.getNumber().equals(pn2.getNumber())) {
				return;
			}
		}
		persons.add(newPerson);
	}

	/**
	 * Saves phonebook to xml file.
	 *
	 * @param filename
	 */
	public synchronized void saveToXMLFile(String filename) {
		Debug.msg("Saving to file " + filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			pw.println("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
					+ "\">");
			pw.println("<phonebook>");
			pw.println("<comment>Phonebook for " + JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION + "</comment>");
			Enumeration en = persons.elements();
			while (en.hasMoreElements()) {
				Person current = (Person) en.nextElement();
				pw.println("<entry>");
				if (current.getFullname().length() > 0) {
					pw.println("\t<name>");
					if (current.getFirstName().length() > 0)
						pw.println("\t\t<firstname>" + current.getFirstName()
								+ "</firstname>");
					if (current.getMiddleName().length() > 0)
						pw.println("\t\t<middlename>" + current.getMiddleName()
								+ "</middlename>");
					if (current.getLastName().length() > 0)
						pw.println("\t\t<lastname>" + current.getLastName()
								+ "</lastname>");
					pw.println("\t</name>");
				}

				if ((current.getStreet().length() > 0)
						|| (current.getPostalCode().length() > 0)
						|| (current.getCity().length() > 0)) {
					pw.println("\t<address>");
					if (current.getStreet().length() > 0)
						pw.println("\t\t<street>" + current.getStreet()
								+ "</street>");
					if (current.getPostalCode().length() > 0)
						pw.println("\t\t<postcode>" + current.getPostalCode()
								+ "</postcode>");
					if (current.getCity().length() > 0)
						pw
								.println("\t\t<city>" + current.getCity()
										+ "</city>");
					pw.println("\t</address>");
				}

				pw.println("\t<phonenumbers standard=\""
						+ current.getStandard() + "\">");
				Enumeration en2 = current.getNumbers().elements();
				while (en2.hasMoreElements()) {
					PhoneNumber nr = (PhoneNumber) en2.nextElement();
					pw.println("\t\t<number type=\"" + nr.getType() + "\">"
							+ nr.getNumber() + "</number>");

				}
				pw.println("\t</phonenumbers>");

				if (current.getEmailAddress().length() > 0) {
					pw.println("\t<internet>");
					if (current.getEmailAddress().length() > 0)
						pw.println("\t\t<email>" + current.getEmailAddress()
								+ "</email>");
					pw.println("\t</internet>");
				}
				/*
				 * if (current.getCategory().length() > 0) { pw.println("\t
				 * <categories>"); if (current.getCategory().length() > 0)
				 * pw.println("\t\t <category>" + current.getCategory() + "
				 * </category>"); pw.println("\t </categories>"); }
				 */
				pw.println("</entry>");
			}
			pw.println("</phonebook>");
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!");
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
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Person person = (Person) persons.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return person.getFullname();
		case 1:
			return person.getStandardTelephoneNumber();
		case 2:
			return person.getStreet();
		case 3:
			return (person.getPostalCode() + " " + person.getCity()).trim();
		case 4:
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
			return (Person) persons.get(rowIndex);
		else
			return null;
	}

	/**
	 * Replaces Phonebook entries with Vector of new Phonebook entries
	 *
	 * @param pb
	 */
	public void updatePersons(Vector pb) {
		this.persons = pb;
	}

	public int getRowCount() {
		return persons.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		try {
			return jfritz.getMessages().getString(columnNames[column]);
		} catch (Exception e) {
			return columnNames[column];
		}
	}

	public Person findPerson(PhoneNumber number) {
		if (number == null)
			return null;
		Enumeration en = persons.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			Vector numbers = p.getNumbers();
			Enumeration en2 = numbers.elements();
			while (en2.hasMoreElements()) {
				PhoneNumber n = (PhoneNumber) en2.nextElement();
				if (n.getNumber().equals(number.getNumber())) {
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
}

package de.moonflower.jfritz.dialogs.phonebook;

/**
 * This is the phonebook
 *
 * @author Robert Palmer
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
import de.moonflower.jfritz.utils.Debug;

public class PhoneBook extends AbstractTableModel {
	private static final String PHONEBOOK_DTD_URI = "http://jfritz.moonflower.de/dtd/phonebook.dtd";

	private static final String PHONEBOOK_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!-- DTD for JFritz phonebook -->"
			+ "<!ELEMENT firstname (commment?,entry*)>"
			+ "<!ELEMENT middlename (#PCDATA)>"
			+ "<!ELEMENT lastname (#PCDATA)>"
			+ "<!ELEMENT entry (firstname?,middlename?,lastname?)>";

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
		persons.add(newPerson);
	}

	/**
	 * Saves phonebook to xml file.
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
			pw.println("<!DOCTYPE phonebook SYSTEM \"" + PHONEBOOK_DTD_URI
					+ "\">");
			pw.println("<phonebook>");
			pw.println("<comment>Phonebook for " + JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION + "</comment>");
			Enumeration en = persons.elements();
			while (en.hasMoreElements()) {
				Person current = (Person) en.nextElement();
				pw.println("<entry>");
				pw.println("\t<firstname>" + current.getFirstName()
						+ "</firstname>");
				pw.println("\t<middlename>" + current.getMiddleName()
						+ "</middlename>");
				pw.println("\t<lastname>" + current.getLastName()
						+ "</lastname>");
				pw.println("\t<street>" + current.getStreet() + "</street>");
				pw.println("\t<postcode>" + current.getPostalCode()
						+ "</postcode>");
				pw.println("\t<city>" + current.getCity() + "</city>");
				pw.println("\t<homenumber>" + current.getHomeTelNumber()
						+ "</homenumber>");
				pw.println("\t<mobilenumber>" + current.getMobileTelNumber()
						+ "</mobilenumber>");
				pw.println("\t<businessnumber>"
						+ current.getBusinessTelNumber() + "</businessnumber>");
				pw.println("\t<othernumber>" + current.getOtherTelNumber()
						+ "</othernumber>");
				pw
						.println("\t<email>" + current.getEmailAddress()
								+ "</email>");
				pw.println("\t<category>" + current.getCategory()
						+ "</category>");
				pw.println("</entry>");
			}
			pw.println("</phonebook>");
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!");
		}
	}

	public void loadFromXMLFile(String filename) {
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
			return person.getFirstName();
		case 1:
			return person.getMiddleName();
		case 2:
			return person.getLastName();
		case 3:
			return person.getHomeTelNumber();
		case 4:
			return person.getMobileTelNumber();
		case 5:
			return person.getBusinessTelNumber();
		case 6:
			return person.getOtherTelNumber();
		case 7:
			return person.getEmailAddress();
		case 8:
			return person.getStreet();
		case 9:
			return person.getPostalCode();
		case 10:
			return person.getCity();
		case 11:
			return person.getCategory();
		default:
			throw new IllegalArgumentException("Invalid column: " + columnIndex);
		}
	}

	public void updatePersons(Vector pb) {
		this.persons = pb;
	}

	public int getRowCount() {
		return persons.size();
	}

	public int getColumnCount() {
		return 12;
	}

	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return jfritz.getMessages().getString("firstName");
		case 1:
			return jfritz.getMessages().getString("middleName");
		case 2:
			return jfritz.getMessages().getString("lastName");
		case 3:
			return jfritz.getMessages().getString("homeTelephoneNumber");
		case 4:
			return jfritz.getMessages().getString("mobileTelephoneNumber");
		case 5:
			return jfritz.getMessages().getString("businessTelephoneNumber");
		case 6:
			return jfritz.getMessages().getString("otherTelephoneNumber");
		case 7:
			return jfritz.getMessages().getString("emailAddress");
		case 8:
			return jfritz.getMessages().getString("street");
		case 9:
			return jfritz.getMessages().getString("postalCode");
		case 10:
			return jfritz.getMessages().getString("city");
		case 11:
			return jfritz.getMessages().getString("category");

		default:
			return null;
		}
	}
}

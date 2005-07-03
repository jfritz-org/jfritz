/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.util.Date;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class PhonebookFileXMLHandler extends DefaultHandler {

	String firstName, company, lastName, type, standard, email, street,
			postCode, city, category;

	Vector numbers;

	String chars;

	PhoneBook phonebook;

	Person person;

	Date calldate;

	int duration;

	boolean privateEntry;

	public PhonebookFileXMLHandler(PhoneBook phonebook) {
		super();
		this.phonebook = phonebook;
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		String eName = lName;
		if ("".equals(eName))
			eName = qName;

		chars = ""; // Important to clear buffer :)

		if (eName.equals("entry")) {
			if (attrs.getValue("private").equals("true")) {
				privateEntry = true;
				}
			else {
				privateEntry = false;
				}
			firstName = "";
			company = "";
			lastName = "";
			numbers = new Vector();
			street = "";
			postCode = "";
			city = "";
			email = "";
			category = "";
			type = "home";
			standard = "home";
		} else if (eName.equals("phonenumbers")) {
			standard = attrs.getValue("standard");
		} else if (eName.equals("number")) {
			type = attrs.getValue("type");
			// Debug.msg("STD: "+standard+" TYPE: "+type);
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		if (qName.equals("firstname")) {
			firstName = chars;
		} else if (qName.equals("company")) {
			company = chars;
		} else if (qName.equals("lastname")) {
			lastName = chars;
		} else if (qName.equals("number")) {
			numbers.add(new PhoneNumber(chars, type));
			// Debug.msg("Adding nr "+chars+" type "+type+ " std "+standard);
		} else if (qName.equals("street")) {
			street = chars;
		} else if (qName.equals("postcode")) {
			postCode = chars;
		} else if (qName.equals("city")) {
			city = chars;
		} else if (qName.equals("email")) {
			email = chars;
		} else if (qName.equals("category")) {
			category = chars;
		} else if (qName.equals("entry")) {
			Person newPerson = new Person(firstName, company, lastName,
					street, postCode, city, email);

			newPerson.setNumbers(numbers, standard);
			newPerson.setPrivateEntry(privateEntry);
			phonebook.addEntry(newPerson);

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}
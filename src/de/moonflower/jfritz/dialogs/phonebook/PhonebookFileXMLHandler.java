/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.Person;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class PhonebookFileXMLHandler extends DefaultHandler {

	String firstName, middleName, lastName, type, standard, homeNumber,
			mobileNumber, businessNumber, otherNumber, standardNumber, email,
			street, postCode, city, category;

	String chars;

	PhoneBook phonebook;

	Person person;

	Date calldate;

	int duration;

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
			firstName = "";
			middleName = "";
			lastName = "";
			homeNumber = "";
			mobileNumber = "";
			businessNumber = "";
			otherNumber = "";
			standardNumber = "";
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
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		if (qName.equals("firstname")) {
			firstName = chars;
		} else if (qName.equals("middlename")) {
			middleName = chars;
		} else if (qName.equals("lastname")) {
			lastName = chars;
		} else if (qName.equals("number")) {
			if (type.equals("home")) {
				homeNumber = chars;
			} else if (type.equals("mobile")) {
				mobileNumber = chars;
			} else if (type.equals("business")) {
				businessNumber = chars;
			} else if (type.equals("other")) {
				otherNumber = chars;
			}
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

			if (standard.equals("mobile"))
				standardNumber = mobileNumber;
			else if (standard.equals("business"))
				standardNumber = businessNumber;
			else if (standard.equals("other"))
				standardNumber = otherNumber;
			else
				standardNumber = homeNumber;

			Person newPerson = new Person(firstName, middleName, lastName,
					street, postCode, city, homeNumber, mobileNumber,
					businessNumber, otherNumber, standardNumber, email,
					category);
			phonebook.addEntry(newPerson);

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}
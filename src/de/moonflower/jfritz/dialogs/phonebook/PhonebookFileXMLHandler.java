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


/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class PhonebookFileXMLHandler extends DefaultHandler {

	String firstName, middleName, lastName, homeNumber, mobileNumber, businessNumber, otherNumber,
			email, street, postCode, city, category;

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
		} else if (qName.equals("homenumber")) {
			homeNumber = chars;
		} else if (qName.equals("mobilenumber")) {
			mobileNumber = chars;
		} else if (qName.equals("businessnumber")) {
			businessNumber = chars;
		} else if (qName.equals("othernumber")) {
			otherNumber = chars;
		} else if (qName.equals("street")) {
			street = chars;
		} else if (qName.equals("postcode")) {
			postCode = chars;
		} else if (qName.equals("city")) {
			city = chars;
		} else if (qName.equals("email")) {
			email = chars;
		} else if (qName.equals("cytegory")) {
			category = chars;
		} else if (qName.equals("entry")) {

			Person newPerson = new Person(firstName,middleName,lastName,
					street,postCode, city,
					homeNumber, mobileNumber,
					businessNumber, otherNumber,
					email, category);
			if (phonebook != null) { // Add an entry to the callerlist
				phonebook.addEntry(newPerson);
			}

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

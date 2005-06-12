/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.QuickDial;
import de.moonflower.jfritz.utils.Debug;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 */
public class QuickDialXMLHandler extends DefaultHandler {

	String chars, id, number, vanity, description;

	QuickDial quickdial;

	QuickDials dataModel;

	public QuickDialXMLHandler(QuickDials dataModel) {
		super();
		this.dataModel = dataModel;
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
			id = "";
			number = "";
			vanity = "";
			description = "";
		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName))
					aName = attrs.getQName(i);
				if (eName.equals("entry") && aName.equals("id")) {
					id = attrs.getValue(i);
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		if (qName.equals("number")) {
			number = chars;
		} else if (qName.equals("vanity")) {
			vanity = chars;
		} else if (qName.equals("description")) {
			description = chars;
		} else if (qName.equals("entry")) {
			if (dataModel != null) { // Add an entry to the dataModel
				Debug.msg("QuickDial: "+id+", "+number+", "+vanity+", "+description);
				dataModel.addEntry(new QuickDial(id, vanity, number, description));
			}
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

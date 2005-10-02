/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * XML Handler for reading the sip provider file
 *
 * @author Arno Willig
 *
 */
public class SIPFileXMLHandler extends DefaultHandler {

	String chars, providerName, phoneNumber;

	boolean active;

	int providerID;

	SipProviderTableModel tableModel;

	public SIPFileXMLHandler(SipProviderTableModel tableModel) {
		super();
		this.tableModel = tableModel;
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

		if (eName.equals("provider")) {
			providerName = "";
			phoneNumber = "";
			providerID = 0;
			active = false;
		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName))
					aName = attrs.getQName(i);
				if (eName.equals("entry") && aName.equals("id")) {
					providerID = Integer.parseInt(attrs.getValue(i));
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		if (qName.equals("name")) {
			providerName = chars;
		} else if (qName.equals("number")) {
			phoneNumber = chars;
		} else if (qName.equals("active")) {
			active = JFritzUtils.parseBoolean(chars);
		} else if (qName.equals("entry")) {

			if (tableModel != null) { // Add an entry to the callerlist
			    tableModel.addProvider(new SipProvider(providerID, phoneNumber, providerName, active));
			}

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

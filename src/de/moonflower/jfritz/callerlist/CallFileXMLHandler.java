/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class CallFileXMLHandler extends DefaultHandler {

	String chars, caller, callbycall, port, route;

	CallerList callerlist;

	CallType calltype;

	Date calldate;

	int duration;

	public CallFileXMLHandler(CallerList callerlist) {
		super();
		this.callerlist = callerlist;
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
			port = "";
			route = "";
			caller = "";
			callbycall = "";
			duration = 0;
			calldate = null;
			calltype = null;
		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName))
					aName = attrs.getQName(i);
				if (eName.equals("entry") && aName.equals("calltype")) {
					calltype = new CallType(attrs.getValue(i));
				} else if (eName.equals("caller") && aName.equals("callbycall")) {
					callbycall = attrs.getValue(i);
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

		if (qName.equals("duration")) {
			duration = Integer.parseInt(chars);
		} else if (qName.equals("port")) {
			port = chars;
		} else if (qName.equals("route")) {
			route = chars;
		} else if (qName.equals("caller")) {
			caller = chars;
		} else if (qName.equals("date")) {
			try {
				calldate = df.parse(chars.replaceAll("\"", ""));
			} catch (ParseException e) {
				Debug.err("Date problem:  " + chars);
				System.exit(0);
				calldate = null;
			}
		} else if (qName.equals("entry")) {

			if (callerlist != null) { // Add an entry to the callerlist
				PhoneNumber number = null;
				if (caller.length() > 0) {
					number = new PhoneNumber(caller);
					if (callbycall.length() > 0)
						number.setCallByCall(callbycall);
				}
				callerlist.addEntry(calltype, calldate, number, port, route,
						duration);
			}

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

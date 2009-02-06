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

import de.moonflower.jfritz.struct.Call;
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

	String chars, caller, callbycall, port, route, comment;

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
		if ("".equals(eName)) //$NON-NLS-1$
			eName = qName;

		//	Important to clear buffer :)
		chars = "";  //$NON-NLS-1$

		if (eName.equals("entry")) { //$NON-NLS-1$
			port = ""; //$NON-NLS-1$
			route = ""; //$NON-NLS-1$
			caller = ""; //$NON-NLS-1$
			callbycall = ""; //$NON-NLS-1$
			comment = ""; //$NON-NLS-1$
			duration = 0;
			calldate = null;
			calltype = null;
		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) //$NON-NLS-1$
					aName = attrs.getQName(i);
				if (eName.equals("entry") && aName.equals("calltype")) { //$NON-NLS-1$,  //$NON-NLS-2$
					calltype = new CallType(attrs.getValue(i));
				} else if (eName.equals("caller") && aName.equals("callbycall")) { //$NON-NLS-1$,  //$NON-NLS-2$
					callbycall = attrs.getValue(i);
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$

		if (qName.equals("duration")) { //$NON-NLS-1$
			duration = Integer.parseInt(chars);
		} else if (qName.equals("port")) { //$NON-NLS-1$
			port = chars;
		} else if (qName.equals("route")) { //$NON-NLS-1$
			route = chars;
		} else if (qName.equals("caller")) { //$NON-NLS-1$
			caller = chars;
		} else if (qName.equals("comment")) { //$NON-NLS-1$
			comment = chars;
		} else if (qName.equals("date")) { //$NON-NLS-1$
			try {
				calldate = df.parse(chars.replaceAll("\"", "")); //$NON-NLS-1$,  //$NON-NLS-2$
			} catch (ParseException e) {
				Debug.err("Date problem:  " + chars); //$NON-NLS-1$
				Debug.errDlg("Date problem");
				calldate = null;
				return;
			}
		} else if (qName.equals("entry")) { //$NON-NLS-1$

			if (callerlist != null) { // Add an entry to the callerlist
				PhoneNumber number = null;
				if (caller.length() > 0) {
					number = new PhoneNumber(caller, false);
					if (callbycall.length() > 0)
						number.setCallByCall(callbycall);
				}
				callerlist.addEntry(new Call(calltype, calldate, number, port, route, duration,
						comment));
			}

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

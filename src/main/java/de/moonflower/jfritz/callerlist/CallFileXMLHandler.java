/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class CallFileXMLHandler extends DefaultHandler {
	private final static Logger log = Logger.getLogger(CallFileXMLHandler.class);

	Vector<Call> newCalls;

	String chars, caller, callbycall, portStr, route, comment;

	CallerList callerlist;

	CallType calltype;

	Date calldate;

	int duration;
	
	private PropertyProvider properties = PropertyProvider.getInstance();

	public CallFileXMLHandler(CallerList callerlist) {
		super();
		this.callerlist = callerlist;
		newCalls = new Vector<Call>(1024);
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
		callerlist.addEntries(newCalls);
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		String eName = lName;
		if ("".equals(eName)) //$NON-NLS-1$
			eName = qName;

		//	Important to clear buffer :)
		chars = "";  //$NON-NLS-1$

		if (eName.equals("entry")) { //$NON-NLS-1$
			portStr = ""; //$NON-NLS-1$
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
					calltype = CallType.getByString(attrs.getValue(i));
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
			portStr = chars;
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
				log.error("Date problem:  " + chars, e); //$NON-NLS-1$
				Debug.errDlg("Date problem");
				calldate = null;
				return;
			}
		} else if (qName.equals("entry")) { //$NON-NLS-1$

			if (callerlist != null) { // Add an entry to the callerlist
				PhoneNumberOld number = null;
				if (caller.length() > 0) {
					number = new PhoneNumberOld(this.properties, caller, false);
					if (callbycall.length() > 0)
						number.setCallByCall(callbycall);
				}

				Port port = null;
				try {
					int portId = Integer.parseInt(portStr);
					port = Port.getPort(portId);
				} catch (NumberFormatException nfe)
				{
					port = new Port(0, portStr, "-1", "-1");
				}
				newCalls.add(new Call(calltype, calldate, number, port, route, duration,
						comment));
			}

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

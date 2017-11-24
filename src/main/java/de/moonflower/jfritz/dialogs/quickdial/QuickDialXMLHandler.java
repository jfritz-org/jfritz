/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.QuickDial;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 */
public class QuickDialXMLHandler extends DefaultHandler {
	private final static Logger log = Logger.getLogger(QuickDialXMLHandler.class);

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
		if ("".equals(eName)) //$NON-NLS-1$
			eName = qName;

		// Important to clear buffer :)
		chars = "";  //$NON-NLS-1$

		if (eName.equals("entry")) { //$NON-NLS-1$
			id = ""; //$NON-NLS-1$
			number = ""; //$NON-NLS-1$
			vanity = ""; //$NON-NLS-1$
			description = ""; //$NON-NLS-1$
		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) //$NON-NLS-1$
					aName = attrs.getQName(i);
				if (eName.equals("entry") && aName.equals("id")) { //$NON-NLS-1$,  //$NON-NLS-2$
					id = attrs.getValue(i);
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		if (qName.equals("number")) { //$NON-NLS-1$
			number = chars;
		} else if (qName.equals("vanity")) { //$NON-NLS-1$
			vanity = chars;
		} else if (qName.equals("description")) { //$NON-NLS-1$
			description = chars;
		} else if (qName.equals("entry")) { //$NON-NLS-1$
			if (dataModel != null) { // Add an entry to the dataModel
				log.debug("QuickDial: "+id+ //$NON-NLS-1$
						", "+number+ //$NON-NLS-1$
						", "+vanity+ //$NON-NLS-1$
						", "+description); //$NON-NLS-1$
				dataModel.addEntry(new QuickDial(id, vanity, number, description));
			}
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

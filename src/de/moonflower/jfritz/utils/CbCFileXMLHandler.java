package de.moonflower.jfritz.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.CallByCall;
import de.moonflower.jfritz.utils.Debug;

/**
 * XML Handler for reading the call by call xml file
 * Note: This file is sensitive to the order in which elements
 * are placed in the file
 *
 * @author Brian Jensen
 *
 */
public class CbCFileXMLHandler extends DefaultHandler {

	String chars, prefix, country_code;

	CallByCall cbc[];

	int length, cbc_count;

	public CbCFileXMLHandler() {
		super();
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

		if (eName.equals("country")) { //$NON-NLS-1$

			cbc_count = 0;
			//made extra large on purpose, will be resized later
			cbc = new CallByCall[20];
			length = 0;
			prefix = "";

		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) //$NON-NLS-1$
					aName = attrs.getQName(i);
				if (eName.equals("country") && aName.equals("code")) { //$NON-NLS-1$,  //$NON-NLS-2$
					country_code = attrs.getValue(i);
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		if (qName.equals("prefix")) { //$NON-NLS-1$
			prefix = chars;
		} else if (qName.equals("length")) { //$NON-NLS-1$
			length = Integer.parseInt(chars);
		} else if (qName.equals("callbycall")) { //$NON-NLS-1$
			Debug.info("Call by Call for "+country_code+" added. Prefix: "+
					prefix+" Length: "+length);
			cbc[cbc_count] = new CallByCall(prefix, length);
			cbc_count++;
		} else if (qName.equals("country")) { //$NON-NLS-1$
			if(!country_code.equals("")){

				//before we place the object in our existing list we need to resize it
				CallByCall[] cbc_resizedList = new CallByCall[cbc_count];
				for(int i = 0; i < cbc_count; i++)
					cbc_resizedList[i] = cbc[i];

				//Add the entry to out list
				PhoneNumber.addCallbyCall(country_code, cbc_resizedList);
			}


		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}
}
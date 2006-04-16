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

	boolean active = false;

	int providerID;

	int startDate = 1, festnetzTakt1 = 60, festnetzTakt2 = 60, festnetzFreiminuten = 0,
		mobileTakt1 = 60, mobileTakt2 = 60, mobileFreiminuten = 0, warnFreiminuten = -1;

	double festnetzKosten = 1.5, mobileKosten = 23;

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
		if ("".equals(eName)) //$NON-NLS-1$
			eName = qName;

		// Important to clear buffer :)
		chars = "";  //$NON-NLS-1$

		if (eName.equals("provider")) { //$NON-NLS-1$
			providerName = ""; //$NON-NLS-1$
			phoneNumber = ""; //$NON-NLS-1$
			providerID = 0; //$NON-NLS-1$
			active = false;
		}
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) //$NON-NLS-1$
					aName = attrs.getQName(i);
				if (eName.equals("entry") && aName.equals("id")) { //$NON-NLS-1$,  //$NON-NLS-2$
					providerID = Integer.parseInt(attrs.getValue(i));
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		if (qName.equals("name")) { //$NON-NLS-1$
			providerName = chars;
		} else if (qName.equals("number")) { //$NON-NLS-1$
			phoneNumber = chars;
		} else if (qName.equals("active")) { //$NON-NLS-1$
			active = JFritzUtils.parseBoolean(chars);
		} else if (qName.equals("startdate")) { //$NON-NLS-1$
			startDate = Integer.parseInt(chars);
		} else if (qName.equals("festnetztakt1")) { //$NON-NLS-1$
			festnetzTakt1 = Integer.parseInt(chars);
		} else if (qName.equals("festnetztakt2")) { //$NON-NLS-1$
			festnetzTakt2 = Integer.parseInt(chars);
		} else if (qName.equals("festnetzkosten")) { //$NON-NLS-1$
			festnetzKosten = Double.parseDouble(chars);
		} else if (qName.equals("festnetzfreiminuten")) { //$NON-NLS-1$
			festnetzFreiminuten = Integer.parseInt(chars);
		} else if (qName.equals("mobiletakt1")) { //$NON-NLS-1$
			mobileTakt1 = Integer.parseInt(chars);
		} else if (qName.equals("mobiletakt2")) { //$NON-NLS-1$
			mobileTakt2 = Integer.parseInt(chars);
		} else if (qName.equals("mobilekosten")) { //$NON-NLS-1$
			mobileKosten = Double.parseDouble(chars);
		} else if (qName.equals("mobilefreiminuten")) { //$NON-NLS-1$
			mobileFreiminuten = Integer.parseInt(chars);
		} else if (qName.equals("warnfreiminuten")) { //$NON-NLS-1$
			warnFreiminuten = Integer.parseInt(chars);
		} else if (qName.equals("entry")) { //$NON-NLS-1$

			if (tableModel != null) { // Add an entry to the callerlist
			    SipProvider sipProvider = new SipProvider(providerID, phoneNumber, providerName);
			    sipProvider.setActive(active);
			    sipProvider.setStartDate(startDate);
			    sipProvider.setFestnetzTakt1(festnetzTakt1);
			    sipProvider.setFestnetzTakt2(festnetzTakt2);
			    sipProvider.setFestnetzKosten(festnetzKosten);
			    sipProvider.setFestnetzFreiminuten(festnetzFreiminuten);
			    sipProvider.setMobileTakt1(mobileTakt1);
			    sipProvider.setMobileTakt2(mobileTakt2);
			    sipProvider.setMobileKosten(mobileKosten);
			    sipProvider.setMobileFreiminuten(mobileFreiminuten);
			    sipProvider.setWarnFreiminuten(warnFreiminuten);
			    tableModel.addProvider(sipProvider);
			}

		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

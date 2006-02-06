/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.JFritz;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class PropertiesXMLHandler extends DefaultHandler {
    private String chars;

    private String keyEntry;

    public PropertiesXMLHandler() {
        super();
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
            keyEntry = attrs.getValue("key");
        }
    }

    public void endElement(String namespaceURI, String sName, String qName)
            throws SAXException {
        if (qName.equals("entry")) {
            String value = chars;
            JFritz.setProperty(keyEntry, value);
        }
    }

    public void characters(char buf[], int offset, int len) throws SAXException {
        chars += new String(buf, offset, len);
    }

}
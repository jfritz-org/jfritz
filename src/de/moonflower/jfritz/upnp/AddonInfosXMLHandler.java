/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.upnp;

import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Handler for reading the call file
 *
 * @author Arno Willig
 *
 */
public class AddonInfosXMLHandler extends DefaultHandler {

	String chars;

	int ByteSendRate, ByteReceiveRate, PacketSendRate, PacketReceiveRate;

	int TotalBytesSent, TotalBytesReceived;

	int AutoDisconnectTime, IdleDisconnectTime;

	String DNSServer1, DNSServer2;

	public AddonInfosXMLHandler() {
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
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

		if (qName.equals("NewByteSendRate")) {
			ByteSendRate = Integer.parseInt(chars);
		} else if (qName.equals("NewByteReceiveRate")) {
			ByteReceiveRate = Integer.parseInt(chars);
		} else if (qName.equals("NewPacketSendRate")) {
			PacketSendRate = Integer.parseInt(chars);
		} else if (qName.equals("NewReceiveSendRate")) {
			PacketReceiveRate = Integer.parseInt(chars);
		} else if (qName.equals("NewTotalBytesSent")) {
			TotalBytesSent = Integer.parseInt(chars);
		} else if (qName.equals("NewTotalBytesReceived")) {
			TotalBytesReceived = Integer.parseInt(chars);
		} else if (qName.equals("NewAutoDisconnectTime")) {
			AutoDisconnectTime = Integer.parseInt(chars);
		} else if (qName.equals("NewIdleDisconnectTime")) {
			IdleDisconnectTime = Integer.parseInt(chars);
		} else if (qName.equals("NewDNSServer1")) {
			DNSServer1 = chars;
		} else if (qName.equals("NewDNSServer2")) {
			DNSServer2 = chars;
		} else if (qName.equals("u:GetAddonInfosResponse")) {
			System.out.println("FRITZ!Box-Statistik:");
			System.out.println("ByteSendRate: " + ByteSendRate);
			System.out.println("ByteReceiveRate: " + ByteReceiveRate);
			System.out.println("TotalBytesSent: " + TotalBytesSent);
			System.out.println("TotalBytesReceived: " + TotalBytesReceived);
			System.out.println("DNSServer 1: " + DNSServer1);
			System.out.println("DNSServer 2: " + DNSServer2);
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

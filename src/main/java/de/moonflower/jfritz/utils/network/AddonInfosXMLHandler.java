/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils.network;

//import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Handler for reading the output of the addonInfos
 * Web service on the box
 *
 * @author Arno Willig
 *
 */
public class AddonInfosXMLHandler extends DefaultHandler {

	UPNPAddonInfosListener listener;

	String chars;

	String ByteSendRate = "-", ByteReceiveRate = "-", PacketSendRate = "-", PacketReceiveRate = "-";

	String TotalBytesSent = "-", TotalBytesReceived = "-";

	String AutoDisconnectTime = "-", IdleDisconnectTime = "-";

	String DNSServer1 = "-", DNSServer2 = "-";

	String voipDNSServer1 = "-", voipDNSServer2 = "-";

	String upnpControl = "-", routedBridgeMode = "-";

	public AddonInfosXMLHandler(UPNPAddonInfosListener ail) {
		super();
		listener = ail;
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		chars = "";  //$NON-NLS-1$
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		//SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

		if (qName.equals("NewByteSendRate")) { //$NON-NLS-1$
			ByteSendRate = chars;
		} else if (qName.equals("NewByteReceiveRate")) { //$NON-NLS-1$
			ByteReceiveRate = chars;
		} else if (qName.equals("NewPacketSendRate")) { //$NON-NLS-1$
			PacketSendRate = chars;
		} else if (qName.equals("NewPacketReceiveRate")) { //$NON-NLS-1$
			PacketReceiveRate = chars;
		} else if (qName.equals("NewTotalBytesSent")) { //$NON-NLS-1$
			TotalBytesSent = chars;
		} else if (qName.equals("NewTotalBytesReceived")) { //$NON-NLS-1$
			TotalBytesReceived = chars;
		} else if (qName.equals("NewAutoDisconnectTime")) { //$NON-NLS-1$
			AutoDisconnectTime = chars;
		} else if (qName.equals("NewIdleDisconnectTime")) { //$NON-NLS-1$
			IdleDisconnectTime = chars;
		} else if (qName.equals("NewDNSServer1")) { //$NON-NLS-1$
			DNSServer1 = chars;
		} else if (qName.equals("NewDNSServer2")) { //$NON-NLS-1$
			DNSServer2 = chars;
		} else if(qName.equals("NewVoipDNSServer1")){
			voipDNSServer1 = chars;
		} else if(qName.equals("NewVoipDNSServer2")){
			voipDNSServer2 = chars;
		} else if(qName.equals("NewUpnpControlEnabled")){
			upnpControl = chars;
		} else if(qName.equals("NewRoutedBridgedModeBoth")){
			routedBridgeMode = chars;
		} else if (qName.equals("u:GetAddonInfosResponse")) { //$NON-NLS-1$

			listener.setBytesRate(ByteSendRate, ByteReceiveRate);
			listener.setTotalBytesInfo(TotalBytesSent, TotalBytesReceived);
			listener.setDNSInfo(DNSServer1, DNSServer2);
			listener.setVoipDNSInfo(voipDNSServer1, voipDNSServer2);
			listener.setDisconnectInfo(AutoDisconnectTime, IdleDisconnectTime);
			listener.setOtherInfo(upnpControl, routedBridgeMode);
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

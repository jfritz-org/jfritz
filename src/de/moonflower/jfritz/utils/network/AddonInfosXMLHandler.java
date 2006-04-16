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

import de.moonflower.jfritz.dialogs.stats.StatsDialog;

/**
 * XML Handler for reading the statistic file
 *
 * @author Arno Willig
 *
 */
public class AddonInfosXMLHandler extends DefaultHandler {

	StatsDialog statsdialog;

	String chars;

	int ByteSendRate, ByteReceiveRate, PacketSendRate, PacketReceiveRate;

	int TotalBytesSent, TotalBytesReceived;

	int AutoDisconnectTime, IdleDisconnectTime;

	String DNSServer1, DNSServer2;

	public AddonInfosXMLHandler(StatsDialog statsdialog) {
		super();
		this.statsdialog = statsdialog;
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
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		//SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

		if (qName.equals("NewByteSendRate")) { //$NON-NLS-1$
			ByteSendRate = Integer.parseInt(chars);
		} else if (qName.equals("NewByteReceiveRate")) { //$NON-NLS-1$
			ByteReceiveRate = Integer.parseInt(chars);
		} else if (qName.equals("NewPacketSendRate")) { //$NON-NLS-1$
			PacketSendRate = Integer.parseInt(chars);
		} else if (qName.equals("NewReceiveSendRate")) { //$NON-NLS-1$
			PacketReceiveRate = Integer.parseInt(chars);
		} else if (qName.equals("NewTotalBytesSent")) { //$NON-NLS-1$
			TotalBytesSent = Integer.parseInt(chars);
		} else if (qName.equals("NewTotalBytesReceived")) { //$NON-NLS-1$
			TotalBytesReceived = Integer.parseInt(chars);
		} else if (qName.equals("NewAutoDisconnectTime")) { //$NON-NLS-1$
			AutoDisconnectTime = Integer.parseInt(chars);
		} else if (qName.equals("NewIdleDisconnectTime")) { //$NON-NLS-1$
			IdleDisconnectTime = Integer.parseInt(chars);
		} else if (qName.equals("NewDNSServer1")) { //$NON-NLS-1$
			DNSServer1 = chars;
		} else if (qName.equals("NewDNSServer2")) { //$NON-NLS-1$
			DNSServer2 = chars;
		} else if (qName.equals("u:GetAddonInfosResponse")) { //$NON-NLS-1$
			System.out.println("FRITZ!Box-Statistic:"); //$NON-NLS-1$
			System.out.println("ByteSendRate: " + ByteSendRate); //$NON-NLS-1$
			System.out.println("ByteReceiveRate: " + ByteReceiveRate); //$NON-NLS-1$
			System.out.println("TotalBytesSent: " + TotalBytesSent); //$NON-NLS-1$
			System.out.println("TotalBytesReceived: " + TotalBytesReceived); //$NON-NLS-1$
			System.out.println("DNSServer 1: " + DNSServer1); //$NON-NLS-1$
			System.out.println("DNSServer 2: " + DNSServer2); //$NON-NLS-1$
			statsdialog.setAddonInfos(ByteSendRate, ByteReceiveRate,
					TotalBytesSent, TotalBytesReceived, DNSServer1, DNSServer2);
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}

}

package de.moonflower.jfritz.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.moonflower.jfritz.utils.network.UPNPUtils;

public class Samsung {

	private static final String urn = "urn:samsung.com:service:MessageBoxService:1#AddMessage";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY); //$NON-NLS-1$
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY); //$NON-NLS-1$

	private String host = "";
	private String port = "52235";
	private String url = "/PMR/control/MessageBoxService";
	private int callId = 0;
	private Calendar cal;

	public Samsung(final String host) {
		this.host = host;
	}

	public int showCall(final String callerNumber, final String callerName, final String calleeNumber, final String calleeName) {
		callId++;
		cal = Calendar.getInstance();

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" >"
				+ "<s:Body>"
				+ "<u:AddMessage xmlns:u=\"urn:samsung.com:service:MessageBoxService:1\\\">"
				+ "<MessageType>text/xml</MessageType>"
				+ "<MessageID>" + callId + "</MessageID>"
				+ "<Message>"
				+ "&lt;Category&gt;Incoming Call&lt;/Category&gt;"
				+ "&lt;DisplayType&gt;Maximum&lt;/DisplayType&gt;"
				+ "&lt;CallTime&gt;"
				+ "&lt;Date&gt;" + dateFormat.format(cal.getTime()) + "&lt;/Date&gt;"
				+ "&lt;Time&gt;" + timeFormat.format(cal.getTime()) + "&lt;/Time&gt;"
				+ "&lt;/CallTime&gt;"
				+ "&lt;Callee&gt;"
				+ "&lt;Number&gt;" + calleeNumber + "&lt;/Number&gt;"
				+ "&lt;Name&gt;" + calleeName + "&lt;/Name&gt;"
				+ "&lt;/Callee&gt;"
				+ "&lt;Caller&gt;"
				+ "&lt;Number&gt;" + callerNumber + "&lt;/Number&gt;"
				+ "&lt;Name&gt;" + callerName + "&lt;/Name&gt;"
				+ "&lt;/Caller&gt;"
				+ "</Message>"
				+ "</u:AddMessage>"
				+ "</s:Body>"
				+ "</s:Envelope>";

		UPNPUtils.getSOAPData("http://"+host+":"+port+url, urn, xml);

		return callId;
	}

	public int showSMS(final String receiverNumber, final String receiverName, final String senderNumber, final String senderName, final String sms) {
		callId++;
		cal = Calendar.getInstance();

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" >"
				+ "<s:Body>"
				+ "<u:AddMessage xmlns:u=\"urn:samsung.com:service:MessageBoxService:1\\\">"
				+ "<MessageType>text/xml</MessageType>"
				+ "<MessageID>" + callId + "</MessageID>"
				+ "<Message>"

				+ "&lt;Category&gt;SMS&lt;/Category&gt;"
				+ "&lt;DisplayType&gt;Maximum&lt;/DisplayType&gt;"
				+ "&lt;ReceiveTime&gt;"
				+ "&lt;Date&gt;" + dateFormat.format(cal.getTime()) + "&lt;/Date&gt;"
				+ "&lt;Time&gt;" + timeFormat.format(cal.getTime()) + "&lt;/Time&gt;"
				+ "&lt;/ReceiveTime&gt;"
				+ "&lt;Receiver&gt;"
				+ "&lt;Number&gt;" + receiverNumber + "&lt;/Number&gt;"
				+ "&lt;Name&gt;" + receiverName + "&lt;/Name&gt;"
				+ "&lt;/Receiver&gt;"
				+ "&lt;Sender&gt;"
				+ "&lt;Number&gt;" + senderNumber + "&lt;/Number&gt;"
				+ "&lt;Name&gt;" + senderName + "&lt;/Name&gt;"
				+ "&lt;/Sender&gt;"
				+ "&lt;Body&gt;"
				+ sms
				+ "&lt;/Body&gt;"
				+ "</Message>"
				+ "</u:AddMessage>"
				+ "</s:Body>"
				+ "</s:Envelope>";

		UPNPUtils.getSOAPData("http://"+host+":"+port+url, urn, xml);

		return callId;
	}

	public int showSchedule(final Date startDate, final Date endDate, final String ownerNumber, final String ownerName, final String subject, final String location, final String body) {
		callId++;

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" >"
				+ "<s:Body>"
				+ "<u:AddMessage xmlns:u=\"urn:samsung.com:service:MessageBoxService:1\\\">"
				+ "<MessageType>text/xml</MessageType>"
				+ "<MessageID>" + callId + "</MessageID>"
				+ "<Message>"

				+ "&lt;Category&gt;Schedule Reminder&lt;/Category&gt;"
				+ "&lt;DisplayType&gt;Maximum&lt;/DisplayType&gt;"
				+ "&lt;StartTime&gt;"
				+ "&lt;Date&gt;" + dateFormat.format(startDate) + "&lt;/Date&gt;"
				+ "&lt;Time&gt;" + timeFormat.format(startDate) + "&lt;/Time&gt;"
				+ "&lt;/StartTime&gt;"
				+ "&lt;Owner&gt;"
				+ "&lt;Number&gt;" + ownerNumber + "&lt;/Number&gt;"
				+ "&lt;Name&gt;" + ownerName + "&lt;/Name&gt;"
				+ "&lt;/Owner&gt;"
				+ "&lt;Subject&gt;" + subject + "&lt;/Subject&gt;"
				+ "&lt;EndTime&gt;"
				+ "&lt;Date&gt;" + dateFormat.format(endDate) + "&lt;/Date&gt;"
				+ "&lt;Time&gt;" + timeFormat.format(endDate) + "&lt;/Time&gt;"
				+ "&lt;/EndTime&gt;"
				+ "&lt;Location&gt;" + location + "&lt;/Location&gt;"
				+ "&lt;Body&gt;"
				+ body
				+ "&lt;/Body&gt;"
				+ "</Message>"
				+ "</u:AddMessage>"
				+ "</s:Body>"
				+ "</s:Envelope>";

		UPNPUtils.getSOAPData("http://"+host+":"+port+url, urn, xml);

		return callId;
	}

	public static void main(String[] args) {
		Samsung samsung = new Samsung("192.168.1.6");
//		samsung.showCall("+491797405835", "Robert", "865072", "Daheim");
//		samsung.showSMS("12345", "Empfaenger", "98765", "Sender", "Dies ist meine Nachricht an den Fernseher!");
		Date startDate = new GregorianCalendar(2010, 5, 24, 14, 01, 00).getTime();
		Date endDate = new GregorianCalendar(2010, 5, 24, 14, 05, 00).getTime();
		
		samsung.showSchedule(startDate, endDate, "12345", "Owner", "Betreff", "Ort", "Dies ist mein Termin!");		
	}
}

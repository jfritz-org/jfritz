/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.moonflower.jfritz.JFritz;

/**
 * @author Arno Willig
 */
public class Call {

	private JFritz jfritz;

	private CallType calltype;

	private Date calldate;

	private PhoneNumber number;

	private String route;

	private String port;

	private int duration;

	public Call(JFritz jfritz, CallType calltype, Date calldate,
			PhoneNumber number, String port, String route, int duration) {
		this.jfritz = jfritz;
		this.calltype = calltype;
		this.calldate = calldate;
		this.number = number;
		this.route = route;
		this.port = port;
		this.duration = duration;
	}

	/**
	 * @return Returns the calldate.
	 */
	public Date getCalldate() {
		return calldate;
	}

	/**
	 * @return Returns the calltype.
	 */
	public CallType getCalltype() {
		return calltype;
	}

	/**
	 * @return Returns the number.
	 */
	public PhoneNumber getPhoneNumber() {
		return number;
	}

	/**
	 * @return Returns the person the number belongs to or null.
	 */
	public Person getPerson() {
		if (number == null)
			return null;
		else
			return jfritz.getPhonebook().findPerson(number);
	}

	/**
	 * @return Returns the port.
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return Returns the route.
	 */
	public String getRoute() {
		return route;
	}

	/**
	 * @return Returns the duration.
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @return Returns CSV String
	 */
	public String toCSV() {
		SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
		String outString = "";
		switch (calltype.toInt()) {
		case 1: {
			outString = "\"Incoming\"";
			break;
		}
		case 2: {
			outString = "\"Missed\"";
			break;
		}
		case 3: {
			outString = "\"Outgoing\"";
			break;
		}
		}

		outString = outString.concat(";\"" + date.format(calldate) + "\"");

		outString = outString.concat(";\"" + time.format(calldate) + "\"");

		if (number == null)
			outString = outString.concat(";\"\"");
		else
			outString = outString.concat(";\"" + number + "\"");

		if (route == null)
			outString = outString.concat(";\"\"");
		else {
			String sipRoute= JFritz.getProperty(route);
			if (sipRoute.equals("")) {
				outString = outString.concat(";\"" + route + "\"");
			} else {
				outString = outString.concat(";\"" + sipRoute + "\"");
			}
		}

		if (port.equals("4"))
			outString = outString.concat(";\"ISDN\"");
		else if (port.equals("0"))
			outString = outString.concat(";\"FON1\"");
		else if (port.equals("1"))
			outString = outString.concat(";\"FON2\"");
		else if (port.equals("2"))
			outString = outString.concat(";\"FON3\"");
		else if (port.equals(""))
			outString = outString.concat(";\"\"");
		else
			outString = outString.concat(";\"" + port + "\"");

		outString = outString.concat(";\"" + duration + "\"");

		if (getPerson() != null) {
			outString = outString.concat(";\"" + getPerson().getFullname()
					+ "\"");
			outString = outString
					.concat(";\"" + getPerson().getStreet() + "\"");
			if (getPerson().getPostalCode().equals("")) {
				outString = outString.concat(";\""
						+ getPerson().getCity() + "\""); // city might be ""
			} else if (getPerson().getCity().equals("")) {
				outString = outString.concat(";\""
						+ getPerson().getPostalCode() + "\""); // postCode might be ""
			} else { // postCode AND city != ""
				outString = outString.concat(";\""
						+ getPerson().getPostalCode() + " "
						+ getPerson().getCity() + "\"");
			}
		} else
			outString = outString.concat(";\"\";\"\";\"\"");
		if (number != null && number.hasCallByCall()) {
			outString = outString.concat(";\"" + number.getCallByCall() + "\"");
		} else {
			outString = outString.concat(";\"\"");
		}

		return outString;
	}

	/**
	 * @return Returns XML String
	 */
	public String toXML() {
		String sep = System.getProperty("line.separator", "\n");
		String output = "";
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		output = ("<entry calltype=\"" + calltype.toString() + "\">" + sep);
		output = output + ("\t<date>" + df.format(calldate) + "</date>" + sep);
		if (number != null) {
			if (number.getCallByCall().length() > 0) {
				output = output
						+ ("\t<caller callbycall=\"" + number.getCallByCall()
								+ "\">" + number.getIntNumber() + "</caller>" + sep);
			} else {
				output = output
						+ ("\t<caller>" + number.getIntNumber() + "</caller>" + sep);
			}
		}
		if (!port.equals(""))
			output = output + ("\t<port>" + port + "</port>" + sep);
		if (!route.equals(""))
			output = output + ("\t<route>" + route + "</route>" + sep);
		if (duration > 0)
			output = output + ("\t<duration>" + duration + "</duration>" + sep);
		output = output + ("</entry>");
		return output;
	}

	public String toString() {
		return toCSV();
	}

}

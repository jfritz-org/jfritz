/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 */
public class Call {

	public final static int ROUTE_FIXED_NETWORK = 0;

	public final static int ROUTE_SIP = 1;

	private CallType calltype;

	private Date calldate;

	private PhoneNumber number;

	private String route;

	private int route_type;

	private String port;

	private int duration;

	private double cost = -1;

	private Person person;

	private String comment = ""; //$NON-NLS-1$

	public Call(CallType calltype, Date calldate, PhoneNumber number,
			String port, String route, int duration) {
		this.calltype = calltype;
		this.calldate = calldate;
		this.number = number;
		this.route = route;

		// fix so that an empty number doesnt get linked to an empty entry in
		// the telephone book
		if ((this.number != null) && this.number.toString().equals("")) {
			this.number = null;
		}

		// Parse the SIP Provider and save it correctly
		if ( route.contains("@")) {
			this.route_type = ROUTE_SIP;
		} else if (this.route.startsWith("Internet: ")) {
			Enumeration en = JFritz.getSIPProviderTableModel()
			.getProviderList().elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getNumber().equals(this.route.substring(10))) {
					this.route = sipProvider.toString();
					this.route_type = ROUTE_SIP;
					break;
				}
			}
		} else {
			route_type = ROUTE_FIXED_NETWORK;
		}

		this.port = port;
		this.duration = duration;
	}

	public Call(CallType calltype, Date calldate, PhoneNumber number,
			String port, String route, int duration, String comment) {
		this(calltype, calldate, number, port, route, duration);
		this.comment = comment;
	}

	/**
	 * This function compares the contents of the current call object with the
	 * contents of the parameter call object
	 *
	 * @author Brian Jensen
	 *
	 * @param call,
	 *            the call value to be compared with
	 * @return a boolean value indicating if this call equals the call given as
	 *         parameter
	 */
	public boolean equals(Object call) {
		Call call2;
		if (!(call instanceof Call)) {
			return false;
		}
		call2 = (Call) call;
		// prepare the two objects for comparing
		String nr1 = "", nr2 = ""; //$NON-NLS-1$,  //$NON-NLS-2$
		if (this.getPhoneNumber() != null) {
			nr1 = this.getPhoneNumber().getFullNumber();
		}
		if (call2.getPhoneNumber() != null) {
			nr2 = call2.getPhoneNumber().getFullNumber();
		}
		String route1 = "", route2 = ""; //$NON-NLS-1$,  //$NON-NLS-2$
		if (this.getRoute() != null) {
			route1 = this.getRoute();
		}
		if (call2.getRoute() != null) {
			route2 = call2.getRoute();
		}

		if ( this.getCalldate().equals(call2.getCalldate())
				&& (nr1).equals(nr2)
				&& (this.getPort().equals(call2.getPort()))
				&& (this.getDuration() == call2.getDuration())
				&& (this.getCalltype().toInt() == call2.getCalltype().toInt())
				&& (route1.equals(route2))) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @return Returns the calldate.
	 */
	public Date getCalldate() {
		return new Date(calldate.getTime()); // FIXME mal checken, wo man das
												// noch alles so machen sollte
	}

	/**
	 * @return Returns the calltype.
	 */
	public CallType getCalltype() {
		return calltype;
	}

	/**
	 * Set call type
	 * @param callType
	 */
	public void setCallType(CallType callType) {
		calltype = callType;
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Returns cost of call
	 *
	 * @return cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @return Returns the duration.
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @return Returns the person the number belongs to or null.
	 */
	public Person getPerson() {
		if (person == null) {
			return null;
		} else {
			return person;
		}
	}

	/**
	 * @return Returns the number.
	 */
	public PhoneNumber getPhoneNumber() {
		return number;
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
	 * Returns the type (UNDEFINED, POTS, ISDN, SIP) of route
	 *
	 * @return type of route
	 */
	public int getRouteType() {
		return route_type;
	}

	public int hashCode() {
		String s = "";
		s += this.getPhoneNumber().getFullNumber();
		s += this.getPort();
		s += this.getDuration();
		s += this.getCalltype().toInt();
		s += this.getRoute();
		return s.hashCode();
	}

	/**
	 * Set call date
	 */
	public void setCalldate(Date calldate) {
		this.calldate = calldate;
	}

	/**
	 * @param comment
	 *            The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Set cost of call
	 *
	 * @param cost
	 *            The cost of call
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Set duration of call
	 *
	 * @param duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setPerson(Person person) {
		this.person = person;

	}

	/**
	 * Set the port
	 *
	 * @param newPort
	 */
	public void setPort(String newPort) {
		port = newPort;
	}

	/**
	 * @return Returns CSV String
	 */
	public String toCSV() {
		SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
		SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
		String outString = ""; //$NON-NLS-1$

		// type
		switch (calltype.toInt()) {
		case 1: {
			outString = "\"Incoming\""; //$NON-NLS-1$
			break;
		}
		case 2: {
			outString = "\"Missed\""; //$NON-NLS-1$
			break;
		}
		case 3: {
			outString = "\"Outgoing\""; //$NON-NLS-1$
			break;
		}
		}

		// date
		outString = outString.concat(";\"" + date.format(calldate) + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// time
		outString = outString.concat(";\"" + time.format(calldate) + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// number
		if (number == null) {
			outString = outString.concat(";\"\""); //$NON-NLS-1$
		} else {
			outString = outString.concat(";\"" + number + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		}

		// route
		if (route == null) {
			outString = outString.concat(";\"\""); //$NON-NLS-1$
		} else {
			// String sipRoute = ""; //$NON-NLS-1$
			String convertedRoute = route;
			if (route.startsWith("SIP")) { // FIXME old code
				Enumeration en = JFritz.getSIPProviderTableModel()
						.getProviderList().elements();
				while (en.hasMoreElements()) {
					SipProvider sipProvider = (SipProvider) en.nextElement();
					if (route.substring(3).equals(
							String.valueOf(sipProvider.getProviderID()))) {
						convertedRoute = sipProvider.toString();
						break;
					}
				}
			}

			/*
			 * This is the old format if (route.startsWith("SIP")) {
			 * //$NON-NLS-1$ Enumeration en = JFritz.getSIPProviderTableModel()
			 * .getProviderList().elements(); while (en.hasMoreElements()) {
			 * SipProvider sipProvider = (SipProvider) en.nextElement(); if
			 * (sipProvider.getProviderID() == Integer.parseInt(route
			 * .substring(3))) { sipRoute = sipProvider.toString(); } } }
			 *
			 * if (sipRoute.equals("")) { //$NON-NLS-1$ outString =
			 * outString.concat(";\"" + route + "\""); //$NON-NLS-1$,
			 * //$NON-NLS-2$ } else { outString = outString.concat(";\"" +
			 * sipRoute + "\""); //$NON-NLS-1$, //$NON-NLS-2$ }
			 */

			outString = outString.concat(";\"" + convertedRoute + "\"");

		}

		// port
		if (port.equals("4")) {
			outString = outString.concat(";\"ISDN\""); //$NON-NLS-1$
		} else if (port.equals("0")) {
			outString = outString.concat(";\"FON1\""); //$NON-NLS-1$
		} else if (port.equals("1")) {
			outString = outString.concat(";\"FON2\""); //$NON-NLS-1$
		} else if (port.equals("2")) {
			outString = outString.concat(";\"FON3\""); //$NON-NLS-1$
		} else if (port.equals("32")) {
			outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
		} else if (port.equals("33")) {
			outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
		} else if (port.equals("34")) {
			outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
		} else if (port.equals("35")) {
			outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
		} else if (port.equals("36")) {
			outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
		} else if (port.equals("")) {
			outString = outString.concat(";\"\""); //$NON-NLS-1$
		} else {
			outString = outString.concat(";\"" + port + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		}

		// duration
		outString = outString.concat(";\"" + duration + "\""); //$NON-NLS-1$, //$NON-NLS-2$

		// address
		if (person != null) {
			outString = outString.concat(";\"" + person.getFullname() //$NON-NLS-1$
					+ "\""); //$NON-NLS-1$
			outString = outString
					.concat(";\"" + person.getStreet() + "\""); //$NON-NLS-1$, //$NON-NLS-2$
			if (person.getPostalCode().equals("")) { //$NON-NLS-1$
				outString = outString.concat(";\"" + person.getCity() //$NON-NLS-1$
						+ "\""); // city might be "" //$NON-NLS-1$
			} else if (person.getCity().equals("")) { //$NON-NLS-1$
				outString = outString.concat(";\"" //$NON-NLS-1$
						+ person.getPostalCode() + "\""); //$NON-NLS-1$
				// postCode might be ""
			} else { // postCode AND city !equals("")
				outString = outString.concat(";\"" //$NON-NLS-1$
						+ person.getPostalCode() + " " //$NON-NLS-1$
						+ person.getCity() + "\""); //$NON-NLS-1$
			}
		} else {
			outString = outString.concat(";\"\";\"\";\"\""); //$NON-NLS-1$
		}

		// CallByCall
		if ((number != null) && number.hasCallByCall()) {
			outString = outString.concat(";\"" + number.getCallByCall() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		} else {
			outString = outString.concat(";\"\""); //$NON-NLS-1$
		}

		// comment
		outString = outString.concat(";\"" + comment + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		return outString;
	}

	public String toString() {
		return toCSV();
	}

	/**
	 * @return Returns XML String
	 */
	public String toXML() {
		String sep = System.getProperty("line.separator", "\n"); //$NON-NLS-1$,  //$NON-NLS-2$
		String output = ""; //$NON-NLS-1$
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
		output = ("<entry calltype=\"" + calltype.toString() + "\">" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		output = output + ("\t<date>" + df.format(calldate) + "</date>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		if (number != null) {
			if (number.getCallByCall().length() > 0) {
				output = output
						+ ("\t<caller callbycall=\"" + number.getCallByCall() //$NON-NLS-1$
								+ "\">" + number.getIntNumber() + "</caller>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
			} else {
				output = output
						+ ("\t<caller>" + number.getIntNumber() + "</caller>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
			}
		}
		if (!port.equals("")) {
			output = output
					+ ("\t<port>" + JFritzUtils.convertSpecialChars(port) + "</port>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		}

		if (!route.equals("")) { //$NON-NLS-1$
			String convertedRoute = route;
			if (route.startsWith("SIP")) {
				Enumeration en = JFritz.getSIPProviderTableModel()
						.getProviderList().elements();
				while (en.hasMoreElements()) {
					SipProvider sipProvider = (SipProvider) en.nextElement();
					if (route.substring(3).equals(
							String.valueOf(sipProvider.getProviderID()))) {
						convertedRoute = sipProvider.toString();
						break;
					}
				}
			}
			output = output
					+ ("\t<route>" + JFritzUtils.convertSpecialChars(convertedRoute) + "</route>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		if (duration > 0) {
			output = output + ("\t<duration>" + duration + "</duration>" + sep); //$NON-NLS-1$, //$NON-NLS-2$
		}

		output = output
				+ ("\t<comment>" + JFritzUtils.convertSpecialChars(comment) + "</comment>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		output = output + ("</entry>"); //$NON-NLS-1$
		return output;
	}
}
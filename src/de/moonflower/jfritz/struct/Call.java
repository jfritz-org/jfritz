/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 */
public class Call implements Serializable {

	public static final long serialVersionUID = 101;

	public final static int ROUTE_FIXED_NETWORK = 0;

	public final static int ROUTE_SIP = 1;

	private CallType calltype;

	private Date calldate;

	private PhoneNumberOld number;

	private String route;

	private int routeType;

	private Port port;

	private int duration;

	private double cost = -1;

	private String comment = ""; //$NON-NLS-1$

	public Call(final CallType calltype, final Date calldate, final PhoneNumberOld number,
			final Port port, final String route, final int duration) {
		this.calltype = calltype;
		this.calldate = calldate;
		this.number = number;
		setRoute(route);
		this.port = port;
		this.duration = duration;

		if (port == null)
		{
			this.port = new Port(-1, "", "-1", "-1");
		}

		// fix so that an empty number doesnt get linked to an empty entry in
		// the telephone book
		if ((this.number != null) && this.number.toString().equals("")) {
			this.number = null;
		}
	}

	public Call(final CallType calltype, final Date calldate, final PhoneNumberOld number,
			final Port port, final String route, final int duration, final String comment) {
		this(calltype, calldate, number, port, route, duration);
		this.comment = comment;
	}

	/**
	 * This function compares the current call object with the
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
				&& (route1.equals(route2)))
		{
			return true;
		}
		else
		{
			return false;
		}

	}

	//clones this object
	public Call clone(){
		return new Call(calltype, calldate, number,
				port, route, duration, comment);

	}

	/**
	 * @return Returns the calldate.
	 */
	public Date getCalldate() {
		if (calldate != null) {
			return new Date(calldate.getTime()); // FIXME mal checken, wo man das
												// noch alles so machen sollte
		}
		return null;
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
	public void setCallType(final CallType callType) {
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
	 * @return Returns the number.
	 */
	public PhoneNumberOld getPhoneNumber() {
		return number;
	}

	public void setPhoneNumber(final PhoneNumberOld number) {
		this.number = number;
	}

	/**
	 * @return Returns the port.
	 */
	public Port getPort() {
		return port;
	}

	/**
	 * @return Returns the route.
	 */
	public String getRoute() {
		return route;
	}

	public void setRoute(final String route) {
		this.route = route;

		// Parse the SIP Provider and save it correctly
		if ( route.contains("@")) {
			this.routeType = ROUTE_SIP;
		} else if (this.route.startsWith("Internet: ")) {
			this.routeType = ROUTE_SIP;
		} else {
			routeType = ROUTE_FIXED_NETWORK;
		}
	}

	/**
	 * Returns the type (UNDEFINED, POTS, ISDN, SIP) of route
	 *
	 * @return type of route
	 */
	public int getRouteType() {
		return routeType;
	}

	public int hashCode() {
		StringBuffer sBuffer = new StringBuffer("");
		sBuffer.append(this.getPhoneNumber().getFullNumber());
		sBuffer.append(this.getPort());
		sBuffer.append(this.getDuration());
		sBuffer.append(this.getCalltype().toInt());
		sBuffer.append(this.getRoute());
		return sBuffer.hashCode();
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

	/**
	 * Set the port
	 *
	 * @param newPort
	 */
	public void setPort(Port newPort) {
		port = newPort;
	}

	/**
	 * @return Returns CSV String
	 */
	public String toCSV() {
		SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
		SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
		StringBuffer outString = new StringBuffer(""); //$NON-NLS-1$

		// type
		switch (calltype.toInt()) {
		case 1: {
			outString.append("\"Incoming\""); //$NON-NLS-1$
			break;
		}
		case 2: {
			outString.append("\"Missed\""); //$NON-NLS-1$
			break;
		}
		case 3: {
			outString.append("\"Outgoing\""); //$NON-NLS-1$
			break;
		}
		default: {
			outString.append("\"ERROR\"");
		}
		}

		// date
		outString = outString.append(";\"" + date.format(calldate) + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// time
		outString = outString.append(";\"" + time.format(calldate) + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		// number
		if (number == null) {
			outString = outString.append(";\"\""); //$NON-NLS-1$
		} else {
			outString = outString.append(";\"" + number + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		}

		// route
		if (route == null) {
			outString = outString.append(";\"\""); //$NON-NLS-1$
		} else {
			outString = outString.append(";\"" + route + "\"");
		}
		if (port != null)
		{
			outString = outString.append(";\"" + port.getName() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		else
		{
			outString = outString.append(";");
		}
		// duration
		outString = outString.append(";\"" + duration + "\""); //$NON-NLS-1$, //$NON-NLS-2$

		// address
		Person person = null;
		if (JFritz.getPhonebook() != null)
		{
			person = JFritz.getPhonebook().findPerson(this);
		}

		if (person == null) {
			outString = outString.append(";\"\";\"\";\"\""); //$NON-NLS-1$
		} else {
			outString = outString.append(";\"" + person.getFullname() //$NON-NLS-1$
					+ "\""); //$NON-NLS-1$
			outString = outString
					.append(";\"" + person.getStreet() + "\""); //$NON-NLS-1$, //$NON-NLS-2$
			if (person.getPostalCode().equals("")) { //$NON-NLS-1$
				outString = outString.append(";\"" + person.getCity() //$NON-NLS-1$
						+ "\""); // city might be "" //$NON-NLS-1$
			} else if (person.getCity().equals("")) { //$NON-NLS-1$
				outString = outString.append(";\"" //$NON-NLS-1$
						+ person.getPostalCode() + "\""); //$NON-NLS-1$
				// postCode might be ""
			} else { // postCode AND city !equals("")
				outString = outString.append(";\"" //$NON-NLS-1$
						+ person.getPostalCode() + " " //$NON-NLS-1$
						+ person.getCity() + "\""); //$NON-NLS-1$
			}
		}

		// CallByCall
		if ((number != null) && number.hasCallByCall()) {
			outString = outString.append(";\"" + number.getCallByCall() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		} else {
			outString = outString.append(";\"\""); //$NON-NLS-1$
		}

		// comment
		outString = outString.append(";\"" + comment + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

		return outString.toString();
	}

	public String toString() {
		return toCSV();
	}

	/**
	 * @return Returns XML String
	 */
	public String toXML() {
		String sep = System.getProperty("line.separator", "\n"); //$NON-NLS-1$,  //$NON-NLS-2$
		StringBuffer output = new StringBuffer(139); //$NON-NLS-1$
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
		output.append("<entry calltype=\"" + calltype.toString() + "\">" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		output.append("\t<date>" + dateFormat.format(calldate) + "</date>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		if (number != null) {
			if (number.getCallByCall().length() > 0) {
				output.append("\t<caller callbycall=\"" + number.getCallByCall() //$NON-NLS-1$
								+ "\">" + number.getIntNumber() + "</caller>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
			} else {
				output.append("\t<caller>" + number.getIntNumber() + "</caller>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
			}
		}
		if (port == null)
		{
			output.append("\t<port>Unknown</port>" + sep);
		}
		else
		{
			if (!port.getName().equals("")) {
				output.append("\t<port>" + JFritzUtils.convertSpecialChars(port.getName()) + "</port>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
			}
		}

		if (!route.equals("")) { //$NON-NLS-1$
			output.append("\t<route>" + JFritzUtils.convertSpecialChars(route) + "</route>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		if (duration > 0) {
			output.append("\t<duration>" + duration + "</duration>" + sep); //$NON-NLS-1$, //$NON-NLS-2$
		}

		output.append("\t<comment>" + JFritzUtils.convertSpecialChars(comment) + "</comment>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
		output.append("</entry>"); //$NON-NLS-1$
		return output.toString();
	}
}
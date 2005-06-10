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
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return "\""+calltype.toInt() + "\";\"" + df.format(calldate) + "\";\"" + number
				+ "\";\"" + route + "\";\"" + port + "\";\"" + duration+"\"";
	}

	public String toString() {
		return toCSV();
	}

}

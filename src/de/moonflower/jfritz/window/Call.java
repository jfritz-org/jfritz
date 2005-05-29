/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.window;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;

/**
 * @author Arno Willig row.add(symbol); row.add(datum); row.add(number);
 *         row.add(participant); row.add(port);
 */
public class Call {

	private JFritz jfritz;

	private CallType calltype;

	private Date calldate;

	private String number;

	private String route;

	private String port;

	private int duration;

	public Call(JFritz jfritz, CallType calltype, Date calldate, String number,
			String port, String route, int duration) {
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
	public String getNumber() {
		return number;
	}

	/**
	 * @return Returns the number.
	 */
	public String getParticipant() {
		String areanumber = JFritzUtils.createAreaNumber(number, jfritz
				.getProperties().getProperty("country.prefix"), jfritz
				.getProperties().getProperty("country.code"), jfritz
				.getProperties().getProperty("area.prefix"), jfritz
				.getProperties().getProperty("area.code"));
		return jfritz.getParticipants().getProperty(areanumber, "");
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
		return calltype.toInt() + ";" + df.format(calldate) + ";" + number
				+ ";" + route + ";" + port + ";" + duration;
	}

	public String toString() {
		return toCSV();
	}

	public boolean isMobileCall() {
		String provider = ReverseLookup.getMobileProvider(number);
		return (!provider.equals(""));
	}
}

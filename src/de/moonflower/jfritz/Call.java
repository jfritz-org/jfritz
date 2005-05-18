/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz;

import java.util.Date;

/**
 * @author Arno Willig
 *			row.add(symbol);
			row.add(datum);
			row.add(number);
			row.add(participant);
			row.add(port);
 */
public class Call {

	CallType calltype;
	Date calldate;
	String number;
	String route;
	String port;
	int duration;

	public Call(CallType calltype,Date calldate,String number, String port, String route, int duration) {
		this.calltype = calltype;
		this.calldate = calldate;
		this.number = number;
		this.route = route;
		this.port = port;
		this.duration = duration;
	}

/*
	public String getParticipantFromNumber(String number, Properties properties) {
		String areanumber = FritzBox.create_area_number(number, properties
				.getProperty("country.prefix"), properties
				.getProperty("area.prefix"), properties
				.getProperty("area.code"));
		String participant = participants.getProperty(areanumber, "");
		if (!number.equals("")) {
			if (participant.equals("")) {
				// TODO participant = ReverseLookup.lookup( number );
				//				System.out.println("Reverse-Lookup for " + number+":
				// "+participant);
				if (!participant.equals("")) {
					participants.setProperty(areanumber, participant);
				}
			}
		}
		return participant;
	}
*/
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
}

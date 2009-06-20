package de.moonflower.jfritz.JFritzEvent.events;

import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventParameter;

public class MessageEvent implements JFritzEvent {

	private static JFritzEventParameter[] parameter = {
		new JFritzEventParameter("Text", "$TEXT"),
		new JFritzEventParameter("ID", "$ID")};

	private String text;

	public MessageEvent() {
		text = "";
	}

	public MessageEvent(String text) {
		this.text = text;
	}

	public String getName() {
		return "Message Event";
	}

	public String toString() {
		return getName();
	}

	public JFritzEventParameter getParameter(byte i) {
		return parameter[i];
	}

	public int getParameterCount() {
		return parameter.length;
	}

}

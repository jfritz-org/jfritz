package de.moonflower.jfritz.JFritzEvent.actions;

import javax.swing.JPanel;

import org.jdom.Element;


public class TrayMessageAction implements JFritzAction {

	private String description;

	public TrayMessageAction() {
		this.description = "";
	}

	public JPanel getConfigPanel() {
		return null;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return "Tray-Message";
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return getName();
	}

	public void run() {
		// TODO Auto-generated method stub

	}

	public TrayMessageAction clone() {
	        try {
	            return (TrayMessageAction) super.clone();
	        } catch (CloneNotSupportedException cnse) {
	            cnse.printStackTrace();
	            return null;
	        }
	}

	public void parameterClicked(Object o) {

	}

	public void loadSettings(Element settings) {
		// TODO Auto-generated method stub

	}

	public Element saveSettings() {
		// TODO Auto-generated method stub
		return null;
	}
}

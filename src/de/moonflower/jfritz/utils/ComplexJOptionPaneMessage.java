package de.moonflower.jfritz.utils;

import javax.swing.JCheckBox;

import de.moonflower.jfritz.Main;

public class ComplexJOptionPaneMessage {

	private String message = "";

	private String property = "";

	private JCheckBox checkBox = null;

	public ComplexJOptionPaneMessage(String property, String message) {
		this.message = message;
		this.property = property;
		checkBox = new JCheckBox(Main.getMessage("infoDialog_showAgain"));
		checkBox.setSelected(JFritzUtils.parseBoolean(Main.getProperty(property)));
	}

	public Object[] getComponents() {
		Object[] components = new Object[2];
		components[0] = message;
		components[1] = checkBox;

		return components;
	}

	public void saveProperty() {
		Main.setStateProperty(property, Boolean
				.toString(checkBox.isSelected()));
	}

	public boolean showDialogEnabled() {
		return !JFritzUtils.parseBoolean(Main.getStateProperty(property));
	}
}

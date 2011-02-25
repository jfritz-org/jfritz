package de.moonflower.jfritz.utils;

import javax.swing.JCheckBox;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;

public class ComplexJOptionPaneMessage {

	private String message = "";

	private String property = "";

	private JCheckBox checkBox = null;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ComplexJOptionPaneMessage(String property, String message) {
		this.message = message;
		this.property = property;
		checkBox = new JCheckBox(messages.getMessage("infoDialog_showAgain"));
		checkBox.setSelected(JFritzUtils.parseBoolean(properties.getProperty(property)));
	}

	public Object[] getComponents() {
		Object[] components = new Object[2];
		components[0] = message;
		components[1] = checkBox;

		return components;
	}

	public void saveProperty() {
		properties.setStateProperty(property, Boolean
				.toString(checkBox.isSelected()));
	}

	public boolean showDialogEnabled() {
		return !JFritzUtils.parseBoolean(properties.getStateProperty(property));
	}
}

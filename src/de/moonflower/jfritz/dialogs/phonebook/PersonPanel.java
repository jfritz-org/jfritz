/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.GridLayout;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.moonflower.jfritz.JFritz;

/**
 * @author Arno Willig
 *
 */
public class PersonPanel extends JPanel {

	private JFritz jfritz;
	private ResourceBundle messages;

	private JTextField tfFirstName, tfMiddleName,tfLastName;
	/**
	 *
	 */
	public PersonPanel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		this.messages = jfritz.getMessages();
		drawPanel();
	}

	private void drawPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2));
		JButton okButton = new JButton("Übernehmen"); // TODO I18N
		JButton cancelButton = new JButton("Abbruch"); // TODO I18N
		JLabel label = new JLabel(messages.getString("firstName") + ": ");
		buttonPanel.add(label);
		tfFirstName = new JTextField();
		buttonPanel.add(tfFirstName);
		label = new JLabel(messages.getString("middleName") + ": ");
		buttonPanel.add(label);
		tfMiddleName = new JTextField();
		buttonPanel.add(tfMiddleName);
		label = new JLabel(messages.getString("lastName") + ": ");
		buttonPanel.add(label);
		tfLastName = new JTextField();
		buttonPanel.add(tfLastName);
		label = new JLabel(messages.getString("street") + ": ");
		label = new JLabel(messages.getString("postalCode") + ": ");
		label = new JLabel(messages.getString("city") + ": ");
		label = new JLabel(messages.getString("homeTelephoneNumber") + ": ");
		label = new JLabel(messages.getString("mobileTelephoneNumber") + ": ");
		label = new JLabel(messages.getString("businessTelephoneNumber") + ": ");
		label = new JLabel(messages.getString("otherTelephoneNumber") + ": ");
		// this.add(buttonPanel);

		// this.add(okButton);
		// this.add(cancelButton);
	}
}

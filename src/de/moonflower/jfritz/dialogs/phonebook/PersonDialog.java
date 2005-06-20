/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 *
 */
public class PersonDialog extends JDialog implements ActionListener {

	private JFritz jfritz;

	private Person person;

	private PersonPanel personPanel;

	JButton okButton, cancelButton;

	private boolean pressed_OK = false;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public PersonDialog(JFritz jfritz, Person person) throws HeadlessException {
		super();
		this.jfritz = jfritz;
		this.person = new Person(person);
		if (this.person == null)
			person = new Person();
		this.setLocationRelativeTo(jfritz.getJframe());
		drawDialog();
	}

	private void drawDialog() {
		super.dialogInit();
		setTitle("Person editieren");
		setModal(true);
		getContentPane().setLayout(new BorderLayout());

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();

		// Top Pane
		JLabel label = new JLabel("Person editieren");
		topPane.add(label);

		// Main Pane
		personPanel = new PersonPanel(jfritz, person);

		// Bottom Pane
		okButton = new JButton("Okay");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);

		cancelButton = new JButton("Abbruch");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		bottomPane.add(okButton);
		bottomPane.add(cancelButton);

		getContentPane().add(topPane, BorderLayout.NORTH);
		getContentPane().add(personPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);
		setSize(new Dimension(350, 400));

	}

	public boolean okPressed() {
		return pressed_OK;
	}

	public boolean showDialog() {
		setVisible(true);
		person.setFirstName(personPanel.getFirstName());
		person.setCompany(personPanel.getCompany());
		person.setLastName(personPanel.getLastName());
		person.setCity(personPanel.getCity());
		person.setEmailAddress(personPanel.getEmail());
		person.setStreet(personPanel.getStreet());
		person.setPostalCode(personPanel.getPostalCode());
		return okPressed();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			pressed_OK = true;
			setVisible(false);
		} else if (e.getActionCommand().equals("cancel")) {
			pressed_OK = false;
			setVisible(false);
		}
	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}
}

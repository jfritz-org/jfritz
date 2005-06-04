/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

import de.moonflower.jfritz.callerlist.PersonCellEditor;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 *
 */
public class PersonEditorPanel extends JComponent implements ActionListener {

	private PersonCellEditor editor;

	private Person person;

	private JTextField input;

	private JButton button;

	/**
	 *
	 */
	public PersonEditorPanel(PersonCellEditor editor) {
		super();
		this.editor = editor;
		drawPanel();
	}

	private void drawPanel() {
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/modify.png")));

		setLayout(new BorderLayout());
		input = new JTextField("  ");
		input.setEditable(false);
		button = new JButton();
		button.setIcon(icon);
		button.setFocusable(false);
		button.addActionListener(this);
		input.setBackground(new Color(127, 255, 255));
		input.setFocusable(true);
		add(button, BorderLayout.EAST);
		add(input, BorderLayout.CENTER);
	}

	public void setText(String text) {
		input.setText(text);
	}

	public String getText() {
		return input.getText();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		PersonDialog p = new PersonDialog(editor.getJfritz(), person);
		if (p.showDialog()) {
			person = p.getPerson();
			input.setText(person.getFullname());
			editor.stopCellEditing();
		}
		p.dispose();

	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}

	/**
	 * @param person
	 *            The person to set.
	 */
	public final void setPerson(Person person) {
		this.person = person;
	}
}
